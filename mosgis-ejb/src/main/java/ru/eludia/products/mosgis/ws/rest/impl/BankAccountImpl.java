package ru.eludia.products.mosgis.ws.rest.impl;

import java.util.Map;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import javax.json.JsonObject;
import javax.ws.rs.InternalServerErrorException;
import static ru.eludia.base.DB.HASH;
import ru.eludia.products.mosgis.db.model.tables.ActualBankAccount;
import ru.eludia.products.mosgis.db.model.tables.BankAccount;
import ru.eludia.products.mosgis.db.model.tables.BankAccountLog;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocBic;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.BankAccountLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class BankAccountImpl extends BaseCRUD<BankAccount> implements BankAccountLocal {

    @Resource (mappedName = "mosgis.inExportBankAccountQueue")
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
	    case TERMINATE:
		return queue;
	    default:
		return null;
	}
    }

    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {                

        JsonObject data = p.getJsonObject ("data");
        if (data == null) throw new InternalServerErrorException ("JSON data not set");
                        
        final String kUuidOrg = BankAccount.c.UUID_ORG.lc ();                
        
        String uuidOrg = data.getString (kUuidOrg, null);
        if (data == null) throw new InternalServerErrorException ("uuid_org data not set");

        db.addJsonArrayCnt (job,
            ActualBankAccount.select (uuidOrg)
            .limit (p.getInt ("offset"), p.getInt ("limit"))
        );

    });}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {

        job.add ("item", db.getJsonObject (ModelHolder.getModel ()
            .get (BankAccount.class, id, "AS root", "*")
	    .toMaybeOne(VocOrganization.class, "AS org", "uuid", "label").on("root.uuid_org = org.uuid")
	    .toMaybeOne(VocOrganization.class, "AS cred_org", "uuid", "label").on("root.uuid_cred_org = cred_org.uuid")
	    .toMaybeOne(VocBic.class, "AS bank", "*").on()
	    .toOne(BankAccountLog.class, "AS log").on()
	    .toMaybeOne(OutSoap.class, "AS soap", "*").on("log.uuid_out_soap=soap.uuid")
        ));
        
        VocGisStatus.addTo (job);
        VocAction.addTo (job);

    });}

    @Override
    public JsonObject doApprove (String id, User user) {return doAction ((db) -> {

        db.update (getTable (), HASH (EnTable.c.UUID,               id,
            BankAccount.c.ID_CTR_STATUS, VocGisStatus.i.PENDING_RQ_PLACING.getId ()
        ));

        logAction (db, user, id, VocAction.i.APPROVE);

    });}

    @Override
    public JsonObject doAlter (String id, User user) {return doAction ((db) -> {

        final Map<String, Object> r = HASH (
            EnTable.c.UUID,               id,
            BankAccount.c.ID_CTR_STATUS,  VocGisStatus.i.PROJECT.getId ()
        );

        db.update (getTable (), r);

        logAction (db, user, id, VocAction.i.ALTER);

    });}


    @Override
    public JsonObject doTerminate(String id, JsonObject p, User user) { return doAction((db) -> {

	db.update(BankAccount.class, getData(p,
	    "uuid", id,
	    "id_ctr_status", VocGisStatus.i.PENDING_RQ_TERMINATE.getId()
	));

	logAction(db, user, id, VocAction.i.TERMINATE);

    });}

    @Override
    public JsonObject doAnnul (String id, JsonObject p, User user) {return doAction ((db) -> {

        final Map<String, Object> r = getData(p,
            EnTable.c.UUID,               id,
            BankAccount.c.ID_CTR_STATUS,  VocGisStatus.i.PENDING_RQ_ANNULMENT.getId ()
        );

        db.update (getTable (), r);

        logAction (db, user, id, VocAction.i.ANNUL);

    });}
}