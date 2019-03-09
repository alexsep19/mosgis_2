package ru.eludia.products.mosgis.ws.rest.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.tables.Account;
import ru.eludia.products.mosgis.db.model.tables.AccountItem;
import ru.eludia.products.mosgis.db.model.tables.Premise;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.AccountItemLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;

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
            .toOne (Account.class, "AS acc", "*").on ()
            .toMaybeOne (Premise.class, "AS prem", "label", Premise.c.TOTALAREA.lc ()).on ()                
            .toMaybeOne (VocOrganization.class, "AS org", "label").on ("acc.uuid_org_customer=org.uuid")
            .toMaybeOne (VocPerson.class,       "AS ind", "label").on ("acc.uuid_person_customer=ind.uuid")
            .where (EnTable.c.IS_DELETED, 0)
            .orderBy ("addr.label")
            .limit (p.getInt ("offset"), p.getInt ("limit"));
        
        JsonObject data = p.getJsonObject ("data");
        
        checkFilter (data, AccountItem.c.UUID_ACCOUNT, select);
        checkFilter (data, AccountItem.c.FIASHOUSEGUID, select);
        checkFilter (data, AccountItem.c.UUID_PREMISE, select);

        db.addJsonArrayCnt (job, select);

    });}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {

        job.add ("item", db.getJsonObject (ModelHolder.getModel ()
            .get (getTable (), id, "AS root", "*")
        ));
        
    });}
    
}