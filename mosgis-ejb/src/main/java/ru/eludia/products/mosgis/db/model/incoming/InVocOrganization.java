package ru.eludia.products.mosgis.db.model.incoming;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.products.mosgis.db.model.tables.License;
import ru.eludia.products.mosgis.db.model.voc.VocUser;

public class InVocOrganization extends Table {

     public enum c implements ColEnum {
        
        UUID         (Type.UUID,      NEW_UUID, "Ключ"),
        TS           (Type.TIMESTAMP, NOW,      "Дата/время записи в БД"),
        UUID_USER    (VocUser.class,  null,     "Оператор"),
        OGRN         (Type.NUMERIC,   15,       "ОГРН");
        
        @Override
        public Col getCol() {
            return col;
        }
        private Col col;

        private c(Type type, Object... p) {
            col = new Col(this, type, p);
        }

        private c(Class c, Object... p) {
            col = new Ref(this, c, p);
        }

    }

    public InVocOrganization () {
        
        super ("in_vc_orgs", "Запросы на импорт организаций по ОГРН");
        
        cols(InVocOrganization.c.class);
        
        pk    (c.UUID);
                        
    }
    
}