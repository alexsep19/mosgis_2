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
import ru.eludia.base.model.def.Bool;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;

public class InXlHouseInfo extends EnTable {
    
    public enum c implements ColEnum {
        
        UUID_XL                 (InXlFile.class, "Файл импорта"),
        
        ORD                     (Type.NUMERIC, 5, "Номер строки"),
        
        ADDRESS                 (Type.STRING, "Адрес"),

        RESIDENTSCOUNT          (Type.INTEGER, null, "Количество проживающих"),
        HASUNDERGROUNDPARKING   (Type.BOOLEAN, null, "Наличие подземного паркинга"),
        
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
    
    private static void setFields (Map<String, Object> r, XSSFRow row) throws InXlHouseInfo.XLException {
        
        try {
            final XSSFCell cell = row.getCell (0);
            if (cell == null) throw new XLException ("Не указан адрес (столбец A)");
            r.put (c.ADDRESS.lc (), cell.getStringCellValue ());
        }
        catch (Exception ex) {
            throw new XLException ("Некорректный тип ячейки адреса (столбец A)");
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
                    final String countS = cell.getStringCellValue ();
                    if (!DB.ok (s)) throw new XLException ("Не указан параметр (столбец C)");
                    r.put (c.RESIDENTSCOUNT.lc (), countS);
                    break;
                case "Наличие подземного паркинга (логическое)":
                    final XSSFCell parkingCell = row.getCell (2);
                    if (parkingCell == null) throw new XLException ("Не указан параметр (столбец C)");
                    final String parkingS = cell.getStringCellValue ();
                    if (!DB.ok (s)) throw new XLException ("Не указан параметр (столбец C)");
                    switch (parkingS) {
                        case "Да":
                            r.put (c.HASUNDERGROUNDPARKING.lc (), Bool.TRUE);
                            break;
                        case "Нет":
                            r.put (c.HASUNDERGROUNDPARKING.lc (), Bool.FALSE);
                            break;
                        default:
                            throw new XLException ("Указан неверный параметр (столбец C): " + parkingS);
                    }
                    break;
                default:
                    throw new XLException ("Указан неверный параметр (столбец B): " + s);
            }
        }
        catch (XLException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new XLException ("Некорректный тип ячейки параметра (столбец B или C)");
        }
        
    }
    
    public InXlHouseInfo () {
        
        super ("in_xl_houses_info", "Строки импорта информации о жилых домах");

        cols  (c.class);
        
        key   ("uuid_xl", c.UUID_XL);
        
        trigger ("BEFORE INSERT", ""
                + "DECLARE "
                    + "cnt NUMBER;"
                + "BEGIN "
                    + "IF :NEW.err IS NOT NULL THEN RETURN; END IF; "
                    + "BEGIN "
                        + "SELECT COUNT (*) INTO cnt FROM in_xl_houses r WHERE r.uuid_xl = :NEW.uuid_xl AND r.address = :NEW.address; "
                        + "IF cnt = 0 THEN "
                            + "raise_application_exception (-20000, 'По указанному адресу не найдено информации по импорту'); "
                        + "ELSEIF cnt > 1 THEN "
                            + "raise_aaplication_exception (-20000, 'Для указанной информации нет соответствующего дома'); "
                    + "END; "
                + "END; "
        );
        
        trigger ("BEFORE UPDATE", ""
                + "DECLARE" 
                    + "PRAGMA AUTONOMOUS_TRANSACTION; "
                + "BEGIN "
                
                    + "IF :NEW.err IS NOT NULL THEN :NEW.is_deleted := 1; END IF; "
                    + "IF NOT (:OLD.is_deleted = 1 AND :NEW.is_deleted = 0) THEN RETURN; END IF; "
                
                    + "INSERT INTO tb_houses (uuid,is_deleted" + sb + ") VALUES (:NEW.uuid,0" + nsb + "); "
                    + "UPDATE tb_houses SET is_deleted=1 WHERE uuid=:NEW.uuid; "
                    + "COMMIT; "

                + "END; "
        );
        
    }
    
}
