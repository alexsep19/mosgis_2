package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Virt;

public class CheckPlan extends EnTable {

    public enum c implements EnColEnum {
        
        YEAR                        (Type.NUMERIC, 4, "Год плана"),
        SIGN                        (Type.BOOLEAN, "Признак подписания"),
        SHOULDNOTBEREGISTERED       (Type.BOOLEAN, "Не должен быть зарегестрирован в ЕРП"),
        
        SHOULDBEREGISTERED          (Type.BOOLEAN,     new Virt ("DECODE(\"SHOULDNOTBEREGISTERED\",1,0,1)"), "Должен быть зарегестрирован в ЕРП"),
        URIREGISTRATIONPLANNUMBER   (Type.NUMERIC, 12, null, "Регистрационный номер плана в ЕРП"),
        
        INSPECTIONPLANGUID          (Type.UUID,        null, "Идентификатор плана проверок в ГИС ЖКХ"),
        REGISTRYNUMBER              (Type.STRING, 255, null, "Реестровый номер плана проверок"),
        
        ID_LOG                      (CheckPlanLog.class, "Последнее событие редактирования")
        
        ;
        
        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class<?> c,   Object... p) {col = new Ref (this, c, p);}

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
    
    public CheckPlan () {
        
        super ("tb_check_plans", "Планы проверок");
        
        cols (c.class);
        
    }
    
}
