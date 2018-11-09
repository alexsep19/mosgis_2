package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Num;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState.i.PENDING;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class OrganizationWork extends Table {

    public OrganizationWork () {
        
        super ("tb_org_works", "Работы и услуги организации");
        
        pk    ("uuid",                     Type.UUID,           NEW_UUID,   "Ключ");
        fk    ("uuid_org",                 VocOrganization.class,                      "Организация");
        col   ("is_deleted",               Type.BOOLEAN,          Bool.FALSE,          "1, если запись удалена; иначе 0");
        col   ("elementguid",              Type.UUID,           null,       "Идентификатор существующего элемента справочника");        
        col   ("uniquenumber",             Type.STRING,                        null,   "Уникальный реестровый номер (в ГИС)");        
        fk    ("id_status",                VocAsyncEntityState.class,          new Num (VocAsyncEntityState.i.PENDING.getId ()), "Статус синхронизации");
        fk    ("id_log",                   OrganizationWorkLog.class,          null, "Последнее событие редактирования");
        
        fk    ("okei",                     VocOkei.class,                      null,   "Единица измерения");
        col   ("stringdimensionunit",      Type.STRING,  100,                  null,   "Другая единица измерения");
        col   ("workname",                 Type.STRING,                                "Название работы/услуги");
        col   ("code_vc_nsi_56",           Type.STRING,  20,                   null,   "Ссылка на НСИ \"Вид работ\" (реестровый номер 56)");
//        col   ("code_vc_nsi_67",           Type.STRING,  20,    null,       "Ссылка на НСИ \"Обязательные работы, обеспечивающие надлежащее содержание МКД\" (реестровый номер 67)");
        
        col   ("label",                    Type.STRING,  new Virt ("(''||\"WORKNAME\")"),  "Наименование");
        col   ("label_uc",                 Type.STRING,  new Virt ("UPPER(\"WORKNAME\")"),  "НАИМЕНОВАНИЕ В ВЕРХНЕМ РЕГИСТРЕ");
        col   ("servicetyperef",           Type.STRING,  new Virt ("(''||\"CODE_VC_NSI_56\")"),  "Вид работ");
//        col   ("requiredserviceref",       Type.STRING,  new Virt ("(''||\"CODE_VC_NSI_67\")"),  "Обязательные работы");

        key   ("org_label", "uuid_org", "label");

        trigger ("BEFORE INSERT OR UPDATE", "BEGIN "

            + "IF UPDATING "
            + "  AND :OLD.id_log IS NOT NULL "             // что-то уже отправляли
            + "  AND :OLD.id_log <> :NEW.id_log "          // пытаются отредактировать вновь
            + "  AND :OLD.id_status = " + PENDING.getId () // прошлая синхронизация не доехала
            + " THEN"
            + "  raise_application_error (-20000, 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.'); "
            + "END IF; "

        + "END;");

    }

}