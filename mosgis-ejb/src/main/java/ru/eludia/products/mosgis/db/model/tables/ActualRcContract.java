package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.View;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class ActualRcContract extends View {

    public enum c implements ColEnum {
        ID                   (Type.UUID,   "id"),
        ORG_LABEL            (Type.STRING, "Исполнитель"),
        ORG_LABEL_UC         (Type.STRING, "ИСПОЛНИТЕЛЬ"),
        CUSTOMER_LABEL       (Type.STRING, "Заказчик"),
        CUSTOMER_LABEL_UC    (Type.STRING, "ЗАКАЗЧИК")
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

    }

    public ActualRcContract () {
        super  ("vw_rc_ctr", "Список договоров РЦ");
        cols   (EnTable.c.class);
        cols   (RcContract.c.class);
        cols   (c.class);
        pk     (c.ID);
    }

    @Override
    public final String getSQL () {

        StringBuilder sb = new StringBuilder ("SELECT ");

        for (EnTable.c i: EnTable.c.values ()) {
            sb.append ("o.");
            sb.append (i.lc ());
            sb.append (',');
        }

        for (RcContract.c i: RcContract.c.values ()) {
            sb.append ("o.");
            sb.append (i.lc ());
            sb.append (',');
        }

        return sb.toString ()
            + " o.uuid id "
            + " , org.label org_label "
            + " , org.label_uc org_label_uc "
            + " , org_customer.label customer_label"
            + " , UPPER(org_customer.label) customer_label_uc"
            + " FROM " + getName (RcContract.class) + " o"
            + " INNER JOIN " + getName (VocOrganization.class) + " org ON o.uuid_org = org.uuid"
            + " LEFT  JOIN " + getName (VocOrganization.class) + " org_customer ON o.uuid_org_customer = org_customer.uuid"
        ;

    }

}