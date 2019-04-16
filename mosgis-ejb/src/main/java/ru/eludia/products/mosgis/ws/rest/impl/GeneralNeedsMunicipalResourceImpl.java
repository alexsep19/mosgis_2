package ru.eludia.products.mosgis.ws.rest.impl;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import javax.json.JsonObject;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.GeneralNeedsMunicipalResource;
import ru.eludia.products.mosgis.db.model.tables.GeneralNeedsMunicipalResourceLog;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi2;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.GeneralNeedsMunicipalResourceLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.ws.rest.impl.tools.ComplexSearch;
import ru.eludia.products.mosgis.ws.rest.impl.tools.Search;
import ru.eludia.products.mosgis.ws.rest.impl.tools.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class GeneralNeedsMunicipalResourceImpl extends BaseCRUD<GeneralNeedsMunicipalResource> implements GeneralNeedsMunicipalResourceLocal {

    @Resource (mappedName = "mosgis.inNsiGeneralNeedsMunicipalResourcesQueue")
    Queue queue;
    
    @Override
    public Queue getQueue (VocAction.i action) {
        
        switch (action) {
            case CREATE:
            case UPDATE:
            case DELETE:
                return queue;
            default:
                return null;
        }
                
    }

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

        select.and (GeneralNeedsMunicipalResource.c.LABEL_UC.lc () + " LIKE ?%", searchString.toUpperCase ());
        
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
    
    private void checkFilter (JsonObject data, GeneralNeedsMunicipalResource.c field, Select select) {
        String key = field.lc ();
        String value = data.getString (key, null);
        if (value == null) return;
        select.and (field, value);
    }

    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {                

        Select select = ModelHolder.getModel ().select (getTable (), "AS root", "*", "uuid AS id")
            .toMaybeOne (GeneralNeedsMunicipalResourceLog.class).on ()
            .toMaybeOne (OutSoap.class,       "err_text").on ()
            .toOne      (VocOrganization.class, "AS org", "label").on ("root.uuid_org=org.uuid")
            .orderBy ("root." + GeneralNeedsMunicipalResource.c.PARENTCODE.lc ())
            .orderBy ("root." + GeneralNeedsMunicipalResource.c.SORTORDER.lc ())
            .limit (p.getInt ("offset"), p.getInt ("limit"));
        
        JsonObject data = p.getJsonObject ("data");
        
        checkFilter (data, GeneralNeedsMunicipalResource.c.UUID_ORG, select);

        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);

    });}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {

        job.add ("item", db.getJsonObject (ModelHolder.getModel ()
            .get (getTable (), id, "AS root", "*")
            .toMaybeOne (GeneralNeedsMunicipalResourceLog.class, "AS log").on ()
            .toMaybeOne (OutSoap.class, "err_text").on ("log.uuid_out_soap=out_soap.uuid")
            .toOne      (VocOrganization.class, "AS org", "label").on ("root.uuid_org=org.uuid")
        ));
        
        VocGisStatus.addTo (job);
        VocAction.addTo (job);
        Nsi2.i.addGeneralNeedsTo (job);
        
        db.addJsonArrays (job
            , Nsi2.getVocSelect ()
            , VocOkei.getVocSelect ()
        );

    });}

    @Override
    public JsonObject getVocs () {return fetchData ((db, job) -> {
        
        final MosGisModel m = ModelHolder.getModel ();

        VocGisStatus.addLiteTo (job);
        VocAction.addTo (job);
        Nsi2.i.addGeneralNeedsTo (job);
        
        db.addJsonArrays (job
            , Nsi2.getVocSelect ()
            , VocOkei.getVocSelect ()
            , m
                .select (VocOrganization.class, "uuid AS id", "label")                    
                .orderBy ("label")
                .and ("uuid", m.select (getTable (), "uuid_org").where ("is_deleted", 0))
                
        );

    });}

}