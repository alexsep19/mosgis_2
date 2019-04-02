package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;

public class TarifCoeff extends EnTable  {

    public static final String TABLE_NAME = "tb_tf_coeffs";

    public enum c implements EnColEnum {

	UUID_TF                (Type.UUID, "Тариф"),

	COEFFICIENTVALUE       (Type.NUMERIC, 6, 3, "Значение коэффициента"),

	PRICE                  (Type.NUMERIC, 13, 3, "Величина тарифа из коэффициента"),

	COEFFICIENTDESCRIPTION (Type.STRING, 4000, "Описание")
        ;

        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}
        @Override
        public boolean isLoggable () {
            return false;
        }

    }

    public TarifCoeff () {

	super  (TABLE_NAME, "Тарифы: значения коэффициентов");

	cols   (c.class);

	key    (c.UUID_TF);

        trigger("BEFORE INSERT OR UPDATE", ""
	    + "DECLARE"
	    + " PRAGMA AUTONOMOUS_TRANSACTION; "
	    + "BEGIN "

	    + " FOR i IN ("
		+ "SELECT "
		+ " o.coefficientdescription label "
		+ "FROM "
		+ TABLE_NAME + " o "
		+ "WHERE o.is_deleted = 0 "
		+ " AND o.uuid <> :NEW.uuid "
		+ " AND o.uuid_tf = :NEW.uuid_tf "
		+ " AND (o.coefficientvalue = :NEW.coefficientvalue) "
		+ " AND (o.coefficientdescription = :NEW.coefficientdescription) "
	    + ") LOOP"
		+ " raise_application_error (-20000, "
		+ "'В этом тарифе уже есть коэффициент с таким же значением и названием: ' || i.label "
		+ "); "
	    + " END LOOP; "

	    + " IF :NEW.coefficientvalue <> :OLD.coefficientvalue OR INSERTING THEN "
	    + "   SELECT :NEW.coefficientvalue * tf.price INTO :NEW.price FROM vw_tarifs tf WHERE tf.id = :NEW.uuid_tf; "
	    + " END IF; "
	    + " COMMIT; "
	    + "END;");
    }

    public static final String CALC_PRICE = ""
	+ " IF :NEW.price <> :OLD.price THEN "
	+ "   UPDATE " + TarifCoeff.TABLE_NAME + " c SET price = c.coefficientvalue * :NEW.price WHERE uuid_tf = :NEW.uuid; "
	+ " END IF; ";
}