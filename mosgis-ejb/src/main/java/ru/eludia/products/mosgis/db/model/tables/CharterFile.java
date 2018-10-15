package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import ru.eludia.base.DB;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Blob.EMPTY_BLOB;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Num.ZERO;
import ru.eludia.products.mosgis.db.model.voc.VocContractDocType;
import ru.gosuslugi.dom.schema.integration.base.Attachment;
import ru.gosuslugi.dom.schema.integration.base.AttachmentType;
import ru.gosuslugi.dom.schema.integration.house_management.BaseServiceCharterType;
import ru.gosuslugi.dom.schema.integration.house_management.ImportCharterRequest;

public class CharterFile extends Table {

    public CharterFile () {
        
        super  ("tb_charter_files", "Файлы, приложенные к договорам");
        
        pk     ("uuid",                  Type.UUID,                NEW_UUID,    "Ключ");
        
        ref    ("uuid_charter",          Charter.class,                         "Ссылка на договор");
        ref    ("uuid_charter_object",   CharterObject.class,     null,         "Ссылка на объект договора");
        ref    ("id_type",               VocContractDocType.class,              "Ссылка на тип документа");
        
        col    ("label",                 Type.STRING,                           "Имя файла");
        col    ("mime",                  Type.STRING,                           "Тип содержимого");
        col    ("len",                   Type.INTEGER,                          "Размер, байт");
        col    ("body",                  Type.BLOB,                EMPTY_BLOB,  "Содержимое");
        col    ("description",           Type.TEXT,                null,        "Примечание");
        col    ("attachmentguid",        Type.UUID,                null,        "Идентификатор сохраненного вложения");
        col    ("attachmenthash",        Type.BINARY, 32,          null,        "ГОСТ Р 34.11-94");

        col    ("id_status",             Type.INTEGER, 2,          ZERO,        "Статус");
        fk     ("id_log",                CharterFileLog.class,    null,         "Последнее событие редактирования");
       
        trigger ("BEFORE UPDATE", "BEGIN "
/*
            + "IF (NVL (:OLD.attachmentguid, '00') = NVL (:NEW.attachmentguid, '00')) THEN BEGIN"
            + " IF :OLD.id_type <> " + VocContractDocType.i.TERMINATION_ATTACHMENT.getId () + " THEN FOR i IN (SELECT uuid FROM tb_charters WHERE uuid=:NEW.uuid_charter AND id_ctr_status NOT IN (10, 11)) LOOP"
            + "   raise_application_error (-20000, 'Внесение изменений в устав в настоящее время запрещено. Операция отменена.'); "
            + " END LOOP; END IF; "
            + " UPDATE tb_charter_files__log SET attachmentguid = :NEW.attachmentguid, attachmenthash = :NEW.attachmenthash WHERE uuid = :NEW.id_log; "
            + "END; END IF; "
*/
            + " IF :NEW.id_status = 0 AND DBMS_LOB.GETLENGTH (:NEW.body) = :NEW.len THEN "
            + "   :NEW.id_status := 1; "
            + " END IF;"

        + "END;");        

    }
    
    public static AttachmentType toAttachmentType (Map<String, Object> r) {
        final AttachmentType result = new AttachmentType ();
        fill (result, r);
        return result;
    }
    
    private static void fill (AttachmentType result, Map<String, Object> r) {        
        final String label = r.get ("label").toString ();
        result.setName (label);
        final String desc = DB.to.String (r.get ("description")).trim ();
        result.setDescription (desc.isEmpty () ? label : desc);
        result.setAttachmentHASH (r.get ("attachmenthash").toString ());
        final Attachment attachment = new Attachment ();
        attachment.setAttachmentGUID (r.get ("attachmentguid").toString ());
        result.setAttachment (attachment);
    }
    
    public static void add (ImportCharterRequest.PlacingCharter pc, Map<String, Object> file) {
        
        VocContractDocType.i type = VocContractDocType.i.forId (file.get ("id_type"));
        
        AttachmentType at = toAttachmentType (file);
        
        switch (type) {
            case CHARTER:
                pc.getAttachmentCharter ().add (at);
                return;
            default:
                // do nothing
        }
        
    }

    static BaseServiceCharterType getBaseServiceType (Map<String, Object> r) {
        
        AttachmentType a = null;//(AttachmentType) r.get ("charter_agreement");

        final BaseServiceCharterType result = new BaseServiceCharterType ();

        if (a != null) result.setProtocolMeetingOwners (a); else result.setCurrentCharter (true);
        
        return result;
        
    }
    
}