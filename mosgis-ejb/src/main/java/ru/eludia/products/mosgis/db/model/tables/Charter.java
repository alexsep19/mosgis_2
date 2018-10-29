package ru.eludia.products.mosgis.db.model.tables;

import java.util.Collection;
import java.util.Map;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Num;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.voc.VocCharterObjectReason;
import ru.eludia.products.mosgis.db.model.voc.VocContractDocType;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.gosuslugi.dom.schema.integration.house_management.CharterDateDetailsExportType;
import ru.gosuslugi.dom.schema.integration.house_management.CharterDateDetailsType;
import ru.gosuslugi.dom.schema.integration.house_management.CharterType;
import ru.gosuslugi.dom.schema.integration.house_management.DaySelectionExportType;
import ru.gosuslugi.dom.schema.integration.house_management.DeviceMeteringsDaySelectionType;
import ru.gosuslugi.dom.schema.integration.house_management.ExportCAChResultType;

public class Charter extends Table {
/*    
    public enum c implements ColEnum {
        
        ;
        
        private Col col;        

        private c (Object... p) {
            col = new Col (p);
        }

        @Override
        public Col getCol () {
            return col;
        }
        
    }
*/

    public Charter () {

        super ("tb_charters", "Уставы");

        pk    ("uuid",                      Type.UUID,             NEW_UUID,            "Ключ");
        col   ("is_deleted",                Type.BOOLEAN,          Bool.FALSE,          "1, если запись удалена; иначе 0");
        fk    ("uuid_org",                  VocOrganization.class,                      "Организация");
        
        fk    ("id_ctr_status",             VocGisStatus.class,          new Num (VocGisStatus.i.PROJECT.getId ()), "Статус устава с точки зрения mosgis");
        fk    ("id_ctr_status_gis",         VocGisStatus.class,          new Num (VocGisStatus.i.PROJECT.getId ()), "Статус устава с точки зрения ГИС ЖКХ");
        fk    ("id_ctr_state_gis",          VocGisStatus.class,          new Num (VocGisStatus.i.NOT_RUNNING.getId ()), "Состояние устава с точки зрения ГИС ЖКХ");
        
        col   ("date_",                     Type.DATE,             null,        "Дата регистрации TCН/ТСЖ/кооператива (Организации Поставщика данных)");
        col   ("automaticrolloveroneyear",  Type.BOOLEAN,          Bool.FALSE,  "Автоматически продлить срок оказания услуг на один год");
        col   ("nocharterapproveprotocol",  Type.BOOLEAN,          Bool.FALSE,  "Протокол, содержащий решение об утверждении устава, отсутствует");              
        
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

        fk    ("uuid_out_soap",             OutSoap.class,             null,    "Последний запрос на импорт в ГИС ЖКХ");
        col   ("charterguid",               Type.UUID,                 null,    "UUID устава в ГИС ЖКХ");
        col   ("charterversionguid",        Type.UUID,                 null,    "Идентификатор последней известной версии устава");

        fk    ("id_log",                    CharterLog.class,          null,    "Последнее событие редактирования");

        col   ("versionnumber",             Type.INTEGER,          10, null,    "Номер версии (по состоянию в ГИС ЖКХ)");

        col   ("terminate",                 Type.DATE,                 null,    "Дата прекращения");
        col   ("reason",                    Type.STRING,         255,  null,    "Причина прекращения");
        
        col   ("rolltodate",                Type.DATE,             null,        "Пролонгировать до даты");
        
        col   ("reasonofannulment",         Type.STRING,         1000, null,    "Причина аннулирования");
        col   ("is_annuled",                Type.BOOLEAN,          new Virt ("DECODE(\"REASONOFANNULMENT\",NULL,0,1)"),  "1, если запись аннулирована; иначе 0");

        key    ("charterguid", "charterguid");
        unique ("uuid_org", "uuid_org");
        
        trigger ("BEFORE INSERT OR UPDATE", ""                
                
        + "DECLARE "
        + " cnt INTEGER := 0;"
                
        + "BEGIN "

            + "IF UPDATING "                
            + " AND :NEW.id_ctr_status = " + VocGisStatus.i.PENDING_RQ_PLACING.getId ()
            + " AND :OLD.id_ctr_status = " + VocGisStatus.i.MUTATING.getId () + ' '
            + " AND :NEW.charterguid IS NOT NULL "
            + "THEN "
            + "  :NEW.id_ctr_status := " + VocGisStatus.i.PENDING_RQ_EDIT.getId () + "; "
            + "END IF; "
                
            + "IF UPDATING "
            + "  AND :OLD.id_ctr_status < " + VocGisStatus.i.PENDING_RQ_PLACING.getId ()
            + "  AND :NEW.id_ctr_status = " + VocGisStatus.i.PENDING_RQ_PLACING.getId ()
            + " THEN "

                + " IF :NEW.ddt_m_start IS NULL THEN raise_application_error (-20000, 'Не задано начало периода ввода показаний приборов учёта. Операция отменена.'); END IF; "
                + " IF :NEW.ddt_m_end   IS NULL THEN raise_application_error (-20000, 'Не задано окончание периода ввода показаний приборов учёта. Операция отменена.'); END IF; "

                + " IF (:NEW.ddt_m_end_nxt > :NEW.ddt_m_start_nxt) AND (:NEW.ddt_m_end > :NEW.ddt_m_start) THEN raise_application_error (-20000, 'Период сдачи показаний по ИПУ указан некорректно: обнаружено пересечение периодов. Операция отменена.'); END IF; "
                    
                + " IF :NEW.ddt_d_start IS NULL THEN raise_application_error (-20000, 'Не задан срок выставления платежных документов. Операция отменена.'); END IF; "
                + " IF :NEW.ddt_i_start IS NULL THEN raise_application_error (-20000, 'Не задан срок внесения платы за помещение / услуги. Операция отменена.'); END IF; "
                    
                    
                + " SELECT COUNT(*) INTO cnt FROM tb_charter_objects WHERE uuid_charter = :NEW.uuid AND is_deleted = 0"
                + " ; IF cnt=0 THEN "
                + "   raise_application_error (-20000, 'Не заявлен ни один объект управления. Операция отменена.'); "
                + " END IF; "

                + " SELECT COUNT(*) INTO cnt FROM tb_charter_files WHERE uuid_charter = :NEW.uuid AND id_status = 1 AND id_type = " + VocContractDocType.i.CHARTER.getId ()
                + " ; IF cnt=0 THEN "
                + "   raise_application_error (-20000, 'На в вкладке \"документы\" не приложен файл устава. Операция отменена.'); "
                + " END IF; "  
                
                + " FOR i IN ("
                + "  SELECT "
                + "    a.label "
                + "  FROM "
                + "    tb_charter_objects o"
                + "    LEFT JOIN tb_charter_files f ON (f.uuid_charter_object=o.uuid AND f.id_status = 1 AND f.id_type=" + VocContractDocType.i.PROTOCOL_MEETING_OWNERS.getId () + ")"
                + "    LEFT JOIN vc_build_addresses a ON (o.fiashouseguid = a.houseguid)"
                + "  WHERE 1=1"
                + "    AND o.uuid_charter = :NEW.uuid"
                + "    AND o.id_reason = " + VocCharterObjectReason.i.PROTOCOL.getId ()
                + "    AND o.is_deleted = 0"
                + "    AND f.uuid IS NULL"
                + "  ) LOOP"
                + "   raise_application_error (-20000, 'Для объекта по адресу ' || i.label || ' не приложен протокол собрания собственников. Операция отменена.'); "
                + " END LOOP; "                                      

            + "END IF; " // UPDATING and approving
                        
        + "END;");
        

    }
    
