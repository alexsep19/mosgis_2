package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.tables.Charter;
import ru.eludia.products.mosgis.db.model.tables.CharterObject;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.ContractObject;
import ru.eludia.products.mosgis.db.model.tables.OrganizationWork;
import ru.eludia.products.mosgis.db.model.tables.WorkingPlan;
import ru.eludia.products.mosgis.db.model.tables.WorkingPlanLog;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.ReportingPeriod;
import ru.eludia.products.mosgis.db.model.tables.WorkingList;
import ru.eludia.products.mosgis.db.model.tables.WorkingListItem;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.gosuslugi.dom.schema.integration.services_service_async.Fault;
import ru.eludia.products.mosgis.db.model.tables.WorkingPlan.c;
import ru.eludia.products.mosgis.db.model.tables.WorkingPlanItem;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisServicesClient;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.services.ExportWorkingPlanResultType;
import ru.gosuslugi.dom.schema.integration.services.GetStateResult;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outImportWorkingPlansQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollImportWorkingPlanMDB  extends GisPollMDB {

    @EJB
    WsGisServicesClient wsGisServicesClient;

    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")                
                
            .toOne (WorkingPlanLog.class,     "AS log", "uuid", "action").on ("log.uuid_out_soap=root.uuid")
            .toOne (WorkingPlan.class,        "AS r", "uuid").on ()

            .toOne (WorkingList.class, "AS l").on ()
                
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
            
            ExportWorkingPlanResultType result = exportWorkingPlanResult.get (0);

            if (result == null) throw new GisPollException ("0", "Сервис ГИС ЖКХ вернул пустой результат");
            
            ExportWorkingPlanResultType.WorkingPlan workingPlan = result.getWorkingPlan ();
            
            if (workingPlan == null) throw new GisPollException ("0", "Сервис ГИС ЖКХ вернул пустой результат");

            Map<Object, Map<String, Object>> code_month2wpi = new HashMap<> ();
            
            db.forEach (db.getModel ()
                .select (WorkingPlanItem.class, EnTable.c.UUID.lc (), WorkingPlanItem.c.MONTH.lc ())
                .where (WorkingPlanItem.c.UUID_WORKING_PLAN, r.get ("r.uuid"))
                .toOne (WorkingListItem.class).on ()
                .toOne (OrganizationWork.class, "uniquenumber AS code").on (),
                (rs) -> {
                    final String k = rs.getString ("code") + '_' + rs.getString ("month");
                    final Map<String, Object> wpi = db.HASH (rs);
                    wpi.remove ("code");
                    wpi.put (WorkingPlanItem.c.UUID_REPORTING_PERIOD.lc (), null);
                    code_month2wpi.put (k, wpi);
                }
            );
            
logger.info ("code2wpi = " + code_month2wpi);

            for (ExportWorkingPlanResultType.WorkingPlan.ReportingPeriod p: workingPlan.getReportingPeriod ()) {
                
                for (ExportWorkingPlanResultType.WorkingPlan.ReportingPeriod.WorkPlanItem i: p.getWorkPlanItem ()) {
                    
                    final String k = i.getWorkGUID ().getCode () + '_' + p.getMonthYear ().getMonth ();                    
                    final Map<String, Object> wpi = code_month2wpi.get (k);
                    if (wpi == null) {
                        logger.warning ("WorkingPlanItem not found for " + k);
                        continue;
                    }
                    wpi.put (WorkingPlanItem.c.WORKPLANITEMGUID.lc (), i.getWorkPlanItemGUID ());
                    wpi.put (WorkingPlanItem.c.UUID_REPORTING_PERIOD.lc (), null);
                    
                }
                                
            }
            
            db.update (WorkingPlanItem.class, code_month2wpi.values ().stream ().collect (Collectors.toList ()));
            
logger.info ("code2wpi = " + code_month2wpi);
            
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
            
            final Map<String, Object> h = statusHash (action.getOkStatus ());            
            
            update (db, uuid, r, h);

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
