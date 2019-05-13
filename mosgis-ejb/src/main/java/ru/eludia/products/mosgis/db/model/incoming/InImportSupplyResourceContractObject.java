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
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationLog;

public class InImportSupplyResourceContractObject extends Table {

    public static final String TABLE_NAME = "in_imp_sr_ctr_obj";

    public enum c implements ColEnum {
        
	UUID             (Type.UUID,      NEW_UUID, "Ключ"),
	CONTRACTROOTGUID (Type.UUID, "Идентификатор ДРСО в ГИС ЖКХ"),
	TS               (Type.TIMESTAMP, NOW,      "Дата/время записи в БД"),
	TS_FROM          (Type.TIMESTAMP, null,     "Дата/время обработать позже"),
	UUID_ORG         (VocOrganization.class,    "Организация-инициатор импорта"),
	UUID_OUT_SOAP    (OutSoap.class, null,      "Импорт"),
	UUID_VC_ORG_LOG  (VocOrganizationLog.class, null, "Родительское событие истории организации")
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
    
    public InImportSupplyResourceContractObject () {
        
        super (TABLE_NAME, "Запросы на импорт ОЖФ ДРСО");
        
        cols  (c.class);
        
        pk    (c.UUID);
    }
    
}
