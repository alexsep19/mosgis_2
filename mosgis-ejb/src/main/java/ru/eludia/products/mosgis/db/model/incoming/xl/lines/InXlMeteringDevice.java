package ru.eludia.products.mosgis.db.model.incoming.xl.lines;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashSet;
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
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi16;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi2;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi27;
import ru.eludia.products.mosgis.jms.xl.base.XLException;

public class InXlMeteringDevice extends EnTable {

    public static final String TABLE_NAME = "in_xl_meters";
    static Pattern MASK_ROOMNUM = Pattern.compile("(\\d+,)*\\d");
    static Pattern MASK_ACCOUNTNUM = Pattern.compile("[^,]+");

    public enum c implements ColEnum {

        UUID_XL                 (InXlFile.class,            "Файл импорта"),
        ORD                     (Type.NUMERIC, 5,           "Номер строки"),
        ERR                     (Type.STRING,  null,        "Ошибка"),
        UNOM                    (Type.INTEGER,                                   "Идентификатор дома в московских системах"),
        VERIF_INT               (Type.INTEGER, null,                             "Межповерочный интервал"),
        
        UUID_ORG                (VocOrganization.class,     "Организация-источник данных"),
        
        ID_TYPE                (VocMeteringDeviceType.class,                    "Тип прибора учёта"),
        INSTALLATIONPLACE      (VocMeteringDeviceInstallationPlace.class, null, "Место установки"),

        UUID_PREMISE           (Premise.class,       null,                      "Помещение"),
	FIASHOUSEGUID          (VocBuilding.class,                              "Глобальный уникальный идентификатор дома по ФИАС"),

        METERINGDEVICENUMBER   (Type.STRING,  50,                               "Заводской (серийный) номер ПУ"),
        METERINGDEVICESTAMP    (Type.STRING,  100,                              "Марка ПУ"),
        METERINGDEVICEMODEL    (Type.STRING,  100,                              "Модель ПУ"),
        
        PREMISESNUM            (Type.STRING, 255,    null,                      "Номер помещения"),
        PREMISESTYPE           (Type.STRING,  30,    null,                      "Жилое/Нежилое (для проверок tb_premises_res/tb_premises_nrs)" ),
        ROOMNUMBER             (Type.STRING, 255,    null,                      "Номер комнаты" ),
        ACCOUNTNUMBER          (Type.STRING,  30,    null,                      "№ лицевого счета"),
        
        INSTALLATIONDATE       (Type.DATE,           null,                      "Дата установки"),
        COMMISSIONINGDATE      (Type.DATE,           null,                      "Дата ввода в эксплуатацию"),
        
        REMOTEMETERINGMODE     (Type.BOOLEAN,  Boolean.FALSE,                   "Наличие возможности дистанционного снятия показаний"),
        REMOTEMETERINGINFO     (Type.STRING,  2000,  null,                      "Информация о наличии возможности дистанционного снятия показаний ПУ указанием наименования установленной системы"),
        
        FIRSTVERIFICATIONDATE  (Type.DATE,           null,                      "Дата последней поверки"),

        CODE_VC_NSI_27         (Nsi27.class,                                    "Тип прибора учета (НСИ 27)"),
        CODE_VC_NSI_16         (Nsi16.class,         null,                      "Межповерочный интервал (НСИ 16)"),

        MASK_VC_NSI_2          (Type.NUMERIC, 3, 0,  BigInteger.ZERO,           "Битовая маска типов коммунальных ресурсов"),
//====== поля для MeteringDeviceValue
//        UUID_METER             (MeteringDevice.class,                           "Прибор учёта"),
//        METERINGVALUET1        (Type.NUMERIC, 22, 7, null,                      "Последнее полученное показание (Т1)"),
//        METERINGVALUET2        (Type.NUMERIC, 22, 7, null,                      "Последнее полученное показание (Т2)"),
//        METERINGVALUET3        (Type.NUMERIC, 22, 7, null,                      "Последнее полученное показание (Т3)"),
//===================================        
        FACTORYSEALDATE        (Type.DATE,           null,                      "Дата опломбирования ПУ заводом-изготовителем"),

