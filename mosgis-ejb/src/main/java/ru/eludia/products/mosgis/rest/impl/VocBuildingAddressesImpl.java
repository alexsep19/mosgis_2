package ru.eludia.products.mosgis.rest.impl;

import java.util.UUID;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.voc.VocBuildingAddress;
import ru.eludia.products.mosgis.rest.api.VocBuildingAddressesLocal;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.web.base.Search;

@Stateless
public class VocBuildingAddressesImpl implements VocBuildingAddressesLocal {

    private static final Logger logger = Logger.getLogger (VocBuildingAddressesImpl.class.getName ());
        
    @Override
    public JsonObject select (JsonObject p) {
        
        Select select = ModelHolder.getModel ().select (VocBuildingAddress.class, "*", "uuid AS id")
            .orderBy ("address")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        final Search search = Search.from (p);

        if (search != null) select = search.filter (select, simple (search, "postalcode", "fiashouseguid", "address_uc LIKE %?%"));

        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {
            db.addJsonArrayCnt (jb, select);
        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();

    }
    
    private String simple (Search search, String i, String g, String s) {
    
        if (search == null) return s;
        
        String searchString = search.getSearchString ();
        
        if (searchString == null || searchString.isEmpty ()) return s;
        
        try {
            Integer.parseInt (searchString);
            return i;
        }
        catch (Exception e) {
            // do nothing
        }
        
        try {
            UUID.fromString (searchString);
            return g;
        }
        catch (Exception e) {
            // do nothing
        }
    
        return s;
        
    }
    
}