package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPaymentBaseType;

public class Payment extends EnTable {

    public static final String TABLE_NAME = "tb_pays";

    public enum c implements EnColEnum {

	UUID_ORG                      (VocOrganization.class, null, "Организация, которая создала данный платёжный документ"),

	ID_TYPE                       (VocPaymentBaseType.class, VocPaymentBaseType.DEFAULT,  "Тип основания для оплаты"),
	UUID_ACCOUNT                  (Account.class,         null, "Лицевой счёт основание для оплаты, заполняется всегда"),
	UUID_PAY_DOC                  (PaymentDocument.class, null, "Платежный документ основание для оплаты, заполняется если основание ПД"),

	YEAR                          (Type.NUMERIC, 4,             "Год периода расчета"),
	MONTH                         (Type.NUMERIC, 4,             "Месяц периода расчета"),
	DT_PERIOD                     (Type.DATE,           null,   "ГГГГ-ММ-01"),

	ORDERNUM                      (Type.STRING, 9, null, "Номер платежа"),

	ORDERDATE                     (Type.DATE,           "Дата внесения платы"),
	AMOUNT                        (Type.NUMERIC, 20, 2, "Сумма, руб."),

	PAYMENTPURPOSE                (Type.STRING, 1000, null, "Назначение платежа"),

	ID_CTR_STATUS                 (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус с точки зрения mosgis"),
	ID_CTR_STATUS_GIS             (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус с точки зрения ГИС ЖКХ"),

	CANCELLATIONDATE              (Type.DATE, null, "Дата аннулирования"),
	CANCELLATIONCOMMENT           (Type.STRING, 210, null, "Причина аннулирования"),
	IS_ANNULED                    (Type.BOOLEAN, new Virt("CASE WHEN CANCELLATIONDATE IS NULL THEN 0 ELSE 1 END"), "1, если запись аннулирована; иначе 0"),

	ORDERGUID                     (Type.UUID, null, "Идентификатор НПА в ГИС ЖКХ, NotificationsOfOrderExecutionGUID"),
	UNIQUENUMBER                  (Type.UUID, null, "Уникальный номер, присвоенный ГИС ЖКХ, OrderID"),

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

    }

    public Payment () {

	super  (TABLE_NAME, "Платежи");

	cols   (c.class);

	key (c.UUID_ACCOUNT);
	key (c.UUID_PAY_DOC);

	trigger ("BEFORE INSERT", ""

            + "DECLARE"
//            + "  PRAGMA AUTONOMOUS_TRANSACTION; "
            + " BEGIN "

	    + " IF :NEW.UUID_ACCOUNT IS NULL AND :NEW.UUID_PAY_DOC IS NULL THEN "
	    + "   raise_application_error (-20000, 'Укажите основание для оплаты'); "
	    + " END IF; "

	    + " IF :NEW.UUID_ACCOUNT IS NULL AND :NEW.UUID_PAY_DOC IS NOT NULL THEN "
	    + "   SELECT UUID_ACCOUNT INTO :NEW.UUID_ACCOUNT FROM " + PaymentDocument.TABLE_NAME + " WHERE uuid = :NEW.UUID_PAY_DOC; "
	    + " END IF; "

            + " :NEW.dt_period := TO_DATE (:NEW.year || LPAD (:NEW.month, 2, '0') || '01', 'YYYYMMDD'); "

            + "END; "
        );
    }

    public enum Action {

        PLACING     (VocGisStatus.i.PENDING_RQ_PLACING,   VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_PLACING)
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
                default: return null;
            }
        }

    };
}