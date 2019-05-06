package ru.eludia.products.mosgis.db.model.tables;

import java.math.BigInteger;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocMeteringDeviceType;
import ru.eludia.products.mosgis.db.model.voc.VocMeteringDeviceInstallationPlace;
import ru.eludia.products.mosgis.db.model.voc.VocMeteringDeviceValueType;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi16;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi27;

public class MeteringDevice extends EnTable {
    
    public static final String TABLE_NAME = "tb_meters";

    public enum c implements EnColEnum {
        
	UUID_XL                 	(InXlFile.class,        null,           "Источник импорта"),
        ID_TYPE                (VocMeteringDeviceType.class,                    "Тип прибора учёта"),
        INSTALLATIONPLACE      (VocMeteringDeviceInstallationPlace.class, null, "Место установки"),

        UUID_ORG               (VocOrganization.class,                          "Организация, которая завела данный прибор в БД"),
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

        TEMPERATURESENSORINFORMATION   (Type.STRING, new Virt  ("NVL(TEMPERATURESENSINGELEMENTINFO,NULL)"),  "Информация о наличии датчиков температуры"),
        PRESSURESENSORINFORMATION      (Type.STRING, new Virt  ("NVL(PRESSURESENSINGELEMENTINFO,NULL)"),  "Информация о наличии датчиков температуры"),

        TRANSFORMATIONRATIO    (Type.NUMERIC, 17, 2, null,                      "Коэффициент трансформации"),
        TARIFFCOUNT            (Type.NUMERIC, 1, 0, BigInteger.ONE,             "Количество тарифов"),
        
        ID_CTR_STATUS          (VocGisStatus.class,    VocGisStatus.DEFAULT,    "Статус с точки зрения mosgis"),
        ID_CTR_STATUS_GIS      (VocGisStatus.class,    VocGisStatus.DEFAULT,    "Статус с точки зрения ГИС ЖКХ"),

        ID_LOG                 (MeteringDeviceLog.class,                        "Последнее событие редактирования"),
        
        IS_POWER               (Type.BOOLEAN, new Virt  ("DECODE(\"MASK_VC_NSI_2\", 4, 1, 0)"),  "1 для счётчиков электричества, 0 для прочих"),
        IS_COLLECTIVE          (Type.BOOLEAN, new Virt  ("DECODE(\"ID_TYPE\", 2, 1, 0)"),  "1 для общедомовых (МКД) приборов, 0 для прочих"),
        IS_FOR_BUILDING        (Type.BOOLEAN, new Virt  ("DECODE(\"ID_TYPE\", 1, 1, 2, 1, 0)"),  "1 для приборов, устанавливаемых на МКД/ЖД в целом, 0 для приборов отдельных помещений"),
               
        METERINGDEVICEGUID            (Type.UUID,       null,                   "Идентификатор ПУ"),
        METERINGDEVICEVERSIONGUID     (Type.UUID,       null,                   "Идентификатор версии ПУ"),
        UNIQUENUMBER                  (Type.STRING,     null,                   "Уникальный номер"),

