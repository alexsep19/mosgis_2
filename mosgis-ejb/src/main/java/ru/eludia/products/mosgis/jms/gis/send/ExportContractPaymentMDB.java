package ru.eludia.products.mosgis.jms.gis.send;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Get;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.ContractObject;
import ru.eludia.products.mosgis.db.model.tables.ContractPayment;
import ru.eludia.products.mosgis.db.model.tables.ContractPaymentFile;
import ru.eludia.products.mosgis.db.model.tables.ContractPaymentFileLog;
import ru.eludia.products.mosgis.db.model.tables.ContractPaymentLog;
import ru.eludia.products.mosgis.db.model.tables.OrganizationWork;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.ServicePayment;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.ejb.wsc.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inHouseContractPaymentsQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportContractPaymentMDB extends UUIDMDB<ContractPaymentLog> {
    
    @EJB
    WsGisHouseManagementClient wsGisHouseManagementClient;
    
    @Resource (mappedName = "mosgis.outExportHouseContractPaymentsQueue")
    Queue outExportHouseContractPaymentsQueue;
    
    @Resource (mappedName = "mosgis.inHouseContractPaymentFilesQueue")
    Queue inHouseContractPaymentFilesQueue;
            
    @EJB
    protected UUIDPublisher UUIDPublisher;
    
    protected Get get (UUID uuid) {        
        
        final MosGisModel m = ModelHolder.getModel ();
        
        try (DB db = m.getDb ()) {
                        
            return (Get) m
                .get (getTable (), uuid, "*")
                .toOne (ContractPayment.class, "AS ctr", "id_ctr_status").on ()
                .toMaybeOne (ContractPaymentFile.class, "AS doc", "*").on ()
                .toMaybeOne (ContractPaymentFileLog.class, "AS doc_log", "ts_start_sending", "err_text").on ("doc.id_log=doc_log.uuid")
                .toOne (Contract.class, "AS ctrt", "contractversionguid").on ()
                .toOne (VocOrganization.class, "AS org", "orgppaguid").on ("ctrt.uuid_org=org.uuid")
                .toMaybeOne (ContractObject.class, "AS o", "*").on ("ctr.uuid_contract_object=o.uuid")
            ;
            
        }
        catch (SQLException ex) {
            throw new IllegalStateException (ex);
        }
                
    }
        
    AckRequest.Ack invoke (DB db, ContractPayment.Action action, UUID messageGUID,  Map<String, Object> r) throws Fault, SQLException {
            
        UUID orgPPAGuid = getOrgPPAGUID (r);
            
        switch (action) {
            case PLACING:     return wsGisHouseManagementClient.placeContractPaymentsInfo (orgPPAGuid, messageGUID, r);
//            case EDITING:     return wsGisHouseManagementClient.editContractPaymentData      (orgPPAGuid, messageGUID, r);
//            case TERMINATION: return wsGisHouseManagementClient.terminateContractPaymentData (orgPPAGuid, messageGUID, r);
//            case ANNULMENT:   return wsGisHouseManagementClient.annulContractPaymentData     (orgPPAGuid, messageGUID, r);
//            case ROLLOVER:    return wsGisHouseManagementClient.rolloverContractPaymentData  (orgPPAGuid, messageGUID, r);
//            case RELOADING:   return wsGisHouseManagementClient.exportContractPaymentData    (orgPPAGuid, messageGUID, Collections.singletonList ((UUID) r.get ("ctr.charterversionguid")));
            default: throw new IllegalArgumentException ("No action implemented for " + action);
        }

    }

    private UUID getOrgPPAGUID (Map<String, Object> r) {
        final UUID orgPPAGuid = (UUID) r.get ("org.orgppaguid");
        return orgPPAGuid;
    }

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        VocGisStatus.i status = VocGisStatus.i.forId (r.get ("ctr.id_ctr_status"));
        
        ContractPayment.Action action = ContractPayment.Action.forStatus (status);
        
        if (action == null) {
            logger.warning ("No action is implemented for " + status);
            return;
        }
        
        final UUID uuidObject = (UUID) r.get ("uuid_object");

        Model m = db.getModel ();        

        r.put ("svc", db.getList (m
            .select (ServicePayment.class, "*")
            .where  (ServicePayment.c.UUID_CONTRACT_PAYMENT.lc (), uuidObject)
            .and    (EnTable.c.IS_DELETED.lc (), 0)
            .toOne (OrganizationWork.class, "AS w", "elementguid", "uniquenumber").on ()
        ));
        
        try {
            
            if (DB.ok (r.get ("uuid_file"))) {

                final Object err = r.get ("doc_log.err_text");
                if (DB.ok (err)) throw new Exception (err.toString ());
                
                final Object ots = r.get ("doc_log.ts_start_sending");            
                
                if (!DB.ok (ots)) {
                    
                    db.update (ContractPaymentFileLog.class, DB.HASH (
                        "uuid",              r.get ("doc.id_log"),
                        "ts_start_sending",  NOW
                    ));
                    
                    UUIDPublisher.publish (inHouseContractPaymentFilesQueue, (UUID) r.get ("uuid_file"));
                    
                    UUIDPublisher.publish (getOwnQueue (), uuid);
                    
                    logger.info ("Sending file, bailing out");
                    
                    return;
                    
                }
                
                if (!DB.ok (r.get ("doc.attachmentguid"))) {
                    
                    UUIDPublisher.publish (getOwnQueue (), uuid);
                    
                    logger.info ("Waiting for " + r.get ("doc.label") + " to be uploaded...");
                    
                }
                
            }

            AckRequest.Ack ack = invoke (db, action, uuid, r);

            db.begin ();

                db.update (OutSoap.class, DB.HASH (
                    "uuid",     uuid,
                    "uuid_ack", ack.getMessageGUID ()
                ));

                db.update (getTable (), DB.HASH (
                    "uuid",          uuid,
                    "uuid_out_soap", uuid,
                    "uuid_message",  ack.getMessageGUID ()
                ));

                db.update (ContractPayment.class, DB.HASH (
                    "uuid",          uuidObject,
                    "uuid_out_soap", uuid,
                    "id_ctr_status", action.getNextStatus ().getId ()
                ));

            db.commit ();

            UUIDPublisher.publish (getQueue (action), ack.getRequesterMessageGUID ());            

        }
        catch (Fault ex) {

            logger.log (Level.SEVERE, "Can't place charter", ex);

            ru.gosuslugi.dom.schema.integration.base.Fault faultInfo = ex.getFaultInfo ();

            db.begin ();

                db.update (OutSoap.class, HASH (
                    "uuid", uuid,
                    "id_status", DONE.getId (),
                    "is_failed", 1,
                    "err_code",  faultInfo.getErrorCode (),
                    "err_text",  faultInfo.getErrorMessage ()
                ));

                db.update (getTable (), DB.HASH (
                    "uuid",          uuid,
                    "uuid_out_soap", uuid
                ));

                db.update (ContractPayment.class, DB.HASH (
                    "uuid",              r.get ("uuid_object"),
                    "uuid_out_soap",     uuid,
                    "id_ctr_status",     action.getFailStatus ().getId ()
                ));

            db.commit ();

            return;

        }
        catch (Exception ex) {
            
            logger.log (Level.SEVERE, "Cannot invoke WS", ex);
            
            db.begin ();

                db.upsert (OutSoap.class, HASH (
                    "uuid", uuid,
                    "svc",  getClass ().getName (),
                    "op",   action.toString (),
                    "is_out",  1,
                    "id_status", DONE.getId (),
                    "is_failed", 1,
                    "err_code",  "0",
                    "err_text",  ex.getMessage ()
                ));

                db.update (getTable (), DB.HASH (
                    "uuid",          uuid,
                    "uuid_out_soap", uuid
                ));

                db.update (ContractPayment.class, DB.HASH (
                    "uuid",              r.get ("uuid_object"),
                    "uuid_out_soap",     uuid,
                    "id_ctr_status",     action.getFailStatus ().getId ()
                ));

            db.commit ();

            return;
            
        }
        
    }
    
    Queue getQueue (ContractPayment.Action action) {
        
        switch (action) {
//            case REFRESHING: return outExportHouseContractPaymentStatusQueue;
//            case RELOADING:  return outExportHouseContractPaymentsDataQueue;
            default:         return outExportHouseContractPaymentsQueue;
        }
        
    }

}
