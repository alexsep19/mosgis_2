package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.NUMERIC;
import static ru.eludia.base.model.Type.STRING;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;

public class InfrastructureResource extends EnTable {
    
    public enum c implements EnColEnum {
        
        CODE_VC_NSI_2  (STRING, 20, "Ресурс (НСИ 2)"),
        
        SETPOWER       (NUMERIC, 9, 3, null, "Установленная мощность"),
        SITINGPOWER    (NUMERIC, 9, 3, null, "Располагаемая мощность"),
        
        TOTALLOAD      (NUMERIC, 9, 3, null, "Присоединенная нагрузка"),
        INDUSTRIALLOAD (NUMERIC, 9, 3, null, "Промышленность"),
        SOCIALLOAD     (NUMERIC, 9, 3, null, "Социальная сфера"),
        POPULATIONLOAD (NUMERIC, 9, 3, null, "Население"),
        
        OKEI           (VocOkei.class, "Единицы измерения (ОКЕИ)"),
        
        ID_LOG         (InfrastructureResourceLog.class, "Посленее событие редактирования")
        
        ;
        
        @Override
        public Col getCol () {return col;}
        private Col col;
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {
            switch (this) {
                case ID_LOG:
                    return false;
                default:
                    return true;
            }
        }
        
    }
    
    public InfrastructureResource () {
        
        super ("tb_infrastructure_resources", "Мощности ОКИ");
        
        cols (c.class);
        
        trigger ("BEFORE INSERT OR UPDATE", ""
                + "BEGIN "
                    + "IF :NEW.totalload < NVL(:NEW.industrialload, 0) + NVL(:NEW.socialload, 0) + NVL(:NEW.populationload, 0) THEN "
                        + "raise_application_error (-20000, 'Значение присоединенной нагрузки не должно быть меньше, чем сумма составляющих нагрузок'); "
                    + "END IF; "
                + "END;"
        );
        
    }
    
}
