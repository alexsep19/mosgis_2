package ru.eludia.products.mosgis.rest.impl;

import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocBic;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.jmx.BicLocal;
import ru.eludia.products.mosgis.rest.api.VocBicLocal;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.impl.base.Base;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class VocBicImpl extends Base<VocBic> implements VocBicLocal {
    
    @EJB
    BicLocal back;

    private static final Logger logger = Logger.getLogger (VocBicImpl.class.getName ());
        
    @Override
    public JsonObject select (JsonObject p) {return fetchData ((db, job) -> {                

        Select select = ModelHolder.getModel ().select (getTable (), "AS root", "*")
            .orderBy (VocBic.c.NAMEP)
            .limit (p.getInt ("offset"), p.getInt ("limit"));
/*
        JsonObject data = p.getJsonObject ("data");

        checkFilter (data, MeteringDevice.c.FIASHOUSEGUID, select);
        checkFilter (data, MeteringDevice.c.UUID_ORG, select);

        applySearch (Search.from (p), select);
*/
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
            NsiTable.getNsiTable (237).getVocSelect ()
        );
        
    });}

}