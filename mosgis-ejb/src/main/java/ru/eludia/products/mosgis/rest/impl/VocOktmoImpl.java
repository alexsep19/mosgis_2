package ru.eludia.products.mosgis.rest.impl;

import java.sql.SQLException;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.voc.VocOktmo;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.api.VocOktmoLocal;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;

@Stateless
public class VocOktmoImpl implements VocOktmoLocal {

    private static final Logger logger = Logger.getLogger (VocOktmoImpl.class.getName ());
    
    private void applyComplexSearch (final ComplexSearch search, Select select) {
        search.filter (select, "");
    }
    
    private void applySimpleSearch (final SimpleSearch search, Select select) {

        final String searchString = search.getSearchString ();
        
        if (searchString == null || searchString.isEmpty ()) return;

        select.and ("code LIKE %?%", searchString.toUpperCase ());
        
    }
    
    private void applySearch (final Search search, Select select) {        

        if (search instanceof SimpleSearch) {
            applySimpleSearch  ((SimpleSearch) search, select);
        }
        else if (search instanceof ComplexSearch) {
            applyComplexSearch ((ComplexSearch) search, select);
        }

    }
    
    @Override
    public JsonObject select (JsonObject p) {
        
        JsonObjectBuilder job = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {

            Select select = ModelHolder.getModel ().select (VocOktmo.class, "*")
                    .orderBy (VocOktmo.c.AREA_CODE.lc ())
                    .orderBy (VocOktmo.c.SETTLEMENT_CODE.lc ())
                    .orderBy (VocOktmo.c.LOCALITY_CODE.lc ())
                    .orderBy (VocOktmo.c.SECTION_CODE.lc ());
            
            applySearch (Search.from (p), select);
            
            db.addJsonArrayCnt (job, select);
            
        }
        catch (SQLException ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return job.build ();

    }

}