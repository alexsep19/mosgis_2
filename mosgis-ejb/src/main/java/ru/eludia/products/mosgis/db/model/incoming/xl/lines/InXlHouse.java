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
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocHouseStatus;
import ru.eludia.products.mosgis.ejb.ModelHolder;

public class InXlHouse extends EnTable {
    
    private static final Logger logger = Logger.getLogger(InXlHouse.class.getName());
    
    public enum c implements ColEnum {
        
        ORD                             (Type.NUMERIC, 5,           "Номер строки"),
        
        OKTMO                           (Type.INTEGER, 11, null,    "ОКТМО"),
        
        ERR                             (Type.STRING,  null,  "Ошибка")
        
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
            House.c.UUID_XL, uuid,
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
        
        NsiTable nsi_24 = NsiTable.getNsiTable (24);
        NsiTable nsi_338 = NsiTable.getNsiTable (338);
        
        try {
            final XSSFCell cell = row.getCell (0);
            if (cell == null) throw new XLException ("Не указан адрес (столбец A)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указан адрес (столбец A)");
            r.put (House.c.ADDRESS.lc (), s);
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
        try {
            final XSSFCell cell = row.getCell (1);
            if (cell == null) throw new XLException ("Не указан UNOM (столбец B)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указан UNOM (столбец B)");
            r.put (House.c.UNOM.lc (), s);
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
        try {
            final XSSFCell cell = row.getCell (2);
            if (cell == null) throw new XLException ("Не указан признак блокированной застройки (столбец C)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указан признак блокированной застройки (столбец C)");
            switch (s) {
                case "Да":
                    r.put (House.c.HASBLOCKS.lc (), 1);
                    break;
                case "Нет":
                    r.put (House.c.HASBLOCKS.lc (), 0);
                    break;
                default:
                    throw new XLException ("Указан неверный признак блокированной застройки (столбец C): " + s);
            }
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
        try {
            if ((int) r.get (House.c.HASBLOCKS.lc ()) == 1) {
                final XSSFCell cell = row.getCell (3);
                if (cell == null) throw new XLException ("Не указан признак нескольких домов с одинаковым адресом (столбец D)");
                final String s = cell.getStringCellValue ();
                if (!DB.ok (s)) throw new XLException ("Не указан признак нескольких домов с одинаковым адресом (столбец D)");
                    switch (s) {
                        case "Да":
                            r.put (House.c.HASMULTIPLEHOUSESWITHSAMEADRES.lc (), 1);
                            break;
                        case "Нет":
                            r.put (House.c.HASMULTIPLEHOUSESWITHSAMEADRES.lc (), 0);
                            break;
                        default:
                            throw new XLException ("Указан неверный признак нескольких домов с одинаковым адресом (столбец D): " + s);
                    }
            }
            else r.put (House.c.HASMULTIPLEHOUSESWITHSAMEADRES.lc (), 0);
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
        try {
            final XSSFCell cell = row.getCell (4);
            if (cell != null) {
                final String s = cell.getStringCellValue ();
                if (DB.ok (s)) r.put (c.OKTMO.lc (), cell.getStringCellValue ());
            }
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
        try {
            final XSSFCell cell = row.getCell (5);
            if (cell == null) throw new XLException ("Не указано состояние дома (столбец F)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указано состояние дома (столбец F)");
            final String code = ModelHolder.getModel ().getDb ().getString (
                ModelHolder.getModel ()
                        .select (nsi_24, "code")
                        .where  ("f_" + nsi_24.getLabelField ().getName (), s)
                        .and    ("is_actual", 1)
            );
            if (code == null) throw new XLException ("Код НСИ 24 не найден для указанного состояния дома (столбец F)");
            r.put (House.c.CODE_VC_NSI_24.lc (), code);
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
        try {
            final XSSFCell cell = row.getCell (6);
            if (cell != null) {
                final String s = cell.getStringCellValue ();
                if (DB.ok (s)) {
                    final String code = ModelHolder.getModel ().getDb ().getString (
                        ModelHolder.getModel ()
                                .select (nsi_338, "code")
                                .where  ("f_" + nsi_338.getLabelField ().getName (), s)
                                .and    ("is_actual", 1)
                    );
                    if (code == null) throw new XLException ("Код НСИ 336 не найден для указанной стадии жизненного цикла (столбец G)");
                    r.put (House.c.CODE_VC_NSI_338.lc (), code);
                }
            }
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
        try {
            final XSSFCell cell = row.getCell (7);
            if (cell == null) throw new XLException ("Не указана общая площадь здания (столбец H)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указана общая площадь здания (столбец H)");
            r.put (House.c.TOTALSQUARE.lc (), cell.getStringCellValue ());
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
        try {
            final XSSFCell cell = row.getCell (8);
            if (cell == null) throw new XLException ("Не указан год ввода в эксплуатацию (столбец I)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указан год ввода в эксплуатацию (столбец I)");
            r.put (House.c.USEDYEAR.lc (), cell.getStringCellValue ());
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
        try {
            final XSSFCell cell = row.getCell (9);
            if (cell == null) throw new XLException ("Не указано количество этажей (столбец J)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указано количество этажей (столбец J)");
            r.put (House.c.FLOORCOUNT.lc (), cell.getStringCellValue ());
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
        try {
            final XSSFCell cell = row.getCell (10);
            if (cell == null) throw new XLException ("Не указано наличие статуса объекта культурного наследия (столбец K)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указано наличие статуса объекта культурного наследия (столбец K)");
            switch (s) {
                case "Да":
                    r.put (House.c.CULTURALHERITAGE.lc (), 1);
                    break;
                case "Нет":
                    r.put (House.c.CULTURALHERITAGE.lc (), 0);
                    break;
                default:
                    throw new XLException ("Указан неверный признак наличия статуса объекта культурного наследия (столбец K): " + s);
            }
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
        try {
            final XSSFCell cell = row.getCell (11);
            if (cell != null && DB.ok (cell.getStringCellValue ())) {
                r.put (House.c.KAD_N.lc (), cell.getStringCellValue ());
            }
            else
                r.put (House.c.KAD_N.lc (), "нет");
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
    }
    
    public InXlHouse () {
        
        super ("in_xl_houses", "Строки импорта жилых домов");
        
        cols (c.class);
        
        for (ColEnum o: House.c.values ()) {
            
            House.c c = (House.c) o;
                
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
        
        key ("uuid_xl", House.c.UUID_XL);
        
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
        StringBuilder usb = new StringBuilder ();
        
        for (House.c c: House.c.values ()) if (c.isToXlImport ()) {
            sb.append (',');
            sb.append (c.lc ());
            nsb.append (",:NEW.");
            nsb.append (c.lc ());
            usb.append ("," + c.lc () + " = :NEW." + c.lc ());
        }
        
        trigger ("BEFORE UPDATE", ""
                + "DECLARE "
                    + "house_id RAW (16); "
                    + "house_status INTEGER; "
                    + "house_hasblocks NUMBER (1); "
                    + "usr RAW (16); "
                    + "org RAW (16); "
                    + "log RAW (16); "
                    + "cnt NUMBER; "
                    + "PRAGMA AUTONOMOUS_TRANSACTION; "
                + "BEGIN "
                
                    + "IF :NEW.err IS NOT NULL THEN :NEW.is_deleted := 1; END IF; "
                    + "IF NOT (:OLD.is_deleted = 1 AND :NEW.is_deleted = 0) THEN RETURN; END IF; "
                
                    + "SELECT COUNT (*) INTO cnt FROM tb_houses r WHERE r.unom = :NEW.unom; "
                    + "IF cnt = 0 THEN "
                        + "INSERT INTO tb_houses (uuid, is_condo" + sb + ") "
                                       + "VALUES (:NEW.uuid, 0" + nsb + ") "
                                       + "RETURNING uuid INTO house_id; "
                    + "ELSE "
                        + "FOR i IN (SELECT uuid FROM tb_houses WHERE unom = :NEW.unom AND is_condo = 1) LOOP "
                            + "raise_application_error (-20000, 'Существует МКД с таким же UNOM'); "
                        + "END LOOP; "
                        + "UPDATE tb_houses SET " + usb.substring(1) + " WHERE unom = :NEW.unom RETURNING uuid, hasblocks, id_status INTO house_id, house_hasblocks, house_status; "
                        + "IF house_status = " + VocHouseStatus.i.PUBLISHED.getId () + " AND house_hasblocks <> :NEW.hasblocks THEN "
                            + "ROLLBACK; "
                            + "raise_application_error (-20000, 'ЖД с данным UNOM и иным признаком блокированной застройки уже опубликован'); "
                        + "END IF; "
                    + "END IF; "
                    
                    + "IF :NEW.oktmo IS NOT NULL THEN "
                        + "UPDATE vc_buildings SET oktmo = :NEW.oktmo WHERE houseguid = :NEW.fiashouseguid; "
                    + "END IF; "
                    
                    + "SELECT f.uuid_user, f.uuid_org INTO usr, org FROM in_xl_files f WHERE f.uuid = :NEW.uuid_xl; "
                    + "INSERT INTO tb_houses__log (action, uuid_object, uuid_user, uuid_org) "
                    + "VALUES ('" + VocAction.i.IMPORT_FROM_FILE.getName () + "', house_id, usr, org) "
                    + "RETURNING uuid INTO log; "
                            
                    + "UPDATE tb_houses SET id_log = log WHERE uuid = house_id; "
                                
                    + "COMMIT; "

                + "END; "
        );
        
    }
    
}
