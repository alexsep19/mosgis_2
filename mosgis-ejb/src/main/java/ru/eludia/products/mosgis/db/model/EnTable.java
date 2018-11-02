package ru.eludia.products.mosgis.db.model;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.BOOLEAN;
import static ru.eludia.base.model.def.Bool.FALSE;
import static ru.eludia.base.model.def.Def.NEW_UUID;

public abstract class EnTable extends Table {

    public enum c implements ColEnum {

        UUID                      (Type.UUID,    NEW_UUID,    "Ключ"),        
        IS_DELETED                (BOOLEAN,      FALSE,  "1, если запись удалена; иначе 0")
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

    }

    public EnTable (String name, String remark) {
        super (name, remark);
        cols  (c.class);
        pk    (getColumn (c.UUID.toString ()));
    }

}