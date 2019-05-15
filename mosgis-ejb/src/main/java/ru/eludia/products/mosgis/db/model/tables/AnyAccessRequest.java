package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.View;
import ru.eludia.products.mosgis.db.model.voc.VocAccessRequestStatus;

public class AnyAccessRequest extends View {

    private static final String TABLE_NAME = "vw_acc_req";

    public enum c implements ColEnum {

        IS_ACTUAL (Type.BOOLEAN, "1 для актуальных записей, 0 для прочих"),
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

    }

    public AnyAccessRequest () {
        super (TABLE_NAME, "Заявки на предоставление доступа, с учётом статуса");
        cols  (AccessRequest.c.class);
        cols  (c.class);
        pk    (AccessRequest.c.ACCESSREQUESTGUID);
    }

    @Override
    public String getSQL () {
        
        StringBuilder sb = new StringBuilder ("SELECT ");
        
        for (AccessRequest.c i: AccessRequest.c.values ()) {
            sb.append ("o.");
            sb.append (i.lc ());
            sb.append (',');
        }
                
        return sb.toString ()
                
        + " CASE "
        + "  WHEN STATUS <> " + VocAccessRequestStatus.i.ACCEPTED.getId () + " THEN 0 "
        + "  WHEN STARTDATE > SYSDATE THEN 0 "
        + "  WHEN ENDDATE < SYSDATE THEN 0 "
        + "  ELSE 1 "
        + " END " + c.IS_ACTUAL.lc ()
                
        + " FROM " + AccessRequest.TABLE_NAME + " o"
                
        ;
        
    }       

}