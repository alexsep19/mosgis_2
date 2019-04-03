package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import ru.eludia.base.DB;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.voc.VocOktmo;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationNsi20;

public class PremiseUsageTarifOktmo extends Table {

    public static final String TABLE_NAME = "tb_pu_tf_oktmo";

    public PremiseUsageTarifOktmo () {

        super (TABLE_NAME, "Территория действия тарифа Размер платы за пользование жилым помещением");

        pkref ("uuid",  PremiseUsageTarif.class, "Тариф Размер платы за пользование жилым помещением");
        pkref ("oktmo", VocOktmo.class, "ОКТМО");
    }

    public static void store(DB db, String id, List<Map<String, Object>> oktmo) throws SQLException {

	db.dupsert(
	    PremiseUsageTarifOktmo.class,
	    DB.HASH("uuid", id),
	    oktmo.stream()
		.map((t) -> {
		    return DB.HASH("oktmo", t.get("id"));
		})
		.collect(Collectors.toList()),
	    "oktmo"
	);
    }

    public static final String CHECK_PENDING_RQ_PLACING = ""

	+ " SELECT COUNT(*) INTO cnt FROM " + TABLE_NAME + " WHERE uuid=:NEW.uuid; "
	+ " IF cnt = 0 THEN "
	+ "   raise_application_error (-20000, 'Укажите территорию действия'); "
	+ " END IF; "

	+ " FOR i IN ("
	    + "SELECT "
	    + " vc_oktmo.code      label "
	    + " , org.label        org_label "
	    + "FROM "
	    + " tb_pu_tf_oktmo o "
	    + " INNER JOIN " + VocOktmo.TABLE_NAME + " vc_oktmo ON vc_oktmo.id = o.oktmo "
	    + " INNER JOIN " + Tarif.TABLE_NAME + " tf ON tf.id = o.uuid "
	    + " INNER JOIN " + VocOrganization.TABLE_NAME + " org ON org.uuid = tf.uuid_org "
	    + " INNER JOIN " + VocOrganizationNsi20.TABLE_NAME + " vc_orgs_nsi_20_oms ON "
	    + "     vc_orgs_nsi_20_oms.uuid = tf.uuid_org AND vc_orgs_nsi_20_oms.code = '10' "
	    + " LEFT JOIN " + VocOrganizationNsi20.TABLE_NAME + " vc_orgs_nsi_20_ogv ON "
	    + "     vc_orgs_nsi_20_ogv.uuid = tf.uuid_org AND vc_orgs_nsi_20_ogv.code IN ('7','9','10') "
	    + " LEFT JOIN vc_org_territories territory ON territory.uuid_org = org.uuid AND territory.oktmo = o.oktmo "
	    + "WHERE "
	    + " o.uuid = :NEW.uuid "
	    + " AND vc_orgs_nsi_20_ogv.uuid IS NULL "
	    + " AND territory.uuid_org IS NULL "
	+ ") LOOP "
	    + " raise_application_error (-20000, "
	    + "'Территория действия ' || i.label || ' не входит в территории организации ' || i.org_label "
	    + "); "
	+ " END LOOP; "
    ;
}