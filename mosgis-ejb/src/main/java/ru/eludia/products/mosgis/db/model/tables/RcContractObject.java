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
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class RcContractObject extends EnTable {

    public enum c implements EnColEnum {

	UUID_RC_CTR           (RcContract.class, "Договор"),
	UUID_ORG_CUSTOMER     (VocOrganization.class, "Заказчик из договора"),

	ID_CTR_STATUS         (VocGisStatus.class, new Num(VocGisStatus.i.PROJECT.getId()), "Статус объекта жилищного фонда с точки зрения mosgis"),

	FIASHOUSEGUID         (VocBuilding.class, "Глобальный уникальный идентификатор дома по ФИАС"),

	DT_FROM               (DATE, "Дата начала обслуживания дома"),
	DT_TO                 (DATE, null, "Дата окончания обслуживания дома;"),

	ID_LOG                (RcContractObjectLog.class, null, "Последнее событие редактирования")
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
                case UUID_RC_CTR:
                case ID_LOG:
                    return false;
                default:
                    return true;
            }
        }
    }

    public RcContractObject () {

        super ("tb_rc_ctr_obj", "Объект жилищного фонда договора РЦ");

        cols  (c.class);

        key   ("uuid_rc_ctr", c.UUID_RC_CTR);

	trigger("BEFORE INSERT OR UPDATE", ""
	    + "DECLARE"
	    + " PRAGMA AUTONOMOUS_TRANSACTION; "
	    + "BEGIN "

	    + " SELECT uuid_org_customer INTO :NEW.uuid_org_customer FROM tb_rc_ctr WHERE uuid=:NEW.uuid_rc_ctr; "

	    + " IF :NEW.is_deleted = 0 THEN BEGIN "

		+ " IF :NEW.DT_TO < :NEW.DT_FROM THEN "
		+ "   raise_application_error (-20000, 'Дата окончания обслуживания дома не может быть раньше даты начала обслуживания обслуживания дома. Операция отменена.'); "
		+ " END IF; "

		+ " FOR i IN ("
		    + "SELECT "
		    + " building.label   building_label "
		    + " , rc_ctr.label     rc_ctr_label "
		    + "FROM "
		    + " tb_rc_ctr_obj o "
		    + " INNER JOIN tb_rc_ctr    rc_ctr   ON o.uuid_rc_ctr = rc_ctr.uuid "
		    + " INNER JOIN vc_buildings building ON o.fiashouseguid = building.houseguid "
		    + "WHERE o.is_deleted = 0 "
		    + " AND o.uuid <> :NEW.uuid "
		    + " AND o.fiashouseguid     = :NEW.fiashouseguid "
		    + " AND o.uuid_org_customer = :NEW.uuid_org_customer "
		    + " AND (o.dt_to   >= :NEW.dt_from OR o.dt_to IS NULL) "
		    + " AND (o.dt_from <= :NEW.dt_to   OR :NEW.dt_to IS NULL) "
		+ ") LOOP"
		    + " raise_application_error (-20000, "
		    + "'Период обслуживания ' || i.building_label "
		    + "|| ' пересекается с периодом обслуживания этого адреса по договору ' || i.rc_ctr_label"
		    + "|| '. Одновременное обслуживание одинаковых адресов домов по разным договорам c одним заказчиком запрещено' "
		    + "|| '. Операция отменена.'); "
		+ " END LOOP; "

	    + " END; END IF; " // IF :NEW.is_deleted = 0
	    + "END;");
    }

}
