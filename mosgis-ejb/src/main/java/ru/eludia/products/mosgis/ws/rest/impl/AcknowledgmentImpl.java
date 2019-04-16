package ru.eludia.products.mosgis.ws.rest.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.db.model.tables.Acknowledgment;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.tables.Account;
import ru.eludia.products.mosgis.db.model.tables.PaymentDocument;
import ru.eludia.products.mosgis.db.model.tables.Payment;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.AcknowledgmentLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class AcknowledgmentImpl extends BaseCRUD<Acknowledgment> implements AcknowledgmentLocal {
    
    @Override
    public JsonObject select (JsonObject p, User user) {return EMPTY_JSON_OBJECT;}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {

        job.add ("item", db.getJsonObject (ModelHolder.getModel ()
            .get   (getTable (), id, "AS root", "*")
            .toOne (PaymentDocument.class, "AS pd", "*").on ()
            .toOne (Account.class, "AS acct", "*").on ()
            .toOne (Payment.class, "AS pay", "*").on ()
        ));
        
    });}

}