package ru.eludia.products.mosgis.ws.rest.impl;

import java.math.BigDecimal;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.incoming.InVocBic;
import ru.eludia.products.mosgis.db.model.voc.VocBic;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi237;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.jmx.BicLocal;
import ru.eludia.products.mosgis.rest.api.VocBicLocal;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.ws.rest.impl.base.Base;
import ru.eludia.products.mosgis.ws.rest.impl.tools.ComplexSearch;
import ru.eludia.products.mosgis.ws.rest.impl.tools.Search;
import ru.eludia.products.mosgis.ws.rest.impl.tools.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class VocBicImpl extends Base<VocBic> implements VocBicLocal {
    
    @EJB
    BicLocal back;

    private static final Logger logger = Logger.getLogger (VocBicImpl.class.getName ());

    private static final Pattern RE_BIC = Pattern.compile ("\\d{9}");

    private void applyComplexSearch (final ComplexSearch search, Select select) {
        search.filter (select, "");
    }
    
    private void applySimpleSearch (final SimpleSearch search, Select select) {

        final String searchString = search.getSearchString ();

        Matcher matcher = RE_BIC.matcher (searchString);

        if (matcher.matches ()) {
            select.and (VocBic.c.BIC, search.getSearchString ());
        }
        else {
            select.and (VocBic.c.NAMEP.lc () + "LIKE %?%", search.getSearchString ().toUpperCase ());
        }

    }    
    
    private void applySearch (final Search search, Select select) {        

        if (search == null) {
//            do nothing
        }
        else if (search instanceof ComplexSearch) {
            applyComplexSearch ((ComplexSearch) search, select);
        }
        else if (search instanceof SimpleSearch) {
            applySimpleSearch  ((SimpleSearch) search, select);
        }
        
    }

    @Override
    public JsonObject select (JsonObject p) {return fetchData ((db, job) -> {                

        Select select = ModelHolder.getModel ().select (getTable (), "AS root", "*")
            .orderBy (VocBic.c.NAMEP)
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);

    });}
        
    @Override
    public JsonObject doImport (User user) {
        back.importBic (user);
        return EMPTY_JSON_OBJECT; 
    }

    @Override
    public JsonObject getVocs (JsonObject p) {return fetchData ((db, job) -> {
        
        db.addJsonArrays (job,
                
            db.getModel ().select (Nsi237.class, "AS vc_nsi_237"
                , Nsi237.c.ID.lc ()
                , Nsi237.c.LABEL.lc ()
            ).orderBy (Nsi237.c.LABEL)
                
        );
        
    });}

    @Override
    public JsonObject getLog () {return fetchData ((db, job) -> {
        
        final JsonObject log = db.getJsonObject (db.getModel ()
            .select  (InVocBic.class, "*")
            .orderBy (InVocBic.c.TS.lc () + " DESC")
        );
        
        job.add ("log", log == null ? EMPTY_JSON_OBJECT : log);

    });}

}