        TEMPERATURESENSOR      (Type.BOOLEAN,  Boolean.FALSE,                   "Наличие датчиков температры"),
        PRESSURESENSOR         (Type.BOOLEAN,  Boolean.FALSE,                   "Наличие датчиков давления"),
//0 -'текущие показания'(базовые показания вводятся), 1 -'потреблённый объём'(базовые показания не вводятся)
        CONSUMEDVOLUME         (Type.BOOLEAN,  Boolean.FALSE,                   "ПУ предоставляет объем потребленного КР"),

        NOTLINKEDWITHMETERING  (Type.BOOLEAN,  Boolean.TRUE,                    "Объем ресурса(ов) определяется с помощью только этого прибора учёта"),
        
        TEMPERATURESENSINGELEMENTINFO  (Type.STRING,  2000,  null,              "Информация о наличии датчиков температуры с указанием их местоположения на узле учета"),
        PRESSURESENSINGELEMENTINFO     (Type.STRING,  2000,  null,              "Информация о наличии датчиков давления с указанием их местоположения на узле учета"),

        TRANSFORMATIONRATIO    (Type.NUMERIC, 17, 2, null,                      "Коэффициент трансформации"),
        TARIFFCOUNT            (Type.NUMERIC, 1, 0, BigInteger.ONE,             "Количество тарифов"),
        
        ID_CTR_STATUS          (VocGisStatus.class,    VocGisStatus.DEFAULT,    "Статус с точки зрения mosgis"),
        ID_CTR_STATUS_GIS      (VocGisStatus.class,    VocGisStatus.DEFAULT,    "Статус с точки зрения ГИС ЖКХ"),
//        ISAUTOMATICCALCULATEVOLUME    (Type.BOOLEAN,  Boolean.FALSE,              "Наличие технической возможности автоматического расчета потребляемого объема ресурса"),         
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        boolean isToCopyMeteringDevice () {

            switch (this) {
                case ORD:
                case ERR:
                    return false;                    
                default:                    
                    return MeteringDevice.c.isExists(this.name());
            }
        }

        boolean isToCopyMeteringDeviceValue () {

            switch (this) {
                case ORD:
                case ERR:
                    return false;                    
                default:                    
                    return MeteringDeviceValue.c.isExists(this.name());
            }
        }

    }
    
    public static Map<String, Object> toHash (UUID uuid, int ord, XSSFRow row, Map<Integer, Integer> resourceMap, HashSet<Integer> refNums) {
        
        Map<String, Object> r = DB.HASH (
            EnTable.c.IS_DELETED, 1,
            c.UUID_XL, uuid,
            c.ORD, ord    
        );
        
        try {
            setFields (r, row, resourceMap, refNums);
        }         
        catch (XLException ex) {
            r.put (c.ERR.lc (), ex.getMessage ());
        }
        
        return r;
        
    }
    
    public static int addResource (Map<Integer, Integer> resourceMap, int k, String label) {
        Integer v = resourceMap.get (k);
        int result = v == null ? 0 : v;
        return result | Nsi2.i.forLabel (label).getId ();
    }    
    
