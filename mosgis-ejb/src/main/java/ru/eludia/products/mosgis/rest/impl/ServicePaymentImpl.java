package ru.eludia.products.mosgis.rest.impl;

import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Operator;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.OrganizationWork;
import ru.eludia.products.mosgis.db.model.tables.ServicePayment;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.ServicePaymentLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ServicePaymentImpl extends BaseCRUD<ServicePayment> implements ServicePaymentLocal {
    
    private static final Logger logger = Logger.getLogger (ServicePaymentImpl.class.getName ());    
       
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
        
        final NsiTable nsiTable = NsiTable.getNsiTable (56);

        Select select = m.select (ServicePayment.class, "*", "uuid AS id")
            .toOne (OrganizationWork.class, "AS w", "label").on ()
            .toMaybeOne (VocOkei.class, "AS ok", "national").on ()
            .toMaybeOne (nsiTable, nsiTable.getLabelField ().getfName () + " AS vc_nsi_56").on ("(w.code_vc_nsi_56=vc_nsi_56.code AND vc_nsi_56.isactual=1)")
            .orderBy ("w.label")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        applySearch (Search.from (p), select);

        final JsonObject data = p.getJsonObject ("data");
        checkRef (ServicePayment.c.UUID_CONTRACT_PAYMENT, data, select);
        checkRef (ServicePayment.c.UUID_CHARTER_PAYMENT, data, select);

        db.addJsonArrayCnt (job, select);

    });}

    private void checkRef (final ServicePayment.c c, final JsonObject data, Select select) {
        final String key = c.lc ();
        final String uuid = data.getString (key, null);
        if (uuid != null) select.and (key, uuid);
    }
    
    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {

        final MosGisModel m = ModelHolder.getModel ();

        job.add ("item", db.getJsonObject (m
            .get (getTable (), id, "AS root", "*")
        ));

        VocAction.addTo (job);

    });}        
    
}
