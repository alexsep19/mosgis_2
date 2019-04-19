package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnTable;

public class AcknowledgmentItem extends EnTable {
    
    public static final String TABLE_NAME = "tb_pay_ack_item";
    
    public enum c implements ColEnum {
        
        UUID_ACK              (Acknowledgment.class,         "Запись квитирования"),

        UUID_PAY              (Payment.class,                "Платёж"),
        
        UUID_CHARGE           (ChargeInfo.class,             "Сторка начислений"),
        UUID_PENALTY          (PenaltiesAndCourtCosts.class, "Неустоек / судебных расходов"),
        
        AMOUNT                (Type.NUMERIC, 13, 2, null,   "Размер превышения платы, рассчитанной с применением повышающего коэффициента над размером платы, рассчитанной без учета повышающего коэффициента, руб."),

        ;
        
        @Override
        public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}
                
    }

    public AcknowledgmentItem () {
        
        super  (TABLE_NAME, "Записи квитирования по строкам начислений");

        cols   (c.class);        

        key ("ack_chg", c.UUID_ACK.lc (), c.UUID_CHARGE.lc ());
        key ("ack_pnl", c.UUID_ACK.lc (), c.UUID_PENALTY.lc ());
        
        key (c.UUID_PAY);
        
        trigger ("BEFORE INSERT", ""

            + "BEGIN "                
            + "  SELECT UUID_PAY INTO :NEW.UUID_PAY FROM " + Acknowledgment.TABLE_NAME + " WHERE uuid = :NEW.UUID_ACK;"                    
            + "END;"

        );
        
        trigger ("AFTER INSERT OR UPDATE", ""
                
            + "DECLARE"
            + " PRAGMA AUTONOMOUS_TRANSACTION; "
	    + " l_sum NUMBER(20,2); "
            + "BEGIN "
                
            + "  SELECT SUM(AMOUNT) INTO l_sum FROM " + TABLE_NAME + " WHERE is_deleted = 0 AND UUID_ACK=:NEW.UUID_ACK AND uuid <> :NEW.UUID;"
            + "  IF l_sum IS NULL THEN l_sum := 0; END IF; "
            + "  IF :NEW.is_deleted = 0 AND :NEW.amount IS NOT NULL THEN l_sum := l_sum + :NEW.amount; END IF; "
            + "  UPDATE " + Acknowledgment.TABLE_NAME + " SET amount=l_sum WHERE uuid=:NEW.UUID_ACK; "
                

/*                
            + "  SELECT SUM(AMOUNT) INTO l_amount_ack FROM " + TABLE_NAME + " WHERE is_deleted = 0 AND UUID_PAY_DOC=:NEW.UUID_PAY_DOC AND uuid <> :NEW.UUID;"
            + "  IF l_amount_ack IS NULL THEN l_amount_ack := 0; END IF; "
            + "  IF :NEW.is_deleted = 0 THEN l_amount_ack := l_amount_ack + :NEW.amount; END IF; "
            + "  UPDATE " + PaymentDocument.TABLE_NAME + " SET amount_ack=l_amount_ack WHERE uuid=:NEW.UUID_PAY_DOC; "
                    
            + "  SELECT SUM(AMOUNT) INTO l_amount_ack FROM " + TABLE_NAME + " WHERE is_deleted = 0 AND UUID_PAY_DOC=:NEW.UUID_PAY AND uuid <> :NEW.UUID;"
            + "  IF l_amount_ack IS NULL THEN l_amount_ack := 0; END IF; "
            + "  IF :NEW.is_deleted = 0 THEN l_amount_ack := l_amount_ack + :NEW.amount; END IF; "
            + "  UPDATE " + Payment.TABLE_NAME + " SET amount_ack=l_amount_ack WHERE uuid=:NEW.UUID_PAY; "
*/                    
            + "  COMMIT;"
                    
            + "END;"
                
        );

    }    

}