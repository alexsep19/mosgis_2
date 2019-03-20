package ru.eludia.products.mosgis.ws.rest.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Map;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.WebApplicationException;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Operator;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.rest.api.LegalActLocal;
import ru.eludia.products.mosgis.db.model.tables.LegalAct;
import ru.eludia.products.mosgis.db.model.tables.LegalActLog;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocFileStatus;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.LegalActOktmo;
import ru.eludia.products.mosgis.db.model.voc.VocLegalActLevel;
import ru.eludia.products.mosgis.db.model.voc.VocOktmo;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi237;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi324;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.ws.rest.impl.tools.ComplexSearch;
import ru.eludia.products.mosgis.ws.rest.impl.tools.Search;
import ru.eludia.products.mosgis.ws.rest.impl.tools.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class LegalActImpl extends BaseCRUD<LegalAct> implements LegalActLocal  {

    private static final Logger logger = Logger.getLogger (LegalActImpl.class.getName ());

    @Override
    public JsonObject doCreate (JsonObject p, User user) {return fetchData ((db, job) -> {

        JsonObject file = p.getJsonObject ("file");

        db.begin ();

            final Map<String, Object> h = ((AttachTable) getTable ()).HASH (file);

            Object id;

            if (file.containsKey ("uuid")) {
                id = file.getString ("uuid");
                h.put (AttachTable.c.BODY.lc (), null);
                db.update (getTable (), h);
                logAction (db, user, id, VocAction.i.UPDATE);
            }
            else {
                id = db.insertId (getTable (), h);
                logAction (db, user, id, VocAction.i.CREATE);
            }

	    if (file.containsKey ("oktmo")) {
		LegalActOktmo.store(db, id, file.getJsonArray("oktmo"));
	    }

            job.add ("id", id.toString ());

        db.commit ();

    });}

    @Override
    public JsonObject doUpdate (String id, JsonObject p, User user) {return doAction (db -> {

        byte [] bytes = Base64.getDecoder ().decode (p.getString ("chunk"));

        Connection cn = db.getConnection ();

        final LegalAct table = (LegalAct) getTable ();

        final String uuid = id.replace ("-", "").toUpperCase ();

        try (PreparedStatement st = cn.prepareStatement ("SELECT body, DBMS_LOB.GETLENGTH(body) FROM " + table.getName () + " WHERE uuid = ? FOR UPDATE")) {

            st.setString (1, uuid);

            try (ResultSet rs = st.executeQuery ()) {

                if (rs.next ()) {

                    Blob blob = rs.getBlob (1);
                    long  len = rs.getLong (2);

                    try (OutputStream os = blob.setBinaryStream (len + 1)) {
                        os.write (bytes);
                    }

                }

            }

            db.update (getTable (), HASH (
                "uuid",      uuid,
                "id_status", 0
            ));

        }

    });}

    @Override
    public JsonObject doEdit(String id, JsonObject p, User user) {return doAction(db -> {

	JsonObject data = p.getJsonObject("data");

	Map<String, Object> r = ((AttachTable) getTable()).HASH(data, "uuid", id);

	r.put("uuid", id);

	db.update(LegalAct.class, r);

	if (data.containsKey("oktmo")) {
	    LegalActOktmo.store(db, id, data.getJsonArray("oktmo"));
	}

	logAction(db, user, id, VocAction.i.UPDATE);
    });}


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
            "uuid",      id,
            "id_log",    id_log
        ));

        publishMessage (action, id_log);

    }

    @Override
    public JsonObject doDelete (String id, User user) {return doAction (db -> {

        db.update (LegalAct.class, HASH (
            "uuid",      id,
            "id_status", 2
        ));

        logAction (db, user, id, VocAction.i.DELETE);
    });}

    @Override
    public void download (String id, OutputStream out) throws IOException, WebApplicationException {fetchData ((db, job) -> {
	db.getStream (getTable (), id, "body", out);
    });}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData((db, job) -> {

	final MosGisModel m = ModelHolder.getModel();

	final JsonObject item = db.getJsonObject(m
	    .get(getTable(), id, "*")
	    .toMaybeOne(VocLegalActLevel.class, "AS vc_legal_act_level").on()
	    .toMaybeOne(VocOrganization.class, "AS org", "label").on("tb_legal_acts.uuid_org = org.uuid")
	    .toOne(LegalActLog.class, "AS log").on()
	    .toMaybeOne(OutSoap.class, "AS soap", "*").on("log.uuid_out_soap=soap.uuid")
	);
	job.add("item", item);

	final JsonArray oktmo = db.getJsonArray(m
	    .select(VocOktmo.class, "AS vc_oktmo", "*")
	    .where(VocOktmo.c.ID.lc(), m
		.select(LegalActOktmo.class, "oktmo").where("uuid", id)
	    )
	    .orderBy("vc_oktmo.code")
	);

	job.add("legal_act_oktmo", oktmo);

    }); }

    @Override
    public JsonObject select (JsonObject p, User u) {return fetchData ((db, job) -> {

        Select select = ModelHolder.getModel ()
            .select  (getTable (), "AS root", "*")
	    .toMaybeOne(VocLegalActLevel.class, "AS vc_legal_act_level").on()
	    .toMaybeOne(VocOrganization.class, "AS org", "label").on("root.uuid_org = org.uuid")
            .toOne   (LegalActLog.class, "AS log").on ()
            .toMaybeOne (OutSoap.class, "AS soap", "*").on ("log.uuid_out_soap=soap.uuid")
            .where   ("id_status", VocFileStatus.i.LOADED.getId ())
            .orderBy ("root.approvedate DESC")
        ;

	JsonObject data = p.getJsonObject("data");

	String k = LegalAct.c.UUID_ORG.lc();
	String v = data.getString(k, null);
	if (DB.ok(v)) {
	    select.and(k, v);
	}

	applySearch(Search.from(p), select);

        db.addJsonArrays (job, select);
    });}

    private void filterOffDeleted(Select select) {
	select.and(EnTable.c.IS_DELETED, Operator.EQ, 0);
    }

    private void applyComplexSearch(final ComplexSearch search, Select select) {

	search.filter(select, "");
	if (!search.getFilters().containsKey("is_deleted")) {
	    filterOffDeleted(select);
	}

    }

    private void applySimpleSearch(final SimpleSearch search, Select select) {

	final String s = search.getSearchString();

	if (s != null) {
	    final String q = s.toUpperCase().replace(' ', '%');
	    select.and("docnumber LIKE ?%", q);
	}
    }

    private void applySearch(final Search search, Select select) {

	if (search instanceof ComplexSearch) {
	    applyComplexSearch((ComplexSearch) search, select);
	} else {
	    if (search instanceof SimpleSearch) {
		applySimpleSearch((SimpleSearch) search, select);
	    }
	    filterOffDeleted(select);
	}
    }

    @Override
    public JsonObject getVocs() { return fetchData((db, job) -> {
	Nsi237.addTo(job, db);
	Nsi324.addTo(job, db);
	VocLegalActLevel.addTo(job);
	VocAction.addTo(job);
	VocGisStatus.addTo(job);
    });}
}