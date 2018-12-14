package ru.eludia.products.mosgis.db.model;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Def;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocUser;

public abstract class LogTable extends Table {

    public LogTable (String name, String remark, Class object, Class... colEnums) {
        
        super (name, remark);
        
        pk    ("uuid",                      Type.UUID,             NEW_UUID,    "Ключ");
        ref   ("action",                    VocAction.class,                    "Действие");
        fk    ("uuid_object",               object,                             "Ссылка на запись");
        col   ("ts",                        Type.TIMESTAMP,        NOW,         "Дата/время события");
        fk    ("uuid_user",                 VocUser.class,         null,        "Оператор");
        
        StringBuilder sbSelect = new StringBuilder ();
        StringBuilder sbInto   = new StringBuilder ();
        
        for (Class e: colEnums) {
            
            for (Object o: e.getEnumConstants ()) {
                
                EnColEnum c = (EnColEnum) o;
                
                if (!c.isLoggable ()) continue;                                
                
                Col col = c.getCol ().clone ();
                
                Def def = col.getDef ();
                boolean isVirtual = def != null && def instanceof Virt;
                
                if (!isVirtual) {
                    col.setDef (null);
                    col.setNullable (true);
                }
                
                add (col);
                
                if (!isVirtual) {
                    
                    if (sbSelect.length () > 0) {
                        sbSelect.append (',');
                        sbInto.append (',');
                    }

                    sbSelect.append (col.getName ());
                    sbInto.append (":NEW.");
                    sbInto.append (col.getName ());
                    
                }
                
            }
            
        }
        
        trigger ("BEFORE INSERT", "BEGIN SELECT " + sbSelect + " INTO " + sbInto + " FROM " + name.substring (0, name.length () - 5) + " WHERE uuid=:NEW.uuid_object; END;");
        
    }

}