package ru.eludia.products.mosgis.ws.rest.impl;

import java.util.UUID;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.voc.VocUnom;
import ru.eludia.products.mosgis.db.model.voc.VocUnom.c;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocUnomStatus;
import ru.eludia.products.mosgis.rest.api.VocUnomLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.Base;
import ru.eludia.products.mosgis.ws.rest.impl.tools.Search;
import ru.eludia.products.mosgis.ws.rest.impl.tools.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class VocUnomImpl extends Base implements VocUnomLocal {

    private static final Logger logger = Logger.getLogger (VocUnomImpl.class.getName ());
    
    @Override
    public JsonObject select (JsonObject p) {return fetchData ((db, job) -> {
        
        Select select = db.getModel ()
            .select  (VocUnom.class, "AS root", "*", "unom AS id")
            .toMaybeOne (VocBuilding.class, "AS b", "label").on ()
//            .toMaybeOne (House.class, "AS h", "uuid", "is_condo").on ("root.fiashouseguid=h.fiashouseguid")
            .orderBy ("root.unom")
            .limit (p.getInt ("offset"), p.getInt ("limit"));
        
        final Search search = Search.from (p);

        if (search != null) {
            
            SimpleSearch simpleSearch = (SimpleSearch) search;
            
            String searchString = simpleSearch.getSearchString ();
            
            if (DB.ok (searchString)) {
                
                try {
                    select.and (c.UNOM, Long.parseLong (searchString));
                }
                catch (Exception ex) {}

                try {                
                    UUID guid = UUID.fromString (searchString);
                    select.and (c.FIASHOUSEGUID, guid);
                } 
                catch (Exception ex) {}

            }
                        
        }
                        
        db.addJsonArrayCnt (job, select);

    });}

    @Override
    public JsonObject getVocs () {
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        VocUnomStatus.addTo (jb);
        return jb.build ();
   }

}