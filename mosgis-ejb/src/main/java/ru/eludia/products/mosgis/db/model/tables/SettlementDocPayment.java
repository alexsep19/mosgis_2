package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Num;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;

public class SettlementDocPayment extends EnTable {

    public enum c implements EnColEnum {

	UUID_ST_DOC           (SettlementDoc.class, "Документ информации о состоянии расчетов"),

	UUID_SR_CTR           (SupplyResourceContract.class, "Договор ресурсоснабжения из документа о состоянии расчетов"),

	ID_SP_STATUS          (VocGisStatus.class, new Num(VocGisStatus.i.PROJECT.getId()), "Статус расчетов с точки зрения mosgis"),

	YEAR                  (Type.NUMERIC, 4, "Год"),
	MONTH                 (Type.NUMERIC, 2, "Месяц 1-12"),

	CREDITED              (Type.NUMERIC, 10, 2, null, "Начислено, руб"),
	RECEIPT               (Type.NUMERIC, 10, 2, null, "Поступило, руб"),
	DEBTS                 (Type.NUMERIC, 10, 2, null, "Размер задолженности (-)/переплаты (+) за период"),
	PAID                  (Type.NUMERIC, 10, 2, null, "Оплачено"),

	REASONOFANNULMENT     (Type.STRING, 1000, null, "Причина аннулирования"),
	IS_ANNULED            (Type.BOOLEAN, new Virt("DECODE(\"REASONOFANNULMENT\",NULL,0,1)"), "1, если запись аннулирована; иначе 0"),

	ID_LOG                (SettlementDocPaymentLog.class, null, "Последнее событие редактирования")
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
                case UUID_ST_DOC:
                case ID_LOG:
                    return false;
                default:
                    return true;
            }
        }
    }

    public SettlementDocPayment () {

        super ("tb_st_doc_ps", "Расчеты договора РСО");

        cols  (c.class);

        key   ("uuid_st_doc", c.UUID_ST_DOC);

	key   ("year", c.YEAR, c.MONTH);

	trigger("BEFORE INSERT OR UPDATE", ""
	    + "DECLARE "
	    + " PRAGMA AUTONOMOUS_TRANSACTION; "
	    + " cnt NUMBER; "
	    + " dt_from DATE; "
	    + " dt_to DATE; "
	    + "BEGIN "

	    + " SELECT uuid_sr_ctr INTO :NEW.uuid_sr_ctr FROM tb_st_docs WHERE uuid = :NEW.uuid_st_doc; "

	    + " dt_from := ADD_MONTHS(TO_DATE(:NEW.year || '-01-01', 'YYYY-MM-DD'), :NEW.month - 1); "
	    + " dt_to   := ADD_MONTHS(dt_from, 1) - 1; "


	    + " FOR i IN (SELECT effectivedate dt_from, completiondate dt_to FROM tb_sr_ctr WHERE uuid = :NEW.uuid_sr_ctr AND completiondate < dt_from OR effectivedate > dt_to"
	    + ") LOOP"
	    + "    raise_application_error (-20000, 'Период начисления должен входить в период действия договора' "
	    + "      || ' с ' || TO_CHAR (i.dt_from, 'DD.MM.YYYY') "
	    + "      || CASE WHEN i.dt_to IS NULL THEN NULL ELSE ' по ' || TO_CHAR (i.dt_to, 'DD.MM.YYYY') END "
	    + "      || '. Операция прервана.'); "
	    + " END LOOP; "

	    + " IF :NEW.is_deleted = 0 THEN "
		+ " FOR i IN ("
		+ "SELECT "
		+ " o.uuid "
		+ "FROM "
		+ " tb_st_doc_ps o "
		+ "WHERE o.is_deleted = 0 "
		+ " AND o.uuid <> :NEW.uuid "
		+ " AND o.uuid_st_doc     = :NEW.uuid_st_doc "
		+ " AND o.year            = :NEW.year "
		+ " AND o.month           = :NEW.month "
		+ " AND o.id_sp_status    <> " + VocGisStatus.i.ANNUL
		+ ") LOOP"
		+ " raise_application_error (-20000, "
		+ "'Указанный период внесения платы пересекается с уже имеющейся в системе информацией' "
		+ "|| '. Скорректируйте значения.'); "
		+ " END LOOP; "
	    + " END IF; " // IF :NEW.is_deleted = 0

	    + " IF UPDATING THEN "
	    + "  IF :NEW.is_deleted = 1 AND :OLD.is_deleted = 0 AND :OLD.id_sp_status <> " + VocGisStatus.i.PROJECT + " THEN "
	    + "    raise_application_error (-20000, 'Можно удалять записи только в статусе Проект. Операция прервана.'); END IF; "
	    + " END IF; "

	    + "END;");
    }

}
