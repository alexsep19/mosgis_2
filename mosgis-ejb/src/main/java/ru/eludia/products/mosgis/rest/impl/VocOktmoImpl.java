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

@Stateless
public class VocOktmoImpl implements VocOktmoLocal {

    private static final Logger logger = Logger.getLogger (VocOktmoImpl.class.getName ());
        
    @Override
    public JsonObject select (JsonObject p) {
        
        JsonObjectBuilder job = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {

            Select select = ModelHolder.getModel ().select (VocOktmo.class, "*")
                    .orderBy (VocOktmo.c.AREA_CODE.lc ())
                    .orderBy (VocOktmo.c.SETTLEMENT_CODE.lc ())
                    .orderBy (VocOktmo.c.LOCALITY_CODE.lc ())
                    .orderBy (VocOktmo.c.SECTION_CODE.lc ());
            db.addJsonArrayCnt (job, select);
            
        }
        catch (SQLException ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return job.build ();

    }

}