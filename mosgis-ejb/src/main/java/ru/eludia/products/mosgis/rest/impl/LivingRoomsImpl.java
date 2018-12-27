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
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.tables.HouseFile;
import ru.eludia.products.mosgis.db.model.tables.Passport;
import ru.eludia.products.mosgis.db.model.tables.LivingRoom;
import ru.eludia.products.mosgis.db.model.tables.Block;
import ru.eludia.products.mosgis.db.model.tables.ResidentialPremise;
import ru.eludia.products.mosgis.db.model.tables.dyn.MultipleRefTable;
import ru.eludia.products.mosgis.db.model.voc.VocHouseStatus;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.impl.base.BasePassport;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.rest.api.LivingRoomsLocal;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class LivingRoomsImpl extends BasePassport<LivingRoom> implements LivingRoomsLocal {
    
    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {
        
        final Passport table = (Passport) getTable ();

        JsonObject item = db.getJsonObject (ModelHolder.getModel ()
            .get (table, id, "*")
            .toOne (House.class, "address").on ()
            .toMaybeOne (ResidentialPremise.class, "premisesnum").on ()
            .toMaybeOne (Block.class, "blocknum").on ()
        );

        job.add ("item", item);                

        db.addJsonArrays (job, 
                
            NsiTable.getNsiTable (330).getVocSelect (), // Причина аннулирования
            NsiTable.getNsiTable (273).getVocSelect () // Основания признания непригодности
                    
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

        JsonObject file = db.getJsonObject (ModelHolder.getModel ().select (HouseFile.class, "*").where ("uuid_living_room", id).and ("id_status", 1));
        
        if (file != null) job.add ("file", file);

    });}
    
    private static final String [] refNames = new String [] {"uuid_premise", "uuid_block", "uuid_house"};
    
    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {
        
        final JsonObject data = p.getJsonObject ("data");
        final Passport table  = (Passport) getTable ();        
        
        String uuidParent = null;
        String refName = null;
        
        for (String i: refNames) {
            refName = i;
            uuidParent = data.getString (refName, null);
            if (uuidParent != null) break;
        }
        
        if (uuidParent == null) throw new IllegalArgumentException  ("Broken param: " + p);
        
        Map<String, Map<String, List<String>>> f2idx = new HashMap <> ();
        
        for (MultipleRefTable refTable: table.getRefTables ()) {
            
            Map<String, List<String>> idx = new HashMap <> ();
            
            QP qp = new QP ("SELECT p.uuid, c.code FROM ");
            qp.append (table.getName ());
            qp.append (" p INNER JOIN ");
            qp.append (refTable.getName ());
            qp.append (" c ON p.uuid = c.");
            qp.append (refTable.getParentRefName ());
            qp.append (" WHERE ");
            qp.add (refName + " = ?", uuidParent, table.getColumn (refName).toPhysical ());
                        
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
            .where (refName, uuidParent)
            .where ("is_deleted", 0)
            .orderBy ("roomnumber");        
        
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

}