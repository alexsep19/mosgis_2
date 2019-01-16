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
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.ActualSupplyResourceContract;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractSubject;
import ru.eludia.products.mosgis.db.model.tables.VocNsiMunicipalServiceResource;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisSupplyResourceContractCustomerType;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.SupplyResourceContractSubjectLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class SupplyResourceContractSubjectImpl extends BaseCRUD<SupplyResourceContractSubject> implements SupplyResourceContractSubjectLocal {

    private static final Logger logger = Logger.getLogger (SupplyResourceContractSubjectImpl.class.getName ());

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
            .orderBy (SupplyResourceContractSubject.c.STARTSUPPLYDATE.lc ())
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        applySearch (Search.from (p), select);

        JsonObject data = p.getJsonObject ("data");

        String k = SupplyResourceContractSubject.c.UUID_SR_CTR.lc ();
        String v = data.getString (k, null);
        if (DB.ok (v)) select.and (k, v);

        db.addJsonArrayCnt (job, select);
    });}

    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {

        final MosGisModel m = ModelHolder.getModel ();

        final JsonObject item = db.getJsonObject (m
            .get (SupplyResourceContractSubject.class, id, "*")
            .toOne(ActualSupplyResourceContract.class, "AS sr_ctr"
                    , "uuid"
                    , "customer_label"
                    , "customer_type_label"
                    , "label"
                    , "isplannedvolume"
                    , "plannedvolumetype"
                    , "uuid_person_customer"
                    , "uuid_org_customer"
                    , "uuid_org"
            ).on("uuid_sr_ctr")
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

        VocAction.addTo(job);
    });}
}