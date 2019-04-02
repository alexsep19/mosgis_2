package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import ru.eludia.base.DB;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.voc.VocOktmo;

public class SocialNormTarifOktmo extends Table {

    public SocialNormTarifOktmo () {

        super ("tb_sn_tf_oktmo", "Территория действия тарифа Социальная норма потребления электрической энергии");

        pkref ("uuid",  SocialNormTarif.class, "Тариф Социальная норма потребления электрической энергии");
        pkref ("oktmo", VocOktmo.class, "ОКТМО");
    }

    public static void store(DB db, String id, List<Map<String, Object>> oktmo) throws SQLException {

	db.dupsert(
	    SocialNormTarifOktmo.class,
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