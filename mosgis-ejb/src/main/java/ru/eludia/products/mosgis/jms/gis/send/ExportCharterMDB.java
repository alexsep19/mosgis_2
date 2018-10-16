package ru.eludia.products.mosgis.jms.gis.send;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.AdditionalService;
import ru.eludia.products.mosgis.db.model.tables.Charter;
import ru.eludia.products.mosgis.db.model.tables.CharterFile;
import ru.eludia.products.mosgis.db.model.tables.CharterFileLog;
import ru.eludia.products.mosgis.db.model.tables.CharterLog;
import ru.eludia.products.mosgis.db.model.tables.CharterObject;
import ru.eludia.products.mosgis.db.model.tables.CharterObjectService;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocContractDocType;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.ejb.wsc.RestGisFilesClient;
import ru.eludia.products.mosgis.ejb.wsc.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.eludia.products.mosgis.jms.gis.poll.GisPollExportCharterStatusMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.house_management.GetStateResult;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inHouseChartersQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportCharterMDB extends UUIDMDB<CharterLog> {
    
    @EJB
    WsGisHouseManagementClient wsGisHouseManagementClient;

    @EJB
    RestGisFilesClient restGisFilesClient;
    
    @Resource (mappedName = "mosgis.outExportHouseChartersQueue")
    Queue outExportHouseChartersQueue;
    
    @Resource (mappedName = "mosgis.outExportHouseCharterStatusQueue")
    Queue outExportHouseCharterStatusQueue;
    
    @Resource (mappedName = "mosgis.inHouseCharterFilesQueue")
    Queue filesQueue;
    
    @Resource (mappedName = "mosgis.inHouseChartersQueue")
    Queue ownQueue;
    
    @EJB
    protected UUIDPublisher UUIDPublisher;
    
    protected Get get (UUID uuid) {        
        
        final MosGisModel m = ModelHolder.getModel ();
        
        try (DB db = m.getDb ()) {
                        
            return (Get) m
                .get (getTable (), uuid, "*")
                .toOne (Charter.class, "AS ctr", "id_ctr_status", "charterversionguid", "charterguid").on ()
                .toOne (VocOrganization.class, "AS org", "orgppaguid").on ("ctr.uuid_org")
            ;
            
        }
        catch (SQLException ex) {
            throw new IllegalStateException (ex);
        }
                
    }
    
    private Map<Object, Map<String, Object>> getId2file (DB db, Model m, Map<String, Object> r) throws SQLException {
        
        Map<Object, Map<String, Object>> id2file = db.getIdx (m
            .select (CharterFile.class, "*")
            .toOne  (CharterFileLog.class, "AS log", "ts_start_sending", "err_text").on ()
            .where  ("uuid_charter", r.get ("uuid_object"))
            .and    ("id_status", 1)
            .and    ("id_type <>", VocContractDocType.i.OTHER.getId ())
        );
        
        return id2file;
        
    }  
    
    private class StaleFileException extends Exception {
        
        public static final String ERR_FIELD = "log.err_text";
        
        UUID uuid;

        public StaleFileException (Map<String, Object> file) {
            super (file.get (ERR_FIELD) != null ? file.get (ERR_FIELD).toString () : "Не удалось передать файл " + file.get ("label"));
            this.uuid = (UUID) file.get ("uuid");
        }

        public UUID getUuid () {
            return uuid;
        }
        
    }
        
    private UUID getFileUUIDwaitingFor (DB db, Collection <Map<String, Object>> files, Charter.Action action) throws SQLException, StaleFileException {
        
        UUID waitingFor = null;
        
        for (Map<String, Object> file: files) {
            
            VocContractDocType.i type = VocContractDocType.i.forId (file.get ("id_type"));
            
            if (!action.needsUpload (type)) continue;
            
            final String err = (String) file.get (StaleFileException.ERR_FIELD);
            
            if (err != null && !err.isEmpty ()) throw new StaleFileException (file);
            
            if (file.get ("attachmentguid") != null) continue;
            
            final Object ots = file.get ("log.ts_start_sending");            
            
            if (ots == null) {
                
                db.update (CharterFileLog.class, DB.HASH (
                    "uuid",              file.get ("id_log"),
                    "ts_start_sending",  NOW
                ));

                UUIDPublisher.publish (filesQueue, waitingFor = (UUID) file.get ("uuid"));
                
            }
            else if (Timestamp.valueOf (ots.toString ()).getTime () < System.currentTimeMillis () - 1000L * 60) {

                throw new StaleFileException (file);

            }
            else if (waitingFor == null) {

                waitingFor = (UUID) file.get ("uuid");

            }
            
        }
        
        return waitingFor;
        
    }
    
    private boolean isFileSetReadyToContinue (DB db, Map<String, Object> r, Collection <Map<String, Object>> files, Charter.Action action) throws SQLException {
        
        UUID waitingFor = null;
        
        try {
            waitingFor = getFileUUIDwaitingFor (db, files, action);
        }
        catch (StaleFileException ex) {

            UUID uuid = (UUID) r.get ("uuid");                   

            db.begin ();

                db.upsert (OutSoap.class, HASH (
                    "uuid", uuid,
                    "is_out",  1,
                    "svc",  "REST",
                    "op",   "file",
                    "id_status", DONE.getId (),
                    "is_failed", 1,
                    "err_code",  "0",
                    "err_text",  ex.getMessage ()
                ));

                db.update (getTable (), DB.HASH (
                    "uuid",          uuid,
                    "uuid_out_soap", uuid
                ));

                db.update (Charter.class, DB.HASH (
                    "uuid",              r.get ("uuid_object"),
                    "uuid_out_soap",     uuid,
                    "id_ctr_status",     action.getFailStatus ().getId ()
                ));

            db.commit ();

            logger.warning ("Stale contract file: " + ex.getUuid ());

            return false;
                
        }
        
        if (waitingFor != null) {
            
            logger.info ("Waiting for " + waitingFor + " to upload");
            
            UUIDPublisher.publish (ownQueue, (UUID) r.get ("uuid"));
            
            return false;
            
        }        
        
        return true;
        
    }

    private void updateVersions (DB db, Map<String, Object> r) throws SQLException {    
        
        UUID rUID = UUID.randomUUID ();

        final UUID ctrUuid = (UUID) r.get ("uuid_object");
        
        UUID orgPPAGuid = getOrgPPAGUID (r);

        AckRequest.Ack exportCharterStatus = null;

        try {
            exportCharterStatus = wsGisHouseManagementClient.exportCharterStatus (orgPPAGuid, rUID, Collections.singletonList ((UUID) r.get ("ctr.charterguid")));
        }
        catch (Exception ex) {
            logger.log (Level.SEVERE, "wsGisHouseManagementClient.exportCharterStatus", ex);
        }                
                
        UUID mGUID = UUID.fromString (exportCharterStatus.getMessageGUID ());

        db.update (Charter.class, HASH (
            "uuid", ctrUuid,
            "charterversionguid", null
        ));

        for (int i = 0; i < 10; i ++) {
            
            try {
                
                Thread.sleep (2000L);
                
                GetStateResult state = null;
                
                try {
                    state = wsGisHouseManagementClient.getState (orgPPAGuid, mGUID);
                }
                catch (Exception ex) {
                    logger.log (Level.SEVERE, "wsGisHouseManagementClient.getState", ex);
                }
                
                if (state.getRequestState () < 2) continue;
                
                GisPollExportCharterStatusMDB.processGetStateResponse (state, db, rUID, true);
                
                break;
                                
            }
            catch (InterruptedException ex) {
                logger.log (Level.WARNING, "It's futile", ex);
            }
            
        }  
        
    }

    AckRequest.Ack invoke (DB db, Charter.Action action, UUID messageGUID,  Map<String, Object> r) throws Fault, SQLException {
            
        UUID orgPPAGuid = getOrgPPAGUID (r);
            
        switch (action) {
            case PLACING:     return wsGisHouseManagementClient.placeCharterData (orgPPAGuid, messageGUID, r);
            case EDITING:     return wsGisHouseManagementClient.editCharterData  (orgPPAGuid, messageGUID, r);
            default: throw new IllegalArgumentException ("No action implemented for " + action);
        }

    }

    private UUID getOrgPPAGUID (Map<String, Object> r) {
        final UUID orgPPAGuid = (UUID) r.get ("org.orgppaguid");
        return orgPPAGuid;
    }

    private boolean isVersionUpdateNeeded (Charter.Action action) {
        switch (action) {
            case EDITING:
//            case ANNULMENT:
//            case TERMINATION:
//            case ROLLOVER:
                return true;
            default:
                return false;
        }
    }
    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        VocGisStatus.i status = VocGisStatus.i.forId (r.get ("ctr.id_ctr_status"));
        
        Charter.Action action = Charter.Action.forStatus (status);
        
        if (action == null) {
            logger.warning ("No action is implemented for " + status);
            return;
        }

        Model m = db.getModel ();

        if (isVersionUpdateNeeded (action)) {
            updateVersions (db, r);
            r = db.getMap (get (uuid));
        }

        if (action.needsUpload ()) {
            if (someFileUploadIsInProgress (db, m, r, action)) return;
        }

        try {

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

                db.update (Charter.class, DB.HASH (
                    "uuid",          r.get ("uuid_object"),
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

                db.update (Charter.class, DB.HASH (
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

                db.update (Charter.class, DB.HASH (
                    "uuid",              r.get ("uuid_object"),
                    "uuid_out_soap",     uuid,
                    "id_ctr_status",     action.getFailStatus ().getId ()
                ));

            db.commit ();

            return;
            
        }
        
    }
    
    Queue getQueue (Charter.Action action) {
        
        switch (action) {
//            case REFRESHING: return outExportHouseCharterStatusQueue;
            default:         return outExportHouseChartersQueue;
        }
        
    }

    private boolean someFileUploadIsInProgress (DB db, Model m, Map<String, Object> r, Charter.Action action) throws SQLException {
        
        Map <Object, Map <String, Object>> id2file = getId2file (db, m, r);
        
        final Collection <Map<String, Object>> files = id2file.values ();
        
        if (!isFileSetReadyToContinue (db, r, files, action)) return true;
        
        addFilesAndObjects (r, files, db, m, id2file);
        
        return false;
        
    }

    private void addFilesAndObjects (Map<String, Object> r, final Collection<Map<String, Object>> files, DB db, Model m, Map<Object, Map<String, Object>> id2file) throws SQLException {
        
        r.put ("files", files);
        
        NsiTable nsi3 = NsiTable.getNsiTable (3);
        
        Map<UUID, Map<String, Object>> id2o = new HashMap <> ();
        
        db.forEach (m
                
            .select (CharterObject.class, "*")
                .toMaybeOne (CharterFile.class, "AS doc", "label", "description", "attachmentguid", "attachmenthash").on ()
                .where  ("uuid_charter", r.get ("uuid_object"))
                .and    ("is_deleted", 0)
                .and    ("id_ctr_status_gis <>", VocGisStatus.i.ANNUL.getId ())
//                
            , (rs) -> {
                Map<String, Object> object = db.HASH (rs);
                object.put ("services", new ArrayList ());
//                UUID agr = (UUID) object.get ("uuid_charter_agreement");
//                if (agr != null) object.put ("contract_agreement", CharterFile.toAttachmentType (id2file.get (agr)));
                id2o.put ((UUID) object.get ("uuid"), object);
                
        });
        
        r.put ("objects", id2o.values ());
        
        db.forEach (m
            .select (CharterObjectService.class, "AS root", "*")
            .toMaybeOne (CharterFile.class, "AS doc", "label", "description", "attachmentguid", "attachmenthash").on ()
            .toMaybeOne (AdditionalService.class, "AS add_service", "uniquenumber", "elementguid").on ()
            .toMaybeOne (nsi3, "AS vc_nsi_3", "code", "guid").on ("vc_nsi_3.code=root.code_vc_nsi_3 AND vc_nsi_3.isactual=1")
            .where ("uuid_charter", r.get ("uuid_object"))
            .and ("is_deleted", 0),

            (rs) -> {

                Map<String, Object> service = db.HASH (rs);

//                UUID agr = (UUID) service.get ("uuid_charter_agreement");
//
//                if (agr != null) service.put ("contract_agreement", CharterFile.toAttachmentType (id2file.get (agr)));

                final Map<String, Object> o = id2o.get (service.get ("uuid_charter_object"));

                if (o != null) ((List) o.get ("services")).add (service);

            }

        );

    }

}
