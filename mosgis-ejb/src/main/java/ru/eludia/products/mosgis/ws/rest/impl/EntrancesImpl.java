package ru.eludia.products.mosgis.ws.rest.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.dialect.ANSI;
import ru.eludia.base.db.sql.build.QP;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.Entrance;
import ru.eludia.products.mosgis.db.model.tables.Lift;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.EntrancesLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.BasePassport;
import ru.eludia.products.mosgis.ws.rest.impl.tools.Search;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class EntrancesImpl extends BasePassport<Entrance> implements EntrancesLocal {
    
    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {

        final JsonObject data = p.getJsonObject ("data");
        final String uuidHouse = data.getString ("uuid_house");

        String [] cols = new String [] {
            "uuid_entrance", 
            "code_vc_nsi_192"
        };
                    
        QP qp = new QP ("SELECT ");
        ((ANSI) db).addQuotedList (qp, cols);
        qp.append (",COUNT(*) cnt FROM (");
            qp.add (db.toQP (ModelHolder.getModel ().select (
                Lift.class, cols)
                    .and ("uuid_house", uuidHouse)
                    .and ("is_deleted", 0)
            ));
        qp.append (") t GROUP BY ");
        ((ANSI) db).addQuotedList (qp, cols);
        
        Map<String, Map<String, Integer>> e2c2cnt = new HashMap <> ();

        db.forEach (qp, rs -> {                
            String e = rs.getString (1);                
            if (!e2c2cnt.containsKey (e)) e2c2cnt.put (e, new HashMap<> ());                
            ((Map) e2c2cnt.get (e)).put (rs.getString (2), rs.getInt (3));                
        });

        JsonArrayBuilder ab = Json.createArrayBuilder ();
        
        Select select = ModelHolder.getModel ()
            .select (Entrance.class, "uuid AS id", 
				     "uuid_org",
                                     "entrancenum", 
                                     "storeyscount", 
                                     "creationyear", 
                                     "terminationdate", 
                                     "is_annuled",
                                     "is_annuled_in_gis",
                                     "id_status", 
                                     "code_vc_nsi_330",
                                     "annulmentinfo")
            .where ("uuid_house", uuidHouse)
            .and   ("is_deleted",  0)
            .orderBy ("entrancenum");

        final Search search = Search.from (p);

        if (search != null) select = search.filter (select, "");

        db.forEach (select, rs -> {

            JsonObjectBuilder jb = db.getJsonObjectBuilder (rs);

            Map<String, Integer> c2cnt = e2c2cnt.get (rs.getString ("id"));

            if (c2cnt != null) c2cnt.entrySet ().forEach (kv -> {
                jb.add ("cnt_" + kv.getKey (), kv.getValue ());
            });

            ab.add (jb);

        });

        job.add (ModelHolder.getModel ().getName (Entrance.class), ab);

    });}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {
        
        job.add ("item", db.getJsonObject (ModelHolder.getModel ().get (getTable (), id, "*")));
        
        db.addJsonArrays (job, 
            NsiTable.getNsiTable (330).getVocSelect ()
        );
        
    });}

    @Override
    public JsonObject doCreate (JsonObject p, User user) {return doAction ((db) -> {

        JsonObject data = p.getJsonObject ("data");

        String uuidHouse = data.getString ("uuid_house");

        db.insert (getTable (), data.getJsonArray ("nos").getValuesAs (JsonString.class).stream ().map (no -> HASH (
            "uuid_house",  uuidHouse,
            "entrancenum", no.getString (),
	    "uuid_org", user.getUuidOrg ()
        )).collect (Collectors.toList ()));

    });}

    @Override
    public JsonObject doDelete (String id, User user) {return doAction ((db) -> {

        db.update (getTable (), HASH (
            "uuid",        id,
            "is_deleted",  1
        ));

    });}
    
    @Override
    public JsonObject doUpdate (String id, JsonObject p, User user) {return doAction ((db) -> {

        JsonObject data = p.getJsonObject ("data");

        final Table table = getTable ();

        Map<String, Object> record = table.HASH (data, "uuid", id);

        db.update (table, record);

    });}
    
    @Override
    public JsonObject doRestore (String id) {return doAction ((db) -> {

        final Table table = getTable ();

        Map<String, Object> record = db.getMap(ModelHolder.getModel ()
                                        .get(table, id, "uuid_house", 
                                                        "entrancenum",
                                                        "storeyscount",
                                                        "creationyear",
                                                        "fiaschildhouseguid",
                                                        "informationconfirmed",
                                                        "gis_modification_date")
        );
        
        record.put ("terminationdate", null);
        record.put ("annulmentinfo", null);
        record.put ("code_vc_nsi_330", null);
        
        db.insert (table, record);
        
    });}

}