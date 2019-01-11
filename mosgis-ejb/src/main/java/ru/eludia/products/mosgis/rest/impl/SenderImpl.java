package ru.eludia.products.mosgis.rest.impl;

import java.util.UUID;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.tables.Sender;
import ru.eludia.products.mosgis.db.model.tables.Sender.c;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocUser;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.SenderLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class SenderImpl extends BaseCRUD<Sender> implements SenderLocal {

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
*/
    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {

        Select select = ModelHolder.getModel ().select (getTable (), "*", "uuid AS id")
            .orderBy (c.LABEL)
            .limit (p.getInt ("offset"), p.getInt ("limit"));
        
        filterOffDeleted (select);

//        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);

    });}

    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {

        job.add ("item", db.getJsonObject (ModelHolder.getModel ()
            .get (getTable (), id, "*")
        ));

    });}
    
    @Override
    public JsonObject doSetPassword (String id, String password, User user) {return doAction ((db) -> {
        
        UUID salt = UUID.randomUUID ();
        
        db.update (getTable (), HASH (
            EnTable.c.UUID,   id,
            c.SALT,   salt,
            c.SHA1,   VocUser.encrypt (salt, password)
        ));
        
        logAction (db, user, id, VocAction.i.SET_PASSWORD);
        
    });}

    @Override
    public JsonObject doLock (String id, String password, User user) {return doAction ((db) -> {
        
        UUID salt = UUID.randomUUID ();
        
        db.update (getTable (), HASH (
            EnTable.c.UUID,   id,
            c.IS_LOCKED,      1
        ));
        
        logAction (db, user, id, VocAction.i.LOCK);
        
    });}
    
    @Override
    public JsonObject doUnlock (String id, String password, User user) {return doAction ((db) -> {
        
        UUID salt = UUID.randomUUID ();
        
        db.update (getTable (), HASH (
            EnTable.c.UUID,   id,
            c.IS_LOCKED,      0
        ));
        
        logAction (db, user, id, VocAction.i.UNLOCK);
        
    });}
    
}