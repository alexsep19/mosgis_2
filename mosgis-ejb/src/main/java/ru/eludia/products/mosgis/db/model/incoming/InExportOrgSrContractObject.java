package ru.eludia.products.mosgis.db.model.incoming;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationLog;

public class InExportOrgSrContractObject extends Table {

    public static final String TABLE_NAME = "in_exp_org_sr_ctr_obj";

    public enum c implements ColEnum {
        
	UUID             (Type.UUID, NEW_UUID, "Ключ"),
	UUID_VC_ORG_LOG  (VocOrganizationLog.class, "Родительское событие истории организации")
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
    
    public InExportOrgSrContractObject () {
        
        super (TABLE_NAME, "Запросы на импорт ОЖФ ДРСО по организации");
        
        cols  (c.class);
        
        pk    (c.UUID);
    }
    
}
