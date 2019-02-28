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
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocHouseStatus;
import ru.eludia.products.mosgis.ejb.ModelHolder;

public class InXlHouse extends EnTable {
    
    private static final Logger logger = Logger.getLogger(InXlHouse.class.getName());
    
    public enum c implements ColEnum {
        
        UUID_XL                         (InXlFile.class,            "Файл импорта"),
        
        ORD                             (Type.NUMERIC, 5,           "Номер строки"),
        
        ADDRESS                         (Type.STRING, null,       "Адрес"),
        UNOM                            (Type.NUMERIC, 12, null,  "UNOM"),
        FIASHOUSEGUID                   (VocBuilding.class,         "Код ФИАС"),
        HASBLOCKS                       (Type.BOOLEAN, null, "Блокированная застройка"),
        HASMULTIPLEHOUSESWITHSAMEADRES  (Type.BOOLEAN, null,       "Несколько ЖД с одинаковым адресом"),
        OKTMO                           (Type.INTEGER, 11, null,    "ОКТМО"),
        
        CODE_VC_NSI_24                  (Type.STRING, 20, null, "Состояние дома"),
        CODE_VC_NSI_338                 (Type.STRING, 20, null, "Стадия жизненного цикла"),
        
        TOTALSQUARE                     (Type.NUMERIC, 25, 4, null, "Общая площадь"),
        USEDYEAR                        (Type.NUMERIC,  4, null,    "Год ввода в эксплуатацию"),
        FLOORCOUNT                      (Type.NUMERIC,  3, null,    "Количество этажей"),
        CULTURALHERITAGE                (Type.BOOLEAN, null, "Наличие у дома статуса объекта культурного наследия"),
        KAD_N                           (Type.STRING, null, "Кадастровый номер"),
        
        RESIDENTSCOUNT                  (Type.INTEGER, null, "Количество проживающих"),
        HASUNDERGROUNDPARKING           (Type.BOOLEAN, null, "Наличие подземного паркинга"),
        
        ERR                             (Type.STRING,  null,  "Ошибка")
        
        ;
        
        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
        
        boolean isToCopy () {
            
            switch (this) {
                case UUID_XL:
                case ADDRESS:
                case UNOM:
                case FIASHOUSEGUID:
                case HASBLOCKS:
                case HASMULTIPLEHOUSESWITHSAMEADRES:
                case CODE_VC_NSI_24:
                case CODE_VC_NSI_338:
                case TOTALSQUARE:
                case USEDYEAR:
                case FLOORCOUNT:
                case CULTURALHERITAGE:
                case KAD_N:
                case RESIDENTSCOUNT:
                case HASUNDERGROUNDPARKING:
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
        
        NsiTable nsi_24 = NsiTable.getNsiTable (24);
        NsiTable nsi_338 = NsiTable.getNsiTable (338);
        
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
            if (cell == null) throw new XLException ("Не указан UNOM (столбец B)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указан UNOM (столбец B)");
            r.put (c.UNOM.lc (), s);
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
                    r.put (c.HASBLOCKS.lc (), 1);
                    break;
                case "Нет":
                    r.put (c.HASBLOCKS.lc (), 0);
                    break;
                default:
                    throw new XLException ("Указан неверный признак блокированной застройки (столбец C): " + s);
            }
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
        try {
            if ((int) r.get (c.HASBLOCKS.lc ()) == 1) {
                final XSSFCell cell = row.getCell (3);
                if (cell != null) {
                    final String s = cell.getStringCellValue ();
                    if (DB.ok (s))
                        switch (s) {
                            case "Да":
                                r.put (c.HASMULTIPLEHOUSESWITHSAMEADRES.lc (), 1);
                                break;
                            case "Нет":
                                r.put (c.HASMULTIPLEHOUSESWITHSAMEADRES.lc (), 0);
                                break;
                            default:
                                throw new XLException ("Указан неверный признак нескольких домов с одинаковым адресом (столбец D): " + s);
                        }
                }
            }
            else r.put (c.HASMULTIPLEHOUSESWITHSAMEADRES.lc (), 0);
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
            r.put (c.CODE_VC_NSI_24.lc (), code);
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
                    r.put (c.CODE_VC_NSI_338.lc (), code);
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
            r.put (c.TOTALSQUARE.lc (), cell.getStringCellValue ());
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
        try {
            final XSSFCell cell = row.getCell (8);
            if (cell == null) throw new XLException ("Не указан год ввода в эксплуатацию (столбец I)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указан год ввода в эксплуатацию (столбец I)");
            r.put (c.USEDYEAR.lc (), cell.getStringCellValue ());
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
        }
        
        try {
            final XSSFCell cell = row.getCell (9);
            if (cell == null) throw new XLException ("Не указано количество этажей (столбец J)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указано количество этажей (столбец J)");
            r.put (c.FLOORCOUNT.lc (), cell.getStringCellValue ());
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
                    r.put (c.CULTURALHERITAGE.lc (), 1);
                    break;
                case "Нет":
                    r.put (c.CULTURALHERITAGE.lc (), 0);
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
                r.put (c.KAD_N.lc (), cell.getStringCellValue ());
            }
            else
                r.put (c.KAD_N.lc (), "нет");
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage ());
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
        StringBuilder usb = new StringBuilder ();
        
        for (c c: c.values ()) if (c.isToCopy ()) {
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
