package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;

public class TarifLegalAct extends Table {

    public TarifLegalAct () {

        super ("tb_tf_legal_acts", "Тарифы: утверждающие нормативно-правовые акты");

        pk    ("uuid", Type.UUID, "Тариф");
        pkref ("uuid_legal_act",  LegalAct.class, "Утверждающий нормативно-правовой акт");
    }

    public static final String CHECK_PENDING_RQ_PLACING = ""
	+ " SELECT COUNT(*) INTO cnt FROM tb_tf_legal_acts WHERE uuid=:NEW.uuid; "
	+ " IF cnt = 0 THEN "
	+ "   raise_application_error (-20000, 'Прикрепите хотя бы один НПА, размещенный в ГИС ЖКХ'); "
	+ " END IF; "

	+ " FOR i IN ("
	    + "SELECT "
	    + " la.name     label "
	    + "FROM "
	    + " tb_tf_legal_acts o "
	    + " LEFT JOIN " + LegalAct.TABLE_NAME + " la ON la.uuid = o.uuid_legal_act "
	    + "WHERE "
	    + " o.uuid = :NEW.uuid "
	    + " AND la.documentguid IS NULL "
	+ ") LOOP "
	    + " raise_application_error (-20000, "
	    + "'НПА ' || i.label || ' не размещен в ГИС ЖКХ' "
	    + "); "
	+ " END LOOP; "
	+ "";
}