package ru.eludia.products.mosgis.db.model;

import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Blob.EMPTY_BLOB;
import static ru.eludia.base.model.def.Num.ZERO;
import ru.gosuslugi.dom.schema.integration.base.Attachment;
import ru.gosuslugi.dom.schema.integration.base.AttachmentType;

public abstract class AttachTable extends EnTable {
    
    protected final static String CHECK_LEN =
        " IF :NEW.id_status = 0 AND DBMS_LOB.GETLENGTH (:NEW.body) = :NEW.len THEN "
        + "   :NEW.id_status := 1; "
        + " END IF;";

    public enum c implements EnColEnum {

        LABEL          (Type.STRING, 1024,                  "Имя файла"),
        MIME           (Type.STRING,                        "Тип содержимого"),
        LEN            (Type.INTEGER,                       "Тип содержимого"),
        BODY           (Type.BLOB,              EMPTY_BLOB, "Содержимое"),
        DESCRIPTION    (Type.STRING, 500,             null, "Имя файла"),
        ATTACHMENTGUID (Type.UUID,                    null, "Идентификатор сохраненного вложения"),
        ATTACHMENTHASH (Type.BINARY, 32,              null, "ГОСТ Р 34.11-94"),
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
    
    public static final AttachmentType toAttachmentType (Object name, Object description, Object guid, Object hash) {
        final AttachmentType a = new AttachmentType ();
        a.setName (name.toString ());
        a.setDescription (DB.ok (description) ? description.toString () : name.toString ());
        a.setAttachmentHASH (hash.toString ());
        final Attachment aa = new Attachment ();
        a.setAttachment (aa);
        aa.setAttachmentGUID (guid.toString ());
        return a;
    }
    
    public static final AttachmentType toAttachmentType (Map<String, Object> r) {
        
        return toAttachmentType (
            r.get (c.LABEL.lc ()), 
            r.get (c.DESCRIPTION.lc ()), 
            r.get (c.ATTACHMENTGUID.lc ()), 
            r.get (c.ATTACHMENTHASH.lc ())
        );
        
    }

}