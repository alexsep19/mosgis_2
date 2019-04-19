package ru.eludia.products.mosgis.ws.rest.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.CitizenCompensationPayment;
import ru.eludia.products.mosgis.db.model.voc.VocCitizenCompensationPaymentType;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.CitizenCompensationPaymentLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.ws.rest.impl.tools.ComplexSearch;
import ru.eludia.products.mosgis.ws.rest.impl.tools.Search;
import ru.eludia.products.mosgis.ws.rest.impl.tools.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class CitizenCompensationPaymentImpl extends BaseCRUD<CitizenCompensationPayment> implements CitizenCompensationPaymentLocal {

    private void filterOffDeleted (Select select) {
        select.and ("is_deleted", 0);
    }

    private void applyComplexSearch (final ComplexSearch search, Select select) {

        search.filter (select, "");

        if (!search.getFilters ().containsKey ("is_deleted")) filterOffDeleted (select);

    }

    private void applySimpleSearch (final SimpleSearch search, Select select) {

        filterOffDeleted (select);

        final String searchString = search.getSearchString ();

        if (searchString == null || searchString.isEmpty ()) return;

        select.and ("label_uc LIKE ?%", searchString.toUpperCase ());
    }

    private void applySearch (final Search search, Select select) {

        if (search instanceof SimpleSearch) {
            applySimpleSearch  ((SimpleSearch) search, select);
        }
        else if (search instanceof ComplexSearch) {
            applyComplexSearch ((ComplexSearch) search, select);
        }
        else {
            filterOffDeleted (select);
        }

    }

    private void checkFilter (JsonObject data, CitizenCompensationPayment.c field, Select select) {
        String key = field.lc ();
        String value = data.getString (key, null);
        if (value == null) return;
        select.and (field, value);
    }

    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {

	final Model m = db.getModel();

	Select select = m.select (getTable (), "AS root", "*")
	    .orderBy (CitizenCompensationPayment.c.PAYMENTDATE.lc() + " DESC")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        JsonObject data = p.getJsonObject ("data");

        checkFilter (data, CitizenCompensationPayment.c.UUID_CIT_COMP, select);

        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);
    });}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {

        final MosGisModel m = ModelHolder.getModel ();

        final JsonObject item = db.getJsonObject (m
            .get (getTable (), id, "AS root", "*")
        );

        job.add ("item", item);

    });}

    @Override
    public JsonObject getVocs () {return fetchData((db, jb) -> {

	VocCitizenCompensationPaymentType.addTo (jb);

    });}
}