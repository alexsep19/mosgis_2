package ru.eludia.products.mosgis.db.model.incoming.xl.lines;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.apache.poi.xssf.usermodel.XSSFRow;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.db.model.tables.MeteringDevice;
import ru.eludia.products.mosgis.db.model.tables.MeteringDeviceValue;
import ru.eludia.products.mosgis.db.model.tables.NonResidentialPremise;
import ru.eludia.products.mosgis.db.model.tables.Premise;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocMeteringDeviceInstallationPlace;
import ru.eludia.products.mosgis.db.model.voc.VocMeteringDeviceType;
import ru.eludia.products.mosgis.db.model.voc.VocMeteringDeviceValueType;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi16;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi2;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi27;
import static ru.eludia.products.mosgis.jms.xl.ParseMeteringValuesMDB.NULL_DATE;
import ru.eludia.products.mosgis.jms.xl.base.XLException;

public class InXlMeteringValues extends EnTable {

    public static final String TABLE_NAME = "in_xl_meter_values";
//целевая MeteringDeviceValue tb_meter_values
    
    public enum c implements ColEnum {

        UUID_XL                 (InXlFile.class,          null,                 "Файл импорта"),
        ORD                     (Type.NUMERIC, 5,                               "Номер строки"),
        ERR                     (Type.STRING,  null,                            "Ошибка"),
        
        DEVICE_NUMBER           (Type.STRING, 20,    null,                      "Номер устройства") ,
        UUID_METER              (MeteringDevice.class,                          "Прибор учёта"),
        ID_TYPE                 (VocMeteringDeviceValueType.class,              "Тип показания"),
        CODE_VC_NSI_2           (Type.STRING,  20,                              "Коммунальный ресурс (НСИ 2)"),
       
        METERINGVALUET1        (Type.NUMERIC, 22, 7, null,                      "Объем по тарифу T1"),
        METERINGVALUET2        (Type.NUMERIC, 22, 7, null,                      "Объем по тарифу T2"),
        METERINGVALUET3        (Type.NUMERIC, 22, 7, null,                      "Объем по тарифу T3"),

        DATEVALUE              (Type.DATE,                                      "Дата снятия показания"),
        DT_PERIOD              (Type.DATE, null,                                "Период (месяц, год), к которому относятся показания"),
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        boolean isToCopy () {
            switch (this) {
                case ORD:
                case ERR:
                    return false;                    
                default:                    
                    return MeteringDeviceValue.c.isExists(this.name());
            }
        }
    }
    
    public static Map<String, Object> toHash (UUID uuid, int ord, XSSFRow row, Date dateValue, Date datePeriod) {
        
        Map<String, Object> r = DB.HASH (
            EnTable.c.IS_DELETED, 1,
            c.UUID_XL, uuid,
            c.ORD, ord    
        );
        
        try {
            setFields (r, row, dateValue, datePeriod);
        }         
        catch (XLException ex) {
            r.put (c.ERR.lc (), ex.getMessage ());
        }
        
        return r;
        
    }
    
    private static Date processDate(Date readDate, Date dateValue, Date datePeriod)  throws XLException{
        //показания во всех строках д.б. за одну дату;
        if (dateValue.equals(NULL_DATE)) {
            dateValue.setTime(readDate.getTime());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(readDate);
            calendar.set(Calendar.DATE, 1);
            datePeriod.setTime(calendar.getTimeInMillis());
        }
        else if (dateValue.compareTo(readDate) != 0) throw new XLException("Показания во всех строках д.б.за одну дату");
        return readDate;
    }
    
    private static void setFields (Map<String, Object> r, XSSFRow row, Date dateValue, Date datePeriod) throws XLException {
        //UUID_METER устанавливается в триггере
        r.put (c.DEVICE_NUMBER.lc (),       toString (row, 1, "Не указан номер прибора"));  
        r.put (c.ID_TYPE.lc (), VocMeteringDeviceValueType.i.CURRENT);
        r.put (c.CODE_VC_NSI_2.lc (),    Nsi2.i.forLabel (toString (row, 2, "Не указан коммунальный ресурс")).getId ());
        r.put (c.METERINGVALUET1.lc (),  toNumeric (row, 3, "Не указано значение показания (Т1)"));
        r.put (c.METERINGVALUET2.lc (),  toNumeric (row, 4 ));
        r.put (c.METERINGVALUET3.lc (),  toNumeric (row, 5 ));
        r.put (c.DATEVALUE.lc (),        processDate((Date) toDate(row, 6, "Не указана дата снятия показания"), dateValue, datePeriod));
        r.put (c.DT_PERIOD.lc (),        datePeriod);
     }

