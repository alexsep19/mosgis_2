package ru.eludia.products.mosgis.db.model.incoming;

import static ru.eludia.base.model.def.Blob.EMPTY_BLOB;
import static ru.eludia.base.model.def.Bool.FALSE;
import static ru.eludia.base.model.def.Def.NOW;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.tables.Sender;

public class UploadedFile extends EnTable {
    
    public enum c implements ColEnum {

        LABEL          (Type.STRING, 1024,              "Имя файла"),
        UUID_SENDER    (Sender.class,       null,       "Поставщик информации"),
        BODY           (Type.BLOB,          EMPTY_BLOB, "Содержимое"),
        ORGPPAGUID     (Type.UUID,                      "Идентификатор зарегистрированной организации"),
    	UPLOAD_CONTEXT (Type.STRING, 50,    null,       "Контекст, в который загружен файл"),
    	IS_COMPLETED   (Type.BOOLEAN,       FALSE,      "Файл загружен? 0 - нет, 1 - да"),
    	TS             (Type.TIMESTAMP,     NOW,        "Дата/время записи в БД");
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class<?> c,   Object... p) {col = new Ref (this, c, p);}

    }

    public UploadedFile () {
        super ("in_upl_files", "Файлы, загруженные через REST-сервис");
        cols  (c.class);
        
        key   ("orgppaguid", "orgppaguid");
        key   ("context", "upload_context");
    }
    
}