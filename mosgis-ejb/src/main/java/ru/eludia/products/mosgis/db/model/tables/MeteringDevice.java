package ru.eludia.products.mosgis.db.model.tables;

import java.math.BigInteger;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocMeteringDeviceType;
import ru.eludia.products.mosgis.db.model.voc.VocMeteringDeviceInstallationPlace;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi16;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi27;

public class MeteringDevice extends EnTable {
    
    public enum c implements EnColEnum {
        
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

        TRANSFORMATIONRATIO    (Type.NUMERIC, 17, 2, null,                      "Коэффициент трансформации"),
        TARIFFCOUNT            (Type.NUMERIC, 1, 0, BigInteger.ONE,             "Количество тарифов"),
        
        ID_CTR_STATUS          (VocGisStatus.class,    VocGisStatus.DEFAULT,    "Статус с точки зрения mosgis"),
        ID_CTR_STATUS_GIS      (VocGisStatus.class,    VocGisStatus.DEFAULT,    "Статус с точки зрения ГИС ЖКХ"),

        ID_LOG                 (MeteringDeviceLog.class,                        "Последнее событие редактирования"),
        
        IS_POWER               (Type.BOOLEAN, new Virt  ("DECODE(\"MASK_VC_NSI_2\", 4, 1, 0)"),  "1 для счётчиков электричества, 0 для прочих"),
        IS_COLLECTIVE          (Type.BOOLEAN, new Virt  ("DECODE(\"ID_TYPE\", 2, 1, 0)"),  "1 для общедомовых (МКД) приборов, 0 для прочих"),
        IS_FOR_BUILDING        (Type.BOOLEAN, new Virt  ("DECODE(\"ID_TYPE\", 1, 1, 2, 1, 0)"),  "1 для приборов, устанавливаемых на МКД/ЖД в целом, 0 для приборов отдельных помещений"),
        
        METERINGDEVICEVERSIONGUID     (Type.UUID,       null,                   "Идентификатор версии ПУ"),

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

    }

    public MeteringDevice () {
        super  ("tb_meters", "Приборы учёта");
        cols   (c.class);
        key    ("fiashouseguid", "fiashouseguid");
        
        trigger ("BEFORE INSERT OR UPDATE", "BEGIN "

            + "SELECT CODE_VC_NSI_27 INTO :NEW.CODE_VC_NSI_27 FROM vc_meter_types WHERE id = :NEW.ID_TYPE; "
                    
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