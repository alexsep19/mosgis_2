package ru.eludia.products.mosgis.rest.impl;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import javax.json.JsonObject;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.tables.BankAccount;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocBic;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.BankAccountLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class BankAccountImpl extends BaseCRUD<BankAccount> implements BankAccountLocal {

    @Resource (mappedName = "mosgis.inBankAccountsQueue")
    Queue queue;

    @Override
    public Queue getQueue () {
        return queue;
    }

    private void filterOffDeleted (Select select) {
        select.and ("is_deleted", 0);
    }
/*
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
*/    
    private void checkFilter (JsonObject data, BankAccount.c field, Select select) {
        String key = field.lc ();
        String value = data.getString (key, null);
        if (value == null) return;
        select.and (field, value);
    }

    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {                

        Select select = ModelHolder.getModel ().select (getTable (), "AS root", "*", "uuid AS id")
            .toMaybeOne (VocBic.class, "AS bank", "*").on ()
            .orderBy ("root.accountnumber")
            .limit (p.getInt ("offset"), p.getInt ("limit"));
        
        JsonObject data = p.getJsonObject ("data");
        
        checkFilter (data, BankAccount.c.UUID_ORG, select);
        
        filterOffDeleted (select);

//        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);

    });}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {

        job.add ("item", db.getJsonObject (ModelHolder.getModel ()
            .get (getTable (), id, "AS root", "*")
        ));
        
        VocGisStatus.addTo (job);
        VocAction.addTo (job);

    });}
/*
    @Override
    public JsonObject getVocs () {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        VocBankAccountType.addTo (jb);
        VocAction.addTo (jb);
        
        return jb.build ();
        
    }
*/        
}