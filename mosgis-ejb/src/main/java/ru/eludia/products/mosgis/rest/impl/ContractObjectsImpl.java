package ru.eludia.products.mosgis.rest.impl;

import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.json.JsonObject;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.tables.ContractFile;
import ru.eludia.products.mosgis.rest.api.ContractObjectsLocal;
import ru.eludia.products.mosgis.db.model.tables.ContractObject;
import ru.eludia.products.mosgis.db.model.voc.VocBuildingAddress;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;

@Stateless
public class ContractObjectsImpl extends BaseCRUD<ContractObject> implements ContractObjectsLocal  {

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
            .select     (getTable (),              "AS root", "*", "uuid AS id")
            .toOne      (VocBuildingAddress.class, "AS fias",      "label"                                       ).on ("root.fiashouseguid=fias.houseguid")
            .toMaybeOne (ContractFile.class,       "AS agreement", "agreementnumber AS no", "agreementdate AS dt").on ()
            .where      ("is_deleted", 0)
            .orderBy    ("fias.label")
            .limit      (p.getInt ("offset"), p.getInt ("limit"));
        
        db.addJsonArrays (job, s.filter (select, ""));

    });}

}