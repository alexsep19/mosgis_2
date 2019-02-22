package ru.eludia.products.mosgis.rest.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.db.sql.build.QP;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.tables.Passport;
import ru.eludia.products.mosgis.db.model.tables.NonResidentialPremise;
import ru.eludia.products.mosgis.db.model.tables.dyn.MultipleRefTable;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocHouseStatus;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.NonResidentialPremisesLocal;
import ru.eludia.products.mosgis.rest.impl.base.BasePassport;
import ru.eludia.products.mosgis.web.base.Search;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class NonResidentialPremisesImpl extends BasePassport<NonResidentialPremise> implements NonResidentialPremisesLocal {
    
    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {
        
        final Passport table = (Passport) getTable ();

        JsonObject item = db.getJsonObject (ModelHolder.getModel ()
            .get (table, id, "*")
            .toOne (House.class, "address", "fiashouseguid").on ()
        );

        job.add ("item", item);                

        VocBuilding.addCaCh (db, job, item.getString ("tb_houses.fiashouseguid"));

        db.addJsonArrays (job, 
            NsiTable.getNsiTable (17).getVocSelect (),
            NsiTable.getNsiTable (330).getVocSelect ()
        );
        
        VocHouseStatus.addTo(job);
        
        for (MultipleRefTable refTable: table.getRefTables ()) {                
                
            JsonObjectBuilder ids = Json.createObjectBuilder ();
                
            db.forEach (
                ModelHolder.getModel ().select (refTable, "code").where (refTable.getPk ().get (0).getName (), id), 
                rs -> {ids.add (rs.getString (1), 1);}
            );
                
            job.add (refTable.getName (), ids);
                
        }        

    });}
    
    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {
        
        final JsonObject data = p.getJsonObject ("data");
        final Passport table  = (Passport) getTable ();        
        final String uuidHouse = data.getString ("uuid_house");
        
        Map<String, Map<String, List<String>>> f2idx = new HashMap <> ();
        
        for (MultipleRefTable refTable: table.getRefTables ()) {
            
            Map<String, List<String>> idx = new HashMap <> ();
            
            QP qp = new QP ("SELECT p.uuid, c.code FROM ");
            qp.append (table.getName ());
            qp.append (" p INNER JOIN ");
            qp.append (refTable.getName ());
            qp.append (" c ON p.uuid = c.");
            qp.append (refTable.getParentRefName ());
            qp.add (" WHERE p.uuid_house = ?", uuidHouse, table.getColumn ("uuid_house").toPhysical ());
            
            db.forEach (qp,
                rs -> {
                    String uuid = rs.getString (1);
                    if (!idx.containsKey (uuid)) idx.put (uuid, new ArrayList <>());
                    idx.get (uuid).add (rs.getString (2));
                }
            );
            
            f2idx.put (refTable.getFieldName (), idx);
            
        }
        
        JsonArrayBuilder a = Json.createArrayBuilder ();
        
        Select select = ModelHolder.getModel ()                
                .select (table, "*", "uuid AS id")
                .where ("uuid_house", uuidHouse)
                .where ("is_deleted", 0)
                .orderBy ("premisesnum");
        
        final Search search = Search.from (p);

        if (search != null) select = search.filter (select, "");
        
        db.forEach (select
                
        , rs -> {
            
            JsonObjectBuilder o = db.getJsonObjectBuilder (rs);
            
            String id = rs.getString ("id");
            
            f2idx.entrySet ().forEach (kv -> {
                
                JsonArrayBuilder aa = Json.createArrayBuilder ();
                
                List<String> ids = kv.getValue ().get (id);
                
                if (ids != null) for (String s: ids) aa.add (s);
                
                o.add (kv.getKey (), aa);
                
            });
            
            a.add (o);
            
        });

        job.add (table.getName (), a);
        
    });}

    @Override
    public JsonObject doRestore (String id) {return doAction ((db) -> {

        final Table table = getTable ();

        Map<String, Object> record = db.getMap(ModelHolder.getModel ()
                                        .get(table, id, "*")
        );
        
        record.put ("terminationdate", null);
        record.put ("annulmentinfo", null);
        record.put ("code_vc_nsi_330", null);
        
        record.remove ("uuid");
        record.remove ("annulmentreason");
        record.remove ("is_annuled");
        record.remove ("premisesguid");
        record.remove ("is_annuled_in_gis");
        record.remove ("id_status");
        
        db.insert (table, record);
        
    });}    

}