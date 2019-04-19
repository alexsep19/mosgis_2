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

public class OverhaulAddressProgramFile extends AttachTable {
    
    public enum c implements EnColEnum {
        
        UUID_OH_ADDR_PR_DOC (OverhaulAddressProgramDocument.class, "Документ адресной программы капитального ремонта"),
        ID_LOG              (OverhaulAddressProgramFileLog.class,  "Послднее событие редактирования")
        
        ;
        
        @Override
        public Col getCol() {
            return col;
        }
        private Col col;

        private c(Type type, Object... p) {
            col = new Col(this, type, p);
        }

        private c(Class c, Object... p) {
            col = new Ref(this, c, p);
        }

        @Override
        public boolean isLoggable() {
            return false;
        }
        
    }
    
    public OverhaulAddressProgramFile () {
        
        super ("tb_oh_addr_pr_files", "Файлы документов адресных программ капитального ремонта");
        
        cols  (c.class);

        key   ("parent", c.UUID_OH_ADDR_PR_DOC);
        key   ("attachmentguid", AttachTable.c.ATTACHMENTGUID);

        trigger("BEFORE UPDATE",
                "BEGIN "
                + CHECK_LEN
                + "END;");
        
    }
    
    public static final Attachments toAttachments(Object name, Object description, Object guid, Object hash) {
        final Attachments a = new Attachments();
        a.setName(name.toString());
        a.setDescription(DB.ok(description) ? description.toString() : name.toString());
        a.setAttachmentHASH(hash.toString());
        final Attachment aa = new Attachment();
        a.setAttachment(aa);
        aa.setAttachmentGUID(guid.toString());
        return a;
    }

    public static final Attachments toAttachments(Map<String, Object> r) {

        return toAttachments(
                r.get(AttachTable.c.LABEL.lc()),
                r.get(AttachTable.c.DESCRIPTION.lc()),
                r.get(AttachTable.c.ATTACHMENTGUID.lc()),
                r.get(AttachTable.c.ATTACHMENTHASH.lc())
        );

    }
    
}
