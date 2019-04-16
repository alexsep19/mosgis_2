package ru.eludia.products.mosgis.db.model.incoming;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Bool.FALSE;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.gosuslugi.dom.schema.integration.msp.ExportCitizenCompensationRequest;

public class InCitizenCompensationCategory extends Table {

    public enum c implements ColEnum {
        
	UUID          (Type.UUID,      NEW_UUID, "Ключ"),
	TS            (Type.TIMESTAMP, NOW,      "Дата/время записи в БД"),
	UUID_ORG      (VocOrganization.class,    "Организация-инициатор импорта"),

	FROMDATE      (Type.DATE, "Дата начала предоставления компенсаций расходов"),
	TODATE        (Type.DATE, null, "Дата окончания предоставления компенсаций расходов"),

	UUID_OUT_SOAP (OutSoap.class, null,      "Импорт"),
	IS_OVER      (Type.BOOLEAN,   FALSE,    "1, процесс завершён; иначе 0")
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
    
    public InCitizenCompensationCategory () {
        
        super ("in_cit_comp_cats", "Запросы на импорт Перечня категорий граждан, имеющих право на получение компенсации расходов");
        
        cols  (c.class);
        
        pk    (c.UUID);
        
    }
}
