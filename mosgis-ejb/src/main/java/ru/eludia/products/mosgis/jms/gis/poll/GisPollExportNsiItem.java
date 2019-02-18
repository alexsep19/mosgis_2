package ru.eludia.products.mosgis.jms.gis.poll;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.model.abs.NamedObject;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.def.Bool;
import ru.eludia.products.mosgis.db.model.incoming.nsi.InNsiItem;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.Passport;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.nsi.NsiMultipleRefTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiMultipleScalarTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.Block;
import ru.eludia.products.mosgis.db.model.tables.LivingRoom;
import ru.eludia.products.mosgis.db.model.tables.NonResidentialPremise;
import ru.eludia.products.mosgis.db.model.tables.ResidentialPremise;
import ru.eludia.products.mosgis.db.model.tables.VocNsi236;
import ru.eludia.products.mosgis.db.model.tables.dyn.MultipleRefTable;
import ru.eludia.products.mosgis.db.model.voc.VocNsiList;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementFieldType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementType;
import ru.gosuslugi.dom.schema.integration.nsi_common.GetStateResult;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocNsi197Roles;
import static ru.eludia.products.mosgis.db.model.voc.VocPassportFields.PASSPORT_FIELDS_LIST_NSI_REGISTRY_NUMBER;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.ejb.wsc.WsGisNsiCommonClient;
import ru.eludia.products.mosgis.jmx.NsiLocal;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementNsiRefFieldType;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportNsiItemQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportNsiItem extends UUIDMDB<OutSoap> {
        
    @EJB
    protected UUIDPublisher UUIDPublisher;

    @EJB
    protected WsGisNsiCommonClient wsGisNsiCommonClient;
    
    @EJB
    NsiLocal nsi;

    @Resource (mappedName = "mosgis.nsiTopic")
    Topic nsiTopic;
    
    @Resource (mappedName = "mosgis.outExportNsiItemQueue")
    Queue outExportNsiItemQueue;

    private static MessageDigest md5;
    
    static {
        try {
            md5 = MessageDigest.getInstance ("MD5");
        }
        catch (NoSuchAlgorithmException ex) {
            Logger.getLogger (GisPollExportNsiItem.class.getName()).log (Level.SEVERE, null, ex);
        }
    }

    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ()
            .get    (getTable (), uuid, "*")
        ;        
    }    
        
    private static final String REGISTRY_NUMBER_TOKEN = "RegistryNumber>";
    private final long MINUTE = 60000;   
    
    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        try {
            
            GetStateResult rp = null;
            
            try {
                rp = wsGisNsiCommonClient.getState ((UUID) r.get ("uuid_ack"));
            }
            catch (Exception ex) {
                
                UUID uu = UUID.randomUUID ();
                
                logger.log (Level.SEVERE, uu.toString (), ex);

                db.update (OutSoap.class, HASH (
                    "uuid", uuid,
                    "id_status", DONE.getId (),
                    "is_failed", 1,
                    "err_code",  "WS",
                    "err_text",  uu + " " + ex.getMessage ()
                ));
                
                return;

            }
            
//            String xml = r.get ("rq").toString ();
//            int p1 = xml.indexOf (REGISTRY_NUMBER_TOKEN) + REGISTRY_NUMBER_TOKEN.length ();
//            int p2 = xml.indexOf ('<', p1);                                        
//            int registryNumber = Integer.valueOf (xml.substring (p1, p2));
            
            
            Map<String, Object> map =  db.getMap (ModelHolder.getModel ()
                        .get(InNsiItem.class, uuid, "registrynumber"));
            
            int registryNumber = Integer.valueOf (map.get ("registrynumber").toString ());

            if (rp.getRequestState () < 3) {
                
                long now = System.currentTimeMillis();
                long r_ts = Timestamp.valueOf (r.get ("ts").toString ()).getTime ();
                
                logger.log (Level.INFO, "RequestState time elapsed: " + (now - r_ts) + " ms");
            
                if (now - r_ts < MINUTE) {
                    logger.log (Level.INFO, "republishing uuid " + uuid);
                    UUIDPublisher.publish (outExportNsiItemQueue, uuid);
                } else {
                    logger.log (Level.INFO, "reimporting nsi " + registryNumber);
                    nsi.importNsiItems(registryNumber);
                }
                
                return;
            }
            
            ErrorMessageType errorMessage = rp.getErrorMessage ();
            
            if (errorMessage != null) {
                                
                if ("INT016041".equals (errorMessage.getErrorCode ())) {
                    
                    nsi.importNsiItems (registryNumber, 1);

                    db.update (OutSoap.class, HASH (
                        "uuid", uuid,
                        "id_status", DONE.getId ()
                    ));
                    
                }
                else {

                    db.update (OutSoap.class, HASH (
                        "uuid", uuid,
                        "id_status", DONE.getId (),
                        "is_failed", 1,
                        "err_code",  errorMessage.getErrorCode (),
                        "err_text",  errorMessage.getDescription ()
                    ));
                    
                    nsi.checkForPending ();

                }                
                
                return;

            }

            List<NsiElementType> nsiElement = null;
            int page = 1;
            int pages = 1;
            
            if (rp.getNsiItem () != null) {
                nsiElement = rp.getNsiItem ().getNsiElement ();
            }
            else if (rp.getNsiPagingItem () != null) {
                final GetStateResult.NsiPagingItem nsiPagingItem = rp.getNsiPagingItem ();
                page = Integer.valueOf (nsiPagingItem.getCurrentPage ().toString ());
                pages = nsiPagingItem.getTotalPages ();
                nsiElement = nsiPagingItem.getNsiElement ();
            }
            
            if (nsiElement == null) {
                
                db.update (OutSoap.class, HASH (
                        "uuid", uuid,
                        "id_status", DONE.getId ()
                ));
                
                return;
                
            }            

            if (page == 1) db.update (VocNsiList.class, HASH (
                    "registrynumber", registryNumber,
                    "cols",           new Fields (nsiElement).toJson ().toString ()
            ));
            
            NsiTable table = null;
            
            try {
                table = new NsiTable (db, registryNumber);
            }
            catch (Exception e) {

                logger.log (Level.WARNING, "Cannot load table definition ", e);

                db.update (OutSoap.class, HASH (
                    "uuid", uuid,
                    "id_status", DONE.getId (),
                    "is_failed", 1,
                    "err_code",  "0",
                    "err_text",  e.getMessage ()
                ));
                
                nsi.checkForPending ();
                
                return;

            }

            List <Map<String, Object>> records = new ArrayList<> ();

            RecordsCollector collectRecord = new RecordsCollector (table, records);

            nsiElement.forEach (collectRecord);

            if (collectRecord.isNested ()) {
                table.addParentCol ();
                
                Map<String, Object> record = db.getMap(VocNsiList.class, registryNumber);
                record.put("is_nested", Bool.TRUE);
                db.update(VocNsiList.class, record);
                
            }

            if (!records.isEmpty ()) {

                final Map<String, Object> firstRecord = records.get (0);

                table.getColumns ().values ().stream ().map (i -> i.getName ()).forEach (key -> firstRecord.put (key, firstRecord.get (key)));

            }

            db.adjustTable (table);
            
            for (Table t: table.getTables ()) db.adjustTable (t);
            
            db.updateSchema (table.getTables ());

            db.begin ();

                for (NsiMultipleRefTable refTable: table.getMultipleRefTables ()) {
                    
                    final String fName = refTable.getTargetField ().getfName ();

                    List <Map<String, Object>> m2m = new ArrayList<> ();
                    
                    for (Map<String, Object> record: records) {
                        
                        List guids = (List) record.remove (fName);
                        
                        if (guids == null) continue;
                                                
                        for (int i = 0; i < guids.size (); i ++) m2m.add (HASH (
                            "guid_from", record.get ("guid"),
                            "guid_to",   guids.get (i),
                            "ord",       i + 1
                        ));
                        
                    }

                    db.d0 ("UPDATE " + refTable.getName () + " SET ord = -1");
                        
                    db.upsert (refTable, m2m);

                    db.d0 ("DELETE FROM " + refTable.getName () + " WHERE ord = -1");
                                        
                }
                
                for (NsiMultipleScalarTable sTable: table.getMultipleScalarTables ()) {
                    
                    final String fName = sTable.getValueField ().getfName ();
                    
                    List <Map<String, Object>> m2m = new ArrayList<> ();
                    
                    for (Map<String, Object> record: records) {
                        
                        List values = (List) record.remove (fName);
                        
                        if (values == null) continue;
                                                
                        for (int i = 0; i < values.size (); i ++) m2m.add (HASH (
                            "guid", record.get ("guid"),
                            fName,  values.get (i),
                            "ord",  i + 1
                        ));
                        
                    }
                    
                    db.d0 ("DELETE FROM " + sTable.getName ());

                    db.insert (sTable, m2m);
                    
                }

		ArrayList<String> key = new ArrayList<String>();

		if (registryNumber == 236) { // HACK: 236 pkey
		    key.add(VocNsi236.c.GUID.lc());
		    key.add(VocNsi236.c.PARENT.lc());
		    records.forEach(rec -> {
			if (rec.get("parent") == null) {
			    rec.put("parent", UUID.fromString("00000000-0000-0000-0000-000000000000"));
			}
		    });
		}

		db.upsert(table, records, key.toArray(new String[key.size()]));
                
                if (registryNumber == 197) {
                    
                    List<Map<String, Object>> recordsWithNewParam = new ArrayList<>();
                    records.forEach(rec -> {
                        if (!VocNsi197Roles.NOT_FOR_UO_FIELDS.contains(rec.get("code").toString())) return;
                        rec.put(VocNsi197Roles.c.IS_FOR_UO.lc(), 0);
                        recordsWithNewParam.add(rec);
                    });
                    
                    db.upsert(VocNsi197Roles.class, records);
                    db.upsert(VocNsi197Roles.class, recordsWithNewParam);
                }

                db.update (OutSoap.class, HASH (
                    "uuid", uuid,
                    "id_status", DONE.getId ()
                ));
                
                if (page == pages) {
                                        
                    db.update (VocNsiList.class, HASH (
                        "registrynumber", registryNumber,
                        "ts_last_import", NOW
                    ));
                
                }
            
            db.commit ();
            
            if (registryNumber == PASSPORT_FIELDS_LIST_NSI_REGISTRY_NUMBER) {
                
                List<Passport> passports = new ArrayList<> ();
                
                passports.add ((Passport) ModelHolder.getModel ().get (House.class));
                passports.add ((Passport) ModelHolder.getModel ().get (Block.class));
                passports.add ((Passport) ModelHolder.getModel ().get (LivingRoom.class));
                passports.add ((Passport) ModelHolder.getModel ().get (ResidentialPremise.class));
                passports.add ((Passport) ModelHolder.getModel ().get (NonResidentialPremise.class));
                
                Set<String> refNames = new HashSet<>();
                
                for (Passport passport: (Iterable<Passport>) passports::iterator) {
                    
                    passport.addNsiFields (db);
                    db.updateSchema(passport);
                    List<MultipleRefTable> refTables = passport.getRefTables ();
                    
                    for (MultipleRefTable refTable: (Iterable<MultipleRefTable>) refTables::iterator) {
                    
                        if (refNames.add (refTable.getName ())) {
                            
                            db.updateSchema(refTable);
                            
                        }
                    
                    }
                }
                
            }
            
            if (page < pages) {
                nsi.importNsiItems (registryNumber, page + 1);
            }
            else {
                nsi.checkForPending ();
                
                JsonObject progressObject = nsi.getProgressStatus ();
                if (progressObject == null || progressObject.isEmpty ()) {  
                    UUIDPublisher.publish(nsiTopic, "Updating model");
                }
                
                logger.info (nsi.getProgressStatusText ());
            }
            
        }
        catch (Exception ex) {
            logger.log (Level.SEVERE, null, ex);
        }
    }
    
    private static final Charset UTF8 = Charset.forName ("UTF-8");
    
    private class Fields {
        
        List<Field> l = new ArrayList <> ();
        Map<String, Field> m = new HashMap <> ();
        
        Fields (List<NsiElementType> els) {
            for (NsiElementType el: els) scanElement (el);            
        }

        private void scanElement (NsiElementType el) {
            
            Set <String> seen = new HashSet ();
            
            for (NsiElementFieldType fl: el.getNsiElementField ()) {
                
                final String name = fl.getName ();
                
                Field f;
                
                if (m.containsKey (name)) {
                    f = m.get (name);
                    if (seen.contains (name)) f.setMul (true);
                }
                else {
                    
                    try {
                        f = new Field (fl);
                    }
                    catch (BizarreFieldException ex) {                                   // NPE для NsiRef без RegistryNumber
                        logger.log (Level.WARNING, el.getGUID () + ": " + ex.getMessage ());
                        continue;
                    }
                    
                    m.put (name, f);
                    l.add (f);
                    
                }
                if (fl instanceof NsiElementNsiRefFieldType) {
                    NsiElementNsiRefFieldType.NsiRef nsiRef = ((NsiElementNsiRefFieldType) fl).getNsiRef ();
                    if (nsiRef == null || !nsiRef.getNsiItemRegistryNumber ().toString ().equals (nsiRef.getRef ().getCode ())) f.setNotNsiListRef (true);
                }
                
                for (Field i: l) i.checkForListRef ();
                
                seen.add (name);
                
            }

            for (NsiElementType childElement: el.getChildElement ()) scanElement (childElement);

        }

        JsonArray toJson () {
            
            JsonArrayBuilder b = Json.createArrayBuilder ();
            
            for (Field i: l) b.add (i.toJson ());
            
            return b.build ();
            
        }
        
        private class BizarreFieldException extends Exception {

            public BizarreFieldException (String message) {
                super (message);
            }
            
        }
        
        private class Field extends NamedObject {
            
            String type;
            int ref = -1;
            boolean mul = false;
            boolean notNsiListRef = false;

            Field (NsiElementFieldType el) throws BizarreFieldException {
                super (DB.to.hex (md5.digest (el.getName ().getBytes (UTF8))).substring (0, 10), el.getName ());
                type = el.getClass ().getName ().replace ("ru.gosuslugi.dom.schema.integration.nsi_base.NsiElement", "").replace ("FieldType", "");
                if (el instanceof NsiElementNsiRefFieldType) {
                    final NsiElementNsiRefFieldType.NsiRef nsiRef = ((NsiElementNsiRefFieldType) el).getNsiRef ();
                    if (nsiRef == null) throw new BizarreFieldException ("NsiRefField without nsiRef: " + el.getName ());
                    final BigInteger registryNumber = nsiRef.getNsiItemRegistryNumber ();
                    if (registryNumber == null) throw new BizarreFieldException ("NsiRef without registryNumber: " + el.getName ());
                    ref = registryNumber.intValue ();
                }
            }
            
            public void checkForListRef () {
                if (!"NsiRef".equals (type)) return;
                if (notNsiListRef) return;
                type = "NsiListRef";
                ref = -1;
            }

            public boolean isNotNsiListRef () {
                return notNsiListRef;
            }

            public void setNotNsiListRef (boolean notNsiListRef) {
                this.notNsiListRef = notNsiListRef;
            }

            public void setMul (boolean mul) {
                this.mul = mul;
            }                        
            
            JsonObjectBuilder toJson () {
                
                final JsonObjectBuilder job = Json.createObjectBuilder ()
                    .add ("name", name)
                    .add ("remark", remark)
                    .add ("type", type);
                
                if (ref > 0) job.add ("ref", ref);
                if (mul) job.add ("mul", true);
                
                return job;
                
            }

        }
        
    }

    private static class RecordsCollector implements Consumer<NsiElementType> {

        private final NsiTable table;
        private final List<Map<String, Object>> records;
        
        private Stack <String> guids = new Stack ();
        private boolean nested = false;

        public RecordsCollector (NsiTable table, List<Map<String, Object>> records) {
            this.table = table;
            this.records = records;
        }

        @Override
        public void accept (NsiElementType i) {
            
            final String guid = i.getGUID ();
            
            Map<String, Object> record = HASH (
                    "guid",     guid,
                    "code",     i.getCode (),
                    "isactual", i.isIsActual ()
            );
            
            if (!guids.isEmpty ()) record.put ("parent", guids.peek ());
            
            i.getNsiElementField ().forEach (field -> table.add (record, field));
            
            records.add (record);
            
            final List<NsiElementType> children = i.getChildElement ();
            
            if (children == null || children.isEmpty ()) return;
            
            nested = true;

            guids.push (guid);
            
            children.forEach (this);
            
            guids.pop ();
            
        }

        public boolean isNested () {
            return nested;
        }                
        
    }
    
}