package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.NUMERIC;
import static ru.eludia.base.model.Type.INTEGER;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;

public class SupplyResourceContractTemperatureChart extends EnTable {

    public enum c implements EnColEnum {
	UUID_XL                          (InXlFile.class, "Файл импорта"),
        UUID_SR_CTR                      (SupplyResourceContract.class, "Договор"),
	UUID_SR_CTR_OBJ                  (SupplyResourceContractObject.class, null, "Объект жилищного фонда договора (заполняется в случае если график привязан ОЖФ)"),

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

	public boolean isToXlImport() {

	    switch (this) {
	    case UUID_XL:
	    case OUTSIDETEMPERATURE:
	    case FLOWLINETEMPERATURE:
	    case OPPOSITELINETEMPERATURE:
		return true;
	    default:
		return false;
	    }
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
		    + " , o.uuid_sr_ctr_obj "
		    + "FROM "
		    + " tb_sr_ctr_t_charts o "
		    + "WHERE o.is_deleted = 0 "
		    + " AND o.uuid_sr_ctr     = :NEW.uuid_sr_ctr "
		    + " AND NVL(o.uuid_sr_ctr_obj, '00') = NVL(:NEW.uuid_sr_ctr_obj, '00') "
		    + " AND o.outsidetemperature = :NEW.outsidetemperature "
		    + " AND o.uuid           <> :NEW.uuid "
		+ ") LOOP "
		+ " raise_application_error (-20000, "
		+ "'Температура наружного воздуха ' || i.t || ' уже указана ' "
		+ "|| CASE WHEN i.uuid_sr_ctr_obj IS NULL THEN 'в договоре' ELSE 'в объекте жилищного фонда' END "
		+ "|| '. Операция отменена.'); "
		+ " END LOOP; "
	    + " END IF; "
	    + "END;");
    }

}
