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
import ru.gosuslugi.dom.schema.integration.house_management.ContractType;
import ru.gosuslugi.dom.schema.integration.house_management.ImprintAgreementType;

public class ContractFile extends Table {

    public ContractFile () {
        
        super  ("tb_contract_files", "Файлы, приложенные к договорам");
        
        pk     ("uuid",                  Type.UUID,                NEW_UUID,    "Ключ");
        
        ref    ("uuid_contract",         Contract.class,                        "Ссылка на договор");
        ref    ("uuid_contract_object",  ContractObject.class,     null,        "Ссылка на объект договора");
        ref    ("id_type",               VocContractDocType.class,              "Ссылка на тип документа");
        
        col    ("label",                 Type.STRING,                           "Имя файла");
        col    ("mime",                  Type.STRING,                           "Тип содержимого");
        col    ("len",                   Type.INTEGER,                          "Размер, байт");
        col    ("body",                  Type.BLOB,                EMPTY_BLOB,  "Содержимое");
        col    ("description",           Type.TEXT,                null,        "Примечание");
        col    ("attachmentguid",        Type.UUID,                null,        "Идентификатор сохраненного вложения");

        col    ("purchasenumber",        Type.STRING, 60,          null,        "Номер извещения (для протокола открытого конкурса)");
        col    ("agreementnumber",       Type.STRING, 255,         null,        "Номер дополнительного соглашения");
        col    ("agreementdate",         Type.DATE,                null,        "Дата дополнительного соглашения");

        col    ("id_status",             Type.INTEGER, 1,          ZERO,        "Статус");

        trigger ("BEFORE UPDATE", "BEGIN "
                
            + " IF :NEW.id_status = 0 AND DBMS_LOB.GETLENGTH (:NEW.body) = :NEW.len THEN "
            + "   :NEW.id_status := 1; "
            + " END IF;"
                
        + "END;");        

    }
    
    private static void fill (AttachmentType result, Map<String, Object> r) {
        result.setName (r.get ("label").toString ());
        result.setDescription (r.get ("description").toString ());
        final Attachment attachment = new Attachment ();
        attachment.setAttachmentGUID (r.get ("attachmentguid").toString ());
        result.setAttachment (attachment);
    }
    
    private static ContractType.AgreementAttachment toAgreementAttachment (Map<String, Object> r) {
        final ContractType.AgreementAttachment result = new ContractType.AgreementAttachment ();
        fill (result, r);
        result.setImprintAgreement ((ImprintAgreementType) DB.to.javaBean (ImprintAgreementType.class, r));
        return result;
    }
    
    public static AttachmentType toAttachmentType (Map<String, Object> r) {
        final AttachmentType result = new AttachmentType ();
        fill (result, r);
        return result;
    }

    public static void add (ContractType c, Map<String, Object> file) {

        VocContractDocType.i type = VocContractDocType.i.forId (file.get ("id_type"));

        if (type == VocContractDocType.i.AGREEMENT_ATTACHMENT) {
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
                c.getContractAttachment ().add (at);
                return;                
            case SIGNED_OWNERS:
                c.getSignedOwners ().add (at);
                return;
            case PROTOCOL_BUILDING_OWNER:
            case PROTOCOL_MEETING_BOARD:
            case PROTOCOL_MEETING_OWNERS:
            case PROTOCOL_OK:
                c.setProtocol (toProtocol (file, type, at));
                return;
            case OTHER:
                // do nothing
                return;                
        }
        

    }    

    private static ContractType.Protocol toProtocol (Map<String, Object> file, VocContractDocType.i type, AttachmentType at) {
        
        final ContractType.Protocol protocol = new ContractType.Protocol ();
        
        final ContractType.Protocol.ProtocolAdd protocolAdd = new ContractType.Protocol.ProtocolAdd ();
        
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
        
        protocol.setProtocolAdd (protocolAdd);
        
        return protocol;
        
    }    

}