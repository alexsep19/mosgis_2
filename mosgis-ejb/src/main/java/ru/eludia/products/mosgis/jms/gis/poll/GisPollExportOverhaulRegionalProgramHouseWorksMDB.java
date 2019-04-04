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
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgram;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramHouseWork;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramHouseWorksImport;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.jms.UUIDPublisher;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisCapitalRepairClient;
import ru.gosuslugi.dom.schema.integration.capital_repair.GetStateResult;
import ru.gosuslugi.dom.schema.integration.capital_repair_service_async.Fault;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.capital_repair.CapRemCommonResultType;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportOverhaulRegionalProgramHouseWorksQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportOverhaulRegionalProgramHouseWorksMDB extends GisPollMDB {
    
    @EJB
    protected UUIDPublisher UUIDPublisher;
    
    @EJB
    WsGisCapitalRepairClient wsGisCapitalRepairClient;
    
    @Resource (mappedName = "mosgis.inExportOverhaulRegionalProgramsQueue")
    Queue inExportOverhaulRegionalProgramsQueue;
    
    @Override
    protected Get get (UUID uuid) {
        
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")                
            .toOne (OverhaulRegionalProgramHouseWorksImport.class, "AS import", "*").on ("import.uuid_out_soap=root.uuid")
        ;
        
    }
    
    private List <Map <String, Object>> getWorks (DB db, Map <String, Object> r, VocGisStatus.i status) throws SQLException {
        
        return db.getList (db.getModel ()
            .select (OverhaulRegionalProgramHouseWork.class, "AS works", "*")
            .where  (OverhaulRegionalProgramHouseWork.c.IMPORT_UUID.lc (), r.get ("import.uuid"))
            .and    (OverhaulRegionalProgramHouseWork.c.ID_ORPHW_STATUS.lc (), status.getId ())
        );
        
    }
    
    @Override
    protected void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        UUID orgPPAGuid = (UUID) r.get ("import.orgppaguid");
        
        try {
            
            GetStateResult state = getState (orgPPAGuid, r);
            
            final ErrorMessageType error = state.getErrorMessage ();
            
            if (error != null) {
                failWorks (db, r, error.getDescription ());
                throw new GisPollException (error);
            }
            
            final List<CapRemCommonResultType> importResult = state.getImportResult ();
            
            if (importResult == null || importResult.isEmpty ()) {
                failWorks (db, r, "Сервис ГИС ЖКХ вернул пустой результат");
                throw new GisPollException ("0", "Сервис ГИС ЖКХ вернул пустой результат");
            }
            
            int okCount = db.getInteger (OverhaulRegionalProgramHouseWorksImport.class, r.get ("import.uuid"), "ok_count");
            
            for (CapRemCommonResultType result: importResult) {
                
                List <CapRemCommonResultType.Error> err = result.getError ();
                if (err != null && !err.isEmpty ())
                    failWork (db, result.getTransportGUID (), err.get (0).getDescription ());
                else {
                    successWork (db, result.getTransportGUID (), result.getGUID (), result.getUniqueNumber ());
                    okCount++;
                }
                                
            }
            
            successProgram (db, r.get ("import.program_uuid").toString ());
            
            db.update (OverhaulRegionalProgramHouseWorksImport.class, HASH (
                "uuid", r.get ("import.uuid"),
                "ok_count", okCount
            ));
            
        }
        catch (GisPollRetryException ex) {
            return;
        }
        catch (GisPollException ex) {        
            ex.register (db, uuid, r);
        }
        
    }
    
    private void failWorks (DB db, Map <String, Object> r, String error) throws SQLException {
        
        List <Map <String, Object>> works = getWorks (db, r, VocGisStatus.i.PENDING_RP_PLACING);
        
        works.stream ()
                .forEach ((map) -> {
                    map.put (OverhaulRegionalProgramHouseWork.c.ID_ORPHW_STATUS.lc (),     VocGisStatus.i.FAILED_PLACING.getId ());
                    map.put (OverhaulRegionalProgramHouseWork.c.ID_ORPHW_STATUS_GIS.lc (), VocGisStatus.i.FAILED_PLACING.getId ());
                    map.put (OverhaulRegionalProgramHouseWork.c.IMPORT_ERR_TEXT.lc (),     error);
                });
        
        db.update (OverhaulRegionalProgramHouseWork.class, works);
        
    }
    
    private void failWork (DB db, String transportGUID, String error) throws SQLException {
        
        db.update (OverhaulRegionalProgramHouseWork.class, HASH (
            "uuid",                                                       transportGUID,
            OverhaulRegionalProgramHouseWork.c.ID_ORPHW_STATUS.lc (),     VocGisStatus.i.FAILED_PLACING.getId (),
            OverhaulRegionalProgramHouseWork.c.ID_ORPHW_STATUS_GIS.lc (), VocGisStatus.i.FAILED_PLACING.getId (),
            OverhaulRegionalProgramHouseWork.c.IMPORT_ERR_TEXT.lc (),     error
        ));
        
    }
    
    private void successWork (DB db, String transportGUID, String guid, String uniqueNumber) throws SQLException {
        
        db.update (OverhaulRegionalProgramHouseWork.class, HASH (
            "uuid", transportGUID,
            OverhaulRegionalProgramHouseWork.c.ID_ORPHW_STATUS.lc (),     VocGisStatus.i.APPROVED.getId (),
            OverhaulRegionalProgramHouseWork.c.ID_ORPHW_STATUS_GIS.lc (), VocGisStatus.i.APPROVED.getId (),
            OverhaulRegionalProgramHouseWork.c.IMPORT_ERR_TEXT.lc (),     null,
            OverhaulRegionalProgramHouseWork.c.GUID.lc (),                guid,
            OverhaulRegionalProgramHouseWork.c.UNIQUENUMBER.lc (),        uniqueNumber
        ));
        
    }
    
    private void successProgram (DB db, String programUUID) throws SQLException {
        
        db.update (OverhaulRegionalProgram.class, HASH (
            "uuid",                                                 programUUID,
            OverhaulRegionalProgram.c.ID_ORP_STATUS.lc (),          VocGisStatus.i.REGIONAL_PROGRAM_WORKS_PLACE_FINISHED.getId (),
            OverhaulRegionalProgram.c.ID_ORP_STATUS_GIS.lc (),      VocGisStatus.i.REGIONAL_PROGRAM_WORKS_PLACE_FINISHED.getId (),
            OverhaulRegionalProgram.c.LAST_SUCCESFULL_STATUS.lc (), VocGisStatus.i.REGIONAL_PROGRAM_WORKS_PLACE_FINISHED.getId ()
        ));
        
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