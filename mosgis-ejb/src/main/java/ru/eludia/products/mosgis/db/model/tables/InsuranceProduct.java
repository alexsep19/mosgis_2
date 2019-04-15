package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Blob.EMPTY_BLOB;
import ru.eludia.base.model.def.Num;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationInsurance;

public class InsuranceProduct extends EnTable {

    public static final String TABLE_NAME = "tb_ins_products";
    
    public enum c implements EnColEnum {
        
        UUID_ORG                  (VocOrganization.class,                      "Организация"),

        INSURANCEORG              (VocOrganizationInsurance.class,             "Корневой идентификатор страховой компании из реестра организаций"),
        NAME                      (Type.STRING,           1024,        null,   "Наименование вложения"),
        DESCRIPTION               (Type.STRING,           500,         null,   "Описание вложения"),
        MIME                      (Type.STRING,                                "Тип содержимого"),
        LEN                       (Type.INTEGER,                               "Размер, байт"),
        BODY                      (Type.BLOB,             EMPTY_BLOB,          "Содержимое"),       

        ATTACHMENTGUID            (Type.UUID,                          null,   "Идентификатор сохраненного вложения"),        
        ATTACHMENTHASH            (Type.BINARY,           32,          null,   "ГОСТ Р 34.11-94"),

        INSURANCEPRODUCTGUID      (Type.UUID,                          null,   "Идентификатор страхового продукта"),
        UNIQUENUMBER              (Type.STRING,                        null,   "Уникальный реестровый номер"),

        LABEL                     (Type.STRING,  new Virt ("(''||\"DESCRIPTION\")"),  "Наименование"),
        LABEL_UC                  (Type.STRING,  new Virt ("UPPER(\"DESCRIPTION\")"),  "НАИМЕНОВАНИЕ В ВЕРХНЕМ РЕГИСТРЕ"),

        ID_STATUS                 (VocAsyncEntityState.class,          new Num (VocAsyncEntityState.i.PENDING.getId ()), "Статус синхронизации"),
        ID_LOG                    (InsuranceProductLog.class,          null, "Последнее событие редактирования"),
        
        ;

        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {

            switch (this) {
                case UUID_ORG:
                case ID_LOG:
                    return false;
                default:
                    return true;
            }

        }

    }    

    public InsuranceProduct () {

        super (TABLE_NAME, "Страховой продукт");

        cols (c.class);

        key   (c.LABEL_UC);
        key   (c.INSURANCEORG);        
        key   ("org_label", "uuid_org", "label");

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