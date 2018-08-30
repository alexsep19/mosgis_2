package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Num;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.voc.VocGisContractType;
import ru.eludia.products.mosgis.db.model.voc.VocGisCustomerType;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class Contract extends Table {

    public Contract () {

        super ("tb_contracts", "Договоры");

        pk    ("uuid",                      Type.UUID,             NEW_UUID,            "Ключ");
        col   ("is_deleted",                Type.BOOLEAN,          Bool.FALSE,          "1, если запись удалена; иначе 0");

        fk    ("id_contract_type",          VocGisContractType.class,                   "Тип договора");
        fk    ("uuid_org",                  VocOrganization.class,                      "Исполнитель");

        fk    ("uuid_org_customer",         VocOrganization.class,              null,   "Заказчик");
        fk    ("id_customer_type",          VocGisCustomerType.class,                   "Тип заказчика");
        
        fk    ("id_ctr_status",             VocGisStatus.class,          new Num (VocGisStatus.i.PROJECT.getId ()), "Статус договора с точки зрения mosgis");
        fk    ("id_ctr_status_gis",         VocGisStatus.class,          new Num (VocGisStatus.i.NOT_RUNNING.getId ()), "Статус договора с точки зрения ГИС ЖКХ");
        
        col   ("docnum",                    Type.STRING,           255,         "Номер договора");
        col   ("signingdate",               Type.DATE,                          "Дата заключения");
        col   ("effectivedate",             Type.DATE,                          "Дата вступления в силу");
        col   ("plandatecomptetion",        Type.DATE,                          "Планируемая дата окончания");

        col   ("automaticrolloveroneyear",  Type.BOOLEAN,          Bool.FALSE,  "1, если запись удалена; иначе 0");
        col   ("code_vc_nsi_58",            Type.STRING,           20,   null,  "Ссылка на НСИ \"Основание заключения договора\" (реестровый номер 58)");
 
        col   ("contractbase",              Type.STRING,  new Virt ("(''||\"CODE_VC_NSI_58\")"),  "Основание заключения договора");

        fk    ("id_log",                    ContractLog.class,         null, "Последнее событие редактирования");

        key   ("org_docnum", "uuid_org", "docnum");

        trigger ("BEFORE INSERT OR UPDATE", "BEGIN "

            + "IF :NEW.id_contract_type = " + VocGisContractType.i.MGMT.getId () + " THEN "

                + "IF :NEW.uuid_org_customer IS NULL "
                + "  THEN "
                + "    :NEW.id_customer_type:=" + VocGisCustomerType.i.OWNERS.getId () + "; "
                + "  ELSE "
                + "    SELECT MIN(id) INTO :NEW.id_customer_type FROM vc_gis_customer_type_nsi_20 "
                + "      WHERE code IN (SELECT code FROM vc_orgs_nsi_20 WHERE uuid = :NEW.uuid_org_customer);"
                + "    IF :NEW.id_customer_type IS NULL THEN "
                + "      raise_application_error (-20000, 'Указанная организация не зарегистрирована в ГИС ЖКХ как возможный заказчик договора управления: ТСЖ, ЖСК и т. п. Операция отменена.'); "
                + "    END IF; "
                + "END IF; "

                + "SELECT MIN(code) INTO :NEW.code_vc_nsi_58 FROM vc_gis_customer_type_nsi_58 WHERE code=:NEW.code_vc_nsi_58 AND id=:NEW.id_customer_type; "
                + "IF :NEW.code_vc_nsi_58 IS NULL THEN "
                + " raise_application_error (-20000, '#code_vc_nsi_58#: Указанное основание заключение договора неприменимо для организации-заказчика. Операция отменена.'); "
                + "END IF; "
                        
            + "END IF; "

/*                
                        
                        
                        
            + "IF UPDATING "
            + "  AND :OLD.id_log IS NOT NULL "             // что-то уже отправляли
            + "  AND :OLD.id_log <> :NEW.id_log "          // пытаются отредактировать вновь
            + "  AND :OLD.id_status = " + PENDING.getId () // прошлая синхронизация не доехала
            + " THEN"
            + "  raise_application_error (-20000, 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.'); "
            + "END IF; "
*/
        + "END;");        

    }

}