package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.gosuslugi.dom.schema.integration.capital_repair.ImportRegionalProgramWorkRequest;
import ru.gosuslugi.dom.schema.integration.capital_repair.ImportRegionalProgramWorkRequest.ImportRegionalProgramWork;

public class OverhaulRegionalProgramHouseWorkLog extends GisWsLogTable {

    public OverhaulRegionalProgramHouseWorkLog () {
        
        super ("tb_oh_reg_pr_house_work__log", "Вид работы по дому РПКР: история изменений",
                OverhaulRegionalProgramHouseWork.class,
                EnTable.c.class,
                OverhaulRegionalProgramHouseWork.c.class);
        
    }
    
    public static ImportRegionalProgramWorkRequest toAnnulRegionalProgramWorkRequest(Map <String, Object> r) {
        ImportRegionalProgramWorkRequest result = new ImportRegionalProgramWorkRequest ();
        result.setRegionalProgramGuid (r.get ("regionalprogramguid").toString ());
        result.getImportRegionalProgramWork ().add (toAnnulRegionalProgramWork (r));
        return result;
    }
    
    private static ImportRegionalProgramWork toAnnulRegionalProgramWork(Map <String, Object> r) {
        ImportRegionalProgramWork result = new ImportRegionalProgramWork ();
        result.setWorkGuid (r.get ("guid").toString ());
        result.setTransportGuid (UUID.randomUUID ().toString ());
        result.setDeleteRegionalProgramWork (Boolean.TRUE);
        return result;
    }
    
    public static Map <String, Object> getForExport (DB db, String id) throws SQLException {
        
        return db.getMap (db.getModel ()
            .get (OverhaulRegionalProgramHouseWorkLog.class, id, "*")
                .toOne (OverhaulRegionalProgramHouse.class, "AS houses").on ()
                    .toOne (OverhaulRegionalProgram.class, "AS program", "regionalprogramguid AS regionalprogramguid").on ("houses.program_uuid=program.uuid")
                        .toOne (VocOrganization.class, "AS org", "orgppaguid AS orgppaguid").on ("program.org_uuid=org.uuid")
        );
        
    }
    
}