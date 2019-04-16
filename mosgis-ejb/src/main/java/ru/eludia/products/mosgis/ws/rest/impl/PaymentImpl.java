package ru.eludia.products.mosgis.ws.rest.impl;

import java.util.Map;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.Payment;
import ru.eludia.products.mosgis.db.model.tables.PaymentLog;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.Account;
import ru.eludia.products.mosgis.db.model.tables.AccountItem;
import ru.eludia.products.mosgis.db.model.tables.Acknowledgment;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.tables.PaymentDocument;
import ru.eludia.products.mosgis.db.model.tables.Premise;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.PaymentLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.ws.rest.impl.tools.ComplexSearch;
import ru.eludia.products.mosgis.ws.rest.impl.tools.Search;
import ru.eludia.products.mosgis.ws.rest.impl.tools.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class PaymentImpl extends BaseCRUD<Payment> implements PaymentLocal {

    @Resource (mappedName = "mosgis.inExportPaymentsQueue")
    Queue queue;

    @Override
    public Queue getQueue () {
        return queue;
    }

    @Override
    protected Queue getQueue(VocAction.i action) {

	switch (action) {
	    case APPROVE:
	    case ANNUL:
		return queue;
	    default:
		return null;
	}
    }

    private void filterOffDeleted (Select select) {
        select.and ("is_deleted", 0);
    }

    private void applyComplexSearch (final ComplexSearch search, Select select) {

        search.filter (select, "");

        if (!search.getFilters ().containsKey ("is_deleted")) filterOffDeleted (select);

    }

    private void applySimpleSearch (final SimpleSearch search, Select select) {

        filterOffDeleted (select);

        final String searchString = search.getSearchString ();

        if (searchString == null || searchString.isEmpty ()) return;

        select.and (Payment.c.ORDERNUM.lc () + " LIKE ?%", searchString.toUpperCase ());

    }

    private void applySearch (final Search search, Select select) {

        if (search instanceof SimpleSearch) {
            applySimpleSearch  ((SimpleSearch) search, select);
        }
        else if (search instanceof ComplexSearch) {
            applyComplexSearch ((ComplexSearch) search, select);
        }
        else {
            filterOffDeleted (select);
        }

    }

    private void checkFilter (JsonObject data, Payment.c field, Select select) {
        String key = field.lc ();
        String value = data.getString (key, null);
        if (value == null) return;
        select.and (field, value);
    }

    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {

        Select select = ModelHolder.getModel ().select (getTable (), "AS root", "*", "uuid AS id")
            .toMaybeOne (PaymentLog.class               ).on ()
            .toMaybeOne (OutSoap.class,         "err_text").on ()
	    .toOne(Account.class, "AS acct",
		 Account.c.ACCOUNTNUMBER.lc(),
		 Account.c.ID_CTR_STATUS.lc(),
		 Account.c.SERVICEID.lc()
	    ).on()
	    .toMaybeOne (PaymentDocument.class, "AS pd", PaymentDocument.c.PAYMENTDOCUMENTNUMBER.lc()).on()
            .toMaybeOne (VocOrganization.class, "AS org", "label").on ("acct.uuid_org=org.uuid")
            .toMaybeOne (VocOrganization.class, "AS org_customer", "label").on ("acct.uuid_org_customer=org_customer.uuid")
            .toMaybeOne (VocPerson.class,       "AS ind_customer", "label").on ("acct.uuid_person_customer=ind_customer.uuid")
            .orderBy ("root.year DESC")
            .orderBy ("root.month DESC")
            .orderBy ("root.ordernum")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        JsonObject data = p.getJsonObject ("data");

        checkFilter (data, Payment.c.UUID_ORG, select);

	checkFilter (data, Payment.c.UUID_ACCOUNT, select);

        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);

    });}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {

        final MosGisModel m = ModelHolder.getModel ();

        final JsonObject item = db.getJsonObject (m
            .get (getTable (), id, "AS root", "*")
            .toOne (Account.class, "AS acct"
                , Account.c.ACCOUNTNUMBER.lc ()
                , Account.c.ID_CTR_STATUS.lc ()
                , Account.c.SERVICEID.lc ()
            ).on ()
            .toMaybeOne (PaymentDocument.class, "AS pd"
                , PaymentDocument.c.PAYMENTDOCUMENTNUMBER.lc ()
                , PaymentDocument.c.ID_CTR_STATUS.lc ()
            ).on ()
            .toMaybeOne (PaymentLog.class, "AS log").on ()
            .toMaybeOne (OutSoap.class, "err_text").on ("log.uuid_out_soap=out_soap.uuid")
            .toOne      (VocOrganization.class, "AS org", "label").on ("root.uuid_org=org.uuid")
            .toMaybeOne (VocOrganization.class, "AS org_customer", "label").on ("acct.uuid_org_customer=org_customer.uuid")
            .toMaybeOne (VocPerson.class,       "AS ind_customer", "label").on ("acct.uuid_person_customer=ind_customer.uuid")
        );

        job.add ("item", item);

	db.addJsonArrays (job

            , m.select (AccountItem.class, "AS acct_items", "*", "uuid AS id")
                .toOne (VocBuilding.class, "AS addr", "label").on ()
                .toMaybeOne (Premise.class, "AS prem", "label", Premise.c.TOTALAREA.lc ()).on ()
                .toOne (House.class, "AS house", "uuid").on ("addr.houseguid=house.fiashouseguid")
                .where (AccountItem.c.UUID_ACCOUNT, item.getString(Payment.c.UUID_ACCOUNT.lc()))
                .where (EnTable.c.IS_DELETED, 0)
                .orderBy ("prem.label")
        );

	VocGisStatus.addLiteTo(job);
        VocAction.addTo (job);
    });}

    @Override
    public JsonObject getVocs () {

        JsonObjectBuilder jb = Json.createObjectBuilder ();

        VocGisStatus.addLiteTo(jb);
        VocAction.addTo (jb);

        return jb.build ();

    }

    @Override
    public JsonObject doApprove (String id, User user) {return doAction ((db) -> {

        db.update (getTable (), HASH (EnTable.c.UUID,               id,
            Payment.c.ID_CTR_STATUS, VocGisStatus.i.PENDING_RQ_PLACING.getId ()
        ));

        logAction (db, user, id, VocAction.i.APPROVE);

    });}

    @Override
    public JsonObject doAlter (String id, User user) {return doAction ((db) -> {

        final Map<String, Object> r = HASH (
            EnTable.c.UUID,               id,
            Payment.c.ID_CTR_STATUS,  VocGisStatus.i.PROJECT.getId ()
        );

        db.update (getTable (), r);

        logAction (db, user, id, VocAction.i.ALTER);

    });}
    
    @Override
    public JsonObject doAnnul (String id, JsonObject p, User user) {return doAction ((db) -> {

        final Map<String, Object> r = getData(p,
            EnTable.c.UUID,               id,
            Payment.c.ID_CTR_STATUS,  VocGisStatus.i.PENDING_RQ_ANNULMENT.getId ()
        );

        db.update (getTable (), r);

        logAction (db, user, id, VocAction.i.ANNUL);

    });}

    @Override
    public JsonObject getAcknowledgments (String id) {return fetchData ((db, job) -> {

        final MosGisModel m = ModelHolder.getModel ();

	db.addJsonArrays (job

            , m.select   (Acknowledgment.class, "AS root", "*", "uuid AS id")
                .toOne   (PaymentDocument.class, "AS pd", "*").on ()
                .where   (Acknowledgment.c.UUID_PAY, id)
                .where   (EnTable.c.IS_DELETED, 0)
                .orderBy ("pd.dt_period")

        );

    });}

}