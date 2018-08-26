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

public class AdditionalService extends Table {

    public AdditionalService () {

        super ("tb_add_services",                                                       "Дополнительные услуги");

        pk    ("uuid",                      Type.UUID,             NEW_UUID,            "Ключ");
        fk    ("uuid_org",                  VocOrganization.class,                      "Организация");
        col   ("is_deleted",                Type.BOOLEAN,          Bool.FALSE,          "1, если запись удалена; иначе 0");
                
        fk    ("okei",                      VocOkei.class,                      null,   "Единица измерения");
        col   ("additionalservicetypename", Type.STRING,           100,         null,   "Наименование вида дополнительной услуги");

        col   ("uniquenumber",              Type.STRING,                        null,   "Уникальный реестровый номер (в ГИС)");
        col   ("elementguid",               Type.UUID,                          null,   "Идентификатор существующей в ГИС версии элемента справочника");

        col   ("label",                     Type.STRING,  new Virt ("(''||\"ADDITIONALSERVICETYPENAME\")"),  "Наименование");
        col   ("label_uc",                  Type.STRING,  new Virt ("UPPER(\"ADDITIONALSERVICETYPENAME\")"),  "НАИМЕНОВАНИЕ В ВЕРХНЕМ РЕГИСТРЕ");

        fk    ("id_status",                 VocAsyncEntityState.class,          new Num (VocAsyncEntityState.i.PENDING.getId ()), "Статус синхронизации");
        fk    ("id_log",                    AdditionalServiceLog.class,         null, "Последнее событие редактирования");

        key   ("label_uc", "label_uc");
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