package ru.eludia.products.mosgis.rest.impl;

import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import javax.json.JsonObject;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Operator;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.CharterObject;
import ru.eludia.products.mosgis.db.model.tables.PublicPropertyContract;
import ru.eludia.products.mosgis.db.model.tables.PublicPropertyContractLog;
import ru.eludia.products.mosgis.db.model.tables.OrganizationWork;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
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
        
        if (s == null || s.isEmpty ()) return;
        
        final String uc = s.toUpperCase ();

        select.andEither ("owner_label_uc LIKE %?%", uc).or ("label", s);

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

        Select select = m.select (PublicPropertyContract.class, "*", "uuid AS id")
            .toMaybeOne (VocBuilding.class, "label").on ()
//            .orderBy (PublicPropertyContract.c.BEGINDATE.lc ())
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        applySearch (Search.from (p), select);
        
//        select.and (PublicPropertyContract.c.UUID_CHARTER.lc (), p.getJsonObject ("data").getString ("uuid_charter"));

        db.addJsonArrayCnt (job, select);

    });}
    
    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {

        final MosGisModel m = ModelHolder.getModel ();
        
        final JsonObject item = db.getJsonObject (m
            .get (getTable (), id, "AS root", "*")
            .toOne (VocOrganization.class, "AS org", "label").on ("ctr.uuid_org")
            .toOne (VocBuilding.class, "AS fias", "label").on ()
            .toMaybeOne (PublicPropertyContractLog.class, "AS cpl").on ()
            .toMaybeOne (OutSoap.class, "err_text").on ("cpl.uuid_out_soap=out_soap.uuid")
        );

        job.add ("item", item);        
                
        db.addJsonArrays (job,

            m
                .select     (OrganizationWork.class, "AS org_works", "uuid AS id", "label")
                .toMaybeOne (VocOkei.class, "AS okei", "national").on ()
                .where      ("uuid_org", item.getString ("ctr.uuid_org"))
                .and        ("id_status", VocAsyncEntityState.i.OK.getId ())
                .orderBy    ("org_works.label")
            
        );

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

}