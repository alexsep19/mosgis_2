package ru.eludia.products.mosgis.db.model.voc;

import javax.json.Json;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

public class VocAction extends Table {

    private static JsonArray jsonArray;
    
    static {
        
        JsonArrayBuilder builder = Json.createArrayBuilder ();
        
        for (i value: i.values ()) builder.add (Json.createObjectBuilder ()
            .add ("id",    value.name)
            .add ("label", value.label)
        );
                    
        jsonArray = builder.build ();
        
    }
    
    private static final String TABLE_NAME = "vc_actions";
    
    public static final void addTo (JsonObjectBuilder job) {
        job.add (TABLE_NAME, jsonArray);
    }   

    public VocAction () {
        
        super (TABLE_NAME, "Действия пользователей системы");
        
        pk    ("name",         Type.STRING,  "Идентификатор");
        col   ("label",        Type.STRING,  "Наименование");
        
        data  (i.class);
                
    }
    
    public enum i {
        
        ALTER           ("alter",           "изменение"),
        ANNUL           ("annul",           "аннулирование"),
        APPROVE         ("approve",         "утверждение"),
        CANCEL          ("cancel",          "отмена"),
        CREATE          ("create",          "создание"),
        DELETE          ("delete",          "удаление"),
        
        DELETE_PROJECT  ("delete_project",  "удаление проекта"),
        
        EXPIRE          ("expire",          "проверка на просроченность"),
        
        IMPORT_FROM_FILE      ("import_from_file", "импорт из файла"),
        IMPORT_FROM_GIS_WS    ("import_from_gis_ws", "прием из ГИС ЖКХ"),
        
        IMPORT_ACCOUNTS            ("import_accounts", "импорт ЛС"),
        IMPORT_MGMT_CONTRACTS      ("import_mgmt_contracts", "импорт ДУ"),
	IMPORT_SR_CONTRACTS        ("import_sr_contracts", "импорт ДРСО"),
	IMPORT_SR_CONTRACT_OBJECTS ("import_sr_contract_objects", "импорт ОЖФ ДРСО"),
        IMPORT_CHARTERS            ("import_charters", "импорт устава"),
        IMPORT_ADD_SERVICES        ("import_add_services", "Импорт справочника дополнительных услуг"),
        IMPORT_OVERHAUL_WORK_TYPES ("import_overhaul_work_types", "Импорт справочника видов работ капитального ремонта"),
	IMPORT_LEGAL_ACTS          ("import_legal_acts", "Импорт нормативно-правовых актов"),
	IMPORT_BASE_DECISION_MSPS  ("import_base_decision_msps", "Импорт НСИ 302"),
	IMPORT_CITIZEN_COMPENSATION_CATEGORIES  ("import_citizen_compensation_categories", "Импорт перечня отдельных категорий граждан"),

        LOCK                        ("lock",                       "блокировка"),
        PROMOTE                     ("promote",                    "утверждение"),
        
        PUBLISHANDPROJECT           ("publishandproject",          "размещение проекта"),
        REFRESH                     ("refresh",                    "обновление статусов"),
        RELOAD                      ("reload",                     "обновление данных"),
        ROLLOVER                    ("rollover",                   "пролонгирование"),
        SET_PASSWORD                ("set_password",               "установка пароля"),
        TERMINATE                   ("terminate",                  "расторжение"),
        UNDELETE                    ("undelete",                   "восстановление"),
        UNLOCK                      ("unlock",                     "разблокировка"),
        UPDATE                      ("update",                     "редактирование"), 
        SEND_TO_GIS                 ("send_to_gis",                "размещение в ГИС ЖКХ"),

        ETL                         ("etl",                        "создание напрямую в БД"),

        ;
                
        String name;
        String label;
        
        public String getName () {
            return name;
        }

        public String getLabel () {
            return label;
        }

        private i (String name, String label) {
            this.name = name;
            this.label = label;            
        }

        public static i forName (String name) {
            for (i i: values ()) if (i.name.equals (name)) return i;
            return null;
        }

        @Override
        public String toString () {
            return name;
        }

    }
        
}