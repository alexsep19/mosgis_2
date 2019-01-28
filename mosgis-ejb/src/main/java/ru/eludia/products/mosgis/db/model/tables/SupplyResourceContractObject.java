package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.DATE;
import static ru.eludia.base.model.Type.STRING;
import static ru.eludia.base.model.Type.NUMERIC;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Num;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocGisSupplyResourceContractCustomerType;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;

public class SupplyResourceContractObject extends EnTable {

    public enum c implements EnColEnum {

        UUID_SR_CTR           (SupplyResourceContract.class, "Договор"),

        ID_CTR_STATUS         (VocGisStatus.class, new Num(VocGisStatus.i.PROJECT.getId()), "Статус объекта жилищного фонда с точки зрения mosgis"),

        FIASHOUSEGUID         (VocBuilding.class, "Глобальный уникальный идентификатор дома по ФИАС"),
        UUID_PREMISE          (Premise.class, null, "Помещение"),

        ID_LOG                (SupplyResourceContractObjectLog.class, null, "Последнее событие редактирования")
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
                case UUID_SR_CTR:
                case ID_LOG:
                    return false;
                default:
                    return true;
            }
        }
    }

    public SupplyResourceContractObject () {

        super ("tb_sr_ctr_obj", "Объект жилищного фонда договора РСО");

        cols  (c.class);

        key   ("uuid_sr_ctr", c.UUID_SR_CTR);

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
		    + " tb_sr_ctr_obj o "
		    + " INNER JOIN tb_sr_ctr    sr_ctr   ON o.uuid_sr_ctr = sr_ctr.uuid "
		    + " INNER JOIN vc_buildings building ON o.fiashouseguid = building.houseguid "
		    + " LEFT JOIN vw_premises  premise  ON o.uuid_premise = premise.id "
		    + "WHERE o.is_deleted = 0 "
		    + " AND o.uuid <> :NEW.uuid "
		    + " AND o.uuid_sr_ctr     = :NEW.uuid_sr_ctr "
		    + " AND o.fiashouseguid   = :NEW.fiashouseguid "
		    + " AND NVL(o.uuid_premise, '00') = NVL(:NEW.uuid_premise, '00') "
		+ ") LOOP"
		    + " raise_application_error (-20000, "
		    + "'Объект жилищного фонда ' || i.building_label "
		    + "|| CASE WHEN i.uuid_premise IS NULL THEN '' ELSE  (' помещение ' || i.premise_label) END "
		    + "|| ' уже есть в договоре ' || i.sr_ctr_label "
		    + "|| '. Операция отменена.'); "
		+ " END LOOP; "

//		+ " IF :NEW.uuid_premise IS NULL THEN BEGIN "
//		    + " FOR sr_ctr IN (SELECT id_customer_type FROM tb_sr_ctr WHERE uuid = :NEW.uuid_sr_ctr "
//		    + ") LOOP"
//		    + "   FOR h IN (SELECT DISTINCT h.is_condo FROM vw_premises p LEFT JOIN tb_houses h ON p.uuid_house = h.uuid WHERE h.fiashouseguid = :NEW.fiashouseguid "
//		    + "   ) LOOP"
//
//			+ " IF h.is_condo = 1 AND sr_ctr.id_customer_type = " + VocGisSupplyResourceContractCustomerType.i.OWNER.getId()
//			+ " THEN "
//			+ "     raise_application_error (-20000, 'Укажите помещение МКД. Операция отменена.'); "
//			+ " END IF; "
//
//			+ " IF h.is_condo = 0 AND sr_ctr.id_customer_type = " + VocGisSupplyResourceContractCustomerType.i.LIVINGHOUSEOWNER.getId()
//			+ " THEN "
//			+ "     raise_application_error (-20000, 'Укажите помещение ЖД. Операция отменена.'); "
//			+ " END IF; "
//
//		    + "   END LOOP; "
//		    + " END LOOP; "
//		+ " END; END IF; " // IF :NEW.uuid_premise IS NULL

	    + " END; END IF; " // IF :NEW.is_deleted = 0
	    + "END;");
    }

}
