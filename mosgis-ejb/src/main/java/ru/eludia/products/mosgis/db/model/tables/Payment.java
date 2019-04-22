package ru.eludia.products.mosgis.db.model.tables;

import java.math.BigDecimal;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPaymentBaseType;

public class Payment extends EnTable {

    public static final String TABLE_NAME = "tb_pays";

    public enum c implements EnColEnum {

	UUID_XL                       (InXlFile.class, null, "Источник импорта"),

	UUID_ORG                      (VocOrganization.class, null, "Организация, которая создала данный платёжный документ"),

	ID_TYPE                       (VocPaymentBaseType.class, VocPaymentBaseType.DEFAULT,  "Тип основания для оплаты"),
	UUID_ACCOUNT                  (Account.class,         null, "Лицевой счёт основание для оплаты, заполняется всегда"),
	UUID_PAY_DOC                  (PaymentDocument.class, null, "Платежный документ основание для оплаты, заполняется если основание ПД"),

	YEAR                          (Type.NUMERIC, 4,             "Год периода расчета"),
	MONTH                         (Type.NUMERIC, 4,             "Месяц периода расчета"),
	DT_PERIOD                     (Type.DATE,           null,   "ГГГГ-ММ-01"),

	ORDERNUM                      (Type.STRING, null, "Номер платежа"),

	ORDERDATE                     (Type.DATE,           "Дата внесения платы"),
	AMOUNT                        (Type.NUMERIC, 20, 2,                                          "Сумма, руб."),
        AMOUNT_ACK                    (Type.NUMERIC, 20, 2, BigDecimal.ZERO,                         "Сквитировано, руб."),
        AMOUNT_NACK                   (Type.NUMERIC, 20, 2, new Virt  ("\"AMOUNT\"-\"AMOUNT_ACK\""), "Не сквитировано, руб."),

	PAYMENTPURPOSE                (Type.STRING, 1000, null, "Назначение платежа"),

	ID_CTR_STATUS                 (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус с точки зрения mosgis"),
	ID_CTR_STATUS_GIS             (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус с точки зрения ГИС ЖКХ"),

	CANCELLATIONDATE              (Type.DATE, null, "Дата аннулирования"),
	CANCELLATIONCOMMENT           (Type.STRING, 210, null, "Причина аннулирования"),
	IS_ANNULED                    (Type.BOOLEAN, new Virt("CASE WHEN CANCELLATIONDATE IS NULL THEN 0 ELSE 1 END"), "1, если запись аннулирована; иначе 0"),

	ORDERGUID                     (Type.UUID,   null, "Идентификатор НПА в ГИС ЖКХ, он же NotificationsOfOrderExecutionGUID"),
	UNIQUENUMBER                  (Type.STRING, null, "Уникальный номер, присвоенный ГИС ЖКХ"),
	ORDERID                       (Type.STRING, new Virt("'' || UNIQUENUMBER"), "Уникальный номер, присвоенный ГИС ЖКХ (синоним)"),

	ID_LOG                        (PaymentLog.class, "Последнее событие редактирования"),

        ;

	@Override
	public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}

	@Override
	public boolean isLoggable () {
	    switch (this) {
		case ID_LOG:
		case UUID_ORG:
		case UUID_ACCOUNT:
		case UUID_PAY_DOC:
		    return false;
		default:
		    return true;
	    }
	}

	public boolean isToXlImport() {

	    switch (this) {
		case UUID_ORG:
		case UUID_PAY_DOC:
		case UUID_ACCOUNT:
		case ID_TYPE:
		case AMOUNT:
		case ORDERDATE:
		case YEAR:
		case MONTH:
		    return true;
	    default:
		return false;
	    }
	}
    }

    public Payment () {

	super  (TABLE_NAME, "Платежи");

	cols   (c.class);

	key (c.UUID_ACCOUNT);
	key (c.UUID_PAY_DOC);

	trigger ("BEFORE INSERT", ""
            + " BEGIN "

	    + " IF :NEW.ID_TYPE IS NULL THEN "
	    + "   raise_application_error (-20000, 'Укажите тип основания для оплаты'); "
	    + " END IF; "

	    + " IF :NEW.UUID_ACCOUNT IS NULL AND :NEW.ID_TYPE = " + VocPaymentBaseType.i.ACCOUNT + " THEN "
	    + "   raise_application_error (-20000, 'Укажите ЛС основание для оплаты'); "
	    + " END IF; "

	    + " IF :NEW.UUID_PAY_DOC IS NULL AND :NEW.ID_TYPE = " + VocPaymentBaseType.i.PAYMENT_DOCUMENT + " THEN "
	    + "   raise_application_error (-20000, 'Укажите ПД основание для оплаты'); "
	    + " END IF; "

	    + " IF :NEW.UUID_ACCOUNT IS NULL AND :NEW.UUID_PAY_DOC IS NOT NULL THEN "
	    + "   SELECT UUID_ACCOUNT INTO :NEW.UUID_ACCOUNT FROM " + PaymentDocument.TABLE_NAME + " WHERE uuid = :NEW.UUID_PAY_DOC; "
	    + " END IF; "

	    + " IF :NEW.MONTH > 12 OR :NEW.MONTH = 0 THEN "
	    + "   raise_application_error (-20000, 'Укажите месяц 1..12'); "
	    + " END IF; "

            + " :NEW.dt_period := TO_DATE (:NEW.year || LPAD (:NEW.month, 2, '0') || '01', 'YYYYMMDD'); "

            + "END; "
        );

	trigger("BEFORE UPDATE", ""
            + "DECLARE"
	    + " id_base varchar(255); "
            + " BEGIN "

	    + " IF :NEW.ID_CTR_STATUS <> :OLD.ID_CTR_STATUS AND :NEW.ID_CTR_STATUS=" + VocGisStatus.i.PENDING_RQ_PLACING + " THEN "

		+ " IF :NEW.ID_TYPE = " + VocPaymentBaseType.i.ACCOUNT + " THEN "
		+ "   SELECT " + Account.c.SERVICEID.lc() + " INTO id_base FROM " + Account.TABLE_NAME + " WHERE UUID = :NEW.UUID_ACCOUNT; "
		+ "   IF id_base IS NULL THEN raise_application_error (-20000, 'Основание лицевой счет не размещен в ГИС ЖКХ: отсутствует идентификатор поставщика услуг'); END IF; "
		+ " END IF; "

		+ " IF :NEW.ID_TYPE = " + VocPaymentBaseType.i.PAYMENT_DOCUMENT + " THEN "
		+ "   SELECT " + PaymentDocument.c.PAYMENTDOCUMENTID.lc() + " INTO id_base FROM " + PaymentDocument.TABLE_NAME + " WHERE UUID = :NEW.UUID_PAY_DOC; "
		+ "   IF id_base IS NULL THEN raise_application_error (-20000, 'Основание платежный документ не размещен в ГИС ЖКХ: отсутствует идентификатор платежного документа');  END IF; "
		+ " END IF; "

	    + " END IF; "

            + "END; "
	);
    }

    public enum Action {

	PLACING     (VocGisStatus.i.PENDING_RQ_PLACING,   VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_PLACING),
	ANNULMENT   (VocGisStatus.i.PENDING_RQ_ANNULMENT, VocGisStatus.i.ANNUL,    VocGisStatus.i.FAILED_ANNULMENT)
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
		case PENDING_RQ_ANNULMENT:   return ANNULMENT;
                default: return null;
            }
        }

    };
}