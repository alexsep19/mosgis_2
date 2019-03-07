package ru.eludia.products.mosgis.db.model.incoming.xl.lines;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Def;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractSubject;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.jms.xl.base.XLException;

public class InXlSupplyResourceContractSubject extends EnTable {

    public enum c implements ColEnum {

	ORD                     (Type.NUMERIC, 5, "Номер строки"),

	CODE_SR_CTR             (Type.STRING, 255, "Иной код договора"),
        UUID_SR_CTR             (Type.UUID, null, "Договор"),

	SERVICETYPE             (Type.STRING, 255, "Вид коммунальной услуги"),
	MUNICIPALRESOURCE       (Type.STRING, 255, "Тарифицируемый ресурс"),

	ERR                     (Type.STRING,  null, "Ошибка"),
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
    }

    public InXlSupplyResourceContractSubject () {

        super ("in_xl_sr_ctr_subj", "Строки импорта предметов ДРСО");

        cols  (c.class);

	for (ColEnum o : SupplyResourceContractSubject.c.values()) {

	    SupplyResourceContractSubject.c c = (SupplyResourceContractSubject.c) o;

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

        key ("uuid_xl", SupplyResourceContractSubject.c.UUID_XL);

        trigger ("BEFORE INSERT", ""
            + "DECLARE "
	    + " in_ctr in_xl_sr_ctr%ROWTYPE; "
            + "BEGIN "
            + " IF :NEW.err IS NOT NULL THEN RETURN; END IF; "

	    + " BEGIN "
	    + "  SELECT * INTO in_ctr FROM in_xl_sr_ctr WHERE uuid_xl = :NEW.uuid_xl AND err IS NULL AND code = :NEW.code_sr_ctr; "
	    + "  EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Не удалось определить договор по иному коду ' || :NEW.code_sr_ctr);"
	    + " END; "

	    + " :NEW.uuid_sr_ctr := in_ctr.uuid; "

	    + " IF :NEW.code_vc_nsi_3 IS NULL THEN BEGIN "
	    + "  SELECT code INTO :NEW.code_vc_nsi_3 FROM vc_nsi_3 WHERE isactual=1 AND label = :NEW.servicetype; "
	    + "  EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Не найдена услуга с названием ' || :NEW.servicetype); "
	    + " END; END IF; "

	    + " IF :NEW.code_vc_nsi_239 IS NULL THEN BEGIN "
	    + "  SELECT code INTO :NEW.code_vc_nsi_239 FROM vc_nsi_239 WHERE isactual=1 AND label = :NEW.municipalresource; "
	    + "  EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Не найден коммунальный ресурс с названием ' || :NEW.municipalresource); "
	    + " END; END IF; "

	    + " IF :NEW.unit IS NOT NULL THEN BEGIN "
	    + "  SELECT code INTO :NEW.unit FROM vc_okei WHERE code = :NEW.unit; "
	    + "  EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Не найдена ЕИ с кодом ' || :NEW.unit); "
	    + " END; END IF; "

	    + " EXCEPTION WHEN OTHERS THEN "
	    + " :NEW.err := REPLACE(SUBSTR(SQLERRM, 1, 1000), 'ORA-20000: ', ''); "

            + "END;"

        );

        StringBuilder sb_insert = new StringBuilder();
	StringBuilder sb_fields = new StringBuilder();

        for (SupplyResourceContractSubject.c c: SupplyResourceContractSubject.c.values ()) if (c.isToXlImport()) {
	    String col = c.lc();
	    sb_fields.append(", ");
	    sb_fields.append(col);
	    sb_insert.append(", ");
	    sb_insert.append(":NEW." + col);
        }

        trigger ("BEFORE UPDATE", ""

            + "DECLARE"
	    + " log         RAW (16); "
	    + " usr         RAW (16); "
            + " PRAGMA AUTONOMOUS_TRANSACTION; "
            + "BEGIN "

            + " IF :NEW.err IS NOT NULL THEN :NEW.is_deleted := 1; END IF; "
            + " IF NOT (:OLD.is_deleted = 1 AND :NEW.is_deleted = 0) THEN RETURN; END IF; "

	    + " INSERT INTO tb_sr_ctr_subj (uuid,uuid_sr_ctr,is_deleted" + sb_fields + ") VALUES (:NEW.uuid,:NEW.uuid_sr_ctr,0" + sb_insert + "); "

	    + " SELECT uuid_user INTO usr FROM in_xl_files WHERE uuid = :NEW.uuid_xl; "
	    + " INSERT INTO tb_sr_ctr_subj__log (action, uuid_object, uuid_user) "
	    + " VALUES ('" + VocAction.i.IMPORT_FROM_FILE.getName() + "', :NEW.uuid, usr) "
	    + " RETURNING uuid INTO log; "
	    + " UPDATE tb_sr_ctr_subj SET id_log = log, is_deleted = 1 WHERE uuid = :NEW.uuid; "

            + " COMMIT; "
            + "END; "

        );

    }

    private static final Logger logger = Logger.getLogger(InXlSupplyResourceContractSubject.class.getName());

    public static Map<String, Object> toHash (UUID uuid, int ord, XSSFRow row, Map<String, Map<String, Object>> vocs) {
        
        Map<String, Object> r = DB.HASH (
            EnTable.c.IS_DELETED, 1,
            SupplyResourceContractSubject.c.UUID_XL, uuid,
            c.ORD, ord    
        );
        
        try {
            setFields (r, row, vocs);
        }         
        catch (XLException ex) {
            r.put (c.ERR.lc (), ex.getMessage ());
        }

	logger.info("InXlSupplyResourceContractSubject.r=" + DB.to.json(r));

        return r;
        
    }
    
    private static void setFields (Map<String, Object> r, XSSFRow row, Map<String, Map<String, Object>> vocs) throws XLException {

	r.put(c.CODE_SR_CTR.lc(), toString(row, 0, "Не указан Иной код договора (столбец A)"));

	try {
	    final XSSFCell cell = row.getCell(1);
	    if (cell == null) {
		throw new XLException("Не указана Коммунальная услуга(столбец B)");
	    }
	    final String s = cell.getStringCellValue();
	    if (!DB.ok(s)) {
		throw new XLException("Не указана Коммунальная услуга(столбец B)");
	    }
	    r.put(c.SERVICETYPE.lc(), s);

	    r.put(SupplyResourceContractSubject.c.CODE_VC_NSI_3.lc(), vocs.get("vc_nsi_3").get(s));

	} catch (XLException ex) {
	    throw ex;
	} catch (Exception ex) {
	    throw new XLException(ex.getMessage());
	}

        try {
            final XSSFCell cell = row.getCell (2);
            if (cell == null) throw new XLException ("Не указан Коммунальный ресурс(столбец C)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указан Коммунальный ресурс(столбец C)");

	    r.put (c.MUNICIPALRESOURCE.lc (), s);

	    r.put(SupplyResourceContractSubject.c.CODE_VC_NSI_239.lc(), vocs.get("vc_nsi_239").get(s));
        }
        catch (XLException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage());
        }

        try {
            final XSSFCell cell = row.getCell (3);
            if (cell == null) throw new XLException ("Не указана Дата начала поставки ресурса(столбец D)");
            final Date d = cell.getDateCellValue ();
            if (d == null) throw new XLException ("Не указана Дата начала поставки ресурса(столбец D)");
            r.put (SupplyResourceContractSubject.c.STARTSUPPLYDATE.lc (), d);
        }
        catch (XLException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage());
        }

