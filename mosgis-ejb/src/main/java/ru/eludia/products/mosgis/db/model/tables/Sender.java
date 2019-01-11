package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Bool.FALSE;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;

public class Sender extends EnTable {
    
    public enum c implements EnColEnum {

        UUID_WORKING_LIST    (WorkingList.class,     "Ссылка на перечень"),
//        ID_LOG               (SenderLog.class,  "Последнее событие редактирования"),
        LABEL_FULL           (Type.STRING, null,  "Наименование системы"),
        LABEL                (Type.STRING,        "Сокращенное наименование"),
        CONTACT              (Type.STRING, null,  "Ответственный за интеграцию"),
        LOGIN                (Type.STRING, null,  "Логин"),
        IS_LOCKED            (Type.BOOLEAN,      FALSE,  "1, если запись заблокирована; иначе 0")
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {
            switch (this) {
//                case ID_LOG:
//                    return false;
                default: 
                    return true;
            }
        }

    } 

    public Sender () {        
        super ("tb_senders", "Поставщики данных");                
        cols   (c.class);        
    }
    
}