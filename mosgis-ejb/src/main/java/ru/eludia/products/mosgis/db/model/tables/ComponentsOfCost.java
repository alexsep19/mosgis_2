package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.gosuslugi.dom.schema.integration.bills.PaymentDocumentType;

public class ComponentsOfCost extends EnTable {
    
    public static final String TABLE_NAME = "tb_comp_cost";
    
    public enum c implements EnColEnum {

        UUID_PAY_DOC          (PaymentDocument.class,                           "Платёжный документ"),

        CODE_VC_NSI_331       (Type.STRING,  20,    null,                       "Составляющая тарифа на электрическую энергию (НСИ 331)"),
        COST                  (Type.NUMERIC, 13, 2, null,                       "Итого к оплате за расчетный период, руб."),

        ID_LOG                (ComponentsOfCostLog.class,                       "Последнее событие редактирования"),

        ;
        
        @Override
        public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}
        
        @Override
        public boolean isLoggable () {
            switch (this) {
                case ID_LOG:
                case UUID_PAY_DOC:
                    return false;
                default:
                    return true;
            }
        }        
        
    }

    public ComponentsOfCost () {
        
        super  (TABLE_NAME, "Составляющие стоимости электрической энергии");
        cols   (c.class);        
        key    (c.UUID_PAY_DOC);
                
    }    
    
    static PaymentDocumentType.ComponentsOfCost toComponentsOfCost (Map<String, Object> t) {
        return new PaymentDocumentType.ComponentsOfCost ();
    }
    
}