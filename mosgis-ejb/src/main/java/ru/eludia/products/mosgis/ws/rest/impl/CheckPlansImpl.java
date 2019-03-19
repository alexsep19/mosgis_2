package ru.eludia.products.mosgis.ws.rest.impl;

import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;

import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.incoming.InInspectionPlans;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.CheckPlan;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.CheckPlansLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.ws.rest.impl.tools.ComplexSearch;
import ru.eludia.products.mosgis.ws.rest.impl.tools.Search;
import ru.eludia.products.mosgis.ws.rest.impl.tools.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class CheckPlansImpl extends BaseCRUD<CheckPlan> implements CheckPlansLocal {

	@Resource (mappedName = "mosgis.inExportInspectionPlansQueue")
    private Queue inExportInspectionPlansQueue;
	
	private void filterOffDeleted (Select select) {
        select.and ("is_deleted", 0);
    }
    
    private void applyComplexSearch (final ComplexSearch search, Select select) {

        search.filter (select, "");
        
        if (!search.getFilters ().containsKey ("is_deleted")) filterOffDeleted (select);

    }
    
    private void applySimpleSearch (final SimpleSearch search, Select select) {

        filterOffDeleted (select);

        final String searchString = search.getSearchString ();
        
        if (searchString == null || searchString.isEmpty ()) return;

        select.and ("uriregistrationplannumber LIKE %?%", searchString);
        
    }
    
    private void applySearch (final Search search, Select select) {        

        if (search instanceof SimpleSearch) {
            applySimpleSearch  ((SimpleSearch) search, select);
        }
        else if (search instanceof ComplexSearch) {
            applyComplexSearch ((ComplexSearch) search, select);
        }
        else {
            filterOffDeleted (select);
        }

    }
    
    @Override
    public JsonObject doUpdate (String id, JsonObject p, User user) {return doAction ((db) -> {
        
        Map <String, Object> data = getData (p, "uuid", id);
        
        if ("1".equals (data.get ("shouldnotberegistered").toString ())) 
            data.put ("uriregistrationplannumber", null);
        
        db.update (getTable (), data);
        
        logAction (db, user, id, VocAction.i.UPDATE);
                        
    });}
    
    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {
        
        Select select = ModelHolder.getModel ().select(getTable (), "AS root", "*", "uuid AS id");
        
        applySearch (Search.from (p), select);
        
        db.addJsonArrayCnt (job, select);
        
    });}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {
        
        JsonObject item = db.getJsonObject(ModelHolder.getModel ().get (CheckPlan.class, id, "AS root", "*"));
        
        job.add ("item", item);
        
    });}
    
    @Override
    public JsonObject getVocs () {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        VocAction.addTo (jb);
        
        final MosGisModel model = ModelHolder.getModel ();

        try (DB db = model.getDb ()) {
            
            db.addJsonArrays (jb,
                    
                NsiTable.getNsiTable (65).getVocSelect (),
                NsiTable.getNsiTable (71).getVocSelect ()
                    
            );

        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }

        return jb.build ();
        
    }

	@Override
	public JsonObject doImport(JsonObject p, User user) {
		
		MosGisModel m = ModelHolder.getModel ();
        
        try (DB db = m.getDb ()) {
        	
        	Map <String, Object> data = getData (p);
        	data.put("uuid_user", user.getId ());
            
            UUID uuid = (UUID) db.insertId (InInspectionPlans.class, data);
            
            UUIDPublisher.publish (inExportInspectionPlansQueue, uuid);
            
            return Json.createObjectBuilder ().add ("id", uuid.toString ()).build ();
            
        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }
	}

}
