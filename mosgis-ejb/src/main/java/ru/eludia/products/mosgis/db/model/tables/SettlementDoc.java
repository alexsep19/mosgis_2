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
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class SettlementDoc extends EnTable {

    public enum c implements EnColEnum {
        UUID_ORG_AUTHOR       (VocOrganization.class, "Поставщик данных"),

	UUID_SR_CTR           (SupplyResourceContract.class, "Договор"),
	UUID_ORG              (VocOrganization.class, "Организация-исполнитель из договора"),
	UUID_ORG_CUSTOMER     (VocOrganization.class, "Организация-заказчик из договора"),

	ID_SD_STATUS          (VocGisStatus.class, new Num(VocGisStatus.i.PROJECT.getId()), "Статус документа расчетов с точки зрения mosgis"),

	ID_LOG                (SettlementDocLog.class, null, "Последнее событие редактирования")
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
		case UUID_ORG:
		case ID_LOG:
                    return false;
                default:
                    return true;
            }
        }
    }

    public SettlementDoc () {

        super ("tb_st_docs", "Документ информации о состоянии расчетов договора РСО");

        cols  (c.class);

        key   ("uuid_sr_ctr", c.UUID_SR_CTR);
	key   ("uuid_org_customer", c.UUID_ORG_CUSTOMER);
	key   ("uuid_org", c.UUID_ORG);

	trigger("BEFORE INSERT OR UPDATE", ""
	    + "DECLARE"
	    + " PRAGMA AUTONOMOUS_TRANSACTION; "
	    + "BEGIN "

	    + " SELECT uuid_org_customer INTO :NEW.uuid_org_customer FROM tb_sr_ctr WHERE uuid = :NEW.uuid_sr_ctr; "
	    + " SELECT uuid_org          INTO :NEW.uuid_org          FROM tb_sr_ctr WHERE uuid = :NEW.uuid_sr_ctr; "

	    + " IF :NEW.is_deleted = 0 THEN "
		+ " FOR i IN ("
		+ "SELECT "
		+ " o.uuid "
		+ "FROM "
		+ " tb_st_docs o "
		+ "WHERE o.is_deleted = 0 "
		+ " AND o.uuid <> :NEW.uuid "
		+ " AND o.uuid_sr_ctr     = :NEW.uuid_sr_ctr "
		+ " AND o.uuid_org_author  = :NEW.uuid_org_author "
		+ " AND o.id_sd_status    <> " + VocGisStatus.i.ANNUL
		+ ") LOOP"
		+ " raise_application_error (-20000, "
		+ "'Уже есть документ о состоянии расчетов на этот договор' "
		+ "|| '. Операция отменена.'); "
		+ " END LOOP; "
	    + " END IF; " // IF :NEW.is_deleted = 0
	    + "END;");
    }

}
