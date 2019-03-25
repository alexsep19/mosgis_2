package ru.eludia.products.mosgis.ws.rest.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Operator;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.def.Bool;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.ActualBankAccount;
import ru.eludia.products.mosgis.db.model.tables.AnyRcContract;
import ru.eludia.products.mosgis.db.model.tables.BankAccount;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.RcContract;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocBic;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocRcContractServiceTypes;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.ActualSomeContractObject;
import ru.eludia.products.mosgis.db.model.tables.CharterLog;
import ru.eludia.products.mosgis.db.model.tables.CharterObject;
import ru.eludia.products.mosgis.db.model.tables.CharterObjectLog;
import ru.eludia.products.mosgis.db.model.tables.CharterObjectService;
import ru.eludia.products.mosgis.db.model.tables.CharterObjectServiceLog;
import ru.eludia.products.mosgis.db.model.tables.RcContractObject;
import ru.eludia.products.mosgis.db.model.voc.VocBuildingAddress;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.RcContractLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.ws.rest.impl.tools.ComplexSearch;
import ru.eludia.products.mosgis.ws.rest.impl.tools.Search;
import ru.eludia.products.mosgis.ws.rest.impl.tools.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class RcContractImpl extends BaseCRUD<RcContract> implements RcContractLocal {

    private static final Logger logger = Logger.getLogger (RcContractImpl.class.getName ());

    private void filterOffDeleted (Select select) {
        select.and (EnTable.c.IS_DELETED, Operator.EQ, 0);
    }

    private void applyComplexSearch (final ComplexSearch search, Select select) {

        search.filter (select, "");
        if (!search.getFilters ().containsKey ("is_deleted")) filterOffDeleted (select);

    }

    private void applySimpleSearch (final SimpleSearch search, Select select) {

        final String s = search.getSearchString ();

        if (s != null) select.and ("customer_label_uc LIKE ?%", s.toUpperCase ().replace (' ', '%'));

    }

    private void applySearch (final Search search, Select select) {

        if (search instanceof ComplexSearch) {
            applyComplexSearch ((ComplexSearch) search, select);
        }
        else {
            if (search instanceof SimpleSearch) applySimpleSearch  ((SimpleSearch) search, select);
            filterOffDeleted (select);
        }

    }

    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {

        final Model m = ModelHolder.getModel ();

        Select select = m.select (AnyRcContract.class, "*")
            .orderBy (RcContract.c.SIGNINGDATE.lc() + " DESC")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        applySearch (Search.from (p), select);

        JsonObject data = p.getJsonObject ("data");

        String k = RcContract.c.UUID_ORG.lc();
        String v = data.getString (k, null);
        if (DB.ok (v)) select.and (k, v);

        String k_c = RcContract.c.UUID_ORG_CUSTOMER.lc();
        String v_c = data.getString(k_c, null);
        if (DB.ok(v_c)) {
            select.and(k_c, v_c);
        }

        db.addJsonArrayCnt (job, select);

    });}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {

        final MosGisModel m = ModelHolder.getModel ();

        final JsonObject item = db.getJsonObject (m
            .get (RcContract.class, id, "*")
            .toOne(VocOrganization.class, "AS org_customer", "label").on("uuid_org_customer")
            .toOne(VocOrganization.class, "AS org", "label").on("uuid_org")
            .toMaybeOne (BankAccount.class,     "AS bank_acct",        "*").on ()
            .toMaybeOne (VocBic.class,                                 "*").on ()
            .toMaybeOne (VocOrganization.class, "AS org_bank_acct","label").on ("bank_acct.uuid_org=org_bank_acct.uuid")
        );

        job.add ("item", item);
        
        final int is_billing = item.getInt (RcContract.c.IS_BILLING.lc ());        
        final boolean ok_is_billing = DB.ok (is_billing);
        ActualBankAccount.addTo (job, db,
            item.getString ((ok_is_billing ?
                RcContract.c.UUID_ORG_CUSTOMER :
                RcContract.c.UUID_ORG
            ).lc ())
        );

    });}

    @Override
    public JsonObject getVocs (JsonObject p) {return fetchData((db, job) -> {
        VocGisStatus.addLiteTo (job);
	VocAction.addTo(job);
	VocRcContractServiceTypes.addTo(job);
        db.addJsonArrays (job, NsiTable.getNsiTable(54).getVocSelect ());
    });}
    
    @Override
    public JsonObject doApprove (String id) {return doAction ((DB db) -> {
          
        db.update (getTable (), HASH (
            "uuid", id,
            "id_ctr_status", 40
        ));
        
        final Model m = db.getModel();
        
        List<Map<String, Object>> idsRcContractObject = db.getList (m.select(RcContractObject.class, "uuid" )
            .where(RcContractObject.c.UUID_RC_CTR, id)
            .and(RcContractObject.c.ID_CTR_STATUS, 10));
        
        List<Map<String, Object>> newIdsRcContractObject = new ArrayList<>();
        
        idsRcContractObject.forEach(rec -> {
            rec.put(RcContractObject.c.ID_CTR_STATUS.lc(), 40);
            newIdsRcContractObject.add(rec);
        });

        db.update (RcContractObject.class, newIdsRcContractObject);

    });}
    
    @Override
    public JsonObject doAlter (String id) {return doAction ((DB db) -> {
          
        db.update (getTable (), HASH (
            "uuid", id,
            "id_ctr_status", 11
        ));

    });}
    
    @Override
    public JsonObject doAnnul (String id, JsonObject p) {return doAction ((DB db) -> {
        
        JsonObject data = p.getJsonObject ("data");
        
        db.update (getTable (), getData (p,
            "uuid", id,
            "id_ctr_status", 110,
            "reason_of_annulment", data.getString ("reason_of_annulment", null)
        ));
        
        final Model m = db.getModel();
        
        List<Map<String, Object>> idsRcContractObject = db.getList (m.select(RcContractObject.class, "uuid" )
            .where(RcContractObject.c.UUID_RC_CTR, id));
        
        List<Map<String, Object>> newIdsRcContractObject = new ArrayList<>();
        
        idsRcContractObject.forEach(rec -> {
            rec.put(RcContractObject.c.ID_CTR_STATUS.lc(), 110);
            newIdsRcContractObject.add(rec);
        });

        db.update (RcContractObject.class, newIdsRcContractObject);
        
    });}
    
    @Override
    public JsonObject doTerminate (String id, JsonObject p) {return doAction ((DB db) -> {
        
        JsonObject data = p.getJsonObject ("data");
        
        db.update (getTable (), getData (p,
            "uuid", id,
            "id_ctr_status", 100,
            "terminate", data.getString ("date_of_termination", null),    
            "reason_of_termination", data.getString ("reason_of_termination", null)
        ));
        
        final Model m = db.getModel();
        
        List<Map<String, Object>> idsRcContractObject = db.getList (m.select(RcContractObject.class, "uuid" )
            .where(RcContractObject.c.UUID_RC_CTR, id));
        
        List<Map<String, Object>> newIdsRcContractObject = new ArrayList<>();
        
        idsRcContractObject.forEach(rec -> {
            rec.put(RcContractObject.c.DT_TO.lc(), data.getString ("date_of_termination", null));
            newIdsRcContractObject.add(rec);
        });

        db.update (RcContractObject.class, newIdsRcContractObject);
        
    });}
}