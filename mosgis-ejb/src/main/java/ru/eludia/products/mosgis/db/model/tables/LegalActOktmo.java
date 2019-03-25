package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.json.JsonString;
import javax.json.JsonArray;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.voc.VocOktmo;
import ru.gosuslugi.dom.schema.integration.base.OKTMORefType;

public class LegalActOktmo extends Table {

    public LegalActOktmo () {

        super ("tb_legal_act_oktmo", "Области действия нормативно-правовых документов");

        pkref ("uuid",  LegalAct.class, "НПА");
        pkref ("oktmo", VocOktmo.class, "ОКТМО");
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

    public static List<Map<String, Object>> toHashList(List<OKTMORefType> oktmos) {

	List<Map<String, Object>> result = new ArrayList<>();

	for (OKTMORefType o : oktmos) {

	    Map<String, Object> legalActOktmo = DB.HASH(
		"code", o.getCode(),
		"name", o.getName()
	    );
	    result.add(legalActOktmo);
	}

	return result;
    }

    public static void store(DB db, List<Map<String, Object>> oktmos) throws SQLException {

	final Model m = ModelHolder.getModel();
	final String code = VocOktmo.c.CODE.lc();
	final String id = VocOktmo.c.ID.lc();

	Map<Object, Map<String, Object>> oktmoIdx = db.getIdx(m.select(VocOktmo.class, code, id), code);

	Map<Object, List<Map<String, Object>>> idx = new HashMap<Object,List<Map<String, Object>>>();

	for (Map<String, Object> i : oktmos) {

	    if (i.get("id") == null && i.get(code) != null) {
		Map<String, Object> o = oktmoIdx.get(i.get(code));
		if (o == null) {
		    continue;
		}
		i.put("id", o.get(id));
	    }

	    List<Map<String, Object>> uuid_oktmos = idx.get(i.get("uuid"));
	    if (uuid_oktmos == null) {
		uuid_oktmos = new ArrayList<Map<String, Object>>();
		idx.put(i.get("uuid"), uuid_oktmos);
	    }
	    uuid_oktmos.add(i);
	}

	for (Map.Entry<Object, List<Map<String, Object>>> entry : idx.entrySet()) {
	    store (db, entry.getKey(), entry.getValue());
	}
    }

    public static void store(DB db, Object id, JsonArray oktmo) throws SQLException {

	store(
	    db,
	    id,
	    oktmo.getValuesAs(JsonString.class).stream()
		.map((t) -> {
		    return DB.HASH("id", t.getString());
		})
		.collect(Collectors.toList())
	);
    }

    public static void store(DB db, Object id, List<Map<String, Object>> oktmo) throws SQLException {

	db.dupsert(
	    LegalActOktmo.class,
	    DB.HASH("uuid", id),
	    oktmo.stream()
		.map((t) -> {
		    return DB.HASH("oktmo", t.get("id"));
		})
		.collect(Collectors.toList()),
	    "oktmo"
	);
    }
}