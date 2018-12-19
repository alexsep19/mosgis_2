package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.gosuslugi.dom.schema.integration.base.Attachment;
import ru.gosuslugi.dom.schema.integration.house_management.Attachments;

public class VotingProtocolFile extends AttachTable {
    
    public enum c implements EnColEnum {

        UUID_PROTOCOL (VotingProtocol.class,    "Ссылка на объект протокола"),
        ID_LOG        (VotingProtocolFileLog.class,  "Последнее событие редактирования")
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {
            return false;
        }

    }
    
    public VotingProtocolFile () {
        
        super  ("tb_voting_protocol_files", "Файлы, приложенные к протоколу ОСС");
        
        cols   (c.class);
        
        key    ("parent", c.UUID_PROTOCOL);
        key    ("attachmentguid", AttachTable.c.ATTACHMENTGUID);
       
        trigger ("BEFORE UPDATE", 
                "BEGIN "
                    + CHECK_LEN
                + "END;");        

    }
    
    public static final Attachments toAttachments (Object name, Object description, Object guid, Object hash) {
        final Attachments a = new Attachments ();
        a.setName (name.toString ());
        a.setDescription (DB.ok (description) ? description.toString () : name.toString ());
        a.setAttachmentHASH (hash.toString ());
        final Attachment aa = new Attachment ();
        a.setAttachment (aa);
        aa.setAttachmentGUID (guid.toString ());
        return a;
    }
    
    public static final Attachments toAttachments (Map<String, Object> r) {
        
        return toAttachments (
            r.get (AttachTable.c.LABEL.lc ()), 
            r.get (AttachTable.c.DESCRIPTION.lc ()), 
            r.get (AttachTable.c.ATTACHMENTGUID.lc ()), 
            r.get (AttachTable.c.ATTACHMENTHASH.lc ())
        );
        
    }
    
}
