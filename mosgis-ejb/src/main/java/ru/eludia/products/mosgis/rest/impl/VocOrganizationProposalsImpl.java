package ru.eludia.products.mosgis.rest.impl;

import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.rest.api.VocOrganizationProposalsLocal;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocBuildingAddress;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationProposal;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationTypes;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;

@Stateless
public class VocOrganizationProposalsImpl extends BaseCRUD<VocOrganizationProposal> implements VocOrganizationProposalsLocal {

    private static final Logger logger = Logger.getLogger (VocOrganizationProposalsImpl.class.getName ());

    private final static String DEFAULT_SEARCH = "label_uc LIKE %?%";

    private static final Pattern RE = Pattern.compile ("(\\d+)\\s*(\\d{2,9})?");

    private void applyKpp (final String kpp, Select select) {

        if (kpp == null || kpp.isEmpty ()) return;

        if (kpp.length () == 9) {
            select.and ("kpp", kpp);
        }
        else {

            StringBuilder sbFrom = new StringBuilder (kpp);
            StringBuilder sbTo   = new StringBuilder (kpp);

            for (int i = kpp.length (); i < 9; i ++) {
                sbFrom.append ('0');
                sbTo.append   ('9');
            }

            select.and ("kpp BETWEEN ", sbFrom.toString (), sbTo.toString ());

        }

    }

    private void applySimpleSearch (final SimpleSearch search, Select select) {

        final String searchString = search.getSearchString ();

        Matcher matcher = RE.matcher (searchString);

        if (matcher.matches ()) {

            String term = matcher.group (1);
            String kpp = matcher.group (2);

            switch (term.length ()) {

                case 10:
                    applyKpp (kpp, select);
                case 12:
                    select.and ("inn", term);
                    break;

                case 13:
                    applyKpp (kpp, select);
                case 15:
                    select.and ("ogrn", term);
                    break;

                default:
                    select.and ("uuid IS NULL");

            }

        }
        else {
            select.and (DEFAULT_SEARCH, searchString.toUpperCase ());
        }

    }

    private void applyComplexSearch (final ComplexSearch search, Select select) {
        search.filter (select, "");
    }

    private void applySearch (final Search search, Select select) {

        if (search == null) {
//            select.and ("uuid IS NULL");
        }
        else if (search instanceof ComplexSearch) {
            applyComplexSearch ((ComplexSearch) search, select);
        }
        else if (search instanceof SimpleSearch) {
            applySimpleSearch  ((SimpleSearch) search, select);
        }

    }

    @Override
    public JsonObject select (JsonObject p) {

        Select select = ModelHolder.getModel ().select (VocOrganizationProposal.class, "*", "uuid AS id")
            .where("is_deleted", 0)
            .orderBy ("label")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        applySearch (Search.from (p), select);

        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {
            db.addJsonArrayCnt (jb, select);
        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }

        return jb.build ();

    }

    @Override
    public JsonObject getItem (String id) {

        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {

            JsonObject item = db.getJsonObject (ModelHolder.getModel ()
                .get (VocOrganizationProposal.class, id, "AS root", "*")
                .toMaybeOne (VocOrganizationTypes.class, "label").on ()
                .toMaybeOne (VocOrganization.class, "AS parent_org", "label").on ("parent")
                .toMaybeOne(VocBuildingAddress.class, "AS vc_build_address", "label").on("fiashouseguid")
                .toMaybeOne(VocGisStatus.class, "AS status", "label").on("id_org_pr_status")
//                .toMaybeOne (VocOrganizationProposalLog.class).on ()
//                .toMaybeOne (OutSoap.class, "id_status").on ()
            );

            jb.add ("item", item);

        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }

        return jb.build ();

    }

    @Override
    public JsonObject doCreate(JsonObject p, User user) {

        return doAction((db, job) -> {

            JsonObject data = p.getJsonObject("data");

            Object id = db.insertId(VocOrganizationProposal.class, HASH(
                    "parent", data.getString("uuid_org_parent"),
                    "id_type", data.getString("id_type"),
                    "fullname", data.getString("fullname"),
                    "shortname", data.getString("shortname"),
                    "ogrn", data.getString("ogrn"),
                    "stateregistrationdate", data.getString("stateregistrationdate"),
                    "inn", data.getString("inn"),
                    "kpp", data.getString("kpp"),
                    "okopf", data.getString("okopf", null),
                    "address", data.getString("address", null),
                    "fiashouseguid", data.getString("fiashouseguid", null),
                    "info_source", data.getString("info_source", null),
                    "dt_info_source", data.getString("dt_info_source", null)
            ));

            job.add("id", id.toString());

            logAction(db, user, id, VocAction.i.CREATE);
        });
    }

    @Override
    public JsonObject doUpdate(String id, JsonObject p, User user) {

        return doAction((db, job) -> {

            JsonObject data = p.getJsonObject("data");

            db.update(VocOrganizationProposal.class, HASH(
                    "uuid", id,
                    "fullname", data.getString("fullname"),
                    "shortname", data.getString("shortname"),
                    "ogrn", data.getString("ogrn"),
                    "stateregistrationdate", data.getString("stateregistrationdate"),
                    "inn", data.getString("inn"),
                    "kpp", data.getString("kpp"),
                    "okopf", data.getString("okopf", null),
                    "address", data.getString("address", null),
                    "fiashouseguid", data.getString("fiashouseguid", null),
                    "activityenddate", data.getString("activityenddate"),
                    "info_source", data.getString("info_source", null),
                    "dt_info_source", data.getString("dt_info_source", null)
            ));

            job.add("id", id);

            logAction(db, user, id, VocAction.i.UPDATE);
        });
    }

    @Override
    public JsonObject doDelete(String id, User user) {
        return doAction((db) -> {

            db.update(getTable(), HASH(
                    "uuid", id,
                    "is_deleted", 1
            ));

        });
    }

    @Override
    protected void logAction (DB db, User user, Object id, VocAction.i action) throws SQLException {

        Table logTable = ModelHolder.getModel ().getLogTable (getTable ());

        if (logTable == null) return;

        String id_log = db.insertId (logTable, HASH (
            "action", action,
            "uuid_object", id,
            "uuid_user", user == null ? null : user.getId ()
        )).toString ();

        db.update (getTable (), HASH (
            "uuid", id,
            "id_log",    id_log
        ));

//      publishMessage(action, id_log);
    }

    @Override
    public JsonObject select (JsonObject p, User user) {
        throw new UnsupportedOperationException ("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}