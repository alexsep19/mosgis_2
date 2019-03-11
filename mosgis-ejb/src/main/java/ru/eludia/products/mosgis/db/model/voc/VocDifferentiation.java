package ru.eludia.products.mosgis.db.model.voc;

import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Collectors;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.gosuslugi.dom.schema.integration.tariff.ExportDifferentiationType;

public class VocDifferentiation extends Table {
    
    public static final String TABLE_NAME = "vc_diff";
    
    public VocDifferentiation () {
        super (TABLE_NAME, "Критерии дифференциации");
        cols  (c.class);        
        pk    (c.DIFFERENTIATIONCODE);        
    }
    
    public enum c implements ColEnum {
        
        DIFFERENTIATIONCODE      (Type.INTEGER,                          "Код критерия дифференциации"),
        DIFFERENTIATIONNAME      (Type.STRING,                           "Наименование критерия дифференциации"),
        DIFFERENTIATIONVALUEKIND (VocDifferentiationValueKindType.class, "Тип значения критерия дифференциации"),
        NSIITEM                  (VocNsiList.class,                      "Информация о справочнике для критерия перечислимого типа"),
        ISPLURAL                 (Type.BOOLEAN,  Boolean.FALSE,          "1, если справочником предусмотрено несколько значений; иначе 0"),
        ;
                                                                                    @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c(Class c, Object... p) {col = new Ref(this, c, p);}    
        static c forName (String s) {
            String u = s.toUpperCase ();
            for (c i: values ()) if (u.equals (i.name ())) return i;
            return null;
        }
    
    }
    
    public static void store (DB db, ExportDifferentiationType diff) throws SQLException {
        
        final Map<String, Object> r = DB.HASH (
            c.DIFFERENTIATIONCODE,      diff.getDifferentiationCode (),
            c.DIFFERENTIATIONNAME,      diff.getDifferentiationName (),
            c.DIFFERENTIATIONVALUEKIND, diff.getDifferentiationValueKind ().value (),
            c.ISPLURAL,                 DB.ok (diff.isIsPlural ())
        );

        ExportDifferentiationType.NsiItem nsiItem = diff.getNsiItem ();
        if (nsiItem != null) r.put (c.NSIITEM.lc (), diff.getNsiItem ().getRegistryNumber ());
        
        db.upsert (VocDifferentiation.class, r);
        
        db.dupsert (VocDifferentiationNsi268.class, 
            DB.HASH (c.DIFFERENTIATIONCODE, diff.getDifferentiationCode ()), 
            diff.getTariffKind ().stream ().map ((t) -> DB.HASH ("code", t.getCode ())).collect (Collectors.toList ()), 
            "code"
        );
        
        db.dupsert (VocDifferentiationUsedFor.class, 
            DB.HASH (c.DIFFERENTIATIONCODE, diff.getDifferentiationCode ()), 
            diff.getUsedFor ().stream ().map ((t) -> DB.HASH (VocTariffCaseType.c.ID, t.value ())).collect (Collectors.toList ()), 
            VocTariffCaseType.c.ID.lc ()
        );
                                
    }    

}