package ru.eludia.products.mosgis.ws.rest.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.tables.ChargeInfo;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.tables.AnyChargeInfo;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.ChargeInfoLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ChargeInfoImpl extends BaseCRUD<ChargeInfo> implements ChargeInfoLocal {
    
    private void checkFilter (JsonObject data, ChargeInfo.c field, Select select) {
        String key = field.lc ();
        String value = data.getString (key, null);
        if (value == null) return;
        select.and (field, value);
    }

    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {                

        Select select = ModelHolder.getModel ().select (AnyChargeInfo.class, "AS root", "*")
            .orderBy ("root.label")
        ;
        
        JsonObject data = p.getJsonObject ("data");
        
        checkFilter (data, ChargeInfo.c.UUID_PAY_DOC, select);

        db.addJsonArrays (job, select);

    });}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {

        job.add ("item", db.getJsonObject (ModelHolder.getModel ()
            .get (getTable (), id, "AS root", "*")
        ));
        
    });}

}