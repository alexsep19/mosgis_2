package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Blob.EMPTY_BLOB;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocUser;

public class InsuranceProductLog extends Table {

    public InsuranceProductLog () {

        super ("tb_ins_products__log",                                                  "Страховой продукт: история изменения");

        pk    ("uuid",                      Type.UUID,             NEW_UUID,            "Ключ");
        ref   ("action",                    VocAction.class,                            "Действие");

        fk    ("uuid_object",               InsuranceProduct.class,                     "Ссылка на запись");
        col   ("ts",                        Type.TIMESTAMP,        NOW,                 "Дата/время события");
        fk    ("uuid_user",                 VocUser.class,                      null,   "Оператор");

        col   ("is_deleted",                Type.BOOLEAN,                       null,   "1, если запись удалена; иначе 0");                

        col   ("insuranceorg",              Type.UUID,                          null,   "Корневой идентификатор страховой компании из реестра организаций");
        col   ("name",                      Type.STRING,           1024,        null,   "Наименование вложения");
        col   ("description",               Type.STRING,           500,         null,   "Описание вложения");
        col   ("mime",                      Type.STRING,                        null,   "Тип содержимого");
        col   ("len",                       Type.INTEGER,                       null,   "Размер, байт");
        col   ("body",                      Type.BLOB,             EMPTY_BLOB,          "Содержимое");       
        
        col   ("attachmentguid",            Type.UUID,                          null,   "Идентификатор сохраненного вложения");        
        col   ("attachmenthash",            Type.BINARY,           32,          null,   "ГОСТ Р 34.11-94");
        
        col   ("insuranceproductguid",      Type.UUID,                          null,   "Идентификатор страхового продукта");

        fk    ("uuid_out_soap",             OutSoap.class,                      null,   "Последний запрос на импорт в ГИС ЖКХ");
        col   ("uuid_message",              Type.UUID,                          null,   "UUID запроса в ГИС ЖКХ");
        col   ("insuranceproductguid_new",  Type.UUID,                          null,   "Идентификатор новой версии существующего (в ГИС) элемента справочника");
        
        key   ("ts", "uuid_object", "ts");
        key   ("uuid_out_soap", "uuid_out_soap");

        trigger ("BEFORE INSERT", "BEGIN "

            + "SELECT"
            + "       is_deleted,          "
            + "       insuranceorg,        "
            + "       name,                "
            + "       description,         "
            + "       mime,                "
            + "       len,                 "
            + "       body,                "
            + "       insuranceproductguid "

            + "INTO " 
            + "       :NEW.is_deleted,          "
            + "       :NEW.insuranceorg,        "
            + "       :NEW.name,                "
            + "       :NEW.description,         "
            + "       :NEW.mime,                "
            + "       :NEW.len,                 "
            + "       :NEW.body,                "
            + "       :NEW.insuranceproductguid "
                
            + " FROM tb_ins_products WHERE uuid=:NEW.uuid_object; "

        + "END;");

        trigger ("AFTER INSERT", "BEGIN "
            + " RETURN;"
        + "END;");

        trigger ("AFTER UPDATE", "BEGIN "
                
            + "IF NVL (:NEW.attachmentguid, '00') <> NVL (:OLD.attachmentguid, '00') THEN "
                + "UPDATE tb_ins_products SET "
                    + "attachmentguid = :NEW.attachmentguid, "
                    + "attachmenthash = :NEW.attachmenthash "
                + "WHERE uuid = :NEW.uuid_object; "
            + "END IF; "
                
            + "IF :NEW.uuid_out_soap <> NVL (:OLD.uuid_out_soap, '00') THEN "
                + "UPDATE out_soap SET "
                    + "uuid_ack = :NEW.uuid_message "
                + "WHERE uuid = :NEW.uuid_out_soap; "
            + "END IF; "
                
        + "END;");

    }

}