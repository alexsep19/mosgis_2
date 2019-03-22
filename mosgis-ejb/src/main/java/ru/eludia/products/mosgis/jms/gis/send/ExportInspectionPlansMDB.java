package ru.eludia.products.mosgis.jms.gis.send;

import static ru.eludia.base.DB.HASH;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;

import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.base.db.util.TypeConverter;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.incoming.InInspectionPlans;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationNsi20;
import ru.eludia.products.mosgis.db.model.voc.VocUser;
import ru.eludia.products.mosgis.jms.UUIDPublisher;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisInspectionClient;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.inspection_service_async.Fault;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inExportInspectionPlansQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportInspectionPlansMDB extends UUIDMDB<InInspectionPlans> {

    @EJB
    protected UUIDPublisher UUIDPublisher;

    @EJB
    protected WsGisInspectionClient wsGisInspectionClient;
    
    @Resource (mappedName = "mosgis.outExportInspectionPlansQueue")
    private Queue queue;
    
    @Override
    protected Get get(UUID uuid) {
        return (Get) ModelHolder.getModel().get(getTable(), uuid, "AS root", "*")
        		.toOne (VocUser.class, "AS usr").on()
        		.toMaybeOne(VocOrganization.class, "AS org", "orgppaguid").on("usr.uuid_org = org.uuid");
    }
    
    @Override
    protected void handleRecord(DB db, UUID uuid, Map<String, Object> r) throws SQLException {
    	
    	List<UUID> orgPPAGuids = new ArrayList<>(); 
    	
    	if (r.get("org.orgppaguid") == null) {
    		
    		db.forEach(db.getModel().select(VocOrganizationNsi20.class, "AS root", "*").where("is_deleted",0)
    		.toOne(VocOrganization.class, "AS org", "orgppaguid").where("is_deleted",0).on()
    		.where("code IN", new Object[]{"4", "5"}), (rs) -> { //4 - ГЖИ, 5 - ОМЖК
    			if (rs.getString("org.orgppaguid") != null)
    				orgPPAGuids.add(TypeConverter.UUIDFromHex(rs.getString("org.orgppaguid")));
    		});
    	} else 
    		orgPPAGuids.add((UUID)r.get("org.orgppaguid"));
    	
    	for (UUID orgPPAGuid : orgPPAGuids) {
    	
	    	UUID outSoapUuid = UUID.randomUUID();
	    	
    		try {
	            
	            AckRequest.Ack ack = wsGisInspectionClient.exportInspectionPlans(orgPPAGuid, outSoapUuid, r);
	            
	            db.update (OutSoap.class, DB.HASH (
	                "uuid",     outSoapUuid,
	                "uuid_ack", ack.getMessageGUID ()
	            ));
	            
	            UUIDPublisher.publish (queue, outSoapUuid);
	            
	        }
	        catch (Fault ex) {
	            
	            db.update (OutSoap.class, HASH (
	                "uuid", outSoapUuid,
	                "id_status", DONE.getId (),
	                "is_failed", 1,
	                "err_code",  ex.getFaultInfo ().getErrorCode (),
	                "err_text",  ex.getFaultInfo ().getErrorMessage ()
	            ));
	            
	            Logger.getLogger (ExportInspectionPlansMDB.class.getName()).log (Level.SEVERE, null, ex);
	        }
	        catch (Exception ex) {
	            
	            db.update (OutSoap.class, HASH (
	                "uuid", outSoapUuid,
	                "id_status", DONE.getId (),
	                "is_failed", 1,
	                "err_code",  "0",
	                "err_text",  ex.getMessage ()
	            ));            
	            
	            Logger.getLogger (ExportInspectionPlansMDB.class.getName()).log (Level.SEVERE, null, ex);
	            
	        }
    	}
    }

    

}
