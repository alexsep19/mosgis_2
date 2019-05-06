package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.def.Bool;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Num;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState.i.PENDING;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class MainMunicipalService extends EnTable {

    public static final String TABLE_NAME = "tb_municipal_svc";
    
    public enum c implements EnColEnum {

        UUID_ORG                 (VocOrganization.class,           "Организация"),

        UNIQUENUMBER             (Type.STRING,         null,       "Уникальный реестровый номер (в ГИС)"),
        ELEMENTGUID              (Type.UUID,           null,       "Идентификатор существующего элемента справочника"),        

        CODE_VC_NSI_3            (Type.STRING,  20,    null,       "Ссылка на НСИ \"Вид коммунальной услуги\" (реестровый номер 3)"),        
        IS_GENERAL               (Type.BOOLEAN,        Bool.FALSE, "1, если услуга предоставляется на общедомовые нужды; иначе 0"),
        SELFPRODUCED             (Type.BOOLEAN,        Bool.FALSE, "1, если услуга производится самостоятельно; иначе 0"),
        MAINMUNICIPALSERVICENAME (Type.STRING,                     "Наименование главной коммунальной услуги"),
        CODE_VC_NSI_2            (Type.STRING,  20,    null,       "Ссылка на НСИ \"Вид коммунального ресурса\" (реестровый номер 2)"),        
        OKEI                     (VocOkei.class,       null,       "Единица измерения"),
        SORTORDER                (Type.STRING,  3,     null,       "Порядок сортировки"),

        LABEL                    (Type.STRING,  new Virt ("(''||\"MAINMUNICIPALSERVICENAME\")"),  "Наименование"),
        LABEL_UC                 (Type.STRING,  new Virt ("UPPER(\"MAINMUNICIPALSERVICENAME\")"),  "НАИМЕНОВАНИЕ В ВЕРХНЕМ РЕГИСТРЕ"),

        MUNICIPALSERVICEREF      (Type.STRING,  new Virt ("(''||\"CODE_VC_NSI_3\")"),  "Вид коммунальной услуги"),
        MUNICIPALRESOURCEREF     (Type.STRING,  new Virt ("(''||\"CODE_VC_NSI_2\")"),  "Вид коммунального ресурса"),
        GENERALNEEDS             (Type.STRING,  new Virt ("DECODE(\"IS_GENERAL\",1,1,NULL)"),  "Признак \"Услуга предоставляется на общедомовые нужды\""),

        ID_STATUS                (VocAsyncEntityState.class,          new Num (VocAsyncEntityState.i.PENDING.getId ()), "Статус синхронизации"),
        ID_LOG                   (MainMunicipalServiceLog.class,          null, "Последнее событие редактирования"),

        GUID                     (Type.UUID,    new Virt ("HEXTORAW(''||RAWTOHEX(\"ELEMENTGUID\"))"),  "GUID НСИ"),
        CODE                     (Type.STRING,  new Virt ("(''||\"UNIQUENUMBER\")"),  "Код НСИ"),

        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

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
        
    public MainMunicipalService () {
        
        super (TABLE_NAME, "Коммунальные услуги");
        
        cols (c.class);

        key   ("org_sort", "uuid_org", "sortorder");
        
        trigger ("BEFORE INSERT OR UPDATE", ""

            + "DECLARE "
            + " cnt INTEGER := 0;"
            + "BEGIN "

            + "IF UPDATING "
            + "  AND :OLD.id_log IS NOT NULL "             // что-то уже отправляли
            + "  AND :OLD.id_log <> :NEW.id_log "          // пытаются отредактировать вновь
            + "  AND :OLD.id_status = " + PENDING.getId () // прошлая синхронизация не доехала
            + " THEN"
            + "  raise_application_error (-20000, 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.'); "
            + "END IF; "
                    
            + "IF :OLD.is_deleted=0 AND :NEW.is_deleted=1 THEN BEGIN "
            + " SELECT"
            + "  COUNT(tb_charge_info.uuid) INTO cnt"
            + " FROM"
            + "  tb_charge_info"
            + " LEFT JOIN tb_pay_docs ON tb_charge_info.uuid_pay_doc = tb_pay_docs.uuid"
            + " WHERE"
            + "  tb_charge_info.is_deleted=0"
            + "  AND tb_pay_docs.is_deleted=0"
            + "  AND tb_pay_docs.id_ctr_status <> " + VocGisStatus.i.ANNUL
            + "  AND tb_charge_info.uuid_m_m_service=:NEW.uuid; "
            + " IF cnt>0 THEN raise_application_error (-20000, 'Удаление запрещено. Запись используется в платежных документах.'); END IF; "
            + "END; END IF;"        
        + "END;");        

    }        
    
}
