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

    }    

}