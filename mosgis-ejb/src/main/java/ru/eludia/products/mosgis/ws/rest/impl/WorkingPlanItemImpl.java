package ru.eludia.products.mosgis.ws.rest.impl;

import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.OrganizationWork;
import ru.eludia.products.mosgis.db.model.tables.WorkingListItem;
import ru.eludia.products.mosgis.db.model.tables.WorkingPlanItem;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.WorkingPlanItemLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class WorkingPlanItemImpl extends BaseCRUD<WorkingPlanItem> implements WorkingPlanItemLocal {
    
    private static final Logger logger = Logger.getLogger (WorkingPlanItemImpl.class.getName ());           
    
    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {
        
        final Model m = ModelHolder.getModel ();
        
        final String f = WorkingPlanItem.c.UUID_REPORTING_PERIOD.lc ();
        
        final NsiTable nsiTable = NsiTable.getNsiTable (56);

        Select select = m.select (WorkingPlanItem.class, "*", EnTable.c.UUID.lc () + " AS id")
            .where (f, p.getJsonObject ("data").getString (f))
            .where (EnTable.c.IS_DELETED.lc (), 0)                
            .toOne (WorkingListItem.class, "AS li", "*").on ()
            .toOne (OrganizationWork.class, "AS w", "label").on ()
            .toMaybeOne (VocOkei.class, "AS ok", "national").on ()
            .toMaybeOne (nsiTable, nsiTable.getLabelField ().getfName () + " AS vc_nsi_56").on ("(w.code_vc_nsi_56=vc_nsi_56.code AND vc_nsi_56.isactual=1)")
            .orderBy ("li." + WorkingListItem.c.INDEX_.lc ())
            .orderBy ("w.label");

        db.addJsonArrays (job, select);

    });}

    @Override
    public JsonObject getItem (String id, User user) {
        throw new UnsupportedOperationException ("Not supported yet.");
    }

}