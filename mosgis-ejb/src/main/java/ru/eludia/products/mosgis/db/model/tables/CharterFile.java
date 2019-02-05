package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.db.util.SyncMap;
import static ru.eludia.base.db.util.SyncMap.ACTUAL;
import static ru.eludia.base.db.util.SyncMap.WANTED;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Blob.EMPTY_BLOB;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Num.ZERO;
import ru.eludia.products.mosgis.db.model.voc.VocCharterObjectReason;
import ru.eludia.products.mosgis.db.model.voc.VocContractDocType;
import ru.eludia.products.mosgis.ejb.wsc.RestGisFilesClient;
import ru.eludia.products.mosgis.jms.gis.poll.GisPollExportCharterDataMDB;
import ru.gosuslugi.dom.schema.integration.base.Attachment;
import ru.gosuslugi.dom.schema.integration.base.AttachmentType;
import ru.gosuslugi.dom.schema.integration.house_management.BaseServiceCharterType;
import ru.gosuslugi.dom.schema.integration.house_management.CharterType;
import ru.gosuslugi.dom.schema.integration.house_management.ExportCAChResultType;

public class CharterFile extends Table {

    public CharterFile () {
        
        super  ("tb_charter_files", "Файлы, приложенные к договорам");
        
        pk     ("uuid",                  Type.UUID,                NEW_UUID,    "Ключ");
        
        ref    ("uuid_charter",          Charter.class,                         "Ссылка на договор");
        ref    ("uuid_charter_object",   CharterObject.class,     null,         "Ссылка на объект договора");
        ref    ("id_type",               VocContractDocType.class,              "Ссылка на тип документа");
        
        col    ("label",                 Type.STRING,                           "Имя файла");
        col    ("mime",                  Type.STRING,              null,        "Тип содержимого");
        col    ("len",                   Type.INTEGER,             null,        "Размер, байт");
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
            + " IF :NEW.len IS NULL THEN :NEW.len := -1; END IF; "
                
            + " IF :NEW.id_status = 0 AND DBMS_LOB.GETLENGTH (:NEW.body) = :NEW.len THEN BEGIN "
            + "   :NEW.id_status := 1; "
            + "   IF :NEW.uuid_charter_object IS NOT NULL THEN "
            + "     UPDATE tb_charter_objects SET uuid_charter_file = :NEW.uuid WHERE "
                + "   uuid = :NEW.uuid_charter_object "
                + "   AND id_reason = " + VocCharterObjectReason.i.PROTOCOL.getId ()
                + "   AND uuid_charter_file IS NULL; "
            + "   END IF;"
            + " END; END IF;"

        + "END;");        

    }
    
    public static AttachmentType toAttachmentType (Map<String, Object> r, String prefix) {
        final AttachmentType result = new AttachmentType ();
        fill (result, r, prefix);
        return result;
    }
    
    private static void fill (AttachmentType result, Map<String, Object> r, String prefix) {        
        final String label = r.get (prefix + "label").toString ();
        result.setName (label);
        final String desc = DB.to.String (r.get (prefix + "description")).trim ();
        result.setDescription (desc.isEmpty () ? label : desc);
        result.setAttachmentHASH (r.get (prefix + "attachmenthash").toString ());
        final Attachment attachment = new Attachment ();
        attachment.setAttachmentGUID (r.get (prefix + "attachmentguid").toString ());
        result.setAttachment (attachment);
    }
    
    public static void add (CharterType pc, Map<String, Object> file) {
        
        VocContractDocType.i type = VocContractDocType.i.forId (file.get ("id_type"));
        
        AttachmentType at = toAttachmentType (file, "");
        
        switch (type) {
            case CHARTER:
                pc.getAttachmentCharter ().add (at);
                break;
            case PROTOCOL_MEETING_OWNERS:
                if (pc.getMeetingProtocol () == null) pc.setMeetingProtocol (new CharterType.MeetingProtocol ());
                pc.getMeetingProtocol ().getProtocolMeetingOwners ().add (at);
                break;
            default:
                // do nothing
        }
        
    }

    static BaseServiceCharterType getBaseServiceType (Map<String, Object> r) {
        
        final BaseServiceCharterType result = new BaseServiceCharterType ();
        
        if (r.get ("doc.attachmenthash") == null)
            result.setCurrentCharter (true);
        else
            result.setProtocolMeetingOwners (toAttachmentType (r, "doc."));
        
        return result;
        
    }
    
    private final static String [] keyFields = {"attachmentguid"};

    public class Sync extends SyncMap<AttachmentType> {
        
        UUID uuid_charter;
        RestGisFilesClient mDB;

        public Sync (DB db, UUID uuid_charter, RestGisFilesClient mDB) {
            super (db);
            this.uuid_charter = uuid_charter;
            this.mDB = mDB;
            commonPart.put ("uuid_charter", uuid_charter);
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
        }

        @Override
        public Table getTable () {
            return CharterFile.this;
        }

        @Override
        public void processCreated (List<Map<String, Object>> created) throws SQLException {
            
            created.forEach ((h) -> {
                
                final UUID uuid = UUID.fromString (h.get ("uuid").toString ());

                logger.info ("Scheduling download for " + uuid);

                mDB.download (uuid);
                
            });
                
        }

        @Override
        public void processUpdated (List<Map<String, Object>> updated) throws SQLException {

            updated.forEach ((h) -> {
                
                Object hashAsIs = ((Map) h.get (ACTUAL)).get (ATTACHMENTHASH);
                Object hashToBe = ((Map) h.get (WANTED)).get (ATTACHMENTHASH);
                
                Map act = (Map) h.get (ACTUAL);
                Object len = act.get ("len");
                    
                if (DB.eq (hashAsIs, hashToBe) && DB.ok (len)) return;
                
                final UUID uuid = UUID.fromString (h.get ("uuid").toString ());

                logger.info ("Scheduling download for " + uuid + ": " + hashToBe + " <> " + hashAsIs);
                
                mDB.download (uuid);
                
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

            for (AttachmentType a: src) {
                Map<String, Object> h = DB.HASH ();
                setFields (h, a);
                h.put ("id_type", type.getId ());
                addRecord (h);
            }

        }        
        
        public void addFrom (ExportCAChResultType.Charter c) {
            addAll (c.getAttachmentCharter (), VocContractDocType.i.CHARTER);
        }        
        
    }

    private static final String ATTACHMENTHASH = "attachmenthash";    

}