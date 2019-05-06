package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Num;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState.i.PENDING;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
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
        
        col   ("label",                    Type.STRING,  new Virt ("(''||\"WORKNAME\")"),  "Наименование");
        col   ("label_uc",                 Type.STRING,  new Virt ("UPPER(\"WORKNAME\")"),  "НАИМЕНОВАНИЕ В ВЕРХНЕМ РЕГИСТРЕ");
        col   ("servicetyperef",           Type.STRING,  new Virt ("(''||\"CODE_VC_NSI_56\")"),  "Вид работ");

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
            + "  COUNT(tb_svc_payments.uuid) INTO cnt"
            + " FROM"
            + "  tb_svc_payments"
            + " LEFT JOIN tb_ctr_payments ON tb_svc_payments.uuid_contract_payment = tb_ctr_payments.uuid"
            + " LEFT JOIN tb_ch_payments ON tb_svc_payments.uuid_charter_payment = tb_ch_payments.uuid"
            + " WHERE"
            + "  tb_svc_payments.is_deleted=0"
            + "  AND (tb_ctr_payments.is_deleted=0 AND tb_ctr_payments.id_ctr_status <> " + VocGisStatus.i.ANNUL
            + "    OR tb_ch_payments.is_deleted=0 AND tb_ch_payments.id_ctr_status <> " + VocGisStatus.i.ANNUL
            + "  )"
            + "  AND tb_svc_payments.uuid_org_work=:NEW.uuid; "
            + " IF cnt>0 THEN raise_application_error (-20000, 'Удаление запрещено. Запись используется в сведениях о размере платы за услуги управления.'); END IF; "

            + " SELECT"
            + "  COUNT(tb_unplanned_works.uuid) INTO cnt"
            + " FROM"
            + "  tb_unplanned_works"
            + " LEFT JOIN tb_reporting_periods ON tb_unplanned_works.uuid_reporting_period = tb_reporting_periods.uuid"
            + " WHERE"
            + "  tb_unplanned_works.is_deleted=0"
            + "  AND tb_reporting_periods.is_deleted=0"
            + "  AND tb_reporting_periods.id_ctr_status <> " + VocGisStatus.i.ANNUL
            + "  AND tb_unplanned_works.uuid_org_work=:NEW.uuid; "
            + " IF cnt>0 THEN raise_application_error (-20000, 'Удаление запрещено. Запись используется во выполненных внеплановых работах/услугах периодов отчётности в планах работ и услуг'); END IF; "

            + " SELECT"
            + "  COUNT(tb_work_list_items.uuid) INTO cnt"
            + " FROM"
            + "  tb_work_list_items"
            + " LEFT JOIN tb_work_lists ON tb_work_list_items.uuid_working_list = tb_work_lists.uuid"
            + " WHERE"
            + "  tb_work_list_items.is_deleted=0"
            + "  AND tb_work_lists.is_deleted=0"
            + "  AND tb_work_lists.id_ctr_status <> " + VocGisStatus.i.ANNUL
            + "  AND tb_work_list_items.uuid_org_work=:NEW.uuid; "
            + " IF cnt>0 THEN raise_application_error (-20000, 'Удаление запрещено. Запись используется в перечне работ и услуг на период'); END IF; "

            + "END; END IF;"
        + "END;");

    }

}