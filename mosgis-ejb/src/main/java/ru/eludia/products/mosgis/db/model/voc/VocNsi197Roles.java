package ru.eludia.products.mosgis.db.model.voc;

import java.util.Arrays;
import java.util.List;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import ru.eludia.products.mosgis.db.model.EnColEnum;

public class VocNsi197Roles extends Table {
    
    public static final List<String> NOT_FOR_UO_FIELDS = Arrays.asList(
        "11052", "11053", "13026", "14526", "20024", "20025", "20062", "20126", 
        "20127", "20128", "20129", "20131", "20132", "20133", "20134", "20136", 
        "20137", "20138", "20139", "20168", "20169", "20170", "20815", "20816", 
        "20817", "20818", "20820", "20821", "21819");
    
    public enum c implements EnColEnum {

        CODE       (Type.STRING,  20,  "Код значения справочника НСИ 197"),
        IS_FOR_UO  (Type.BOOLEAN, Bool.TRUE, "Разрешено для УО"),
        IS_FOR_OMS (Type.BOOLEAN, Bool.TRUE, "Разрешено для ОМС"),
        IS_FOR_ESP (Type.BOOLEAN, Bool.TRUE, "Разрешено для ЕСП");
        
        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {
            return false;
        }

    }
    
    public VocNsi197Roles () {
        
        super ("vc_nsi_197_roles", "Роли, для которых разрешено изменение параметров ОЖФ");
        
        cols (c.class);
        
        pk (c.CODE);
        
    }    
}
