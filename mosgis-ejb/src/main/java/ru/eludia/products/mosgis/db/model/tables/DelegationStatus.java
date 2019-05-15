package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.View;
import ru.eludia.products.mosgis.db.model.voc.VocDelegationStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class DelegationStatus extends View {

    private static final String TABLE_NAME = "vw_dlg_status";

    public enum c implements ColEnum {

        UUID          (VocOrganization.class,     "Организация"),
        ID_DLG_STATUS (VocDelegationStatus.class, "Статус"),
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

    }

    public DelegationStatus () {
        super (TABLE_NAME, "Статус делегирования организации");
        cols  (AccessRequest.c.class);
        cols  (c.class);
        pk    (AccessRequest.c.ACCESSREQUESTGUID);
    }

    @Override
    public String getSQL () {

        return "SELECT "
            + " o.uuid "
            + ", DECODE (MAX (t." + AnyAccessRequest.c.IS_ACTUAL.lc () + "), 1, 1, 0, -1, 0) " + c.ID_DLG_STATUS.lc ()
            + " FROM " + VocOrganization.TABLE_NAME + " o"
            + " LEFT JOIN " + AnyAccessRequest.TABLE_NAME + " t"
            + "     ON t.orgrootentityguid = o.orgrootentityguid"
            + " GROUP BY o.uuid"
        ; 
        
    }       

}