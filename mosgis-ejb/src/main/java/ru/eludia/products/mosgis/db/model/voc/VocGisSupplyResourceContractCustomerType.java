package ru.eludia.products.mosgis.db.model.voc;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;

public class VocGisSupplyResourceContractCustomerType extends Table {

    private static final String TABLE_NAME = "vc_gis_sr_customer_type";

    public VocGisSupplyResourceContractCustomerType () {

        super ("vc_gis_sr_customer_type", "Типы заказчиков в договорах ресурсоснабжения");

        pk    ("id",           Type.INTEGER, "Ключ");
        col   ("name",         Type.STRING,  "Идентификатор");
        col   ("label",        Type.STRING,  "Наименование");

        data  (i.class);

    }

    public enum i {

        OWNER               (1, "ApartmentBuildingOwner",               "Собственник (пользователь) помещений МКД"),
        REPRESENTATIVEOWNER (2, "ApartmentBuildingRepresentativeOwner", "Представитель собственников МКД"),
        SOLEOWNER           (3, "ApartmentBuildingSoleOwner",           "Собственник МКД"),
        LIVINGHOUSEOWNER    (4, "LivingHouseOwner",                     "Собственник (пользователь) помещений ЖД"),
        ORGANIZATION        (5, "Organization",                         "Исполнитель коммунальных услуг"),
        OFFER               (6, "Offer",                                "Договор оферта")
        ;

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

	public boolean getIsJur() { // TODO: as is_jur, is_phy
	    return this.id != VocGisSupplyResourceContractCustomerType.i.OFFER.getId();
	}

	public boolean getIsPhy() {
	    return this.id == VocGisSupplyResourceContractCustomerType.i.OWNER.getId()
		|| this.id == VocGisSupplyResourceContractCustomerType.i.REPRESENTATIVEOWNER.getId()
		|| this.id == VocGisSupplyResourceContractCustomerType.i.LIVINGHOUSEOWNER.getId()
	    ;
	}

	public static i forName(String name) {
	    for (i i : values()) {
		if (i.name.equals(name)) {
		    return i;
		}
	    }
	    return null;
	}

	public static i forLabel(String label) {
	    for (i i : values()) {
		if (i.label.equals(label)) {
		    return i;
		}
	    }
	    return null;
	}

	public static i forId(int id) {
	    for (i i : values()) {
		if (i.id == id) {
		    return i;
		}
	    }
	    return null;
	}

	public static i forId(Object id) {
	    return forId(Integer.parseInt(id.toString()));
	}

	@Override
	public String toString() {
	    return Integer.toString(id);
	}

	private i (int id, String name, String label) {
            this.id = (byte) id;
            this.name = name;
            this.label = label;
        }
    }

    private static JsonArray jsonArray;

    public static final void addTo(JsonObjectBuilder job) {
        job.add(TABLE_NAME, jsonArray);
    }

    static {

        JsonArrayBuilder builder = Json.createArrayBuilder();

        for (VocGisSupplyResourceContractCustomerType.i value : VocGisSupplyResourceContractCustomerType.i.values()) {

            builder.add(Json.createObjectBuilder()
                    .add("id", value.id)
                    .add("label", value.label)
            );
        }

        jsonArray = builder.build();
    }
}