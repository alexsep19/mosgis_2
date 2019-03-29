package ru.eludia.products.mosgis.jms.gis.send;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;
import javax.json.Json;
import javax.json.JsonObjectBuilder;

import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.base.db.util.TypeConverter;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.tables.CheckPlan;
import ru.eludia.products.mosgis.db.model.tables.CheckPlanLog;
import ru.eludia.products.mosgis.db.model.tables.PlannedExamination;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationTypes;
import ru.eludia.products.mosgis.jms.UUIDPublisher;
import ru.eludia.products.mosgis.jms.gis.send.base.GisExportMDB;
import ru.eludia.products.mosgis.util.XmlUtils;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisInspectionClient;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.inspection.ImportInspectionPlanRequest.ImportInspectionPlan.ImportPlannedExamination;
import ru.gosuslugi.dom.schema.integration.inspection.InspectionPlanType;
import ru.gosuslugi.dom.schema.integration.inspection.PlannedExaminationType;
import ru.gosuslugi.dom.schema.integration.inspection.PlannedExaminationType.PlannedExaminationInfo;
import ru.gosuslugi.dom.schema.integration.inspection.PlannedExaminationType.PlannedExaminationInfo.Duration;
import ru.gosuslugi.dom.schema.integration.inspection.PlannedExaminationType.RegulatoryAuthorityInformation;
import ru.gosuslugi.dom.schema.integration.inspection.ScheduledExaminationSubjectInPlanInfoType;
import ru.gosuslugi.dom.schema.integration.inspection.ScheduledExaminationSubjectInPlanInfoType.Individual;
import ru.gosuslugi.dom.schema.integration.inspection.ScheduledExaminationSubjectInPlanInfoType.Organization;
import ru.gosuslugi.dom.schema.integration.inspection_service_async.Fault;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inImportInspectionPlanQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ImportInspectionPlanMDB extends GisExportMDB<CheckPlanLog> {

    @EJB
    private UUIDPublisher UUIDPublisher;

    @EJB
    private WsGisInspectionClient wsGisInspectionClient;
    
    @Resource (mappedName = "mosgis.outImportInspectionPlanQueue")
    private Queue getStateQueue;
    
    @Override
    protected Get get (UUID uuid) {
    	return (Get) ModelHolder.getModel()
    			.get(getTable(), uuid, "AS root", "*")
                .toOne (VocOrganization.class, "AS org", "orgppaguid").on("root.uuid_org = org.uuid");
    }
    
    AckRequest.Ack invoke(DB db, CheckPlan.Action action, UUID messageGUID, Map<String, Object> r) throws Fault, SQLException {

		UUID orgPPAGuid = (UUID) r.get("org.orgppaguid");

		switch (action) {
			case SEND_TO_GIS:
				fillData(db, r);
				return wsGisInspectionClient.importInspectionPlan(orgPPAGuid, messageGUID, r);
			default:
				throw new IllegalArgumentException("No action implemented for " + action);
		}

	}
    
    private void fillData(DB db, Map<String, Object> r) throws SQLException {
    	
    	List<Map<String, Object>> examinations = db.getList(db.getModel()
    			.select(PlannedExamination.class, "*")
				.toOne(VocOrganization.class, "AS subject", VocOrganization.c.UUID.lc(), VocOrganization.c.ID_TYPE.lc())
					.on(PlannedExamination.c.SUBJECT_UUID.lc())
    			.where(PlannedExamination.c.CHECK_PLAN_UUID, r.get("uuid_object"))
    			.and("is_deleted", 0));
    	
    	if (examinations.isEmpty())
    		throw new IllegalArgumentException("В плане должна быть хотя бы одна проверка");
    	
    	JsonObjectBuilder objectByTransportGuid = Json.createObjectBuilder();
        r.put("object_by_transport_guid", objectByTransportGuid);
    	
    	InspectionPlanType inspectionPlan = new InspectionPlanType();
    	if (TypeConverter.Boolean(r.get(CheckPlan.c.SHOULDNOTBEREGISTERED.lc())))
    		inspectionPlan.setShouldNotBeRegistered(true);
    	else {
    		inspectionPlan.setShouldBeRegistered(true);
    		inspectionPlan.setURIRegistrationPlanNumber(new BigInteger(r.get(CheckPlan.c.URIREGISTRATIONPLANNUMBER.lc()).toString()));
    	}
    	inspectionPlan.setSign(true);
    	inspectionPlan.setYear(Short.valueOf(r.get(CheckPlan.c.YEAR.lc()).toString()));
    	
    	r.put("inspectionplan", inspectionPlan);
    	r.put("transportguid", r.get("uuid_object"));
    	
		objectByTransportGuid.add(r.get("uuid_object").toString(),
				Json.createObjectBuilder()
						.add("object", CheckPlan.Objects.INSPECTION_PLAN.name())
						.add("key", r.get(CheckPlan.c.YEAR.lc()).toString())
						.build());
    	
    	List<ImportPlannedExamination> importPlannedExaminations = new ArrayList<>();
    	for (Map<String, Object> examination : examinations) {
    		ImportPlannedExamination importPlannedExamination = new ImportPlannedExamination();
    		
    		// TODO importPlannedExamination.setAnnulPlannedExamination(value);
    		// TODO importPlannedExamination.setCancelPlannedExamination(value);
    		// TODO importPlannedExamination.setExaminationChangeInfo(value);
    		
			examination.put("oversightactivitiesref", XmlUtils.createNsiRef(65, examination.get(PlannedExamination.c.CODE_VC_NSI_65.lc()).toString()));
			
			examination.put("base", XmlUtils.createNsiRef(68, examination.get(PlannedExamination.c.CODE_VC_NSI_68.lc()).toString()));
			examination.put("examinationform", XmlUtils.createNsiRef(71, examination.get(PlannedExamination.c.CODE_VC_NSI_71.lc()).toString()));
			examination.put("duration", TypeConverter.javaBean(Duration.class, examination));
			examination.put("plannedexaminationinfo", TypeConverter.javaBean(PlannedExaminationInfo.class, examination));
			
			examination.put("regulatoryauthorityinformation", TypeConverter.javaBean(RegulatoryAuthorityInformation.class, examination));
			
			ScheduledExaminationSubjectInPlanInfoType subject = new ScheduledExaminationSubjectInPlanInfoType();
			if (VocOrganizationTypes.i.ENTPS.equals(VocOrganizationTypes.i.forId(examination.get("subject.id_type")))) {
				Individual individual = TypeConverter.javaBean(Individual.class, examination);
				individual.setOrgRootEntityGUID(examination.get("subject.uuid").toString());
				subject.setIndividual(individual);
				
			} else {
				Organization organization = TypeConverter.javaBean(Organization.class, examination);
				organization.setOrgRootEntityGUID(examination.get("subject.uuid").toString());
				subject.setOrganization(organization);
			}
			examination.put("subject", subject);
			
    		importPlannedExamination.setPlannedExamination(TypeConverter.javaBean(PlannedExaminationType.class, examination));
    		
    		Object plannedExaminationGuid = examination.get(PlannedExamination.c.PLANNEDEXAMINATIONGUID.lc());
    		if (plannedExaminationGuid != null)
    			importPlannedExamination.setPlannedExaminationGuid(plannedExaminationGuid.toString());
    		
    		importPlannedExamination.setTransportGUID(examination.get("uuid").toString());
    		objectByTransportGuid.add(examination.get("uuid").toString(),
    				Json.createObjectBuilder()
    						.add("object", CheckPlan.Objects.PLANNED_EXAMINATION.name())
    						.add("key", examination.get(PlannedExamination.c.NUMBERINPLAN.lc()).toString())
    						.build());
    		
    		importPlannedExaminations.add(importPlannedExamination);
    	}
    	r.put("importplannedexamination", importPlannedExaminations);
    	
    	String objectByTransportGuidStr = objectByTransportGuid.build().toString();
        r.put("object_by_transport_guid", objectByTransportGuidStr);
    }
    
	@Override
	protected void handleRecord(DB db, UUID uuid, Map<String, Object> r) throws SQLException {

		VocGisStatus.i status = VocGisStatus.i.forId(r.get(CheckPlan.c.ID_STATUS.lc()));

		CheckPlan.Action action = CheckPlan.Action.forStatus(status);

		if (action == null) {
			logger.warning("No action is implemented for " + status);
			return;
		}

		try {
			AckRequest.Ack ack = invoke(db, action, uuid, r);
			store(db, ack, r, action.getNextStatus());
			uuidPublisher.publish(getStateQueue, ack.getRequesterMessageGUID());
		} catch (Fault ex) {
			logger.log(Level.SEVERE, "Can't place inspection plan", ex);
			fail(db, ex.getFaultInfo(), r, action.getFailStatus());
			return;
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Cannot invoke WS", ex);
			fail(db, action.toString(), action.getFailStatus(), ex, r);
			return;
		}
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
		return CheckPlan.c.ID_STATUS.getCol();
	}
}
