package ru.eludia.products.mosgis.db.model.incoming.xl.lines;

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
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;

public class InXlHouseInfo extends EnTable {
    
    private static final Logger logger = Logger.getLogger(InXlHouseInfo.class.getName());
    
    public enum c implements ColEnum {
        
        UUID_XL                 (InXlFile.class, "Файл импорта"),
        
        ORD                     (Type.NUMERIC, 5, "Номер строки"),
        
        ADDRESS                 (Type.STRING, null, "Адрес"),

        F_20140                 (Type.NUMERIC, 10, null, "Количество проживающих"),
        F_20819                 (Type.BOOLEAN, null, "Наличие подземного паркинга"),
        
        ERR                     (Type.STRING,  null,  "Ошибка")
        
        ;
        
        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
        
    }
    
    private static class XLException extends Exception {
        public XLException (String s) {
            super (s);
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
        
        try {
            final XSSFCell cell = row.getCell (0);
            if (cell == null) throw new XLException ("Не указан адрес (столбец A)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указан адрес (столбец A)");
            r.put (c.ADDRESS.lc (), s);
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
        try {
            final XSSFCell cell = row.getCell (1);
            if (cell == null) throw new XLException ("Не указан тип параметра (столбец B)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указан тип параметра (столбец B)");
            switch (s) {
                case "Количество проживающих (целое)":
                    final XSSFCell countCell = row.getCell (2);
                    if (countCell == null) throw new XLException ("Не указан параметр (столбец C)");
                    final String count = countCell.getStringCellValue ();
                    if (!DB.ok (count)) throw new XLException ("Не указан параметр (столбец C)");
                    r.put (c.F_20140.lc (), count);
                    break;
                case "Наличие подземного паркинга (логическое)":
                    final XSSFCell parkingCell = row.getCell (2);
                    if (parkingCell == null) throw new XLException ("Не указан параметр (столбец C)");
                    final String parkingS = parkingCell.getStringCellValue ();
                    if (!DB.ok (parkingS)) throw new XLException ("Не указан параметр (столбец C)");
                    switch (parkingS) {
                        case "Да":
                            r.put (c.F_20819.lc (), 1);
                            break;
                        case "Нет":
                            r.put (c.F_20819.lc (), 0);
                            break;
                        default:
                            throw new XLException ("Указан неверный параметр (столбец C): " + parkingS);
                    }
                    break;
                default:
                    throw new XLException ("Указан неверный параметр (столбец B): " + s);
            }
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
    }
    
    public InXlHouseInfo () {
        
        super ("in_xl_houses_info", "Строки импорта информации о жилых домах");

        cols  (c.class);
        
        key   ("uuid_xl", c.UUID_XL);
        
    }
    
}
