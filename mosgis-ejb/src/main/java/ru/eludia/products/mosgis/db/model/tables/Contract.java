package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import ru.eludia.base.model.def.Num;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocContractDocType;
import ru.eludia.products.mosgis.db.model.voc.VocGisContractType;
import ru.eludia.products.mosgis.db.model.voc.VocGisCustomerType;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import static ru.eludia.products.mosgis.db.model.voc.VocGisStatus.i.ANNUL;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.gosuslugi.dom.schema.integration.house_management.ContractType;
import ru.gosuslugi.dom.schema.integration.house_management.DateDetailsExportType;
import ru.gosuslugi.dom.schema.integration.house_management.DateDetailsType;
import ru.gosuslugi.dom.schema.integration.house_management.DaySelectionExportType;
import ru.gosuslugi.dom.schema.integration.house_management.DeviceMeteringsDaySelectionType;    
import ru.gosuslugi.dom.schema.integration.house_management.ExportCAChResultType;
import ru.gosuslugi.dom.schema.integration.organizations_registry_base.RegOrgType;

public class Contract extends EnTable {

    public enum c implements ColEnum {

        ID_CONTRACT_TYPE         (VocGisContractType.class,                   "Тип договора"),
        UUID_ORG                 (VocOrganization.class,                      "Исполнитель"),

        UUID_ORG_CUSTOMER        (VocOrganization.class,              null,   "Заказчик"),
        ID_CUSTOMER_TYPE         (VocGisCustomerType.class,                   "Тип заказчика"),

        ID_CTR_STATUS            (VocGisStatus.class,          new Num (VocGisStatus.i.PROJECT.getId ()), "Статус договора с точки зрения mosgis"),
        ID_CTR_STATUS_GIS        (VocGisStatus.class,          new Num (VocGisStatus.i.PROJECT.getId ()), "Статус договора с точки зрения ГИС ЖКХ"),
        ID_CTR_STATE_GIS         (VocGisStatus.class,          new Num (VocGisStatus.i.NOT_RUNNING.getId ()), "Состояние договора с точки зрения ГИС ЖКХ"),

        DOCNUM                   (Type.STRING,           255,         "Номер договора"),
        SIGNINGDATE              (Type.DATE,                          "Дата заключения"),
        EFFECTIVEDATE            (Type.DATE,                          "Дата вступления в силу"),
        PLANDATECOMPTETION       (Type.DATE,                          "Планируемая дата окончания"),
        TERMINATE                (Type.DATE,             null,        "Дата расторжения"),
        ROLLTODATE               (Type.DATE,             null,        "Пролонгировать до даты"),

        AUTOMATICROLLOVERONEYEAR (Type.BOOLEAN,          Bool.FALSE,  "Автоматически продлить срок оказания услуг на один год"),
        CODE_VC_NSI_58           (Type.STRING,           20,   null,  "Ссылка на НСИ \"Основание заключения договора\" (реестровый номер 58)"),
        CODE_VC_NSI_54           (Type.STRING,           20,   null,  "Ссылка на НСИ \"Основание расторжения договора\" (реестровый номер 54)"),

        DDT_M_START              (Type.NUMERIC,          2,   null,  "Начало периода ввода показаний ПУ (1..31 — конкретное число, 99 — последнее число)"),
        DDT_M_START_NXT          (Type.BOOLEAN,          Bool.FALSE, "1, если начало периода ввода показаний ПУ в следующем месяце, иначе 0"),
        DDT_M_END                (Type.NUMERIC,          2,   null,  "Окончание периода ввода показаний ПУ (1..31 — конкретное число, 99 — последнее число)"),
        DDT_M_END_NXT            (Type.BOOLEAN,          Bool.FALSE, "1, если окончание периода ввода показаний ПУ в следующем месяце, иначе 0"),

