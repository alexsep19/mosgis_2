package ru.eludia.products.mosgis.db.model.incoming.xl.lines;

import java.math.BigInteger;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.db.model.tables.MeteringDeviceLog;
import ru.eludia.products.mosgis.db.model.tables.Premise;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocMeteringDeviceInstallationPlace;
import ru.eludia.products.mosgis.db.model.voc.VocMeteringDeviceType;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi16;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi27;
import ru.eludia.products.mosgis.jms.xl.base.XLException;

public class InXlMeteringDevice extends EnTable {

    public enum c implements ColEnum {

        UUID_XL                 (InXlFile.class,            "Файл импорта"),
        ORD                     (Type.NUMERIC, 5,           "Номер строки"),
        
        UUID_ORG                (VocOrganization.class,     "Организация-источник данных"),
        ERR                     (Type.STRING,  null,        "Ошибка"),
        
        
        
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
                       
        METERINGDEVICEGUID            (Type.UUID,       null,                   "Идентификатор ПУ"),
        METERINGDEVICEVERSIONGUID     (Type.UUID,       null,                   "Идентификатор версии ПУ"),
        UNIQUENUMBER                  (Type.STRING,     null,                   "Уникальный номер"),        

        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        boolean isToCopy () {

            switch (this) {

                case UUID_XL:

//                case UUID_CONTRACT:
//                case UUID_CONTRACT_AGREEMENT:

                case FIASHOUSEGUID:

//                case ENDDATE:
//                case STARTDATE:
                    
                    return true;
                    
                default:
                    
                    return false;

            }

        }

    }
    
    public static Map<String, Object> toHash (UUID uuid, int ord, XSSFRow row) {
        
        Map<String, Object> r = DB.HASH (
            EnTable.c.IS_DELETED, 1,
            c.UUID_XL, uuid,
            c.ORD, ord    
        );
        
        try {
            setFields (r, row);
        }         
        catch (XLException ex) {
            r.put (c.ERR.lc (), ex.getMessage ());
        }
        
        return r;
        
    }
    
    
    
    private static void setFields (Map<String, Object> r, XSSFRow row) throws XLException {
        
        r.put (c.METERINGDEVICENUMBER.lc (), toString (row, 1, "Не указан серийный номер"));        
        r.put (c.ID_TYPE.lc (), VocMeteringDeviceType.i.fromXL (toString (row, 2, "Не указан тип ПУ").toString ()));      
        r.put (c.METERINGDEVICESTAMP.lc (), toString (row, 3, "Не указана марка"));
        r.put (c.METERINGDEVICEMODEL.lc (), toString (row, 4, "Не указана модель"));

    }

    public InXlMeteringDevice () {

        super ("in_xl_ctr_objects", "Строки импорта объектов управления");

        cols  (c.class);

        key ("uuid_xl", c.UUID_XL);
        
        trigger ("BEFORE INSERT", ""
                
            + "DECLARE "
            + " ctr tb_contracts%ROWTYPE; "
  
            + "BEGIN "
                
            + " SELECT uuid_org INTO :NEW.uuid_org FROM in_xl_files WHERE uuid=:NEW.uuid_xl; "

            + " IF :NEW.err IS NOT NULL THEN RETURN; END IF; "
/*                
            + "BEGIN "
            + " SELECT * INTO ctr FROM tb_contracts WHERE is_deleted=0 AND uuid_org=:NEW.uuid_org AND docnum=:NEW.docnum AND signingdate=:NEW.signingdate; "
            + " EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Не удалось определить договор по номеру и дате подписания');"
            + "END;"
                               
            + " IF ctr.id_ctr_status > 11 THEN FOR i IN (SELECT label FROM vc_gis_status WHERE id=ctr.id_ctr_status) LOOP raise_application_error (-20000, 'Договор находится в статусе \"' || i.label || '\"; добавление объектов запрещено'); END LOOP; END IF; "
            + " IF :NEW.startdate < ctr.effectivedate THEN raise_application_error (-20000, 'Дата начала выходит за период действия договора'); END IF; "
            + " IF :NEW.enddate > ctr.plandatecomptetion THEN raise_application_error (-20000, 'Дата окончания выходит за период действия договора'); END IF; "
            + " :NEW.uuid_contract := ctr.uuid; "

            + " IF :NEW.agreementdate IS NOT NULL THEN BEGIN "
            + "  SELECT uuid INTO :NEW.uuid_contract_agreement FROM tb_contract_files WHERE id_status=1 AND uuid_contract=:NEW.uuid_contract AND agreementnumber=:NEW.agreementnumber AND agreementdate=:NEW.agreementdate; "
            + "  EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Не удалось определить дополнительное соглашение по номеру и дате');"
            + " END; END IF; "                                

            + "BEGIN "
            + " SELECT fiashouseguid INTO :NEW.fiashouseguid FROM vc_unom WHERE unom=:NEW.unom; "
            + " EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Не удалось однозначно определить GUID ФИАС по UNOM');"
            + "END;"
*/
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

            + "DECLARE" 
            + " PRAGMA AUTONOMOUS_TRANSACTION; "
            + " l_err VARCHAR2 (1000); "
            + "BEGIN "

            + " IF :NEW.err IS NOT NULL THEN :NEW.is_deleted := 1; END IF; "
            + " IF NOT (:OLD.is_deleted = 1 AND :NEW.is_deleted = 0) THEN RETURN; END IF; "

            + " INSERT INTO tb_contract_objects (uuid,is_deleted" + sb + ") VALUES (:NEW.uuid,0" + nsb + "); "
            + " UPDATE tb_contract_objects SET is_deleted=1 WHERE uuid=:NEW.uuid; "
            + " COMMIT; "

            + "END; "

        );        

    }

}