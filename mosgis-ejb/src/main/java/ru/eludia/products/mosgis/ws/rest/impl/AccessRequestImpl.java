package ru.eludia.products.mosgis.ws.rest.impl;

import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.tables.AccessRequest;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.AccessRequestLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.Base;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class AccessRequestImpl extends Base implements AccessRequestLocal {

    private static final Logger logger = Logger.getLogger (AccessRequestImpl.class.getName ());
    
    @Override
    public JsonObject select (JsonObject p, User u) {return fetchData ((db, job) -> {
        
        final String key = AccessRequest.c.ORGROOTENTITYGUID.lc ();
        
        Select select = db.getModel ()
            .select  (AccessRequest.class, "*")
            .where (key, p.getJsonObject ("data").getString (key))
            .orderBy (AccessRequest.c.STARTDATE.lc () + " DESC")
        ;
        
        db.addJsonArrays (job, select);

    });}

}