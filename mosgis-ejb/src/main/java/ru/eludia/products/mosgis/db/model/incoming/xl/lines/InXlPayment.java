package ru.eludia.products.mosgis.db.model.incoming.xl.lines;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.apache.poi.xssf.usermodel.XSSFRow;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Def;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.db.model.tables.Payment;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.jms.xl.base.XLException;

public class InXlPayment extends EnTable {

    public static final String TABLE_NAME = "in_xl_payments";

    public enum c implements ColEnum {

        UUID_XL                 (InXlFile.class,            "Файл импорта"),
        ORD                     (Type.NUMERIC, 5,           "Номер строки"),
        ERR                     (Type.STRING,  null,        "Ошибка"),

	SERVICEID               (Type.STRING,      null,    "Идентификатор жилищно-коммунальной услуги"),
	PAYMENTDOCUMENTID       (Type.STRING,  18, null,   "Идентификатор платежного документа")
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        boolean isToCopy () {

            switch (this) {
                case ORD:
                case ERR:
                case SERVICEID:
                case PAYMENTDOCUMENTID:
                    return false;                    
                default:                    
                    return true;
            }

        }

    }
    
    public static Map<String, Object> toHash (UUID uuid, int ord, XSSFRow row) {
        
        Map<String, Object> r = DB.HASH (
            EnTable.c.IS_DELETED, 1,
            c.UUID_XL, uuid,
            c.ORD, ord    
        );
        
        try {
            setFields (r, row);
        }         
        catch (XLException ex) {
            r.put (c.ERR.lc (), ex.getMessage ());
        }
        
        return r;
        
    }

    private static void setFields (Map<String, Object> r, XSSFRow row) throws XLException {

	r.put (Payment.c.AMOUNT.lc (),     toNumeric (row, 1, "Не указана сумма"));
        r.put (Payment.c.ORDERDATE.lc (),  toDate (row, 2, "Не указана дата"));

	String period = toString(row, 3, "Не указан период оплаты");
	try {
	    String[] p = period.split(".");
	    Integer month = Integer.parseInt(p[0]);
	    Integer year = Integer.parseInt(p[1]);
	    r.put(Payment.c.MONTH.lc(), month);
	    r.put(Payment.c.YEAR.lc(), year);
	} catch (Exception e) {
	    throw new XLException (e.getMessage());
	}

	r.put(c.PAYMENTDOCUMENTID.lc(), toDate(row, 4));
	r.put(c.SERVICEID.lc(), toDate(row, 5));
    }

    public InXlPayment () {

        super (TABLE_NAME, "Строки импорта приборов учёта");

        cols  (c.class);

	for (ColEnum o : Payment.c.values()) {

	    Payment.c c = (Payment.c) o;

	    if (!c.isToXlImport()) {
		continue;
	    }

	    Col col = c.getCol().clone();

	    Def def = col.getDef();
	    boolean isVirtual = def != null && def instanceof Virt;

	    if (!isVirtual) {
		col.setDef(null);
		col.setNullable(true);
	    }

	    add(col);
	}

	key ("uuid_xl", c.UUID_XL);
        
        trigger ("BEFORE INSERT", ""
                
            + "DECLARE "
            + " cnt NUMBER; "
            + "BEGIN "
                
            + " SELECT uuid_org INTO :NEW.uuid_org FROM in_xl_files WHERE uuid=:NEW.uuid_xl; "

            + " IF :NEW.err IS NOT NULL THEN RETURN; END IF; "

	    + " IF :NEW.paymentdocumentid IS NOT NULL THEN "
            + "   SELECT COUNT(*), MIN(paymentdocumentid) INTO cnt, :NEW.paymentdocumentid FROM tb_pay_docs WHERE is_deleted=0 AND paymentdocumentid=:NEW.paymentdocumentid; "
            + "   IF cnt=0 THEN raise_application_error (-20000, 'Неизвестное значение идентификатора платежного документа: ' || :NEW.paymentdocumentid); END IF; "
            + "   IF cnt>1 THEN raise_application_error (-20000, 'Идентификатору платежного документа ' || :NEW.paymentdocumentid || ' соответствует не одна запись ПД, а ' || cnt); END IF; "
	    + " END IF; "

	    + " IF :NEW.serviceid IS NOT NULL THEN "
            + "   SELECT COUNT(*), MIN(serviceid) INTO cnt, :NEW.serviceid FROM tb_accounts WHERE is_deleted=0 AND serviceid=:NEW.serviceid; "
            + "   IF cnt=0 THEN raise_application_error (-20000, 'Неизвестное значение идентификатора поставщика услуг ЛС: ' || :NEW.paymentdocumentid); END IF; "
            + "   IF cnt>1 THEN raise_application_error (-20000, 'Идентификатору поставщика услуг' || :NEW.paymentdocumentid || ' соответствует не одна запись ЛС, а ' || cnt); END IF; "
	    + " END IF; "

	    + " IF :NEW.serviceid IS NULL AND :NEW.paymentdocumentid IS NULL THEN "
	    + "   raise_application_error (-20000, 'Не указан ни идентификатор платежного документа, ни идентификатор поставщика услуг'); "
	    + " END IF; "

	    + " EXCEPTION WHEN OTHERS THEN "
            + " :NEW.err := REPLACE(SUBSTR(SQLERRM, 1, 1000), 'ORA-20000: ', ''); "

            + "END;"

        );
        
        StringBuilder sb = new StringBuilder ();
        StringBuilder nsb = new StringBuilder ();
	StringBuilder upsb = new StringBuilder();
        
        for (c c: c.values ()) if (c.isToCopy ()) {
            sb.append (',');
            sb.append (c.lc ());
            nsb.append (",:NEW.");
            nsb.append (c.lc ());
            upsb.append ("," + c.lc() + "=n." + c.lc());
        }        

        trigger ("BEFORE UPDATE", ""

            + "BEGIN "
            + " IF :NEW.err IS NOT NULL THEN :NEW.is_deleted := 1; END IF; "
            + "END; "

        );        
        
        trigger ("AFTER UPDATE", ""

            + "DECLARE" 
            + " PRAGMA AUTONOMOUS_TRANSACTION; "
            + "BEGIN "

            + " IF NOT (:OLD.is_deleted = 1 AND :NEW.is_deleted = 0) THEN RETURN; END IF; "

            + " INSERT INTO " + Payment.TABLE_NAME + " (uuid,is_deleted" + sb + ") VALUES (:NEW.uuid,1" + nsb + "); "

//	    + "   MERGE INTO " + Payment.TABLE_NAME + "  o "
//	    + "    USING (SELECT :NEW.uuid, 1 is_deleted" + nsb + " FROM DUAL) n"
//	    + "    ON (o.orderdate = n.orderdate AND o.year = n.year AND o.month = n.month"
//	    + "      AND NVL(o.paymentdocumentid, '00') = NVL(n.paymentdocumentid, '00') "
//	    + "      AND NVL(o.serviceid,'00') = NVL(:NEW.serviceid,'00') "
//	    + "     )"
//	    + "    WHEN MATCHED THEN UPDATE SET is_deleted=0" + upsb
//	    + "    WHEN NOT MATCHED THEN INSERT (uuid, is_deleted" + sb + ") VALUES (:NEW.uuid, 1" + nsb + "); "

		+ " COMMIT; "

            + "END; "

        );        

    }

}