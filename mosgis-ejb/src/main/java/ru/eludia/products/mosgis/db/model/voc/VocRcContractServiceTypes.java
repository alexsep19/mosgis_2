package ru.eludia.products.mosgis.db.model.voc;

import java.util.UUID;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;
import ru.gosuslugi.dom.schema.integration.house_management.ContractType;
import static ru.eludia.products.mosgis.db.model.voc.VocOrganization.regOrgType;

public class VocRcContractServiceTypes extends Table {

    private static final String TABLE_NAME = "vc_rc_ctr_service_types";

    public VocRcContractServiceTypes () {
        
        super (TABLE_NAME, "Типы услуг в договорах РЦ");
        
        pk    ("id",           Type.INTEGER, "Ключ");
        col   ("label",        Type.STRING,  "Наименование");
        
        data  (i.class);

    }
    
    public enum i {

	BILLING              (1, "расчетно-кассовое обслуживание"),
	INFO                 (2, "расчетно-информационное обслуживание")
	;

	byte id;
	String label;

	public byte getId() {
	    return id;
	}

	public String getLabel() {
	    return label;
	}

	public ru.eludia.base.model.def.Num asDef() {
	    return new ru.eludia.base.model.def.Num(id);
	}

	private i (int id, String label) {
            this.id = (byte) id;
            this.label = label;
        }
    }

    private static JsonArray jsonArray;

    static {

	JsonArrayBuilder builder = Json.createArrayBuilder();

	for (VocRcContractServiceTypes.i value : VocRcContractServiceTypes.i.values()) {
	    builder.add(Json.createObjectBuilder()
		.add("id", value.id)
		.add("label", value.label)
	    );
	}

	jsonArray = builder.build();

    }

    public static final void addTo(JsonObjectBuilder job) {
	job.add(TABLE_NAME, jsonArray);
    }
}