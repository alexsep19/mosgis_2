package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;

public class Acknowledgment extends EnTable {
    
    public static final String TABLE_NAME = "tb_pay_ack";
    
    public enum c implements EnColEnum {
        
        UUID_PAY_DOC          (PaymentDocument.class,       "Платёжный документ"),
        UUID_PAY              (Payment.class,               "Платёж"),
        AMOUNT                (Type.NUMERIC, 13, 2, null,   "Размер превышения платы, рассчитанной с применением повышающего коэффициента над размером платы, рассчитанной без учета повышающего коэффициента, руб."),
        
	ID_CTR_STATUS         (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус с точки зрения mosgis"),
	ID_CTR_STATUS_GIS     (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус с точки зрения ГИС ЖКХ"),

        ID_LOG                (AcknowledgmentLog.class,     "Последнее событие редактирования"),

        ;
        
        @Override
        public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}
        
        @Override
        public boolean isLoggable () {
            switch (this) {
//                case ID_LOG:
                case UUID_PAY:
                case UUID_PAY_DOC:
                    return false;
                default:
                    return true;
            }
        }        
        
    }

    public Acknowledgment () {
        
        super  (TABLE_NAME, "Записи квитирования");

        cols   (c.class);        

        key (c.UUID_PAY);
        key (c.UUID_PAY_DOC);
        
        trigger ("AFTER INSERT OR UPDATE", ""
            + "DECLARE"
            + " PRAGMA AUTONOMOUS_TRANSACTION; "
	    + " l_amount_ack NUMBER(20,2); "
            + "BEGIN "
                
            + "  SELECT SUM(AMOUNT) INTO l_amount_ack FROM " + TABLE_NAME + " WHERE is_deleted = 0 AND UUID_PAY_DOC=:NEW.UUID_PAY_DOC AND uuid <> :NEW.UUID;"
            + "  IF l_amount_ack IS NULL THEN l_amount_ack := 0; END IF; "
            + "  IF :NEW.is_deleted = 0 THEN l_amount_ack := l_amount_ack + :NEW.amount; END IF; "
            + "  UPDATE " + PaymentDocument.TABLE_NAME + " SET amount_ack=l_amount_ack WHERE uuid=:NEW.UUID_PAY_DOC; "
                    
            + "  SELECT SUM(AMOUNT) INTO l_amount_ack FROM " + TABLE_NAME + " WHERE is_deleted = 0 AND UUID_PAY_DOC=:NEW.UUID_PAY AND uuid <> :NEW.UUID;"
            + "  IF l_amount_ack IS NULL THEN l_amount_ack := 0; END IF; "
            + "  IF :NEW.is_deleted = 0 THEN l_amount_ack := l_amount_ack + :NEW.amount; END IF; "
            + "  UPDATE " + Payment.TABLE_NAME + " SET amount_ack=l_amount_ack WHERE uuid=:NEW.UUID_PAY; "
                    
            + "  COMMIT;"
                    
            + "END;"
        );

    }    

}