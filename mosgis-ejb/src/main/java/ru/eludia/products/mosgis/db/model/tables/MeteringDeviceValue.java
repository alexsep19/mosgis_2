package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocMeteringDeviceValueType;

public class MeteringDeviceValue extends EnTable {

    public enum c implements EnColEnum {

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

    }

    public MeteringDeviceValue () {
        
        super  ("tb_meter_values", "Показания приборов учёта");
        cols   (c.class);
        key    ("uuid_meter", "uuid_meter");

        trigger ("BEFORE INSERT OR UPDATE", ""
                
            + "DECLARE" 
            + " PRAGMA AUTONOMOUS_TRANSACTION; "
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
            + " :NEW.DT_PERIOD := TRUNC (:NEW.DATEVALUE, 'MM'); "
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