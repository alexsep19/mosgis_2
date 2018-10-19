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
import static ru.eludia.products.mosgis.db.model.voc.VocGisStatus.i.ANNUL;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.gosuslugi.dom.schema.integration.house_management.ContractType;
import ru.gosuslugi.dom.schema.integration.house_management.DateDetailsType;
import ru.gosuslugi.dom.schema.integration.house_management.DeviceMeteringsDaySelectionType;    

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
        col   ("terminate",                 Type.DATE,             null,        "Дата расторжения");
        col   ("rolltodate",                Type.DATE,             null,        "Пролонгировать до даты");

        col   ("automaticrolloveroneyear",  Type.BOOLEAN,          Bool.FALSE,  "Автоматически продлить срок оказания услуг на один год");
        col   ("code_vc_nsi_58",            Type.STRING,           20,   null,  "Ссылка на НСИ \"Основание заключения договора\" (реестровый номер 58)");
        col   ("code_vc_nsi_54",            Type.STRING,           20,   null,  "Ссылка на НСИ \"Основание расторжения договора\" (реестровый номер 54)");

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
        
        fk    ("uuid_out_soap",             OutSoap.class,             null,    "Последний запрос на импорт в ГИС ЖКХ");
        col   ("contractguid",              Type.UUID,                 null,    "UUID договора в ГИС ЖКХ");
        col   ("contractversionguid",       Type.UUID,                 null,    "Идентификатор последней известной версии договора");

        fk    ("id_log",                    ContractLog.class,         null,    "Последнее событие редактирования");
        
        col   ("versionnumber",             Type.INTEGER,          10, null,    "Номер версии (по состоянию в ГИС ЖКХ)");
        col   ("reasonofannulment",         Type.STRING,         1000, null,    "Причина аннулирования");
        col   ("is_annuled",                Type.BOOLEAN,          new Virt ("DECODE(\"REASONOFANNULMENT\",NULL,0,1)"),  "1, если запись аннулирована; иначе 0");

        key   ("org_docnum", "uuid_org", "docnum");
        key   ("contractguid", "contractguid");
        
        trigger ("BEFORE UPDATE", ""
                
            + "DECLARE" 
            + " PRAGMA AUTONOMOUS_TRANSACTION; "
            + "BEGIN "

            + "IF 1=1"
            + "  AND :NEW.rolltodate IS NOT NULL "
            + "  AND :OLD.rolltodate IS NULL "
            + " THEN "
                + " FOR i IN ("
                    + "SELECT "
                    + " o.startdate"
                    + " , o.enddate"
                    + " , c.docnum"
                    + " , c.signingdate"
                    + " , org.label "
                    + " , a.label address "
                    + "FROM "
                    + " tb_contract_objects o "
                    + " INNER JOIN tb_contracts c ON o.uuid_contract = c.uuid"
                    + " INNER JOIN vc_orgs org    ON c.uuid_org      = org.uuid "
                    + " INNER JOIN vc_build_addresses a ON o.fiashouseguid = a.houseguid "
                    + "WHERE o.is_deleted = 0"
                    + " AND o.is_annuled = 0"
                    + " AND o.fiashouseguid IN (SELECT fiashouseguid FROM tb_contract_objects WHERE is_deleted = 0 AND is_annuled = 0 AND uuid_contract = :NEW.uuid) "
                    + " AND o.enddate   >= :NEW.effectivedate "
                    + " AND o.startdate <= :NEW.rolltodate "
                    + " AND o.uuid_contract <> :NEW.uuid "
                    + ") LOOP"
                + " raise_application_error (-20000, "
                    + "'Дом по адресу ' || i.address || ' обслуживается с ' "
                    + "|| TO_CHAR (i.startdate, 'DD.MM.YYYY')"
                    + "||' по '"
                    + "|| TO_CHAR (i.enddate, 'DD.MM.YYYY')"
                    + "||' по договору управления от '"
                    + "|| TO_CHAR (i.signingdate, 'DD.MM.YYYY')"
                    + "||' №'"
                    + "|| i.docnum"
                    + "||' с '"
                    + "|| i.label"
                    + "|| '. Операция отменена.'); "
                + " END LOOP; "                        
            + " END IF; "

            + "END; "

        );

        trigger ("BEFORE INSERT OR UPDATE", ""                

        + "DECLARE "
        + " cnt INTEGER := 0;"
                
        + "BEGIN "

            + "IF UPDATING "
            + " AND :NEW.id_ctr_status = " + VocGisStatus.i.PENDING_RQ_PLACING.getId ()
            + " AND :OLD.id_ctr_status = " + VocGisStatus.i.MUTATING.getId () + ' '
            + "THEN "
            + "  :NEW.id_ctr_status := " + VocGisStatus.i.PENDING_RQ_EDIT.getId () + "; "
            + "END IF; "

            + "IF UPDATING "
            + " AND :OLD.id_ctr_status NOT IN (10, 11) "
            + " AND :OLD.id_ctr_status = :NEW.id_ctr_status "
            + " AND NVL (:OLD.id_log, '00')        = NVL (:NEW.id_log, '00') "
            + " AND NVL (:OLD.uuid_out_soap, '00') = NVL (:NEW.uuid_out_soap, '00') "
            + " AND NVL (:OLD.contractguid, '00')  = NVL (:NEW.contractguid, '00') "                                        
            + " AND NVL (:OLD.contractversionguid, '00') = NVL (:NEW.contractversionguid, '01') "
            + " AND NVL (:OLD.code_vc_nsi_54, '00') = NVL (:NEW.code_vc_nsi_54, '00') "
            + "THEN "
            + "   raise_application_error (-20000, 'Внесение изменений в договор в настоящее время запрещено. Операция отменена.'); "
            + "END IF; "

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
            + "  AND :OLD.id_ctr_status <> " + VocGisStatus.i.MUTATING.getId ()
            + "  AND :NEW.id_ctr_status = " + VocGisStatus.i.MUTATING.getId ()
            + " THEN BEGIN "
                + "IF :OLD.id_ctr_status NOT IN ("
                   + VocGisStatus.i.FAILED_PLACING.getId () + ","
                   + VocGisStatus.i.FAILED_STATE.getId () + ","
                   + VocGisStatus.i.APPROVED.getId () + ","
                   + VocGisStatus.i.REJECTED.getId () + ","
                   + VocGisStatus.i.FAILED_TERMINATE.getId () + 
                ") THEN "
                + " raise_application_error (-20000, 'Текущий статус договора не допускает внесения изменений. Операция отменена.'); "
                + "END IF; "
                + "IF :OLD.id_ctr_status = " + VocGisStatus.i.FAILED_PLACING.getId () + " THEN :NEW.id_ctr_status := " + VocGisStatus.i.PROJECT.getId () + "; END IF; "
            + "END; END IF; "
                        
            + "IF UPDATING "
            + "  AND :OLD.id_ctr_status < " + VocGisStatus.i.PENDING_RQ_PLACING.getId ()
            + "  AND :NEW.id_ctr_status = " + VocGisStatus.i.PENDING_RQ_PLACING.getId ()
            + " THEN "

                + " IF :NEW.ddt_m_start IS NULL THEN raise_application_error (-20000, 'Не задано начало периода ввода показаний приборов учёта. Операция отменена.'); END IF; "
                + " IF :NEW.ddt_m_end   IS NULL THEN raise_application_error (-20000, 'Не задано окончание периода ввода показаний приборов учёта. Операция отменена.'); END IF; "

                + " IF (:NEW.ddt_m_end_nxt > :NEW.ddt_m_start_nxt) AND (:NEW.ddt_m_end > :NEW.ddt_m_start) THEN raise_application_error (-20000, 'Период сдачи показаний по ИПУ указан некорректно: обнаружено пересечение периодов. Операция отменена.'); END IF; "
                    
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

                + " UPDATE tb_contract_objects SET id_ctr_status = :NEW.id_ctr_status WHERE uuid_contract=:NEW.uuid AND is_deleted=0 AND id_ctr_status NOT IN (" + ANNUL.getId () + "); "
                        
            + "END IF; " // UPDATING and approving

            + "IF :NEW.uuid_out_soap IS NOT NULL AND (:OLD.uuid_out_soap IS NULL OR (:NEW.uuid_out_soap <> :OLD.uuid_out_soap)) THEN "
            + " UPDATE tb_contract_objects__log SET uuid_out_soap = :NEW.uuid_out_soap WHERE uuid IN (SELECT id_log FROM tb_contract_objects WHERE uuid_contract=:NEW.uuid AND is_deleted=0 AND uuid_out_soap IS NULL); "
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
    
    public enum Action {
        
        PLACING     (VocGisStatus.i.PENDING_RP_PLACING,   VocGisStatus.i.FAILED_PLACING),
        APPROVING   (VocGisStatus.i.PENDING_RP_APPROVAL,  VocGisStatus.i.FAILED_STATE),
        REFRESHING  (VocGisStatus.i.PENDING_RP_REFRESH,   VocGisStatus.i.FAILED_STATE),
        RELOADING   (VocGisStatus.i.PENDING_RP_RELOAD,    VocGisStatus.i.FAILED_STATE),
        ROLLOVER    (VocGisStatus.i.PENDING_RP_ROLLOVER,  VocGisStatus.i.FAILED_STATE),
        TERMINATION (VocGisStatus.i.PENDING_RP_TERMINATE, VocGisStatus.i.FAILED_TERMINATE),
        ANNULMENT   (VocGisStatus.i.PENDING_RP_ANNULMENT, VocGisStatus.i.FAILED_ANNULMENT),
        EDITING     (VocGisStatus.i.PENDING_RP_EDIT,      VocGisStatus.i.FAILED_STATE)
        ;
        
        VocGisStatus.i nextStatus;
        VocGisStatus.i failStatus;

        private Action (VocGisStatus.i nextStatus, VocGisStatus.i failStatus) {
            this.nextStatus = nextStatus;
            this.failStatus = failStatus;
        }

        public VocGisStatus.i getNextStatus () {
            return nextStatus;
        }

        public VocGisStatus.i getFailStatus () {
            return failStatus;
        }
        
        public static Action forStatus (VocGisStatus.i status) {
            switch (status) {
                case PENDING_RQ_PLACING:   return PLACING;
                case PENDING_RQ_APPROVAL:  return APPROVING;
                case PENDING_RQ_REFRESH:   return REFRESHING;
                case PENDING_RQ_EDIT:      return EDITING;
                case PENDING_RQ_TERMINATE: return TERMINATION;
                case PENDING_RQ_ANNULMENT: return ANNULMENT;
                case PENDING_RQ_ROLLOVER:  return ROLLOVER;
                case PENDING_RQ_RELOAD:    return RELOADING;
                default: return null;
            }            
        }
        
        public final boolean needsUpload (VocContractDocType.i type) {
            
            if (type == VocContractDocType.i.OTHER) return false;
            
            boolean isTermination = type == VocContractDocType.i.TERMINATION_ATTACHMENT;
            
            switch (this) {
                case TERMINATION:
                    return isTermination;
                case PLACING:    
                case EDITING:    
                    return !isTermination;
                default: 
                    return false;
            }
            
        }
        
        public final boolean needsUpload () {
            for (VocContractDocType.i type: VocContractDocType.i.values ()) if (this.needsUpload (type)) return true;
            return false;            
        }        
                
    };

}