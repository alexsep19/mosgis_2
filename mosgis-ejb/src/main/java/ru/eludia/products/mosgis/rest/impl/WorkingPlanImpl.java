package ru.eludia.products.mosgis.rest.impl;

import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.WorkingPlan;
import ru.eludia.products.mosgis.db.model.tables.WorkingPlanItem;
import ru.eludia.products.mosgis.db.model.tables.WorkingPlanItem.c;
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
    public JsonObject select (JsonObject p, User user) {
        throw new UnsupportedOperationException ("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JsonObject getItem (String id) {
        throw new UnsupportedOperationException ("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JsonObject doUpdate (String id, JsonObject p, User user) {return doAction ((db) -> {
        
        JsonObject data = p.getJsonObject ("data");
        
        db.upsert (WorkingPlanItem.class, DB.HASH (
            c.UUID_WORKING_PLAN,      id,
            c.UUID_WORKING_LIST_ITEM, data.getString (c.UUID_WORKING_LIST_ITEM.lc ()),
            c.MONTH,                  data.getString (c.MONTH.lc ()),
            c.WORKCOUNT,              data.getString (c.WORKCOUNT.lc ())
        ), 
            c.UUID_WORKING_PLAN.lc (), 
            c.UUID_WORKING_LIST_ITEM.lc (),
            c.MONTH.lc ()
        );
        
    });}        

}