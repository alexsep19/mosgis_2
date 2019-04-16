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
import ru.eludia.products.mosgis.db.model.voc.VocOktmo;
import ru.eludia.products.mosgis.db.model.voc.VocOverhaulWorkType;
import ru.gosuslugi.dom.schema.integration.capital_repair.ImportPlanWorkRequest;
import ru.gosuslugi.dom.schema.integration.capital_repair.ImportPlanWorkRequest.ImportPlanWork;
import ru.gosuslugi.dom.schema.integration.capital_repair.WorkFinancingType;
import ru.gosuslugi.dom.schema.integration.capital_repair.WorkPlanType;

public class OverhaulShortProgramHouseWorksImport extends EnTable {
    
    public enum c implements EnColEnum {
        
        ORGPPAGUID    (UUID,           "Идентификатор зарегистрированной организации"),
        PROGRAM_UUID  (OverhaulShortProgram.class, "Краткосрочная программа"),
        
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
    
    public OverhaulShortProgramHouseWorksImport () {
        
        super   ("tb_oh_shrt_pr_works_imports", "Записи импорта видов работ краткосрочной программы капитального ремонта");
        
        cols    (c.class);
        
    }
    
    public static ImportPlanWorkRequest toImportPlanWorkRequest (Map <String, Object> r) {
        ImportPlanWorkRequest result = new ImportPlanWorkRequest ();
        List <ImportPlanWork> worksList = result.getImportPlanWork ();
        for (Map <String, Object> work: (List <Map <String, Object>>) r.get ("works")) {
            worksList.add (toImportPlanWork (work));
        }
        result.setPlanGUID (r.get ("planguid").toString ());
        return result;
    }
    
    private static ImportPlanWork toImportPlanWork(Map<String, Object> work) {
        ImportPlanWork result = new ImportPlanWork ();
        result.setTransportGuid (work.get ("uuid").toString ());
        if (work.containsKey ("workguid") && work.get ("workguid") != null)
            result.setWorkGuid (work.get ("workguid").toString ());
        result.setLoadPlanWork (toPlanWorkLoad (work));
        return result;
    }
    
    private static WorkPlanType toPlanWorkLoad (Map <String, Object> work) {
        WorkPlanType result = DB.to.javaBean (WorkPlanType.class, work);
        result.setWorkKind (NsiTable.toDom (work, "nsi_219"));
        result.setOKTMO (VocOktmo.createOKTMORef ((Long) work.get ("oktmo")));
        result.setFinancing (toWorkFinancingType (work));
        return result;
    }
    
    private static WorkFinancingType toWorkFinancingType (Map <String, Object> work) {
        WorkFinancingType result = DB.to.javaBean (WorkFinancingType.class, work);
        return result;
    }
    
    public static Map <String, Object> getForExport (DB db, String id) throws SQLException {
        
        Map <String, Object> record = new HashMap <> ();
        
        List <Map <String, Object>> works = db.getList (db.getModel ()
            .select (OverhaulShortProgramHouseWork.class, "AS oh_work", "*")
                .toOne (OverhaulShortProgramHouseWorksImport.class, "AS imports").where ("uuid", id).on ()
                .toOne  (OverhaulShortProgramHouse.class, "AS oh_house").on ()
                    .toOne  (House.class, "AS in_house", "fiashouseguid AS fiashouseguid").on ("oh_house.house=in_house.uuid")
                        .toOne  (VocBuilding.class, "AS build", "oktmo AS oktmo").on ("in_house.fiashouseguid=build.houseguid")
                .toOne  (VocOverhaulWorkType.class, "AS nsi_219", "code", "guid").where ("isactual", 1).on ("oh_work.work=nsi_219.code")
            .and    ("is_deleted", 0)
        );
        
        record.put ("works", works);
        
        Map <String, Object> importData = db.getMap (db.getModel ()
            .get (OverhaulShortProgramHouseWorksImport.class, id, "orgppaguid")
                .toOne (OverhaulShortProgram.class, "planguid AS planguid").on ()
        );
        
        record.put ("orgppaguid", importData.get ("orgppaguid"));
        record.put ("planguid", importData.get ("planguid"));
        record.put ("uuid", id);
        
        return record;
        
    }
    
}
