package ru.eludia.products.mosgis.db.model.incoming;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class InOverhaulWorkType extends Table {

    public enum c implements ColEnum {
        
        UUID          (Type.UUID,      NEW_UUID, "Ключ"),
        TS            (Type.TIMESTAMP, NOW,      "Дата/время записи в БД"),
        UUID_ORG      (VocOrganization.class,    "Организация-инициатор импорта"),
        UUID_OUT_SOAP (OutSoap.class, null,      "Импорт")
        
        ;
        
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
    
    public InOverhaulWorkType () {
        
        super ("in_oh_wk_types", "Запросы на импорт видов работ капитального ремонта");
        
        cols  (c.class);
        
        pk    (c.UUID);
        
    }
    
}
