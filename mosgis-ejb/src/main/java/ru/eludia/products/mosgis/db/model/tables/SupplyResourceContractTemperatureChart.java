package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.NUMERIC;
import static ru.eludia.base.model.Type.INTEGER;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;

public class SupplyResourceContractTemperatureChart extends EnTable {

    public enum c implements EnColEnum {

        UUID_SR_CTR                      (SupplyResourceContract.class, "Договор"),

	OUTSIDETEMPERATURE               (INTEGER, "Температура наружного воздуха, °С"),
	FLOWLINETEMPERATURE              (NUMERIC, 12, 1, "Температура теплоносителя в подающем трубопроводе, °С"),
	OPPOSITELINETEMPERATURE          (NUMERIC, 12, 1, "Температура теплоносителя в обратном трубопроводе, °С")
        ;

        @Override
        public Col getCol() {
            return col;
        }
        private Col col;

        private c(Type type, Object... p) {
            col = new Col(this, type, p);
        }

        private c(Class c, Object... p) {
            col = new Ref(this, c, p);
        }

        @Override
        public boolean isLoggable() {
	    return false;
        }
    }

    public SupplyResourceContractTemperatureChart () {

        super ("tb_sr_ctr_t_charts", "Температурный график договора РСО");

        cols  (c.class);

        key   ("uuid_sr_ctr", c.UUID_SR_CTR);
    }

}
