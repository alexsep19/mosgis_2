package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.INTEGER;
import static ru.eludia.base.model.Type.TIMESTAMP;
import static ru.eludia.base.model.Type.UUID;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOktmo;
import ru.eludia.products.mosgis.db.model.voc.VocOverhaulWorkType;
import ru.gosuslugi.dom.schema.integration.capital_repair.ImportRegionalProgramWorkRequest;
import ru.gosuslugi.dom.schema.integration.capital_repair.RegionalProgramWorkType;

public class OverhaulRegionalProgramHouseWorksImport extends EnTable {
    
    public enum c implements EnColEnum {
        
        ORGPPAGUID    (UUID,           "Идентификатор зарегистрированной организации"),
        PROGRAM_UUID  (OverhaulRegionalProgram.class, "Региональная программа"),
        
        COUNT         (INTEGER,  null, "Количество импортируемых в ГИС видов работ"),
        OK_COUNT      (INTEGER,  null, "Количество успешно импортированных в ГИС видов работ"),
        TS            (TIMESTAMP, NOW, "Дата/время начала импорта"),
        
        UUID_OUT_SOAP (OutSoap.class, null, "Запрос на импорт в ГИС ЖКХ"),
        UUID_MESSAGE  (UUID, null, "UUID запроса в ГИС ЖКХ")
        
        ;
        
        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}        
        @Override
        public boolean isLoggable () {
            return false;
        }
        
    }
    
    public OverhaulRegionalProgramHouseWorksImport () {
        
        super   ("tb_oh_reg_pr_works_imports", "Записи импорта видов работ региональной программы капитального ремонта");
        
        cols    (c.class);
        
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
        result.setTransportGuid (work.get ("uuid").toString ());
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
                .toOne (OverhaulRegionalProgramHouseWorksImport.class, "AS imports").where ("uuid", id).on ()
                .toOne  (OverhaulRegionalProgramHouse.class, "AS oh_house").on ()
                    .toOne  (House.class, "AS in_house", "fiashouseguid AS fiashouseguid").on ("oh_house.house=in_house.uuid")
                        .toOne  (VocBuilding.class, "AS build", "oktmo AS oktmo").on ("in_house.fiashouseguid=build.houseguid")
                .toOne  (VocOverhaulWorkType.class, "AS nsi_219", "code", "guid").where ("isactual", 1).on ("oh_work.work=nsi_219.code")
            .and    ("is_deleted", 0)
        );
        
        record.put ("works", works);
        
        Map <String, Object> importData = db.getMap (db.getModel ()
            .get (OverhaulRegionalProgramHouseWorksImport.class, id, "orgppaguid")
                .toOne (OverhaulRegionalProgram.class, "regionalprogramguid AS regionalprogramguid").on ()
        );
        
        record.put ("orgppaguid", importData.get ("orgppaguid"));
        record.put ("regionalprogramguid", importData.get ("regionalprogramguid"));
        record.put ("uuid", id);
        
        return record;
        
    }
    
}
