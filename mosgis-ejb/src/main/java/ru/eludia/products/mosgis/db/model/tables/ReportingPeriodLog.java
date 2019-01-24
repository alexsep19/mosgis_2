package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.gosuslugi.dom.schema.integration.services.CompletedWorksByPeriodType;
import ru.gosuslugi.dom.schema.integration.services.ImportCompletedWorksRequest;

public class ReportingPeriodLog extends GisWsLogTable {

    public ReportingPeriodLog () {

        super ("tb_reporting_periods__log", "История редактирования периодов отчётности в планах работ и услуг", ReportingPeriod.class
            , EnTable.c.class
            , ReportingPeriod.c.class
        );
        
    }
    
    public static ImportCompletedWorksRequest toCompletedWorksRequest (Map<String, Object> r) {
        final ImportCompletedWorksRequest result = new ImportCompletedWorksRequest ();
        result.setCompletedWorksByPeriod (toCompletedWorksByPeriodType (r));
        return result;
    }    
    
    private static CompletedWorksByPeriodType toCompletedWorksByPeriodType (Map<String, Object> r) {
        final CompletedWorksByPeriodType result = DB.to.javaBean (CompletedWorksByPeriodType.class, r);
        for (Map<String, Object> p: (List<Map<String, Object>>) r.get ("planned_works")) result.getPlannedWork ().add (WorkingPlanItem.toPlannedWork (p));
        for (Map<String, Object> u: (List<Map<String, Object>>) r.get ("unplanned_works")) result.getUnplannedWork ().add (UnplannedWork.toUnplannedWork (u));
        return result;
    }
    
    public Get getForExport (String id) {

        return (Get) getModel ()
                
            .get (this, id, "*")
                
            .toOne (ReportingPeriod.class, "AS r"
                , ReportingPeriod.c.UUID_WORKING_PLAN.lc ()
                , ReportingPeriod.c.ID_CTR_STATUS.lc ()
            ).on ()
                
            .toOne (WorkingPlan.class, "AS wp").on ()
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

    public static void addPlannedWorksForExport (DB db, Map<String, Object> r) throws SQLException {

        r.put ("planned_works", db.getList (db.getModel ()                
            .select (WorkingPlanItem.class, "*")
            .where  (WorkingPlanItem.c.UUID_WORKING_PLAN, r.get ("r.uuid_working_plan"))
            .and    ("is_deleted", 0)
        ));

    }
    
    public static void addUnplannedWorksForExport (DB db, Map<String, Object> r) throws SQLException {
        
        r.put ("unplanned_works", db.getList (db.getModel ()                
            .select (UnplannedWork.class, "*")
            .where  (UnplannedWork.c.UUID_REPORTING_PERIOD, r.get ("r.uuid"))
            .and    ("is_deleted", 0)
        ));
        
    }    
    
}