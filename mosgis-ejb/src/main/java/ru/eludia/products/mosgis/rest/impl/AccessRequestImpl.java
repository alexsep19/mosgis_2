package ru.eludia.products.mosgis.rest.impl;

import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.ejb.Stateless;
import javax.json.JsonObject;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.tables.AccessRequest;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.AccessRequestLocal;
import ru.eludia.products.mosgis.rest.impl.base.Base;

@Stateless
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