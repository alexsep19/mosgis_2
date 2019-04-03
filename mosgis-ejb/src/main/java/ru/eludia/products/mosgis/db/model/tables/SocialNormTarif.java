package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class SocialNormTarif extends EnTable  {

    public static final String TABLE_NAME = "tb_sn_tfs";

    public enum c implements EnColEnum {
	UUID_ORG               (VocOrganization.class, "Организация, которая завела данный тариф в БД"),

	NAME                   (Type.STRING, 4000, null, "Наименование"),

	DATEFROM               (Type.DATE, "Дата начала действия"),
	DATETO                 (Type.DATE, null, "Дата окончания действия"),

	PRICE                  (Type.NUMERIC, 15, 3, null, "Величина"),
	UNIT                   (VocOkei.class, "Единица измерения"),

	ID_CTR_STATUS          (VocGisStatus.class,    VocGisStatus.DEFAULT,    "Статус с точки зрения mosgis"),
	ID_CTR_STATUS_GIS      (VocGisStatus.class,    VocGisStatus.DEFAULT,    "Статус с точки зрения ГИС ЖКХ"),

	CANCELREASON           (Type.STRING, 4000, null, "Причина аннулирования"),
	IS_ANNULED             (Type.BOOLEAN, new Virt("DECODE(\"CANCELREASON\",NULL,0,1)"), "1, если запись аннулирована; иначе 0"),
	TARIFFGUID             (Type.UUID, null, "Идентификатор НПА в ГИС ЖКХ"),

	ID_LOG                 (SocialNormTarifLog.class, "Последнее событие редактирования")
        ;

        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}
        @Override
        public boolean isLoggable () {
            switch (this) {
                case UUID_ORG:
                case ID_LOG:
                    return false;
                default:
                    return true;
            }
        }

    }

    public enum Action {
        
        PLACING     (VocGisStatus.i.PENDING_RQ_PLACING,   VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_PLACING),
        EDITING     (VocGisStatus.i.PENDING_RQ_EDIT,      VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_STATE),
        ANNULMENT   (VocGisStatus.i.PENDING_RQ_ANNULMENT, VocGisStatus.i.ANNUL,    VocGisStatus.i.FAILED_ANNULMENT),
        ;
        
        VocGisStatus.i nextStatus;
        VocGisStatus.i okStatus;
        VocGisStatus.i failStatus;

        private Action (VocGisStatus.i nextStatus, VocGisStatus.i okStatus, VocGisStatus.i failStatus) {
            this.nextStatus = nextStatus;
            this.okStatus = okStatus;
            this.failStatus = failStatus;
        }

        public VocGisStatus.i getNextStatus () {
            return nextStatus;
        }

        public VocGisStatus.i getFailStatus () {
            return failStatus;
        }

        public VocGisStatus.i getOkStatus () {
            return okStatus;
        }

        public static Action forStatus (VocGisStatus.i status) {
            
            switch (status) {
                case PENDING_RQ_PLACING:   return PLACING;
                case PENDING_RP_PLACING:   return PLACING;
                case PENDING_RQ_EDIT:      return EDITING;
                case PENDING_RP_EDIT:      return EDITING;
                case PENDING_RQ_ANNULMENT: return ANNULMENT;
                case PENDING_RP_ANNULMENT: return ANNULMENT;
                default: return null;
            }
            
        }
    };

    public SocialNormTarif () {

	super  (TABLE_NAME, "Тарифы: Социальная норма потребления электрической энергии");

	cols   (c.class);

	key    (c.UUID_ORG);

        trigger ("BEFORE UPDATE", ""
	    + "DECLARE "
	    + "BEGIN "
	    + "IF :NEW.ID_CTR_STATUS <> :OLD.ID_CTR_STATUS "
	    + " AND :OLD.ID_CTR_STATUS <> " + VocGisStatus.i.FAILED_PLACING.getId()
	    + " AND :NEW.ID_CTR_STATUS =  " + VocGisStatus.i.PROJECT.getId()
	    + " THEN "
	    + " :NEW.ID_CTR_STATUS := " + VocGisStatus.i.MUTATING.getId()
	    + "; END IF; "
        + "END;");

	trigger("BEFORE INSERT OR UPDATE", ""
	    + "DECLARE"
	    + " PRAGMA AUTONOMOUS_TRANSACTION; "
	    + " cnt NUMBER; "
	    + "BEGIN "
	    + " IF :NEW.is_deleted = 0 THEN BEGIN "
		+ " IF :NEW.datefrom > :NEW.dateto THEN "
		+ "   raise_application_error (-20000, 'Дата начала действия не может превышать дату окончания действия'); "
		+ " END IF; "
		+ " IF :NEW.unit NOT IN (" + VocOkei.CODES_ENERGY_WT + ") THEN "
		+ "   raise_application_error (-20000, 'Укажите единицу измерения электрической энергии'); "
		+ " END IF; "
		+ " FOR i IN ("
		    + "SELECT "
		    + " o.name     label "
		    + " , o.datefrom dt_from "
		    + " , o.dateto   dt_to "
		    + "FROM "
		    + TABLE_NAME + " o "
		    + "WHERE o.is_deleted = 0 "
		    + " AND o.uuid <> :NEW.uuid "
		    + " AND (o.datefrom <= :NEW.dateto   OR :NEW.dateto IS NULL) "
		    + " AND (o.dateto   >= :NEW.datefrom OR o.dateto IS NULL) "
		+ ") LOOP"
		    + " raise_application_error (-20000, "
		    + "'Указанный период пересекается с другой информацией о размере платы за пользование жилым помещением ' || i.label "
		    + "|| ' с ' "
		    + "|| TO_CHAR (i.dt_from, 'DD.MM.YYYY')"
		    + "|| CASE WHEN i.dt_to IS NOT NULL THEN ' по ' || TO_CHAR (i.dt_to, 'DD.MM.YYYY') ELSE '' END "
		    + "); "
		+ " END LOOP; "
	    + " END; END IF; "
	    + " IF :NEW.ID_CTR_STATUS <> :OLD.ID_CTR_STATUS AND :NEW.ID_CTR_STATUS=" + VocGisStatus.i.PENDING_RQ_PLACING + " THEN "

		+ " SELECT COUNT(*) INTO cnt FROM tb_sn_tf_oktmo WHERE uuid=:NEW.uuid; "
		+ " IF cnt = 0 THEN "
		+ "   raise_application_error (-20000, 'Укажите территорию действия'); "
		+ " END IF; "

		+ " SELECT COUNT(*) INTO cnt FROM tb_tf_legal_acts WHERE uuid=:NEW.uuid; "
		+ " IF cnt = 0 THEN "
		+ "   raise_application_error (-20000, 'Прикрепите хотя бы один НПА, размещенный в ГИС ЖКХ'); "
		+ " END IF; "

		+ " FOR i IN ("
		    + "SELECT "
		    + " la.name     label "
		    + "FROM "
		    + " tb_tf_legal_acts o "
		    + " LEFT JOIN " + LegalAct.TABLE_NAME + " la ON la.uuid = o.uuid_legal_act "
		    + "WHERE "
		    + " o.uuid = :NEW.uuid "
		    + " AND la.documentguid IS NULL "
		+ ") LOOP "
		    + " raise_application_error (-20000, "
		    + "'НПА ' || i.label || ' не размещен в ГИС ЖКХ' "
		    + "); "
		+ " END LOOP; "
	    + " END IF; "
	    + " COMMIT; "
	    + "END;");

	trigger ("AFTER UPDATE", ""
	    + "DECLARE "
	    + " PRAGMA AUTONOMOUS_TRANSACTION; "
	    + "BEGIN "
	    + TarifCoeff.CALC_PRICE
	    + " COMMIT; "
	    + "END;");
    }
}