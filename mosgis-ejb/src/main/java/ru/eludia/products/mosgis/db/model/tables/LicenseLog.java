package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.DATE;
import static ru.eludia.base.model.Type.STRING;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocLicenseStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocUser;

public class LicenseLog extends Table {
    
    public LicenseLog() {
        
        super ("tb_licenses__log",                                 "История изменения лицензий");
        
        pk    ("uuid",                      Type.UUID,             NEW_UUID,            "Ключ");
        
        fk    ("uuid_object",               License.class,                              "Ссылка на запись");
        col   ("ts",                        Type.TIMESTAMP,        NOW,                 "Дата/время события");
        fk    ("uuid_user",                 VocUser.class,                      null,   "Оператор");
        fk    ("uuid_out_soap",             OutSoap.class,                      null,   "Последний запрос на импорт в ГИС ЖКХ");
        
        col ("license_version",             Type.INTEGER,                       null, "Версия лицензии");
        col ("license_number",              STRING,                9,                 "Номер лицензии");
        col ("license_reg_date",            DATE,                                     "Дата регистрации лицензии");
        fk  ("license_status",              VocLicenseStatus.class,             null, "Статус лицензии с точки зрения mosgis");
        fk  ("licensing_authority",         VocOrganization.class,              null, "Наименование лицензирующего органа");
        col ("region_fias_guid",            Type.UUID,             null,              "Адрес осуществления лицензируемого вида деятельности (код по ФИАС)");
        col ("licenseable_type_of_activity",STRING,                             2000, "Лицензируемый вид деятельности с указанием выполняемых работ, оказываемых услуг, составляющих лицензируемый вид деятельности");
        col ("additional_information",      STRING,                             2000, "Дополнительная информация");
        fk  ("license_organization",        VocOrganization.class,              null, "Лицензиат"); 

//        col   ("uuid_message",              Type.UUID,                          null,   "UUID запроса в ГИС ЖКХ");
//        col   ("elementguid_new",           Type.UUID,                          null,   "Идентификатор новой версии существующего (в ГИС) элемента справочника");        
//        col   ("is_deleted",                Type.BOOLEAN,                       null,   "1, если запись удалена; иначе 0");
/*
       trigger ("BEFORE INSERT", "BEGIN "
               
           + "SELECT"
           + "       license_reg_date,              "
           + "       license_organization,           "
           + "       license_number,                 "
           + "       licenseable_type_of_activity,  "
           + "       license_version,            "
           + "       additional_information,        "
           + "       license_status,                 "
           + "       region_fias_guid,              "
           + "       licensing_authority             "
           + "INTO "                
           + "       :NEW.license_reg_date,              "
           + "       :NEW.license_organization,           "
           + "       :NEW.license_number,                 "
           + "       :NEW.licenseable_type_of_activity,  "
           + "       :NEW.license_version,            "
           + "       :NEW.additional_information,        "
           + "       :NEW.license_status,                 "
           + "       :NEW.region_fias_guid,              "                   
           + "       :NEW.licensing_authority             "
           + " FROM "
           + "  tb_licences                   "
           + " WHERE uuid=:NEW.uuid_object; "

       + "END;");      
*/        
    }    
    
}
