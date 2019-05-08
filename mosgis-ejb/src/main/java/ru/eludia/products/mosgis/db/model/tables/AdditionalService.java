package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.db.util.SyncMap;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
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
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementFieldType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementOkeiRefFieldType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementStringFieldType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiRef;

public class AdditionalService extends EnTable {

    public static final String TABLE_NAME = "tb_add_services";

    public enum c implements EnColEnum {

        UUID_ORG                  (VocOrganization.class,                      "Организация"),

        OKEI                      (VocOkei.class,                      null,   "Единица измерения"),
        ADDITIONALSERVICETYPENAME (Type.STRING,           100,         null,   "Наименование вида дополнительной услуги"),

        UNIQUENUMBER              (Type.STRING,                        null,   "Уникальный реестровый номер (в ГИС)"),
        ELEMENTGUID               (Type.UUID,                          null,   "Идентификатор существующей в ГИС версии элемента справочника"),

        LABEL                     (Type.STRING,  new Virt ("(''||\"ADDITIONALSERVICETYPENAME\")"),  "Наименование"),
        LABEL_UC                  (Type.STRING,  new Virt ("UPPER(\"ADDITIONALSERVICETYPENAME\")"),  "НАИМЕНОВАНИЕ В ВЕРХНЕМ РЕГИСТРЕ"),
        
        GUID                      (Type.UUID,    new Virt ("HEXTORAW(''||RAWTOHEX(\"ELEMENTGUID\"))"),  "GUID НСИ"),
        CODE                      (Type.STRING,  new Virt ("(''||\"UNIQUENUMBER\")"),  "Код НСИ"),

        ID_STATUS                 (VocAsyncEntityState.class,          new Num (VocAsyncEntityState.i.PENDING.getId ()), "Статус синхронизации"),
        ID_LOG                    (AdditionalServiceLog.class,         null, "Последнее событие редактирования"),
        
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

    public AdditionalService () {

        super (TABLE_NAME,                                                       "Дополнительные услуги");
        cols   (c.class);

        key   ("uuid_org", "uuid_org");
        key   ("label_uc", "label_uc");
        key   ("org_label", "uuid_org", "label");

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
            + "  COUNT(tb_account_svc.uuid) INTO cnt"
            + " FROM"
            + "  tb_account_svc"
            + " LEFT JOIN tb_accounts ON tb_account_svc.uuid_account = tb_accounts.uuid"
            + " WHERE"
            + "  tb_account_svc.is_deleted=0"
            + "  AND tb_accounts.is_deleted=0"
            + "  AND tb_accounts.id_ctr_status <> " + VocGisStatus.i.ANNUL
            + "  AND tb_account_svc.uuid_add_service=:NEW.uuid; "
            + " IF cnt>0 THEN raise_application_error (-20000, 'Удаление запрещено. Запись используется в лицевом счёте.'); END IF; "

            + " SELECT"
            + "  COUNT(tb_charter_services.uuid) INTO cnt"
            + " FROM"
            + "  tb_charter_services"
            + " LEFT JOIN tb_charters ON tb_charter_services.uuid_charter = tb_charters.uuid"
            + " WHERE"
            + "  tb_charter_services.is_deleted=0"
            + "  AND tb_charters.is_deleted=0"
            + "  AND tb_charters.id_ctr_status <> " + VocGisStatus.i.ANNUL
            + "  AND tb_charter_services.uuid_add_service=:NEW.uuid; "
            + " IF cnt>0 THEN raise_application_error (-20000, 'Удаление запрещено. Запись используется в уставе.'); END IF; "

            + " SELECT"
            + "  COUNT(tb_contract_services.uuid) INTO cnt"
            + " FROM"
            + "  tb_contract_services"
            + " LEFT JOIN tb_contracts ON tb_contract_services.uuid_contract = tb_contracts.uuid"
            + " WHERE"
            + "  tb_contract_services.is_deleted=0"
            + "  AND tb_contracts.is_deleted=0"
            + "  AND tb_contracts.id_ctr_status <> " + VocGisStatus.i.ANNUL
            + "  AND tb_contract_services.uuid_add_service=:NEW.uuid; "
            + " IF cnt>0 THEN raise_application_error (-20000, 'Удаление запрещено. Запись используется в договоре управления.'); END IF; "

            + " SELECT"
            + "  COUNT(tb_charge_info.uuid) INTO cnt"
            + " FROM"
            + "  tb_charge_info"
            + " LEFT JOIN tb_pay_docs ON tb_charge_info.uuid_pay_doc = tb_pay_docs.uuid"
            + " WHERE"
            + "  tb_charge_info.is_deleted=0"
            + "  AND tb_pay_docs.is_deleted=0"
            + "  AND tb_pay_docs.id_ctr_status <> " + VocGisStatus.i.ANNUL
            + "  AND tb_charge_info.uuid_add_service=:NEW.uuid; "
            + " IF cnt>0 THEN raise_application_error (-20000, 'Удаление запрещено. Запись используется в платежных документах.'); END IF; "

            + "END; END IF;"

        + "END;");        

    }
    
    private final static String [] keyFields = {"uniquenumber"};

    public class Sync extends SyncMap<NsiRef> {
        
        UUID uuid_org;

        public Sync (DB db, UUID uuid_org) {
            super (db);
            this.uuid_org = uuid_org;
            commonPart.put ("uuid_org", uuid_org);
            commonPart.put ("is_deleted", 0);
        }                

        @Override
        public String[] getKeyFields () {
            return keyFields;
        }

        @Override
        public void setFields (Map<String, Object> h, NsiRef o) {
            h.put ("uniquenumber", o.getCode ());
        }

        @Override
        public Table getTable () {
            return AdditionalService.this;
        }
        
    }
    
    public static Map<String, Object> toHASH (NsiElementType t) {
        
        final Map<String, Object> result = DB.HASH (
            "is_deleted",   t.isIsActual () ? 0 : 1,
            "uniquenumber", t.getCode (),
            "elementguid",  t.getGUID ()
        );

        for (NsiElementFieldType f: t.getNsiElementField ()) {
            
            if (f instanceof NsiElementOkeiRefFieldType) {
                result.put ("okei", ((NsiElementOkeiRefFieldType) f).getCode ());
            }
            else if (f instanceof NsiElementStringFieldType && "Вид дополнительной услуги".equals (f.getName ())) {
                result.put ("additionalservicetypename", ((NsiElementStringFieldType) f).getValue ());
            }
            
        }

        return result;
        
    }    

}