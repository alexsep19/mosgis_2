package ru.eludia.products.mosgis.ws.rest.impl;

import java.util.Collections;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.incoming.InVocDiff;
import ru.eludia.products.mosgis.db.model.voc.VocDifferentiation;
import ru.eludia.products.mosgis.db.model.voc.VocDifferentiationNsi268;
import ru.eludia.products.mosgis.db.model.voc.VocDifferentiationUsedFor;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.PremiseUsageTarif;
import ru.eludia.products.mosgis.db.model.voc.VocDifferentiationValueKindType;
import ru.eludia.products.mosgis.db.model.voc.VocNsiList;
import ru.eludia.products.mosgis.db.model.voc.VocTariffCaseType;
import ru.eludia.products.mosgis.jmx.DiffLocal;
import ru.eludia.products.mosgis.rest.api.VocDifferentiationLocal;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.ws.rest.impl.base.Base;
import ru.eludia.products.mosgis.ws.rest.impl.tools.ComplexSearch;
import ru.eludia.products.mosgis.ws.rest.impl.tools.Search;
import ru.eludia.products.mosgis.ws.rest.impl.tools.SimpleSearch;
import ru.eludia.base.db.sql.gen.Predicate;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class VocDifferentiationImpl extends Base<VocDifferentiation> implements VocDifferentiationLocal {
    
    @EJB
    DiffLocal back;

    private static final Logger logger = Logger.getLogger (VocDifferentiationImpl.class.getName ());

    @Override
    public JsonObject select (JsonObject p) {return fetchData ((db, job) -> {                
        
        final MosGisModel m = ModelHolder.getModel ();

	Select select =  m.select (VocDifferentiation.class, "AS root", "*", VocDifferentiation.c.DIFFERENTIATIONCODE.lc () + " AS id")
	    .toMaybeOne (VocNsiList.class, "name").on ()
	    .orderBy (VocDifferentiation.c.DIFFERENTIATIONNAME);

	applySearch(Search.from(p), select);

	db.addJsonArrays (job
	    , select

            , m.select (VocDifferentiationNsi268.class, "*")
                
            , m.select (VocDifferentiationUsedFor.class, "*")
                
            , NsiTable.getNsiTable (268).getVocSelect ()
                                
        );
        
        VocTariffCaseType.addTo (job);
        VocDifferentiationValueKindType.addTo (job);

    });}

    private static final Predicate IGNORE = new Predicate("=", Collections.singletonList(null).toArray());

    private void applyComplexSearch (final ComplexSearch search, Select select) {

        search.filter (select, "");

	Predicate id_tariff_type = search.getFilters().getOrDefault("id_tariff_type", IGNORE);

	select.where(VocDifferentiation.c.DIFFERENTIATIONCODE.lc() + " IN",
	    ModelHolder.getModel().select(VocDifferentiationUsedFor.class, VocDifferentiation.c.DIFFERENTIATIONCODE.lc())
	       .and(VocTariffCaseType.c.ID.lc(), id_tariff_type)
	);

    }

    private void applySimpleSearch (final SimpleSearch search, Select select) {

        final String searchString = search.getSearchString ();
        
        if (searchString == null || searchString.isEmpty ()) return;

        select.and (VocDifferentiation.c.DIFFERENTIATIONCODE.lc () + " LIKE ?%", searchString);
        
    }
    
    private void applySearch (final Search search, Select select) {

        if (search instanceof SimpleSearch) {
            applySimpleSearch  ((SimpleSearch) search, select);
        }
        else if (search instanceof ComplexSearch) {
            applyComplexSearch ((ComplexSearch) search, select);
        }
    }
    @Override
    public JsonObject doImport (User user) {
        back.importDiff (user);
        return EMPTY_JSON_OBJECT; 
    }

    @Override
    public JsonObject getLog () {return fetchData ((db, job) -> {
        
        final JsonObject log = db.getJsonObject (db.getModel ()
            .select  (InVocDiff.class, "*")
            .orderBy (InVocDiff.c.TS.lc () + " DESC")
        );
        
        job.add ("log", log == null ? EMPTY_JSON_OBJECT : log);

    });}

}