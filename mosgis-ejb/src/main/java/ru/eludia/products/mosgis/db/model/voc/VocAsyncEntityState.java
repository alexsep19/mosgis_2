package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;

public class VocAsyncEntityState extends Table {

    public static final String TABLE_NAME = "vc_async_entity_states";

    public VocAsyncEntityState () {
        
        super (TABLE_NAME, "Статусы сихронизируемых сущностей");
        
        pk    ("id",           Type.INTEGER, "Ключ");        
        col   ("label",        Type.STRING,  "Наименование");
        
        data  (i.class);

    }
    
    public enum i {

        PENDING     (10, "Передача в процессе"),
        OK          (20, "Успешно передано"),
        FAIL        (30, "Ошибка передачи");
        
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