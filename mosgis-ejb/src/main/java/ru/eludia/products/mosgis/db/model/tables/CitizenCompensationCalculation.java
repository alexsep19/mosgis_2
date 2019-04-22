package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.gosuslugi.dom.schema.integration.msp.ImportCitizenCompensationRequest.ImportCitizenCompensation.Calculation;
import ru.gosuslugi.dom.schema.integration.msp.CitizenCompensationCalculationType;

public class CitizenCompensationCalculation extends EnTable {

    public static final String TABLE_NAME = "tb_cit_comp_calcs";

    public enum c implements EnColEnum {

	UUID_CIT_COMP                (CitizenCompensation.class, "Гражданин, получающий компенсацию расходов"),

	PERIODFROM                   (Type.DATE, "Дата начала расчета"),

	PERIODTO                     (Type.DATE, "Дата окончания расчета"),

	CALCULATIONDATE              (Type.DATE, "Дата расчета"),

	COMPENSATIONSUM              (Type.NUMERIC, 20, 2, "Размер компенсационной выплаты")
        ;

        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {
	    return false;
        }

    }

    public CitizenCompensationCalculation () {

        super (TABLE_NAME, "Расчеты и перерасчеты для гражданина, получающего компенсации расходов");

        cols (c.class);

        key (c.UUID_CIT_COMP);
    }

    public static Calculation toCitizenCompensationCalculation(Map<String, Object> r) {

	final Calculation result = DB.to.javaBean(Calculation.class, r);

	result.setLoadCalculations(DB.to.javaBean(CitizenCompensationCalculationType.class, r));

	result.setTransportGuid(UUID.randomUUID().toString());

	return result;
    }
}