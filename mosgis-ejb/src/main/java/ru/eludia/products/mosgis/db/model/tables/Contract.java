package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Num;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.voc.VocContractDocType;
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
        fk    ("id_ctr_status_gis",         VocGisStatus.class,          new Num (VocGisStatus.i.PROJECT.getId ()), "Статус договора с точки зрения ГИС ЖКХ");
        fk    ("id_ctr_state_gis",          VocGisStatus.class,          new Num (VocGisStatus.i.NOT_RUNNING.getId ()), "Состояние договора с точки зрения ГИС ЖКХ");
        
        col   ("docnum",                    Type.STRING,           255,         "Номер договора");
        col   ("signingdate",               Type.DATE,                          "Дата заключения");
        col   ("effectivedate",             Type.DATE,                          "Дата вступления в силу");
        col   ("plandatecomptetion",        Type.DATE,                          "Планируемая дата окончания");

        col   ("automaticrolloveroneyear",  Type.BOOLEAN,          Bool.FALSE,  "1, если запись удалена; иначе 0");
        col   ("code_vc_nsi_58",            Type.STRING,           20,   null,  "Ссылка на НСИ \"Основание заключения договора\" (реестровый номер 58)");

    //  DateDetailsType:

        //    PeriodMetering
        col   ("ddt_m_start",               Type.NUMERIC,          2,   null,  "Начало периода ввода показаний ПУ (1..31 — конкретное число; 99 — последнее число)");
        col   ("ddt_m_start_nxt",           Type.BOOLEAN,          Bool.FALSE, "1, если начало периода ввода показаний ПУ в следующем месяце; иначе 0");
        col   ("ddt_m_end",                 Type.NUMERIC,          2,   null,  "Окончание периода ввода показаний ПУ (1..31 — конкретное число; 99 — последнее число)");
        col   ("ddt_m_end_nxt",             Type.BOOLEAN,          Bool.FALSE, "1, если окончание периода ввода показаний ПУ в следующем месяце; иначе 0");

        //    PaymentDocumentInterval
        col   ("ddt_d_start",               Type.NUMERIC,          2,   null,  "Срок представления (выставления) платежных документов для внесения платы за жилое помещение и (или) коммунальные услуги (1..30 — конкретное число; 99 — последнее число)");
        col   ("ddt_d_start_nxt",           Type.BOOLEAN,          Bool.FALSE, "1, если срок представления (выставления) платежных документов для внесения платы за жилое помещение и (или) коммунальные услуги в следующем месяце; иначе 0");

        //    PaymentInterval
        col   ("ddt_i_start",               Type.NUMERIC,          2,   null,  "Срок внесения платы за жилое помещение и (или) коммунальные услуги (1..30 — конкретное число; 99 — последнее число)");
        col   ("ddt_i_start_nxt",           Type.BOOLEAN,          Bool.FALSE, "1, если срок внесения платы за жилое помещение и (или) коммунальные услуги в следующем месяце; иначе 0");

        col   ("contractbase",              Type.STRING,  new Virt ("(''||\"CODE_VC_NSI_58\")"),  "Основание заключения договора");

        fk    ("id_log",                    ContractLog.class,         null, "Последнее событие редактирования");

        key   ("org_docnum", "uuid_org", "docnum");

        trigger ("BEFORE INSERT OR UPDATE", ""                

        + "DECLARE "
        + " cnt INTEGER := 0;"
                
        + "BEGIN "

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

            + "IF UPDATING "
            + "  AND :OLD.id_ctr_status < " + VocGisStatus.i.APPROVED.getId ()
            + "  AND :NEW.id_ctr_status = " + VocGisStatus.i.APPROVED.getId ()
            + " THEN "

                + " SELECT COUNT(*) INTO cnt FROM tb_contract_files WHERE uuid_contract = :NEW.uuid AND id_status = 1 AND id_type = " + VocContractDocType.i.CONTRACT.getId ()
                + " ; IF cnt=0 THEN "
                + "   raise_application_error (-20000, 'На в кладке \"документы\" не приложен файл договора. Операция отменена.'); "
                + " END IF; "

                + "IF :NEW.code_vc_nsi_58 = 2 THEN "
                + " SELECT COUNT(*) INTO cnt FROM tb_contract_files WHERE uuid_contract = :NEW.uuid AND id_status = 1 AND id_type = " + VocContractDocType.i.PROTOCOL_OK.getId ()
                + " ; IF cnt=0 THEN "
                + "   raise_application_error (-20000, 'К договору не приложен протокол открытого конкурса. Операция отменена.'); "
                + " END IF; "
                + "END IF; "
                                        
            + "END IF; "

        + "END;");        

    }

}