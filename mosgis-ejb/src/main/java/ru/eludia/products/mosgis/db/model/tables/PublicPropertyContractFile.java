package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.voc.VocPublicPropertyContractFileType;

public class PublicPropertyContractFile extends AttachTable {
    
    public enum c implements EnColEnum {

        UUID_CTR (PublicPropertyContract.class,    "Ссылка на договор на пользование общим имуществом"),
        ID_TYPE  (VocPublicPropertyContractFileType.class, VocPublicPropertyContractFileType.getDefault (), "Тип"),
        ID_LOG   (PublicPropertyContractFileLog.class,  "Последнее событие редактирования")
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
    
    public PublicPropertyContractFile () {
        
        super  ("tb_pp_ctr_files", "Файлы, приложенные к договорам на пользование общим имуществом");
        
        cols   (c.class);
        
        key    ("parent", c.UUID_CTR);
        key    ("attachmentguid", AttachTable.c.ATTACHMENTGUID);
       
        trigger ("BEFORE UPDATE", "BEGIN " + CHECK_LEN + "END;");

    }
        
}
