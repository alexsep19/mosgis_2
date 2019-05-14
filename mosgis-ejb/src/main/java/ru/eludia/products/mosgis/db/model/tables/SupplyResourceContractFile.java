package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.jms.Queue;
import ru.eludia.base.DB;
import ru.eludia.base.db.util.SyncMap;
import static ru.eludia.base.db.util.SyncMap.ACTUAL;
import static ru.eludia.base.db.util.SyncMap.WANTED;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.voc.VocContractDocType;
import ru.eludia.products.mosgis.db.model.voc.VocSupplyResourceContractFileType;
import ru.eludia.products.mosgis.ws.rest.clients.RestGisFilesClient;
import ru.gosuslugi.dom.schema.integration.base.AttachmentType;
import ru.gosuslugi.dom.schema.integration.house_management.ExportCAChResultType;
import ru.gosuslugi.dom.schema.integration.house_management.ExportSupplyResourceContractResultType;

public class SupplyResourceContractFile extends AttachTable {

    public static final String TABLE_NAME = "tb_sr_ctr_files";

    public enum c implements EnColEnum {

        UUID_SR_CTR  (SupplyResourceContract.class,         "Ссылка на договор ресурсоснабжения"),
        ID_TYPE      (VocSupplyResourceContractFileType.class, VocSupplyResourceContractFileType.getDefault (), "Тип"),
        ID_LOG       (SupplyResourceContractFileLog.class,  "Последнее событие редактирования")
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

    public SupplyResourceContractFile () {

        super  (TABLE_NAME, "Файлы, приложенные к договорам ресурсоснабжения");

        cols   (c.class);

        key    ("parent", c.UUID_SR_CTR);
        key    ("attachmentguid", AttachTable.c.ATTACHMENTGUID);

        trigger ("BEFORE UPDATE", "BEGIN " + CHECK_LEN + "END;");

    }
    
    private final static String [] keyFields = {"attachmentguid"};

    public class Sync extends SyncMap<AttachmentType> {
        
        UUID uuid_charter;
        RestGisFilesClient mDB;
	private Queue queue;

        public Sync (DB db, UUID uuid_sr_ctr, RestGisFilesClient mDB, Queue queue) {
	    super (db);
	    this.uuid_charter = uuid_sr_ctr;
	    this.mDB = mDB;
	    this.queue = queue;
	    commonPart.put ("uuid_sr_ctr", uuid_sr_ctr);
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
	    h.put ("len", 0);
	    h.put ("mime", " ");
        }

        @Override
        public Table getTable () {
            return SupplyResourceContractFile.this;
        }

        @Override
        public void processCreated (List<Map<String, Object>> created) throws SQLException {
            
            created.forEach ((h) -> {
                
                final UUID uuid = UUID.fromString (h.get ("uuid").toString ());

                logger.info ("Scheduling download for " + uuid);

                mDB.download (uuid, queue);
                
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
                
                mDB.download (uuid, queue);
                
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
                
        private void addAll (List<AttachmentType> src, VocSupplyResourceContractFileType.i type) {

            for (AttachmentType a: src) {
                Map<String, Object> h = DB.HASH ();
                setFields (h, a);
                h.put ("id_type", type.getId ());
                addRecord (h);
            }

        }        
        
        public void addFrom (ExportSupplyResourceContractResultType t) {
	    
	    List<AttachmentType> files = DB.ok(t.getIsContract())? t.getIsContract().getContractAttachment()
		: DB.ok(t.getIsNotContract()) ? t.getIsNotContract().getContractAttachment()
		: new ArrayList<> ();

	    addAll(files, VocSupplyResourceContractFileType.i.CONTRACT);

        }        
        
    }

    private static final String ATTACHMENTHASH = "attachmenthash";

}
