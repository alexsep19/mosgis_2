package ru.eludia.products.mosgis.ws.rest.impl;

import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Predicate;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.incoming.InVocOrganization;
import ru.eludia.products.mosgis.rest.api.VocOrganizationsLocal;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.AccessRequest;
import ru.eludia.products.mosgis.db.model.tables.Charter;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.VoteInitiator;
import ru.eludia.products.mosgis.db.model.voc.VocAccessRequestStatus;
import ru.eludia.products.mosgis.db.model.voc.VocAccessRequestType;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisCustomerTypeNsi20;
import ru.eludia.products.mosgis.db.model.voc.VocGisCustomerTypeNsi58;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationLog;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationNsi20;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationHours;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationTypes;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.tables.DelegationStatus;
import ru.eludia.products.mosgis.db.model.voc.VocDelegationStatus;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.ws.rest.impl.tools.ComplexSearch;
import ru.eludia.products.mosgis.ws.rest.impl.tools.Search;
import ru.eludia.products.mosgis.ws.rest.impl.tools.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class VocOrganizationsImpl extends BaseCRUD<VocOrganization> implements VocOrganizationsLocal {

    private static final Logger logger = Logger.getLogger (VocOrganizationsImpl.class.getName ());
    
    @Resource (mappedName = "mosgis.inOrgQueue")
    Queue inOrgQueue;
    
    @Resource (mappedName = "mosgis.inOrgByGUIDQueue")
    Queue inOrgByGUIDQueue;
    
    @Resource (mappedName = "mosgis.inExportOrgMgmtContractsQueue")
    Queue inExportOrgMgmtContractsQueue;
    
    @Resource (mappedName = "mosgis.inExportOrgSrContractsQueue")
    Queue inExportOrgSrContractsQueue;

    @Resource (mappedName = "mosgis.inExportOrgCharterQueue")
    Queue inExportOrgCharterQueue;

    @Resource (mappedName = "mosgis.inExportOrgAddServicesQueue")
    Queue inExportOrgAddServicesQueue;

    @Resource (mappedName = "mosgis.inExportOrgAccountsQueue")
    Queue inExportOrgAccountsQueue;

    @Override
    protected Queue getQueue (VocAction.i action) {

        switch (action) {
            case REFRESH: return inOrgByGUIDQueue;
            case IMPORT_MGMT_CONTRACTS: return inExportOrgMgmtContractsQueue;
	    case IMPORT_SR_CONTRACTS: return inExportOrgSrContractsQueue;
            case IMPORT_CHARTERS:       return inExportOrgCharterQueue;
            case IMPORT_ADD_SERVICES:   return inExportOrgAddServicesQueue;
            case IMPORT_ACCOUNTS:       return inExportOrgAccountsQueue;
            default: return null;
        }

    }

    private final static String DEFAULT_SEARCH = "label_uc LIKE %?%";
    
    private static final Pattern RE = Pattern.compile ("(\\d+)\\s*(\\d{2,9})?");
    
    private void applyKpp (final String kpp, Select select) {
        
        if (kpp == null || kpp.isEmpty ()) return;
        
        if (kpp.length () == 9) {            
            select.and ("kpp", kpp);            
        }
        else {
            
            StringBuilder sbFrom = new StringBuilder (kpp);
            StringBuilder sbTo   = new StringBuilder (kpp);
            
            for (int i = kpp.length (); i < 9; i ++) {
                sbFrom.append ('0');
                sbTo.append   ('9');
            }
            
            select.and ("kpp BETWEEN ", sbFrom.toString (), sbTo.toString ());
            
        }

    }
    
    private void applySimpleSearch (final SimpleSearch search, Select select) {

        final String searchString = search.getSearchString ();

        Matcher matcher = RE.matcher (searchString);

        if (matcher.matches ()) {

            String term = matcher.group (1);            
            String kpp = matcher.group (2);

            switch (term.length ()) {

                case 10:
                    applyKpp (kpp, select);
                case 12:
                    select.and ("inn", term);
                    break;

                case 13:
                    applyKpp (kpp, select);
                case 15:
                    select.and ("ogrn", term);
                    break;

                default:
                    select.and ("uuid IS NULL");

            }                        

        }
        else {
            select.and (DEFAULT_SEARCH, searchString.toUpperCase ());
        }
        
    }

    private void applyComplexSearch (final ComplexSearch search, Select select) {
        search.filter (select, "");
        Predicate code_vc_nsi_20 = search.getFilters ().get ("code_vc_nsi_20");
        if (code_vc_nsi_20 != null) select.and ("uuid", ModelHolder.getModel ().select (VocOrganizationNsi20.class, "uuid").and ("code", code_vc_nsi_20));           
    }

    private void applySearch (final Search search, Select select) {        

        if (search == null) {
//            select.and ("uuid IS NULL");
        }
        else if (search instanceof ComplexSearch) {
            applyComplexSearch ((ComplexSearch) search, select);
        }
        else if (search instanceof SimpleSearch) {
            applySimpleSearch  ((SimpleSearch) search, select);
        }
        
    }
   
    @Override
    public JsonObject select (JsonObject p, User user) {
        
        Select select = ModelHolder.getModel ().select (VocOrganization.class, "AS root", "*", "uuid AS id")
            .toMaybeOne (DelegationStatus.class, "AS dlg_status", "id_dlg_status AS id_dlg_status").on ("root.uuid=dlg_status.uuid")
            .where ("id_type", p.getString ("id_type", null))
            .and ("is_deleted", 0)
            .orderBy ("label")
            .limit (p.getInt ("offset"), p.getInt ("limit"));
        
        String uuidOrg = user.getUuidOrg ();
        if (!DB.ok (uuidOrg)) uuidOrg = "00000000-0000-0000-0000-000000000000";        
        select.and ("uuid_org_owner...", uuidOrg);
//        Object [] o = {uuidOrg};
//        select.and (VocOrganization.c.UUID_ORG_OWNER.lc (), new Predicate (Operator.EQ, false, true, o));

        applySearch (Search.from (p), select);

        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {
            db.addJsonArrayCnt (jb, select);
        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();

    }    
    
    @Override
    public JsonObject list (JsonObject p) {
        
        int limit = p.getInt ("max");
        String protocol = p.getString("protocol_uuid");
        Search search = Search.from (p);
        
        Select select = ModelHolder.getModel ().select (VocOrganization.class, "AS root", "uuid AS id", "label AS text")
                .where ("uuid NOT IN", ModelHolder.getModel ()
                    .select (VoteInitiator.class, "uuid_org")
                    .where  ("uuid_protocol", protocol)
                    .and    ("uuid_org IS NOT NULL")
                    .and    ("is_deleted", 0)
                )
                .orderBy ("label")
                .limit (0, limit);
        
        applySearch (search, select);
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {
            db.addJsonArrayCnt (jb, select);
        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();
        
    }

    @Override
    public JsonObject getItem (String id, User user) {

        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        Model m = ModelHolder.getModel ();

        try (DB db = m.getDb ()) {

            JsonObject item = db.getJsonObject (ModelHolder.getModel ()
                .get (VocOrganization.class, id, "AS root", "*")
                .toMaybeOne (VocOrganizationTypes.class, "label").on ()
                .toMaybeOne (VocOrganizationLog.class).on ()
                .toMaybeOne (OutSoap.class, "id_status").on ()
                .toMaybeOne (Charter.class, "AS charter", "uuid").on ("root.uuid=charter.uuid_org")
                .toMaybeOne (DelegationStatus.class, "AS dlg_status", "*").on ("root.uuid=dlg_status.uuid")
                .toMaybeOne (VocOrganization.class, "AS parent", "uuid", "label").on ("root.parent=parent.uuid")    
                .orderBy ("charter.id_ctr_status_gis")
            );

            jb.add ("item", item);
            
            db.addJsonArrays (jb, ModelHolder.getModel ().select (VocOrganizationNsi20.class, "code").where ("uuid", id));
            
            if (DB.ok (db.getString (m.select (AccessRequest.class, AccessRequest.c.ACCESSREQUESTGUID.lc ()).where (AccessRequest.c.ORGROOTENTITYGUID.lc (), id)))) jb.add ("is_delegated", 1);
            
            VocAccessRequestType.addTo (jb);
            VocAccessRequestStatus.addTo (jb);
            VocAction.addTo (jb);
	    VocGisStatus.addTo(jb);
            VocDelegationStatus.addTo (jb);

        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }

        return jb.build ();

    }

    @Override
    public JsonObject doImport (JsonObject p, User user) {
        
        MosGisModel m = ModelHolder.getModel ();
        
        try (DB db = m.getDb ()) {
            
            UUID uuid = (UUID) db.insertId (InVocOrganization.class, HASH (
                "uuid_user", user.getId (),
                "ogrn",      p.getJsonObject ("data").getString ("ogrn")
            ));
            
            UUIDPublisher.publish (inOrgQueue, uuid);
            
            return Json.createObjectBuilder ().add ("id", uuid.toString ()).build ();
            
        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }

    }

    @Override
    public JsonObject getVocs () {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {
            
            db.addJsonArrays (jb, 
                    
                ModelHolder.getModel ().select (VocOrganizationTypes.class, "*").orderBy ("label"),
                
                NsiTable.getNsiTable (20).getVocSelect ()
            
            );


        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }

        VocDelegationStatus.addTo (jb);

        return jb.build ();
        
    }

    @Override
    public JsonObject getMgmtNsi58 (String id) {return fetchData ((db, job) -> {

        db.addJsonArrays (job,

            NsiTable.getNsiTable (58).getVocSelect ()

            .toOne (VocGisCustomerTypeNsi58.class, "AS it", "isdefault")
                    
                .where ("id", 
                        
                    db.getModel ().select (VocGisCustomerTypeNsi20.class, "id")
                            
                    .toOne (VocOrganizationNsi20.class)
                        .where ("uuid", id)
                    .on ("vc_gis_customer_type_nsi_20.code=vc_orgs_nsi_20.code")
                        
                )
                    
            .on ("vc_nsi_58.code=it.code")

        );

    });}

    @Override
    public JsonObject doRefresh (String id, User user) {return doAction ((db) -> {
        logAction (db, user, id, VocAction.i.REFRESH);        
    });}
    
    @Override
    public JsonObject doImportMgmtContracts (String id, User user) {return doAction ((db) -> {
        logAction (db, user, id, VocAction.i.IMPORT_MGMT_CONTRACTS);
    });}
    
    @Override
    public JsonObject doImportSrContracts (String id, User user) {return doAction ((db) -> {
        logAction (db, user, id, VocAction.i.IMPORT_SR_CONTRACTS);
    });}

    @Override
    public JsonObject doImportAccounts (String id, User user) {return doAction ((db) -> {
        logAction (db, user, id, VocAction.i.IMPORT_ACCOUNTS);
    });}
    
    @Override
    public JsonObject doImportCharters (String id, User user) {return doAction ((db) -> {
        logAction (db, user, id, VocAction.i.IMPORT_CHARTERS);
    });}
    
    @Override
    public JsonObject doImportAddServices (String id, User user) {return doAction ((db) -> {
        logAction (db, user, id, VocAction.i.IMPORT_ADD_SERVICES);
    });}

    @Override
    public JsonObject doPatch (String id, JsonObject p, User user) {

        JsonObject data = p.getJsonObject("data");

        try (DB db = ModelHolder.getModel().getDb()) {

            db.update(getTable(), HASH(
                    "orgrootentityguid", id,
                    data.getString("k"), data.getString("v", null)
            ));

            logAction(db, user, id, VocAction.i.UPDATE);

        } catch (Exception ex) {
            throw new InternalServerErrorException(ex);
        }

        return this.getItem (id, user);
    }

    @Override
    public JsonObject doPatchHours(String id, JsonObject p) {return doAction((db) -> {

            JsonObject data = p.getJsonObject("data");

            String k = data.getString("k");
            String v = data.getString("v", null);

            db.upsert(VocOrganizationHours.class, HASH(
                    "uuid_org", id,
                    "weekday", data.getString("weekday"),
                    k, v
            ), "uuid_org", "weekday");
    });}

    @Override
    public JsonObject getHours(String id) {

        return fetchData((db, job) -> {

            final MosGisModel m = ModelHolder.getModel();

            db.addJsonArrays(job,
                m.select(VocOrganizationHours.class, "AS voc_organization_hours", "*", "weekday AS id")
                    .where("uuid_org", id)
                    .orderBy("weekday")
            );

        });
    }

    @Override
    protected void logAction (DB db, User user, Object id, VocAction.i action) throws SQLException {

        Table logTable = ModelHolder.getModel ().getLogTable (getTable ());

        if (logTable == null) return;

        String id_log = db.insertId (logTable, HASH (
            "action", action,
            "uuid_object", id,
            "uuid_user", user == null ? null : user.getId ()
        )).toString ();
        
        db.update (getTable (), HASH (
            "orgrootentityguid", id,
            "id_log",    id_log
        ));

        publishMessage (action, id_log);

    }    

}