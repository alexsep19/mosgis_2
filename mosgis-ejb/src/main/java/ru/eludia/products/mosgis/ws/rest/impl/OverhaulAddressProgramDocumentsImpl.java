package ru.eludia.products.mosgis.ws.rest.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.tables.OverhaulAddressProgram;
import ru.eludia.products.mosgis.db.model.tables.OverhaulAddressProgramDocument;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi79;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.ValidationException;
import ru.eludia.products.mosgis.rest.api.OverhaulAddressProgramDocumentsLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.ws.rest.impl.tools.ComplexSearch;
import ru.eludia.products.mosgis.ws.rest.impl.tools.Search;
import ru.eludia.products.mosgis.ws.rest.impl.tools.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class OverhaulAddressProgramDocumentsImpl extends BaseCRUD <OverhaulAddressProgramDocument> implements OverhaulAddressProgramDocumentsLocal {
    
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

        select.and ("fullname_uc LIKE ?%", searchString.toUpperCase ());
        
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
    public JsonObject select(JsonObject p, User user) {return fetchData ((db, job) -> {
        
        Select select = ModelHolder.getModel ().select (OverhaulAddressProgramDocument.class, "AS root", "*")
                .where   ("program_uuid", p.getString ("program_uuid"))
                .orderBy ("number_")
                .orderBy ("fullname")
                .orderBy ("date_")
                .limit   (p.getInt ("offset"), p.getInt ("limit"));
        
        applySearch (Search.from (p), select);
        
        db.addJsonArrayCnt (job, select);
        
    });}

    @Override
    public JsonObject getItem(String id, User user) {return fetchData ((db, job) -> {
        
        job.add ("item", db.getJsonObject (ModelHolder.getModel ()
            .get   (getTable (), id, "*")
            .toOne (OverhaulAddressProgram.class, "AS program", "is_deleted", "org_uuid", "id_oap_status").on ()
        ));
        
        VocGisStatus.addLiteTo (job);
        VocAction.addTo (job);
        VocNsi79.addToOverhaulShortProgramDocument (db, job);
        
    });}

    @Override
    public JsonObject getVocs() {
        
        JsonObjectBuilder job = Json.createObjectBuilder ();
        
        try (DB db = ModelHolder.getModel ().getDb ()) {            
            VocGisStatus.addLiteTo (job);
            VocAction.addTo (job);
            VocNsi79.addToOverhaulShortProgramDocument (db, job);
        }
        catch (ValidationException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return job.build ();
        
    }
    
}
