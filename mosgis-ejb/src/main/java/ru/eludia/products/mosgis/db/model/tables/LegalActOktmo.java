package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.stream.Collectors;
import javax.json.JsonString;
import javax.json.JsonArray;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Select;
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

    public static Select select(DB db, Object id) throws SQLException {

	final Model m = db.getModel();

	return m
	    .select(VocOktmo.class, "AS vc_oktmo", "*")
	    .where(VocOktmo.c.ID.lc(), m
		.select(LegalActOktmo.class, "oktmo").where("uuid", id)
	    )
	    .orderBy("vc_oktmo.code")
	;
    }
}