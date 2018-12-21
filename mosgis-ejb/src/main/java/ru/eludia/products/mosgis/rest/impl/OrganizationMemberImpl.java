package ru.eludia.products.mosgis.rest.impl;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Operator;
import ru.eludia.base.db.sql.gen.Predicate;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.db.util.TypeConverter;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.OrganizationMember;
import ru.eludia.products.mosgis.db.model.tables.OrganizationMemberFile;
import ru.eludia.products.mosgis.db.model.tables.OrganizationMemberDocument;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationParticipant;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.OrganizationMemberLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;


@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class OrganizationMemberImpl extends BaseCRUD<OrganizationMemberDocument> implements OrganizationMemberLocal {

    private static final Logger logger = Logger.getLogger (OrganizationMemberImpl.class.getName ());

    private void filterOffDeleted (Select select) {
        select.and (EnTable.c.IS_DELETED, Operator.EQ, 0);
    }

    private void applyComplexSearch (final ComplexSearch search, Select select) {

        search.filter (select, "");

        if (!search.getFilters ().containsKey ("is_deleted")) filterOffDeleted (select);


        Map<String, Predicate> filters = search.getFilters();

        Predicate dt = search.getFilters().get("dt");

        if (dt != null) {
            final Object[] v = dt.getValues();

            Timestamp dt_filter = TypeConverter.timestamp(v[0]); // HACK: Predicate("...>="
            Object[] dt_minus_s = new Object[] {new Timestamp(dt_filter.getTime() - 1000)};
            Object[] dt_plus_s = new Object[] {new Timestamp(dt_filter.getTime() + 1000)};

            filters.put(OrganizationMember.c.DT_FROM.lc(), new Predicate("<", dt_plus_s));
            filters.put(OrganizationMember.c.DT_TO.lc(), new Predicate("...>", dt_minus_s));
            filters.remove("dt");
        }

        search.apply(select);

    }

    private void applySimpleSearch (final SimpleSearch search, Select select) {

        final String s = search.getSearchString ();

        if (s == null || s.isEmpty ()) return;

        final String uc = s.toUpperCase ();

        select.andEither ("label_uc LIKE %?%", uc).or ("label", s);

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

        final JsonObject data = p.getJsonObject("data");

        Select select = m.select (OrganizationMember.class, "*")
            .where("uuid_org", data.getString("uuid_org", null))
            .toMaybeOne(VocOrganizationParticipant.class, "AS participants", "label").on()
            .orderBy ("tb_org_members.label")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);

    });}

    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {

        final MosGisModel m = ModelHolder.getModel ();

        final JsonObject item = db.getJsonObject (m
            .get (OrganizationMemberDocument.class, id, "AS root", "*")
            .toOne(VocOrganization.class, "AS org_parent", "label", "id_type").on("uuid_org")
            .toMaybeOne (VocOrganization.class, "AS org_auth", "label", "id_type").on ("uuid_org_author")
            .toMaybeOne (VocOrganization.class, "AS org", "label", "id_type").on("uuid_org_member")
            .toMaybeOne(VocPerson.class, "AS person", "label", "uuid_org").on("uuid_person_member")
        );

        db.addJsonArrays(job,
            m.select(OrganizationMemberFile.class, "AS files", "uuid AS id", "label")
                .where("uuid_org_member", id)
                .and("is_deleted", 0)
                .limit(0, 1)
        );

        job.add ("item", item);

        VocAction.addTo (job);
        VocOrganizationParticipant.addTo(job);

    });}

    @Override
    public JsonObject getVocs() {
        return fetchData((db, job) -> {

            db.addJsonArrays(job,
                    ModelHolder.getModel().select(VocOrganizationParticipant.class, "*").orderBy("label")
            );
        });
    }

    @Override
    public JsonObject doCreate(JsonObject p, User user) {

        return doAction((db, job) -> {

            JsonObject data = p.getJsonObject("data");

            Object id = db.insertId(OrganizationMemberDocument.class, HASH(
                    "uuid_org_author", user.getUuidOrg(),
                    "uuid_org", data.getString("uuid_org"),
                    "participant", data.getInt("participant"),
                    "dt_from", data.getString("dt_from", null),
                    "uuid_org_member", data.getString("uuid_org_member", null),
                    "uuid_person_member", data.getString("uuid_person_member", null)
            ));

            job.add("id", id.toString());

            logAction(db, user, id, VocAction.i.CREATE);
        });
    }

    @Override
    protected void logAction(DB db, User user, Object id, VocAction.i action) throws SQLException {

        Table logTable = ModelHolder.getModel().getLogTable(getTable());

        if (logTable == null) {
            return;
        }

        String id_log = db.insertId(logTable, HASH(
                "action", action,
                "uuid_object", id,
                "uuid_user", user == null ? null : user.getId()
        )).toString();

        db.update(getTable(), HASH(
                "uuid", id,
                "id_log", id_log
        ));

//      publishMessage(action, id_log);
    }
}
