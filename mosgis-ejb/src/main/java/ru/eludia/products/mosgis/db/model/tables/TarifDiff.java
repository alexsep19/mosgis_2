package ru.eludia.products.mosgis.db.model.tables;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocDifferentiation;
import ru.eludia.products.mosgis.db.model.voc.VocDifferentiationOperator;
import ru.eludia.products.mosgis.db.model.voc.VocDifferentiationValueKindType;
import ru.eludia.products.mosgis.db.model.voc.VocOktmo;
import ru.gosuslugi.dom.schema.integration.tariff.DifferentiationType;
import ru.gosuslugi.dom.schema.integration.tariff.ValueEnumerationType;
import ru.gosuslugi.dom.schema.integration.tariff.ValueMultilineType;
import ru.gosuslugi.dom.schema.integration.tariff.ValueOKTMOType;

public class TarifDiff extends EnTable  {

    public static final String TABLE_NAME = "tb_tf_diffs";

    public enum c implements EnColEnum {

	UUID_TF                (Type.UUID, null, "Тариф"),

	CODE_DIFF              (VocDifferentiation.class, "Критерий дифференциации"),

	ID_TYPE                (VocDifferentiationValueKindType.class, "Тип значения"),
	OPERATOR               (VocDifferentiationOperator.class, "Оператор"),

	LABEL                  (Type.STRING, 500, null, "Значение типа многострочное: наименование"),
	VALUESTRING            (Type.STRING, 4000, null, "Значение типа строка, многострочное"),

	VALUEREAL              (Type.NUMERIC, 13, 3, null, "Значение типа вещественное число, целое число, год"),
	VALUEREAL_TO           (Type.NUMERIC, 13, 3, null, "Значение по типа вещественное число, целое число, год"),

	VALUEINTEGER           (Type.INTEGER, null, "Значение типа целое число"),
	VALUEINTEGER_TO        (Type.INTEGER, null, "Значение по типа целое число"),

	VALUEYEAR              (Type.NUMERIC, 4, null, "Значение типа год"),
	VALUEYEAR_TO           (Type.NUMERIC, 4, null, "Значение по типа год"),

	VALUEDATE              (Type.DATE, null, "Значение типа дата"),
	VALUEDATE_TO           (Type.DATE, null, "Значение типа дата по"),

