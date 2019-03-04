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

public class InXlLivingRoomInfo extends EnTable {
    
    private static final Logger logger = Logger.getLogger(InXlLivingRoomInfo.class.getName());
    
    public enum c implements ColEnum {
        
        UUID_XL                 (InXlFile.class,            "Файл импорта"),
        
        ORD                     (Type.NUMERIC, 5,           "Номер строки"),
        
        ADDRESS                 (Type.STRING,      null, "Адрес"),
        BLOCKNUM                (Type.STRING, 255, null, "Номер блока"),
        ROOMNUMBER              (Type.STRING, 255, null, "Номер комнаты"),
        
        F_20130                 (Type.NUMERIC, 10, null, "Количество граждан, проживающих в комнате в коммунальной квартире"),
        F_21821                 (Type.NUMERIC, 19,4, null, "Площадь общего имущества в коммунальной квартире"),
        
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
            if (cell != null) {
                final String s = cell.getStringCellValue ();
                if (DB.ok (s)) r.put (c.BLOCKNUM.lc (), s);
            }
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
        try {
            final XSSFCell cell = row.getCell (2);
            if (cell == null) throw new XLException ("Не указан номер комнаты (столбец C)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указан номер комнаты (столбец C)");
            r.put (c.ROOMNUMBER.lc (), s);
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
        try {
            final XSSFCell cell = row.getCell (3);
            if (cell == null) throw new XLException ("Не указан тип параметра (столбец D)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указан тип параметра (столбец D)");
            switch (s) {
                case "Количество граждан, проживающих в комнате в коммунальной квартире (целое)":
                    final XSSFCell countCell = row.getCell (4);
                    if (countCell == null) throw new XLException ("Не указан параметр (столбец E)");
                    final String count = countCell.getStringCellValue ();
                    if (!DB.ok (count)) throw new XLException ("Не указан параметр (столбец E)");
                    r.put (c.F_20130.lc (), count);
                    break;
                case "Площадь общего имущества в коммунальной квартире (вещественное),кв.м":
                    final XSSFCell parkingCell = row.getCell (4);
                    if (parkingCell == null) throw new XLException ("Не указан параметр (столбец E)");
                    final String square = parkingCell.getStringCellValue ();
                    if (!DB.ok (square)) throw new XLException ("Не указан параметр (столбец E)");
                    r.put (c.F_21821.lc (), square);
                    break;
                default:
                    throw new XLException ("Указан неверный параметр (столбец D): " + s);
            }
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
    }
    
    public InXlLivingRoomInfo () {
        
        super ("in_xl_rooms_info", "Строки импорта информации о комнатах");
        
        cols  (c.class);
        
        key   ("uuid_xl", c.UUID_XL);
        
    }
    
}
