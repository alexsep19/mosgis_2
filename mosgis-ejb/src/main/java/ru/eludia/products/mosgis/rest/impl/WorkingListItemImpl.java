package ru.eludia.products.mosgis.rest.impl;

import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.json.JsonObject;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.tables.OrganizationWork;
import ru.eludia.products.mosgis.db.model.tables.WorkingListItem;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.WorkingListItemLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;

@Stateless
public class WorkingListItemImpl extends BaseCRUD<WorkingListItem> implements WorkingListItemLocal {
    
    private static final Logger logger = Logger.getLogger (WorkingListItemImpl.class.getName ());           
    
    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {
        
        final Model m = ModelHolder.getModel ();
        
        final String f = WorkingListItem.c.UUID_WORKING_LIST.lc ();
        
        Select select = m.select (WorkingListItem.class, "*", EnTable.c.UUID.lc () + " AS id")
            .where (f, p.getJsonObject ("data").getString (f))
            .where (EnTable.c.IS_DELETED.lc (), 0)
            .toOne (OrganizationWork.class, "AS w", "label").on ()
            .orderBy (WorkingListItem.c.INDEX_.lc ())
            .orderBy ("w.label");

        db.addJsonArrays (job, select);

    });}

    @Override
    public JsonObject getItem (String id) {
        throw new UnsupportedOperationException ("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}