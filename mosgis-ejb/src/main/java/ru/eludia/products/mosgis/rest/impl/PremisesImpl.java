package ru.eludia.products.mosgis.rest.impl;

import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.json.JsonObject;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.tables.Premise;
import ru.eludia.products.mosgis.rest.api.PremisesLocal;
import ru.eludia.products.mosgis.rest.impl.base.Base;

@Stateless
public class PremisesImpl extends Base implements PremisesLocal {

    private static final Logger logger = Logger.getLogger (PremisesImpl.class.getName ());

    @Override
    public JsonObject select (JsonObject p) {return fetchData ((db, job) -> {

        Select select = db.getModel ()
            .select  (Premise.class, "*")                
            .and ("uuid_house", p.getJsonObject ("data").getString ("uuid_house"))
            .orderBy ("label")
//            .limit (p.getInt ("offset"), p.getInt ("limit"))
            ;

        db.addJsonArrays (job, select);

    });}

}