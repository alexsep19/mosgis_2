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
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.ejb.ModelHolder;

public class InXlHouse extends EnTable {
    
    public enum c implements ColEnum {
        
        UUID_XL                         (InXlFile.class,            "Файл импорта"),
        
        ORD                             (Type.NUMERIC, 5,           "Номер строки"),
        
        ADDRESS                         (Type.STRING,         "Адрес"),
        UNOM                            (Type.NUMERIC, 12,    "UNOM"),
        FIASHOUSEGUID                   (VocBuilding.class,         "Код ФИАС"),
        HASBLOCKS                       (Type.BOOLEAN, Bool.FALSE, "Блокированная застройка"),
        HASMULTIPLEHOUSESWITHSAMEADRES  (Type.BOOLEAN, null,       "Несколько ЖД с одинаковым адресом"),
        OKTMO                           (Type.INTEGER, 11, null,    "ОКТМО"),
        
        CODE_VC_NSI_24                  (Type.STRING, 20,       "Состояние дома"),
        CODE_VC_NSI_336                 (Type.STRING, 20, null, "Стадия жизненного цикла"),
        
        TOTALSQUARE                     (Type.NUMERIC, 25, 4,        "Общая площадь"),
        USEDYEAR                        (Type.NUMERIC,  4,     "Год ввода в эксплуатацию"),
        FLOORCOUNT                      (Type.NUMERIC,  3,     "Количество этажей"),
        CULTURALHERITAGE                (Type.BOOLEAN, Bool.FALSE, "Наличие у дома статуса объекта культурного наследия"),
        KAD_N                           (Type.STRING, "нет", "Кадастровый номер"),
        
        ERR                             (Type.STRING,  null,  "Ошибка")
        
        ;
        
        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
        
