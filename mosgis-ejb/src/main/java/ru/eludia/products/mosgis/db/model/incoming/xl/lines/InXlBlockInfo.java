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

public class InXlBlockInfo extends EnTable {
    
    private static final Logger logger = Logger.getLogger(InXlBlockInfo.class.getName());
    
    public enum c implements ColEnum {
        
        UUID_XL                 (InXlFile.class,            "Файл импорта"),
        
        ORD                     (Type.NUMERIC, 5,           "Номер строки"),
        
        ADDRESS                 (Type.STRING,      null, "Адрес"),
        BLOCKNUM                (Type.STRING, 255, null, "Номер блока"),
        F_20002                 (Type.NUMERIC, 10, null, "Количество комнат (НСИ 14)"),
        F_20125                 (Type.NUMERIC, 10, null, "Количество проживающих"),
        F_20003                 (Type.NUMERIC, 10, null, "Назначение помещения, относящегося к общему долевому имуществу собственников помещений (НСИ 17)"),
        
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
        
        NsiTable nsi_14 = NsiTable.getNsiTable (14);
        NsiTable nsi_17 = NsiTable.getNsiTable (17);
        
        try {
            final XSSFCell cell = row.getCell (0);
            if (cell == null) throw new XLException ("Не указан адрес (столбец A)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указан адрес (столбец A)");
            r.put (InXlHouseInfo.c.ADDRESS.lc (), s);
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
            if (cell == null) throw new XLException ("Не указан тип параметра (столбец C)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указан тип параметра (столбец C)");
            switch (s) {
                case "Количество комнат (перечислимый),шт":
                    final XSSFCell roomCountCell = row.getCell (3);
                    if (roomCountCell == null) throw new XLException ("Не указан параметр (столбец D)");
                    final String roomCount = roomCountCell.getStringCellValue ();
                    if (!DB.ok (roomCount)) throw new XLException ("Не указан параметр (столбец D)");
                    final String code_14 = ModelHolder.getModel ().getDb ().getString (
                        ModelHolder.getModel ()
                                .select (nsi_14, "code")
                                .where  ("f_" + nsi_14.getLabelField ().getName (), roomCount)
                                .and    ("is_actual", 1)
                    );
                    if (code_14 == null) throw new XLException ("Код НСИ 14 не найден для указанного количества комнат (столбец D)");
                    r.put (c.F_20002.lc (), code_14);
                    break;
                case "Количество лиц, проживающих в квартире (целое)":
                    final XSSFCell countCell = row.getCell (3);
                    if (countCell == null) throw new XLException ("Не указан параметр (столбец D)");
                    final String count = countCell.getStringCellValue ();
                    if (!DB.ok (count)) throw new XLException ("Не указан параметр (столбец D)");
                    r.put (c.F_20125.lc (), count);
                    break;
                case "Назначение помещения, относящегося к общему долевому имуществу собственников помещений (перечислимый, множественный)":
                    final XSSFCell destCell = row.getCell (3);
                    if (destCell == null) throw new XLException ("Не указан параметр (столбец D)");
                    final String dest = destCell.getStringCellValue ();
                    if (!DB.ok (dest)) throw new XLException ("Не указан параметр (столбец D)");
                    final String code_17 = ModelHolder.getModel ().getDb ().getString (
                        ModelHolder.getModel ()
                                .select (nsi_17, "code")
                                .where  ("f_" + nsi_17.getLabelField ().getName (), dest)
                                .and    ("is_actual", 1)
                    );
                    if (code_17 == null) throw new XLException ("Код НСИ 17 не найден для указанного назначения помещения (столбец D)");
                    r.put (c.F_20003.lc (), code_17);
                    break;
                default:
                    throw new XLException ("Указан неверный параметр (столбец C): " + s);
            }
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
    }
    
    public InXlBlockInfo () {
        
        super ("in_xl_block_info", "Строки импорта дополнительной информации о блоках");
        
        cols  (c.class);
        
        key   ("uuid_xl", c.UUID_XL);
        
    }
    
}
