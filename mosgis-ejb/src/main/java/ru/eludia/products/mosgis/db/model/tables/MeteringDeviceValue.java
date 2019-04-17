package ru.eludia.products.mosgis.db.model.tables;

import java.util.HashSet;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.incoming.xl.lines.InXlMeteringValues;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocMeteringDeviceValueType;

public class MeteringDeviceValue extends EnTable {

    public static final String TABLE_NAME = "tb_meter_values";

    public enum c implements EnColEnum {

        UUID_XL                (InXlMeteringValues.class,        null,          "Источник импорта"),
        UUID_METER             (MeteringDevice.class,                           "Прибор учёта"),
        ID_TYPE                (VocMeteringDeviceValueType.class,               "Тип показания"),        
        CODE_VC_NSI_2          (Type.STRING,  20,                               "Коммунальный ресурс (НСИ 2)"),

        DATEVALUE              (Type.DATE,                                      "Дата снятия показания"),
        DT_PERIOD              (Type.DATE, null,                                "Период (месяц, год), к которому относятся показания"),

        METERINGVALUET1        (Type.NUMERIC, 22, 7, null,                      "Объем по тарифу T1"),
        METERINGVALUET2        (Type.NUMERIC, 22, 7, null,                      "Объем по тарифу T2"),
        METERINGVALUET3        (Type.NUMERIC, 22, 7, null,                      "Объем по тарифу T3"),
        
        METERINGVALUE          (Type.NUMERIC, 22, 7, new Virt ("0+METERINGVALUET1"), "Объем"),

        ID_CTR_STATUS          (VocGisStatus.class,    VocGisStatus.DEFAULT,    "Статус с точки зрения mosgis"),
        ID_CTR_STATUS_GIS      (VocGisStatus.class,    VocGisStatus.DEFAULT,    "Статус с точки зрения ГИС ЖКХ"),

        ID_LOG                 (MeteringDeviceValueLog.class,                   "Последнее событие редактирования"),
        GIS_GUID               (Type.UUID,           null,                      "уникальный номер на стороне ГИС ЖКХ"),

        ;

        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}        
        @Override
        public boolean isLoggable () {
            switch (this) {
                case ID_LOG:
                case UUID_METER:
                case ID_TYPE:
                case CODE_VC_NSI_2:
                    return false;
                default:
                    return true;
            }
        }        

        static public boolean isExists(String name){
            for(c item: c.values()){
               if (item.name().equals(name)) return true;
            }
            return false;
        }

    }

    public MeteringDeviceValue () {
        
        super  (TABLE_NAME, "Показания приборов учёта");
        cols   (c.class);
        key    (c.UUID_XL);
        key    ("uuid_meter", "uuid_meter");

        trigger ("BEFORE INSERT OR UPDATE", ""
                
            + "DECLARE" 
            + " PRAGMA AUTONOMOUS_TRANSACTION; "
            + " cnt NUMBER; "
            + " d NUMBER (2); "
            + " ddt_m_start NUMBER (2); "
            + " ddt_m_start_nxt NUMBER (1); "
            + " ddt_m_end NUMBER (2); "
            + " ddt_m_end_nxt NUMBER (1); "
            + "BEGIN "
                
            + "IF :NEW.is_deleted = 0 AND :NEW.id_type = " + VocMeteringDeviceValueType.i.BASE + " THEN "
            + " FOR i IN ("
                + "SELECT "
                + " o.uuid "
                + "FROM "
                + " tb_meter_values o "
                + "WHERE o.is_deleted = 0 "
                + " AND o.uuid <> NVL (:NEW.uuid, '00') "
                + " AND o.uuid_meter = :NEW.uuid_meter "
                + " AND o.id_type = :NEW.id_type "
                + " AND o.code_vc_nsi_2 = :NEW.code_vc_nsi_2 "
                + ") LOOP"
            + " raise_application_error (-20000, 'Базовые показания для данного прибора учёта уже введены. Операция отменена.'); "
            + " END LOOP; "
            + " :NEW.DT_PERIOD := NULL; "
            + "END IF; "

            + "IF :NEW.is_deleted = 0 AND :NEW.id_type <> " + VocMeteringDeviceValueType.i.BASE + " THEN "
            + " BEGIN "
            + "  SELECT ddt_m_start,ddt_m_start_nxt,ddt_m_end,ddt_m_end_nxt INTO ddt_m_start,ddt_m_start_nxt,ddt_m_end,ddt_m_end_nxt FROM " + ActualCaChObject.TABLE_NAME + " WHERE fiashouseguid IN (SELECT fiashouseguid FROM " + MeteringDevice.TABLE_NAME + " WHERE uuid=:NEW.uuid_meter);"
            + "  EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Для данного дома не найден договор управления или устав с заданным периодом ввода показаний приборов учёта.');"
            + " END; "
                    
            + " d := EXTRACT (DAY FROM :NEW.DATEVALUE); "
            + " IF ddt_m_start_nxt=ddt_m_end_nxt AND (d<ddt_m_start  OR d>ddt_m_end) THEN raise_application_error (-20000, 'Указанная дата лежит вне периода приёма показаний'); END IF;"
            + " IF ddt_m_start_nxt<ddt_m_end_nxt AND (d<ddt_m_start AND d>ddt_m_end) THEN raise_application_error (-20000, 'Указанная дата лежит вне периода приёма показаний'); END IF;"

            + " :NEW.DT_PERIOD := TRUNC (:NEW.DATEVALUE, 'MM'); "
            + " IF d < ddt_m_start THEN :NEW.DT_PERIOD := ADD_MONTHS (:NEW.DT_PERIOD, -1); END IF;"
            + " IF ddt_m_start_nxt = 1 THEN :NEW.DT_PERIOD := ADD_MONTHS (:NEW.DT_PERIOD, 1); END IF;"
                    
            + " SELECT COUNT(*) INTO cnt FROM " + TABLE_NAME + " WHERE is_deleted=0 AND UUID_METER=:NEW.UUID_METER AND ID_TYPE=:NEW.ID_TYPE AND CODE_VC_NSI_2=:NEW.CODE_VC_NSI_2 AND DT_PERIOD=:NEW.DT_PERIOD;"
            + " IF cnt>0 THEN raise_application_error (-20000, 'Показания на данный период уже внесены'); END IF;"

            + "END IF; "

        + "END;");

    }
    
    public enum Action {
        
        PLACING     (VocGisStatus.i.PENDING_RP_PLACING,   VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_PLACING),
        ;
        
        VocGisStatus.i nextStatus;
        VocGisStatus.i okStatus;
        VocGisStatus.i failStatus;

        private Action (VocGisStatus.i nextStatus, VocGisStatus.i okStatus, VocGisStatus.i failStatus) {
            this.nextStatus = nextStatus;
            this.okStatus = okStatus;
            this.failStatus = failStatus;
        }

        public VocGisStatus.i getNextStatus () {
            return nextStatus;
        }

        public VocGisStatus.i getFailStatus () {
            return failStatus;
        }

        public VocGisStatus.i getOkStatus () {
            return okStatus;
        }
        
        public static Action forStatus (VocGisStatus.i status) {
            switch (status) {
                case PENDING_RQ_PLACING:   return PLACING;
                case PENDING_RP_PLACING:   return PLACING;
                default: return null;
            }            
        }
                                
    };    

}