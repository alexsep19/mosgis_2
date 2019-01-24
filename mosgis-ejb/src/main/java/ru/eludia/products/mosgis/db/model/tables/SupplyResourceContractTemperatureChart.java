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

	trigger("BEFORE INSERT OR UPDATE", ""
	    + "DECLARE"
	    + " PRAGMA AUTONOMOUS_TRANSACTION; "
	    + "BEGIN "
	    + " IF :NEW.is_deleted = 0 THEN "
		+ " FOR i IN ("
		    + "SELECT "
		    + " o.outsidetemperature t "
		    + "FROM "
		    + " tb_sr_ctr_t_charts o "
		    + "WHERE o.is_deleted = 0 "
		    + " AND o.uuid_sr_ctr     = :NEW.uuid_sr_ctr "
		    + " AND o.outsidetemperature = :NEW.outsidetemperature "
		    + " AND o.uuid           <> :NEW.uuid "
		+ ") LOOP "
		+ " raise_application_error (-20000, "
		+ "'Температура наружного воздуха ' || i.t || ' уже указана в договоре'"
		+ "|| '. Операция отменена.'); "
		+ " END LOOP; "
	    + " END IF; "
	    + "END;");
    }

}
