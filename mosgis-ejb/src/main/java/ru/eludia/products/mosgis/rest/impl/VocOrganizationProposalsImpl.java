package ru.eludia.products.mosgis.rest.impl;

import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.Queue;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.rest.api.VocOrganizationProposalsLocal;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocBuildingAddress;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationProposal;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationProposal.c;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationTypes;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOksm;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;

@Stateless
public class VocOrganizationProposalsImpl extends BaseCRUD<VocOrganizationProposal> implements VocOrganizationProposalsLocal {

    @Resource (mappedName = "mosgis.inVocOrganizationProposalsQueue")
    Queue queue;

    @Override
    public Queue getQueue () {
        return queue;
    }
    
    @Override
    protected void publishMessage (VocAction.i action, String id_log) {
        
        switch (action) {
            case APPROVE:
                super.publishMessage (action, id_log);
            default:
                return;
        }
        
    }
    
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
    public JsonObject select (JsonObject p, User user) {

        Select select = ModelHolder.getModel ().select (VocOrganizationProposal.class, "*", "uuid AS id")
            .where ("is_deleted", 0)
            .and (c.UUID_ORG_OWNER, user.getUuidOrg ())
            .orderBy ("accreditationstartdate DESC, stateregistrationdate DESC")
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
                .toMaybeOne (VocBuildingAddress.class, "AS vc_build_address", "label").on("fiashouseguid")
                .toMaybeOne (VocOksm.class, "*").on("registrationcountry")
                .toMaybeOne (VocGisStatus.class, "AS status", "label").on("id_org_pr_status")
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
    public JsonObject getVocs() { return fetchData((db, job) -> {

            db.addJsonArrays(job,
                ModelHolder.getModel().select(VocOrganizationTypes.class, "*").where("id IN", 2, 3).orderBy("label")
            );
    }); }

    @Override
    public JsonObject doCreate(JsonObject p, User user) {

        return doAction((db, job) -> {

            JsonObject data = p.getJsonObject ("data");

            Map<String, Object> r = HASH (
                c.UUID_ORG_OWNER, user.getUuidOrg (),
                c.ID_TYPE, data.getString("id_type"),
                c.FULLNAME, data.getString("fullname"),
                c.SHORTNAME, data.getString("shortname"),
                c.INN, data.getString("inn"),
                c.KPP, data.getString("kpp"),
                c.ADDRESS, data.getString("address", null)
            );

            if (data.getString("id_type").equals ("2")) { // Обособленное подразделение
                r.put("fiashouseguid", data.getString("fiashouseguid"));
                r.put("parent", data.getString("uuid_org_parent"));
                r.put("stateregistrationdate", data.getString("stateregistrationdate"));
                r.put("ogrn", data.getString("ogrn"));
                r.put("okopf", data.getString("okopf", null));
                r.put("info_source", data.getString("info_source", null));
                r.put("dt_info_source", data.getString("dt_info_source", null));
            }

            if (data.getString("id_type").equals("3")) { // ФПИЮЛ
                r.put("fiashouseguid", data.getString("fiashouseguid", null));
                r.put("nza", data.getString("nza"));
                r.put("accreditationstartdate", data.getString("accreditationstartdate"));
                r.put("accreditationenddate", data.getString("accreditationenddate", null));
                r.put("registrationcountry", data.getString("registrationcountry"));
            }

            Object id = db.insertId(VocOrganizationProposal.class, r);

            job.add("id", id.toString());

            logAction(db, user, id, VocAction.i.CREATE);
        });
    }

    @Override
    public JsonObject doUpdate(String id, JsonObject p, User user) {

        return doAction((db, job) -> {

            JsonObject data = p.getJsonObject("data");

            Map<String, Object> r = HASH(
                "uuid", id,
                "fullname", data.getString("fullname"),
                "shortname", data.getString("shortname"),
                "inn", data.getString("inn"),
                "kpp", data.getString("kpp"),
                "address", data.getString("address", null),
                "activityenddate", data.getString("activityenddate", null)
            );

            if (data.getString("id_type").equals("2")) { // Обособленное подразделение
                r.put("fiashouseguid", data.getString("fiashouseguid", null));
                r.put("stateregistrationdate", data.getString("stateregistrationdate"));
                r.put("ogrn", data.getString("ogrn"));
                r.put("okopf", data.getString("okopf", null));
                r.put("info_source", data.getString("info_source", null));
                r.put("dt_info_source", data.getString("dt_info_source", null));
            }

            if (data.getString("id_type").equals("3")) { // ФПИЮЛ
                r.put("fiashouseguid", data.getString("fiashouseguid"));
                r.put("nza", data.getString("nza"));
                r.put("accreditationstartdate", data.getString("accreditationstartdate"));
                r.put("accreditationenddate", data.getString("accreditationenddate", null));
                r.put("registrationcountry", data.getString("registrationcountry"));
            }

            db.update(VocOrganizationProposal.class, r);

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
    public JsonObject doApprove (String id, User user) {return doAction ((db) -> {

        db.update (getTable (), HASH (EnTable.c.UUID, id,
            VocOrganizationProposal.c.ID_ORG_PR_STATUS, VocGisStatus.i.PENDING_RQ_PLACING.getId ()
        ));

        logAction (db, user, id, VocAction.i.APPROVE);

    });}

}