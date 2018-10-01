package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.ArrayList;
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
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.ContractObject;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.ejb.wsc.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.eludia.products.mosgis.rest.api.MgmtContractLocal;
import ru.gosuslugi.dom.schema.integration.house_management.ExportStatusCAChResultType;
import ru.gosuslugi.dom.schema.integration.house_management.GetStateResult;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportHouseMgmtContractStatusQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportMgmtContractStatusMDB extends UUIDMDB<OutSoap> {

    @EJB
    WsGisHouseManagementClient wsGisHouseManagementClient;

    @Resource (mappedName = "mosgis.outExportHouseMgmtContractStatusQueue")
    Queue queue;
    
    @EJB
    protected UUIDPublisher UUIDPublisher;

    @EJB
    MgmtContractLocal mgmtContract;


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

        List <Map <String, Object>> contractRecors = new ArrayList<> (rp.getExportCAChResult ().size ());
        List <Map <String, Object>> objectRecors   = new ArrayList<> (rp.getExportCAChResult ().size ());
        List <UUID> toPromote = new ArrayList<> ();                    
        
        Model m = db.getModel ();

        for (ExportStatusCAChResultType er: rp.getExportStatusCAChResult ()) {
            
            final String contractGUID = er.getContractGUID ();
            
            String sUid = db.getString (m.select (Contract.class, "uuid").where ("contractguid", contractGUID));
            
            if (sUid == null) {
                logger.warning ("Contract not found: " + contractGUID);
                continue;
            }
            
            UUID uuidContract = DB.to.UUIDFromHex (sUid);

            VocGisStatus.i status = VocGisStatus.i.forName (er.getContractStatus ().value ());

            if (status == null) {
                logger.warning ("Unknown status: '" + er.getContractStatus () + "'. Will use FAILED_STATE instead.");
                status = VocGisStatus.i.FAILED_STATE;
            }                                

            if (status == VocGisStatus.i.REVIEWED) toPromote.add (uuidContract);                
            
            final Map<String, Object> ctr = HASH (
                "uuid",                uuidContract,
                "contractversionguid", er.getContractVersionGUID (),
                "id_ctr_status",       status.getId (),
                "id_ctr_status_gis",   status.getId ()
            );
            
            VocGisStatus.i state = VocGisStatus.i.forName (er.getState ());
            if (state != null) ctr.put ("id_ctr_state_gis", state.getId ());

            contractRecors.add (ctr);                                
            
            for (ExportStatusCAChResultType.ContractObject co: er.getContractObject ()) {
                
                VocGisStatus.i os = VocGisStatus.i.forName (co.getManagedObjectStatus ().value ());
                if (os == null) {
                    logger.warning ("Unknown status: '" + co.getManagedObjectStatus () + "'. Will use FAILED_STATE instead.");
                    os = VocGisStatus.i.FAILED_STATE;
                }
                
                objectRecors.add (HASH (
                    "uuid_contract",             uuidContract,
                    "fiashouseguid",             co.getFIASHouseGuid (),
                    "id_ctr_status_gis",         os.getId (),
                    "contractobjectversionguid", co.getContractObjectVersionGUID ()
                ));                
                
            }

        }

        db.update (Contract.class, contractRecors);
        db.upsert (ContractObject.class, objectRecors, objectKey);
        
        db.update (OutSoap.class, HASH (
            "uuid", uuid,
            "id_status", DONE.getId ()
        ));

        for (UUID id: toPromote) mgmtContract.doPromote (id.toString ());

    }        
    
    private static String [] objectKey   = {"uuid_contract", "fiashouseguid"};
    
}
