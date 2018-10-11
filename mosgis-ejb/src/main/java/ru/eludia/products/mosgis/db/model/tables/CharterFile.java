package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Blob.EMPTY_BLOB;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Num.ZERO;
import ru.eludia.products.mosgis.db.model.voc.VocContractDocType;

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
/*    
    private static void fill (AttachmentType result, Map<String, Object> r) {        
        result.setName (r.get ("label").toString ());
        final String desc = r.get ("description").toString ();
        result.setDescription (desc == null || desc.isEmpty () ? " " : desc);
        result.setAttachmentHASH (r.get ("attachmenthash").toString ());
        final Attachment attachment = new Attachment ();
        attachment.setAttachmentGUID (r.get ("attachmentguid").toString ());
        result.setAttachment (attachment);
    }
    
    private static CharterType.AgreementAttachment toAgreementAttachment (Map<String, Object> r) {
        final CharterType.AgreementAttachment result = new CharterType.AgreementAttachment ();
        fill (result, r);
        result.setImprintAgreement ((ImprintAgreementType) DB.to.javaBean (ImprintAgreementType.class, r));
        return result;
    }
    
    public static AttachmentType toAttachmentType (Map<String, Object> r) {
        final AttachmentType result = new AttachmentType ();
        fill (result, r);
        return result;
    }

    public static void add (CharterType c, Map<String, Object> file) {

        VocCharterDocType.i type = VocCharterDocType.i.forId (file.get ("id_type"));

        if (type == VocCharterDocType.i.AGREEMENT_ATTACHMENT) {
            c.getAgreementAttachment ().add (toAgreementAttachment (file));
            return;
        }
        
        AttachmentType at = toAttachmentType (file);
        
        switch (type) {
            case CHARTER:
                c.getCharter ().add (at);
                return;
            case COMMISSIONING_PERMIT_AGREEMENT:
                c.getCommissioningPermitAgreement ().add (at);
                return;
            case CONTRACT:
            case CONTRACT_ATTACHMENT:
                c.getCharterAttachment ().add (at);
                return;                
            case SIGNED_OWNERS:
                c.getSignedOwners ().add (at);
                return;
            case PROTOCOL_BUILDING_OWNER:
            case PROTOCOL_MEETING_BOARD:
            case PROTOCOL_MEETING_OWNERS:
            case PROTOCOL_OK:
                if (c.getProtocol ()                   == null)                   c.setProtocol (new CharterType.Protocol ());
                if (c.getProtocol ().getProtocolAdd () == null) c.getProtocol ().setProtocolAdd (new CharterType.Protocol.ProtocolAdd ());
                addProtocol (c.getProtocol ().getProtocolAdd (), file, type, at);
                return;
            case TERMINATION_ATTACHMENT:
            case OTHER:
                // do nothing
                return;
        }
        

    }    

    private static void addProtocol (CharterType.Protocol.ProtocolAdd protocolAdd, Map<String, Object> file, VocCharterDocType.i type, AttachmentType at) {
                
        protocolAdd.setPurchaseNumber (file.get ("purchasenumber").toString ());
        
        switch (type) {
            
            case PROTOCOL_BUILDING_OWNER:
                protocolAdd.getProtocolBuildingOwner ().add (at);
                break;
            case PROTOCOL_MEETING_BOARD:
                protocolAdd.getProtocolMeetingBoard ().add (at);
                break;
            case PROTOCOL_MEETING_OWNERS:
                protocolAdd.getProtocolMeetingOwners ().add (at);
                break;
            case PROTOCOL_OK:
                protocolAdd.getProtocolOK ().add (at);
                break;

        }
                
    }    
    
    public static BaseServiceType getBaseServiceType (Map<String, Object> r) {
        
        AttachmentType a = (AttachmentType) r.get ("charter_agreement");

        final BaseServiceType result = new BaseServiceType ();

        if (a != null) result.setAgreement (a); else result.setCurrentDoc (true);
        
        return result;

    }
*/
}