package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
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
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.AdditionalService;
import ru.eludia.products.mosgis.db.model.tables.Charter;
import ru.eludia.products.mosgis.db.model.tables.CharterFile;
import ru.eludia.products.mosgis.db.model.tables.CharterObject;
import ru.eludia.products.mosgis.db.model.tables.CharterObjectLog;
import ru.eludia.products.mosgis.db.model.tables.CharterObjectService;
import ru.eludia.products.mosgis.db.model.tables.CharterObjectServiceLog;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationLog;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.wsc.RestGisFilesClient;
import ru.eludia.products.mosgis.ejb.wsc.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.eludia.products.mosgis.rest.ValidationException;
import ru.eludia.products.mosgis.rest.api.CharterLocal;
import ru.gosuslugi.dom.schema.integration.house_management.CharterStatusExportType;
import ru.gosuslugi.dom.schema.integration.house_management.ExportCAChResultType;
import ru.gosuslugi.dom.schema.integration.house_management.GetStateResult;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportOrgChartersQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportOrgCharterDataMDB extends GisPollMDB {
    
    @EJB
    WsGisHouseManagementClient wsGisHouseManagementClient;
    
    @EJB
    CharterLocal mgmtCharter;
        
    @Resource (mappedName = "mosgis.outExportOrgChartersQueue")
    Queue outExportHouseCharterFilesQueue;    

    @Resource (mappedName = "mosgis.inHouseChartersQueue")
    Queue inHouseChartersQueue;        
    
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
            .toOne (VocOrganizationLog.class,  "AS log", "uuid", "action", "uuid_user").on ("log.uuid_out_soap=root.uuid")
            .toOne (VocOrganization.class,     "AS org", "uuid").on ("log.uuid_object=org.uuid")
        ;
        
    }    
    
    public void download (final UUID uuid) {
        uuidPublisher.publish (outExportHouseCharterFilesQueue, uuid);
    }

    @Override
    protected void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
                
        if (DB.ok (r.get ("is_failed"))) throw new IllegalStateException (r.get ("err_text").toString ());
        
        UUID orgPPAGuid          = (UUID) r.get ("orgppaguid");
        UUID orgUuid             = (UUID) r.get ("org.uuid");

        try {
            
            GetStateResult state = getState (orgPPAGuid, r);        
            
            ExportCAChResultType.Charter theCharter = null;
            
            List<UUID> toAnnul = null;
                        
            for (ExportCAChResultType er: state.getExportCAChResult ()) {
                
                ExportCAChResultType.Charter charter = er.getCharter ();
                
                if (charter == null) continue;
                
                if (charter.getCharterStatus () != CharterStatusExportType.APPROVED) continue;
                
                if (theCharter == null) {
                    theCharter = charter;
                    continue;
                }
                
                theCharter.getAttachmentCharter ().addAll (charter.getAttachmentCharter ());
                theCharter.getCharterPaymentsInfo ().addAll (charter.getCharterPaymentsInfo ());
                theCharter.getContractObject ().addAll (charter.getContractObject ());
                
                if (toAnnul == null) toAnnul = new ArrayList<> ();
                toAnnul.add (UUID.fromString (charter.getCharterVersionGUID ()));
                
            }
                        
            if (theCharter == null) {
                logger.info ("No actual charter returned");
                return;
            }

            updateCharter  (db, orgUuid, theCharter);
            
            if (toAnnul != null) for (UUID u: toAnnul) {
                
                try {
                    wsGisHouseManagementClient.annulCharterData (orgPPAGuid, UUID.randomUUID (), HASH (
                        "reasonofannulment", "exportCAChData вернул более одного утверждённого устава: первый оставляем, остальные аннулируем",
                        "ctr.charterversionguid", u
                    ));
                }
                catch (Exception e) {
                    logger.log (Level.SEVERE, "Cannot annul conflicting charter", e);
                }
                
                UUID ctrUuid = (UUID) db.getMap (db.getModel ().select (Charter.class, "uuid").where (Charter.c.CHARTERGUID, theCharter.getCharterGUID ())).get ("uuid");
                
                db.upsert (Charter.class, HASH (               
                    EnTable.c.UUID,          ctrUuid,
                    Charter.c.ID_CTR_STATUS, VocGisStatus.i.PENDING_RQ_PLACING.getId ()
                ));
                
                String idLog = ((MosGisModel) db.getModel ()).createIdLog (db, db.getModel ().get (Charter.class), null, ctrUuid, VocAction.i.APPROVE);
                
                uuidPublisher.publish (inHouseChartersQueue, idLog);

            }
            
        }
        catch (GisPollRetryException ex) {
            return;
        }
        catch (GisPollException ex) {
            ex.register (db, uuid, r);
        }        

    }        

    private void updateCharter (DB db, UUID orgUuid, ExportCAChResultType.Charter charter) throws SQLException {
        
        Model m = db.getModel ();
        
        db.upsert (Charter.class, HASH (               
            Charter.c.CHARTERGUID,        charter.getCharterGUID (),
            Charter.c.CHARTERVERSIONGUID, charter.getCharterVersionGUID (),
            Charter.c.UUID_ORG,           orgUuid,
            Charter.c.ID_CTR_STATUS,      VocGisStatus.i.PENDING_RP_RELOAD.getId (),
            Charter.c.ID_CTR_STATUS_GIS,  VocGisStatus.i.forName (charter.getCharterStatus ().value ())
        ), Charter.c.UUID_ORG.lc ());
        
        UUID ctrUuid = (UUID) db.getMap (m.select (Charter.class, "uuid").where (Charter.c.CHARTERGUID, charter.getCharterGUID ())).get ("uuid");
        
        Map<String, Object> logRecord = db.getMap (m.select (VocOrganizationLog.class, "action", "uuid_user", "uuid_out_soap", "uuid_message").where ("uuid_object", orgUuid).orderBy ("ts DESC"));
                
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

            ((MosGisModel) db.getModel ()).createIdLog (db, db.getModel ().get (Charter.class), null, ctrUuid, VocAction.i.IMPORT_CHARTERS);

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
            
            ((MosGisModel) db.getModel ()).createIdLog (db, db.getModel ().get (Charter.class), null, ctrUuid, VocAction.i.IMPORT_CHARTERS);
                            
        }        
                
    }

    private void log (Collection<Map<String, Object>> records, Map<String, Object> logRecord, DB db, Class c) throws SQLException {
        for (Map<String, Object> i: records) {
            logRecord.put ("uuid_object", i.get ("uuid"));
            db.insert (c, logRecord);
        }
    }
    
}
