package ru.eludia.products.mosgis.db.model.incoming.xl.lines;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.apache.poi.xssf.usermodel.XSSFRow;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.jms.xl.base.XLException;

public class InXlOrgPackItem extends EnTable {

    public static final String TABLE_NAME = "in_xl_org_pack_item";

    public enum c implements ColEnum {

        UUID_XL                 (InXlFile.class,            "Файл импорта"),
        ORD                     (Type.NUMERIC, 5,           "Номер строки"),
        ERR                     (Type.STRING,  null,        "Ошибка"),

	ORGN                    (Type.STRING,  15,          "ОГРН(ИП)"),
	KPP                     (Type.STRING,   9, null,    "КПП")
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

    }

    private static final Pattern RE_OGRN = Pattern.compile ("\\d{13}(\\d\\d)?");
    private static final Pattern RE_KPP = Pattern.compile ("\\d{9}");
    
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

	String ogrn = toString (row, 0, RE_OGRN, "Некорректный формат ОГРН(ИП)");
        
	String kpp = toString (row, 1);
        
        if (DB.ok (kpp)) {            
            if (ogrn.length () == 15) throw new XLException ("Индивидуальные предпрениматели не имеют КПП.");
            if (!RE_KPP.matcher (kpp).matches ()) throw new XLException ("Некорректный формат КПП.");
            r.put (c.KPP.lc (), kpp);
        }        

        r.put (c.ORGN.lc (), ogrn);
        
    }

    public InXlOrgPackItem () {

        super (TABLE_NAME, "Строки импорта пакетов ОГРН");

        cols  (c.class);

	key ("uuid_xl", c.UUID_XL);        
        
        trigger ("BEFORE INSERT OR UPDATE", ""

            + "BEGIN "
            + " IF :NEW.err IS NOT NULL THEN :NEW.is_deleted := 1; END IF; "
            + "END; "

        );

    }

}