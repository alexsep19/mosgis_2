package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.gosuslugi.dom.schema.integration.volume_quality.AddressObjectType;
import ru.gosuslugi.dom.schema.integration.volume_quality.IntervalType;

public class IntervalObject extends EnTable {

    public enum c implements EnColEnum {

	UUID_INTERVAL         (Interval.class, "Информация о перерывах"),

	FIASHOUSEGUID         (VocBuilding.class, "Глобальный уникальный идентификатор дома по ФИАС"),
	UUID_PREMISE          (Premise.class, null, "Помещение"),

	ID_LOG                (IntervalObjectLog.class, null, "Последнее событие редактирования")
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
            switch (this) {
                case UUID_INTERVAL:
                case ID_LOG:
                    return false;
                default:
                    return true;
            }
        }
    }

    public IntervalObject () {

        super ("tb_interval_obj", "Объекты жилищного фонда информации о перерывах РСО");

        cols  (c.class);

        key   ("uuid_interval", c.UUID_INTERVAL);

        key   ("fiashouseguid1", c.FIASHOUSEGUID, c.UUID_PREMISE);

	trigger("BEFORE INSERT OR UPDATE", ""
	    + "DECLARE"
	    + " PRAGMA AUTONOMOUS_TRANSACTION; "
	    + "BEGIN "
	    + " IF :NEW.is_deleted = 0 THEN BEGIN "
		+ " FOR i IN ("
		    + "SELECT "
		    + " o.uuid_premise     uuid_premise"
		    + " , premise.label    premise_label"
		    + " , building.label   building_label "
		    + " , sr_ctr.label     sr_ctr_label "
		    + "FROM "
		    + " tb_interval_obj o "
		    + " INNER JOIN tb_intervals interval ON o.uuid_interval = interval.uuid "
		    + " INNER JOIN tb_sr_ctr    sr_ctr   ON interval.uuid_sr_ctr = sr_ctr.uuid "
		    + " INNER JOIN vc_buildings building ON o.fiashouseguid = building.houseguid "
		    + " LEFT  JOIN vw_premises  premise  ON o.uuid_premise = premise.id "
		    + "WHERE o.is_deleted = 0 "
		    + " AND o.uuid <> :NEW.uuid "
		    + " AND o.uuid_interval   = :NEW.uuid_interval "
		    + " AND o.fiashouseguid   = :NEW.fiashouseguid "
		    + " AND NVL(o.uuid_premise, '00') = NVL(:NEW.uuid_premise, '00') "
		+ ") LOOP"
		    + " raise_application_error (-20000, "
		    + "'Объект жилищного фонда ' || i.building_label "
		    + "|| CASE WHEN i.uuid_premise IS NULL THEN '' ELSE  (' помещение ' || i.premise_label) END "
		    + "|| ' уже есть в информации о перерывах по договору ' || i.sr_ctr_label "
		    + "|| '. Операция отменена.'); "
		+ " END LOOP; "

	    + " END; END IF; " // IF :NEW.is_deleted = 0
	    + "END;");
    }

    static IntervalType.AddressObject toAddressObject(Map<String, Object> r) {

	final IntervalType.AddressObject result = DB.to.javaBean(IntervalType.AddressObject.class, r);

	return result;
    }
}