    private static void setFields (Map<String, Object> r, XSSFRow row, Map<Integer, Integer> resourceMap, HashSet<Integer> refNums) throws XLException {
        
        r.put (c.METERINGDEVICENUMBER.lc (),  toString (row, 1, "Не указан серийный номер"));  
        //для Индивидуальный всегда возвращается RESIDENTIAL_PREMISE, потом в триггере устанавливается правильно
        VocMeteringDeviceType.i vocMeteringDeviceType = VocMeteringDeviceType.i.fromXL (toString (row, 2, "Не указан тип ПУ").toString ());
        r.put (c.ID_TYPE.lc (), vocMeteringDeviceType);      
        r.put (c.METERINGDEVICESTAMP.lc (),   toString (row, 3, "Не указана марка"));
        r.put (c.METERINGDEVICEMODEL.lc (),   toString (row, 4, "Не указана модель"));
        //G
        r.put (c.UNOM.lc (),                  toNumeric (row, 6, "Не указан UNOM"));
        
        r.put (c.PREMISESTYPE.lc(),           
               VocMeteringDeviceType.i.COLLECTIVE.equals(vocMeteringDeviceType)? toNull (row, 7, "Указан тип помещения"): toString (row, 7));
        r.put (c.PREMISESNUM.lc(),            
               VocMeteringDeviceType.i.COLLECTIVE.equals(vocMeteringDeviceType)? toNull (row, 8, "Указан номер помещения"): 
                       toString (row, 8, "Не указан номер помещения"));
        r.put (c.ROOMNUMBER.lc(),             
               VocMeteringDeviceType.i.LIVING_ROOM.equals(vocMeteringDeviceType)? 
                       toString (row, 9, MASK_ROOMNUM, "Не указан или ошибочен номер комнаты"): toNull (row, 9, "Указан номер комнаты") );
        //K
        r.put (c.ACCOUNTNUMBER.lc(),          
               VocMeteringDeviceType.i.COLLECTIVE.equals(vocMeteringDeviceType)? 
                       toNull (row, 10, "Указан лицевой счет"): toString (row, 10, MASK_ACCOUNTNUM, "Не указан или ошибочный лицевой счет"));
        
        r.put (c.REMOTEMETERINGMODE.lc (),    toBool (row, 11, "Не указано наличие возможности дистанционного снятия показаний"));
        r.put (c.REMOTEMETERINGINFO.lc (),    toString (row, 12));
        r.put (c.NOTLINKEDWITHMETERING.lc (), 1 - toBool (row, 13, "Не указано, определяется ли объём ресурсов несколькими ПУ"));
        r.put (c.INSTALLATIONPLACE.lc (),     toString (row, 14));
    
        //Q
        BigDecimal refNum;
        if ((refNum = toNumeric (row, 0)) != null){
            if (refNums.contains(refNum.intValue())){
                throw new XLException ("Дублируется ссылочный номер ПУ "+refNum.intValue());
            }
            refNums.add(refNum.intValue());
        }
        try {
            final String resourceLabel = toString (row, 16, "Не указаны виды коммунальных ресурсов");
            final int mask = addResource (resourceMap, refNum == null? -1: refNum.intValue(), resourceLabel);            
            r.put (c.MASK_VC_NSI_2.lc (), Nsi2.i.forId (mask).getId ());
        }
        catch (Exception ex) {
            throw new XLException ("Некорректно заданы виды коммунальных ресурсов");
        }
        //R 17
        //S 18
        //AG
        r.put (c.CONSUMEDVOLUME.lc (), toBoolNotNull (row, 32) == 1? 0: 1 );
        //T 
//        if (isAutomaticCalculateVolume == null || isAutomaticCalculateVolume == 0){
//           r.put (c.METERINGVALUET1.lc (),  toNumeric (row, 19, "Не указано последнее полученное показание (Т1)"));
//           r.put (c.METERINGVALUET2.lc (),  toNumeric (row, 20 ));
//           r.put (c.METERINGVALUET3.lc (),  toNumeric (row, 21 ));
//        }
        //X
        Date installDate = (Date) toDate (row, 23);
        r.put (c.INSTALLATIONDATE.lc (), installDate);
        //Y
        Date commDate =  VocMeteringDeviceType.i.COLLECTIVE.equals(vocMeteringDeviceType)? 
                         (Date)toDate (row, 24): (Date)toDate (row, 24, "Не указана дата ввода в эксплуатацию");
        if (installDate != null && installDate.after(commDate)) throw new XLException ("Дата установки больше даты ввода в эксплуатацию");
        r.put (c.COMMISSIONINGDATE.lc (), commDate);
        //Z
        r.put (c.FIRSTVERIFICATIONDATE.lc (),         toDate (row, 25));
        //AA
        r.put (c.FACTORYSEALDATE.lc (),               toDate (row, 26));
        //AB
        r.put (c.VERIF_INT.lc (),                     toNumeric (row, 27));
        //AC
        r.put (c.TEMPERATURESENSOR.lc (),             toBool (row, 28));
        //AD
        r.put (c.TEMPERATURESENSINGELEMENTINFO.lc (), toString (row, 29));
        //AE
        r.put (c.PRESSURESENSOR.lc (),                toBool (row, 30));
        //AF
        r.put (c.PRESSURESENSINGELEMENTINFO.lc (),    toString (row, 31));
    }

