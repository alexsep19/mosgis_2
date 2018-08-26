package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.base.model.def.Virt;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState.i.PENDING;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.db.model.voc.VocUser;

public class OrganizationWorkLog extends Table {

    public OrganizationWorkLog () {

        super ("tb_org_works__log", "Работы и услуги организации: история изменения");

        pk    ("uuid",                      Type.UUID,             NEW_UUID,            "Ключ");
        col   ("action",                    Type.STRING,                                "Действие");
        fk    ("uuid_object",               OrganizationWork.class,                     "Ссылка на запись");
        col   ("ts",                        Type.TIMESTAMP,        NOW,                 "Дата/время события");
        fk    ("uuid_user",                 VocUser.class,                      null,   "Оператор");
        fk    ("uuid_out_soap",             OutSoap.class,                      null,   "Последний запрос на импорт в ГИС ЖКХ");
        col   ("uuid_message",              Type.UUID,                          null,   "UUID запроса в ГИС ЖКХ");
        col   ("elementguid",               Type.UUID,           null,       "Идентификатор существующего элемента справочника");                
        col   ("elementguid_new",           Type.UUID,                          null,   "Идентификатор новой версии существующего (в ГИС) элемента справочника");
        col   ("is_deleted",                Type.BOOLEAN,                       null,   "1, если запись удалена; иначе 0");                

        fk    ("okei",                      VocOkei.class,                      null,   "Единица измерения");
        col   ("stringdimensionunit",       Type.STRING,  100,                  null,   "Другая единица измерения");
        col   ("workname",                  Type.STRING,                                "Название работы/услуги");
        col   ("code_vc_nsi_56",            Type.STRING,  20,                   null,   "Ссылка на НСИ \"Вид работ\" (реестровый номер 56)");
        col   ("codes_vc_nsi_67",           Type.STRING,                        null,   "Ссылки на НСИ \"Обязательные работы, обеспечивающие надлежащее содержание МКД\" (реестровый номер 67)");

        col   ("label",                    Type.STRING,  new Virt ("(''||\"WORKNAME\")"),  "Наименование");
        col   ("label_uc",                 Type.STRING,  new Virt ("UPPER(\"WORKNAME\")"),  "НАИМЕНОВАНИЕ В ВЕРХНЕМ РЕГИСТРЕ");
        col   ("servicetyperef",           Type.STRING,  new Virt ("(''||\"CODE_VC_NSI_56\")"),  "Вид работ");
        
        key   ("ts", "uuid_object", "ts");
        key   ("uuid_out_soap", "uuid_out_soap");        
        
        trigger ("BEFORE INSERT", "BEGIN "
                                
            + "SELECT"
            + "       is_deleted,              "
            + "       elementguid,             "
            + "       okei,                    "
            + "       stringdimensionunit,     "
            + "       workname,                "
            + "       code_vc_nsi_56           "

            + "INTO "
                
            + "       :NEW.is_deleted,              "
            + "       :NEW.elementguid,             "
            + "       :NEW.okei,                    "
            + "       :NEW.stringdimensionunit,     "
            + "       :NEW.workname,                "
            + "       :NEW.code_vc_nsi_56           "

            + " FROM tb_org_works WHERE uuid=:NEW.uuid_object; "

            + " SELECT "
            + " '[' || (LISTAGG ('\"' || code || '\"', ',') WITHIN GROUP (ORDER BY code)) || ']' "
            + " INTO "
            + "  :NEW.codes_vc_nsi_67 "
            + " FROM "
            + "  tb_org_works_nsi_67 "
            + " WHERE "
            + "  uuid = :NEW.uuid_object; "

        + "END;");

        trigger ("AFTER INSERT", "BEGIN "
            + " RETURN;"
        + "END;");

    }

}