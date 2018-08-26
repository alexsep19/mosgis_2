package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;

public class VocAsyncRequestState extends Table {

    public VocAsyncRequestState () {
        
        super ("vc_async_request_states", "Статусы обработки запросов");
        
        pk    ("id",           Type.INTEGER, "Ключ");        
        col   ("label",        Type.STRING,  "Наименование");
        
        data  (i.class);

    }
    
    public enum i {
                
        ACCEPTED    (1, "получено"),
        IN_PROGRESS (2, "в обработке"),
        DONE        (3, "обработано");
        
        byte id;
        String label;

        public byte getId () {
            return id;
        }

        public String getLabel () {
            return label;
        }

        private i (int id, String label) {
            this.id = (byte) id;
            this.label = label;
        }
        
    }
    
}