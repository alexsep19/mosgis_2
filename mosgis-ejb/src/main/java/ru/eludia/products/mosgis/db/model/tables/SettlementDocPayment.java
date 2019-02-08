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

//	trigger("BEFORE INSERT OR UPDATE", ""
//	    + "DECLARE"
////	    + " PRAGMA AUTONOMOUS_TRANSACTION; "
//	    + "BEGIN "
//	    + " IF :NEW.is_deleted = 0 THEN BEGIN "
//	    + " END; END IF; " // IF :NEW.is_deleted = 0
//	    + "END;");
    }

}
