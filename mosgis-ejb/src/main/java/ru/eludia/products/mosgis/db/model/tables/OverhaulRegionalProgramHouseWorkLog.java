package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocOktmo;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOverhaulWorkType;
import ru.gosuslugi.dom.schema.integration.capital_repair.ImportRegionalProgramWorkRequest;
import ru.gosuslugi.dom.schema.integration.capital_repair.RegionalProgramWorkType;

public class OverhaulRegionalProgramHouseWorkLog extends GisWsLogTable {

    public OverhaulRegionalProgramHouseWorkLog () {
        
        super ("tb_oh_reg_pr_house_work__log", "Вид работы по дому РПКР: история изменений",
                OverhaulRegionalProgramHouseWork.class,
                EnTable.c.class,
                OverhaulRegionalProgramHouseWork.c.class);
        
    }
    
    public static ImportRegionalProgramWorkRequest toImportRegionalProgramWorkRequest (Map <String, Object> r) {
        ImportRegionalProgramWorkRequest result = new ImportRegionalProgramWorkRequest ();
        List <ImportRegionalProgramWorkRequest.ImportRegionalProgramWork> worksList = result.getImportRegionalProgramWork ();
        for (Map <String, Object> work: (List <Map <String, Object>>) r.get ("works")) {
            worksList.add (toImportRegionalProgramWork (work));
        }
        result.setRegionalProgramGuid (r.get ("regionalprogramguid").toString ());
        return result;
    }
    
    private static ImportRegionalProgramWorkRequest.ImportRegionalProgramWork toImportRegionalProgramWork(Map<String, Object> work) {
        ImportRegionalProgramWorkRequest.ImportRegionalProgramWork result = new ImportRegionalProgramWorkRequest.ImportRegionalProgramWork ();
        result.setTransportGuid (UUID.randomUUID ().toString ());
        if (work.containsKey ("workguid") && work.get ("workguid") != null)
            result.setWorkGuid (work.get ("workguid").toString ());
        result.setLoadRegionalProgramWork (toRegionalProgramWorkLoad (work));
        return result;
    }
    
    private static RegionalProgramWorkType toRegionalProgramWorkLoad(Map<String, Object> work) {
        RegionalProgramWorkType result = DB.to.javaBean (RegionalProgramWorkType.class, work);
        result.setWorkType (NsiTable.toDom (work, "nsi_219"));
        result.setOKTMO (VocOktmo.createOKTMORef ((Long) work.get ("oktmo")));
        return result;
    }
    
    public static Map <String, Object> getForExport (DB db, String id) throws SQLException {
        
        Map <String, Object> record = new HashMap <> ();
        
        List <Map <String, Object>> works = db.getList (db.getModel ()
                .select (OverhaulRegionalProgramHouseWork.class, "AS oh_work", "*")
                    .toOne  (OverhaulRegionalProgramHouse.class, "AS oh_house").on ()
                        .toOne  (House.class, "AS in_house", "fiashouseguid AS fiashouseguid").on ("oh_house.house=in_house.uuid")
                            .toOne  (VocBuilding.class, "AS build", "oktmo AS oktmo").on ("in_house.fiashouseguid=build.houseguid")
                        .toOne  (OverhaulRegionalProgram.class, "AS program").on ("oh_house.program_uuid=program.uuid")
                            .toOne  (VocOrganization.class, "orgppaguid AS orgppaguid").on ()
                            .toOne  (OverhaulRegionalProgramLog.class, "AS log").where ("uuid", id).on ("program.id_log=log.uuid")
                    .toOne  (VocOverhaulWorkType.class, "AS nsi_219", "code", "guid").on ("oh_work.work=nsi_219.code")
                .and    ("is_deleted", 0)
        );
        
        record.put ("works", works);
        
        Map <String, Object> programData = db.getMap (db.getModel ()
                .get   (OverhaulRegionalProgramLog.class, id)
                .toOne (OverhaulRegionalProgram.class, "regionalprogramguid AS regionalprogramguid").on ()
                .toOne (VocOrganization.class, "orgppaguid AS orgppaguid").on ()
        );
        
        record.put ("orgppaguid", programData.get ("orgppaguid"));
        record.put ("regionalprogramguid", programData.get ("regionalprogramguid"));
        record.put ("uuid", id);
        
        return record;
        
    }
    
}