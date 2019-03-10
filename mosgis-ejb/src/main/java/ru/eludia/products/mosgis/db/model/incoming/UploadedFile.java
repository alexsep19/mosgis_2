package ru.eludia.products.mosgis.db.model.incoming;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnTable;
import static ru.eludia.base.model.def.Blob.EMPTY_BLOB;
import ru.eludia.products.mosgis.db.model.tables.Sender;

public class UploadedFile extends EnTable {
    
    public enum c implements ColEnum {

        LABEL          (Type.STRING, 1024,                  "Имя файла"),
//        LEN            (Type.INTEGER,                       "Размер"),
        UUID_SENDER    (Sender.class,           null,       "Поставщик информации"),
        BODY           (Type.BLOB,              EMPTY_BLOB, "Содержимое"),
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

    }

    public UploadedFile () {
        super ("in_upl_files", "Файлы, загруженные через REST-сервис");
        cols  (c.class);
    }
    
}