package ru.eludia.products.mosgis.rest.impl.base;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.Passport;
import ru.eludia.products.mosgis.db.model.tables.dyn.MultipleRefTable;
import ru.eludia.products.mosgis.db.model.voc.VocPassportFields;
import static ru.eludia.products.mosgis.db.model.voc.VocRdColType.i.REF;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.base.PassportBackend;

public abstract class BasePassport <T extends Table> extends BaseCRUD<T> implements PassportBackend {
            
    public void setMultipleFields (final Passport table, DB db, String id, JsonObject p) throws SQLException {
        
        JsonObject data = p.getJsonObject ("data");
        
        for (MultipleRefTable i: table.getRefTables ()) {
            
            String fieldName = i.getFieldName ();
            
            if (!data.containsKey (fieldName)) continue;
            
            db.dupsert (i, HASH (i.getPk ().get (0).getName (), id), 
                    
                data.getJsonArray (fieldName).stream ().map (jn -> HASH (
                    "code", ((JsonString) jn).getString ()
                )).collect (Collectors.toList ())                    
                    
                , "code"
            
            );
            
        }
        
    }

    @Override
    public JsonObject getVocPassportFields (String id, Integer[] ids) {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {

            Map<Integer, JsonObject> id2o = new HashMap <> (ids.length);

            db.forEach (ModelHolder.getModel ()
                .select (VocPassportFields.class, "*")
                .where  ("id IN", ids)
//                .and    (PassportKind.PREMISE_NRS.getFilterFieldName (), 1), 
                , rs -> {
                    JsonObject o = db.getJsonObject (rs);
                    id2o.put (Integer.parseInt (o.getString ("id")), o);
                }
            );
            
            JsonArrayBuilder vc_pass_fields = Json.createArrayBuilder ();
            for (Integer i: ids) {
                final JsonObject o = id2o.get (i);
                if (o != null) vc_pass_fields.add (o);
            }
                        
            jb.add ("vc_pass_fields", vc_pass_fields);

            for (JsonObject i: id2o.values ()) {
                
                if (i.getInt ("id_type", -1) != REF.getId ()) continue;
                
                try {
                    db.addJsonArrays (jb, NsiTable.getNsiTable (db, i.getInt ("voc")).getVocSelect ());
                } 
                catch (Exception ex) {
                    logger.log (Level.SEVERE, "Cannot fetch vocabulary for " + i, ex);
                }                                                

            }

        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();
        
    }  
    
    private Passport getPassport () throws SQLException {
        return (Passport) getTable ();
    }

    @Override
    public JsonObject doCreate (JsonObject p, User user) {return doAction ((db) -> {
        
        final Passport table = getPassport ();

        String id = db.insertId (table, getData (p)).toString ();
        
        setMultipleFields (table, db, id, p);

    });}

    @Override
    public JsonObject doUpdate (String id, JsonObject p, User user) {return doAction ((db) -> {
        
        final Passport table = getPassport ();
                
        db.update (table, getData (p,
            "uuid", id
        ));
        
        setMultipleFields (table, db, id, p);
                
    });}

    @Override
    public JsonObject doSetMultiple (String id, JsonObject p) {return doAction ((db) -> {
        
        JsonObject data = p.getJsonObject ("data");

        MultipleRefTable refTable = new MultipleRefTable (getTable (), data.getString ("code"), "");
            
        refTable.setModel (ModelHolder.getModel ());
            
        db.adjustTable (refTable);
            
        db.dupsert (refTable, 
                    
            HASH ("uuid", id), 
                
            data.getJsonArray ("ids")
                .getValuesAs (JsonNumber.class)
                .stream ()
                .map (i -> HASH ("code", i.intValue ()))                    
                .collect (Collectors.toList ())
            ,
                    
            "code"
            
        );
        
    });}
    
}