package ru.eludia.products.mosgis.rest.impl;

import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.json.JsonObject;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.rest.api.ContractObjectsLocal;
import ru.eludia.products.mosgis.db.model.tables.ContractFile;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;

@Stateless
public class ContractObjectsImpl extends BaseCRUD<ContractFile> implements ContractObjectsLocal  {

    private static final Logger logger = Logger.getLogger (ContractObjectsImpl.class.getName ());    

    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {

        job.add ("item", db.getJsonObject (db.getModel ()
            .get (getTable (), id, "*")
        ));

    });}

    @Override
    public JsonObject select (JsonObject p, User u) {return fetchData ((db, job) -> {
        
        ComplexSearch s = new ComplexSearch (p.getJsonArray ("search"));
        
        if (!s.getFilters ().containsKey ("uuid_contract")) throw new IllegalStateException ("uuid_contract filter is not set");
                
        Select select = db.getModel ()
            .select (getTable (), "*", "uuid AS id")
            .where  ("id_status",  1)
            .orderBy ("uuid")
            .limit (p.getInt ("offset"), p.getInt ("limit"));
        
        db.addJsonArrays (job, s.filter (select, ""));

    });}

}