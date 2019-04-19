package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocCitizenCompensationPaymentType;

public class CitizenCompensationPayment extends EnTable {

    public static final String TABLE_NAME = "tb_cit_comp_pays";

    public enum c implements EnColEnum {

	UUID_CIT_COMP               (CitizenCompensation.class, "Гражданин, получающий компенсацию расходов"),

	PAYMENTTYPE                 (VocCitizenCompensationPaymentType.class, "Тип выплаты"),

	PAYMENTDATE                 (Type.DATE, "Дата выплаты"),

	PAYMENTSUM                  (Type.NUMERIC, 20, 2, "Сумма выплаты, руб.")
        ;

        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {
	    return false;
        }

    }

    public CitizenCompensationPayment () {

        super (TABLE_NAME, "Выплата гражданину, получающему субсидии/компенсации расходов");

        cols (c.class);

        key (c.UUID_CIT_COMP);
    }
}