package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import ru.eludia.base.DB;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocBuildingAddress;

public class TarifDiffFias extends Table {

    public TarifDiffFias () {

        super ("tb_tf_diff_fias", "Адрес ФИАС критерия дифференциации");

        pkref ("uuid",  TarifDiff.class, "Критерий дифференциации");
        pkref ("fiashouseguid", VocBuildingAddress.class, "Адрес");
    }

    public static void store(DB db, String id, List<Map<String, Object>> oktmo) throws SQLException {

	db.dupsert(TarifDiffFias.class,
	    DB.HASH("uuid", id),
	    oktmo.stream()
		.map((t) -> {
		    return DB.HASH("fiashouseguid", t.get("id"));
		})
		.collect(Collectors.toList()),
	    "fiashouseguid"
	);
    }
}