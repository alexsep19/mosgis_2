package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;

public class Lock extends Table {

    public Lock () {

        super ("tb_locks", "Объекты блокировки");

        pk    ("id", Type.STRING, "Ключ");

        data  (i.class);
        
    }
            
    public enum i {
        
        STUCK_CONTRACTS ("stuck_contracts");
        
        String id;

        public String getId () {
            return id;
        }

        private i (String id) {
            this.id = id;
        }
    }
    
}