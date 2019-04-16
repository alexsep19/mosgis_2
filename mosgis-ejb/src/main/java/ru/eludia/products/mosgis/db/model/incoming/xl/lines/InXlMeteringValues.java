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
            + "  SELECT count(*) INTO cnt FROM tb_meter_values WHERE UUID_METER = :NEW.UUID_METER AND DT_PERIOD = :NEW.DT_PERIOD; "  
            + "  IF cnt > 0 THEN raise_application_error (-20000, 'Период показаний '||to_char('dd.mm.yyyy',:NEW.DT_PERIOD) ||' для устройства '||:NEW.DEVICE_NUMBER||' уже есть'); END IF; "

            + "  IF TARIFFCOUNT = 2 AND (:NEW.METERINGVALUET2 is null OR :NEW.METERINGVALUET3 is null) THEN raise_application_error (-20000, 'Не указано значение показания (Т2)'); END IF; "    
            + "  IF TARIFFCOUNT = 3 AND :NEW.METERINGVALUET3 is null THEN raise_application_error (-20000, 'Не указано значение показания (Т3)'); END IF; "    
                
            + " EXCEPTION WHEN OTHERS THEN "
            + " :NEW.err := REPLACE(SUBSTR(SQLERRM, 1, 1000), 'ORA-20000: ', ''); "

            + "END;"
        );

//        trigger ("BEFORE INSERT", ""
//                
//            + "DECLARE "
//            + " cnt NUMBER; "
//            + " val1 NUMBER; "
//            + " val2 NUMBER; "
//            + " UUID1 RAW(16); "
//            + " UUID2 RAW(16); "
//            + "BEGIN "
//                
//            + " SELECT uuid_org INTO :NEW.uuid_org FROM in_xl_files WHERE uuid=:NEW.uuid_xl; "
//
//            + " IF :NEW.err IS NOT NULL THEN RETURN; END IF; "
//                
//            + " SELECT COUNT(*), MIN(fiashouseguid) INTO cnt, :NEW.fiashouseguid FROM vc_unom WHERE id_status=1 AND unom=:NEW.unom; "
//            + " IF cnt=0 THEN raise_application_error (-20000, 'Неизвестное значение UNOM: ' || :NEW.unom); END IF; "
//            + " IF cnt>1 THEN raise_application_error (-20000, 'UNOM ' || :NEW.unom || ' соответствует не одна запись ФИАС, а ' || cnt); END IF; "
//
//            + " IF :NEW.verif_int IS NOT NULL THEN BEGIN "
//            + "  SELECT id INTO :NEW.code_vc_nsi_16 FROM vw_nsi_16 WHERE value=:NEW.verif_int; "
//            + "  EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Значение межповерочного интервала не найдено в справочнике НСИ 16');"
//            + " END; END IF; "                               
//
//            + " IF :NEW.ID_TYPE != " + VocMeteringDeviceType.i.COLLECTIVE.getId() +" THEN BEGIN "
//            + "  select r.premisesnum, r.UUID, n.premisesnum, n.UUID INTO val1, UUID1, val2, UUID2 from vc_unom u "
//            + "  join tb_houses h on u.FIASHOUSEGUID = h.FIASHOUSEGUID "
//            + "  left join tb_premises_res r on h.UUID = r.uuid_house and r.premisesnum = :NEW.PREMISESNUM "
//            + "  left join tb_premises_nrs n on h.UUID = n.uuid_house and n.premisesnum = :NEW.PREMISESNUM "
//            + "  where u.unom = :NEW.unom; "
//            + "  IF (:NEW.PREMISESTYPE = 'Нежилое' AND val2 IS NOT NULL) OR "
//            + "     (:NEW.PREMISESTYPE IS NULL AND val2 IS NOT NULL AND val1 IS NULL) THEN "        
//            + "    :NEW.ID_TYPE := " + VocMeteringDeviceType.i.NON_RESIDENTIAL_PREMISE.getId() +"; "
//            + "    :NEW.UUID_PREMISE :=  UUID2; "   
//            + "  ELSIF (:NEW.PREMISESTYPE = 'Жилое' AND val1 IS NOT NULL) OR "
//            + "     (:NEW.PREMISESTYPE IS NULL AND val1 IS NOT NULL AND val2 IS NULL) THEN "        
//            + "    :NEW.UUID_PREMISE :=  UUID1; "   
//            + "  ELSIF (:NEW.PREMISESTYPE IS NULL AND val1 IS NOT NULL AND val2 IS NOT NULL) THEN "
//            + "     raise_application_error (-20000, 'Помещение '|| :NEW.PREMISESNUM || ' пустого типа ' || ' присутствует в жилом и не жилом'); "
//            + "  ELSE "        
//            + "     raise_application_error (-20000, 'Не найдено '|| nvl(:NEW.PREMISESTYPE, '(тип не указан)') || ' помещение: ' || :NEW.PREMISESNUM); "
//            + "  END IF; "
//            + " END;END IF; "
//                    
//            + " IF :NEW.ACCOUNTNUMBER IS NOT NULL THEN BEGIN "
//            + "  SELECT 1 INTO cnt FROM tb_accounts WHERE ACCOUNTNUMBER = :NEW.ACCOUNTNUMBER; "
//            + "  EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Лицевой счет '|| :NEW.ACCOUNTNUMBER ||' не найден');"
//            + " END; END IF; "                               
//                
//            + " EXCEPTION WHEN OTHERS THEN "
//            + " :NEW.err := REPLACE(SUBSTR(SQLERRM, 1, 1000), 'ORA-20000: ', ''); "
//
//            + "END;"
//
//        );
        
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
//ORA-02291: integrity constraint (ALEX.SYS_C0010053) violated - parent key not found ORA-06512: at "ALEX.TR_FD3E3E10BDDD5669FE633454181", line 1 ORA-04088: error during execution of trigger 'ALEX.TR_FD3E3E10BDDD5669FE633454181' 
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