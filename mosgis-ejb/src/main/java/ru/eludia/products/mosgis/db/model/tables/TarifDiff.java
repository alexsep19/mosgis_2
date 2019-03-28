package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocDifferentiation;
import ru.eludia.products.mosgis.db.model.voc.VocDifferentiationOperator;
import ru.eludia.products.mosgis.db.model.voc.VocDifferentiationValueKindType;

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
	    + " IF :NEW.is_deleted = 0 THEN BEGIN "
		+ " FOR i IN ("
		    + "SELECT "
		    + " pu_tf.name label "
		    + "FROM "
		    + TABLE_NAME + " o "
		    + "LEFT JOIN " + PremiseUsageTarif.TABLE_NAME + " pu_tf ON pu_tf.uuid = o.uuid_tf "
		    + "WHERE o.is_deleted = 0 "
		    + " AND o.uuid <> :NEW.uuid "
		    + " AND o.code_diff = :NEW.code_diff "
		    + " AND o.uuid_tf   = :NEW.uuid_tf "
		+ ") LOOP"
		    + " raise_application_error (-20000, "
		    + "'Указанный критерий дифференциации уже есть в ' || i.label "
		    + "); "
		+ " END LOOP; "
	    + " END; END IF; "
	    + "END;");
    }
}