        ;

        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}        
        @Override
        public boolean isLoggable () {
            switch (this) {
                case FIASHOUSEGUID:
                case UUID_ORG:
                case CODE_VC_NSI_27:
                case ID_LOG:
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

    public MeteringDevice () {
        super  (TABLE_NAME, "Приборы учёта");
        cols   (c.class);
        key    (c.UUID_XL);
        key    (c.FIASHOUSEGUID);

        trigger ("AFTER UPDATE", "BEGIN "

            + "IF :NEW.ID_CTR_STATUS <> :OLD.ID_CTR_STATUS AND :NEW.ID_CTR_STATUS = " + VocGisStatus.i.APPROVED
            + " THEN "
                + " UPDATE " + MeteringDeviceValue.TABLE_NAME + " SET ID_CTR_STATUS=:NEW.ID_CTR_STATUS WHERE uuid_meter=:NEW.uuid AND id_type=" + VocMeteringDeviceValueType.i.BASE
            + "; END IF; "

        + "END;");
        
        trigger ("BEFORE INSERT OR UPDATE", 
            "DECLARE "
            + " PRAGMA AUTONOMOUS_TRANSACTION; "
            + " cnt NUMBER; "
            + "BEGIN "

            + "SELECT CODE_VC_NSI_27 INTO :NEW.CODE_VC_NSI_27 FROM vc_meter_types WHERE id = :NEW.ID_TYPE; "

            + "IF :NEW.is_deleted = 0 THEN BEGIN "
                + " IF :NEW.COMMISSIONINGDATE < :NEW.INSTALLATIONDATE THEN raise_application_error (-20000, 'Дата ввода в эксплуатацию не должна быть раньше даты установки.'); END IF; "
//                + " IF :NEW.FIRSTVERIFICATIONDATE < :NEW.FACTORYSEALDATE THEN raise_application_error (-20000, 'Дата последней поверки должна быть не раньше даты опломбирования изготовителем'); END IF; "
                + " IF :NEW.FIRSTVERIFICATIONDATE > TRUNC (SYSDATE, 'DD') THEN raise_application_error (-20000, 'Дата опломбирования изготовителем не может быть позже сегодняшней даты'); END IF; "
                + " IF :NEW.FACTORYSEALDATE > TRUNC (SYSDATE, 'DD') THEN raise_application_error (-20000, 'Дата последней поверки не может быть позже сегодняшней даты'); END IF; "
                + " SELECT COUNT(*) INTO cnt FROM " + TABLE_NAME + " WHERE uuid<>:NEW.uuid AND is_deleted=0 AND fiashouseguid=:NEW.fiashouseguid AND ID_TYPE=:NEW.ID_TYPE AND ID_TYPE=:NEW.ID_TYPE AND METERINGDEVICENUMBER=:NEW.METERINGDEVICENUMBER AND METERINGDEVICESTAMP=:NEW.METERINGDEVICESTAMP AND METERINGDEVICEMODEL=:NEW.METERINGDEVICEMODEL;"
                + " IF cnt>0 THEN raise_application_error (-20000, 'Прибор учёта с такой моделью, маркой и заводским номером уже зарегистрирован в этом доме'); END IF; "
            + " END; END IF; "

            + "IF UPDATING AND :NEW.ID_CTR_STATUS <> :OLD.ID_CTR_STATUS "
                + " AND :OLD.ID_CTR_STATUS <> " + VocGisStatus.i.FAILED_PLACING
                + " AND :NEW.ID_CTR_STATUS =  " + VocGisStatus.i.PROJECT
            + " THEN "
                + " :NEW.ID_CTR_STATUS := " + VocGisStatus.i.MUTATING
            + "; END IF; "

        + "END;");

        trigger ("BEFORE UPDATE", 

            "DECLARE "
            + " PRAGMA AUTONOMOUS_TRANSACTION; "
            + " cnt NUMBER; "
            + " code NUMBER; "

            + "BEGIN "
                                    
            + "IF :NEW.is_deleted=0 AND :NEW.ID_CTR_STATUS <> :OLD.ID_CTR_STATUS AND :NEW.ID_CTR_STATUS=" + VocGisStatus.i.PENDING_RQ_PLACING.getId () + " THEN BEGIN "
                    
                + "IF :NEW.COMMISSIONINGDATE IS NULL THEN raise_application_error (-20000, 'Дата ввода в эксплуатацию должна быть определена до отправки в ГИС ЖКХ.'); END IF; "
                
                + "IF :NEW.CONSUMEDVOLUME=0 THEN BEGIN "
                    + "FOR code in 1 .. 5 LOOP "
                        + "IF BITAND (:NEW.MASK_VC_NSI_2, POWER (2, code-1)) <> 0 THEN BEGIN "
                            + " SELECT COUNT(*) INTO cnt FROM tb_meter_values WHERE is_deleted=0 AND CODE_VC_NSI_2=code AND id_type= " + VocMeteringDeviceValueType.i.BASE + " AND UUID_METER=:NEW.uuid; "
                            + " IF cnt=0 THEN raise_application_error (-20000, 'До отправки в ГИС должны быть внесены базовые показания. Операция отменена.'); END IF; "
                        + "END; END IF; "
                    + "END LOOP;"
                + "END; END IF; "                    
                                    
                + "IF :NEW.ID_TYPE<>"  + VocMeteringDeviceType.i.COLLECTIVE + " THEN BEGIN "
                    + " SELECT COUNT(*) INTO cnt FROM tb_meter_acc WHERE UUID=:NEW.uuid; "
                    + " IF cnt=0 THEN raise_application_error (-20000, 'Не указан ни один лицевой счёт. Операция отменена.'); END IF; "                        
                    + " FOR i IN ("
                        + "SELECT "
                        + " a.accountnumber "
                        + "FROM "
                        + " tb_meter_acc o "
                        + " INNER JOIN tb_accounts a ON o.uuid_account = a.uuid AND a.accountguid IS NULL "
                        + "WHERE o.uuid = :NEW.uuid"
                        + ") LOOP"
                    + " raise_application_error (-20000, "
                        + "'Лицевой счёт №' || i.accountnumber || ' не отправлен в ГИС ЖКХ. Операция отменена.'); "
                    + " END LOOP; "
                + "END; END IF; "                                                        

                    + " FOR i IN ("
                        + "SELECT "
                        + " m.meteringdevicenumber "
                        + "FROM "
                        + " tb_meter_meters o "
                        + " INNER JOIN tb_meters m ON o.uuid_meter = m.uuid AND m.meteringdeviceversionguid IS NULL "
                        + "WHERE o.uuid = :NEW.uuid"
                        + ") LOOP"
                    + " raise_application_error (-20000, "
                        + "'Связанный прибор учёта с заводским (серийным) №' || i.meteringdevicenumber || ' не опубликован в ГИС ЖКХ. Операция отменена.'); "
                    + " END LOOP; "

            + "END; END IF; "
                    
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