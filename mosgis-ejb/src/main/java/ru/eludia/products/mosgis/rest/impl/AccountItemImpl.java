package ru.eludia.products.mosgis.rest.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.tables.AccountItem;
import ru.eludia.products.mosgis.db.model.tables.Premise;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.AccountItemLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class AccountItemImpl extends BaseCRUD<AccountItem> implements AccountItemLocal {

    private void checkFilter (JsonObject data, AccountItem.c field, Select select) {
        String key = field.lc ();
        String value = data.getString (key, null);
        if (value == null) return;
        select.and (field, value);
    }

    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {                

        Select select = ModelHolder.getModel ().select (getTable (), "AS root", "*", "uuid AS id")
//            .toMaybeOne (AccountItemLog.class               ).on ()
            .toOne (VocBuilding.class, "AS addr", "label").on ()
            .toMaybeOne (Premise.class, "AS prem", "label", Premise.c.TOTALAREA.lc ()).on ()                
            .where (EnTable.c.IS_DELETED, 0)
            .orderBy ("addr.label")
            .limit (p.getInt ("offset"), p.getInt ("limit"));
        
        JsonObject data = p.getJsonObject ("data");
        
        checkFilter (data, AccountItem.c.UUID_ACCOUNT, select);

        db.addJsonArrayCnt (job, select);

    });}

    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {

        job.add ("item", db.getJsonObject (ModelHolder.getModel ()
            .get (getTable (), id, "AS root", "*")
        ));
        
    });}
    
}