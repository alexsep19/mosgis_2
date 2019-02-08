package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocRcContractServiceTypes;


public class RcContract extends EnTable {

    public enum c implements EnColEnum {

	UUID_ORG             (VocOrganization.class, "Расчетный центр"),
	UUID_ORG_CUSTOMER    (VocOrganization.class, "Организация-заказчик"),

	ID_CTR_STATUS        (VocGisStatus.class,        VocGisStatus.i.PROJECT.asDef (),     "Статус договора с точки зрения mosgis"),

	ID_LOG               (RcContractLog.class,  "Последнее событие редактирования"),

	CONTRACTNUMBER       (Type.STRING, 255, "Номер договора"),
	SIGNINGDATE          (Type.DATE, "Дата заключения"),
	EFFECTIVEDATE        (Type.DATE, "Дата вступления в силу"),
	COMPLETIONDATE       (Type.DATE, null, "Дата окончания плановая"),
	TERMINATE            (Type.DATE, null, "Дата растрожения"),

	DT_FROM              (Type.DATE, null, "Дата начала действия договора"),
	DT_TO                (Type.DATE, null, "Дата окончания действия договора"),

	LABEL                (Type.STRING, new Virt("'№' || contractnumber || ' от ' || TO_CHAR (signingdate, 'DD.MM.YYYY')"), "№/дата"),

	ID_SERVICE_TYPE      (VocRcContractServiceTypes.class, VocRcContractServiceTypes.i.BILLING.asDef(), "Вид услуг"),

	IS_ACCOUNTS          (Type.BOOLEAN, Bool.FALSE, "1, если Ведение лицевых счетов; иначе 0"),
	IS_INVOICES          (Type.BOOLEAN, Bool.TRUE,  "1, если РЦ осуществляет формирование квитанций на оплату (ПД); иначе 0"),
	IS_PROC_PAY          (Type.BOOLEAN, Bool.FALSE, "1, если РЦ осуществляет обработку поступивших платежей; иначе 0"),
	IS_LIST_MD           (Type.BOOLEAN, Bool.FALSE, "1, если РЦ осуществляет ведение перечня приборов учета; иначе 0"),
	IS_PROC_MD_IND       (Type.BOOLEAN, Bool.FALSE, "1, если РЦ осуществляет сбор и обработку данных показателей ИПУ; иначе 0"),
	IS_ALL_HOUSE         (Type.BOOLEAN, Bool.FALSE, "1, если РЦ доступны все объекты жилищного фонда; иначе 0"),

	DDT_D_START          (Type.NUMERIC, 2, null, "Дата выставления платежных документов до (1..30 — конкретное число; 99 — последнее число)"),
	DDT_D_START_NXT      (Type.BOOLEAN,    null, "1, если Дата выставления платежных документов до в следующем месяце; иначе 0")
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {
            switch (this) {
                case ID_LOG:
                case UUID_ORG:
                case UUID_ORG_CUSTOMER:
		case LABEL:
		case DT_FROM:
		case DT_TO:
                    return false;
                default:
                    return true;
            }
        }

    }

    public RcContract () {

        super ("tb_rc_ctr", "Договор услуг РЦ (расчетного центра)");

        cols   (c.class);

        key    ("uuid_org", c.UUID_ORG);

        trigger("BEFORE INSERT OR UPDATE", ""
		+ "DECLARE"
		+ " PRAGMA AUTONOMOUS_TRANSACTION; "
                + "BEGIN "

                + " IF :NEW.is_deleted = 0 THEN "

		    + " IF :NEW.effectivedate < :NEW.signingdate THEN"
		    + "   raise_application_error (-20000, 'Дата вступления в силу  не может быть раньше даты заключения договора. Операция отменена.'); "
		    + " END IF; "

		    + " IF :NEW.completiondate < :NEW.signingdate THEN"
		    + "   raise_application_error (-20000, 'Дата окончания плановая не может быть раньше даты вступления в силу. Операция отменена.'); "
		    + " END IF; "

		    + " :NEW.dt_from := :NEW.effectivedate; "
		    + " :NEW.dt_to   := NVL(:NEW.terminate, :NEW.completiondate); "

		    + " IF :NEW.is_all_house = 1 AND :NEW.id_ctr_status <> " + VocGisStatus.i.ANNUL.getId() + " THEN "
			+ " FOR i IN ("
			+ "SELECT "
			+ " o.label "
			+ "FROM "
			+ " tb_rc_ctr o "
			+ "WHERE o.is_deleted = 0 "
			+ " AND o.uuid <> :NEW.uuid "
			+ " AND o.is_all_house = 1 "
			+ " AND o.id_ctr_status <> " + VocGisStatus.i.ANNUL.getId()
			+ " AND (o.dt_to   >= :NEW.dt_from OR o.dt_to IS NULL) "
			+ " AND (o.dt_from <= :NEW.dt_to   OR :NEW.dt_to IS NULL) "
			+ ") LOOP"
			+ " raise_application_error (-20000, "
			+ "'В период действия текущего договора уже существует договор ' || i.label "
			+ "|| ' с параметром \"Доступны все объекты жилищного фонда\"' "
			+ "|| '. Операция отменена.'); "
			+ " END LOOP; "
		    + " END IF; "

                + " END IF; " // is_deleted = 0
        + "END;");
    }

}