package ru.eludia.products.mosgis.rest.impl;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.tables.Entrance;
import ru.eludia.products.mosgis.db.model.tables.Lift;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.ValidationException;
import ru.eludia.products.mosgis.rest.api.LiftsLocal;
import ru.eludia.products.mosgis.rest.impl.base.BasePassport;
import ru.eludia.products.mosgis.web.base.Search;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)

public class LiftsImpl extends BasePassport<Lift> implements LiftsLocal {

    private static final Logger logger = Logger.getLogger (LiftsImpl.class.getName ());

    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, jb) -> {
        
        final JsonObject data = p.getJsonObject ("data");
                
        Select select = ModelHolder.getModel ()               
            .select (getTable (), "AS root","*", "uuid AS id")
            .where ("uuid_house", data.getString ("uuid_house"))
            .and   ("is_deleted",  0)
            .toOne (Entrance.class, "AS entrance", "entrancenum").on ()
            .orderBy ("entrance.entrancenum")
            .orderBy ("code_vc_nsi_192")
            .orderBy ("factorynum");
        
        final Search search = Search.from (p);

        if (search != null) select = search.filter (select, "");
        
        db.addJsonArrays (jb, select);

    });}

    @Override
    public JsonObject getItem (String id) {

        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {            
            jb.add ("item", db.getJsonObject (ModelHolder.getModel ().get (Lift.class, id, "*")));
        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();

    }
    
    @Override
    public JsonObject doCreate (JsonObject p, User user) {return doAction ((db) -> {
        
        db.insert (getTable (), getData (p));
        
    });}

    @Override
    public JsonObject doUpdate (String id, JsonObject p, User user) {return doAction ((db) -> {

        JsonObject data = p.getJsonObject ("data");

        final Table table = getTable ();

        Map<String, Object> record = table.HASH (data, "uuid", id);

        db.update (table, record);
        
        checkYear (db, id, data.getString (Lift.YEAR_FIELD, null));

    });}
    
    @Override
    public JsonObject doAdd (JsonObject p) {
        
        JsonObject data = p.getJsonObject ("data");
                
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        Map<String, Object> record = HASH (
            "uuid_entrance", data.getString ("uuid_entrance"),
            "code_vc_nsi_192", data.getString ("code_vc_nsi_192")
        );

        try (DB db = ModelHolder.getModel ().getDb ()) {
            db.insert (Lift.class, Collections.nCopies (data.getInt ("cnt"), record));
        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();

    }

    private void checkYear (final DB db, String id, Object year) throws SQLException {

        if (year == null) return;
        
        JsonObject entrance = db.getJsonObject (ModelHolder.getModel ()
            .get (Lift.class, id)
                .toOne (Entrance.class, "AS e", "entrancenum", "creationyear")
                .where ("creationyear > ", year)
            .on ()
        );

        if (entrance == null) return;

        throw new ValidationException (Lift.YEAR_FIELD, 
            "По имеющимся сведениям, подъезд №" + 
            entrance.getString ("e.entrancenum") + 
            ", в котором установлен данный лифт, введён в эксплуатацию в " + 
            entrance.getInt ("e.creationyear") + 
            " г. Операция отменена."
        );

    }    

}