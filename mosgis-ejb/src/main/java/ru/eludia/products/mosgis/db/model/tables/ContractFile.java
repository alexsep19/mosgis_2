package ru.eludia.products.mosgis.db.model.tables;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Blob.EMPTY_BLOB;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Num.ZERO;
import ru.eludia.products.mosgis.db.model.voc.VocContractDocType;
import ru.gosuslugi.dom.schema.integration.base.Attachment;
import ru.gosuslugi.dom.schema.integration.base.AttachmentType;
import ru.gosuslugi.dom.schema.integration.house_management.BaseServiceType;
import ru.gosuslugi.dom.schema.integration.house_management.ContractExportType;
import ru.gosuslugi.dom.schema.integration.house_management.ContractType;
import ru.gosuslugi.dom.schema.integration.house_management.ExportCAChResultType;
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
        col    ("attachmenthash",        Type.BINARY, 32,          null,        "ГОСТ Р 34.11-94");

        col    ("purchasenumber",        Type.STRING, 60,          null,        "Номер извещения (для протокола открытого конкурса)");
        col    ("agreementnumber",       Type.STRING, 255,         null,        "Номер дополнительного соглашения");
        col    ("agreementdate",         Type.DATE,                null,        "Дата дополнительного соглашения");

        col    ("id_status",             Type.INTEGER, 2,          ZERO,        "Статус");
        fk     ("id_log",                ContractFileLog.class,    null,        "Последнее событие редактирования");
        
        key    ("attachmentguid", "attachmentguid");
       
        trigger ("BEFORE UPDATE", "BEGIN "

            + "IF (NVL (:OLD.attachmentguid, '00') = NVL (:NEW.attachmentguid, '00')) THEN BEGIN"
//            + " IF :OLD.id_type <> " + VocContractDocType.i.TERMINATION_ATTACHMENT.getId () + " THEN FOR i IN (SELECT uuid FROM tb_contracts WHERE uuid=:NEW.uuid_contract AND id_ctr_status NOT IN (10, 11) AND contractversionguid IS NOT NULL) LOOP"
//            + "   raise_application_error (-20000, 'Внесение изменений в договор в настоящее время запрещено. Операция отменена.'); "
//            + " END LOOP; END IF; "
            + " UPDATE tb_contract_files__log SET attachmentguid = :NEW.attachmentguid, attachmenthash = :NEW.attachmenthash WHERE uuid = :NEW.id_log; "
            + "END; END IF; "

            + " IF :NEW.id_status = 0 AND DBMS_LOB.GETLENGTH (:NEW.body) = :NEW.len THEN "
            + "   :NEW.id_status := 1; "
            + " END IF;"

        + "END;");        

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
                if (c.getProtocol ()                   == null)                   c.setProtocol (new ContractType.Protocol ());
                if (c.getProtocol ().getProtocolAdd () == null) c.getProtocol ().setProtocolAdd (new ContractType.Protocol.ProtocolAdd ());
                addProtocol (c.getProtocol ().getProtocolAdd (), file, type, at);
                return;
            case TERMINATION_ATTACHMENT:
            case OTHER:
                // do nothing
                return;
        }
        

    }    

    private static void addProtocol (ContractType.Protocol.ProtocolAdd protocolAdd, Map<String, Object> file, VocContractDocType.i type, AttachmentType at) {
                
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
        
        AttachmentType a = (AttachmentType) r.get ("contract_agreement");

        final BaseServiceType result = new BaseServiceType ();

        if (a != null) result.setAgreement (a); else result.setCurrentDoc (true);
        
        return result;

    }
            
    public static Map<UUID, Map<String, Object>> toHashes (ExportCAChResultType.Contract c, Map<String, Object> commonPart) {
        
        Map<UUID, Map<String, Object>> m = new HashMap ();
        
        add (m, c.getCharter (), VocContractDocType.i.CHARTER, commonPart);
        add (m, c.getCommissioningPermitAgreement (), VocContractDocType.i.COMMISSIONING_PERMIT_AGREEMENT, commonPart);
        add (m, c.getContractAttachment (), VocContractDocType.i.CONTRACT_ATTACHMENT, commonPart);
        add (m, c.getSignedOwners (), VocContractDocType.i.SIGNED_OWNERS, commonPart);
        add (m, c.getProtocol (), commonPart);
        
        return m;
        
    }

    private static void add (Map<UUID, Map<String, Object>> m, ContractExportType.Protocol p, Map<String, Object> commonPart) {
        
        if (p == null) return;
            
        ContractExportType.Protocol.ProtocolAdd pa = p.getProtocolAdd ();
            
        if (pa == null) return;
                
        commonPart.put ("purchasenumber", pa.getPurchaseNumber ());

        add (m, pa.getProtocolBuildingOwner (), VocContractDocType.i.PROTOCOL_BUILDING_OWNER, commonPart);
        add (m, pa.getProtocolMeetingBoard  (), VocContractDocType.i.PROTOCOL_MEETING_BOARD,  commonPart);
        add (m, pa.getProtocolMeetingOwners (), VocContractDocType.i.PROTOCOL_MEETING_OWNERS, commonPart);                

        commonPart.remove ("purchasenumber");
                        
    }
    
    public static void add (Map<UUID, Map<String, Object>> dst, List<AttachmentType> src, VocContractDocType.i type, Map<String, Object> commonPart) {
        
        for (AttachmentType a: src) {
            Map<String, Object> h = toHash (a);
            h.putAll (commonPart);
            h.put ("id_type", type.getId ());
            dst.put ((UUID) h.get ("attachmentguid"), h);
        }
        
    }

    public static Map<String, Object> toHash (AttachmentType a) {
        
        Map<String, Object> h = DB.HASH (
            "label",          a.getName (),
            "description",    a.getDescription (),
            "attachmentguid", UUID.fromString (a.getAttachment ().getAttachmentGUID ()),
            "attachmenthash", a.getAttachmentHASH ()
        );
        
        if (a instanceof ContractType.AgreementAttachment) {           
            ContractType.AgreementAttachment aa = (ContractType.AgreementAttachment) a;
            h.put ("agreementnumber", aa.getImprintAgreement ().getAgreementNumber ());
            h.put ("agreementdate",   aa.getImprintAgreement ().getAgreementDate ());
        }        
        
        return h;

    }    

}