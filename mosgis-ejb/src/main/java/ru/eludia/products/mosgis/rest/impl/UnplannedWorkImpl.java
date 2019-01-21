package ru.eludia.products.mosgis.rest.impl;

import java.util.Collections;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Predicate;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.OrganizationWork;
import ru.eludia.products.mosgis.db.model.tables.UnplannedWork;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.UnplannedWorkLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;
import ru.eludia.products.mosgis.web.base.ComplexSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class UnplannedWorkImpl extends BaseCRUD<UnplannedWork> implements UnplannedWorkLocal {
    
    private static final Logger logger = Logger.getLogger (UnplannedWorkImpl.class.getName ());           
    private static final Predicate IGNORE = new Predicate ("=", Collections.singletonList (null).toArray ());
    
    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {
        
        final Model m = ModelHolder.getModel ();
        
        final String f = UnplannedWork.c.UUID_REPORTING_PERIOD.lc ();
        
        final NsiTable nsiTable = NsiTable.getNsiTable (56);
        
        final Search search = Search.from (p);
        
        String q = null;
        Predicate orgWorkPredicate = IGNORE;
        Predicate nsi56Predicate = IGNORE;

        if (search instanceof SimpleSearch) {
            q = ((SimpleSearch) search).getSearchString ();
            if (q != null) q = q.trim ().toUpperCase ();
        }
        
        if (search instanceof ComplexSearch) {
            ComplexSearch complexSearch = (ComplexSearch) search;
            orgWorkPredicate = complexSearch.getFilters ().getOrDefault ("uuid_org_work", IGNORE);
            nsi56Predicate = complexSearch.getFilters ().getOrDefault ("code_vc_nsi_56", IGNORE);
        }
        
        Select select = m.select (UnplannedWork.class, "*", EnTable.c.UUID.lc () + " AS id")
            .toMaybeOne (VocOrganization.class, "label AS label_organizationguid").on ()
            .where (f, p.getJsonObject ("data").getString (f))
            .where (EnTable.c.IS_DELETED.lc (), 0)
            .and (UnplannedWork.c.UUID_ORG_WORK.lc (), orgWorkPredicate)
            .toOne (OrganizationWork.class, "AS w", "label", "code_vc_nsi_56 AS code_vc_nsi_56")
                .and ("label_uc LIKE %?%", q)
                .and ("code_vc_nsi_56", nsi56Predicate)
                .on ()
            .toMaybeOne (VocOkei.class, "AS ok", "national").on ()
            .toMaybeOne (nsiTable, nsiTable.getLabelField ().getfName () + " AS vc_nsi_56").on ("(w.code_vc_nsi_56=vc_nsi_56.code AND vc_nsi_56.isactual=1)")
            .orderBy ("w.label");
                
        db.addJsonArrays (job, select);

    });}

    @Override
    public JsonObject getItem (String id) {
        throw new UnsupportedOperationException ("Not supported yet.");
    }

}