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
import ru.eludia.products.mosgis.db.model.voc.VocSettlementDocType;

public class SettlementDoc extends EnTable {

    public enum c implements EnColEnum {

	ID_TYPE               (VocSettlementDocType.class, VocSettlementDocType.i.RSO.asDef(), "Тип расчетов"),

        UUID_ORG_AUTHOR       (VocOrganization.class, "Поставщик данных"),

	UUID_SR_CTR           (SupplyResourceContract.class, "Договор"),
	UUID_ORG              (VocOrganization.class, "Организация-исполнитель из договора"),
	UUID_ORG_CUSTOMER     (VocOrganization.class, "Организация-заказчик из договора"),

	ID_SD_STATUS          (VocGisStatus.class, new Num(VocGisStatus.i.PROJECT.getId()), "Статус документа расчетов с точки зрения mosgis"),
	ID_SD_STATUS_GIS      (VocGisStatus.class, VocGisStatus.i.PROJECT.asDef(), "Статус документа расчетов с точки зрения ГИС ЖКХ"),

	REASONOFANNULMENT     (Type.STRING, 1000, null, "Причина аннулирования"),
	IS_ANNULED            (Type.BOOLEAN, new Virt("DECODE(\"REASONOFANNULMENT\",NULL,0,1)"), "1, если запись аннулирована; иначе 0"),

	SETTLEMENTGUID        (Type.UUID, null, "Идентификатор документа о расчетах в ГИС ЖКХ"),

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
		case UUID_ORG_AUTHOR:
		case UUID_ORG:
		case UUID_ORG_CUSTOMER:
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

    public enum Action {

	PLACING  (VocGisStatus.i.PENDING_RP_PLACING,   VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_PLACING),
	EDITING  (VocGisStatus.i.PENDING_RP_EDIT,      VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_STATE),
	ANNULMENT(VocGisStatus.i.PENDING_RP_ANNULMENT, VocGisStatus.i.ANNUL,    VocGisStatus.i.FAILED_ANNULMENT);

	VocGisStatus.i nextStatus;
	VocGisStatus.i okStatus;
	VocGisStatus.i failStatus;

	private Action(VocGisStatus.i nextStatus, VocGisStatus.i okStatus, VocGisStatus.i failStatus) {
	    this.nextStatus = nextStatus;
	    this.okStatus = okStatus;
	    this.failStatus = failStatus;
	}

	public VocGisStatus.i getNextStatus() {
	    return nextStatus;
	}

	public VocGisStatus.i getFailStatus() {
	    return failStatus;
	}

	public VocGisStatus.i getOkStatus() {
	    return okStatus;
	}

	public static Action forStatus(VocGisStatus.i status) {

	    switch (status) {

	    case PENDING_RQ_PLACING:
		return PLACING;
	    case PENDING_RQ_EDIT:
		return EDITING;
	    case PENDING_RQ_ANNULMENT:
		return ANNULMENT;

	    case PENDING_RP_PLACING:
		return PLACING;
	    case PENDING_RP_EDIT:
		return EDITING;
	    case PENDING_RP_ANNULMENT:
		return ANNULMENT;

	    default:
		return null;
	    }
	}
    };
}
