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
import ru.eludia.products.mosgis.db.model.tables.Block;

public class InXlLivingRoom extends EnTable {
    
    private static final Logger logger = Logger.getLogger(InXlLivingRoom.class.getName());
    
    public enum c implements ColEnum {
        
        UUID_XL                 (InXlFile.class,            "Файл импорта"),
        
        ORD                     (Type.NUMERIC, 5,           "Номер строки"),
        
        HOUSE_UNOM              (Type.NUMERIC, 12, null, "UNOM дома"),
        UUID_BLOCK              (Block.class,      null, "Ссылка на блок"),
        
        ADDRESS                 (Type.STRING,      null, "Адрес"),
        BLOCKNUM                (Type.STRING, 255, null, "Номер блока"),
        ROOMNUMBER              (Type.STRING, 255, null, "Номер комнаты"),
        
        SQUARE                  (Type.NUMERIC, 25, 4, null, "Площадь"),
        CADASTRALNUMBER         (Type.STRING,         null, "Кадастровый номер"),
        
        INFORMATIONCONFIRMED    (Type.BOOLEAN, null, "Информация подтверждена поставщиком"),
        
        F_20130                 (Type.NUMERIC, 10, null, "Количество граждан, проживающих в комнате в коммунальной квартире"),
        F_21821                 (Type.NUMERIC, 19,4, null, "Площадь общего имущества в коммунальной квартире"),
        
        ERR                     (Type.STRING,  null,  "Ошибка")
        
        ;
            
        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
        
        boolean isToCopy () {
            
            switch (this) {
                case UUID_XL:
                case UUID_BLOCK:
                case ROOMNUMBER:
                case SQUARE:
                case CADASTRALNUMBER:
                case INFORMATIONCONFIRMED:
                case F_20130:
                case F_21821:
                    return true;
                default:
                    return false;
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
    
    private static class XLException extends Exception {
        public XLException (String s) {
            super (s);
        }        
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
            if (cell != null) {
                final String s = cell.getStringCellValue ();
                if (DB.ok (s)) r.put (c.SQUARE.lc (), s);
            }
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
        try {
            final XSSFCell cell = row.getCell (4);
            if (cell == null) throw new XLException ("Не указан номер комнаты (столбец E)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указан номер комнаты (столбец E)");
            r.put (c.CADASTRALNUMBER.lc (), s);
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
        try {
            final XSSFCell cell = row.getCell (5);
            if (cell == null) throw new XLException ("Не указан признак подтверждения поставщика (столбец F)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указан признак подтверждения поставщика (столбец F)");
            switch (s) {
                case "Да":
                    r.put (c.INFORMATIONCONFIRMED.lc (), 1);
                    break;
                case "Нет":
                    r.put (c.INFORMATIONCONFIRMED.lc (), 0);
                    break;
                default:
                    throw new XLException ("Указан неверный признак подтверждения поставщика (столбец F): " + s);
            }
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
    }
    
    public InXlLivingRoom () {
        
        super ("in_xl_rooms", "Строки импорта комнат");
        
        cols  (c.class);
        
        key   ("uuid_xl", c.UUID_XL);
        
        StringBuilder sb = new StringBuilder ();
        StringBuilder nsb = new StringBuilder ();
        StringBuilder usb = new StringBuilder ();
        
        for (c c: c.values ()) if (c.isToCopy ()) {
            sb.append (',');
            sb.append (c.lc ());
            nsb.append (",:NEW.");
            nsb.append (c.lc ());
            usb.append ("," + c.lc () + " = :NEW." + c.lc ());
        }
        
        trigger ("BEFORE INSERT", ""
                + "BEGIN "
                    + "IF :NEW.err IS NOT NULL THEN RETURN; END IF; "
                + "END; "
        );
        
        trigger ("BEFORE UPDATE", ""
                + "DECLARE "
                    + "house_id RAW (16); "
                    + "cnt NUMBER; "
                    + "PRAGMA AUTONOMOUS_TRANSACTION; "
                + "BEGIN "
                    + "BEGIN "
                        + "IF :NEW.err IS NOT NULL THEN :NEW.is_deleted := 1; END IF; "
                        + "IF NOT (:OLD.is_deleted = 1 AND :NEW.is_deleted = 0) THEN RETURN; END IF; "
                
                        + "BEGIN "
                            + "SELECT uuid INTO house_id FROM tb_houses h WHERE h.unom = :NEW.house_unom AND h.address = :NEW.address; "
                        + "EXCEPTION WHEN NO_DATA_FOUND THEN "
                            + "raise_application_error (-20000, 'ЖД по заданным UNOM и адресу не найден'); "
                        + "END; "
                
                        + "IF :NEW.blocknum IS NOT NULL THEN "
                            + "SELECT uuid INTO :NEW.uuid_block FROM tb_blocks b WHERE b.uuid_house = house_id AND b.blocknum = :NEW.blocknum; "
                        + "END IF; "
                
                        + "SELECT COUNT (*) INTO cnt "
                            + "FROM tb_living_rooms r INNER JOIN tb_houses h ON r.uuid_house = h.uuid "
                            + "WHERE r.roomnumber = :NEW.roomnumber AND h.uuid = house_id; "
                        + "IF cnt = 0 THEN "
                            + "INSERT INTO tb_living_rooms (uuid_house" + sb + ") "
                                           + "VALUES (house_id" + nsb + "); "
                        + "ELSE "
                            + "UPDATE tb_living_rooms SET " + usb.substring(1) + " WHERE uuid_house = house_id AND roomnumber = :NEW.roomnumber; "
                        + "END IF; "
                        + "COMMIT; "
                    + "EXCEPTION WHEN NO_DATA_FOUND THEN "
                        + ":NEW.err := REPLACE(SUBSTR(SQLERRM, 1, 1000), 'ORA-20000: ', ''); "
                    + "END; "
                + "END; "
        );
        
    }
    
}