        boolean isToCopy () {
            
            switch (this) {
                case ADDRESS:
                case UNOM:
                case FIASHOUSEGUID:
                case HASBLOCKS:
                case HASMULTIPLEHOUSESWITHSAMEADRES:
                case CODE_VC_NSI_24:
                case CODE_VC_NSI_336:
                case TOTALSQUARE:
                case USEDYEAR:
                case FLOORCOUNT:
                case CULTURALHERITAGE:
                case KAD_N:
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
    
    private static void setFields (Map<String, Object> r, XSSFRow row) throws InXlHouse.XLException {
        
        NsiTable nsi_24 = NsiTable.getNsiTable (24);
        NsiTable nsi_336 = NsiTable.getNsiTable (336);
        
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
            if (cell == null) throw new XLException ("Не указан UNOM (столбец B)");
            r.put (c.UNOM.lc (), cell.getStringCellValue ());
        }
        catch (Exception ex) {
            throw new XLException ("Некорректный тип ячейки UNOM (столбец B)");
        }
        
        try {
            final XSSFCell cell = row.getCell (2);
            if (cell == null) throw new XLException ("Не указан признак блокированной застройки (столбец C)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указан признак блокированной застройки (столбец C)");
            switch (s) {
                case "Да":
                    r.put (c.HASBLOCKS.lc (), Bool.TRUE);
                    break;
                case "Нет":
                    r.put (c.HASBLOCKS.lc (), Bool.FALSE);
                    break;
                default:
                    throw new XLException ("Указан неверный признак блокированной застройки (столбец C): " + s);
            }
        }
        catch (XLException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new XLException ("Некорректный тип ячейки признака блокированной застройки (столбец C)");
        }
        
        try {
            final XSSFCell cell = row.getCell (3);
            if (cell != null) {
                final String s = cell.getStringCellValue ();
                if (DB.ok (s))
                    switch (s) {
                        case "Да":
                            r.put (c.HASMULTIPLEHOUSESWITHSAMEADRES.lc (), Bool.TRUE);
                            break;
                        case "Нет":
                            r.put (c.HASMULTIPLEHOUSESWITHSAMEADRES.lc (), Bool.FALSE);
                            break;
                        default:
                            throw new XLException ("Указан неверный признак нескольких домов с одинаковым адресом (столбец D): " + s);
                    }
            }
        }
        catch (XLException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new XLException ("Некорректный тип ячейки признака нескольких домов с одинаковым адресом (столбец D)");
        }
        
        try {
            final XSSFCell cell = row.getCell (4);
            if (cell == null) throw new XLException ("Не указан код ОКТМО (столбец E)");
            r.put (c.OKTMO.lc (), cell.getStringCellValue ());
        }
        catch (Exception ex) {
            throw new XLException ("Некорректный тип ячейки кода ОКТМО (столбец E)");
        }
        
        try {
            final XSSFCell cell = row.getCell (5);
            if (cell == null) throw new XLException ("Не указано состояние дома (столбец F)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указано состояние дома (столбец F)");
            final String code = ModelHolder.getModel ().getDb ().getString (
                ModelHolder.getModel ()
                        .select (nsi_24, "code")
                        .where  (nsi_24.getLabelField ().getName (), s)
                        .and    ("is_actual", 1)
            );
            if (code == null) throw new XLException ("Код НСИ 24 не найден для указанного состояния дома (столбец F)");
            r.put (c.CODE_VC_NSI_24.lc (), code);
        }
        catch (Exception ex) {
            throw new XLException ("Некорректный тип ячейки состояния дома (столбец F)");
        }
        
        try {
            final XSSFCell cell = row.getCell (6);
            if (cell != null) {
                final String s = cell.getStringCellValue ();
                if (DB.ok (s)) {
                    final String code = ModelHolder.getModel ().getDb ().getString (
                        ModelHolder.getModel ()
                                .select (nsi_336, "code")
                                .where  (nsi_336.getLabelField ().getName (), s)
                                .and    ("is_actual", 1)
                    );
                    if (code == null) throw new XLException ("Код НСИ 338 не найден для указанной стадии жизненного цикла (столбец F)");
                    r.put (c.CODE_VC_NSI_336.lc (), code);
                }
            }
        }
        catch (Exception ex) {
            throw new XLException ("Некорректный тип ячейки стадии жизненного цикла (столбец G)");
        }
        
        try {
            final XSSFCell cell = row.getCell (7);
            if (cell == null) throw new XLException ("Не указана общая площадь здания (столбец H)");
            r.put (c.TOTALSQUARE.lc (), cell.getStringCellValue ());
        }
        catch (Exception ex) {
            throw new XLException ("Некорректный тип ячейки общей площади здания (столбец H)");
        }
        
        try {
            final XSSFCell cell = row.getCell (8);
            if (cell == null) throw new XLException ("Не указан год ввода в эксплуатацию (столбец I)");
            r.put (c.USEDYEAR.lc (), cell.getStringCellValue ());
        }
        catch (Exception ex) {
            throw new XLException ("Некорректный тип ячейки года ввода в эксплуатацию (столбец I)");
        }
        
        try {
            final XSSFCell cell = row.getCell (9);
            if (cell == null) throw new XLException ("Не указано количество этажей (столбец J)");
            r.put (c.FLOORCOUNT.lc (), cell.getStringCellValue ());
        }
        catch (Exception ex) {
            throw new XLException ("Некорректный тип ячейки количества этажей (столбец J)");
        }
        
        try {
            final XSSFCell cell = row.getCell (10);
            if (cell == null) throw new XLException ("Не указано наличие статуса объекта культурного наследия (столбец K)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указано наличие статуса объекта культурного наследия (столбец K)");
            switch (s) {
                case "Да":
                    r.put (c.CULTURALHERITAGE.lc (), Bool.TRUE);
                    break;
                case "Нет":
                    r.put (c.CULTURALHERITAGE.lc (), Bool.FALSE);
                    break;
                default:
                    throw new XLException ("Указан неверный признак наличия статуса объекта культурного наследия (столбец K): " + s);
            }
        }
        catch (XLException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new XLException ("Некорректный тип ячейки признака наличия статуса объекта культурного наследия (столбец K)");
        }
        
        try {
            final XSSFCell cell = row.getCell (11);
            if (cell == null) throw new XLException ("Не указан кадастровый номер (столбец L)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указан кадастровый номер (столбец L)");
            r.put (c.KAD_N.lc (), s);
        }
        catch (Exception ex) {
            throw new XLException ("Некорректный тип ячейки кадастрового номера (столбец L)");
        }
        
    }
    
    public InXlHouse () {
        
        super ("in_xl_houses", "Строки импорта жилых домов");
        
        cols (c.class);
        
        key ("uuid_xl", c.UUID_XL);
        
        trigger ("BEFORE INSERT", ""
                + "BEGIN "
                    + "IF :NEW.err IS NOT NULL THEN RETURN; END IF; "
                    
                    + "BEGIN "
                        + "SELECT fiashouseguid INTO :NEW.fiashouseguid FROM vc_unom WHERE unom=:NEW.unom; "
                        + "EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Не удалось однозначно определить GUID ФИАС по UNOM'); "
                    + "END; "

                    + "EXCEPTION WHEN OTHERS THEN "
                    + ":NEW.err := REPLACE(SUBSTR(SQLERRM, 1, 1000), 'ORA-20000: ', ''); "
                + "END; "
        );
        
        StringBuilder sb = new StringBuilder ();
        StringBuilder nsb = new StringBuilder ();
        
        for (c c: c.values ()) if (c.isToCopy ()) {
            sb.append (',');
            sb.append (c.lc ());
            nsb.append (",:NEW.");
            nsb.append (c.lc ());
        }
        
        trigger ("BEFORE UPDATE", ""
                + "DECLARE " 
                    + "PRAGMA AUTONOMOUS_TRANSACTION; "
                + "BEGIN "
                
                    + "IF :NEW.err IS NOT NULL THEN :NEW.is_deleted := 1; END IF; "
                    + "IF NOT (:OLD.is_deleted = 1 AND :NEW.is_deleted = 0) THEN RETURN; END IF; "
                
                    + "INSERT INTO tb_houses (uuid" + sb + ") VALUES (:NEW.uuid" + nsb + "); "
                    + "COMMIT; "

                + "END; "
        );
        
    }
    
}
