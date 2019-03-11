package ru.eludia.products.mosgis.db.model.incoming;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Bool.FALSE;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.products.mosgis.db.model.voc.VocDifferentiation;
import ru.eludia.products.mosgis.db.model.voc.VocUser;

public class InVocDiff extends Table {

     public enum c implements ColEnum {
        
        UUID         (Type.UUID,      NEW_UUID, "Ключ"),
        TS           (Type.TIMESTAMP, NOW,      "Дата/время записи в БД"),
        TS_TO        (Type.DATETIME,  null,     "Дата/время окончания операции"),
        UUID_USER    (VocUser.class,  null,     "Оператор"),
        IS_OVER      (Type.BOOLEAN,   FALSE,    "1, процесс завершён; иначе 0"),
        ;
        
        @Override public Col getCol() {return col;}private Col col; private c(Type type, Object... p) {col = new Col(this, type, p);} private c(Class c, Object... p) {col = new Ref(this, c, p);}

    }

    public InVocDiff () {        
        super ("in_" + VocDifferentiation.TABLE_NAME, "Запросы на импорт справочника критериев дифференциации");
        cols  (InVocDiff.c.class);        
        pk    (c.UUID);
/*        
        trigger ("BEFORE UPDATE",                 
            "BEGIN "
              + "  IF :NEW.IS_OVER = 1 AND :OLD.IS_OVER = 0 THEN "
                + " :NEW.TS_TO := SYSDATE;"                      
              + "  END IF;"
            + "END;"     
        );
        
        trigger ("AFTER UPDATE",                 
            "BEGIN "
              + " IF :NEW.IS_OVER = 1 AND :OLD.IS_OVER = 0 THEN "
                + " UPDATE " + VocDifferentiation.TABLE_NAME + " SET is_deleted = 1 WHERE UUID_IMPORT <> :NEW.UUID;"                      
              + " END IF;"
            + "END;"     
        );
*/        
    }
    
}