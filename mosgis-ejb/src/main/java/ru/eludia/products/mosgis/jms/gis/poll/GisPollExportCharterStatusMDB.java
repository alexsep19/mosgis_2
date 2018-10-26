package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.Model;
import ru.eludia.products.mosgis.db.model.tables.Charter;
import ru.eludia.products.mosgis.db.model.tables.CharterObject;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.ejb.wsc.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.eludia.products.mosgis.rest.api.CharterLocal;
import ru.gosuslugi.dom.schema.integration.house_management.ExportStatusCAChResultType;
import ru.gosuslugi.dom.schema.integration.house_management.GetStateResult;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportHouseCharterStatusQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportCharterStatusMDB extends UUIDMDB<OutSoap> {
    
    private static Logger logger = java.util.logging.Logger.getLogger (GisPollExportCharterStatusMDB.class.getName ());    

    @EJB
    WsGisHouseManagementClient wsGisHouseManagementClient;

    @Resource (mappedName = "mosgis.outExportHouseCharterStatusQueue")
    Queue queue;
    
    @EJB
    protected UUIDPublisher UUIDPublisher;

    @EJB
    CharterLocal mgmtCharter;


    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {

        GetStateResult rp;

        try {
            rp = wsGisHouseManagementClient.getState ((UUID) r.get ("orgppaguid"), (UUID) r.get ("uuid_ack"));
        }
        catch (Fault ex) {
            throw new IllegalStateException (ex);
/*                
            final ru.gosuslugi.dom.schema.integration.base.Fault faultInfo = ex.getFaultInfo ();
            throw new FU (faultInfo.getErrorCode (), faultInfo.getErrorMessage (), VocGisStatus.i.FAILED_STATE);
*/                
        }

        if (rp.getRequestState () < DONE.getId ()) {
            logger.info ("requestState = " + rp.getRequestState () + ", retry...");
            UUIDPublisher.publish (queue, uuid);
            return;
        }

        List <UUID> toPromote = processGetStateResponse (rp, db, uuid, false);

        for (UUID id: toPromote) mgmtCharter.doPromote (id.toString ());

    }        

    public static List<UUID> processGetStateResponse (GetStateResult rp, DB db, UUID uuid, boolean versionsOnly) throws SQLException {
        
        List <Map <String, Object>> charterRecords = new ArrayList<> (rp.getExportCAChResult ().size ());
        List <Map <String, Object>> objectRecords   = new ArrayList<> (rp.getExportCAChResult ().size ());
        List <UUID> toPromote = new ArrayList<> ();
        Model m = db.getModel ();        
        for (ExportStatusCAChResultType er: rp.getExportStatusCAChResult ()) processExportStatus (er, db, m, toPromote, versionsOnly, charterRecords, objectRecords);
        db.update (Charter.class, charterRecords);
        db.upsert (CharterObject.class, objectRecords, "uuid_charter", "fiashouseguid");
        db.update (OutSoap.class, HASH (
            "uuid", uuid,
            "id_status", DONE.getId ()
        ));
        
        return toPromote;
        
    }   
    
    private static boolean processExportStatus (ExportStatusCAChResultType er, DB db, Model m, List<UUID> toPromote, boolean versionsOnly, List<Map<String, Object>> charterRecords, List<Map<String, Object>> objectRecords) throws SQLException {
        
        final String charterGUID = er.getCharterGUID ();
        
        String sUid = db.getString (m.select (Charter.class, "uuid").where ("charterguid", charterGUID));
        if (sUid == null) {
            logger.warning ("Charter not found: " + charterGUID);
            return true;
        }
        
        UUID uuidCharter = DB.to.UUIDFromHex (sUid);
        
        VocGisStatus.i status = VocGisStatus.i.forName (er.getCharterStatus ().value ());
        
        if (status == null) {
            logger.warning ("Unknown status: '" + er.getCharterStatus () + "'. Will use FAILED_STATE instead.");
            status = VocGisStatus.i.FAILED_STATE;
        }
        
        if (status == VocGisStatus.i.REVIEWED) toPromote.add (uuidCharter);
        
        final Map<String, Object> ctr = HASH (
            "uuid",               uuidCharter,
            "charterversionguid", er.getCharterVersionGUID ()
        );
        
        if (!versionsOnly) {
            
            ctr.put ("id_ctr_status", status.getId ());
            ctr.put ("id_ctr_status_gis", status.getId ());
            ctr.put ("versionnumber", er.getVersionNumber ());
            
            VocGisStatus.i state = VocGisStatus.i.forName (er.getState ());
            if (state != null) ctr.put ("id_ctr_state_gis", state.getId ());
            
        }
        
        charterRecords.add (ctr);
        
        for (ExportStatusCAChResultType.ContractObject co: er.getContractObject ()) {
            
            VocGisStatus.i os = VocGisStatus.i.forName (co.getManagedObjectStatus ().value ());
            if (os == null) {
                logger.warning ("Unknown status: '" + co.getManagedObjectStatus () + "'. Will use FAILED_STATE instead.");
                os = VocGisStatus.i.FAILED_STATE;
            }
            
            final Map<String, Object> or = HASH (
                "uuid_charter",              uuidCharter,
                "fiashouseguid",             co.getFIASHouseGuid (),
                "isconflicted",              Boolean.TRUE.equals (co.isIsConflicted ()) ? 1 : 0,
                "isblocked",                 Boolean.TRUE.equals (co.isIsBlocked ()) ? 1 : 0,
                "contractobjectversionguid", co.getContractObjectVersionGUID ()
            );
                        
            if (!versionsOnly) or.put ("id_ctr_status_gis", os.getId ());
            
            objectRecords.add (or);
            
        }
        
        return false;
        
    }
    
//    private static String [] objectKey   = {"uuid_charter", "fiashouseguid"};
    
}
