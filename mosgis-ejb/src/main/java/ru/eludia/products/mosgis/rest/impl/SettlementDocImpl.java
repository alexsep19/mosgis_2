package ru.eludia.products.mosgis.rest.impl;

import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Operator;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract;
import ru.eludia.products.mosgis.db.model.tables.SettlementDoc;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.SettlementDocLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class SettlementDocImpl extends BaseCRUD<SettlementDoc> implements SettlementDocLocal {

    private static final Logger logger = Logger.getLogger (SettlementDocImpl.class.getName ());

    private void filterOffDeleted (Select select) {
        select.and (EnTable.c.IS_DELETED, Operator.EQ, 0);
    }

    private void applyComplexSearch (final ComplexSearch search, Select select) {

        search.filter (select, "");
        if (!search.getFilters ().containsKey ("is_deleted")) filterOffDeleted (select);

    }

    private void applySimpleSearch (final SimpleSearch search, Select select) {

        final String s = search.getSearchString ();

//        if (s != null) select.and ("root.label LIKE ?%", s.toUpperCase ().replace (' ', '%'));

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

        Select select = m.select (SettlementDoc.class, "AS root", "*")
            .toOne(VocOrganization.class, "AS org", "label").on("root.uuid_org_author = org.uuid")
            .toOne(SupplyResourceContract.class, "AS sr_ctr", "uuid", "label").on()
	    .toMaybeOne(VocOrganization.class, "AS org_executor", "label").on("root.uuid_org=org_executor.uuid")
	    .toMaybeOne(VocOrganization.class, "AS org_customer", "label").on("root.uuid_org_customer=org_customer.uuid")
            .orderBy ("root.id_log desc")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        applySearch (Search.from (p), select);


	JsonObject data = p.getJsonObject("data");

	String k = SettlementDoc.c.UUID_ORG.lc();
	String v = data.getString(k, null);
	if (DB.ok(v)) {
	    select.andEither(SettlementDoc.c.UUID_ORG.lc(), v).or(SettlementDoc.c.UUID_ORG_CUSTOMER.lc(), v);
	}

        db.addJsonArrayCnt (job, select);
    });}

    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {

        final MosGisModel m = ModelHolder.getModel ();

        final JsonObject item = db.getJsonObject (m
            .get (SettlementDoc.class, id, "AS root", "*")
	    .toOne(VocOrganization.class, "AS org", "label").on("root.uuid_org_author = org.uuid")
	    .toOne(SupplyResourceContract.class, "AS sr_ctr", "uuid", "label").on()
	    .toMaybeOne(VocOrganization.class, "AS org_executor", "label").on("root.uuid_org=org_executor.uuid")
	    .toMaybeOne(VocOrganization.class, "AS org_customer", "label").on("root.uuid_org_customer=org_customer.uuid")
        );

        job.add ("item", item);

	VocGisStatus.addTo(job);
	VocAction.addTo(job);
    });}

    @Override
    public JsonObject getVocs (JsonObject p) {return fetchData((db, job) -> {

    });}
}