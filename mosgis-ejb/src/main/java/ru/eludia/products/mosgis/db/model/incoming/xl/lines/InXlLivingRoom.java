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
import ru.eludia.base.model.def.Def;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.tables.LivingRoom;

public class InXlLivingRoom extends EnTable {
    
    private static final Logger logger = Logger.getLogger(InXlLivingRoom.class.getName());
    
    public enum c implements ColEnum {
        
        ORD                     (Type.NUMERIC, 5,           "Номер строки"),
        
        HOUSE_UNOM              (Type.NUMERIC, 12, null, "UNOM дома"),
        ADDRESS                 (Type.STRING,      null, "Адрес"),
        BLOCKNUM                (Type.STRING, 255, null, "Номер блока"),
        
        ERR                     (Type.STRING,  null,  "Ошибка")
        
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
            LivingRoom.c.UUID_XL, uuid,
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
            r.put (LivingRoom.c.ROOMNUMBER.lc (), s);
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
        try {
            final XSSFCell cell = row.getCell (3);
            if (cell != null) {
                final String s = cell.getStringCellValue ();
                if (DB.ok (s)) r.put (LivingRoom.c.SQUARE.lc (), s);
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
            r.put (LivingRoom.c.CADASTRALNUMBER.lc (), s);
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
                    r.put (LivingRoom.c.INFORMATIONCONFIRMED.lc (), 1);
                    break;
                case "Нет":
                    r.put (LivingRoom.c.INFORMATIONCONFIRMED.lc (), 0);
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
        
        for (ColEnum o: LivingRoom.c.values ()) {
            
            LivingRoom.c c = (LivingRoom.c) o;
                
            if (!c.isToXlImport ()) continue;
            
            Col col = c.getCol ().clone ();
                
                Def def = col.getDef ();
                boolean isVirtual = def != null && def instanceof Virt;
                
                if (!isVirtual) {
                    col.setDef (null);
                    col.setNullable (true);
                }
                
                add (col);
            
        }
        
        key   ("uuid_xl", LivingRoom.c.UUID_XL);
        
        StringBuilder sb = new StringBuilder ();
        StringBuilder nsb = new StringBuilder ();
        StringBuilder usb = new StringBuilder ();
        
        for (LivingRoom.c c: LivingRoom.c.values ()) if (c.isToXlImport ()) {
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
