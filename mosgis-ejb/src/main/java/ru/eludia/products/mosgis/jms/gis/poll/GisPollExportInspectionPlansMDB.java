package ru.eludia.products.mosgis.jms.gis.poll;

import static ru.eludia.base.DB.HASH;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;

import java.sql.SQLException;
import java.time.LocalDate;
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

import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Operator;
import ru.eludia.base.db.util.TypeConverter;
import ru.eludia.products.mosgis.db.model.incoming.InVocOrganization;
import ru.eludia.products.mosgis.db.model.tables.CheckPlan;
import ru.eludia.products.mosgis.db.model.tables.CheckPlanLog;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.PlannedExamination;
import ru.eludia.products.mosgis.db.model.tables.PlannedExaminationFile;
import ru.eludia.products.mosgis.db.model.tables.PlannedExaminationLog;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPlannedExaminationFileType;
import ru.eludia.products.mosgis.db.model.voc.VocPlannedExaminationStatus;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.eludia.products.mosgis.util.StringUtils;
import ru.eludia.products.mosgis.ws.rest.clients.RestGisFilesClient;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisInspectionClient;
import ru.gosuslugi.dom.schema.integration.base.AttachmentType;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.inspection.ExportCancelledInfoWithAttachmentsType;
import ru.gosuslugi.dom.schema.integration.inspection.ExportExaminationChangeInfoType;
import ru.gosuslugi.dom.schema.integration.inspection.ExportInspectionPlanResultType;
import ru.gosuslugi.dom.schema.integration.inspection.ExportInspectionPlanResultType.PlannedExamination.PlannedExaminationInfo;
import ru.gosuslugi.dom.schema.integration.inspection.ExportPlannedExaminationType;
import ru.gosuslugi.dom.schema.integration.inspection.GetStateResult;
import ru.gosuslugi.dom.schema.integration.inspection.InspectionPlanStateType;
import ru.gosuslugi.dom.schema.integration.inspection.ScheduledExaminationSubjectInPlanInfoType;
import ru.gosuslugi.dom.schema.integration.inspection_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportInspectionPlansQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportInspectionPlansMDB  extends GisPollMDB {
    
    @EJB
    private WsGisInspectionClient wsGisInspectionClient;
    
    @EJB
    private RestGisFilesClient filesClient;
    
    @Resource (mappedName = "mosgis.inOrgQueue")
    private Queue inOrgQueue;
    
    @Resource (mappedName = "mosgis.outExportPlannedExaminationFilesQueue")
    private Queue exportFilesQueue;
       
    @Override
    protected void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        try {
            
            GetStateResult result = getState(r);

            ErrorMessageType errorMessage = result.getErrorMessage();
            if (errorMessage != null) {
                if ("INT002012".equals(errorMessage.getErrorCode())) {

                    logger.warning("Inspection plan not found");

                    db.update(OutSoap.class, HASH(
                            "uuid", uuid,
                            "id_status", DONE.getId()
                    ));

                    return;
                } else {
                    throw new GisPollException(errorMessage);
                }
            }
            
            Map<String, Object> org = db.getMap(db.getModel().select(VocOrganization.class, "*").where(VocOrganization.c.ORGPPAGUID, r.get("orgppaguid")));
            
            for (ExportInspectionPlanResultType inspectionPlan : result.getExportInspectionPlanResult()) {
                saveInspectionPlan(db, uuid, org.get("uuid"), inspectionPlan);
            }

            db.update (OutSoap.class, HASH (
                "uuid", uuid,
                "id_status", DONE.getId ()
            ));

        } catch (GisPollRetryException ex) {
        } catch (GisPollException ex) {            
            ex.register (db, uuid, r);
        }
        
    }
    
    private GetStateResult getState (Map<String, Object> r) throws GisPollRetryException, GisPollException {

        GetStateResult rp;
        
        try {
            rp = wsGisInspectionClient.getState ((UUID) r.get("orgppaguid"), (UUID) r.get ("uuid_ack"));
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
    
    private void saveInspectionPlan(DB db, UUID uuidOutSoap, Object orgUuid, ExportInspectionPlanResultType inspectionPlan) throws SQLException, GisPollRetryException {

    	Model m = db.getModel();
    	
    	final String uuid = inspectionPlan.getInspectionPlanGuid();
    	
    	Map<String, Object> checkPlanRecord = HASH(
				CheckPlan.c.INSPECTIONPLANGUID,        uuid, 
				CheckPlan.c.REGISTRYNUMBER,            inspectionPlan.getRegistryNumber(), 
				CheckPlan.c.SHOULDNOTBEREGISTERED,     inspectionPlan.getInspectionPlan().isShouldNotBeRegistered() != null ? true : false, 
				CheckPlan.c.SIGN,                      InspectionPlanStateType.SIGNED.equals(inspectionPlan.getPlanState()),
				CheckPlan.c.URIREGISTRATIONPLANNUMBER, inspectionPlan.getInspectionPlan().getURIRegistrationPlanNumber(), 
				CheckPlan.c.YEAR,                      inspectionPlan.getInspectionPlan().getYear()
		);
    	
		db.upsert(CheckPlan.class, checkPlanRecord, CheckPlan.c.INSPECTIONPLANGUID.lc());
		
		checkPlanRecord = db.getMap(m
    			.select(CheckPlan.class, "*")
    			.where(CheckPlan.c.INSPECTIONPLANGUID, uuid)
    			.and("is_deleted", 0));
		
		Object checkPlanRecordId = checkPlanRecord.get("uuid");
		
		UUID checkPlanRecordLogId = UUID.randomUUID();
		
		checkPlanRecord.put("uuid",          checkPlanRecordLogId);
		checkPlanRecord.put("uuid_object",   checkPlanRecordId);
		checkPlanRecord.put("uuid_out_soap", uuidOutSoap);
		checkPlanRecord.put("action",        VocAction.i.RELOAD);
		
		db.upsert(CheckPlanLog.class, checkPlanRecord);
		
		db.update(PlannedExamination.class, HASH(
				"uuid", checkPlanRecordId, 
				CheckPlan.c.ID_LOG, checkPlanRecordLogId), 
				"uuid");
		
		boolean needLoadOrg = false;
		
		for (ExportInspectionPlanResultType.PlannedExamination examination : inspectionPlan.getPlannedExamination()) {
			
			PlannedExaminationInfo examinationInfo = examination.getPlannedExaminationInfo();
			ExportPlannedExaminationType.PlannedExaminationInfo plannedExaminationInfo = examinationInfo.getPlannedExaminationInfo();
			
			Map<String, Object> examinationRecord = HASH(
					PlannedExamination.c.REGULATOR_UUID,         orgUuid,
					PlannedExamination.c.CHECK_PLAN_UUID,        checkPlanRecordId,
					PlannedExamination.c.CODE_VC_NSI_65,         examinationInfo.getOversightActivitiesRef() != null ? examinationInfo.getOversightActivitiesRef().getCode() : null,
					PlannedExamination.c.CODE_VC_NSI_68,         plannedExaminationInfo.getBase() != null ? plannedExaminationInfo.getBase().getCode() : null,
					PlannedExamination.c.CODE_VC_NSI_71,         plannedExaminationInfo.getExaminationForm().getCode(),
					PlannedExamination.c.ID_STATUS_GIS,          VocPlannedExaminationStatus.i.valueOf(examination.getState().name()).getId(),
					PlannedExamination.c.PLANNEDEXAMINATIONGUID, examination.getPlannedExaminationGuid(),
					PlannedExamination.c.REGISTRYNUMBER,         examination.getRegistryNumber()
			);
			
			ScheduledExaminationSubjectInPlanInfoType subject = examinationInfo.getSubject();
			if (subject.getIndividual() != null) {
				examinationRecord.putAll(TypeConverter.Map(subject.getIndividual()));
				examinationRecord.put(PlannedExamination.c.SUBJECT_UUID.lc(), subject.getIndividual().getOrgRootEntityGUID());
			} else if (subject.getOrganization() != null) {
				examinationRecord.putAll(TypeConverter.Map(subject.getOrganization()));
				examinationRecord.put(PlannedExamination.c.SUBJECT_UUID.lc(), subject.getOrganization().getOrgRootEntityGUID());
			}
			
			boolean hasOrg = checkOrg(db, examinationRecord.get(PlannedExamination.c.SUBJECT_UUID.lc()).toString());
			needLoadOrg = needLoadOrg || !hasOrg;
			
			ExportExaminationChangeInfoType changeInfo = examinationInfo.getExaminationChangeInfo();
			ExportCancelledInfoWithAttachmentsType cancelledInfo = examinationInfo.getCancelledInfo();
			
			List<AttachmentType> changeInfoAttachments = new ArrayList<>();
			List<AttachmentType> cancelledInfoAttachments = new ArrayList<>();
			
			if (changeInfo != null) {
				examinationRecord.putAll(TypeConverter.Map(changeInfo));
				examinationRecord.put(PlannedExamination.c.CHANGE_CODE_VC_NSI_271.lc(), changeInfo.getChangeReason().getCode());
				
				if (changeInfo.getOrganization() != null) {
					hasOrg = hasOrg & checkOrg(db, changeInfo.getOrganization().getOrgRootEntityGUID());
					needLoadOrg = needLoadOrg || !hasOrg;
					examinationRecord.put(PlannedExamination.c.CHANGE_ORG_UUID.lc(), changeInfo.getOrganization().getOrgRootEntityGUID());
				}
				
				changeInfoAttachments.addAll(changeInfo.getAttachments());
			}
			
			if (cancelledInfo != null) {
				examinationRecord.putAll(HASH(
						PlannedExamination.c.CANCEL_CODE_VC_NSI_271, cancelledInfo.getReason().getCode(),
						PlannedExamination.c.CANCELDATE, cancelledInfo.getDate(),
						PlannedExamination.c.CANCELDECISIONNUMBER, cancelledInfo.getNumber(),
						PlannedExamination.c.ADDITIONCANCELINFO, cancelledInfo.getAdditionalInfo()
						));
				
				if (cancelledInfo.getOrganisation() != null) {
					hasOrg = hasOrg & checkOrg(db, cancelledInfo.getOrganisation().getOrgRootEntityGUID());
					needLoadOrg = needLoadOrg || !hasOrg;
					examinationRecord.put(PlannedExamination.c.CHANGE_ORG_UUID.lc(), cancelledInfo.getOrganisation().getOrgRootEntityGUID());
				}
				
				cancelledInfoAttachments.addAll(cancelledInfo.getAttachments());
			}
			
			if (hasOrg) {
				examinationRecord.putAll(TypeConverter.Map(examinationInfo));
				examinationRecord.putAll(TypeConverter.Map(examinationInfo.getRegulatoryAuthorityInformation()));
				examinationRecord.putAll(TypeConverter.Map(plannedExaminationInfo));
				examinationRecord.putAll(TypeConverter.Map(plannedExaminationInfo.getDuration()));
				
				db.upsert(PlannedExamination.class, examinationRecord, PlannedExamination.c.PLANNEDEXAMINATIONGUID.lc());
				
				examinationRecord = db.getMap(m
		    			.select(PlannedExamination.class, "*")
		    			.where(PlannedExamination.c.PLANNEDEXAMINATIONGUID, examination.getPlannedExaminationGuid())
		    			.and("is_deleted", 0));
				
				Object examinationRecordId = examinationRecord.get("uuid");
				
				UUID examinationRecordLogId = UUID.randomUUID();
				
				examinationRecord.put("uuid",          examinationRecordLogId);
				examinationRecord.put("uuid_object",   examinationRecordId);
				examinationRecord.put("uuid_out_soap", uuidOutSoap);
				examinationRecord.put("action",        VocAction.i.RELOAD);
				
				db.upsert(PlannedExaminationLog.class, examinationRecord);
				
				db.update(PlannedExamination.class, HASH(
						"uuid", examinationRecordId, 
						PlannedExamination.c.ID_LOG, examinationRecordLogId), 
						"uuid");
				
				PlannedExaminationFile.Sync plannedExaminationFiles = 
						((PlannedExaminationFile) m.get(PlannedExaminationFile.class))
						.new Sync(db, (UUID) examinationRecordId, filesClient, exportFilesQueue);
				plannedExaminationFiles.addAll(changeInfoAttachments, VocPlannedExaminationFileType.i.CHANGE);
				plannedExaminationFiles.addAll(cancelledInfoAttachments, VocPlannedExaminationFileType.i.CANCEL);
				plannedExaminationFiles.sync();
			}
		}
		
		if (needLoadOrg) {
			uuidPublisher.publish (getOwnQueue (), getUuid ());
			throw new GisPollRetryException ();
		}

    }
    
	private boolean checkOrg(DB db, String uuid) throws SQLException {

		Map<String, Object> org = db.getMap(VocOrganization.class, uuid);

		if (org != null && !org.isEmpty())
			return true;

		logger.log(Level.SEVERE, "Не найдена организация по идентификатору  " + uuid);

		String inVocOrganizationUuid = db.getString(db.getModel()
				.select(InVocOrganization.class, InVocOrganization.c.UUID.lc())
				.where(InVocOrganization.c.ORGROOTENTITYGUID.lc(), uuid)
				.and(InVocOrganization.c.TS, Operator.GT, LocalDate.now().minusDays(5))
				.orderBy(InVocOrganization.c.TS.lc() + " DESC"));

		boolean getOrg = false;
		if (StringUtils.isNotBlank(inVocOrganizationUuid)) {
			Integer outSoapStatus = db.getInteger(OutSoap.class, inVocOrganizationUuid, "id_status");
			if (outSoapStatus == 3)
				getOrg = true;
		} else
			getOrg = true;

		if (getOrg) {
			UUID orgQueueUuid = (UUID) db.insertId(InVocOrganization.class,
					HASH(InVocOrganization.c.ORGROOTENTITYGUID, uuid));

			uuidPublisher.publish(inOrgQueue, orgQueueUuid);
		}

		return false;
	}
    
}
