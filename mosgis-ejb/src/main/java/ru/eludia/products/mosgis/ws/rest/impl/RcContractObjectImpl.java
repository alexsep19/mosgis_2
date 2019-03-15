package ru.eludia.products.mosgis.ws.rest.impl;

import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Operator;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.RcContract;
import ru.eludia.products.mosgis.db.model.tables.RcContractObject;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.tables.ActualSomeContractObject;
import ru.eludia.products.mosgis.db.model.voc.VocBuildingAddress;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.RcContractObjectLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.ws.rest.impl.tools.ComplexSearch;
import ru.eludia.products.mosgis.ws.rest.impl.tools.Search;
import ru.eludia.products.mosgis.ws.rest.impl.tools.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class RcContractObjectImpl extends BaseCRUD<RcContractObject> implements RcContractObjectLocal {

    private static final Logger logger = Logger.getLogger (RcContractObjectImpl.class.getName ());

    private void filterOffDeleted (Select select) {
        select.and (EnTable.c.IS_DELETED, Operator.EQ, 0);
    }

    private void applyComplexSearch (final ComplexSearch search, Select select) {

        search.filter (select, "");
        if (!search.getFilters ().containsKey ("is_deleted")) filterOffDeleted (select);

    }

    private void applySimpleSearch (final SimpleSearch search, Select select) {

        final String s = search.getSearchString ();

        if (s != null) select.and ("label LIKE ?%", s.toUpperCase ().replace (' ', '%'));

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

        Select select = m.select (RcContractObject.class, "*", "uuid AS id")
            .toOne(VocBuilding.class, "AS building", "houseguid", "label").on()
            .where("uuid_rc_ctr", p.getJsonObject("data").getString("uuid_rc_ctr"))
            .orderBy ("building.label")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);
    });}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {

        final MosGisModel m = ModelHolder.getModel ();

        final JsonObject item = db.getJsonObject (m
            .get (RcContractObject.class, id, "AS root", "*")
            .toOne(RcContract.class, "AS rc_ctr", "*").on()
            .toOne(VocBuilding.class, "AS building", "label").on()
        );

        job.add ("item", item);
    });}

    @Override
    public JsonObject getVocs (JsonObject p) {return fetchData((db, job) -> {
        VocGisStatus.addTo(job);
        VocAction.addTo(job);
    });}

    private static final Pattern RE_ZIP = Pattern.compile("[1-9][0-9]{5}");

    @Override
    public JsonObject getBuildings(JsonObject p, User user) { return fetchData((db, job) -> {

	final Model m = db.getModel();

	Select select = m
	    .select(VocBuildingAddress.class, "AS root", "houseguid AS id", "label", "postalcode", "is_condo", "uuid_house")
	    .where("houseguid", m.select(ActualSomeContractObject.class, ActualSomeContractObject.c.FIASHOUSEGUID.lc())
		.where(ActualSomeContractObject.c.UUID_ORG, user.getUuidOrg())
	    )
	    .orderBy("postalcode, label")
	    .limit(0, 50);


	StringBuilder sb = new StringBuilder();
	StringTokenizer st = new StringTokenizer(p.getString("search", ""));

	while (st.hasMoreTokens()) {

	    final String token = st.nextToken();

	    if (sb.length() == 0 && RE_ZIP.matcher(token).matches()) {
		select.and("postalcode", token);
	    } else {
		sb.append(token.toUpperCase().replace('Ё', 'Е'));
		sb.append('%');
	    }
	}

	if (sb.length() > 0) {
	    select.and("label_uc LIKE", sb.toString());
	}

	db.addJsonArrays(job, select);
    });}
}