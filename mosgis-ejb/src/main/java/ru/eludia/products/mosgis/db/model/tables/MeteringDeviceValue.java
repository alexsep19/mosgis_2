package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;

public class MeteringDeviceValue extends EnTable {

    public enum c implements EnColEnum {

        UUID_METER             (MeteringDevice.class,                           "Прибор учёта"),

        METERINGVALUET1        (Type.NUMERIC, 22, 7, null,                      "Объем по тарифу T1"),
        METERINGVALUET2        (Type.NUMERIC, 22, 7, null,                      "Объем по тарифу T2"),
        METERINGVALUET3        (Type.NUMERIC, 22, 7, null,                      "Объем по тарифу T3"),

        DATEVALUE              (Type.DATE,                                      "Дата снятия показания"),
        CODE_VC_NSI_2          (Type.STRING,  20,                               "Коммунальный ресурс (НСИ 2)"),

        ID_CTR_STATUS          (VocGisStatus.class,    VocGisStatus.DEFAULT,    "Статус с точки зрения mosgis"),
        ID_CTR_STATUS_GIS      (VocGisStatus.class,    VocGisStatus.DEFAULT,    "Статус с точки зрения ГИС ЖКХ"),
        
        ;

        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}        
        @Override
        public boolean isLoggable () {
            switch (this) {
//                case ID_LOG:
//                    return false;
                default:
                    return true;
            }
        }        

    }

    public MeteringDeviceValue () {
        super  ("tb_meter_values", "Показания приборов учёта");
        cols   (c.class);
//        key    ("fiashouseguid", "fiashouseguid");
/*        
        trigger ("BEFORE INSERT OR UPDATE", "BEGIN "

            + "SELECT CODE_VC_NSI_27 INTO :NEW.CODE_VC_NSI_27 FROM vc_meter_types WHERE id = :NEW.ID_TYPE; "
                    
        + "END;");
*/        
    }

}