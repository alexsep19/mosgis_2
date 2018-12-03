package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.View;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;

public class ActualPublicPropertyContract extends View {

    public enum c implements ColEnum {
        ID                   (Type.UUID,   "id"),
        IS_CUSTOMER_ORG      (Type.BOOLEAN, "1 для организации, 0 для физлица"),
        ORG_LABEL            (Type.STRING, "Исполнитель"),
        ADDRESS              (Type.STRING, "Адрес"),
        ADDRESS_UC           (Type.STRING, "АДРЕС"),
        CUSTOMER_LABEL       (Type.STRING, "Заказчик"),
        CUSTOMER_LABEL_UC    (Type.STRING, "ЗАКАЗЧИК")
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

    }
         
    public ActualPublicPropertyContract () {    
        super  ("vw_pp_ctr", "Объекты уставов, определяющие права доступа в данный момент");
        cols   (EnTable.c.class);
        cols   (PublicPropertyContract.c.class);
        cols   (c.class);
        pk     (c.ID);
    }

    @Override
    public final String getSQL () {

        return "SELECT "
            + " o.*"
            + " , o.uuid id "
            + " , DECODE(o.uuid_org_customer, NULL, 0, 1) is_customer_org"
            + " , org.label org_label "
            + " , fias.label address "
            + " , fias.label_uc address_uc "
            + " , NVL (org_customer.label, prc_customer.label) AS customer_label"
            + " , UPPER(NVL (org_customer.label, prc_customer.label)) AS customer_label_uc"
            + " FROM " + getName (PublicPropertyContract.class) + " o"
            + " INNER JOIN " + getName (VocOrganization.class) + " org ON o.uuid_org = org.uuid"
            + " INNER JOIN " + getName (VocBuilding.class) + " fias ON o.fiashouseguid = fias.houseguid"
            + " LEFT  JOIN " + getName (VocOrganization.class) + " org_customer ON o.uuid_org_customer = org_customer.uuid"
            + " LEFT  JOIN " + getName (VocPerson.class) + " prc_customer ON o.uuid_person_customer = prc_customer.uuid"
            + " WHERE o.is_deleted = 0"
        ;

    }

}