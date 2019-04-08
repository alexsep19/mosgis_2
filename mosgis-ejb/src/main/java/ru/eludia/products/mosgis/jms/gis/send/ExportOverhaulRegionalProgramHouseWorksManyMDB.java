package ru.eludia.products.mosgis.jms.gis.send;

import java.sql.SQLException;
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
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramHouseWork;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramHouseWorksImport;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.jms.gis.send.base.GisExportMDB;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisCapitalRepairClient;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.capital_repair_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inExportOverhaulRegionalProgramHouseWorksManyQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportOverhaulRegionalProgramHouseWorksManyMDB extends GisExportMDB <OverhaulRegionalProgramHouseWorksImport> {
    
    private final int CAPACITY = 500;
    
    @EJB
    WsGisCapitalRepairClient wsGisCapitalRepairClient;
    
    @Resource (mappedName = "mosgis.outExportOverhaulRegionalProgramHouseWorksManyQueue")
    Queue outExportOverhaulRegionalProgramHouseWorksManyQueue;
    
    AckRequest.Ack invoke (DB db, UUID messageGUID,  Map<String, Object> r) throws Fault, SQLException {
        
        UUID orgPPAGuid = (UUID) r.get ("orgppaguid");
        return wsGisCapitalRepairClient.importRegionalProgramWork (orgPPAGuid, messageGUID, r);
        
    }
    
    @Override
    protected void handleRecord(DB db, UUID uuid, Map<String, Object> r) throws Exception {
        
        r = OverhaulRegionalProgramHouseWorksImport.getForExport (db, uuid.toString ());
        logger.info ("r=" + DB.to.json (r));
        
        List <Map <String, Object>> works = (List <Map <String, Object>>) r.get ("works");
        
        final int size = works.size ();
        final int partsCount = size / CAPACITY + ((size % CAPACITY == 0) ? 0 : 1);
        
        db.update (getTable (), DB.HASH (
            "uuid", r.get ("uuid"),
            "count", size,
            "ok_count", 0
        ));
        
        for (int i = 0; i < partsCount; i++) {
            
            final int from = i * CAPACITY;
            final int to   = (i + 1) * CAPACITY;
            
            r.put ("works", works.subList (from, (to > size ? size : to)));
            logger.info ("{part} r=" + DB.to.json (r));
            
            try {            
                AckRequest.Ack ack = invoke (db, uuid, r);
                store (db, ack, r);
                uuidPublisher.publish (outExportOverhaulRegionalProgramHouseWorksManyQueue, ack.getRequesterMessageGUID ());
            }
            catch (Fault ex) {
                logger.log (Level.SEVERE, "Can't place regional program works", ex);
                fail (db, ex.getFaultInfo (), r);
                return;
            }
            catch (Exception ex) {            
                logger.log (Level.SEVERE, "Cannot invoke WS", ex);            
                fail (db, ex, r);
                return;            
            }
            
        }
        
    }
    
    private void worksChangeStatus (Map <String, Object> r, VocGisStatus.i status) {
        
        ((List <Map <String, Object>>) r.get ("works")).stream ()
                .forEach((map) -> {
                    map.put ("id_orphw_status", status);
                    map.put ("od_orphw_status_gis", status);
                });
        
    }

    protected void store (DB db, AckRequest.Ack ack, Map<String, Object> r) throws SQLException {
        
        worksChangeStatus (r, VocGisStatus.i.PENDING_RP_PLACING);
        
        Object uuid = r.get ("uuid");
        
        db.begin ();
        
            OutSoap.registerAck (db, ack);

            db.update (getTable (), DB.HASH (
                "uuid",          uuid,
                "uuid_out_soap", uuid,
                "uuid_message",  ack.getMessageGUID ()
            ));
            
            db.update (OverhaulRegionalProgramHouseWork.class, (List <Map <String, Object>>) r.get ("works"));
        
        db.commit ();
        
    }
    
    protected void fail (DB db, ru.gosuslugi.dom.schema.integration.base.Fault faultInfo, Map<String, Object> r) throws SQLException {

        worksChangeStatus (r, VocGisStatus.i.FAILED_PLACING);
        
        Object uuid = r.get ("uuid");

        db.begin ();
        
            OutSoap.registerFault (db, uuid, faultInfo);

            db.update (getTable (), DB.HASH (
                "uuid",          uuid,
                "uuid_out_soap", uuid
            ));
            
            db.update (OverhaulRegionalProgramHouseWork.class, (List <Map <String, Object>>) r.get ("works"));
        
        db.commit ();
        
    }
    
    protected void fail (DB db, Exception ex, Map<String, Object> r) throws SQLException {
        
        worksChangeStatus (r, VocGisStatus.i.FAILED_PLACING);
        
        Object uuid = r.get ("uuid");
        
        db.begin ();
        
            OutSoap.registerException (db, uuid, getClass ().getName (), "importRegionalProgramWork", ex);
        
            db.update (getTable (), DB.HASH (
                "uuid",          uuid,
                "uuid_out_soap", uuid
            ));
            
            db.update (OverhaulRegionalProgramHouseWork.class, (List <Map <String, Object>>) r.get ("works"));
        
        db.commit ();
        
    }

    @Override
    protected Queue getFilesQueue() {
        return null;
    }

    @Override
    protected Table getFileLogTable() {
        return null;
    }

    @Override
    protected Col getStatusCol() {
        return null;
    }
    
}