        DDT_D_START              (Type.NUMERIC,          2,   null,  "Срок представления (выставления) платежных документов для внесения платы за жилое помещение и (или) коммунальные услуги (1..30 — конкретное число, 99 — последнее число)"),
        DDT_D_START_NXT          (Type.BOOLEAN,          Bool.FALSE, "1, если срок представления (выставления) платежных документов для внесения платы за жилое помещение и (или) коммунальные услуги в следующем месяце, иначе 0"),

        DDT_I_START              (Type.NUMERIC,          2,   null,  "Срок внесения платы за жилое помещение и (или) коммунальные услуги (1..30 — конкретное число, 99 — последнее число)"),
        DDT_I_START_NXT          (Type.BOOLEAN,          Bool.FALSE, "1, если срок внесения платы за жилое помещение и (или) коммунальные услуги в следующем месяце, иначе 0"),

        CONTRACTBASE             (Type.STRING,  new Virt ("(''||\"CODE_VC_NSI_58\")"),  "Основание заключения договора"),

        UUID_OUT_SOAP            (OutSoap.class,             null,    "Последний запрос на импорт в ГИС ЖКХ"),
        CONTRACTGUID             (Type.UUID,                 null,    "UUID договора в ГИС ЖКХ"),
        CONTRACTVERSIONGUID      (Type.UUID,                 null,    "Идентификатор последней известной версии договора"),

        ID_LOG                   (ContractLog.class,         null,    "Последнее событие редактирования"),

        VERSIONNUMBER            (Type.INTEGER,          10, null,    "Номер версии (по состоянию в ГИС ЖКХ)"),
        REASONOFANNULMENT        (Type.STRING,         1000, null,    "Причина аннулирования"),
        IS_ANNULED               (Type.BOOLEAN,          new Virt ("DECODE(\"REASONOFANNULMENT\",NULL,0,1)"),  "1, если запись аннулирована, иначе 0"),

        ;