    private static DeviceMeteringsDaySelectionType deviceMeteringsDaySelectionType (Map<String, Object> r, String key) {        
        final DeviceMeteringsDaySelectionType result = new DeviceMeteringsDaySelectionType ();
        String s = r.get (key).toString ();
        result.setLastDay ("99".equals (s) ? true: null);
        if (result.isLastDay () == null) result.setDate (Byte.parseByte (s));
        result.setIsNextMonth ("1".equals (r.get (key + "_nxt").toString ()));
        return result;
    }
    
    private static CharterDateDetailsType.PaymentDocumentInterval paymentDocumentInterval (Map<String, Object> r, String key) {
        final CharterDateDetailsType.PaymentDocumentInterval result = new CharterDateDetailsType.PaymentDocumentInterval ();
        String s = r.get (key).toString ();
        result.setLastDay ("99".equals (s) ? true: null);
        if (result.isLastDay () == null) result.setStartDate (Byte.parseByte (s));
        if ("1".equals (r.get (key + "_nxt").toString ())) result.setNextMounth (true); else result.setCurrentMounth (true);
        return result;
    }
    
    private static CharterDateDetailsType.PaymentInterval paymentInterval (Map<String, Object> r, String key) {
        final CharterDateDetailsType.PaymentInterval result = new CharterDateDetailsType.PaymentInterval ();
        String s = r.get (key).toString ();
        result.setLastDay ("99".equals (s) ? true: null);
        if (result.isLastDay () == null) result.setStartDate (Byte.parseByte (s));
        if ("1".equals (r.get (key + "_nxt").toString ())) result.setNextMounth (true); else result.setCurrentMounth (true);
        return result;
    }    
    
