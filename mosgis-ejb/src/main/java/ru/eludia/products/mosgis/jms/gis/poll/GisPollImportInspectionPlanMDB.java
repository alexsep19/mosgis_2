package ru.eludia.products.mosgis.jms.gis.poll;

import static ru.eludia.base.DB.HASH;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;

import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.tables.CheckPlan;
import ru.eludia.products.mosgis.db.model.tables.CheckPlanLog;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.PlannedExamination;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.eludia.products.mosgis.util.StringUtils;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisInspectionClient;
import ru.gosuslugi.dom.schema.integration.base.CommonResultType;
import ru.gosuslugi.dom.schema.integration.inspection.GetStateResult;
import ru.gosuslugi.dom.schema.integration.inspection_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outImportInspectionPlanQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollImportInspectionPlanMDB  extends GisPollMDB {

    @EJB
    WsGisInspectionClient wsGisInspectionClient;

    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")                
            .toOne (CheckPlanLog.class,     "AS log", "uuid", "action", "id_ctr_status", "uuid_object").on ("log.uuid_out_soap=root.uuid")
            .toOne (VocOrganization.class, "AS org", "orgppaguid").on ("log.uuid_org=org.uuid")
        ;
    }

    @Override
    protected void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        if (DB.ok (r.get ("is_failed"))) throw new IllegalStateException (r.get ("err_text").toString ());
        
        UUID orgPPAGuid = (UUID) r.get ("org.orgppaguid");
        
        CheckPlan.Action action = CheckPlan.Action.forStatus (VocGisStatus.i.forId (r.get ("log.id_ctr_status")));
                
        try {
            
            GetStateResult state = getState (orgPPAGuid, r);
            
            if (state.getErrorMessage() != null) throw new GisPollException (state.getErrorMessage());
            
            final List<CommonResultType> commonResultList = state.getCommonResult();
            
            if (commonResultList.isEmpty ()) 
            	throw new GisPollException ("0", "Сервис ГИС ЖКХ вернул пустой результат");
            
            List<String> commonResultErrors = new ArrayList<>();
            
            String objectByTransportGuidStr = r.get("object_by_transport_guid").toString();
            JsonObject objectByTransportGuid;
            try (JsonReader jsonReader = Json.createReader(new StringReader(objectByTransportGuidStr))) {
                objectByTransportGuid = jsonReader.readObject();
            }
            
            db.begin ();
            
            for (CommonResultType commonResult : commonResultList) {
            	JsonObject object = objectByTransportGuid.getJsonObject(commonResult.getTransportGUID());
            	if (object == null) {
            		commonResultErrors.add("Из ГИС ЖКХ вернулся неизвестный идентификатор " + commonResult.getTransportGUID());
            		continue;
            	}
            	
            	CheckPlan.Objects planObject = CheckPlan.Objects.valueOf(object.getString("object"));
            	
            	if (!commonResult.getError().isEmpty()) {
                    String err = "";
                    for (CommonResultType.Error error : commonResult.getError()) {
                        if (StringUtils.isNotBlank(err)) err += "; ";
                        err += (error.getErrorCode() + " " + error.getDescription());
                    }
                    
                    if (CheckPlan.Objects.INSPECTION_PLAN.equals(planObject))
                    	err = planObject.getName() + " за " + object.getString("key") + " год: " + err;
                    else
                    	err = planObject.getName() + " " + object.getString("key") + ": " + err;
                    
                    commonResultErrors.add(err);
                    continue;
            	}
            	
            	if (CheckPlan.Objects.INSPECTION_PLAN.equals(planObject)) {
            		updateCheckPlan (db, uuid, r, HASH(
            			CheckPlan.c.ID_CTR_STATUS,      action.getOkStatus().getId(),
            			CheckPlan.c.INSPECTIONPLANGUID, commonResult.getGUID(),
            			CheckPlan.c.REGISTRYNUMBER,     commonResult.getUniqueNumber(),  
            			CheckPlan.c.GIS_UPDATE_DATE,    commonResult.getUpdateDate()));
				} else {
					db.update(PlannedExamination.class, HASH(
							EnTable.c.UUID,                              commonResult.getTransportGUID(), 
							PlannedExamination.c.PLANNEDEXAMINATIONGUID, commonResult.getGUID(),
							PlannedExamination.c.REGISTRYNUMBER,         commonResult.getUniqueNumber(), 
							PlannedExamination.c.GIS_UPDATE_DATE,        commonResult.getUpdateDate()));
				}
            }
            	
            if (!commonResultErrors.isEmpty())
            	throw new GisPollException ("0", commonResultErrors.stream().collect(Collectors.joining("; ")));
            	
            db.update (OutSoap.class, HASH (
                "uuid", uuid,
                "id_status", DONE.getId ()
            ));
            
            db.commit ();

        }
        catch (GisPollRetryException ex) {
            return;
        }
		catch (GisPollException ex) {
			updateCheckPlan(db, uuid, r, HASH(CheckPlan.c.ID_CTR_STATUS, action.getFailStatus().getId()));
			ex.register(db, uuid, r);
		}
        
    }

	private void updateCheckPlan(DB db, UUID uuid, Map<String, Object> r, Map<String, Object> h) throws SQLException {

		h.put("uuid", r.get("log.uuid_object"));
		db.update(CheckPlan.class, h);

		h.put("uuid", uuid);
		db.update(CheckPlanLog.class, h);

	}
    
	private GetStateResult getState(UUID orgPPAGuid, Map<String, Object> r)
			throws GisPollRetryException, GisPollException {

		GetStateResult rp;

		try {
			rp = wsGisInspectionClient.getState(orgPPAGuid, (UUID) r.get("uuid_ack"));
		} catch (Fault ex) {
			throw new GisPollException(ex.getFaultInfo());
		} catch (Throwable ex) {
			throw new GisPollException(ex);
		}

		checkIfResponseReady(rp);

		return rp;
	}
    
}