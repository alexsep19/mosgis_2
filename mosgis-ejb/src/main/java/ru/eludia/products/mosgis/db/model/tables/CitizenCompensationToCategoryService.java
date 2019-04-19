package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.json.JsonString;
import javax.json.JsonArray;
import ru.eludia.base.DB;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.voc.VocServiceType;

public class CitizenCompensationToCategoryService extends Table {

    public CitizenCompensationToCategoryService () {

        super ("tb_cit_comp_to_cat_service", "Услуги категорий граждан");

        pkref ("uuid",  CitizenCompensationToCategory.class, "Категория");
        pkref ("id_service", VocServiceType.class, "Услуга");
    }
    
    public static void store(DB db, Object id, JsonArray services) throws SQLException {

	db.dupsert(
	    CitizenCompensationToCategoryService.class,
	    DB.HASH("uuid", id),
	    services.getValuesAs(JsonString.class).stream()
		.map((t) -> {
		    return DB.HASH("id_service", t.getString());
		})
		.collect(Collectors.toList()),
	    "id_service"
	);
    }
}