    public InXlMeteringValues () {

        super (TABLE_NAME, "Строки импорта показаний приборов учёта");

        cols  (c.class);

        key ("uuid_xl", c.UUID_XL);

        trigger ("BEFORE INSERT", ""
            + "DECLARE "
            + " cnt NUMBER; "
            + " DEVICE_ORG_UUID RAW(16); "
            + " ORG_UUID RAW(16); "
            + " TARIFFCOUNT NUMBER; "    
            + "BEGIN "
                
            + "  BEGIN "                
            + "    SELECT uuid, UUID_ORG, TARIFFCOUNT INTO :NEW.UUID_METER, DEVICE_ORG_UUID, TARIFFCOUNT FROM tb_meters WHERE METERINGDEVICENUMBER = :NEW.DEVICE_NUMBER; "
            + "    EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Устройство '||:NEW.DEVICE_NUMBER||' не найдено');"
            + "  END; "                               
            
            + "  BEGIN "    
            + "    SELECT UUID_ORG INTO ORG_UUID FROM IN_XL_FILES WHERE UUID_ORG = DEVICE_ORG_UUID AND UUID = :NEW.UUID_XL; "    
            + "    EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Устройство '||:NEW.DEVICE_NUMBER||' принадлежит другой организации: UUID_ORG = '||ORG_UUID||' DEVICE_ORG_UUID = '||DEVICE_ORG_UUID); "
            + "  END; "     
              //для каждого устройства: периода показаний в базе не должно быть
            + "  SELECT count(*) INTO cnt FROM tb_meter_values WHERE is_deleted=0 AND UUID_METER = :NEW.UUID_METER AND ID_TYPE=:NEW.ID_TYPE AND CODE_VC_NSI_2=:NEW.CODE_VC_NSI_2 AND DT_PERIOD = :NEW.DT_PERIOD; "  
            + "  IF cnt > 0 THEN raise_application_error (-20000, 'Период показаний '||to_char(:NEW.DT_PERIOD,'dd.mm.yyyy') ||' для устройства '||:NEW.DEVICE_NUMBER||' уже есть'); END IF; "

            + "  IF TARIFFCOUNT = 2 AND (:NEW.METERINGVALUET2 is null OR :NEW.METERINGVALUET3 is null) THEN raise_application_error (-20000, 'Не указано значение показания (Т2)'); END IF; "    
            + "  IF TARIFFCOUNT = 3 AND :NEW.METERINGVALUET3 is null THEN raise_application_error (-20000, 'Не указано значение показания (Т3)'); END IF; "    
                
            + " EXCEPTION WHEN OTHERS THEN "
            + " :NEW.err := 'Trigger error '||REPLACE(SUBSTR(SQLERRM, 1, 1000), 'ORA-20000: ', ''); "

            + "END;"
        );
        
        StringBuilder sb = new StringBuilder ();
        StringBuilder nsb = new StringBuilder ();
        
        for (c c: c.values ()) if (c.isToCopy ()) {
            sb.append (',');
            sb.append (c.lc ());
            nsb.append (",:NEW.");
            nsb.append (c.lc ());
        }        

        trigger ("BEFORE UPDATE", ""

            + "BEGIN "
            + " IF :NEW.err IS NOT NULL THEN :NEW.is_deleted := 1; END IF; "
            + "END; "
        );        
        
        trigger ("AFTER UPDATE", ""

            + "DECLARE" 
            + " PRAGMA AUTONOMOUS_TRANSACTION; "
            + "BEGIN "

            + " IF NOT (:OLD.is_deleted = 1 AND :NEW.is_deleted = 0) THEN RETURN; END IF; "

            + " INSERT INTO " + MeteringDeviceValue.TABLE_NAME + " (uuid,is_deleted" + sb + ") VALUES (:NEW.uuid,1" + nsb + "); "
            + " COMMIT; "

            + "END; "

        );        

    }

}