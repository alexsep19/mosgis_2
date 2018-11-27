package ru.eludia.products.mosgis.db.model.tables;

import java.util.logging.Logger;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.DATE;
import static ru.eludia.base.model.Type.NUMERIC;
import static ru.eludia.base.model.Type.UUID;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;

public class CharterPayment extends EnTable {

    public enum c implements EnColEnum {

        UUID_CHARTER         (Charter.class,          "Ссылка на договор"),
        UUID_CHARTER_OBJECT  (CharterObject.class,    "Ссылка на объект договора"),
        UUID_FILE_0          (CharterPaymentFile.class, "Ссылка на протокол для членов кооператива"),
        UUID_FILE_1          (CharterPaymentFile.class, "Ссылка на протокол для не членов кооператива"),
        FIASHOUSEGUID        (VocBuilding.class,       "Дом"),
        ID_CTR_STATUS        (VocGisStatus.class,      VocGisStatus.DEFAULT, "Статус объекта договора с точки зрения mosgis"),
        ID_CTR_STATUS_GIS    (VocGisStatus.class,      VocGisStatus.DEFAULT, "Статус объекта договора с точки зрения ГИС ЖКХ"),        
        
        ID_LOG               (CharterPaymentLog.class,  "Последнее событие редактирования"),
                
        BEGINDATE            (DATE,                    "Дата начала периода"),
        ENDDATE              (DATE,                    "Дата окончания периода"),
        PAYMENT_0            (NUMERIC, 10, 2, null,    "Размер платы за содержание и ремонт жилого помещения для собственника помещения в МКД"),
        PAYMENT_1            (NUMERIC, 10, 2, null,    "Размер обязательных платежей и (или) взносов членов товарищества, кооператива"),

        VERSIONGUID          (UUID, null, "Идентификатор версии сведений о размере платы по ДУ")

        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {
            switch (this) {
                case ID_LOG:
                case UUID_CHARTER:
                case UUID_CHARTER_OBJECT:
                case FIASHOUSEGUID:
                    return false;
                default: 
                    return true;
            }
        }

    }

    public CharterPayment () {
        
        super ("tb_ch_payments", "[Сведения о размере платы за] услуги управления");

        cols   (c.class);
        
        key    ("uuid_charter", c.UUID_CHARTER);
        key    ("uuid_charter_object", c.UUID_CHARTER_OBJECT);

        trigger ("BEFORE INSERT",                 
            "BEGIN "
            + "  IF :NEW.uuid_charter_object IS NOT NULL THEN "
                + "SELECT fiashouseguid INTO :NEW.fiashouseguid FROM tb_charter_objects WHERE uuid=:NEW.uuid_charter_object; "                      
            + "  END IF;"
            + "END;"                
        );
                
        trigger ("BEFORE INSERT OR UPDATE", ""
                
            + "DECLARE" 
            + " PRAGMA AUTONOMOUS_TRANSACTION; "
            + "BEGIN "                    
                    
            + "IF :NEW.is_deleted = 0 THEN BEGIN "
                
                + "  IF :NEW.uuid_charter_object IS NOT NULL THEN "
                + "     SELECT fiashouseguid INTO :NEW.fiashouseguid FROM tb_charter_objects WHERE uuid=:NEW.uuid_charter_object; "
                + "  END IF;"

                + " FOR i IN ("
                    + "SELECT "
                    + " o.begindate"
                    + " , o.enddate "
                    + "FROM "
                    + " tb_ch_payments o "
                    + "WHERE o.is_deleted = 0"
                    + " AND o.uuid_charter = :NEW.uuid_charter "
                    + " AND (o.fiashouseguid IS NULL OR :NEW.fiashouseguid IS NULL OR o.fiashouseguid = :NEW.fiashouseguid)"
                    + " AND o.enddate   >= :NEW.begindate "
                    + " AND o.begindate <= :NEW.enddate "
                    + " AND o.uuid <> NVL(:NEW.uuid, '00') "
                    + ") LOOP"
                + " raise_application_error (-20000, "
                    + "'Указанный период пересекается с другой информацией о размере платы за жилое помещение с ' "
                    + "|| TO_CHAR (i.begindate, 'DD.MM.YYYY')"
                    + "||' по '"
                    + "|| TO_CHAR (i.enddate, 'DD.MM.YYYY')"
                    + "|| '. Операция отменена.'); "
                + " END LOOP; "

            + "END; END IF; "
                    
        + "END;");        

    }
    
    private static final Logger logger = Logger.getLogger (CharterPayment.class.getName ());    
    
    public enum Action {
        
        PLACING     (VocGisStatus.i.PENDING_RP_PLACING,   VocGisStatus.i.FAILED_PLACING),
        ANNULMENT   (VocGisStatus.i.PENDING_RP_ANNULMENT, VocGisStatus.i.FAILED_ANNULMENT)
        ;
        
        VocGisStatus.i nextStatus;
        VocGisStatus.i failStatus;

        private Action (VocGisStatus.i nextStatus, VocGisStatus.i failStatus) {
            this.nextStatus = nextStatus;
            this.failStatus = failStatus;
        }

        public VocGisStatus.i getNextStatus () {
            return nextStatus;
        }

        public VocGisStatus.i getFailStatus () {
            return failStatus;
        }
        
        public static Action forStatus (VocGisStatus.i status) {
            switch (status) {
                case PENDING_RQ_PLACING:   return PLACING;
                case PENDING_RQ_ANNULMENT: return ANNULMENT;
                default: return null;
            }            
        }
                        
    };
    
}