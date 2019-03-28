package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Blob.EMPTY_BLOB;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Num;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationInsurance;

public class InsuranceProduct extends Table {

    public static final String TABLE_NAME = "tb_ins_products";

    public InsuranceProduct () {

        super (TABLE_NAME,                                                       "Страховой продукт");

        pk    ("uuid",                      Type.UUID,             NEW_UUID,            "Ключ");
        fk    ("uuid_org",                  VocOrganization.class,                      "Организация");
        col   ("is_deleted",                Type.BOOLEAN,          Bool.FALSE,          "1, если запись удалена; иначе 0");
        
        fk    ("insuranceorg",              VocOrganizationInsurance.class,             "Корневой идентификатор страховой компании из реестра организаций");
        col   ("name",                      Type.STRING,           1024,        null,   "Наименование вложения");
        col   ("description",               Type.STRING,           500,         null,   "Описание вложения");
        col   ("mime",                      Type.STRING,                                "Тип содержимого");
        col   ("len",                       Type.INTEGER,                               "Размер, байт");
        col   ("body",                      Type.BLOB,             EMPTY_BLOB,          "Содержимое");       
        
        col   ("attachmentguid",            Type.UUID,                          null,   "Идентификатор сохраненного вложения");        
        col   ("attachmenthash",            Type.BINARY,           32,          null,   "ГОСТ Р 34.11-94");

        col   ("insuranceproductguid",      Type.UUID,                          null,   "Идентификатор страхового продукта");
        col   ("uniquenumber",              Type.STRING,                        null,   "Уникальный реестровый номер");

        col   ("label",                     Type.STRING,  new Virt ("(''||\"DESCRIPTION\")"),  "Наименование");
        col   ("label_uc",                  Type.STRING,  new Virt ("UPPER(\"DESCRIPTION\")"),  "НАИМЕНОВАНИЕ В ВЕРХНЕМ РЕГИСТРЕ");

        fk    ("id_status",                 VocAsyncEntityState.class,          new Num (VocAsyncEntityState.i.PENDING.getId ()), "Статус синхронизации");
        fk    ("id_log",                    InsuranceProductLog.class,          null, "Последнее событие редактирования");

        key   ("label_uc", "label_uc");
        key   ("org_label", "uuid_org", "label");
        key   ("insuranceorg", "insuranceorg");

        trigger ("BEFORE INSERT OR UPDATE", "BEGIN "

            + "IF UPDATING "
            + "  AND :OLD.id_log IS NOT NULL "             // что-то уже отправляли
            + "  AND :OLD.id_log <> :NEW.id_log "          // пытаются отредактировать вновь
            + "  AND :OLD.id_status = " + VocAsyncEntityState.i.PENDING.getId () // прошлая синхронизация не доехала
            + " THEN"
            + "  raise_application_error (-20000, 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.'); "
            + "END IF; "

            + "IF UPDATING "
            + "  AND :NEW.len = 0 "
            + " THEN"
            + "  :NEW.body := EMPTY_BLOB(); "
            + "END IF; "
                    
        + "END;");        

    }

}