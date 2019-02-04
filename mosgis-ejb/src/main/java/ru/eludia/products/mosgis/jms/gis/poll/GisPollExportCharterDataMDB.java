package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.tables.AdditionalService;
import ru.eludia.products.mosgis.db.model.tables.Charter;
import ru.eludia.products.mosgis.db.model.tables.CharterFile;
import ru.eludia.products.mosgis.db.model.tables.CharterLog;
import ru.eludia.products.mosgis.db.model.tables.CharterObject;
import ru.eludia.products.mosgis.db.model.tables.CharterObjectLog;
import ru.eludia.products.mosgis.db.model.tables.CharterObjectService;
import ru.eludia.products.mosgis.db.model.tables.CharterObjectServiceLog;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.wsc.RestGisFilesClient;
import ru.eludia.products.mosgis.ejb.wsc.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.eludia.products.mosgis.rest.ValidationException;
import ru.eludia.products.mosgis.rest.api.CharterLocal;
import ru.gosuslugi.dom.schema.integration.house_management.ExportCAChResultType;
import ru.gosuslugi.dom.schema.integration.house_management.GetStateResult;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportHouseChartersDataQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportCharterDataMDB extends GisPollMDB {
    
    @EJB
    WsGisHouseManagementClient wsGisHouseManagementClient;
    
    @EJB
    CharterLocal mgmtCharter;
        
    @Resource (mappedName = "mosgis.outExportHouseCharterFilesQueue")
    Queue outExportHouseCharterFilesQueue;    

    @EJB
    RestGisFilesClient filesClient;
    
    private GetStateResult getState (UUID orgPPAGuid, Map<String, Object> r) throws GisPollRetryException, GisPollException {

        GetStateResult rp;
        
        try {
            rp = wsGisHouseManagementClient.getState (orgPPAGuid, (UUID) r.get ("uuid_ack"));
        }
        catch (Fault ex) {
            throw new GisPollException (ex.getFaultInfo ());
        }
        catch (Throwable ex) {            
            throw new GisPollException (ex);
        }
        
        checkIfResponseReady (rp);
        
        return rp;
        
    }    
    
    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")                
            .toOne (CharterLog.class,     "AS log", "uuid", "id_ctr_status", "action", "uuid_user").on ("log.uuid_out_soap=root.uuid")
            .toOne (Charter.class,        "AS ctr", "uuid", "charterguid", "charterversionguid", "uuid_org").on ()
        ;
        
    }    
    
    public void download (final UUID uuid) {
        uuidPublisher.publish (outExportHouseCharterFilesQueue, uuid);
    }

    @Override
    protected void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
                
        if (DB.ok (r.get ("is_failed"))) throw new IllegalStateException (r.get ("err_text").toString ());
        
        UUID orgPPAGuid          = (UUID) r.get ("orgppaguid");
        UUID ctrUuid             = (UUID) r.get ("ctr.uuid");
        UUID orgUuid             = (UUID) r.get ("ctr.uuid_org");
        UUID charterversionguid = (UUID) r.get ("ctr.charterversionguid");
        
        if (charterversionguid == null) {
            logger.warning ("Empty charterversionguid, bailing out");
            return;
        }
        
        String scharterversionguid = charterversionguid.toString ();
                
        try {
            
            GetStateResult state = getState (orgPPAGuid, r);        
        
            for (ExportCAChResultType er: state.getExportCAChResult ()) {

                ExportCAChResultType.Charter charter = er.getCharter ();

                if (isWrong (charter, scharterversionguid)) continue;                                       

                db.update (Charter.class, HASH (
                    "uuid", ctrUuid,
                    "charterversionguid", null
                ));                

                db.begin ();
                
                    updateCharter  (db, orgUuid, ctrUuid, charter);

                db.commit ();

                break;

            }            
            
        }
        catch (GisPollRetryException ex) {
            return;
        }
        catch (GisPollException ex) {
            ex.register (db, uuid, r);
        }        

    }        

    private void updateCharter (DB db, UUID orgUuid, UUID ctrUuid, ExportCAChResultType.Charter charter) throws SQLException {
        
        Model m = db.getModel ();
        
        Map<String, Object> logRecord = db.getMap (m.select (CharterLog.class, "action", "uuid_user", "uuid_out_soap", "uuid_message").orderBy ("ts DESC"));
                
        try {
            
            db.begin ();
            
            AdditionalService.Sync adds = ((AdditionalService) m.get (AdditionalService.class)).new Sync (db, orgUuid);
            adds.reload ();

            CharterFile.Sync charterFiles = ((CharterFile) m.get (CharterFile.class)).new Sync (db, ctrUuid, filesClient);
            charterFiles.addFrom (charter);
            charterFiles.sync ();

            final String key = "uuid_charter";
            List<Map<String, Object>> idsCharterObject = db.getList (m.select (CharterObject.class, "uuid").where (key, ctrUuid).and ("is_deleted", 0));
            List<Map<String, Object>> idsCharterObjectService = db.getList (m.select (CharterObjectService.class, "uuid").where (key, ctrUuid).and ("is_deleted", 0));
            final Map<String, Object> del = HASH (
                key, ctrUuid,
                "is_deleted", 1
            );        

            db.update (CharterObject.class,        del, key);
            log (idsCharterObject, logRecord, db, CharterObjectLog.class);

            db.update (CharterObjectService.class, del, key);
            for (Map<String, Object> i: idsCharterObjectService) {
                logRecord.put ("uuid_object", i.get ("uuid"));
                db.insert (CharterObjectServiceLog.class, logRecord);
            }

            CharterObject.Sync charterObjects = ((CharterObject) m.get (CharterObject.class)).new Sync (db, ctrUuid, charterFiles);
            charterObjects.addAll (charter.getContractObject ());
            charterObjects.sync ();        
            log (charterObjects.values (), logRecord, db, CharterObjectLog.class);

            final CharterObjectService srvTable = (CharterObjectService) m.get (CharterObjectService.class);

            CharterObjectService.SyncH charterObjectServicesH = (srvTable).new SyncH (db, ctrUuid, charterFiles);        
            CharterObjectService.SyncA charterObjectServicesA = (srvTable).new SyncA (db, ctrUuid, charterFiles, adds);

            charter.getContractObject ().forEach ((co) -> {
                Map<String, Object> parent = HASH ("uuid_charter_object", charterObjects.getPk (co));
                charterObjectServicesH.addAll (co.getHouseService (), parent);
                charterObjectServicesA.addAll (co.getAddService (), parent);
            });

            charterObjectServicesH.sync ();
            log (charterObjectServicesH.values (), logRecord, db, CharterObjectServiceLog.class);

            charterObjectServicesA.sync ();
            log (charterObjectServicesA.values (), logRecord, db, CharterObjectServiceLog.class);
            
            VocGisStatus.i status = VocGisStatus.i.forName (charter.getCharterStatus ().value ());

            final Map<String, Object> h = HASH (
                "id_ctr_status",       status.getId (),
                "id_ctr_status_gis",   status.getId (),
                "charterversionguid", charter.getCharterVersionGUID ()
            );

            h.put ("date_", charter.getDate ());

            Charter.setExtraFields (h, charter);

            h.put ("uuid", ctrUuid);
            db.update (Charter.class, h);

            h.put ("uuid", getUuid ());
            db.update (CharterLog.class, h);

            db.update (OutSoap.class, HASH (
                "uuid", getUuid (),
                "id_status", DONE.getId ()
            ));
            
            db.commit ();
            
        }
        catch (Exception ex) {
            
            db.rollback ();
            
            db.getConnection ().setAutoCommit (true);
                            
            ValidationException vex = ValidationException.wrap (ex);
                
            db.update (OutSoap.class, HASH (
                "uuid", logRecord.get ("uuid_out_soap"),
                "is_failed", 1,
                "err_code", 0,
                "err_text", vex == null ? ex.getMessage () : vex.getMessage ()
            ));

            db.update (Charter.class, HASH (
                "uuid", ctrUuid,
                Charter.c.ID_CTR_STATUS.lc (), VocGisStatus.i.FAILED_STATE.getId ()
            ));

            db.update (CharterLog.class, HASH (
                "uuid", logRecord.get ("uuid"),
                Charter.c.ID_CTR_STATUS.lc (), VocGisStatus.i.FAILED_STATE.getId ()
            ));
                
        }        
                
    }

    private void log (Collection<Map<String, Object>> records, Map<String, Object> logRecord, DB db, Class c) throws SQLException {
        for (Map<String, Object> i: records) {
            logRecord.put ("uuid_object", i.get ("uuid"));
            db.insert (c, logRecord);
        }
    }

    private boolean isWrong (ExportCAChResultType.Charter charter, String scharterversionguid) {
        
        if (charter == null) {
            logger.warning ("Not a Charter? Bizarre, bizarre...");
            return true;
        }
        
        String v = charter.getCharterVersionGUID ();
        
        if (v == null) {
            logger.warning ("Empty CharterVersionGUID? Bizarre, bizarre...");
            return true;
        }
        
        if (!v.equals (scharterversionguid)) {
            logger.warning ("We requested " + scharterversionguid + ". Why did they send back " + v + "?");
            return true;
        }
        
        return false;
        
    }
    
}
