package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import java.util.UUID;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Num;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocContractDocType;
import ru.eludia.products.mosgis.db.model.voc.VocGisContractType;
import ru.eludia.products.mosgis.db.model.voc.VocGisCustomerType;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.gosuslugi.dom.schema.integration.house_management.BaseServiceType;
import ru.gosuslugi.dom.schema.integration.house_management.ContractType;
import ru.gosuslugi.dom.schema.integration.house_management.DateDetailsType;
import ru.gosuslugi.dom.schema.integration.house_management.DeviceMeteringsDaySelectionType;
import ru.gosuslugi.dom.schema.integration.house_management.ImportContractRequest;

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

                + " IF :NEW.ddt_m_start IS NULL THEN raise_application_error (-20000, 'Не задано начало периода ввода показаний приборов учёта. Операция отменена.'); END IF; "
                + " IF :NEW.ddt_m_end   IS NULL THEN raise_application_error (-20000, 'Не задано окончание периода ввода показаний приборов учёта. Операция отменена.'); END IF; "
                + " IF :NEW.ddt_d_start IS NULL THEN raise_application_error (-20000, 'Не задан срок выставления платежных документов. Операция отменена.'); END IF; "
                + " IF :NEW.ddt_i_start IS NULL THEN raise_application_error (-20000, 'Не задан срок внесения платы за помещение / услуги. Операция отменена.'); END IF; "

                + " SELECT COUNT(*) INTO cnt FROM tb_contract_objects WHERE uuid_contract = :NEW.uuid AND is_deleted = 0"
                + " ; IF cnt=0 THEN "
                + "   raise_application_error (-20000, 'Не заявлен ни один объект управления. Операция отменена.'); "
                + " END IF; "

                + " SELECT COUNT(*) INTO cnt FROM tb_contract_files WHERE uuid_contract = :NEW.uuid AND id_status = 1 AND id_type = " + VocContractDocType.i.CONTRACT.getId ()
                + " ; IF cnt=0 THEN "
                + "   raise_application_error (-20000, 'На в вкладке \"документы\" не приложен файл договора. Операция отменена.'); "
                + " END IF; "

                + "IF :NEW.code_vc_nsi_58 = 2 THEN "
                + " SELECT COUNT(*) INTO cnt FROM tb_contract_files WHERE uuid_contract = :NEW.uuid AND id_status = 1 AND id_type = " + VocContractDocType.i.PROTOCOL_OK.getId ()
                + " ; IF cnt=0 THEN "
                + "   raise_application_error (-20000, 'К договору не приложен протокол открытого конкурса. Операция отменена.'); "
                + " END IF; "
                + "END IF; "

                + "IF :NEW.code_vc_nsi_58 = 10 THEN "
                + " SELECT COUNT(*) INTO cnt FROM tb_contract_files WHERE uuid_contract = :NEW.uuid AND id_status = 1 AND id_type = " + VocContractDocType.i.CHARTER.getId ()
                + " ; IF cnt=0 THEN "
                + "   raise_application_error (-20000, 'К договору не приложен устав. Операция отменена.'); "
                + " END IF; "
                + "END IF; "

                + "IF :NEW.code_vc_nsi_58 = 5 THEN "
                + " SELECT COUNT(*) INTO cnt FROM tb_contract_files WHERE uuid_contract = :NEW.uuid AND id_status = 1 AND id_type = " + VocContractDocType.i.PROTOCOL_MEETING_BOARD.getId ()
                + " ; IF cnt=0 THEN "
                + "   raise_application_error (-20000, 'К договору не приложен протокол заседания правления. Операция отменена.'); "
                + " END IF; "
                + "END IF; "
                        
                + "IF :NEW.code_vc_nsi_58 = 6 THEN "
                + " SELECT COUNT(*) INTO cnt FROM tb_contract_files WHERE uuid_contract = :NEW.uuid AND id_status = 1 AND id_type = " + VocContractDocType.i.PROTOCOL_BUILDING_OWNER.getId ()
                + " ; IF cnt=0 THEN "
                + "   raise_application_error (-20000, 'К договору не приложено решение органа управления застройщика. Операция отменена.'); "
                + " END IF; "
                + "END IF; "
                        
                + "IF :NEW.code_vc_nsi_58 = 9 THEN "
                + " FOR i IN ("
                + "  SELECT "
                + "    a.label "
                + "  FROM "
                + "    tb_contract_objects o"
                + "    LEFT JOIN tb_contract_files f ON (f.uuid_contract_object=o.uuid AND f.id_status = 1 AND f.id_type=" + VocContractDocType.i.COMMISSIONING_PERMIT_AGREEMENT.getId () + ")"
                + "    LEFT JOIN vc_build_addresses a ON (o.fiashouseguid = a.houseguid)"
                + "  WHERE 1=1"
                + "    AND o.uuid_contract = :NEW.uuid"
                + "    AND o.is_deleted = 0"
                + "    AND f.uuid IS NULL"
                + "  ) LOOP"
                + "   raise_application_error (-20000, 'Для объекта по адресу ' || i.label || ' не приложено разрешение на ввод в эксплуатацию. Операция отменена.'); "
                + " END LOOP; "
                + "END IF; "

                        
                + "IF :NEW.code_vc_nsi_58 = 1 THEN "                        

                    + "IF :NEW.id_customer_type = 1 THEN BEGIN "
                        
                        + " SELECT COUNT(*) INTO cnt FROM tb_contract_files WHERE uuid_contract = :NEW.uuid AND id_status = 1 AND id_type = " + VocContractDocType.i.SIGNED_OWNERS.getId ()
                        + " ; IF cnt=0 THEN "
                        + "   raise_application_error (-20000, 'К договору не приложен реестр собственников. Операция отменена.'); "
                        + " END IF; "

                        + " SELECT COUNT(*) INTO cnt FROM tb_contract_files WHERE uuid_contract = :NEW.uuid AND id_status = 1 AND id_type = " + VocContractDocType.i.PROTOCOL_MEETING_OWNERS.getId ()
                        + " ; IF cnt=0 THEN "
                        + "   raise_application_error (-20000, 'К договору не приложен протокол собрания собственников. Операция отменена.'); "
                        + " END IF; "
                                
                    + "END; ELSE "
                        
                        + " FOR i IN ("
                        + "  SELECT "
                        + "    a.label "
                        + "  FROM "
                        + "    tb_contract_objects o"
                        + "    LEFT JOIN tb_contract_files f ON (f.uuid_contract_object=o.uuid AND f.id_status = 1 AND f.id_type=" + VocContractDocType.i.SIGNED_OWNERS.getId () + ")"
                        + "    LEFT JOIN vc_build_addresses a ON (o.fiashouseguid = a.houseguid)"
                        + "  WHERE 1=1"
                        + "    AND o.uuid_contract = :NEW.uuid"
                        + "    AND o.is_deleted = 0"
                        + "    AND f.uuid IS NULL"
                        + "  ) LOOP"
                        + "   raise_application_error (-20000, 'Для объекта по адресу ' || i.label || ' не приложен реестр собственников. Операция отменена.'); "
                        + " END LOOP; "
                                
                        + " FOR i IN ("
                        + "  SELECT "
                        + "    a.label "
                        + "  FROM "
                        + "    tb_contract_objects o"
                        + "    LEFT JOIN tb_contract_files f ON (f.uuid_contract_object=o.uuid AND f.id_status = 1 AND f.id_type=" + VocContractDocType.i.PROTOCOL_MEETING_OWNERS.getId () + ")"
                        + "    LEFT JOIN vc_build_addresses a ON (o.fiashouseguid = a.houseguid)"
                        + "  WHERE 1=1"
                        + "    AND o.uuid_contract = :NEW.uuid"
                        + "    AND o.is_deleted = 0"
                        + "    AND f.uuid IS NULL"
                        + "  ) LOOP"
                        + "   raise_application_error (-20000, 'Для объекта по адресу ' || i.label || ' не приложен протокол собрания собственников. Операция отменена.'); "
                        + " END LOOP; "
                                                
                    + "END IF; "
                        
                + "END IF; "

            + "END IF; "

        + "END;");        

    }
    
    private static DateDetailsType.PaymentDocumentInterval paymentDocumentInterval (Map<String, Object> r, String key) {
        final DateDetailsType.PaymentDocumentInterval result = new DateDetailsType.PaymentDocumentInterval ();
        String s = r.get (key).toString ();
        result.setLastDay ("99".equals (s) ? true: null);
        if (result.isLastDay () == null) result.setStartDate (Byte.parseByte (s));
        if ("1".equals (r.get (key + "_nxt").toString ())) result.setNextMounth (true); else result.setCurrentMounth (true);
        return result;
    }
    
    private static DateDetailsType.PaymentInterval paymentInterval (Map<String, Object> r, String key) {
        final DateDetailsType.PaymentInterval result = new DateDetailsType.PaymentInterval ();
        String s = r.get (key).toString ();
        result.setLastDay ("99".equals (s) ? true: null);
        if (result.isLastDay () == null) result.setStartDate (Byte.parseByte (s));
        if ("1".equals (r.get (key + "_nxt").toString ())) result.setNextMounth (true); else result.setCurrentMounth (true);
        return result;
    }

    private static DeviceMeteringsDaySelectionType deviceMeteringsDaySelectionType (Map<String, Object> r, String key) {        
        final DeviceMeteringsDaySelectionType result = new DeviceMeteringsDaySelectionType ();
        String s = r.get (key).toString ();
        result.setLastDay ("99".equals (s) ? true: null);
        if (result.isLastDay () == null) result.setDate (Byte.parseByte (s));
        result.setIsNextMonth ("1".equals (r.get (key + "_nxt").toString ()));
        return result;
    }

    public static void fillContract (ContractType c, Map<String, Object> r) {

        VocGisCustomerType.i.forId (r.get ("id_customer_type")).setCustomer (c, (UUID) r.get ("uuid_org_customer"));

        c.setContractBase (NsiTable.toDom ((String) r.get ("code_vc_nsi_58"), (UUID) r.get ("vc_nsi_58.guid")));
       
        final DateDetailsType dd = new DateDetailsType ();
        
        final DateDetailsType.PeriodMetering pm = new DateDetailsType.PeriodMetering ();                                
        pm.setStartDate (deviceMeteringsDaySelectionType       (r, "ddt_m_start"));
        pm.setEndDate   (deviceMeteringsDaySelectionType       (r, "ddt_m_end"));       
        dd.setPeriodMetering (pm);
        dd.setPaymentDocumentInterval (paymentDocumentInterval (r, "ddt_d_start"));
        dd.setPaymentInterval         (paymentInterval         (r, "ddt_i_start"));                
        c.setDateDetails (dd);
                
    }
    
}