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
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractSubject;
import ru.eludia.products.mosgis.db.model.tables.VocNsiMunicipalServiceResource;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocBuildingAddress;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.SupplyResourceContractObjectSubjectLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class SupplyResourceContractObjectSubjectImpl extends BaseCRUD<SupplyResourceContractSubject> implements SupplyResourceContractObjectSubjectLocal {

    private static final Logger logger = Logger.getLogger (SupplyResourceContractObjectSubjectImpl.class.getName ());

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

        Select select = m.select (SupplyResourceContractSubject.class, "*", "uuid AS id")
            .where("uuid_sr_ctr_obj", p.getJsonObject("data").getString("uuid_sr_ctr_obj"))
            .orderBy ("startsupplydate")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);
    });}

    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {

        final MosGisModel m = ModelHolder.getModel ();

        final JsonObject item = db.getJsonObject (m
            .get (SupplyResourceContractSubject.class, id, "AS root", "*")
            .toOne(SupplyResourceContract.class, "AS sr_ctr", "*").on()
        );

        job.add ("item", item);
    });}

    @Override
    public JsonObject getVocs (JsonObject p) {return fetchData((db, job) -> {

	final Model m = db.getModel();

	String uuid_sr_ctr = p.getString("uuid_sr_ctr");

	db.addJsonArrays(job,
	    NsiTable.getNsiTable(3).getVocSelect().where("code IN"
		, m.select(SupplyResourceContractSubject.class, "code_vc_nsi_3")
		    .where("uuid_sr_ctr", uuid_sr_ctr)
		    .and("uuid_sr_ctr_obj IS NULL")
		    .and("is_deleted", 0)
	    ),
	    NsiTable.getNsiTable(239).getVocSelect().where("code IN"
		 , m.select(SupplyResourceContractSubject.class, "code_vc_nsi_239")
		    .where("uuid_sr_ctr", uuid_sr_ctr)
		    .and("uuid_sr_ctr_obj IS NULL")
		    .and("is_deleted", 0)
	    ),
	    m.select(VocNsiMunicipalServiceResource.class, "*"),
	    m.select(VocOkei.class, "code AS id", "national AS label")
	);

        VocGisStatus.addTo(job);
        VocAction.addTo(job);
    });}
}