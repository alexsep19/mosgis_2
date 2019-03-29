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
	    + "BEGIN "
	    + " SELECT :NEW.coefficientvalue * tf.price INTO :NEW.price FROM vw_tariffs tf WHERE tf.id = :NEW.uuid_tf; "
	    + "END;");
    }
}