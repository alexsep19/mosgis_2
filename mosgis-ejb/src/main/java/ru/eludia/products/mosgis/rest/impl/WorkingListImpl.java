package ru.eludia.products.mosgis.rest.impl;

import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Operator;
import ru.eludia.base.db.sql.gen.Predicate;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.Charter;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.CharterObject;
import ru.eludia.products.mosgis.db.model.tables.ContractObject;
import ru.eludia.products.mosgis.db.model.tables.OrganizationWork;
import ru.eludia.products.mosgis.db.model.tables.WorkingPlan;
import ru.eludia.products.mosgis.db.model.tables.WorkingList;
import ru.eludia.products.mosgis.db.model.tables.WorkingListItem;
import ru.eludia.products.mosgis.db.model.tables.WorkingListItemLog;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.WorkingListLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class WorkingListImpl extends BaseCRUD<WorkingList> implements WorkingListLocal {
    
    private static final Logger logger = Logger.getLogger (WorkingListImpl.class.getName ());
       
    @Resource (mappedName = "mosgis.inWorkingListsQueue")
    Queue queue;

    @Override
    public Queue getQueue () {
        return queue;
    }

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

    private void filterOffDeleted (Select select) {
        select.and (EnTable.c.IS_DELETED, Operator.EQ, 0);
    }

    private void applyComplexSearch (final ComplexSearch search, Select select) {
        
        final Map<String, Predicate> filters = search.getFilters ();
        
        Predicate dt = filters.get ("dt");
        
        if (dt != null) {
            final Object [] v = dt.getValues ();
            filters.put (WorkingList.c.DT_FROM.lc (), new Predicate ("<=", v));
            filters.put (WorkingList.c.DT_TO.lc (),   new Predicate (">=", v));
            filters.remove ("dt");
        }
        
        search.apply (select);
        
        if (!filters.containsKey ("is_deleted")) filterOffDeleted (select);
        
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

        Select select = m.select (WorkingList.class, "*", EnTable.c.UUID.lc () + " AS id")
            .orderBy (WorkingList.c.DT_FROM.lc ())
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);

    });}
    
    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {

        final MosGisModel m = ModelHolder.getModel ();
        
        final JsonObject item = db.getJsonObject (m
            .get (getTable (), id, "AS root", "*")
            .toMaybeOne (VocBuilding.class, "AS fias", "label").on ()
            .toMaybeOne (ContractObject.class, "AS cao", "startdate", "enddate").on ()
            .toMaybeOne (Contract.class, "AS ca", "*").on ()
            .toMaybeOne (CharterObject.class, "AS cho", "startdate", "enddate").on ()
            .toMaybeOne (Charter.class, "AS ch", "*").on ()
            .toMaybeOne (VocOrganization.class, "AS chorg", "label").on ("ch.uuid_org=chorg.uuid")
        );

        job.add ("item", item);

        VocBuilding.addCaCh (db, job, item.getString (WorkingList.c.FIASHOUSEGUID.lc ()));

        VocGisStatus.addTo (job);
        VocAction.addTo (job);

        db.addJsonArrays (job,

            NsiTable.getNsiTable (56).getVocSelect (), 
            
            m
                .select  (WorkingPlan.class, "AS plans", "*")
                .where   (EnTable.c.IS_DELETED.lc (), 0)
                .where   (WorkingPlan.c.UUID_WORKING_LIST.lc (), id)
                .orderBy (WorkingPlan.c.YEAR.lc ()),

            m
                .select     (OrganizationWork.class, "AS org_works", "uuid AS id", "label")
                .toMaybeOne (VocOkei.class, "AS okei", "national").on ()
                .where      ("uuid_org", item.getString ("ca.uuid_org", item.getString ("ch.uuid_org", "00")))
                .and        ("id_status", VocAsyncEntityState.i.OK.getId ())
                .and        ("is_deleted", 0)
                .orderBy    ("org_works.label")

        );

    });}

    @Override
    public JsonObject doAddItems (String id, JsonObject p, User user) {return doAction ((db) -> {
        
        for (JsonValue t: p.getJsonObject ("data").getJsonArray ("ids")) {
            
            String uuid = db.insertId (WorkingListItem.class, HASH (
                 WorkingListItem.c.UUID_WORKING_LIST.lc (), id,
                 WorkingListItem.c.COUNT.lc (), 1,
                 WorkingListItem.c.UUID_ORG_WORK.lc (), ((JsonString) t).getString ()
            )).toString ();
            
            String id_log = db.insertId (WorkingListItemLog.class, HASH (
                "action", VocAction.i.CREATE.getName (),
                "uuid_object", uuid,
                "uuid_user", user.getId ()
            )).toString ();

            db.update (WorkingListItem.class, HASH (
                "uuid",      uuid,
                "id_log",    id_log
            ));
            
        }        
        
    });}
    
    @Override
    public JsonObject doApprove (String id, User user) {return doAction ((db) -> {

        db.update (getTable (), HASH (EnTable.c.UUID,               id,
            WorkingList.c.ID_CTR_STATUS, VocGisStatus.i.PENDING_RQ_PLACING.getId ()
        ));

        logAction (db, user, id, VocAction.i.APPROVE);

    });}

}