        @Override
        public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}

    }        
        
    public Contract () {

        super ("tb_contracts", "Договоры");
        cols   (c.class);        

        key   ("org_docnum", "uuid_org", "docnum");
        key   ("contractguid", "contractguid");
        
        trigger ("BEFORE UPDATE", ""
                
            + "DECLARE" 
            + " PRAGMA AUTONOMOUS_TRANSACTION; "
            + "BEGIN "

            + "IF 1=1"
            + "  AND :NEW.rolltodate IS NOT NULL "
            + "  AND :OLD.rolltodate IS NULL "
            + " THEN BEGIN "
                                
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
                    + "WHERE o.is_to_ignore = 0"
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

                + " FOR i IN ("
                    + "SELECT "
                    + " o.startdate"
                    + " , o.enddate"
                    + " , org.label "
                    + " , a.label address "
                    + "FROM "
                    + " tb_charter_objects o "
                    + " INNER JOIN tb_charters c ON o.uuid_charter = c.uuid"
                    + " INNER JOIN vc_orgs org    ON c.uuid_org      = org.uuid "
                    + " INNER JOIN vc_build_addresses a ON o.fiashouseguid = a.houseguid "
                    + "WHERE o.is_to_ignore = 0"
                    + " AND o.fiashouseguid IN (SELECT fiashouseguid FROM tb_contract_objects WHERE is_deleted = 0 AND is_annuled = 0 AND uuid_contract = :NEW.uuid) "
                    + " AND (o.enddate IS NULL OR o.enddate   >= :NEW.effectivedate )"
                    + " AND o.startdate <= :NEW.rolltodate "
                    + ") LOOP"
                + " raise_application_error (-20000, "
                    + "'Дом по адресу ' || i.address || ' обслуживается с ' "
                    + "|| TO_CHAR (i.startdate, 'DD.MM.YYYY')"
                    + "|| CASE WHEN i.enddate IS NULL THEN NULL ELSE ' по '"
                    + "|| TO_CHAR (i.enddate, 'DD.MM.YYYY') END"
                    + "||' согласно уставу '"
                    + "|| i.label"
                    + "|| '. Операция отменена.'); "
                + " END LOOP; "  
               
            + " END; END IF; "

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
        
        trigger ("AFTER UPDATE", ""
            + "BEGIN"
            + " IF :OLD.is_deleted = 0 AND :NEW.is_deleted = 1 THEN "
            + "  UPDATE tb_contract_objects SET is_deleted = 1 WHERE uuid_contract = :NEW.uuid; "
            + "  INSERT INTO tb_contract_objects__log (uuid_object, action) SELECT uuid, 'delete' FROM tb_contract_objects WHERE uuid_contract = :NEW.uuid; "
            + "  UPDATE tb_contract_services SET is_deleted = 1 WHERE uuid_contract = :NEW.uuid; "
            + "  INSERT INTO tb_contract_services__log (uuid_object, action) SELECT uuid, 'delete' FROM tb_contract_services WHERE uuid_contract = :NEW.uuid; "
            + " END IF;"
            + "END;"
        );        

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
        
    public static void setDateFields (Map<String, Object> h, ExportCAChResultType.Contract contract) {
        h.put ("signingdate",        contract.getSigningDate ());
        h.put ("effectivedate",      contract.getEffectiveDate ());
        h.put ("plandatecomptetion", contract.getPlanDateComptetion ());
    }
    
    private static void setExtraFields (Map<String, Object> h, ExportCAChResultType.Contract.Terminate terminate) {
        if (terminate == null) {
            h.put ("terminate",      null);
            h.put ("code_vc_nsi_54", null);
        }
        else {
            h.put ("terminate",      terminate.getTerminate ());
            h.put ("code_vc_nsi_54", terminate.getReasonRef ().getCode ());
        }
    }
    
    private static byte day (Byte d, Boolean last) {
        return DB.ok (last) ? 99 : d;
    }

    private static void setExtraFields (Map<String, Object> h, String field, DaySelectionExportType p) {
        h.put ("ddt_m_" + field,          day   (p.getDate (), p.isLastDay ()));
        h.put ("ddt_m_" + field + "_nxt", DB.ok (p.isIsNextMonth ()));
    }
    
    private static void setExtraFields (Map<String, Object> h, DateDetailsExportType.PeriodMetering p) {
        setExtraFields (h, "start", p.getStartDate ());
        setExtraFields (h, "end",   p.getEndDate ());
    }

    private static void setExtraFields (Map<String, Object> h, DateDetailsExportType.PaymentDocumentInterval p) {
        h.put ("ddt_d_start",     day   (p.getStartDate (), p.isLastDay ()));
        h.put ("ddt_d_start_nxt", DB.ok (p.isNextMounth ()));
    }

    private static void setExtraFields (Map<String, Object> h, DateDetailsExportType.PaymentInterval p) {
        h.put ("ddt_i_start",     day   (p.getStartDate (), p.isLastDay ()));
        h.put ("ddt_i_start_nxt", DB.ok (p.isNextMounth ()));
    }
        
    private static void setExtraFields (Map<String, Object> h, DateDetailsExportType dateDetails) {
        setExtraFields (h, dateDetails.getPeriodMetering ());
        setExtraFields (h, dateDetails.getPaymentDocumentInterval ());
        setExtraFields (h, dateDetails.getPaymentInterval ());
    }
    
    public static void setExtraFields (Map<String, Object> h, ExportCAChResultType.Contract contract) {
        h.put ("docnum",                   contract.getDocNum ());
        h.put ("automaticrolloveroneyear", DB.ok (contract.isAutomaticRollOverOneYear ()));
        h.put ("code_vc_nsi_58",           contract.getContractBase ().getCode ());
        setExtraFields (h, contract.getTerminate ());
        setExtraFields (h, contract.getDateDetails ());
        /*
        fk    ("uuid_org",                  VocOrganization.class,                      "Исполнитель");
        fk    ("uuid_org_customer",         VocOrganization.class,              null,   "Заказчик");
        fk    ("id_customer_type",          VocGisCustomerType.class,                   "Тип заказчика");
         */ 
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
    
    public static Map<String, Object> toHASH (ExportCAChResultType.Contract ctr) {
        
        VocGisStatus.i status = VocGisStatus.i.forName (ctr.getContractStatus ().value ());
        
        final Map<String, Object> result = DB.HASH (
            c.ID_CTR_STATUS, status,
            c.ID_CTR_STATUS_GIS, status,
            c.CONTRACTGUID, ctr.getContractGUID (),
            c.CONTRACTVERSIONGUID, ctr.getContractVersionGUID (),
            c.AUTOMATICROLLOVERONEYEAR, DB.ok (ctr.isAutomaticRollOverOneYear ()) ? 1 : 0,
            c.CODE_VC_NSI_58, ctr.getContractBase ().getCode (),
            c.DOCNUM, ctr.getDocNum (),
            c.SIGNINGDATE, ctr.getSigningDate (),
            c.EFFECTIVEDATE, ctr.getEffectiveDate (),
            c.PLANDATECOMPTETION, ctr.getPlanDateComptetion (),
            c.ID_CUSTOMER_TYPE, VocGisCustomerType.i.OWNERS.getId ()
        );
        
        set (result, ctr.getMunicipalHousing (), VocGisCustomerType.i.MUNICIPAL_HOUSING);
        set (result, ctr.getBuildingOwner (), VocGisCustomerType.i.BUILDINGOWNER);
        set (result, ctr.getCooperative (), VocGisCustomerType.i.COOPERATIVE);
                
        DateDetailsExportType dd = ctr.getDateDetails ();
        if (dd != null) {
            set (result, dd.getPeriodMetering ());
            set (dd.getPaymentDocumentInterval (), result);
            set (dd.getPaymentInterval (), result);
        }                

        ExportCAChResultType.Contract.Terminate terminate = ctr.getTerminate ();
        if (terminate != null) {
            result.put (c.CODE_VC_NSI_54.lc (), terminate.getReasonRef ().getCode ());
            result.put (c.TERMINATE.lc (), terminate.getTerminate ());
        }

        return result;

    }

    private static void set (DateDetailsExportType.PaymentInterval d, final Map<String, Object> result) {
        if (d == null) return;
        result.put (c.DDT_I_START.lc (), DB.ok (d.isLastDay ()) ? 99 : d.getStartDate ());
        result.put (c.DDT_I_START_NXT.lc (), DB.ok (d.isNextMounth ()) ? 1 : 0);
    }

    private static void set (DateDetailsExportType.PaymentDocumentInterval d, final Map<String, Object> result) {
        if (d == null) return;
        result.put (c.DDT_D_START.lc (), DB.ok (d.isLastDay ()) ? 99 : d.getStartDate ());
        result.put (c.DDT_D_START_NXT.lc (), DB.ok (d.isNextMounth ()) ? 1 : 0);
    }

    private static void set (final Map<String, Object> result, final DateDetailsExportType.PeriodMetering d) {
        if (d == null) return;
        setStart (d.getStartDate (), result);
        setEnd (d.getEndDate (), result);
    }

    private static void setEnd (final DaySelectionExportType d, final Map<String, Object> result) {
        if (d == null) return;
        result.put (c.DDT_M_END.lc (), DB.ok (d.isLastDay ()) ? 99 : d.getDate ());
        result.put (c.DDT_M_END_NXT.lc (), DB.ok (d.isIsNextMonth ()) ? 1 : 0);
    }

    private static void setStart (final DaySelectionExportType d, final Map<String, Object> result) {
        if (d == null) return;
        result.put (c.DDT_M_START.lc (), DB.ok (d.isLastDay ()) ? 99 : d.getDate ());
        result.put (c.DDT_M_START_NXT.lc (), DB.ok (d.isIsNextMonth ()) ? 1 : 0);
    }
    
    private static void set (Map<String, Object> result, RegOrgType orgCustomer, VocGisCustomerType.i type) {
        if (orgCustomer == null) return;
        result.put (c.UUID_ORG_CUSTOMER.lc (), orgCustomer.getOrgRootEntityGUID ());
        result.put (c.ID_CUSTOMER_TYPE.lc (), type.getId ());
    }

}