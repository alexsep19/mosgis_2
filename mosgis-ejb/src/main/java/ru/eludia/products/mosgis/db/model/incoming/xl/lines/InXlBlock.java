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
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.ejb.ModelHolder;

public class InXlBlock extends EnTable {
    
    private static final Logger logger = Logger.getLogger(InXlHouse.class.getName());
    
    public enum c implements ColEnum {
        
        UUID_XL                 (InXlFile.class,            "Файл импорта"),
        
        ORD                     (Type.NUMERIC, 5,           "Номер строки"),
        
        HOUSE_UNOM              (Type.NUMERIC, 12, null, "UNOM дома"),
        
        ADDRESS                 (Type.STRING, null, "Адрес"),
        BLOCKNUM                (Type.STRING, 255, null, "Номер блока"),
        IS_NRS                  (Type.BOOLEAN, null, "Категория помещения (жилой/нежилой блок)"),
        CODE_VC_NSI_30          (Type.STRING, 20, null, "Характеристика помещения"),
        TOTALAREA               (Type.NUMERIC, 25, 4, null, "Общая площадь помещения"),
        GROSSAREA               (Type.NUMERIC, 25, 4, null, "Жилая площадь помещения"),
        CADASTRALNUMBER         (Type.STRING, null, "Кадастровый номер"),
        INFORMATIONCONFIRMED    (Type.BOOLEAN, null, "Информация подтверждена поставщиком"),
        
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
                case BLOCKNUM:
                case IS_NRS:
                case CODE_VC_NSI_30:
                case TOTALAREA:
                case GROSSAREA:
                case CADASTRALNUMBER:
                case INFORMATIONCONFIRMED:
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
        
        NsiTable nsi_30 = NsiTable.getNsiTable (30);
        
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
            if (cell == null) throw new XLException ("Не указан номер блока (столбец B)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указан номер блока (столбец B)");
            r.put (c.BLOCKNUM.lc (), s);
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
        try {
            final XSSFCell cell = row.getCell (2);
            if (cell == null) throw new XLException ("Не указана категория помещения (столбец C)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указана категория помещения (столбец C)");
            switch (s) {
                case "Жилое":
                    r.put (c.IS_NRS.lc (), 0);
                    break;
                case "Нежилое":
                    r.put (c.IS_NRS.lc (), 1);
                    break;
                default:
                    throw new XLException ("Указана неверная категория помещения (столбец C): " + s);
            }
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
        try {
            final XSSFCell cell = row.getCell (3);
            if (cell != null) {
                final String s = cell.getStringCellValue ();
                if (DB.ok (s)) {
                    final String code = ModelHolder.getModel ().getDb ().getString (
                        ModelHolder.getModel ()
                                .select (nsi_30, "code")
                                .where  ("f_" + nsi_30.getLabelField ().getName (), s)
                                .and    ("is_actual", 1)
                    );
                    if (code == null) throw new XLException ("Код НСИ 30 не найден для указанной характеристики помещения (столбец D)");
                    r.put (c.CODE_VC_NSI_30.lc (), code);
                }
            }
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
        try {
            final XSSFCell cell = row.getCell (4);
            if (cell != null) {
                final double area = cell.getNumericCellValue ();
                if (DB.ok (area)) r.put (c.TOTALAREA.lc (), area);
            }
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
        try {
            final XSSFCell cell = row.getCell (5);
            if (cell != null) {
                final double area = cell.getNumericCellValue ();
                if (DB.ok (area)) r.put (c.GROSSAREA.lc (), area);
            }
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
        try {
            final XSSFCell cell = row.getCell (6);
            if (cell == null) throw new XLException ("Не указан кадастровый номер (столбец G)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указан кадастровый номер (столбец G)");
            r.put (c.CADASTRALNUMBER.lc (), s);
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
        try {
            final XSSFCell cell = row.getCell (7);
            if (cell == null) throw new XLException ("Не указан признак подтверждения поставщика (столбец H)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указан признак подтверждения поставщика (столбец H)");
            switch (s) {
                case "Да":
                    r.put (c.INFORMATIONCONFIRMED.lc (), 1);
                    break;
                case "Нет":
                    r.put (c.INFORMATIONCONFIRMED.lc (), 0);
                    break;
                default:
                    throw new XLException ("Указан неверный признак подтверждения поставщика (столбец H): " + s);
            }
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
    }
    
    public InXlBlock () {
        
        super ("in_xl_blocks", "Строки импорта блоков ЖД");
        
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
                        + "BEGIN "
                            + "IF :NEW.err IS NOT NULL THEN :NEW.is_deleted := 1; END IF; "
                            + "IF NOT (:OLD.is_deleted = 1 AND :NEW.is_deleted = 0) THEN RETURN; END IF; "

                            + "SELECT uuid INTO house_id FROM tb_houses h WHERE h.unom = :NEW.house_unom AND h.address = :NEW.address; "
                        + "EXCEPTION WHEN NO_DATA_FOUND THEN "
                            + "raise_application_error (-20000, 'ЖД по заданным UNOM и адресу не найден'); "
                        + "END; "
                
                        + "SELECT COUNT (*) INTO cnt "
                            + "FROM tb_blocks b INNER JOIN tb_houses h ON b.uuid_house = h.uuid "
                            + "WHERE b.blocknum = :NEW.blocknum AND h.uuid = house_id; "
                        + "IF cnt = 0 THEN "
                            + "INSERT INTO tb_blocks (uuid_house" + sb + ") "
                                           + "VALUES (house_id" + nsb + "); "
                        + "ELSE "
                            + "UPDATE tb_blocks SET " + usb.substring(1) + " WHERE uuid_house = house_id AND blocknum = :NEW.blocknum; "
                        + "END IF; "
                        + "COMMIT; "
                    + "EXCEPTION WHEN NO_DATA_FOUND THEN "
                        + ":NEW.err := REPLACE(SUBSTR(SQLERRM, 1, 1000), 'ORA-20000: ', ''); "
                    + "END; "
                + "END; "
        );
        
    }
    
}
