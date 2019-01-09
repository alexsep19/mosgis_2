package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class WorkingPlanLog extends GisWsLogTable {

    public WorkingPlanLog () {

        super ("tb_work_plans__log", "История редактирования планов работ и услуг на период", WorkingPlan.class
            , EnTable.c.class
            , WorkingPlan.c.class
        );
        
    }
/*    
    public Get getForExport (String id) {
        
        return (Get) getModel ()
            .get (this, id, "*")
            .toOne (WorkingPlan.class, "AS r"
                , WorkingPlan.c.ID_CTR_STATUS.lc ()
                , WorkingPlan.c.FIASHOUSEGUID.lc () + " AS fiashouseguid"
            ).on ()
                
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
        
    public static ImportWorkingPlanRequest toImportWorkingPlanRequest (Map<String, Object> r) {
        final ImportWorkingPlanRequest result = DB.to.javaBean (ImportWorkingPlanRequest.class, r);
        result.setApprovedWorkingPlanData (toApprovedWorkingPlanData (r));
        return result;
    }
    
    private static ImportWorkingPlanRequest.ApprovedWorkingPlanData toApprovedWorkingPlanData (Map<String, Object> r) {
        final ImportWorkingPlanRequest.ApprovedWorkingPlanData result = DB.to.javaBean (ImportWorkingPlanRequest.ApprovedWorkingPlanData.class, r);
        result.setTransportGUID (UUID.randomUUID ().toString ());
        result.setMonthYearFrom (toMonthYearFrom (r, WorkingPlan.c.DT_FROM));
        result.setMonthYearTo   (toMonthYearTo   (r, WorkingPlan.c.DT_TO));
        for (Map<String, Object> i: (List<Map<String, Object>>) r.get ("items")) result.getWorkListItem ().add (WorkingPlanItem.toDom (i));
        return result;
    }
    
    private static WorkingPlanBaseType.MonthYearFrom toMonthYearFrom (Map<String, Object> r, WorkingPlan.c c) {
        String s = r.get (c.lc ()).toString ();
        final WorkingPlanBaseType.MonthYearFrom result = new WorkingPlanBaseType.MonthYearFrom ();
        result.setYear (Short.parseShort (s.substring (0, 4)));
        result.setMonth (Integer.parseInt (s.substring (5, 7)));
        return result;
    }
    
    private static WorkingPlanBaseType.MonthYearTo toMonthYearTo (Map<String, Object> r, WorkingPlan.c c) {
        String s = r.get (c.lc ()).toString ();
        final WorkingPlanBaseType.MonthYearTo result = new WorkingPlanBaseType.MonthYearTo ();
        result.setYear (Short.parseShort (s.substring (0, 4)));
        result.setMonth (Integer.parseInt (s.substring (5, 7)));
        return result;
    }
    
    public static ExportWorkingPlanRequest toExportWorkingPlanRequest (Map<String, Object> r) {
        final ExportWorkingPlanRequest result = new ExportWorkingPlanRequest ();
        result.getWorkListGUID ().add (r.get (WorkingPlan.c.WORKLISTGUID.lc ()).toString ());
        return result;
    }
    
    public static ImportWorkingPlanRequest toCancelImportWorkingPlanRequest (Map<String, Object> r) {
        final ImportWorkingPlanRequest result = DB.to.javaBean (ImportWorkingPlanRequest.class, r);
        result.setCancelWorkingPlan (toCancelWorkingPlan (r));
        return result;
    }    

    private static ImportWorkingPlanRequest.CancelWorkingPlan toCancelWorkingPlan (Map<String, Object> r) {
        final ImportWorkingPlanRequest.CancelWorkingPlan result = DB.to.javaBean (ImportWorkingPlanRequest.CancelWorkingPlan.class, r);
        result.setTransportGUID (UUID.randomUUID ().toString ());
        return result;
    }
*/                
}