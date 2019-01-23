package ru.eludia.products.mosgis.rest.impl;

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
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.tables.Premise;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractObject;
import ru.eludia.products.mosgis.db.model.tables.VocNsi276;
import ru.eludia.products.mosgis.db.model.tables.VocNsiMunicipalServiceResource;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocBuildingAddress;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.SupplyResourceContractObjectLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class SupplyResourceContractObjectImpl extends BaseCRUD<SupplyResourceContractObject> implements SupplyResourceContractObjectLocal {

    private static final Logger logger = Logger.getLogger (SupplyResourceContractObjectImpl.class.getName ());

    private void filterOffDeleted (Select select) {
        select.and (EnTable.c.IS_DELETED, Operator.EQ, 0);
    }

    private void applyComplexSearch (final ComplexSearch search, Select select) {

        search.filter (select, "");
        if (!search.getFilters ().containsKey ("is_deleted")) filterOffDeleted (select);

    }

    private void applySimpleSearch (final SimpleSearch search, Select select) {

        final String s = search.getSearchString ();

//        if (s != null) select.and ("vc_nsi_3.label LIKE ?%", s.toUpperCase ().replace (' ', '%'));

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

        Select select = m.select (SupplyResourceContractObject.class, "*", "uuid AS id")
            .toOne(VocBuilding.class, "AS building", "houseguid", "label").on()
            .toMaybeOne(Premise.class, "AS premise", "id", "label").on()
            .where("uuid_sr_ctr", p.getJsonObject("data").getString("uuid_sr_ctr"))
            .orderBy ("building.label")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);
    });}

    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {

        final MosGisModel m = ModelHolder.getModel ();

        final JsonObject item = db.getJsonObject (m
            .get (SupplyResourceContractObject.class, id, "AS root", "*")
            .toOne(SupplyResourceContract.class, "AS sr_ctr", "*").on()
            .toOne(VocBuilding.class, "AS building", "label").on()
            .toMaybeOne(House.class, "AS house", "uuid", "is_condo").on("house.fiashouseguid = root.fiashouseguid")
            .toMaybeOne(Premise.class, "AS premise", "label", "id").on()
        );

        job.add ("item", item);
    });}

    @Override
    public JsonObject getVocs (JsonObject p) {return fetchData((db, job) -> {

	final Model m = db.getModel();

	db.addJsonArrays(job,
	    NsiTable.getNsiTable(3).getVocSelect(),
	    NsiTable.getNsiTable(239).getVocSelect(),
	    m.select(VocNsiMunicipalServiceResource.class, "*"),
	    m.select(VocOkei.class, "code AS id", "national AS label")
	);

        VocGisStatus.addTo(job);
        VocAction.addTo(job);
	VocNsi276.addTo(db, job);
    });}

    private static final Pattern RE_ZIP = Pattern.compile("[1-9][0-9]{5}");

    @Override
    public JsonObject getBuildings(JsonObject p) { return fetchData((db, job) -> {

        Select select = db.getModel()
            .select(VocBuildingAddress.class, "AS root", "houseguid AS id", "label", "postalcode", "is_condo", "uuid_house")
            .orderBy("root.postalcode, root.label")
            .limit(0, 50)
        ;

        select.andEither("is_condo IS NULL").or("is_condo", p.getString("is_condo", null));

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