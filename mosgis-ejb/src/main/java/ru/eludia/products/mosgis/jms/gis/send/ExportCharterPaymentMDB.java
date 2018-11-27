package ru.eludia.products.mosgis.jms.gis.send;

import java.sql.SQLException;
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
import ru.eludia.products.mosgis.db.model.tables.Charter;
import ru.eludia.products.mosgis.db.model.tables.CharterObject;
import ru.eludia.products.mosgis.db.model.tables.CharterPayment;
import ru.eludia.products.mosgis.db.model.tables.CharterPaymentFile;
import ru.eludia.products.mosgis.db.model.tables.CharterPaymentFileLog;
import ru.eludia.products.mosgis.db.model.tables.CharterPaymentLog;
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
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inHouseCharterPaymentsQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportCharterPaymentMDB extends UUIDMDB<CharterPaymentLog> {
    
    @EJB
    WsGisHouseManagementClient wsGisHouseManagementClient;
    
    @Resource (mappedName = "mosgis.outExportHouseCharterPaymentsQueue")
    Queue outExportHouseCharterPaymentsQueue;
    
    @Resource (mappedName = "mosgis.inHouseCharterPaymentFilesQueue")
    Queue inHouseCharterPaymentFilesQueue;
            
    @EJB
    protected UUIDPublisher UUIDPublisher;
    
    protected Get get (UUID uuid) {        
        
        final MosGisModel m = ModelHolder.getModel ();
        
        try (DB db = m.getDb ()) {
                        
            return (Get) m
                .get (getTable (), uuid, "*")
                .toOne (CharterPayment.class, "AS ctr", "id_ctr_status").on ()
                .toMaybeOne (CharterPaymentFile.class, "AS doc_0", "*").on ("ctr.uuid_file_0=doc_0.uuid")
                .toMaybeOne (CharterPaymentFileLog.class, "AS doc_log_0", "ts_start_sending", "err_text").on ("doc_0.id_log=doc_log_0.uuid")
                .toMaybeOne (CharterPaymentFile.class, "AS doc_1", "*").on ("ctr.uuid_file_0=doc_1.uuid")
                .toMaybeOne (CharterPaymentFileLog.class, "AS doc_log_1", "ts_start_sending", "err_text").on ("doc_1.id_log=doc_log_1.uuid")
                .toOne (Charter.class, "AS ctrt", "charterversionguid").on ()
                .toOne (VocOrganization.class, "AS org", "orgppaguid").on ("ctrt.uuid_org=org.uuid")
                .toMaybeOne (CharterObject.class, "AS o", "contractobjectversionguid").on ("ctr.uuid_charter_object=o.uuid")
            ;
            
        }
        catch (SQLException ex) {
            throw new IllegalStateException (ex);
        }
                
    }
        
    AckRequest.Ack invoke (DB db, CharterPayment.Action action, UUID messageGUID,  Map<String, Object> r) throws Fault, SQLException {
            
        UUID orgPPAGuid = getOrgPPAGUID (r);
            
        switch (action) {
            case PLACING:     return wsGisHouseManagementClient.placeCharterPaymentsInfo (orgPPAGuid, messageGUID, r);
//            case ANNULMENT:   return wsGisHouseManagementClient.annulCharterPaymentData     (orgPPAGuid, messageGUID, r);
//            case EDITING:     return wsGisHouseManagementClient.editCharterPaymentData      (orgPPAGuid, messageGUID, r);
//            case TERMINATION: return wsGisHouseManagementClient.terminateCharterPaymentData (orgPPAGuid, messageGUID, r);
//            case ROLLOVER:    return wsGisHouseManagementClient.rolloverCharterPaymentData  (orgPPAGuid, messageGUID, r);
//            case RELOADING:   return wsGisHouseManagementClient.exportCharterPaymentData    (orgPPAGuid, messageGUID, Collections.singletonList ((UUID) r.get ("ctr.charterversionguid")));
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
        
        CharterPayment.Action action = CharterPayment.Action.forStatus (status);
        
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
            
            if (action == CharterPayment.Action.PLACING && DB.ok (r.get ("uuid_file"))) {

                final Object err = r.get ("doc_log.err_text");
                if (DB.ok (err)) throw new Exception (err.toString ());
                
                final Object ots = r.get ("doc_log.ts_start_sending");            
                
                if (!DB.ok (ots)) {
                    
                    db.update (CharterPaymentFileLog.class, DB.HASH (
                        "uuid",              r.get ("doc.id_log"),
                        "ts_start_sending",  NOW
                    ));
                    
                    UUIDPublisher.publish (inHouseCharterPaymentFilesQueue, (UUID) r.get ("uuid_file"));
                    
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

                db.update (CharterPayment.class, DB.HASH (
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

                db.update (CharterPayment.class, DB.HASH (
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
            
                String err = ex.getMessage ();
                if (err.length () > 2000) err = err.substring (0, 2000);

                db.upsert (OutSoap.class, HASH (
                    "uuid", uuid,
                    "svc",  getClass ().getName (),
                    "op",   action.toString (),
                    "is_out",  1,
                    "id_status", DONE.getId (),
                    "is_failed", 1,
                    "err_code",  "0",
                    "err_text",  err
                ));

                db.update (getTable (), DB.HASH (
                    "uuid",          uuid,
                    "uuid_out_soap", uuid
                ));

                db.update (CharterPayment.class, DB.HASH (
                    "uuid",              r.get ("uuid_object"),
                    "uuid_out_soap",     uuid,
                    "id_ctr_status",     action.getFailStatus ().getId ()
                ));

            db.commit ();

            return;
            
        }
        
    }
    
    Queue getQueue (CharterPayment.Action action) {        
        return outExportHouseCharterPaymentsQueue;        
    }

}
