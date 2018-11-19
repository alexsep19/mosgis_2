package ru.eludia.products.mosgis.db.model;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Blob.EMPTY_BLOB;
import static ru.eludia.base.model.def.Num.ZERO;

public abstract class AttachTable extends EnTable {
    
    protected final static String CHECK_LEN =
        " IF :NEW.id_status = 0 AND DBMS_LOB.GETLENGTH (:NEW.body) = :NEW.len THEN "
        + "   :NEW.id_status := 1; "
        + " END IF;";

    public enum c implements EnColEnum {

        LABEL          (Type.STRING,                        "Имя файла"),
        MIME           (Type.STRING,                        "Тип содержимого"),
        LEN            (Type.INTEGER,                       "Тип содержимого"),
        BODY           (Type.BLOB,              EMPTY_BLOB, "Содержимое"),
        DESCIPTION     (Type.TEXT, null,                    "Имя файла"),
        ATTACHMENTGUID (Type.UUID,                    null, "Идентификатор сохраненного вложения"),
        ATTACHMENTHASH (Type.BINARY, 32,              null, "Идентификатор сохраненного вложения"),
        ID_STATUS      (Type.INTEGER,                 ZERO, "Статус"),
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {
            return true;
        }

    }

    public AttachTable (String name, String remark) {
        super (name, remark);
        cols  (c.class);
    }

}