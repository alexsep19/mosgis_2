package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Blob.EMPTY_BLOB;
import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnColEnum;

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
    
}
