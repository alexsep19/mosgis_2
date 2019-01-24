package ru.eludia.products.mosgis.rest.impl;

import java.util.Map;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.build.QP;
import ru.eludia.base.db.sql.gen.Operator;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.incoming.nsi.InNsiItem;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.ActualSupplyResourceContract;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractLog;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractSubject;
import ru.eludia.products.mosgis.db.model.tables.VocNsi239;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocGisSupplyResourceContractCustomerType;
import ru.eludia.products.mosgis.db.model.voc.VocUserOktmo;
import ru.eludia.products.mosgis.db.model.voc.VocGisContractDimension;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;
import ru.eludia.products.mosgis.db.model.voc.VocSupplyResourceContractFileType;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.SupplyResourceContractLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class SupplyResourceContractImpl extends BaseCRUD<SupplyResourceContract> implements SupplyResourceContractLocal {

    private static final Logger logger = Logger.getLogger (SupplyResourceContractImpl.class.getName ());

    private static final String IS_RS_CTR_NSI_58 = "f_101d7ea249"; // Применимо к договорам РС

    private void filterOffDeleted (Select select) {
        select.and (EnTable.c.IS_DELETED, Operator.EQ, 0);
    }

    private void applyComplexSearch (final ComplexSearch search, Select select) {

        search.filter (select, "");
        if (!search.getFilters ().containsKey ("is_deleted")) filterOffDeleted (select);

    }

    private void applySimpleSearch (final SimpleSearch search, Select select) {

        final String s = search.getSearchString ();

        if (s != null) select.and ("customer_label_uc LIKE ?%", s.toUpperCase ().replace (' ', '%'));

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

        Select select = m.select (ActualSupplyResourceContract.class, "*")
            .orderBy (SupplyResourceContract.c.SIGNINGDATE.lc() + " DESC")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        applySearch (Search.from (p), select);

        JsonObject data = p.getJsonObject ("data");

        String k = SupplyResourceContract.c.UUID_ORG.lc();
        String v = data.getString (k, null);
        if (DB.ok (v)) select.and (k, v);

        String k_c = SupplyResourceContract.c.UUID_ORG_CUSTOMER.lc();
        String v_c = data.getString(k_c, null);
        if (DB.ok(v_c)) {
            select.and(k_c, v_c);
        }

        db.addJsonArrayCnt (job, select);

    });}

    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {

        final MosGisModel m = ModelHolder.getModel ();

        final JsonObject item = db.getJsonObject (m
            .get (SupplyResourceContract.class, id, "*")
            .toOne(VocGisSupplyResourceContractCustomerType.class, "AS customer_type", "label").on()
            .toMaybeOne(VocOrganization.class, "AS org_customer", "label").on("uuid_org_customer")
            .toMaybeOne(VocPerson.class, "AS person_customer", "label").on("uuid_person_customer")
            .toMaybeOne(VocOrganization.class, "AS org", "label").on("uuid_org")
        );

        job.add ("item", item);

	if (item.getInt(SupplyResourceContract.c.SPECQTYINDS.lc()) == VocGisContractDimension.i.BY_CONTRACT.getId()) {

	    String is_on_tab_temperature = db.getString(m.select(SupplyResourceContractSubject.class, "AS root", "uuid")
		.toOne(SupplyResourceContract.class, SupplyResourceContract.c.SPECQTYINDS.lc()).on()
		.where(EnTable.c.IS_DELETED, 0)
		.and(SupplyResourceContractSubject.c.UUID_SR_CTR, id)
		.and(SupplyResourceContractSubject.c.CODE_VC_NSI_239, VocNsi239.CODE_VC_NSI_239_HEAT_ENERGY)
		.limit(0, 1)
	    );

	    job.add("is_on_tab_temperature", is_on_tab_temperature != null);
	}
	

	VocGisStatus.addLiteTo (job);
        VocAction.addTo (job);
        VocGisContractDimension.addTo(job);
        VocSupplyResourceContractFileType.addTo(job);
        db.addJsonArrays(job,
                NsiTable.getNsiTable(58).getVocSelect().where(IS_RS_CTR_NSI_58, 1)
        );
    });}

    @Override
    public JsonObject getVocs (JsonObject p) {return fetchData((db, job) -> {

        VocGisStatus.addLiteTo (job);
        VocGisSupplyResourceContractCustomerType.addTo(job);

        db.addJsonArrays(job,
            NsiTable.getNsiTable(58).getVocSelect().where(IS_RS_CTR_NSI_58, 1)
        );
    });}
}