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
import ru.eludia.products.mosgis.db.model.tables.AdditionalService;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.ContractFile;
import ru.eludia.products.mosgis.db.model.tables.ContractObject;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class InXlContractObjectService extends EnTable {

    public enum c implements ColEnum {

        UUID_XL                 (InXlFile.class,                "Файл импорта"),
        ORD                     (Type.NUMERIC, 5,               "Номер строки"),

        ADDRESS                 (Type.STRING, null,             "Адрес"),

        UUID_CONTRACT_OBJECT    (ContractObject.class,  null,   "Ссылка на объект договора"),
        UUID_CONTRACT_AGREEMENT (ContractFile.class,            "Ссылка на дополнительное соглашение"),        
        UUID_CONTRACT           (Contract.class,        null,   "Ссылка на договор"),
        UUID_ORG                (VocOrganization.class, null,   "Организация-исполнитель договора"),

        STARTDATE               (Type.DATE, null,               "Дата начала предоставления услуг"),
        ENDDATE                 (Type.DATE, null,               "Дата окончания предоставления услуг"),

        KIND                    (Type.STRING, 2, null,          "Вид услуги"),
        UNIQUENUMBER            (Type.STRING, null,             "Код услуги"),

        CODE_VC_NSI_3           (Type.STRING, 20, null,         "Коммунальная услуга"),
        UUID_ADD_SERVICE        (AdditionalService.class, null, "Дополнительная услуга"),        

        ERR                     (Type.STRING,  null,            "Ошибка"),

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
                case UUID_CONTRACT_OBJECT:
                case UUID_CONTRACT_AGREEMENT:
                    
                case CODE_VC_NSI_3:
                case UUID_ADD_SERVICE:

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
            final XSSFCell cell = row.getCell (0);
            if (cell == null) throw new XLException ("Не указан адрес (столбец A)");
            r.put (c.ADDRESS.lc (), cell.getStringCellValue ());
        }
        catch (Exception ex) {
            throw new XLException ("Некорректный тип ячейки адреса (столбец A)");
        }
        
        
        try {
            final XSSFCell cell = row.getCell (2);
            if (cell == null) throw new XLException ("Не указан код услуги (столбец C)");
            r.put (c.UNIQUENUMBER.lc (), cell.getStringCellValue ());
        }
        catch (Exception ex) {
            throw new XLException ("Некорректный код услуги (столбец C)");
        }
        
        try {
            final XSSFCell cell = row.getCell (1);
            if (cell == null) throw new XLException ("Не указан вид услуги (столбец B)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указан вид услуги (столбец B)");
            r.put (c.KIND.lc (), s);
            switch (s) {
                case "КУ":
                    r.put (c.CODE_VC_NSI_3.lc (), r.get (c.UNIQUENUMBER.lc ()));
                case "ДУ":
                    break;
                default:
                    throw new XLException ("Неизвестный вид услуги (столбец B): " + s);
            }
        }
        catch (XLException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new XLException ("Некорректный тип ячейки номера договора (столбец B)");
        }
        
        
        try {
            final XSSFCell cell = row.getCell (3);
            if (cell == null) throw new XLException ("Не указана дата начала (столбец D)");
            final Date d = cell.getDateCellValue ();
            if (d == null) throw new XLException ("Не указана дата начала (столбец D)");
            r.put (c.STARTDATE.lc (), d);
        }
        catch (XLException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new XLException ("Некорректное значение даты начала (столбец D)");
        }   
        
        try {
            final XSSFCell cell = row.getCell (4);
            if (cell == null) throw new XLException ("Не указана дата окончания (столбец E)");
            final Date d = cell.getDateCellValue ();
            if (d == null) throw new XLException ("Не указана дата окончания (столбец E)");
            r.put (c.ENDDATE.lc (), d);
        }
        catch (XLException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new XLException ("Некорректное значение даты окончания (столбец E)");
        }        

        if (((Date) r.get (c.ENDDATE.lc ())).before ((Date) r.get (c.STARTDATE.lc ()))) throw new XLException ("Дата окончания (столбец E) не может предшествовать дате начала (столбец D)");
        
    }

    public InXlContractObjectService () {

        super ("in_xl_ctr_services", "Строки импорта объектов управления");

        cols  (c.class);

        key ("uuid_xl", c.UUID_XL);
        
        trigger ("BEFORE INSERT", ""
                
            + "DECLARE "
            + " in_obj in_xl_ctr_objects%ROWTYPE; "
            + "BEGIN "
                
            + " IF :NEW.err IS NOT NULL THEN RETURN; END IF; "                
/*                
                
            + " BEGIN "
            + "  SELECT * INTO in_obj FROM in_xl_ctr_objects WHERE uuid_xl=:NEW.uuid_xl AND address=:NEW.address AND startdate<=:NEW.startdate AND enddate>=:NEW.enddate; "
            + "  EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Не удалось определить объект управления по адресу и датам');"
            + " END; END IF; "                                
                
            + " :NEW.uuid_org                := in_obj.uuid_org; "
            + " :NEW.uuid_contract           := in_obj.uuid_contract; "
            + " :NEW.uuid_contract_object    := in_obj.uuid; "
            + " :NEW.uuid_contract_agreement := in_obj.uuid_contract_agreement; "

            + " IF :NEW.code_vc_nsi_3 IS NOT NULL THEN BEGIN "
            + "  SELECT code INTO :NEW.code_vc_nsi_3 FROM vc_nsi_3 WHERE is_deleted=0 AND code=:NEW.code_vc_nsi_3; "
            + "  EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Не найдена услуга с кодом ' || :NEW.code_vc_nsi_3);"
            + " END; END IF; "
                
            + " IF :NEW.code_vc_nsi_3 IS NULL THEN BEGIN "
            + "  SELECT uuid INTO :NEW.uuid_add_service FROM tb_add_services WHERE is_deleted=0 AND uuid_org=:NEW.uuid_org AND uniquenumber=:NEW.uniquenumber; "
            + "  EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Не найдена дополнительная услуга с кодом ' || :NEW.uniquenumber);"
            + " END; END IF; "
*/
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

            + "BEGIN "

            + " IF :NEW.err IS NOT NULL THEN RETURN; END IF; "
            + " IF NOT (:OLD.is_deleted = 1 AND :NEW.is_deleted = 0) THEN RETURN; END IF; "

            + " INSERT INTO tb_contract_services (uuid,is_deleted" + sb + ") VALUES (:NEW.uuid,0" + nsb + "); "
            + " UPDATE tb_contract_services SET is_deleted=1 WHERE uuid=:NEW.uuid; "
            + " COMMIT; "

            + "END; "

        );        

    }

}