    public static void fillCharter (CharterType c, Map<String, Object> r) {
        
        if (Boolean.FALSE.equals (c.isNoCharterApproveProtocol ())) c.setNoCharterApproveProtocol (null);
        if (Boolean.FALSE.equals (c.isAutomaticRollOverOneYear ())) c.setAutomaticRollOverOneYear (null);
       
        final CharterDateDetailsType dd = new CharterDateDetailsType ();
        
        final CharterDateDetailsType.PeriodMetering pm = new CharterDateDetailsType.PeriodMetering ();
        
        pm.setStartDate (deviceMeteringsDaySelectionType       (r, "ddt_m_start"));
        pm.setEndDate   (deviceMeteringsDaySelectionType       (r, "ddt_m_end"));       
        dd.setPeriodMetering (pm);
        dd.setPaymentDocumentInterval (paymentDocumentInterval (r, "ddt_d_start"));
        dd.setPaymentInterval         (paymentInterval         (r, "ddt_i_start"));
                
        c.setDateDetails (dd);
        
        for (Map<String, Object> file: (Collection<Map<String, Object>>) r.get ("files")) CharterFile.add (c, file);
        
        if (c.getMeetingProtocol ().getProtocolMeetingOwners ().isEmpty ()) c.setNoCharterApproveProtocol (true);
                
    }
    
    public static void setExtraFields (Map<String, Object> h, ExportCAChResultType.Charter charter) {
        h.put ("automaticrolloveroneyear", DB.ok (charter.isAutomaticRollOverOneYear ()));
        setExtraFields (h, charter.getTerminate ());
        setExtraFields (h, charter.getDateDetails ());
    }   
    
    private static void setExtraFields (Map<String, Object> h, CharterDateDetailsExportType dateDetails) {
        setExtraFields (h, dateDetails.getPeriodMetering ());
        setExtraFields (h, dateDetails.getPaymentDocumentInterval ());
        setExtraFields (h, dateDetails.getPaymentInterval ());
    }    
    
    private static void setExtraFields (Map<String, Object> h, CharterDateDetailsExportType.PeriodMetering p) {
        setExtraFields (h, "start", p.getStartDate ());
        setExtraFields (h, "end",   p.getEndDate ());
    }
    
    private static byte day (Byte d, Boolean last) {
        return DB.ok (last) ? 99 : d;
    }

    private static void setExtraFields (Map<String, Object> h, String field, DaySelectionExportType p) {
        h.put ("ddt_m_" + field,          day   (p.getDate (), p.isLastDay ()));
        h.put ("ddt_m_" + field + "_nxt", DB.ok (p.isIsNextMonth ()));
    }

    private static void setExtraFields (Map<String, Object> h, CharterDateDetailsExportType.PaymentDocumentInterval p) {
        h.put ("ddt_d_start",     day   (p.getStartDate (), p.isLastDay ()));
        h.put ("ddt_d_start_nxt", DB.ok (p.isNextMounth ()));
    }

    private static void setExtraFields (Map<String, Object> h, CharterDateDetailsExportType.PaymentInterval p) {
        h.put ("ddt_i_start",     day   (p.getStartDate (), p.isLastDay ()));
        h.put ("ddt_i_start_nxt", DB.ok (p.isNextMounth ()));
    }    
    
    private static void setExtraFields (Map<String, Object> h, ExportCAChResultType.Charter.Terminate terminate) {
        
        if (terminate == null) {
            h.put ("terminate", null);
            h.put ("reason",    null);
        }
        else {
            h.put ("terminate", terminate.getTerminate ());
            h.put ("reason",    terminate.getReason ());
        }
        
    }

    public enum Action {
        
        PLACING     (VocGisStatus.i.PENDING_RP_PLACING,   VocGisStatus.i.FAILED_PLACING),
        EDITING     (VocGisStatus.i.PENDING_RP_EDIT,      VocGisStatus.i.FAILED_STATE),
        ANNULMENT   (VocGisStatus.i.PENDING_RP_ANNULMENT, VocGisStatus.i.FAILED_ANNULMENT),
        TERMINATION (VocGisStatus.i.PENDING_RP_TERMINATE, VocGisStatus.i.FAILED_TERMINATE),
        ROLLOVER    (VocGisStatus.i.PENDING_RP_ROLLOVER,  VocGisStatus.i.FAILED_STATE),
        RELOADING   (VocGisStatus.i.PENDING_RP_RELOAD,    VocGisStatus.i.FAILED_STATE)
//        APPROVING   (VocGisStatus.i.PENDING_RP_APPROVAL,  VocGisStatus.i.FAILED_STATE),
//        REFRESHING  (VocGisStatus.i.PENDING_RP_REFRESH,   VocGisStatus.i.FAILED_STATE),
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
                case PENDING_RQ_EDIT:      return EDITING;
                case PENDING_RQ_TERMINATE: return TERMINATION;
                case PENDING_RQ_ANNULMENT: return ANNULMENT;
                case PENDING_RQ_RELOAD:    return RELOADING;
                case PENDING_RQ_ROLLOVER:  return ROLLOVER;
//                case PENDING_RQ_APPROVAL:  return APPROVING;
//                case PENDING_RQ_REFRESH:   return REFRESHING;
                default: return null;
            }            
        }
        
        public final boolean needsUpload (VocContractDocType.i type) {            
            if (type == VocContractDocType.i.OTHER) return false; // just in case            
            return true;            
        }
        
        public final boolean needsUpload () {
            for (VocContractDocType.i type: VocContractDocType.i.values ()) if (this.needsUpload (type)) return true;
            return false;            
        }        
                
    };
            
}