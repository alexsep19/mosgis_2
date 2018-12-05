package ru.eludia.products.mosgis.rest.impl;

import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Operator;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.ActualPublicPropertyContract;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.tables.PublicPropertyContract;
import ru.eludia.products.mosgis.db.model.tables.PublicPropertyContractLog;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.PublicPropertyContractLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class PublicPropertyContractImpl extends BaseCRUD<PublicPropertyContract> implements PublicPropertyContractLocal {
/*
    @Resource (mappedName = "mosgis.inHousePublicPropertyContractsQueue")
    Queue queue;

    @Override
    public Queue getQueue () {
        return queue;
    }
*/
    private static final Logger logger = Logger.getLogger (PublicPropertyContractImpl.class.getName ());    
       
    private void filterOffDeleted (Select select) {
        select.and (EnTable.c.IS_DELETED, Operator.EQ, 0);
    }

    private void applyComplexSearch (final ComplexSearch search, Select select) {

        search.filter (select, "");
        if (!search.getFilters ().containsKey ("is_deleted")) filterOffDeleted (select);
        
    }
    
    private void applySimpleSearch (final SimpleSearch search, Select select) {

        final String s = search.getSearchString ();
        
        if (s != null) select.and ("address_uc LIKE ?%", s.toUpperCase ().replace (' ', '%'));

    }
    
    private void applySearch (final Search search, Select select) {        

        if (search instanceof ComplexSearch) {
            applyComplexSearch ((ComplexSearch) search, select);
        }
        else {
            if (search instanceof SimpleSearch) applySimpleSearch  ((SimpleSearch) search, select);
            filterOffDeleted (select);        
        }
        
    }
    
    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {
        
        final Model m = ModelHolder.getModel ();

        Select select = m.select (ActualPublicPropertyContract.class, "*")
            .orderBy (PublicPropertyContract.c.STARTDATE.lc ())
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        applySearch (Search.from (p), select);
        
        JsonObject data = p.getJsonObject ("data");
        
        String k = PublicPropertyContract.c.UUID_ORG.lc ();
        String v = data.getString (k, null);
        if (DB.ok (v)) select.and (k, v);
                      
//        select.and (PublicPropertyContract.c.UUID_CHARTER.lc (), p.getJsonObject ("data").getString ("uuid_charter"));

        db.addJsonArrayCnt (job, select);

    });}
    
    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {

        final MosGisModel m = ModelHolder.getModel ();
        
        final JsonObject item = db.getJsonObject (m
            .get (ActualPublicPropertyContract.class, id, "AS root", "*")
            .toMaybeOne (PublicPropertyContractLog.class, "AS cpl").on ()
            .toMaybeOne (OutSoap.class, "err_text").on ("cpl.uuid_out_soap=out_soap.uuid")
            .toMaybeOne (House.class, "AS house", "uuid").on ("root.fiashouseguid=house.fiashouseguid")
        );

        job.add ("item", item);        
        
        VocGisStatus.addTo (job);
        VocAction.addTo (job);

    });}            
    
    @Override
    protected void publishMessage (VocAction.i action, String id_log) {
        
        switch (action) {
            case APPROVE:
            case PROMOTE:
            case REFRESH:
            case TERMINATE:
            case ANNUL:
            case ROLLOVER:
            case RELOAD:
                super.publishMessage (action, id_log);
            default:
                return;
        }
        
    }    

    @Override
    public JsonObject getVocs (JsonObject p) {
        
        JsonObjectBuilder job = Json.createObjectBuilder ();
        
        VocGisStatus.addTo (job);
        
        return job.build ();
        
    }

}