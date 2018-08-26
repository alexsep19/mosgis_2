package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.base.model.def.Virt;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState.i.PENDING;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.db.model.voc.VocUser;

public class MainMunicipalServiceLog extends Table {

    public MainMunicipalServiceLog () {

        super ("tb_municipal_svc__log", "Коммунальные услуги: история изменения");

        pk    ("uuid",                      Type.UUID,             NEW_UUID,            "Ключ");
        col   ("action",                    Type.STRING,                                "Действие");
        fk    ("uuid_object",               MainMunicipalService.class,                 "Ссылка на запись");
        col   ("ts",                        Type.TIMESTAMP,        NOW,                 "Дата/время события");
        fk    ("uuid_user",                 VocUser.class,                      null,   "Оператор");
        fk    ("uuid_out_soap",             OutSoap.class,                      null,   "Последний запрос на импорт в ГИС ЖКХ");
        col   ("uuid_message",              Type.UUID,                          null,   "UUID запроса в ГИС ЖКХ");
        col   ("elementguid_new",           Type.UUID,                          null,   "Идентификатор новой версии существующего (в ГИС) элемента справочника");
        col   ("is_deleted",                Type.BOOLEAN,                       null,   "1, если запись удалена; иначе 0");                
        
        col   ("elementguid",              Type.UUID,           null,       "Идентификатор существующего элемента справочника");                
        col   ("code_vc_nsi_3",            Type.STRING,  20,    null,       "Ссылка на НСИ \"Вид коммунальной услуги\" (реестровый номер 3)");        
        col   ("is_general",               Type.BOOLEAN,        Bool.FALSE, "1, если услуга предоставляется на общедомовые нужды; иначе 0");
        col   ("selfproduced",             Type.BOOLEAN,        Bool.FALSE, "1, если услуга производится самостоятельно; иначе 0");
        col   ("mainmunicipalservicename", Type.STRING,                     "Наименование главной коммунальной услуги");
        col   ("code_vc_nsi_2",            Type.STRING,  20,    null,       "Ссылка на НСИ \"Вид коммунального ресурса\" (реестровый номер 2)");        
        fk    ("okei",                     VocOkei.class,       null,       "Единица измерения");
        col   ("sortorder",                Type.STRING,  3,     null,       "Порядок сортировки");        

        col   ("label",                    Type.STRING,  new Virt ("(''||\"MAINMUNICIPALSERVICENAME\")"),  "Наименование");
        col   ("label_uc",                 Type.STRING,  new Virt ("UPPER(\"MAINMUNICIPALSERVICENAME\")"),  "НАИМЕНОВАНИЕ В ВЕРХНЕМ РЕГИСТРЕ");        
        col   ("municipalserviceref",      Type.STRING,  new Virt ("(''||\"CODE_VC_NSI_3\")"),  "Вид коммунальной услуги");
        col   ("municipalresourceref",     Type.STRING,  new Virt ("(''||\"CODE_VC_NSI_2\")"),  "Вид коммунального ресурса");
        col   ("generalneeds",             Type.STRING,  new Virt ("DECODE(\"IS_GENERAL\",1,1,NULL)"),  "Признак \"Услуга предоставляется на общедомовые нужды\"");

        
        key   ("ts", "uuid_object", "ts");
        key   ("uuid_out_soap", "uuid_out_soap");        
        
        trigger ("BEFORE INSERT", "BEGIN "
                                
            + "SELECT"
            + "       is_deleted,              "
            + "       elementguid,             "
            + "       code_vc_nsi_3,           "
            + "       is_general,              "
            + "       selfproduced,            "
            + "       mainmunicipalservicename,"
            + "       code_vc_nsi_2,           "
            + "       okei,                    "
            + "       sortorder                "

            + "INTO "
                
            + "       :NEW.is_deleted,              "
            + "       :NEW.elementguid,             "
            + "       :NEW.code_vc_nsi_3,           "
            + "       :NEW.is_general,              "
            + "       :NEW.selfproduced,            "
            + "       :NEW.mainmunicipalservicename,"
            + "       :NEW.code_vc_nsi_2,           "
            + "       :NEW.okei,                    "
            + "       :NEW.sortorder                "

            + " FROM tb_municipal_svc WHERE uuid=:NEW.uuid_object; "

        + "END;");

        trigger ("AFTER INSERT", "BEGIN "
            + " RETURN;"
        + "END;");

    }

}