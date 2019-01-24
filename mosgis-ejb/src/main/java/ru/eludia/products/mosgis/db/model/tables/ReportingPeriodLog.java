package ru.eludia.products.mosgis.db.model.tables;

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
        return result;
    }
    
    public Get getForExport (String id) {

        return (Get) getModel ()
                
            .get (this, id, "*")
                
            .toOne (ReportingPeriod.class, "AS r"
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
/*    
    public static void addPlannedWorksForExport (DB db, Map<String, Object> r) throws SQLException {

        r.put ("refs", db.getList (db.getModel ()
                
//            .select (PublicPropertyContractVotingProtocol.class)
//            .toOne  (VotingProtocol.class, VotingProtocol.c.VOTINGPROTOCOLGUID.lc () + " AS guid").and (EnTable.c.IS_DELETED.IS_DELETED.lc (), 0).on ()
//            .where  (PublicPropertyContractVotingProtocol.c.UUID_CTR.lc (), r.get ("uuid_object"))
//            .and    ("is_deleted", 0)

        ));

    }
*/    
    
}