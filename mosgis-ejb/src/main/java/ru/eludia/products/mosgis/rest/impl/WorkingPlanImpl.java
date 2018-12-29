package ru.eludia.products.mosgis.rest.impl;

import java.util.Map;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.Charter;
import ru.eludia.products.mosgis.db.model.tables.CharterObject;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.ContractObject;
import ru.eludia.products.mosgis.db.model.tables.OrganizationWork;
import ru.eludia.products.mosgis.db.model.tables.WorkingList;
import ru.eludia.products.mosgis.db.model.tables.WorkingListItem;
import ru.eludia.products.mosgis.db.model.tables.WorkingPlan;
import ru.eludia.products.mosgis.db.model.tables.WorkingPlanItem;
import ru.eludia.products.mosgis.db.model.tables.WorkingPlanItem.c;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.WorkingPlanLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class WorkingPlanImpl extends BaseCRUD<WorkingPlan> implements WorkingPlanLocal {
    
    private static final Logger logger = Logger.getLogger (WorkingPlanImpl.class.getName ());
/*       
    @Resource (mappedName = "mosgis.inWorkingPlansQueue")
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
*/

    @Override
    public JsonObject select (JsonObject p, User user) {return EMPTY_JSON_OBJECT;}

    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {
        
        final MosGisModel m = ModelHolder.getModel ();
        
        final String fhg = WorkingList.c.FIASHOUSEGUID.lc ();

        final JsonObject item = db.getJsonObject (m
            .get (getTable (), id, "AS root", "*")
            .toOne (WorkingList.class, fhg + " AS " + fhg).on ()
            .toMaybeOne (VocBuilding.class, "AS fias", "label").on ()
            .toMaybeOne (ContractObject.class, "AS cao", "uuid", "startdate", "enddate").on ()
            .toMaybeOne (Contract.class, "AS ca", "*").on ()
            .toMaybeOne (CharterObject.class, "AS cho", "uuid", "startdate", "enddate").on ()
            .toMaybeOne (Charter.class, "AS ch", "*").on ()
            .toMaybeOne (VocOrganization.class, "AS chorg", "label").on ("ch.uuid_org=chorg.uuid")                
        );
        
        VocBuilding.addCaCh (db, job, item.getString (fhg));

        job.add ("item", item);
        
        VocGisStatus.addLiteTo (job);
        VocAction.addTo (job);        
        
        final String f = WorkingListItem.c.UUID_WORKING_LIST.lc ();
        
        final NsiTable nsiTable = NsiTable.getNsiTable (56);

        db.addJsonArrays (job,
                
            m
                .select (WorkingListItem.class, "*", EnTable.c.UUID.lc () + " AS id")
                .where (f, item.getString (f))
                .where (EnTable.c.IS_DELETED, 0)
                .toOne (OrganizationWork.class, "AS w", "label").on ()
                .toMaybeOne (VocOkei.class, "AS ok", "national").on ()
                .toMaybeOne (nsiTable, nsiTable.getLabelField ().getfName () + " AS vc_nsi_56").on ("(w.code_vc_nsi_56=vc_nsi_56.code AND vc_nsi_56.isactual=1)")
                .orderBy (WorkingListItem.c.INDEX_)
                .orderBy ("w.label"),
            
            m
                .select     (WorkingPlanItem.class, "AS cells", "*")
                .where      (c.UUID_WORKING_PLAN, id)
                .and        (EnTable.c.IS_DELETED, 0)

        );        
        
    });}

    @Override
    public JsonObject doUpdate (String id, JsonObject p, User user) {return doAction ((db) -> {
        
        JsonObject data = p.getJsonObject ("data");
        
        final Map<String, Object> r = DB.HASH (
            c.UUID_WORKING_PLAN,      id,
            c.UUID_WORKING_LIST_ITEM, data.getString (c.UUID_WORKING_LIST_ITEM.lc ()),
            c.MONTH,                  data.getInt    (c.MONTH.lc ()),
            c.WORKCOUNT,              data.getInt    (c.WORKCOUNT.lc (), 0)
        );
        
        int m = data.getInt (c.DAYS_BITMASK.lc (), 0);
        r.put (c.DAYS_BITMASK.lc (), m == 0 ? null : Integer.toHexString (m));

        db.upsert (WorkingPlanItem.class, r, 
            c.UUID_WORKING_PLAN.lc (), 
            c.UUID_WORKING_LIST_ITEM.lc (),
            c.MONTH.lc ()
        );
        
    });}        

}