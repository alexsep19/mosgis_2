package ru.eludia.products.mosgis.rest.impl;

import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.jms.Queue;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Predicate;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.rest.api.VocOrganizationsLocal;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.Charter;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisCustomerTypeNsi20;
import ru.eludia.products.mosgis.db.model.voc.VocGisCustomerTypeNsi58;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationLog;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationNsi20;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationTypes;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.jmx.OrgMBean;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;

@Stateless
public class VocOrganizationsImpl extends BaseCRUD<VocOrganization> implements VocOrganizationsLocal {

    private static final Logger logger = Logger.getLogger (VocOrganizationsImpl.class.getName ());
    
    @EJB
    OrgMBean org;
    
    @Resource (mappedName = "mosgis.inOrgByGUIDQueue")
    Queue queue;

    @Override
    public Queue getQueue () {
        return queue;
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
    public JsonObject select (JsonObject p) {
        
        Select select = ModelHolder.getModel ().select (VocOrganization.class, "*", "uuid AS id")
            .orderBy ("label")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

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
    public JsonObject getItem (String id) {

        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {

            JsonObject item = db.getJsonObject (ModelHolder.getModel ()
                .get (VocOrganization.class, id, "AS root", "*")
                .toMaybeOne (VocOrganizationTypes.class, "label").on ()
                .toMaybeOne (VocOrganizationLog.class).on ()
                .toMaybeOne (OutSoap.class, "id_status").on ()
                .toMaybeOne (Charter.class, "AS charter", "uuid").on ("root.uuid=charter.uuid_org")
            );

            jb.add ("item", item);
            
            db.addJsonArrays (jb, ModelHolder.getModel ().select (VocOrganizationNsi20.class, "code").where ("uuid", id));

        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }

        return jb.build ();

    }

    @Override
    public JsonObject doImport (JsonObject p) {

        org.importOrg (p.getJsonObject ("data").getString ("ogrn"));

        return Json.createObjectBuilder ().build ();

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

    @Override
    public JsonObject select (JsonObject p, User user) {
        throw new UnsupportedOperationException ("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}