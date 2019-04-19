package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.gosuslugi.dom.schema.integration.capital_repair.ImportPlanWorkRequest;
import ru.gosuslugi.dom.schema.integration.capital_repair.ImportPlanWorkRequest.ImportPlanWork;

public class OverhaulAddressProgramHouseWorkLog extends GisWsLogTable {

    public OverhaulAddressProgramHouseWorkLog () {
        
        super ("tb_oh_addr_pr_house_work__log", "Вид работы по дому адресной программы: история изменений",
                OverhaulAddressProgramHouseWork.class,
                EnTable.c.class,
                OverhaulAddressProgramHouseWork.c.class);
        
    }
    
    public static ImportPlanWorkRequest toAnnulPlanWorkRequest(Map <String, Object> r) {
        ImportPlanWorkRequest result = new ImportPlanWorkRequest ();
        result.setPlanGUID (r.get ("planguid").toString ());
        result.getImportPlanWork ().add (toAnnulRegionalProgramWork (r));
        return result;
    }
    
    private static ImportPlanWork toAnnulRegionalProgramWork(Map <String, Object> r) {
        ImportPlanWork result = new ImportPlanWork ();
        result.setWorkGuid (r.get ("guid").toString ());
        result.setTransportGuid (UUID.randomUUID ().toString ());
        result.setDeletePlanWork (Boolean.TRUE);
        return result;
    }
    
    public static Map <String, Object> getForExport (DB db, String id) throws SQLException {
        
        return db.getMap (db.getModel ()
            .get (OverhaulAddressProgramHouseWorkLog.class, id, "*")
                .toOne (OverhaulAddressProgramHouse.class, "AS houses").on ()
                    .toOne (OverhaulAddressProgram.class, "AS program", "planguid AS planguid").on ("houses.program_uuid=program.uuid")
                        .toOne (VocOrganization.class, "AS org", "orgppaguid AS orgppaguid").on ("program.org_uuid=org.uuid")
        );
        
    }
    
}
