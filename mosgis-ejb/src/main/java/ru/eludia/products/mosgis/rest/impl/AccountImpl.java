package ru.eludia.products.mosgis.rest.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.Account;
import ru.eludia.products.mosgis.db.model.tables.AccountLog;
import ru.eludia.products.mosgis.db.model.tables.Charter;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.voc.VocAccountType;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.AccountLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class AccountImpl extends BaseCRUD<Account> implements AccountLocal {
/*
    @Resource (mappedName = "mosgis.inNsiAccountsQueue")
    Queue queue;

    @Override
    public Queue getQueue () {
        return queue;
    }
*/
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

        select.and ("accountnumber LIKE ?%", searchString.toUpperCase ());
        
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
    
    private void checkFilter (JsonObject data, Account.c field, Select select) {
        String key = field.lc ();
        String value = data.getString (key, null);
        if (value == null) return;
        select.and (field, value);
    }

    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {                

        Select select = ModelHolder.getModel ().select (getTable (), "AS root", "*", "uuid AS id")
            .toMaybeOne (AccountLog.class               ).on ()
            .toMaybeOne (OutSoap.class,       "err_text").on ()
            .toMaybeOne (VocOrganization.class, "AS org", "label").on ("root.uuid_org_customer=org.uuid")
            .toMaybeOne (VocPerson.class,       "AS ind", "label").on ("root.uuid_person_customer=ind.uuid")
            .orderBy ("root.accountnumber")
            .limit (p.getInt ("offset"), p.getInt ("limit"));
        
        JsonObject data = p.getJsonObject ("data");
        
        checkFilter (data, Account.c.UUID_CONTRACT, select);
        checkFilter (data, Account.c.UUID_CHARTER, select);

        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);

    });}

    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {

        job.add ("item", db.getJsonObject (ModelHolder.getModel ()
            .get (getTable (), id, "AS root", "*")
            .toMaybeOne (Contract.class, "AS ca", "*").on ()
            .toMaybeOne (Charter.class, "AS ch", "*").on ()
            .toMaybeOne (AccountLog.class, "AS log").on ()
            .toMaybeOne (OutSoap.class, "err_text").on ("log.uuid_out_soap=out_soap.uuid")
            .toOne (VocOrganization.class, "AS org", "label").on ("root.uuid_org=org.uuid")
            .toMaybeOne (VocOrganization.class, "AS org_customer", "label").on ("root.uuid_org_customer=org_customer.uuid")
            .toMaybeOne (VocPerson.class,       "AS ind_customer", "label").on ("root.uuid_person_customer=ind_customer.uuid")
        ));
        
        VocAccountType.addTo (job);
        VocGisStatus.addTo (job);
        VocAction.addTo (job);

    });}

    @Override
    public JsonObject getVocs () {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        VocAccountType.addTo (jb);
        VocAction.addTo (jb);
        
/*        
        
        try (DB db = ModelHolder.getModel ().getDb ()) {
            
            db.addJsonArrays (jb,
                    
                ModelHolder.getModel ()
                    .select (VocOkei.class, "code AS id", "national AS label")
                    .orderBy ("national"),
                    
                ModelHolder.getModel ()
                    .select (VocOrganization.class, "uuid AS id", "label")                    
                    .orderBy ("label")
                    .and ("uuid", ModelHolder.getModel ().select (Account.class, "uuid_org").where ("is_deleted", 0)),

                ModelHolder.getModel ()
                    .select (VocAsyncEntityState.class, "id", "label")                    
                    .orderBy ("label")

            );

        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }
*/
        return jb.build ();
        
    }
    
}