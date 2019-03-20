package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.stream.Collectors;
import javax.json.JsonString;
import javax.json.JsonArray;
import javax.json.JsonObject;
import ru.eludia.base.DB;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.voc.VocOktmo;

public class LegalActOktmo extends Table {

    public LegalActOktmo () {

        super ("tb_legal_act_oktmo", "Области действия нормативно-правовых документов");

        pkref ("uuid",  LegalAct.class, "НПА");
        pkref ("oktmo", VocOktmo.class, "ОКТМО");
    }

    public static void store(DB db, Object id, JsonArray oktmo) throws SQLException {

	db.dupsert(
	    LegalActOktmo.class,
	    DB.HASH("uuid", id),
	    oktmo.getValuesAs(JsonString.class).stream()
		.map((t) -> {
		    return DB.HASH("oktmo", t.getString());
		})
		.collect(Collectors.toList()),
	    "oktmo"
	);
    }
}