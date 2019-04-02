package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.View;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.voc.VocBic;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.ModelHolder;

public class ActualBankAccount extends View {
    
    public static final String TABLE_NAME = "vw_bnk_accts";
       
    public enum c implements ColEnum {
        
	UUID_ORG_CUSTOMER (VocOrganization.class, "Организация, способная указывать данный счёт как платёжный реквизит в договоре"),
        LABEL             (Type.STRING,           "Наименование"),
        ;

        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}

    }    

    public ActualBankAccount () {
        super  (TABLE_NAME, "Список действующих платёжных реквизитов");
        cols   (c.class);
        cols   (EnTable.c.class);
        cols   (BankAccount.c.class);
        pk     (EnTable.c.UUID);
    }

 @Override
    public final String getSQL () {
        
        StringBuilder osb = new StringBuilder (" ");
        osb.append (c.UUID_ORG_CUSTOMER.lc ());
        osb.append (", o.");
        osb.append (BankAccount.c.ACCOUNTNUMBER.lc ());
        osb.append (" || ' ' ||");
        osb.append (VocBic.c.NAMEP.lc ());
        osb.append (" ");
        osb.append (c.LABEL.lc ());
        
        for (EnTable.c i: EnTable.c.values ()) {
            osb.append (',');
            osb.append ("o.");
            osb.append (i.lc ());
        }
        
        for (BankAccount.c i: BankAccount.c.values ()) {
            osb.append (',');
            osb.append ("o.");
            osb.append (i.lc ());
        }        

        return 
                
            "(SELECT " + BankAccount.c.UUID_ORG.lc () + osb
                + " FROM " + BankAccount.TABLE_NAME + " o"
                + " INNER JOIN " + VocBic.TABLE_NAME + " v ON o. " + BankAccount.c.BIKCREDORG + "=v." + VocBic.c.BIC.lc ()
                + " WHERE o." + EnTable.c.IS_DELETED.lc () + "=0"

            + ") UNION" +

            "(SELECT c." + RcContract.c.UUID_ORG_CUSTOMER.lc () + osb
                + " FROM " + BankAccount.TABLE_NAME + " o"
                + " INNER JOIN " + VocBic.TABLE_NAME + " v ON o. " + BankAccount.c.BIKCREDORG + "=v." + VocBic.c.BIC.lc ()
                + " INNER JOIN " + getName (ActualRcContract.class) + " c ON "
                    + "c." + RcContract.c.UUID_ORG.lc () + "=o." + BankAccount.c.UUID_ORG.lc ()
                    + " AND o." + EnTable.c.IS_DELETED.lc () + "=0"
            + ")"

        ;
        
    }
    
    public static Select select (Object uuidOrg) {
        
        final MosGisModel m = ModelHolder.getModel ();
        
        return m.select (ActualBankAccount.class, "AS root", "*", "uuid AS id")
            .toOne (VocOrganization.class, "AS org", VocOrganization.c.LABEL.lc ()).on ("root.uuid_org=org.uuid")
            .toMaybeOne (VocBic.class, "AS bank", "*").on ()
            .orderBy ("root.accountnumber")
            .and (c.UUID_ORG_CUSTOMER, uuidOrg)
        ;        
        
    }
    
    public static void addTo (JsonObjectBuilder job, DB db, Object uuidOrg) throws SQLException {       
        job.add (BankAccount.TABLE_NAME, db.getJsonArray (select (uuidOrg)));
    }

}