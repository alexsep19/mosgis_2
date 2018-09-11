package ru.eludia.products.mosgis.db.model.voc;

import java.util.UUID;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;
import ru.gosuslugi.dom.schema.integration.house_management.ContractType;
import static ru.eludia.products.mosgis.db.model.voc.VocOrganization.regOrgType;

public class VocGisCustomerType extends Table {

    public VocGisCustomerType () {
        
        super ("vc_gis_customer_type", "Типы заказчиков в договорах управления");
        
        pk    ("id",           Type.INTEGER, "Ключ");        
        col   ("name",         Type.STRING,  "Идентификатор");
        col   ("label",        Type.STRING,  "Наименование");
        
        data  (i.class);

    }
    
    public enum i {

        OWNERS             (1, "Owners",           "Собственник объекта жилищного фонда"),
        COOPERATIVE        (2, "Cooperative",      "ТСЖ/Кооператив"),
        MUNICIPAL_HOUSING  (3, "MunicipalHousing", "Собственник муниципального жилья"),
        BUILDINGOWNER      (4, "BuildingOwner",    "Застройщик");

        byte id;
        String name;
        String label;

        public byte getId () {
            return id;
        }

        public String getName () {
            return name;
        }

        public String getLabel () {
            return label;
        }

        private i (int id, String name, String label) {
            this.id = (byte) id;
            this.name = name;
            this.label = label;            
        }
        
        public static i forId (Object v) {
            byte b = Byte.parseByte (v.toString ());
            for (i i: values ()) if (i.id == b) return i;
            throw new IllegalArgumentException ("Invalid VocGisCustomerType id: " + v);
        }
        
        public final void setCustomer (ContractType c, UUID uuid) {

            switch (this) {

                case OWNERS:
                    c.setOwners (Boolean.TRUE);
                    break;
                case COOPERATIVE:
                    c.setCooperative (regOrgType (uuid));
                    break;
                case MUNICIPAL_HOUSING:
                    c.setMunicipalHousing (regOrgType (uuid));
                    break;
                case BUILDINGOWNER:
                    c.setBuildingOwner (regOrgType (uuid));
                    break;            

            }

        }

    }
    
}