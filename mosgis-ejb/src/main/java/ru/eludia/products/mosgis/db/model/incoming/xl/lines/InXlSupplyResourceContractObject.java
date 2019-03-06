package ru.eludia.products.mosgis.db.model.incoming.xl.lines;

import java.util.Map;
import java.util.UUID;
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
import ru.eludia.products.mosgis.db.model.tables.Premise;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractObject;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.jms.xl.base.XLException;

public class InXlSupplyResourceContractObject extends EnTable {

    public enum c implements ColEnum {

        ORD                     (Type.NUMERIC, 5, "Номер строки"),

	CODE_SR_CTR             (Type.STRING, 255, "Иной код договора"),
	UUID_SR_CTR             (Type.UUID, null, "Договор"),

	IS_CONDO                (Type.BOOLEAN, null, "1 для МКД, 0 для ЖД"),
	ADDRESS                 (Type.STRING, null, "Адрес"),
        UNOM                    (Type.NUMERIC, 15, null, "UNOM"),

	APARTMENTNUMBER         (Type.STRING, null, "Номер помещения / Номер блока"),
	ROOMNUMBER              (Type.STRING, null, "Номер комнаты"),
	ROOMTYPE                (Type.STRING, null, "Характеристика помещения"),
	IS_ADD_ROOM             (Type.BOOLEAN, null, "1 для МКД, 0 для ЖД"),

	AREA                    (Type.NUMERIC, 25, 4, null, "Площадь"),
	ENTRANCENUM             (Type.STRING, null, "Номер подъезда"),

	COUNTINGRESOURCE        (Type.BOOLEAN, null, "1, если РСО размещает информацию о начислениях за коммунальные услуги; 0 - размещает Исполнитель коммунальных услуг."),
	MDINFO                  (Type.BOOLEAN, null, "1, если РСО размещает информацию об индивидуальных приборах учета и их показаниях; иначе 0"),

	ERR                     (Type.STRING,  null, "Ошибка"),

        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
    }
    
    public static Map<String, Object> toHash (UUID uuid, int ord, XSSFRow row) {
        
        Map<String, Object> r = DB.HASH (
            EnTable.c.IS_DELETED, 1,
            SupplyResourceContractObject.c.UUID_XL, uuid,
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
        
        try {
            final XSSFCell cell = row.getCell (0);
            if (cell == null) throw new XLException ("Не указан иной код договора (столбец A)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указан иной код договора (столбец A)");
            r.put (c.CODE_SR_CTR.lc (), s);
        }
        catch (XLException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage());
        }

        try {
            final XSSFCell cell = row.getCell (2);
            if (cell == null) throw new XLException ("Не указан адрес (столбец B)");
            r.put (c.ADDRESS.lc (), cell.getStringCellValue ());
        }
        catch (Exception ex) {
            throw new XLException(ex.getMessage());
        }

        try {
            final XSSFCell cell = row.getCell (3);
            if (cell == null) throw new XLException ("Не указан UNOM (столбец D)");
            r.put (c.UNOM.lc (), cell.getStringCellValue());
        }
        catch (XLException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new XLException(ex.getMessage());
        }

	try {
	    final XSSFCell cell = row.getCell(4);
	    if (cell == null) {
		throw new XLException("Не указан Номер квартиры (помещения) / номер блока (столбец E)");
	    }
	    r.put(c.APARTMENTNUMBER.lc(), cell.getStringCellValue());
	} catch (XLException ex) {
	} catch (Exception ex) {
	    throw new XLException(ex.getMessage());
	}

	try {
	    final XSSFCell cell = row.getCell(5);
	    if (cell == null) {
		throw new XLException("Не указан Номер комнаты(столбец F)");
	    }
	    r.put(c.ROOMNUMBER.lc(), cell.getStringCellValue());
	} catch (XLException ex) {
	} catch (Exception ex) {
	    throw new XLException(ex.getMessage());
	}
    }

    public InXlSupplyResourceContractObject () {

        super ("in_xl_sr_ctr_obj", "Строки импорта объектов жилищного фонда ДРСО");

        cols  (c.class);

	for (ColEnum o : SupplyResourceContractObject.c.values()) {

	    SupplyResourceContractObject.c c = (SupplyResourceContractObject.c) o;

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

        key ("uuid_xl", SupplyResourceContractObject.c.UUID_XL);
        
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

            + "BEGIN "
            + " SELECT fiashouseguid INTO :NEW.fiashouseguid FROM vc_unom WHERE unom=:NEW.unom; "
            + " EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Не удалось однозначно определить GUID ФИАС по UNOM');"
            + "END;"

            + " EXCEPTION WHEN OTHERS THEN "
            + " :NEW.err := REPLACE(SUBSTR(SQLERRM, 1, 1000), 'ORA-20000: ', ''); "

            + "END;"

        );
        
        StringBuilder sb = new StringBuilder ();
        StringBuilder nsb = new StringBuilder ();
        
        for (SupplyResourceContractObject.c c: SupplyResourceContractObject.c.values ()) if (c.isToXlImport()) {
            sb.append (',');
            sb.append (c.lc ());
            nsb.append (",:NEW.");
            nsb.append (c.lc ());
        }        

        trigger ("BEFORE UPDATE", ""

            + "DECLARE"
	    + " log         RAW (16); "
	    + " usr         RAW (16); "
            + " PRAGMA AUTONOMOUS_TRANSACTION; "
            + "BEGIN "

            + " IF :NEW.err IS NOT NULL THEN :NEW.is_deleted := 1; END IF; "
            + " IF NOT (:OLD.is_deleted = 1 AND :NEW.is_deleted = 0) THEN RETURN; END IF; "

	    + " INSERT INTO tb_sr_ctr_obj (uuid,uuid_sr_ctr,is_deleted" + sb + ") VALUES (:NEW.uuid,:NEW.uuid_sr_ctr,0" + nsb + "); "

	    + " SELECT uuid_user INTO usr FROM in_xl_files WHERE uuid = :NEW.uuid_xl; "
	    + " INSERT INTO tb_sr_ctr_obj__log (action, uuid_object, uuid_user) "
	    + " VALUES ('" + VocAction.i.IMPORT_FROM_FILE.getName() + "', :NEW.uuid, usr) "
	    + " RETURNING uuid INTO log; "
	    + " UPDATE tb_sr_ctr_obj SET id_log = log, is_deleted = 1 WHERE uuid = :NEW.uuid; "

            + " COMMIT; "

            + "END; "

        );        

    }

}