	try {
	    final XSSFCell cell = row.getCell(4);
	    if (cell == null) {
		throw new XLException("Не указана Дата окончания поставки ресурса(столбец E)");
	    }
	    final Date d = cell.getDateCellValue();
	    if (d == null) {
		throw new XLException("Не указана Дата окончания поставки ресурса(столбец E)");
	    }
	    r.put(SupplyResourceContractSubject.c.ENDSUPPLYDATE.lc(), d);
	} catch (XLException ex) {
	} catch (Exception ex) {
	    throw new XLException(ex.getMessage());
	}

	r.put(SupplyResourceContractSubject.c.VOLUME.lc(), toNumeric(row, 5));

	try {
	    final XSSFCell cell = row.getCell(6);
	    if (cell == null) {
		throw new XLException("Не указан Код единицы измерения(столбец G)");
	    }
	    final String s = cell.getStringCellValue();
	    if (!DB.ok(s)) {
		throw new XLException("Не указан Код единицы измерения(столбец G)");
	    }
	    r.put(SupplyResourceContractSubject.c.UNIT.lc(), s);
	} catch (XLException ex) {
	} catch (Exception ex) {
	    throw new XLException(ex.getMessage());
	}

	try {
	    final XSSFCell cell = row.getCell(7);
	    if (cell == null) {
		throw new XLException("Не указан Режим подачи(столбец H)");
	    }
	    final String s = cell.getStringCellValue();
	    if (!DB.ok(s)) {
		throw new XLException("Не указан Режим подачи(столбец H)");
	    }
	    r.put(SupplyResourceContractSubject.c.FEEDINGMODE.lc(), s);
	} catch (XLException ex) {
	} catch (Exception ex) {
	    throw new XLException(ex.getMessage());
	}
    }
}