package ru.eludia.products.mosgis.rest.impl;

import java.util.Map;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.Queue;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.ContractLog;
import ru.eludia.products.mosgis.db.model.tables.MgmtContract;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState;
import ru.eludia.products.mosgis.db.model.voc.VocContractDocType;
import ru.eludia.products.mosgis.db.model.voc.VocGisContractType;
import ru.eludia.products.mosgis.db.model.voc.VocGisCustomerType;
import static ru.eludia.products.mosgis.db.model.voc.VocGisCustomerType.i.OWNERS;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocGisCustomerTypeNsi58;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;
import ru.eludia.products.mosgis.rest.api.MgmtContractLocal;

@Stateless
public class MgmtContractImpl extends BaseCRUD<Contract> implements MgmtContractLocal {

    @Resource (mappedName = "mosgis.inHouseMgmtContractsQueue")
    Queue queue;

    @Override
    public Queue getQueue () {
        return queue;
    }

    @Override
    protected void publishMessage (String action, String id_log) {
        if ("approve".equals (action)) super.publishMessage (action, id_log);
    }

    private void filterOffDeleted (Select select) {
        select.and ("is_deleted", 0);
    }

    private void applyComplexSearch (final ComplexSearch search, Select select) {

        search.filter (select, "");
        
        if (!search.getFilters ().containsKey ("is_deleted")) filterOffDeleted (select);

    }

    private void applySimpleSearch (final SimpleSearch search, Select select) {

        filterOffDeleted (select);

        final String searchString = search.getSearchString ();
        
        if (searchString == null || searchString.isEmpty ()) return;

        select.and ("label_uc LIKE %?%", searchString.toUpperCase ());
        
    }
    
    private void applySearch (final Search search, Select select) {        

        if (search instanceof SimpleSearch) {
            applySimpleSearch  ((SimpleSearch) search, select);
        }
        else if (search instanceof ComplexSearch) {
            applyComplexSearch ((ComplexSearch) search, select);
        }
        else {
            filterOffDeleted (select);
        }

    }

    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {
        
        final MosGisModel model = ModelHolder.getModel ();

        Select select = model.select (MgmtContract.class, "AS root", "*", "uuid AS id")
            .toOne (VocOrganization.class, "AS org", "label").on ("uuid_org")
            .toMaybeOne (VocOrganization.class, "AS org_customer", "label").on ("uuid_org_customer")
            .toMaybeOne (ContractLog.class         ).on ()
            .toMaybeOne (OutSoap.class,           "err_text").on ()
            .and ("uuid_org", p.getJsonObject ("data").getString ("uuid_org", null))
            .orderBy ("org.label")
            .orderBy ("root.docnum")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);

    });}

    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {

        job.add ("item", db.getJsonObject (ModelHolder.getModel ()
            .get (MgmtContract.class, id, "*")
            .toOne      (VocOrganization.class,                    "label").on ("uuid_org")
            .toMaybeOne (VocOrganization.class, "AS org_customer", "label").on ("uuid_org_customer")
            .toMaybeOne (ContractLog.class                                ).on ()
            .toMaybeOne (OutSoap.class,                         "err_text").on ()
        ));

    });}

    @Override
    public JsonObject getVocs () {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        final MosGisModel model = ModelHolder.getModel ();

        try (DB db = model.getDb ()) {
            
            db.addJsonArrays (jb,
                    
                NsiTable.getNsiTable (db, 58).getVocSelect ()
                    .toMaybeOne (VocGisCustomerTypeNsi58.class, "AS it", "isdefault")
                        .when ("id", OWNERS.getId ())
                    .on ("vc_nsi_58.code=it.code")
                .where ("f_7d0f481f17", 1),
                    
                model
                    .select (VocOrganization.class, "uuid AS id", "label")                    
                    .orderBy ("label")
                    .and ("uuid", model.select (Contract.class, "uuid_org").where ("is_deleted", 0)),

                model
                    .select (VocAsyncEntityState.class, "id", "label")                    
                    .orderBy ("label"),
                
                model
                    .select (VocGisCustomerType.class, "id", "label")                    
                    .orderBy ("label"),
                
                model
                    .select (VocContractDocType.class, "id", "label")                    
                    .orderBy ("label"),

                model
                    .select (VocGisStatus.class, "id", "label")                    
                    .orderBy ("id")

            );

        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }

        return jb.build ();
        
    }
    
    @Override
    public JsonObject doCreate (JsonObject p, User user) {return fetchData ((db, job) -> {

        final Table table = getTable ();

        Map<String, Object> data = getData (p);

        data.put ("id_contract_type", VocGisContractType.i.MGMT.getId ());
        data.put (UUID_ORG, user.getUuidOrg ());

        Object insertId = db.insertId (table, data);

        logAction (db, user, insertId, "create");

        job.add ("id", insertId.toString ());

    });}

    @Override
    public JsonObject doApprove (String id, User user) {return doAction ((db) -> {

        db.update (getTable (), HASH (
            "uuid",           id,
            "id_ctr_status",  VocGisStatus.i.APPROVED.getId ()
        ));

        logAction (db, user, id, "approve");
        
    });}

}