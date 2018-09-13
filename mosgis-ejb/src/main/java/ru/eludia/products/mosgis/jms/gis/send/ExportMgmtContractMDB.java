package ru.eludia.products.mosgis.jms.gis.send;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.AdditionalService;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.ContractFile;
import ru.eludia.products.mosgis.db.model.tables.ContractLog;
import ru.eludia.products.mosgis.db.model.tables.ContractObject;
import ru.eludia.products.mosgis.db.model.tables.ContractObjectService;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.ejb.wsc.GisRestStream;
import ru.eludia.products.mosgis.ejb.wsc.RestGisFilesClient;
import ru.eludia.products.mosgis.ejb.wsc.RestGisFilesClient.Context;
import ru.eludia.products.mosgis.ejb.wsc.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inHouseMgmtContractsQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportMgmtContractMDB extends UUIDMDB<ContractLog> {
    
    @EJB
    WsGisHouseManagementClient wsGisHouseManagementClient;

    @EJB
    RestGisFilesClient restGisFilesClient;
    
    @Resource (mappedName = "mosgis.outExportHouseMgmtContractsQueue")
    Queue queue;
    
    @EJB
    protected UUIDPublisher UUIDPublisher;
    
    protected Get get (UUID uuid) {        
        
        final MosGisModel m = ModelHolder.getModel ();
        
        try (DB db = m.getDb ()) {
            
            NsiTable nsi58 = NsiTable.getNsiTable (db, 58);
            
            return (Get) m
                .get (getTable (), uuid, "*")
                .toOne (Contract.class, "AS ctr").on ()
                .toOne (VocOrganization.class, "AS org", "orgppaguid").on ("ctr.uuid_org")
                .toOne (nsi58, "AS vc_nsi_58", "guid").on ("vc_nsi_58.code=ctr.code_vc_nsi_58 AND vc_nsi_58.isactual=1")
            ;
            
        }
        catch (SQLException ex) {
            throw new IllegalStateException (ex);
        }
                
    }

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        final UUID orgppaguid = (UUID) r.get ("org.orgppaguid");
        
        Model m = db.getModel ();
        
        Map<UUID, Map<String, Object>> id2file = new HashMap <> ();
        
        db.forEach (m.select (ContractFile.class, "*").where ("uuid_contract", r.get ("uuid_object")).and ("id_status", 1), (rs) -> {                        
            final Map<String, Object> file = db.HASH (rs);
            id2file.put ((UUID) file.get ("uuid"), file);
        });
        
        final Collection<Map<String, Object>> files = id2file.values ();
        
        for (Map<String, Object> file: files) {
            
            try (
                GisRestStream out = new GisRestStream (
                    restGisFilesClient,
                    Context.HOMEMANAGEMENT,
                    orgppaguid, 
                    file.get ("label").toString (), 
                    Long.parseLong (file.get ("len").toString ()),
                    (uploadId) -> file.put ("attachmentguid", uploadId)
                )
            ) {

                db.getStream (m.get (ContractFile.class, file.get ("uuid"), "body"), out);

                file.put ("attachmenthash", out.getAttachmentHASH ());

                db.update (ContractFile.class, file);

            }
            catch (Exception ex) {
                logger.log (Level.SEVERE, "Cannot upload " + file, ex);
                return;
            }
            
        }
        
        r.put ("files", id2file.values ());

            NsiTable nsi3 = NsiTable.getNsiTable (db, 3);

            Map<UUID, Map<String, Object>> id2o = new HashMap <> ();
            
            db.forEach (m.select (ContractObject.class, "*").where ("uuid_contract", r.get ("uuid_object")).and ("is_deleted", 0), (rs) -> {                
                Map<String, Object> object = db.HASH (rs);                                
                object.put ("services", new ArrayList ());
                UUID agr = (UUID) object.get ("uuid_contract_agreement");
                if (agr != null) object.put ("contract_agreement", ContractFile.toAttachmentType (id2file.get (agr)));
                id2o.put ((UUID) object.get ("uuid"), object);                
            });
            
            r.put ("objects", id2o.values ());
            
            db.forEach (m
                .select (ContractObjectService.class, "AS root", "*")
                .toMaybeOne (AdditionalService.class, "AS add_service", "uniquenumber", "elementguid").on ()
                .toMaybeOne (nsi3, "AS vc_nsi_3", "code", "guid").on ("vc_nsi_3.code=root.code_vc_nsi_3 AND vc_nsi_3.isactual=1")
                .where ("uuid_contract", r.get ("uuid_object"))
                .and ("is_deleted", 0), 
            (rs) -> {
                Map<String, Object> service = db.HASH (rs);                                
                UUID agr = (UUID) service.get ("uuid_contract_agreement");
                if (agr != null) service.put ("contract_agreement", ContractFile.toAttachmentType (id2file.get (agr)));
                ((List) id2o.get (service.get ("uuid_contract_object")).get ("services")).add (service);
            });
                        
            UUID messageGUID = UUID.randomUUID ();
                                    
            try {
                
                AckRequest.Ack ack = wsGisHouseManagementClient.placeContractData (orgppaguid, messageGUID, r);
                
                db.begin ();

                    db.update (OutSoap.class, DB.HASH (
                        "uuid",     messageGUID,
                        "uuid_ack", ack.getMessageGUID ()
                    ));

                    db.update (getTable (), DB.HASH (
                        "uuid",          uuid,
                        "uuid_out_soap", messageGUID,
                        "uuid_message",  ack.getMessageGUID ()
                    ));

                    db.update (Contract.class, DB.HASH (
                        "uuid",          r.get ("uuid_object"),
                        "uuid_out_soap", messageGUID
                    ));

                db.commit ();

                UUIDPublisher.publish (queue, ack.getRequesterMessageGUID ());            
                
            }
            catch (Fault ex) {

                logger.log (Level.SEVERE, "Can't place management contract", ex);

                ru.gosuslugi.dom.schema.integration.base.Fault faultInfo = ex.getFaultInfo ();

                db.begin ();

                    db.update (OutSoap.class, HASH (
                        "uuid", messageGUID,
                        "id_status", DONE.getId (),
                        "is_failed", 1,
                        "err_code",  faultInfo.getErrorCode (),
                        "err_text",  faultInfo.getErrorMessage ()
                    ));

                    db.update (getTable (), DB.HASH (
                        "uuid",          uuid,
                        "uuid_out_soap", messageGUID
                    ));
                    
                    db.update (Contract.class, DB.HASH (
                        "uuid",          r.get ("uuid_object"),
                        "uuid_out_soap", messageGUID
                    ));

                db.commit ();

                return;

            }            
    }
    
}