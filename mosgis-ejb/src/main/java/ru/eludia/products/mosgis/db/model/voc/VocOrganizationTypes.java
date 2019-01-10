package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.DB;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;
import ru.gosuslugi.dom.schema.integration.organizations_registry_base.EntpsType;
import ru.gosuslugi.dom.schema.integration.organizations_registry_base.LegalType;
import ru.gosuslugi.dom.schema.integration.organizations_registry_base.ForeignBranchType;
import ru.gosuslugi.dom.schema.integration.organizations_registry_base.SubsidiaryType;

public class VocOrganizationTypes extends Table {

    public VocOrganizationTypes () {

        super ("vc_organization_types", "Типы организаций");

        pk    ("id",           Type.INTEGER, "Ключ");        
        col   ("label",        Type.STRING,  "Наименование");

        data  (i.class);

    }

    public enum i {

        ENTPS (-1, "Индивидуальный предприниматель"),
        LEGAL (1, "Юридическое лицо"),
        SUBSIDIARY (2, "Обособленное подразделение"),
        FOREIGN_BRANCH (3, "ФПИЮЛ (Филиал или представительство иностранного юридического лица)");

        byte id;
        String label;

        public byte getId () {
            return id;
        }

        public String getLabel () {
            return label;
        }

        private i (int id, String label) {
            this.id = (byte) id;
            this.label = label;
        }
        
        public static i forId (Object o) {
            Long iiiiid = DB.to.Long (o);
            if (iiiiid == null) return null;
            byte id = iiiiid.byteValue ();
            for (i i: values ()) if (i.id == id) return i;
            return null;
        }

        public static i valueOf (Object o) {
            if (o instanceof EntpsType) return ENTPS;
            if (o instanceof LegalType) return LEGAL;
            if (o instanceof SubsidiaryType) return SUBSIDIARY;
            if (o instanceof ForeignBranchType) return FOREIGN_BRANCH;
            throw new IllegalArgumentException ("Unknown org type: " + o.getClass ().getName ());
        }

    }

}