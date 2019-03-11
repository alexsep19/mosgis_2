package ru.eludia.products.mosgis.ws.rest.impl;

import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.db.model.incoming.InVocDiff;
import ru.eludia.products.mosgis.db.model.voc.VocDifferentiation;
import ru.eludia.products.mosgis.db.model.voc.VocDifferentiationNsi268;
import ru.eludia.products.mosgis.db.model.voc.VocDifferentiationUsedFor;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocDifferentiationValueKindType;
import ru.eludia.products.mosgis.db.model.voc.VocNsiList;
import ru.eludia.products.mosgis.db.model.voc.VocTariffCaseType;
import ru.eludia.products.mosgis.jmx.DiffLocal;
import ru.eludia.products.mosgis.rest.api.VocDifferentiationLocal;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.ws.rest.impl.base.Base;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class VocDifferentiationImpl extends Base<VocDifferentiation> implements VocDifferentiationLocal {
    
    @EJB
    DiffLocal back;

    private static final Logger logger = Logger.getLogger (VocDifferentiationImpl.class.getName ());

    @Override
    public JsonObject select (JsonObject p) {return fetchData ((db, job) -> {                
        
        final MosGisModel m = ModelHolder.getModel ();

        db.addJsonArrays (job
                
            , m.select (VocDifferentiation.class, "AS root", "*", VocDifferentiation.c.DIFFERENTIATIONCODE.lc () + " AS id")
                .toMaybeOne (VocNsiList.class, "name").on ()
                .orderBy (VocDifferentiation.c.DIFFERENTIATIONNAME)
                
            , m.select (VocDifferentiationNsi268.class, "*")
                
            , m.select (VocDifferentiationUsedFor.class, "*")
                
            , NsiTable.getNsiTable (268).getVocSelect ()
                                
        );
        
        VocTariffCaseType.addTo (job);
        VocDifferentiationValueKindType.addTo (job);

    });}
        
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