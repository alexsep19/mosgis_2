package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.gosuslugi.dom.schema.integration.bills.PaymentDocumentType;

public class PenaltiesAndCourtCosts extends EnTable {
    
    public static final String TABLE_NAME = "tb_penalties";
    
    public enum c implements EnColEnum {
        
        UUID_PAY_DOC          (PaymentDocument.class,                           "Платёжный документ"),
        
        CODE_VC_NSI_329       (Type.STRING,  20,    null,                       "Вид неустойки и судебных расходов (НСИ 329)"),
        CAUSE                 (Type.STRING,  null,                              "Основания начислений"),
        TOTALPAYABLE          (Type.NUMERIC, 13, 2, null,                       "Итого к оплате за расчетный период, руб."),
        UUID_BNK_ACCT         (BankAccount.class, null,                         "Платёжные реквизиты"),
        
        ID_LOG                (PenaltiesAndCourtCostsLog.class,                 "Последнее событие редактирования"),

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

    public PenaltiesAndCourtCosts () {
        
        super  (TABLE_NAME, "Неустойки и судебные расходы");
        cols   (c.class);        
        key    (c.UUID_PAY_DOC);
                
    }    
    
    static PaymentDocumentType.PenaltiesAndCourtCosts toPenaltiesAndCourtCosts (Map<String, Object> r) {
        final PaymentDocumentType.PenaltiesAndCourtCosts result = DB.to.javaBean (PaymentDocumentType.PenaltiesAndCourtCosts.class, r);
        result.setServiceType (NsiTable.toDom (r, "vc_nsi_329"));
        return result;
    }    
    
}
