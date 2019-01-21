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
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractQualityLevel;
import ru.eludia.products.mosgis.db.model.tables.VocNsi276;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.SupplyResourceContractQualityLevelLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class SupplyResourceContractQualityLevelImpl extends BaseCRUD<SupplyResourceContractQualityLevel> implements SupplyResourceContractQualityLevelLocal {

    private static final Logger logger = Logger.getLogger (SupplyResourceContractQualityLevelImpl.class.getName ());

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

        Select select = m.select (SupplyResourceContractQualityLevel.class, "*", "uuid AS id")
            .toOne(VocNsi276.class, "*").on("tb_sr_ctr_qls.code_vc_nsi_276 = vc_nsi_276.code")
            .toMaybeOne(VocOkei.class, "AS okei", "*").on()
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        applySearch (Search.from (p), select);

        JsonObject data = p.getJsonObject ("data");

        String k = SupplyResourceContractQualityLevel.c.UUID_SR_CTR_SUBJ.lc ();
        String v = data.getString (k, null);
        if (DB.ok (v)) select.and (k, v);

        db.addJsonArrayCnt (job, select);
    });}

    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {

        final MosGisModel m = ModelHolder.getModel ();

        final JsonObject item = db.getJsonObject (m
            .get (SupplyResourceContractQualityLevel.class, id, "*")
            .toMaybeOne(VocOkei.class, "AS okei", "*").on()
        );

        job.add ("item", item);
    });}

    @Override
    public JsonObject getVocs (JsonObject p) {return fetchData((db, job) -> {

        VocNsi276.addTo(db, job);

        final Model m = db.getModel();

        db.addJsonArrays(job,
            m.select(VocOkei.class, "code AS id", "national AS label").orderBy("national")
        );


        String uuid_sr_ctr = p.getString("uuid_sr_ctr", null);

        if (DB.ok(uuid_sr_ctr)){

            db.addJsonArrays(job,
                m.select(SupplyResourceContractQualityLevel.class, "AS used_vc_nsi_276", "code_vc_nsi_276 AS id")
                    .where("uuid_sr_ctr", uuid_sr_ctr)
                    .and("is_deleted", 0)
            );
        }
    });}
}