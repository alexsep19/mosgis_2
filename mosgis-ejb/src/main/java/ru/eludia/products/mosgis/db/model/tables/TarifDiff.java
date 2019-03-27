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

	super  (TABLE_NAME, "Тарифы: значения критериев дифференциации тарифов");

	cols   (c.class);

	key    (c.UUID_TF);
    }
}