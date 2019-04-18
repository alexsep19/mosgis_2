package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;

public class CitizenCompensationDecision extends EnTable {

    public static final String TABLE_NAME = "tb_cit_comp_decs";

    public enum c implements EnColEnum {

	UUID_CIT_COMP                (CitizenCompensation.class, "Гражданин, получающий компенсацию расходов"),

	NUMBER_                      (Type.STRING, 256, null, "Номер решения"),

	CODE_VC_NSI_301              (Type.STRING, 20, "Тип решения"),
	CODE_VC_NSI_302              (Type.STRING, 20, null, "Основание решения"),

	DECISIONDATE                 (Type.DATE, "Дата решения"),
	EVENTDATE                    (Type.DATE, null, "Дата события: приостановления, возобновления или прекращения")
        ;

        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {
	    return false;
        }

    }

    public CitizenCompensationDecision () {

        super (TABLE_NAME, "Решения о предоставлении гражданам компенсации расходов");

        cols (c.class);

        key (c.UUID_CIT_COMP);
    }
}