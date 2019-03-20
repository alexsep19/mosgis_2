package ru.eludia.products.mosgis.db.model.incoming.xl.lines;

import java.math.BigInteger;
import java.util.Map;
import java.util.UUID;
import org.apache.poi.xssf.usermodel.XSSFRow;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.db.model.tables.MeteringDevice;
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
        
        INSTALLATIONDATE       (Type.DATE,           null,                      "Дата установки"),
        COMMISSIONINGDATE      (Type.DATE,           null,                      "Дата ввода в эксплуатацию"),
        
        REMOTEMETERINGMODE     (Type.BOOLEAN,  Boolean.FALSE,                   "Наличие возможности дистанционного снятия показаний"),
        REMOTEMETERINGINFO     (Type.STRING,  2000,  null,                      "Информация о наличии возможности дистанционного снятия показаний ПУ указанием наименования установленной системы"),
        
        FIRSTVERIFICATIONDATE  (Type.DATE,           null,                      "Дата последней поверки"),

        CODE_VC_NSI_27         (Nsi27.class,                                    "Тип прибора учета (НСИ 27)"),
        CODE_VC_NSI_16         (Nsi16.class,         null,                      "Межповерочный интервал (НСИ 16)"),

        MASK_VC_NSI_2          (Type.NUMERIC, 3, 0,  BigInteger.ZERO,           "Битовая маска типов коммунальных ресурсов"),

        FACTORYSEALDATE        (Type.DATE,           null,                      "Дата опломбирования ПУ заводом-изготовителем"),

        TEMPERATURESENSOR      (Type.BOOLEAN,  Boolean.FALSE,                   "Наличие датчиков температры"),
        PRESSURESENSOR         (Type.BOOLEAN,  Boolean.FALSE,                   "Наличие датчиков давления"),
        CONSUMEDVOLUME         (Type.BOOLEAN,  Boolean.FALSE,                   "ПУ предоставляет объем потребленного КР"),

        NOTLINKEDWITHMETERING  (Type.BOOLEAN,  Boolean.TRUE,                    "Объем ресурса(ов) определяется с помощью только этого прибора учёта"),
        
        TEMPERATURESENSINGELEMENTINFO  (Type.STRING,  2000,  null,              "Информация о наличии датчиков температуры с указанием их местоположения на узле учета"),
        PRESSURESENSINGELEMENTINFO     (Type.STRING,  2000,  null,              "Информация о наличии датчиков давления с указанием их местоположения на узле учета"),

        TRANSFORMATIONRATIO    (Type.NUMERIC, 17, 2, null,                      "Коэффициент трансформации"),
        TARIFFCOUNT            (Type.NUMERIC, 1, 0, BigInteger.ONE,             "Количество тарифов"),
        
        ID_CTR_STATUS          (VocGisStatus.class,    VocGisStatus.DEFAULT,    "Статус с точки зрения mosgis"),
        ID_CTR_STATUS_GIS      (VocGisStatus.class,    VocGisStatus.DEFAULT,    "Статус с точки зрения ГИС ЖКХ"),
                       
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
                case UNOM:
                case VERIF_INT:
                    return false;                    
                default:                    
                    return true;
            }

        }

    }
    
    public static Map<String, Object> toHash (UUID uuid, int ord, XSSFRow row, Map<Integer, Integer> resourceMap) {
        
        Map<String, Object> r = DB.HASH (
            EnTable.c.IS_DELETED, 1,
            c.UUID_XL, uuid,
            c.ORD, ord    
        );
        
        try {
            setFields (r, row, resourceMap);
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
    
    private static void setFields (Map<String, Object> r, XSSFRow row, Map<Integer, Integer> resourceMap) throws XLException {
        
        r.put (c.METERINGDEVICENUMBER.lc (),  toString (row, 1, "Не указан серийный номер"));        
        r.put (c.ID_TYPE.lc (),               VocMeteringDeviceType.i.fromXL (toString (row, 2, "Не указан тип ПУ").toString ()));      
        r.put (c.METERINGDEVICESTAMP.lc (),   toString (row, 3, "Не указана марка"));
        r.put (c.METERINGDEVICEMODEL.lc (),   toString (row, 4, "Не указана модель"));
        r.put (c.UNOM.lc (),                  toNumeric (row, 6, "Не указан UNOM"));
        r.put (c.REMOTEMETERINGMODE.lc (),    toBool (row, 11, "Не указано наличие возможности дистанционного снятия показаний"));
        r.put (c.REMOTEMETERINGINFO.lc (),    toString (row, 12));
        r.put (c.NOTLINKEDWITHMETERING.lc (), 1 - toBool (row, 13, "Не указано, определяется ли объём ресурсов несколькими ПУ"));
        r.put (c.INSTALLATIONPLACE.lc (),     toString (row, 14));
    
        try {
            final String resourceLabel = toString (row, 16, "Не указаны виды коммунальных ресурсов");
            final int mask = addResource (resourceMap, toNumeric (row, 0).intValue (), resourceLabel);            
            r.put (c.MASK_VC_NSI_2.lc (), Nsi2.i.forId (mask).getId ());
        }
        catch (Exception ex) {
            throw new XLException ("Некорректно заданы виды коммунальных ресурсов");
        }
        
        r.put (c.FIRSTVERIFICATIONDATE.lc (),         toDate (row, 25));
        r.put (c.FACTORYSEALDATE.lc (),               toDate (row, 26));
        r.put (c.VERIF_INT.lc (),                     toNumeric (row, 27));
        r.put (c.TEMPERATURESENSOR.lc (),             toBool (row, 28));
        r.put (c.TEMPERATURESENSINGELEMENTINFO.lc (), toString (row, 29));
        r.put (c.PRESSURESENSOR.lc (),                toBool (row, 30));
        r.put (c.PRESSURESENSINGELEMENTINFO.lc (),    toString (row, 31));
        
    }

    public InXlMeteringDevice () {

        super (TABLE_NAME, "Строки импорта приборов учёта");

        cols  (c.class);

        key ("uuid_xl", c.UUID_XL);
        
        trigger ("BEFORE INSERT", ""
                
            + "DECLARE "
            + " cnt NUMBER; "
  
            + "BEGIN "
                
            + " SELECT uuid_org INTO :NEW.uuid_org FROM in_xl_files WHERE uuid=:NEW.uuid_xl; "

            + " IF :NEW.err IS NOT NULL THEN RETURN; END IF; "
                
            + " SELECT COUNT(*), MIN(fiashouseguid) INTO cnt, :NEW.fiashouseguid FROM vc_unom WHERE id_status=1 AND unom=:NEW.unom; "
            + " IF cnt=0 THEN raise_application_error (-20000, 'Неизвестное значение UNOM: ' || :NEW.unom); END IF; "
            + " IF cnt>1 THEN raise_application_error (-20000, 'UNOM ' || :NEW.unom || ' соответствует не одна запись ФИАС, а ' || cnt); END IF; "

            + " IF :NEW.verif_int IS NOT NULL THEN BEGIN "
            + "  SELECT id INTO :NEW.code_vc_nsi_16 FROM vw_nsi_16 WHERE value=:NEW.verif_int; "
            + "  EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Значение межповерочного интервала не найдено в справочнике НСИ 16');"
            + " END; END IF; "                               
                
            + " EXCEPTION WHEN OTHERS THEN "
            + " :NEW.err := REPLACE(SUBSTR(SQLERRM, 1, 1000), 'ORA-20000: ', ''); "

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
//            + " l_err VARCHAR2 (1000); "
            + "BEGIN "

            + " IF NOT (:OLD.is_deleted = 1 AND :NEW.is_deleted = 0) THEN RETURN; END IF; "

            + " INSERT INTO " + MeteringDevice.TABLE_NAME + " (uuid,is_deleted" + sb + ") VALUES (:NEW.uuid,1" + nsb + "); "
            + " COMMIT; "

            + "END; "

        );        

    }

}