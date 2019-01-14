package ru.eludia.products.mosgis.rest.impl;

import java.util.UUID;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.voc.VocUnom;
import ru.eludia.products.mosgis.db.model.voc.VocUnom.c;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.rest.api.VocUnomLocal;
import ru.eludia.products.mosgis.rest.impl.base.Base;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class VocUnomImpl extends Base implements VocUnomLocal {

    private static final Logger logger = Logger.getLogger (VocUnomImpl.class.getName ());
    
    @Override
    public JsonObject select (JsonObject p) {return fetchData ((db, job) -> {
        
        Select select = db.getModel ()
            .select  (VocUnom.class, "AS root", "unom AS id", "*")
            .toMaybeOne (VocBuilding.class, "AS b", "label").on ()
            .toMaybeOne (House.class, "AS h", "uuid", "is_condo").on ("root.fiashouseguid=h.fiashouseguid")
            .orderBy ("unom")
            .limit (0, 50);
        
        String search = p.getString ("search", "");
        
        if (DB.ok (search)) {
            
            long unom = DB.to.Long (search);
            
            if (unom > 0) {
                select.and (c.UNOM, unom);
            }
            else {
                try {                
                    UUID guid = UUID.fromString (search);
                    select.and (c.FIASHOUSEGUID, guid);
                } 
                catch (Exception ex) {}

            }             
            
        }
        
        db.addJsonArrays (job, select);

    });}

}