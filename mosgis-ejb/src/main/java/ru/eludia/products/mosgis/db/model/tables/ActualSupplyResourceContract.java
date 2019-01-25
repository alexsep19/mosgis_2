package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.View;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract;
import ru.eludia.products.mosgis.db.model.voc.VocGisSupplyResourceContractCustomerType;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;

public class ActualSupplyResourceContract extends View {

    public enum c implements ColEnum {
        ID                   (Type.UUID,   "id"),
        IS_CUSTOMER_ORG      (Type.BOOLEAN, "1 для организации, 0 для физлица"),
        ORG_LABEL            (Type.STRING, "Исполнитель"),
        ORG_LABEL_UC         (Type.STRING, "ИСПОЛНИТЕЛЬ"),
        CUSTOMER_TYPE_LABEL  (Type.STRING, "Тип Заказчика"),
        CUSTOMER_LABEL       (Type.STRING, "Заказчик"),
        CUSTOMER_LABEL_UC    (Type.STRING, "ЗАКАЗЧИК")
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

    }

    public ActualSupplyResourceContract () {
        super  ("vw_sr_ctr", "Объекты договоров ресурсоснабжения, определяющие права доступа в данный момент");
        cols   (EnTable.c.class);
        cols   (SupplyResourceContract.c.class);
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

        for (SupplyResourceContract.c i: SupplyResourceContract.c.values ()) {
            sb.append ("o.");
            sb.append (i.lc ());
            sb.append (',');
        }

        return sb.toString ()
            + " o.uuid id "
            + " , DECODE(o.uuid_org_customer, NULL, 0, 1) is_customer_org"
            + " , org.label org_label "
            + " , org.label_uc org_label_uc "
            + " , customer_types.label customer_type_label"
            + " , NVL (org_customer.label, prc_customer.label) customer_label"
            + " , UPPER(NVL (org_customer.label, prc_customer.label)) customer_label_uc"
            + " FROM " + getName (SupplyResourceContract.class) + " o"
            + " INNER JOIN " + getName (VocOrganization.class) + " org ON o.uuid_org = org.uuid"
            + " LEFT  JOIN " + getName (VocOrganization.class) + " org_customer ON o.uuid_org_customer = org_customer.uuid"
            + " LEFT  JOIN " + getName (VocPerson.class) + " prc_customer ON o.uuid_person_customer = prc_customer.uuid"
            + " LEFT  JOIN " + getName(VocGisSupplyResourceContractCustomerType.class) + " customer_types ON o.id_customer_type = customer_types.id "
        ;

    }

}