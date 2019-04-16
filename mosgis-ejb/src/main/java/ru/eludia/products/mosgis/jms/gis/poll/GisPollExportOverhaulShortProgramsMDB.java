package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
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
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.OverhaulShortProgram;
import ru.eludia.products.mosgis.db.model.tables.OverhaulShortProgram.c;
import ru.eludia.products.mosgis.db.model.tables.OverhaulShortProgramHouse;
import ru.eludia.products.mosgis.db.model.tables.OverhaulShortProgramHouseWork;
import ru.eludia.products.mosgis.db.model.tables.OverhaulShortProgramHouseWorksImport;
import ru.eludia.products.mosgis.db.model.tables.OverhaulShortProgramLog;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.jms.UUIDPublisher;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.gosuslugi.dom.schema.integration.capital_repair.CapRemCommonResultType;
import ru.gosuslugi.dom.schema.integration.capital_repair.GetStateResult;
import ru.gosuslugi.dom.schema.integration.capital_repair_service_async.Fault;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisCapitalRepairClient;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportOverhaulShortProgramsQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportOverhaulShortProgramsMDB extends GisPollMDB {
    
    @EJB
    protected UUIDPublisher UUIDPublisher;
    
    @EJB
    WsGisCapitalRepairClient wsGisCapitalRepairClient;
    
    @Resource (mappedName = "mosgis.inExportOverhaulShortProgramHouseWorksManyQueue")
    Queue inExportOverhaulShortProgramHouseWorksManyQueue;
    
    @Override
    protected Get get (UUID uuid) {
        
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")                
            .toOne (OverhaulShortProgramLog.class,     "AS log", "uuid", "action", "id_osp_status", "uuid_user AS user").on ("log.uuid_out_soap=root.uuid")
            .toOne (OverhaulShortProgram.class,        "AS program", "uuid").on ()
            .toOne (VocOrganization.class, "AS org", "orgppaguid").on ("program.org_uuid=org.uuid")
        ;
        
    }
    
    @Override
    protected void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        UUID orgPPAGuid          = (UUID) r.get ("org.orgppaguid");
        
        OverhaulShortProgram.Action action = OverhaulShortProgram.Action.forStatus (VocGisStatus.i.forId (r.get ("log.id_osp_status")));
        
        try {
            
            GetStateResult state = getState (orgPPAGuid, r);
            
            final ErrorMessageType error = state.getErrorMessage ();
            
            if (error != null) throw new GisPollException (error);
            
            final List<CapRemCommonResultType> importResult = state.getImportResult ();
            
            if (importResult == null || importResult.isEmpty ()) throw new GisPollException ("0", "Сервис ГИС ЖКХ вернул пустой результат");
            
            for (CapRemCommonResultType.Error err: importResult.get (0).getError ()) throw new GisPollException (err);
            
            final Map<String, Object> h = statusHash (action.getOkStatus ());
            Queue nextQueue = null;

            switch (action) {            
                case PROJECT_PUBLISH:
                    String planGUID = importResult.get (0).getGUID ();
                    String uniqueNumber = importResult.get (0).getUniqueNumber ();
                    h.put (c.PLANGUID.lc (), planGUID);
                    h.put (c.UNIQUENUMBER.lc (), uniqueNumber);
                    nextQueue = inExportOverhaulShortProgramHouseWorksManyQueue;
                    break;
                case PROJECT_DELETE:
                case ANNUL:
                    List <Map <String, Object>> works = db.getList (db.getModel ()
                        .select (OverhaulShortProgramHouseWork.class, "AS works", "*")
                            .toOne (OverhaulShortProgramHouse.class, "AS houses").on ()
                                .toOne (OverhaulShortProgram.class, "AS program").where ("uuid", r.get ("program.uuid")).on ("program.uuid = houses.program_uuid")
                        .where (OverhaulShortProgramHouseWork.c.ID_OSPHW_STATUS.lc (), VocGisStatus.i.APPROVED.getId ())
                    );
                    
                    works.forEach ((map) -> {
                        map.put (OverhaulShortProgramHouseWork.c.ID_OSPHW_STATUS.lc (), VocGisStatus.i.ANNUL.getId ());
                    });
                    
                    db.update (OverhaulShortProgramHouseWork.class, works);
                    db.update (OverhaulShortProgram.class, HASH (
                        "uuid",       r.get ("program.uuid"),
                        "is_deleted", 1
                    ));
            }

            h.put (c.LAST_SUCCESFULL_STATUS.lc (), action.getOkStatus ());
            update (db, uuid, r, h);
            db.update (OutSoap.class, HASH (
                "uuid", getUuid (),
                "id_status", DONE.getId ()
            ));
            
            if (nextQueue != null) {
                String importWorksId = db.insertId (OverhaulShortProgramHouseWorksImport.class, HASH (
                    OverhaulShortProgramHouseWorksImport.c.PROGRAM_UUID.lc (), r.get ("program.uuid"),
                    OverhaulShortProgramHouseWorksImport.c.ORGPPAGUID.lc (),   orgPPAGuid
                )).toString ();
                List <Map <String, Object>> works = db.getList (db.getModel ()
                    .select (OverhaulShortProgramHouseWork.class, "AS works", "*")
                        .toOne (OverhaulShortProgramHouse.class, "AS houses").where ("is_deleted", 0).on ()
                            .toOne (OverhaulShortProgram.class, "AS programs").where ("uuid", r.get ("program.uuid")).on ("programs.uuid=houses.program_uuid")
                    .where ("is_deleted", 0)
                    .and   ("id_osphw_status <>", VocGisStatus.i.APPROVED.getId ())
                );
                works.stream ().forEach ((map) -> {
                    map.put (OverhaulShortProgramHouseWork.c.IMPORT_UUID.lc (), importWorksId);
                });
                db.update (OverhaulShortProgramHouseWork.class, works);
                UUIDPublisher.publish (nextQueue, importWorksId);
            }

        }
        catch (GisPollRetryException ex) {
            return;
        }
        catch (GisPollException ex) {            
            update (db, uuid, r, statusHash (action.getFailStatus ()));            
            ex.register (db, uuid, r);
        }
        
    }
    
    private static Map<String, Object> statusHash (VocGisStatus.i status) {
        
        final byte id = status.getId ();
        
        return HASH (
            c.ID_OSP_STATUS,          id,
            c.ID_OSP_STATUS_GIS,      id
        );
        
    }

    private void update (DB db, UUID uuid, Map<String, Object> r, Map<String, Object> h) throws SQLException {
        
logger.info ("h=" + h);
        
        h.put ("uuid", r.get ("program.uuid"));
        db.update (OverhaulShortProgram.class, h);
        
        h.put ("uuid", uuid);
        db.update (OverhaulShortProgramLog.class, h);
        
    }
    
    private GetStateResult getState (UUID orgPPAGuid, Map<String, Object> r) throws GisPollRetryException, GisPollException {

        GetStateResult rp;
        
        try {
            rp = wsGisCapitalRepairClient.getState (orgPPAGuid, (UUID) r.get ("uuid_ack"));
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
    
}
