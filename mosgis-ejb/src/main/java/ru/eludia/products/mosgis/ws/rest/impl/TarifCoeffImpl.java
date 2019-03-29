package ru.eludia.products.mosgis.ws.rest.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.tables.TarifCoeff;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.rest.api.TarifCoeffLocal;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class TarifCoeffImpl extends BaseCRUD<TarifCoeff> implements TarifCoeffLocal {
    
    private void checkFilter (JsonObject data, TarifCoeff.c field, Select select) {
        String key = field.lc ();
        String value = data.getString (key, null);
        if (value == null) return;
        select.and (field, value);
    }

    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {                

	final Model m = ModelHolder.getModel();

        Select select = m.select (TarifCoeff.class, "AS root", "*")
	    .where(EnTable.c.IS_DELETED, 0)
            .orderBy(TarifCoeff.c.COEFFICIENTDESCRIPTION)
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        checkFilter (p.getJsonObject("data"), TarifCoeff.c.UUID_TF, select);

	db.addJsonArrayCnt (job, select);
    });}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {

	final Model m = ModelHolder.getModel();

	final JsonObject item = db.getJsonObject (m
            .get (TarifCoeff.class, id, "AS root", "*")
        );

        job.add ("item", item);
    });}
}