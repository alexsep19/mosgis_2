package ru.eludia.products.mosgis.rest.impl;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.PassportKind;
import static ru.eludia.products.mosgis.PassportKind.CONDO;
import static ru.eludia.products.mosgis.PassportKind.COTTAGE;
import ru.eludia.products.mosgis.rest.api.HousesLocal;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.Block;
import ru.eludia.products.mosgis.db.model.tables.Entrance;
import ru.eludia.products.mosgis.db.model.tables.Lift;
import ru.eludia.products.mosgis.db.model.tables.ResidentialPremise;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.tables.dyn.MultipleRefTable;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocBuildingAddress;
import ru.eludia.products.mosgis.db.model.voc.VocPassportFields;
import static ru.eludia.products.mosgis.db.model.voc.VocRdColType.i.REF;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.ValidationException;
import ru.eludia.products.mosgis.web.base.Search;

@Stateless
public class HousesImpl implements HousesLocal {

    private static final Logger logger = Logger.getLogger (HousesImpl.class.getName ());
        
    @Override
    public JsonObject select (JsonObject p) {
        
        Select select = ModelHolder.getModel ().select (House.class, "uuid AS id", "address", "is_condo", "fiashouseguid", "unom")
            .orderBy ("address")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        final Search search = Search.from (p);

        if (search != null) select = search.filter (select, simple (search, "unom", "fiashouseguid", "address_uc LIKE %?%"));

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

    private static final int NSI_VOC_CONDITION = 24;

    @Override
    public JsonObject getItem (String id) {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {
            
            JsonObject item = db.getJsonObject (ModelHolder.getModel ()
                    .get (House.class, id, "*")
                    .toMaybeOne (VocBuilding.class,        "AS fias",      "postalcode", "okato", "oktmo").on ()
                    .toMaybeOne (VocBuildingAddress.class, "AS fias_addr", "label").on ("fias.houseguid = fias_addr.houseguid")
            );
            
            jb.add ("item", item);
            
            if (item.getInt ("is_condo", 1) == 0) {
                
                if (null != db.getString (ModelHolder.getModel ()
                        
                    .select (Block.class, "uuid")
                    .where ("uuid_house", id)
                    .and ("is_deleted", 0))
                        
                ) jb.add ("has_blocks", 1);
                
            }
            else {
                
                if (null != db.getString (ModelHolder.getModel ()
                        
                    .select (ResidentialPremise.class, "uuid")
                    .where ("uuid_house", id)
                    .and ("is_deleted", 0)
                    .and ("code_vc_nsi_30", 2)) // коммуналка
                        
                ) jb.add ("has_shared_premises_res", 1);
                
            }
            
            db.addJsonArrays (jb, NsiTable.getNsiTable (NSI_VOC_CONDITION).getVocSelect ());
            
            House housesTable = new House ();
            housesTable.setModel (ModelHolder.getModel ());
            housesTable.addNsiFields (db);
            for (MultipleRefTable refTable: housesTable.getRefTables ()) {                
                
                JsonObjectBuilder ids = Json.createObjectBuilder ();
                
                db.forEach (
                    ModelHolder.getModel ().select (refTable, "code").where (refTable.getPk ().get (0).getName (), id), 
                    rs -> {ids.add (rs.getString (1), 1);}
                );
                
                jb.add (refTable.getName (), ids);
                
            }
            
        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();

    }

    @Override
    public JsonObject doUpdate (String id, JsonObject p) {

        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        JsonObject data = p.getJsonObject ("data");

        try (DB db = ModelHolder.getModel ().getDb ()) {
            
            final Map<String, Object> r = HASH (
                    
                "uuid",                  id,
                    
                "code_vc_nsi_24",        data.getString ("code_vc_nsi_24", null),
                    
                "culturalheritage",      data.getString ("culturalheritage", null),
                "floorcount",            data.getString ("floorcount", null),
                "kad_n",                 data.getString ("kad_n", null),
                "minfloorcount",         data.getString ("minfloorcount", null),
                "totalsquare",           data.getString ("totalsquare", null),
                "undergroundfloorcount", data.getString ("undergroundfloorcount", null),
                "usedyear",              data.getString ("usedyear", null),
                    
                "hasblocks",                      data.getInt ("hasblocks", 0),
                "hasmultiplehouseswithsameadres", data.getInt ("hasmultiplehouseswithsameadres", 0)
                    
            );
            
            db.begin ();            
                db.update (House.class, r);                
                checkYear (db, id, r.get ("usedyear"));            
            db.commit ();

        }
        catch (ValidationException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();

    }
    
    private void checkYear (final DB db, String id, Object year) throws SQLException {
        if (year == null) return;
        checkEntranceYear (db, id, year);
        checkLiftYear (db, id, year);
    }
    
    private void checkEntranceYear (final DB db, String id, Object year) throws SQLException {

        JsonObject entrance = db.getJsonObject (ModelHolder.getModel ().select (Entrance.class, "entrancenum", "creationyear")
            .and ("is_deleted",      0)
            .and ("uuid_house",      id)
            .and ("creationyear < ", year)
        );

        if (entrance == null) return;

        throw new ValidationException ("usedyear", 
            "По имеющимся сведениям, подъезд №" + 
            entrance.getString ("entrancenum") + 
            " построен в " + 
            entrance.getInt ("creationyear") + 
            "г. Операция отменена."
        );

    }    

    private void checkLiftYear (final DB db, String id, Object year) throws SQLException {

        JsonObject lift = db.getJsonObject (ModelHolder.getModel ().select (Lift.class, "factorynum", Lift.YEAR_FIELD)
            .and ("is_deleted",            0)
            .and ("uuid_house",            id)
            .and (Lift.YEAR_FIELD + " < ", year)
        );

        if (lift == null) return;

        throw new ValidationException ("usedyear", 
            "По имеющимся сведениям, лифт №" + 
            lift.getString ("factorynum") + 
            " введён в эксплуатацию в " + 
            lift.getInt (Lift.YEAR_FIELD) + 
            "г. Операция отменена."
        );

    }    

    @Override
    public JsonObject getVocPassportFields (String id, Integer[] ids) {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {
                        
            PassportKind kind = db.getInteger (House.class, id, "is_condo") == 1 ? CONDO : COTTAGE;

            Map<Integer, JsonObject> id2o = new HashMap <> (ids.length);

            db.forEach (ModelHolder.getModel ()
                .select (VocPassportFields.class, "*")
                .where  ("id IN", ids)
                .and    (kind.getFilterFieldName (), 1), 
                rs -> {
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
                    db.addJsonArrays (jb, NsiTable.getNsiTable (i.getInt ("voc")).getVocSelect ());
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

    @Override
    public JsonObject doPatch (String id, JsonObject p) {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        JsonObject data = p.getJsonObject ("data");

        try (DB db = ModelHolder.getModel ().getDb ()) {
            
            final String k = data.getString ("k");
            final String v = data.getString ("v", null);
        
            db.begin ();            
            
                db.update (House.class, HASH (
                    "uuid", id, 
                    k, v)
                );
                
            db.commit ();

        }
        catch (ValidationException ex) {
            throw ex;
        }        
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();
        
    }

    @Override
    public JsonObject doSetMultiple (String id, JsonObject p) {
    
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        JsonObject data = p.getJsonObject ("data");

        try (DB db = ModelHolder.getModel ().getDb ()) {
            
            MultipleRefTable refTable = new MultipleRefTable (ModelHolder.getModel ().get (House.class), data.getString ("code"), "");
            
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
            
        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();
        
    }
    
}