    public InXlMeteringDevice () {

        super (TABLE_NAME, "Строки импорта приборов учёта");

        cols  (c.class);

        key ("uuid_xl", c.UUID_XL);
        
        trigger ("BEFORE INSERT", ""
                
            + "DECLARE "
            + " cnt NUMBER; "
            + " val1 NUMBER; "
            + " val2 NUMBER; "
            + " UUID1 RAW(16); "
            + " UUID2 RAW(16); "
            + "BEGIN "
                
            + "BEGIN "
            + " SELECT uuid_org INTO :NEW.uuid_org FROM in_xl_files WHERE uuid=:NEW.uuid_xl; "
            + " EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Организация не найдена'); "
            + "END; "

            + " IF :NEW.err IS NOT NULL THEN RETURN; END IF; "
                
            + " SELECT COUNT(*), MIN(fiashouseguid) INTO cnt, :NEW.fiashouseguid FROM vc_unom WHERE id_status=1 AND unom=:NEW.unom; "
            + " IF cnt=0 THEN raise_application_error (-20000, 'Неизвестное значение UNOM: ' || :NEW.unom); END IF; "
            + " IF cnt>1 THEN raise_application_error (-20000, 'UNOM ' || :NEW.unom || ' соответствует не одна запись ФИАС, а ' || cnt); END IF; "

            + " IF :NEW.verif_int IS NOT NULL THEN BEGIN "
            + "  SELECT id INTO :NEW.code_vc_nsi_16 FROM vw_nsi_16 WHERE value=:NEW.verif_int; "
            + "  EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Значение межповерочного интервала не найдено в справочнике НСИ 16');"
            + " END; END IF; "                               

            + " IF :NEW.ID_TYPE != " + VocMeteringDeviceType.i.COLLECTIVE.getId() +" THEN BEGIN "
            + "  select r.premisesnum, r.UUID, n.premisesnum, n.UUID INTO val1, UUID1, val2, UUID2 from vc_unom u "
            + "  join tb_houses h on u.FIASHOUSEGUID = h.FIASHOUSEGUID "
            + "  left join tb_premises_res r on h.UUID = r.uuid_house and r.premisesnum = :NEW.PREMISESNUM "
            + "  left join tb_premises_nrs n on h.UUID = n.uuid_house and n.premisesnum = :NEW.PREMISESNUM "
            + "  where u.unom = :NEW.unom; "
            + "  IF (:NEW.PREMISESTYPE = 'Нежилое' AND val2 IS NOT NULL) OR "
            + "     (:NEW.PREMISESTYPE IS NULL AND val2 IS NOT NULL AND val1 IS NULL) THEN "        
            + "    :NEW.ID_TYPE := " + VocMeteringDeviceType.i.NON_RESIDENTIAL_PREMISE.getId() +"; "
            + "    :NEW.UUID_PREMISE :=  UUID2; "   
            + "  ELSIF (:NEW.PREMISESTYPE = 'Жилое' AND val1 IS NOT NULL) OR "
            + "     (:NEW.PREMISESTYPE IS NULL AND val1 IS NOT NULL AND val2 IS NULL) THEN "        
            + "    :NEW.UUID_PREMISE :=  UUID1; "   
            + "  ELSIF (:NEW.PREMISESTYPE IS NULL AND val1 IS NOT NULL AND val2 IS NOT NULL) THEN "
            + "     raise_application_error (-20000, 'Помещение '|| :NEW.PREMISESNUM || ' пустого типа ' || ' присутствует в жилом и не жилом'); "
            + "  ELSE "        
            + "     raise_application_error (-20000, 'Не найдено '|| nvl(:NEW.PREMISESTYPE, '(тип не указан)') || ' помещение: ' || :NEW.PREMISESNUM); "
            + "  END IF; "
            + "     EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Помещение '||:NEW.PREMISESNUM|| ' не найдено'); "  
            + " END;END IF; "
                //лицевые счета могут быть через запятую
            + " IF :NEW.ACCOUNTNUMBER IS NOT NULL THEN BEGIN "
            + "   FOR ACCOUNTNUM IN (SELECT REGEXP_SUBSTR (:NEW.ACCOUNTNUMBER, '[^,]+', 1, LEVEL) TXT FROM DUAL "
            + "       CONNECT BY REGEXP_SUBSTR (:NEW.ACCOUNTNUMBER, '[^,]+', 1, LEVEL) IS NOT NULL) LOOP "
            + "   SELECT count(*) INTO cnt FROM tb_accounts a "
            + "   join tb_account_items i on i.UUID_ACCOUNT = a.UUID "
            + "   where TRIM(ACCOUNTNUM.TXT) in (a.serviceid, a.UNIFIEDACCOUNTNUMBER, a.ACCOUNTNUMBER) AND a.id_ctr_status_gis = 40 AND "
            + "      (exists (select 1 from tb_premises_nrs n where i.uuid_premise = n.uuid and n.premisesnum = :NEW.PREMISESNUM) "
            + "      or exists (select 1 from tb_premises_res r where i.uuid_premise = r.uuid and r.premisesnum = :NEW.PREMISESNUM)); "
            + "  IF cnt = 0 THEN raise_application_error (-20000, 'Лицевой счет '|| ACCOUNTNUM.TXT ||' не найден для помещения '||:NEW.PREMISESNUM);END IF; "
            + "  IF cnt > 1 THEN raise_application_error (-20000, 'Лицевой счет '|| ACCOUNTNUM.TXT ||' найден для '||cnt||'-х помещений');END IF; "
            + " END LOOP; "
            + " END; "   
            + " END IF; "         
                
            + " EXCEPTION WHEN OTHERS THEN "
            + " :NEW.err := REPLACE(SUBSTR(SQLERRM, 1, 1000), 'ORA-20000: ', ''); "

            + "END;"

        );
        
        StringBuilder fieldsMeteringDevice = new StringBuilder ();
        StringBuilder valuesMeteringDevice = new StringBuilder ();
        
        for (c c: c.values ()) if (c.isToCopyMeteringDevice ()) {
            fieldsMeteringDevice.append (',');
            fieldsMeteringDevice.append (c.lc ());
            valuesMeteringDevice.append (",:NEW.");
            valuesMeteringDevice.append (c.lc ());
        }        

//        StringBuilder fieldsMeteringValues = new StringBuilder ();
//        StringBuilder valuesMeteringValues = new StringBuilder ();
//        
//        for (c c: c.values ()) if (c.isToCopyMeteringDeviceValue ()) {
//            fieldsMeteringValues.append (',');
//            fieldsMeteringValues.append (c.lc ());
//            valuesMeteringValues.append (",:NEW.");
//            valuesMeteringValues.append (c.lc ());
//        }        
        
        trigger ("BEFORE UPDATE", ""

            + "BEGIN "
            + " IF :NEW.err IS NOT NULL THEN :NEW.is_deleted := 1; END IF; "
            + "END; "

        );        
        
        trigger ("AFTER UPDATE", ""

            + "DECLARE" 
            + " PRAGMA AUTONOMOUS_TRANSACTION; "
//            + " l_err VARCHAR2 (1000); "
            + "BEGIN "

            + " IF NOT (:OLD.is_deleted = 1 AND :NEW.is_deleted = 0) THEN RETURN; END IF; "

            + " INSERT INTO " + MeteringDevice.TABLE_NAME + " (uuid,is_deleted" + fieldsMeteringDevice + ") VALUES (:NEW.uuid,1" + valuesMeteringDevice + "); "
            + " COMMIT; "

            + "END; "

        );        

    }

}