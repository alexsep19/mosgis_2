package ru.eludia.products.mosgis.rest.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import javax.json.JsonObject;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Operator;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.ActualBankAccount;
import ru.eludia.products.mosgis.db.model.tables.ActualSupplyResourceContract;
import ru.eludia.products.mosgis.db.model.tables.BankAccount;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractFile;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractFileLog;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractLog;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractSubject;
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi3;
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi239;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocBic;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocGisSupplyResourceContractCustomerType;
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

    @Resource(mappedName = "mosgis.inHouseSupplyResourceContractsQueue")
    Queue queue;

    @Override
    public Queue getQueue() {
	return queue;
    }

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
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {

        final MosGisModel m = ModelHolder.getModel ();

        final JsonObject item = db.getJsonObject (m
            .get (SupplyResourceContract.class, id, "*")
            .toOne(VocGisSupplyResourceContractCustomerType.class, "AS customer_type", "label").on()
            .toMaybeOne(VocOrganization.class, "AS org_customer", "label").on("uuid_org_customer")
            .toMaybeOne(VocPerson.class, "AS person_customer", "label").on("uuid_person_customer")
            .toMaybeOne(VocOrganization.class, "AS org", "label").on("uuid_org")
	    .toMaybeOne(SupplyResourceContractLog.class, "AS cpl").on()
	    .toMaybeOne(OutSoap.class, "uuid", "err_text").on("cpl.uuid_out_soap=out_soap.uuid")
            .toMaybeOne(BankAccount.class,     "AS bank_acct",        "*").on ()
            .toMaybeOne(VocBic.class,                                 "*").on ()
            .toMaybeOne(VocOrganization.class, "AS org_bank_acct","label").on ("bank_acct.uuid_org=org_bank_acct.uuid")
        );

        job.add ("item", item);
        ActualBankAccount.addTo (job, db, item.getString (SupplyResourceContract.c.UUID_ORG.lc ()));

	if (item.getInt(SupplyResourceContract.c.SPECQTYINDS.lc()) == VocGisContractDimension.i.BY_CONTRACT.getId()) {

	    String is_on_tab_temperature = db.getString(m.select(SupplyResourceContractSubject.class, "AS root", "uuid")
		.where(EnTable.c.IS_DELETED, 0)
		.and(SupplyResourceContractSubject.c.UUID_SR_CTR, id)
		.and(SupplyResourceContractSubject.c.UUID_SR_CTR_OBJ.lc() + " IS NULL")
		.and(SupplyResourceContractSubject.c.CODE_VC_NSI_239, VocNsi239.CODE_HEAT_ENERGY)
		.limit(0, 1)
	    );

	    job.add("is_on_tab_temperature", is_on_tab_temperature != null);
	}

	switch (VocGisStatus.i.forId(item.getInt(SupplyResourceContract.c.ID_CTR_STATUS.lc(), VocGisStatus.i.PROJECT.getId()))) {
	    case ANNUL:
	    case PENDING_RQ_ANNULMENT:
	    case FAILED_ANNULMENT:
		JsonObject lastAnnul = db.getJsonObject(m
		    .select(SupplyResourceContractLog.class, "AS root", "*")
		    .and("uuid_object", id)
		    .and("action", VocAction.i.ANNUL.getName())
		    .orderBy("root.ts DESC")
		    .toMaybeOne(OutSoap.class, "AS soap")
		    .on()
		);

		if (lastAnnul != null) {
		    job.add("last_annul", lastAnnul);
		}

		break;

	    case APPROVAL_PROCESS:
	    case PENDING_RQ_TERMINATE:
	    case TERMINATED:

		JsonObject lastTermination = db.getJsonObject(m
		    .select(SupplyResourceContractLog.class, "AS root", "*")
		    .and("uuid_object", id)
		    .and("action", VocAction.i.TERMINATE.getName())
		    .orderBy("root.ts DESC")
		);

		if (lastTermination != null) {
		    job.add("last_termination", lastTermination);
		}

		break;

	    default:
	}

	VocGisStatus.addLiteTo (job);
        VocAction.addTo (job);
        VocGisContractDimension.addTo(job);
        VocSupplyResourceContractFileType.addTo(job);
        db.addJsonArrays(job,
	    NsiTable.getNsiTable(54).getVocSelect().where("isactual", 1),
	    NsiTable.getNsiTable(58).getVocSelect().where(IS_RS_CTR_NSI_58, 1),
	    VocNsi3.getVocSelect(),
	    VocNsi239.getVocSelect()
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

    @Override
    protected void publishMessage (VocAction.i action, String id_log) {

        switch (action) {
            case APPROVE:
	    case ANNUL:
	    case TERMINATE:
                super.publishMessage (action, id_log);
            default:
                return;
        }

    }

    @Override
    public JsonObject doApprove(String id, User user) {
	return doAction((db) -> {

	    db.update(getTable(), HASH(
		EnTable.c.UUID, id,
		SupplyResourceContract.c.ID_CTR_STATUS, VocGisStatus.i.PENDING_RQ_PLACING.getId()
	    ));

	    logAction(db, user, id, VocAction.i.APPROVE);

	    List<UUID> ids = new ArrayList<>();
	    db.forEach(db.getModel().select(SupplyResourceContractFile.class, "uuid")
		.where(SupplyResourceContractFile.c.UUID_SR_CTR.lc(), id).and(EnTable.c.IS_DELETED.lc(), 0)
		, (rs) -> {
		final Object u = db.getValue(rs, 1);
		if (u != null) {
		    ids.add((UUID) u);
		}
	    });

	    for (UUID idFile : ids) {

		String idFileLog = db.insertId(SupplyResourceContractFileLog.class, HASH(
		    "action", VocAction.i.APPROVE.getName(),
		    "uuid_object", idFile,
		    "uuid_user", user == null ? null : user.getId()
		)).toString();

		db.update(SupplyResourceContractFile.class, HASH(
		    "uuid", idFile,
		    AttachTable.c.ATTACHMENTGUID, null,
		    "id_log", idFileLog
		));

	    }

	});
    }

    @Override
    public JsonObject doAlter(String id, JsonObject p, User user) {
	return doAction((db) -> {

	    final Map<String, Object> r = HASH(
		EnTable.c.UUID, id,
		SupplyResourceContract.c.ID_CTR_STATUS, VocGisStatus.i.PROJECT.getId()
	    );

	    db.update(getTable(), r);

	    logAction(db, user, id, VocAction.i.ALTER);
	});
    }

    @Override
    public JsonObject doAnnul(String id, JsonObject p, User user) {
	return doAction((db) -> {

	    db.update(getTable(), getData(p,
		EnTable.c.UUID, id,
		SupplyResourceContract.c.ID_CTR_STATUS, VocGisStatus.i.PENDING_RQ_ANNULMENT.getId(),
		SupplyResourceContract.c.REASONOFANNULMENT, p.getJsonObject("data").getString(SupplyResourceContract.c.REASONOFANNULMENT.lc())
	    ));

	    logAction(db, user, id, VocAction.i.ANNUL);

	});
    }

    @Override
    public JsonObject doTerminate(String id, JsonObject p, User user) {
	return doAction((db) -> {

	    db.update(getTable(), getData(p,
		"uuid", id,
		"id_ctr_status", VocGisStatus.i.PENDING_RQ_TERMINATE.getId()
	    ));

	    logAction(db, user, id, VocAction.i.TERMINATE);

	});
    }

}