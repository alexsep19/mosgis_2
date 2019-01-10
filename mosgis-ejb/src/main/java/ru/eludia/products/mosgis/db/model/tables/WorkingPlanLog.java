package ru.eludia.products.mosgis.db.model.tables;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.xml.datatype.XMLGregorianCalendar;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.gosuslugi.dom.schema.integration.services.ImportWorkingPlanRequest;
import ru.gosuslugi.dom.schema.integration.services.WorkingPlanType;

public class WorkingPlanLog extends GisWsLogTable {

    public WorkingPlanLog () {

        super ("tb_work_plans__log", "История редактирования планов работ и услуг на период", WorkingPlan.class
            , EnTable.c.class
            , WorkingPlan.c.class
        );
        
    }
    
    public static ImportWorkingPlanRequest toImportWorkingPlanRequest (Map<String, Object> r) {
        final ImportWorkingPlanRequest result = new ImportWorkingPlanRequest ();
        result.getWorkingPlan ().add (toWorkingPlan (r));
        return result;
    }
    
    private static WorkingPlanType toWorkingPlan (Map<String, Object> r) {
        final WorkingPlanType result = DB.to.javaBean (WorkingPlanType.class, r);
        List<Map<String, Object>> items = (List<Map<String, Object>>) r.get ("items");
        if (DB.ok (items)) for (Map<String, Object> item: items) result.getWorkPlanItem ().add (toWorkPlanItem (item, result.getYear ()));
        result.setTransportGUID (UUID.randomUUID ().toString ());
        return result;
    }
    
    private static WorkingPlanType.WorkPlanItem toWorkPlanItem (Map<String, Object> r, short year) {
        final WorkingPlanType.WorkPlanItem result = DB.to.javaBean (WorkingPlanType.WorkPlanItem.class, r);
        result.setYear (year);
        addDates (result.getWorkDate (), result.getYear (), result.getMonth (), r.get (WorkingPlanItem.c.DAYS_BITMASK.lc ()));
        if (!result.getWorkDate ().isEmpty ()) result.setWorkCount (null);
        result.setTransportGUID (UUID.randomUUID ().toString ());
        return result;
    }
    
    private static void addDates (List<XMLGregorianCalendar> workDates, short year, int month, Object mask) {
        
        int bits = (int) DB.to.Long (mask);
        if (bits == 0L) return;

        StringBuilder sb = new StringBuilder ();
        sb.append (year);
        sb.append ('-');
        if (month < 10) sb.append ('0');
        sb.append (month);
        sb.append ("-00");
                
        int m = 1;
        for (int i = 1; i <= 31; i++) {

            if ((m & bits) == m) {
                sb.setCharAt (8, (char) ('0' + (i / 10)));
                sb.setCharAt (9, (char) ('0' + (i % 10)));
                workDates.add (DB.to.XMLGregorianCalendar (sb.toString ()));
            }
            
            m <<= 1;
            
        }
            
    }
        
    public Get getForExport (String id) {

        return (Get) getModel ()
                
            .get (this, id, "*")
            .toOne (WorkingPlan.class, "AS r"
                , WorkingPlan.c.ID_CTR_STATUS.lc ()
            ).on ()

            .toOne (WorkingList.class, "AS l"
                , WorkingList.c.WORKLISTGUID.lc () + " AS worklistguid"
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

}