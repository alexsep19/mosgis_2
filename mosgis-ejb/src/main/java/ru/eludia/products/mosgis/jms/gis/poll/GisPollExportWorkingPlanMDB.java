package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.tables.Charter;
import ru.eludia.products.mosgis.db.model.tables.CharterObject;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.ContractObject;
import ru.eludia.products.mosgis.db.model.tables.WorkingPlan;
import ru.eludia.products.mosgis.db.model.tables.WorkingPlanLog;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.ReportingPeriod;
import ru.eludia.products.mosgis.db.model.tables.WorkingList;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.gosuslugi.dom.schema.integration.services_service_async.Fault;
import ru.eludia.products.mosgis.db.model.tables.WorkingPlan.c;
import ru.eludia.products.mosgis.db.model.tables.WorkingPlanItem;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.ejb.wsc.WsGisServicesClient;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.services.ExportWorkingPlanResultType;
import ru.gosuslugi.dom.schema.integration.services.GetStateResult;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportWorkingPlansQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportWorkingPlanMDB  extends GisPollMDB {

    @EJB
    WsGisServicesClient wsGisServicesClient;
    
    @Resource (mappedName = "mosgis.inWorkingPlansQueue")
    Queue inWorkingPlansQueue;

    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")                
                
            .toOne (WorkingPlanLog.class,     "AS log", "uuid", "action").on ("log.uuid_out_soap=root.uuid")
            .toOne (WorkingPlan.class,        "AS r", "uuid").on ()
            .toOne (WorkingList.class,        "AS l").on ()
                
            .toMaybeOne (ContractObject.class, "AS co").on ()
            .toMaybeOne (Contract.class, "AS ctr"
                , "contractguid AS contractguid"
            ).on ()
            .toMaybeOne (VocOrganization.class, "AS o1"
                , "orgppaguid AS orgppaguid_1"
            ).on ("ctr.uuid_org=o1.uuid")
                
            .toMaybeOne (CharterObject.class, "AS co").on ()
            .toMaybeOne (Charter.class, "AS ch"
                , "charterguid AS charterguid"
            ).on ()
            .toMaybeOne (VocOrganization.class, "AS o2"
                , "orgppaguid AS orgppaguid_2"
            ).on ("ch.uuid_org=o1.uuid")
        ;
    }

    @Override
    protected void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        if (DB.ok (r.get ("is_failed"))) throw new IllegalStateException (r.get ("err_text").toString ());
        
        UUID orgPPAGuid = (UUID) r.get ("orgppaguid_1");
        if (orgPPAGuid == null) orgPPAGuid = (UUID) r.get ("orgppaguid_2");
        
        WorkingPlan.Action action = WorkingPlan.Action.forLogAction (VocAction.i.forName (r.get ("log.action").toString ()));
                
        try {
            
            GetStateResult state = getState (orgPPAGuid, r);
            
            ErrorMessageType errorMessage = state.getErrorMessage ();
            
            if (errorMessage != null) throw new GisPollException (errorMessage);            
            
            List<ExportWorkingPlanResultType> exportWorkingPlanResult = state.getExportWorkingPlanResult ();            
                        
            if (exportWorkingPlanResult == null || exportWorkingPlanResult.isEmpty ()) throw new GisPollException ("0", "Сервис ГИС ЖКХ вернул пустой результат");
            
            ExportWorkingPlanResultType plan = exportWorkingPlanResult.get (0);
            
            if (plan == null) throw new GisPollException ("0", "Сервис ГИС ЖКХ вернул пустой результат");
            
            ExportWorkingPlanResultType.WorkingPlan workingPlan = plan.getWorkingPlan ();
            
            if (workingPlan == null) throw new GisPollException ("0", "Сервис ГИС ЖКХ вернул пустой результат");
            
            if (action == WorkingPlan.Action.PLACING) {
                
                db.update (WorkingPlanItem.class, HASH (
                    WorkingPlanItem.c.UUID_WORKING_PLAN, r.get ("r.uuid"),
                    WorkingPlanItem.c.UUID_REPORTING_PERIOD, null
                ), WorkingPlanItem.c.UUID_WORKING_PLAN.lc ());
                
                db.dupsert (ReportingPeriod.class, 
                        
                    HASH (
                        ReportingPeriod.c.UUID_WORKING_PLAN, r.get ("r.uuid")
                    ),
                        
                    workingPlan.getReportingPeriod ().stream ().map ((t) -> HASH (
                        ReportingPeriod.c.MONTH, t.getMonthYear ().getMonth (),
                        ReportingPeriod.c.REPORTINGPERIODGUID, t.getReportingPeriodGuid ()
                    )).collect (Collectors.toList ()),
                    
                    ReportingPeriod.c.MONTH.lc ()
                        
                );                

            }

            update (db, uuid, r, statusHash (action.getOkStatus ()));

            db.update (OutSoap.class, HASH (
                "uuid", getUuid (),
                "id_status", DONE.getId ()
            ));

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
        
        return HASH (c.ID_CTR_STATUS,     id,
            c.ID_CTR_STATUS_GIS, id
        );
        
    }

    private void update (DB db, UUID uuid, Map<String, Object> r, Map<String, Object> h) throws SQLException {
        
logger.info ("h=" + h);
        
        h.put ("uuid", r.get ("r.uuid"));
        db.update (WorkingPlan.class, h);
        
        h.put ("uuid", uuid);
        db.update (WorkingPlanLog.class, h);
        
    }
    
    private GetStateResult getState (UUID orgPPAGuid, Map<String, Object> r) throws GisPollRetryException, GisPollException {

        GetStateResult rp;
        
        try {
            rp = wsGisServicesClient.getState (orgPPAGuid, (UUID) r.get ("uuid_ack"));
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
