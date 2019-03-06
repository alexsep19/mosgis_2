package ru.eludia.products.mosgis.db.model;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.BOOLEAN;
import static ru.eludia.base.model.def.Bool.FALSE;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.products.mosgis.jms.xl.base.XLException;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import ru.eludia.base.DB;

public abstract class EnTable extends Table {

    public static void appendNotDeleted (StringBuilder sb) {
        sb.append (c.IS_DELETED.lc ());
        sb.append ("=0");
    }

    public enum c implements EnColEnum {

        UUID                      (Type.UUID,    NEW_UUID,    "Ключ"),        
        IS_DELETED                (BOOLEAN,      FALSE,  "1, если запись удалена; иначе 0")
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {
            return this == IS_DELETED;
        }

    }

    public EnTable (String name, String remark) {
        super (name, remark);
        cols  (c.class);
        pk    (c.UUID);
    }

    public static Object toNumeric(XSSFRow row, int col, Object error) throws XLException {

	Object result = toNumeric(row, col);

	if (result == null) {
	    throw new XLException(error.toString());
	}

	return result;
    }

    public static Object toNumeric(XSSFRow row, int col) throws XLException {

	String s;

	try {
	    final XSSFCell cell = row.getCell(col);
	    if (cell == null) {
		return null;
	    }

	    s = cell.getStringCellValue();

	    s = s.trim().replaceAll("\\D", "");

	    if (!DB.ok(s)) {
		return null;
	    }
	} catch (Exception ex) {
	    throw new XLException(ex.getMessage());
	}

	return DB.to.Long(s);
    }
}