package ru.eludia.products.mosgis.rest.impl;

import java.sql.SQLException;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.rest.api.VcRd1Local;
import ru.eludia.products.mosgis.db.model.rd.RdTable;
import ru.eludia.products.mosgis.db.model.voc.VocRd1;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;
import ru.eludia.products.mosgis.web.base.ComplexSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class VcRd1Impl implements VcRd1Local {

    private static final Logger logger = Logger.getLogger (VcRd1Impl.class.getName ());

    public JsonObject select (JsonObject p) {

        final Select select = ModelHolder.getModel ()
            .select (VocRd1.class, "*")
            .orderBy ("address")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        final Search search = Search.from (p);

        if (search != null) {
                        
            if (search instanceof SimpleSearch) {
                
                String searchString = search.getSearchString ();                

                try {
                    Integer.parseInt (searchString);
                    select.and ("unom", searchString);
                }
                catch (Exception e) {
                    select.and ("address_uc LIKE %?%", searchString.toUpperCase ().replaceAll (" ", "%"));
                }
                
            }
            else {

                ((ComplexSearch) search).apply (select);

            }            

        }

        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {
            db.addJsonArrayCnt (jb, select);
        }
        catch (SQLException ex) {
            throw new InternalServerErrorException (ex);
        }

        return jb.build ();

    }
    
    public JsonObject getVocs () {

        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        try (DB db = ModelHolder.getModel ().getDb ()) {
            db.addJsonArrays (jb, 
                ModelHolder.getModel ().select (new RdTable (db, 1240), "id", "name AS label").orderBy ("name"),
                ModelHolder.getModel ().select (new RdTable (db, 1540), "id", "name AS label").orderBy ("name")
            );
        }
        catch (SQLException ex) {
            throw new InternalServerErrorException (ex);
        }

        return jb.build ();

    }

}