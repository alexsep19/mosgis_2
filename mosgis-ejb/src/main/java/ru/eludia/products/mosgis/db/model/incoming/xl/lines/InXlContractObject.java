package ru.eludia.products.mosgis.db.model.incoming.xl.lines;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.ContractFile;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class InXlContractObject extends EnTable {

    public enum c implements ColEnum {

        UUID_XL                 (InXlFile.class,            "Файл импорта"),
        ORD                     (Type.NUMERIC, 5,           "Номер строки"),
        
        UUID_ORG                (VocOrganization.class,     "Организация-исполнитель договора"),

        ADDRESS                 (Type.STRING, null,         "Адрес"),
        DOCNUM                  (Type.STRING, null,         "Номер договора"),
        SIGNINGDATE             (Type.DATE, null,           "Дата заключения"),

        UUID_CONTRACT           (Contract.class,            "Ссылка на договор"),
        UUID_CONTRACT_AGREEMENT (ContractFile.class,        "Ссылка на дополнительное соглашение"),        
        
        UNOM                    (Type.NUMERIC, 15, null,    "UNOM"),
        FIASHOUSEGUID           (VocBuilding.class,         "Код ФИАС"),
        
        STARTDATE               (Type.DATE, null,           "Дата начала предоставления услуг"),
        ENDDATE                 (Type.DATE, null,           "Дата окончания предоставления услуг"),
        
        AGREEMENTNUMBER         (Type.STRING, null,         "Номер дополнительного соглашения"),
        AGREEMENTDATE           (Type.DATE, null,           "Дата дополнительного соглашения"),
        
        ERR                     (Type.STRING,  null,        "Ошибка"),

        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        boolean isToCopy () {

            switch (this) {

                case UUID_XL:

                case UUID_CONTRACT:
                case UUID_CONTRACT_AGREEMENT:

                case FIASHOUSEGUID:

                case ENDDATE:
                case STARTDATE:
                    
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
            r.put (c.ADDRESS.lc (), row.getCell (0).getStringCellValue ());
        }
        catch (Exception ex) {
            throw new XLException ("Некорректный тип ячейки адреса (столбец A)");
        }
        
        try {
            final XSSFCell cell = row.getCell (1);
            if (cell == null) throw new XLException ("Не указан номер договора (столбец B)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указан номер договора (столбец B)");
            r.put (c.DOCNUM.lc (), s);
        }
        catch (XLException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new XLException ("Некорректный тип ячейки номера договора (столбец B)");
        }
        
        try {
            final XSSFCell cell = row.getCell (2);
            if (cell == null) throw new XLException ("Не указана дата заключения договора (столбец C)");
            final Date d = cell.getDateCellValue ();
            if (d == null) throw new XLException ("Не указана дата заключения договора (столбец C)");
            r.put (c.SIGNINGDATE.lc (), d);
        }
        catch (XLException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new XLException ("Некорректное значение даты заключения договора (столбец C)");
        }
        
        try {
            final XSSFCell cell = row.getCell (3);
            if (cell == null) throw new XLException ("Не указан UNOM (столбец D)");
            r.put (c.UNOM.lc (), Long.parseLong (cell.getStringCellValue ()));
        }
        catch (XLException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new XLException ("Некорректное значение UNOM (столбец D)");
        }
        
        try {
            final XSSFCell cell = row.getCell (4);
            if (cell == null) throw new XLException ("Не указана дата начала (столбец E)");
            final Date d = cell.getDateCellValue ();
            if (d == null) throw new XLException ("Не указана дата начала (столбец E)");
            r.put (c.STARTDATE.lc (), d);
        }
        catch (XLException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new XLException ("Некорректное значение даты начала (столбец E)");
        }   

        if (((Date) r.get (c.STARTDATE.lc ())).before ((Date) r.get (c.SIGNINGDATE.lc ()))) throw new XLException ("Дата начала (столбец E) не может предшествовать дате заключения договора (столбец C)");
        
        try {
            final XSSFCell cell = row.getCell (5);
            if (cell == null) throw new XLException ("Не указана дата окончания (столбец F)");
            final Date d = cell.getDateCellValue ();
            if (d == null) throw new XLException ("Не указана дата окончания (столбец F)");
            r.put (c.ENDDATE.lc (), d);
        }
        catch (XLException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new XLException ("Некорректное значение даты окончания (столбец F)");
        }        

        if (((Date) r.get (c.ENDDATE.lc ())).before ((Date) r.get (c.STARTDATE.lc ()))) throw new XLException ("Дата окончания (столбец F) не может предшествовать дате начала (столбец E)");

        if (row.getCell (6) != null && row.getCell (6).getStringCellValue () != null) return;
        
        try {
            final XSSFCell cell = row.getCell (7);
            if (cell == null) throw new XLException ("Не указан номер дополнительного соглашения (столбец H)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указан номер дополнительного соглашения (столбец H)");
            r.put (c.AGREEMENTNUMBER.lc (), s);
        }
        catch (XLException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new XLException ("Некорректный тип номера дополнительного соглашения (столбец H)");
        }
        
        try {
            final XSSFCell cell = row.getCell (8);
            if (cell == null) throw new XLException ("Не указана дата дополнительного соглашения (столбец I)");
            final Date d = cell.getDateCellValue ();
            if (d == null) throw new XLException ("Не указана дата дополнительного соглашения (столбец I)");
            r.put (c.SIGNINGDATE.lc (), d);
        }
        catch (XLException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new XLException ("Некорректное значение даты дополнительного соглашения (столбец I)");
        }        

    }

    public InXlContractObject () {

        super ("in_xl_ctr_objects", "Строки импорта объектов управления");

        cols  (c.class);

        key ("uuid_xl", c.UUID_XL);
        
        trigger ("BEFORE INSERT", ""
                
            + "DECLARE "
            + " ctr tb_contracts%ROWTYPE; "
  
            + "BEGIN "
                
            + " SELECT uuid_org INTO :NEW.uuid_org FROM in_xl_files WHERE uuid=:NEW.uuid_xl; "

            + " IF :NEW.err IS NOT NULL THEN RETURN; END IF; "
                
            + "BEGIN "
            + " SELECT * INTO ctr FROM tb_contracts WHERE is_deleted=0 AND uuid_org=:NEW.uuid_org AND docnum=:NEW.docnum AND signingdate=:NEW.signingdate; "
            + " EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Не удалось определить договор по номеру и дате подписания');"
            + "END;"
                               
            + " IF ctr.id_ctr_status > 11 THEN FOR i IN (SELECT label FROM vc_gis_status WHERE id=ctr.id_ctr_status) LOOP raise_application_error (-20000, 'Договор находится в статусе \"' || i.label || '\"; добавление объектов запрещено'); END LOOP; END IF; "
            + " IF :NEW.startdate < ctr.effectivedate THEN raise_application_error (-20000, 'Дата начала выходит за период действия договора'); END IF; "
            + " IF :NEW.enddate > ctr.plandatecomptetion THEN raise_application_error (-20000, 'Дата окончания выходит за период действия договора'); END IF; "
            + " :NEW.uuid_contract := ctr.uuid; "

            + " IF :NEW.agreementdate IS NOT NULL THEN BEGIN "
            + "  SELECT uuid INTO :NEW.uuid_contract_agreement FROM tb_contract_files WHERE id_status=1 AND uuid_contract=:NEW.uuid_contract AND agreementnumber=:NEW.agreementnumber AND agreementdate=:NEW.agreementdate; "
            + "  EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Не удалось определить дополнительное соглашение по номеру и дате');"
            + " END; END IF; "                                

            + "BEGIN "
            + " SELECT fiashouseguid INTO :NEW.fiashouseguid FROM vc_unom WHERE unom=:NEW.unom; "
            + " EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Не удалось однозначно определить GUID ФИАС по UNOM');"
            + "END;"

            + " EXCEPTION WHEN OTHERS THEN "
            + " :NEW.err := REPLACE(SUBSTR(SQLERRM, 1, 1000), 'ORA-20000: ', ''); "

            + "END;"

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

            + "DECLARE" 
            + " PRAGMA AUTONOMOUS_TRANSACTION; "
            + " l_err VARCHAR2 (1000); "
            + "BEGIN "

            + " IF :NEW.err IS NOT NULL THEN RETURN; END IF; "
            + " IF NOT (:OLD.is_deleted = 1 AND :NEW.is_deleted = 0) THEN RETURN; END IF; "

            + " INSERT INTO tb_contract_objects (uuid,is_deleted" + sb + ") VALUES (:NEW.uuid,0" + nsb + "); "
            + " UPDATE tb_contract_objects SET is_deleted=-1 WHERE uuid=:NEW.uuid; "
            + " COMMIT; "

            + "END; "

        );        

    }

}