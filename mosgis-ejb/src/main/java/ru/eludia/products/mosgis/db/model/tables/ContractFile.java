package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.db.util.SyncMap;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Blob.EMPTY_BLOB;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Num.ZERO;
import ru.eludia.products.mosgis.db.model.voc.VocContractDocType;
import ru.eludia.products.mosgis.ejb.wsc.RestGisFilesClient;
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
        col    ("mime",                  Type.STRING,              null,        "Тип содержимого");
        col    ("len",                   Type.INTEGER,             null,        "Размер, байт");
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
                
            + " IF :NEW.len IS NULL THEN :NEW.len := -1; END IF; "                

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
    
    private final static String [] keyFields = {"attachmentguid"};

    public class Sync extends SyncMap<AttachmentType> {
        
        UUID uuid_contract;
        RestGisFilesClient filesClient;

        public Sync (DB db, UUID uuid_contract, RestGisFilesClient mDB) {
            super (db);
            this.uuid_contract = uuid_contract;
            this.filesClient = mDB;
            commonPart.put ("uuid_contract", uuid_contract);
            commonPart.put ("id_status", 1);
        }                

        @Override
        public String[] getKeyFields () {
            return keyFields;
        }

        @Override
        public void setFields (Map<String, Object> h, AttachmentType a) {
                        
            h.put ("label", a.getName ());
            h.put ("description",    a.getDescription ());
            h.put ("attachmentguid", a.getAttachment ().getAttachmentGUID ());
            h.put ("attachmenthash", a.getAttachmentHASH ().toUpperCase ());
            
            if (a instanceof ContractType.AgreementAttachment) {           
                ContractType.AgreementAttachment aa = (ContractType.AgreementAttachment) a;
                h.put ("agreementnumber", aa.getImprintAgreement ().getAgreementNumber ());
                h.put ("agreementdate",   aa.getImprintAgreement ().getAgreementDate ());
            }        
            
        }

        @Override
        public Table getTable () {
            return ContractFile.this;
        }

        @Override
        public void processCreated (List<Map<String, Object>> created) throws SQLException {
            
            created.forEach ((h) -> {
                
                final UUID uuid = UUID.fromString (h.get ("uuid").toString ());

                logger.info ("Scheduling download for " + uuid);

                filesClient.download (uuid);
                
            });
                
        }

        @Override
        public void processUpdated (List<Map<String, Object>> updated) throws SQLException {
            
            updated.forEach ((h) -> {
                
                Object hashAsIs = ((Map) h.get (ACTUAL)).get (ATTACHMENTHASH);
                Object hashToBe = ((Map) h.get (WANTED)).get (ATTACHMENTHASH);
                    
                if (DB.eq (hashAsIs, hashToBe) && Long.parseLong (((Map) h.get (ACTUAL)).get ("len").toString ()) > 0) return;
                
                final UUID uuid = UUID.fromString (h.get ("uuid").toString ());

                logger.info ("Scheduling download for " + uuid + ": " + hashToBe + " <> " + hashAsIs);
                
                filesClient.download (uuid);
                
            });
            
        } 

        @Override
        public void processDeleted (List<Map<String, Object>> deleted) throws SQLException {
            
            deleted.forEach ((h) -> {
                Object u = h.get ("uuid");
                h.clear ();
                h.put ("uuid", u);
                h.put ("id_status", 2);
            });
            
            db.update (getTable (), deleted);

        }
        
        private void addAll (List<AttachmentType> src, VocContractDocType.i type) {
            addAll (src, type, Collections.EMPTY_MAP);
        }
        
        private void addAll (List<AttachmentType> src, VocContractDocType.i type, Map<String, Object> commonPart) {

            for (AttachmentType a: src) {
                Map<String, Object> h = DB.HASH ();
                setFields (h, a);
                h.putAll (commonPart);
                h.put ("id_type", type.getId ());
                addRecord (h);
            }

        }
        
        private void add (ContractExportType.Protocol p) {

            if (p == null) return;

            ContractExportType.Protocol.ProtocolAdd pa = p.getProtocolAdd ();

            if (pa == null) return;
            
            Map<String, Object> commonPart = DB.HASH ("purchasenumber", pa.getPurchaseNumber ());

            addAll (pa.getProtocolBuildingOwner (), VocContractDocType.i.PROTOCOL_BUILDING_OWNER, commonPart);
            addAll (pa.getProtocolMeetingBoard  (), VocContractDocType.i.PROTOCOL_MEETING_BOARD,  commonPart);
            addAll (pa.getProtocolMeetingOwners (), VocContractDocType.i.PROTOCOL_MEETING_OWNERS, commonPart);                

        }
        
        public void addFrom (ExportCAChResultType.Contract c) {
            
            addAll (c.getCharter (), VocContractDocType.i.CHARTER);
            addAll (c.getCommissioningPermitAgreement (), VocContractDocType.i.COMMISSIONING_PERMIT_AGREEMENT);
            addAll (c.getContractAttachment (), VocContractDocType.i.CONTRACT_ATTACHMENT);
            addAll (c.getSignedOwners (), VocContractDocType.i.SIGNED_OWNERS);            
            add (c.getProtocol ());
            
        }        
        
    }   
    
    private static final String ATTACHMENTHASH = "attachmenthash";

}