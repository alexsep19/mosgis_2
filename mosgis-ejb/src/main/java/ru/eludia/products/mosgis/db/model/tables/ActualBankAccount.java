package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.View;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class ActualBankAccount extends View {
    
    public enum c implements ColEnum {
        
	UUID_ORG_CUSTOMER (VocOrganization.class, "Организация, способная указывать данный счёт как платёжный реквизит в договоре"),
        ;

        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}

    }    

    public ActualBankAccount () {
        super  ("vw_bnk_accts", "Список действующих платёжных реквизитов");
        cols   (EnTable.c.class);
        cols   (BankAccount.c.class);
        pk     (EnTable.c.UUID);
    }

    @Override
    public final String getSQL () {
        
        StringBuilder sb = new StringBuilder ();
        StringBuilder osb = new StringBuilder ();
        
        for (EnTable.c i: EnTable.c.values ()) {
            
            sb.append (',');
            sb.append (i.lc ());
            
            osb.append (',');
            osb.append ("o.");
            osb.append (i.lc ());
            
        }
        
        for (BankAccount.c i: BankAccount.c.values ()) {
            
            sb.append (',');
            sb.append (i.lc ());
            
            osb.append (',');
            osb.append ("o.");
            osb.append (i.lc ());
            
        }        

        return 
                
            "(SELECT " + BankAccount.c.UUID_ORG.lc () + " " + c.UUID_ORG_CUSTOMER.lc () + sb 
                + " FROM " + getName (BankAccount.class) 
                + " WHERE " + EnTable.c.IS_DELETED.lc () + "=0"

            + ") UNION" +
                
            "(SELECT c." + RcContract.c.UUID_ORG_CUSTOMER.lc () + " " + c.UUID_ORG_CUSTOMER.lc () + osb 
                + " FROM " + getName (BankAccount.class) + " o"
                + " INNER JOIN " + getName (ActualRcContract.class) + " c ON "
                    + "c." + RcContract.c.UUID_ORG.lc () + "=o." + BankAccount.c.UUID_ORG.lc ()
            + ")"

        ;
        
    }

}