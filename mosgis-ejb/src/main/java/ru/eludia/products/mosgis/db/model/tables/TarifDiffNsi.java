package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import ru.eludia.base.DB;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;

public class TarifDiffNsi extends Table {

    public TarifDiffNsi () {

        super ("tb_tf_diff_nsi", "Значения перечислимого типа критериев дифференциации");

        pkref ("uuid",  TarifDiff.class, "Критерий дифференциации");
        col   ("code_vc_nsi", Type.STRING, 20, null, "Код элемента справочника");
    }

    public static void store(DB db, String id, List<Map<String, Object>> nsi_elements) throws SQLException {

	db.dupsert(TarifDiffNsi.class,
	    DB.HASH("uuid", id),
	    nsi_elements.stream()
		.map((t) -> {
		    return DB.HASH("code_vc_nsi", t.get("code"));
		})
		.collect(Collectors.toList()),
	    "code_vc_nsi"
	);
    }
}