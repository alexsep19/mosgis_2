package ru.eludia.products.mosgis.db.model.tables;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.gosuslugi.dom.schema.integration.services.ExportWorkingListRequest;
import ru.gosuslugi.dom.schema.integration.services.ImportWorkingListRequest;
import ru.gosuslugi.dom.schema.integration.services.WorkingListBaseType;

public class WorkingListLog extends GisWsLogTable {

    public WorkingListLog () {

        super ("tb_work_lists__log", "История редактирования перечней работ и услуг на период", WorkingList.class
            , EnTable.c.class
            , WorkingList.c.class
        );
        
    }
    
    public Get getForExport (String id) {
        
        return (Get) getModel ()
            .get (this, id, "*")
            .toOne (WorkingList.class, "AS r"
                , WorkingList.c.ID_CTR_STATUS.lc ()
                , WorkingList.c.FIASHOUSEGUID.lc () + " AS fiashouseguid"
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
        
    public static ImportWorkingListRequest toImportWorkingListRequest (Map<String, Object> r) {
        final ImportWorkingListRequest result = DB.to.javaBean (ImportWorkingListRequest.class, r);
        result.setApprovedWorkingListData (toApprovedWorkingListData (r));
        return result;
    }
    
    private static ImportWorkingListRequest.ApprovedWorkingListData toApprovedWorkingListData (Map<String, Object> r) {
        final ImportWorkingListRequest.ApprovedWorkingListData result = DB.to.javaBean (ImportWorkingListRequest.ApprovedWorkingListData.class, r);
        result.setTransportGUID (UUID.randomUUID ().toString ());
        result.setMonthYearFrom (toMonthYearFrom (r, WorkingList.c.DT_FROM));
        result.setMonthYearTo   (toMonthYearTo   (r, WorkingList.c.DT_TO));
        for (Map<String, Object> i: (List<Map<String, Object>>) r.get ("items")) result.getWorkListItem ().add (WorkingListItem.toDom (i));
        return result;
    }
    
    private static WorkingListBaseType.MonthYearFrom toMonthYearFrom (Map<String, Object> r, WorkingList.c c) {
        String s = r.get (c.lc ()).toString ();
        final WorkingListBaseType.MonthYearFrom result = new WorkingListBaseType.MonthYearFrom ();
        result.setYear (Short.parseShort (s.substring (0, 4)));
        result.setMonth (Integer.parseInt (s.substring (5, 7)));
        return result;
    }
    
    private static WorkingListBaseType.MonthYearTo toMonthYearTo (Map<String, Object> r, WorkingList.c c) {
        String s = r.get (c.lc ()).toString ();
        final WorkingListBaseType.MonthYearTo result = new WorkingListBaseType.MonthYearTo ();
        result.setYear (Short.parseShort (s.substring (0, 4)));
        result.setMonth (Integer.parseInt (s.substring (5, 7)));
        return result;
    }
    
    public static ExportWorkingListRequest toExportWorkingListRequest (Map<String, Object> r) {
        final ExportWorkingListRequest result = new ExportWorkingListRequest ();
        result.getWorkListGUID ().add (r.get (WorkingList.c.WORKLISTGUID.lc ()).toString ());
        return result;
    }
    
    public static ImportWorkingListRequest toCancelImportWorkingListRequest (Map<String, Object> r) {
        final ImportWorkingListRequest result = DB.to.javaBean (ImportWorkingListRequest.class, r);
        result.setCancelWorkingList (toCancelWorkingList (r));
        return result;
    }    

    private static ImportWorkingListRequest.CancelWorkingList toCancelWorkingList (Map<String, Object> r) {
        final ImportWorkingListRequest.CancelWorkingList result = DB.to.javaBean (ImportWorkingListRequest.CancelWorkingList.class, r);
        result.setTransportGUID (UUID.randomUUID ().toString ());
        return result;
    }
                
}