	VALUEBOOLEAN           (Type.BOOLEAN, null, "Значение типа логическое")
        ;

        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}
        @Override
        public boolean isLoggable () {
            return false;
        }

    }

    public TarifDiff () {

	super  (TABLE_NAME, "Тарифы: значения критериев дифференциации");

	cols   (c.class);

	key    (c.UUID_TF);

	trigger("BEFORE INSERT OR UPDATE", ""
	    + "DECLARE"
	    + " PRAGMA AUTONOMOUS_TRANSACTION; "
	    + "BEGIN "
	    + "  IF :NEW.is_deleted = 0 THEN BEGIN "

	    + "     IF :NEW.operator NOT IN ('"
	    +        VocDifferentiationOperator.i.RANGE
	    +         "','" + VocDifferentiationOperator.i.EXCLUDINGRANGE + "'"
	    + "     ) THEN "
	    + "       :NEW.valuereal_to := NULL; "
	    + "       :NEW.valueinteger_to := NULL; "
	    + "       :NEW.valueyear_to := NULL; "
	    + "       :NEW.valuedate_to := NULL; "
	    + "     END IF; "

		+ " FOR i IN ("
		    + "SELECT "
		    + " tf.label "
		    + "FROM "
		    + TABLE_NAME + " o "
		    + "LEFT JOIN " + Tarif.TABLE_NAME + " tf ON tf.id = o.uuid_tf "
		    + "WHERE o.is_deleted = 0 "
		    + " AND o.uuid <> :NEW.uuid "
		    + " AND o.code_diff = :NEW.code_diff "
		    + " AND o.uuid_tf   = :NEW.uuid_tf "
		    + " AND NVL(o.operator, '00') = NVL(:NEW.operator, '00') "
		    + " AND o.valuereal = :NEW.valuereal "
		    + " AND NVL(o.valuereal_to, 0) = NVL(:NEW.valuereal_to, 0) "
		    + " AND o.id_type IN ('"
		    +  VocDifferentiationValueKindType.i.REAL
		    + "') "
		+ ") LOOP"
		    + " raise_application_error (-20000, "
		    + "'Критерий дифференциации с таким же оператором и вещественным значением уже указан в этом тарифе' "
		    + "); "
		+ " END LOOP; "

		+ " FOR i IN ("
		    + "SELECT "
		    + " tf.label "
		    + "FROM "
		    + TABLE_NAME + " o "
		    + "LEFT JOIN " + Tarif.TABLE_NAME + " tf ON tf.id = o.uuid_tf "
		    + "WHERE o.is_deleted = 0 "
		    + " AND o.uuid <> :NEW.uuid "
		    + " AND o.code_diff = :NEW.code_diff "
		    + " AND o.uuid_tf   = :NEW.uuid_tf "
		    + " AND NVL(o.operator, '00') = NVL(:NEW.operator, '00') "
		    + " AND o.valueinteger = :NEW.valueinteger "
		    + " AND NVL(o.valueinteger_to, 0) = NVL(:NEW.valueinteger_to, 0) "
		    + " AND o.id_type IN ('"
		    + VocDifferentiationValueKindType.i.INTEGER
		    + "') "
		+ ") LOOP"
		    + " raise_application_error (-20000, "
		    + "'Критерий дифференциации с таким же оператором и целым значением уже указан в этом тарифе' "
		    + "); "
		+ " END LOOP; "

		+ " FOR i IN ("
		    + "SELECT "
		    + " tf.label "
		    + "FROM "
		    + TABLE_NAME + " o "
		    + "LEFT JOIN " + Tarif.TABLE_NAME + " tf ON tf.id = o.uuid_tf "
		    + "WHERE o.is_deleted = 0 "
		    + " AND o.uuid <> :NEW.uuid "
		    + " AND o.code_diff = :NEW.code_diff "
		    + " AND o.uuid_tf   = :NEW.uuid_tf "
		    + " AND NVL(o.operator, '00') = NVL(:NEW.operator, '00') "
		    + " AND o.valueyear = :NEW.valueyear "
		    + " AND NVL(o.valueyear_to, 0) = NVL(:NEW.valueyear_to, 0) "
		    + " AND o.id_type IN ('"
		    + VocDifferentiationValueKindType.i.YEAR
		    + "') "
		+ ") LOOP"
		    + " raise_application_error (-20000, "
		    + "'Критерий дифференциации с таким же оператором и значением года уже указан в этом тарифе' "
		    + "); "
		+ " END LOOP; "

		+ " FOR i IN ("
		    + "SELECT "
		    + " tf.label "
		    + "FROM "
		    + TABLE_NAME + " o "
		    + "LEFT JOIN " + Tarif.TABLE_NAME + " tf ON tf.id = o.uuid_tf "
		    + "WHERE o.is_deleted = 0 "
		    + " AND o.uuid <> :NEW.uuid "
		    + " AND o.code_diff = :NEW.code_diff "
		    + " AND o.uuid_tf   = :NEW.uuid_tf "
		    + " AND o.valuedate = :NEW.valuedate "
		    + " AND NVL(o.valuedate_to, TO_DATE('3999-12-31', 'YYYY-MM-DD')) = NVL(:NEW.valuedate_to, TO_DATE('3999-12-31', 'YYYY-MM-DD')) "
		    + " AND o.id_type IN ('"
		    + VocDifferentiationValueKindType.i.DATE
		    + "') "
		+ ") LOOP"
		    + " raise_application_error (-20000, "
		    + "'Критерий дифференциации с таким же значением даты уже указан в этом тарифе' "
		    + "); "
		+ " END LOOP; "

		+ " FOR i IN ("
		    + "SELECT "
		    + " tf.label "
		    + "FROM "
		    + TABLE_NAME + " o "
		    + "LEFT JOIN " + Tarif.TABLE_NAME + " tf ON tf.id = o.uuid_tf "
		    + "WHERE o.is_deleted = 0 "
		    + " AND o.uuid <> :NEW.uuid "
		    + " AND o.code_diff = :NEW.code_diff "
		    + " AND o.uuid_tf   = :NEW.uuid_tf "
		    + " AND o.valueboolean = :NEW.valueboolean "
		    + " AND o.id_type IN ('"
		    +  VocDifferentiationValueKindType.i.BOOLEAN
		    + "') "
		+ ") LOOP"
		    + " raise_application_error (-20000, "
		    + "'Критерий дифференциации с таким же логическим значением уже указан в этом тарифе' "
		    + "); "
		+ " END LOOP; "

		+ " FOR i IN ("
		    + "SELECT "
		    + " tf.label "
		    + "FROM "
		    + TABLE_NAME + " o "
		    + "LEFT JOIN " + Tarif.TABLE_NAME + " tf ON tf.id = o.uuid_tf "
		    + "WHERE o.is_deleted = 0 "
		    + " AND o.uuid <> :NEW.uuid "
		    + " AND o.code_diff = :NEW.code_diff "
		    + " AND o.uuid_tf   = :NEW.uuid_tf "
		    + " AND o.valuestring = :NEW.valuestring "
		    + " AND o.label = :NEW.label "
		    + " AND o.id_type IN ('"
		    +  VocDifferentiationValueKindType.i.MULTILINE
		    + "') "
		+ ") LOOP"
		    + " raise_application_error (-20000, "
		    + "'Критерий дифференциации с таким же многострочным значением уже указан в этом тарифе' "
		    + "); "
		+ " END LOOP; "
	    + " END; END IF; " // IF :NEW.is_deleted = 0
	    + "END;");
    }

    public static DifferentiationType toDifferentiation(Map<String, Object> i) {

	VocDifferentiationValueKindType.i id_type = VocDifferentiationValueKindType.i.forId(i.get(TarifDiff.c.ID_TYPE.lc()));

	Object op = i.get(TarifDiff.c.OPERATOR.lc());

	VocDifferentiationOperator.i operator = DB.ok(op)? VocDifferentiationOperator.i.forId(op) : null;

	if (!VocDifferentiationValueKindType.i.BOOLEAN.equals(id_type)) {
	    i.put("valueboolean", null);
	}


	boolean is_range = VocDifferentiationOperator.i.RANGE.equals(operator) || VocDifferentiationOperator.i.EXCLUDINGRANGE.equals(operator);

	if (is_range) {
	    String type = id_type.toString().toLowerCase();
	    i.put("value" + type + "1", i.get("value" + type));
	    i.put("value" + type + "2", i.get("value" + type + "_to"));
	    i.put("value" + type, null);
	}

	DifferentiationType result = DB.to.javaBean(DifferentiationType.class, i);

	result.setDifferentiationCode(BigInteger.valueOf((Long)i.get("code_diff")));

	if (DB.ok(operator)) {
	    if (is_range && !VocDifferentiationValueKindType.i.ENUMERATION.equals(id_type)) {
		result.setOperator2(operator.toString());
	    } else {
		switch(id_type){
		    case DATE:
		    case INTEGER:
		    case REAL:
		    case YEAR:
			result.setOperator1(operator.toString());
			break;
		    case ENUMERATION:
			result.setOperator3(operator.toString());
		}
	    }
	}

	switch(id_type){
	    case DATE:
	    case YEAR:
	    case REAL:
	    case INTEGER:
	    case BOOLEAN:
	    case STRING:
		return result;
	    case MULTILINE:
		i.put("name", i.get(TarifDiff.c.LABEL.lc()));
		i.put("value", i.get(TarifDiff.c.VALUESTRING.lc()));
		result.setValueMultiline(DB.to.javaBean(ValueMultilineType.class, i));
		result.setValueString(null);
		return result;
	    case FIAS:
		result.getValueFIAS().addAll((List<String>)i.get("fias"));
		return result;
	    case OKTMO:
		for (String o : (List<String>) i.get("oktmo")) {
		    ValueOKTMOType oktmo = new ValueOKTMOType ();
		    oktmo.setCode(o);
		    result.getValueOKTMO().add(oktmo);
		}
		return result;
	    case ENUMERATION:
		for (Map<String, Object> nsi : (List <Map<String, Object>>) i.get("enumeration")) {
		    ValueEnumerationType val = new ValueEnumerationType();
		    val.setGUID(DB.to.String(nsi.get("enumeration.guid")));
		    val.setCode(DB.to.String(nsi.get("enumeration.code")));
		    result.getValueEnumeration().add(val);
		}
		return result;
	}

	return null;
    }

    public static List<Map<String, Object>> getForExportDifferentiation(DB db, String id) throws SQLException {

	final Model m = db.getModel();

	Select diffsSelect = m
	    .select(TarifDiff.class, "AS root", "*")
	    .where(TarifDiff.c.UUID_TF.lc(), id)
	    .and(EnTable.c.IS_DELETED, 0)
	;

	List <Map<String, Object>> diffs = new ArrayList();

	Map<Object, Map<String, Object>> uuid2diff = new HashMap();

	db.forEach(diffsSelect, (rs) -> {
	    Map<String, Object> i = db.HASH(rs);
	    diffs.add(i);
	    uuid2diff.put(i.get("uuid"), i);

	    VocDifferentiationValueKindType.i id_type = VocDifferentiationValueKindType.i.forId(i.get(TarifDiff.c.ID_TYPE.lc()));

	    if (VocDifferentiationValueKindType.i.FIAS.equals(id_type)) {
		i.put("fias", new ArrayList());
	    }
	    if (VocDifferentiationValueKindType.i.ENUMERATION.equals(id_type)) {
		i.put("enumeration", new ArrayList());
	    }
	    if (VocDifferentiationValueKindType.i.OKTMO.equals(id_type)) {
		i.put("oktmo", new ArrayList());
	    }
	});

	db.forEach(m
	    .select(TarifDiffFias.class, "*")
	    .where("uuid IN", m
		.select(TarifDiff.class, "uuid")
		.where(TarifDiff.c.UUID_TF.lc(), id)
		.and(EnTable.c.IS_DELETED, 0)
	    )
	    , (rs) -> {
		Map<String, Object> i = db.HASH(rs);
		Map<String, Object> t = uuid2diff.get(i.get("uuid"));
		((List<String>)t.get("fias")).add(DB.to.String(i.get("fiashouseguid")));
	    }
	);

	db.forEach(m
	    .select(TarifDiffOktmo.class, "*")
	    .toOne(VocOktmo.class, "AS o", "code").on()
	    .where("uuid IN", m
		.select(TarifDiff.class, "uuid")
		.where(TarifDiff.c.UUID_TF.lc(), id)
		.and(EnTable.c.IS_DELETED, 0)
	    )
	    , (rs) -> {
		Map<String, Object> i = db.HASH(rs);
		Map<String, Object> t = uuid2diff.get(i.get("uuid"));
		((List<String>)t.get("oktmo")).add(DB.to.String(i.get("o.code")));
	    }
	);

	Map<Object, Map<String, Object>> nsi2o = db.getIdx(m
	    .select(TarifDiff.class, "AS root", "*")
	    .toOne(PremiseUsageTarif.class, "AS tf", "*").on("tf.uuid = root.uuid_tf")
	    .toOne(VocDifferentiation.class, "AS vc_diff", "*").where(VocDifferentiation.c.NSIITEM.lc() + " IS NOT NULL").on()
	    .where(TarifDiff.c.UUID_TF.lc(), id)
	    .and(EnTable.c.IS_DELETED, 0)
	    , "vc_diff.nsiitem"
	);

	for (Map.Entry<Object, Map<String, Object>> entry : nsi2o.entrySet()) {

	    Long rnum = DB.to.Long(entry.getKey());

	    Integer registrynumber = rnum.intValue();

	    NsiTable nsiTable = NsiTable.getNsiTable(registrynumber);

	    db.forEach(
		m.select(TarifDiffNsi.class, "AS root", "*")
		    .toOne(nsiTable, "AS enumeration", "code", "guid")
			.where("isactual", 1)
			.on("root.code_vc_nsi = enumeration.code")
		    .where("uuid IN", m
			.select(TarifDiff.class, "uuid")
			.where(TarifDiff.c.UUID_TF.lc(), id)
			.and(EnTable.c.IS_DELETED, 0)
		    )
		, (rs) -> {
		    Map<String, Object> i = db.HASH(rs);
		    Map<String, Object> t = uuid2diff.get(i.get("uuid"));
		    ((List<Map <String, Object>>) t.get("enumeration")).add(i);
		}
	    );
	}

	return diffs;
    }
}