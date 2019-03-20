package ru.eludia.products.mosgis.ws.rest.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.PaymentDocument;
import ru.eludia.products.mosgis.db.model.tables.PaymentDocumentLog;
import ru.eludia.products.mosgis.db.model.tables.Charter;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract;
import ru.eludia.products.mosgis.db.model.voc.VocPaymentDocumentType;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.tables.Account;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.PaymentDocumentLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.ws.rest.impl.tools.ComplexSearch;
import ru.eludia.products.mosgis.ws.rest.impl.tools.Search;
import ru.eludia.products.mosgis.ws.rest.impl.tools.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class PaymentDocumentImpl extends BaseCRUD<PaymentDocument> implements PaymentDocumentLocal {
/*
    @Resource (mappedName = "mosgis.inPaymentDocumentsQueue")
    Queue queue;

    @Override
    public Queue getQueue () {
        return queue;
    }
*/
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

        select.and (PaymentDocument.c.PAYMENTDOCUMENTNUMBER.lc () + " LIKE ?%", searchString.toUpperCase ());
        
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
    
    private void checkFilter (JsonObject data, PaymentDocument.c field, Select select) {
        String key = field.lc ();
        String value = data.getString (key, null);
        if (value == null) return;
        select.and (field, value);
    }

    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {                

        Select select = ModelHolder.getModel ().select (getTable (), "AS root", "*", "uuid AS id")
            .toMaybeOne (PaymentDocumentLog.class               ).on ()
            .toMaybeOne (OutSoap.class,         "err_text").on ()
            .toOne      (Account.class,         "AS acct", Account.c.ACCOUNTNUMBER.lc ()).on ()                
            .toMaybeOne (VocOrganization.class, "AS org", "label").on ("acct.uuid_org_customer=org.uuid")
            .toMaybeOne (VocPerson.class,       "AS ind", "label").on ("acct.uuid_person_customer=ind.uuid")
            .orderBy ("root.year DESC")
            .orderBy ("root.month DESC")
            .orderBy ("root.id_type")
            .orderBy ("root.paymentdocumentnumber")
            .limit (p.getInt ("offset"), p.getInt ("limit"));
        
        JsonObject data = p.getJsonObject ("data");
        
        checkFilter (data, PaymentDocument.c.UUID_ORG, select);
        checkFilter (data, PaymentDocument.c.UUID_ACCOUNT, select);
        
        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);

    });}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {

        job.add ("item", db.getJsonObject (ModelHolder.getModel ()
            .get (getTable (), id, "AS root", "*")
            .toOne      (Account.class, "AS acct", Account.c.ACCOUNTNUMBER.lc ()).on ()
            .toMaybeOne (Contract.class, "AS ca", "*").on ()
            .toMaybeOne (Charter.class, "AS ch", "*").on ()
            .toMaybeOne (SupplyResourceContract.class, "AS sr_ctr", "*").on ()
            .toMaybeOne (PaymentDocumentLog.class, "AS log").on ()
            .toMaybeOne (OutSoap.class, "err_text").on ("log.uuid_out_soap=out_soap.uuid")
            .toOne      (VocOrganization.class, "AS org", "label").on ("root.uuid_org=org.uuid")
            .toMaybeOne (VocOrganization.class, "AS org_customer", "label").on ("acct.uuid_org_customer=org_customer.uuid")
            .toMaybeOne (VocPerson.class,       "AS ind_customer", "label").on ("acct.uuid_person_customer=ind_customer.uuid")
        ));
        
        VocPaymentDocumentType.addTo (job);
        VocGisStatus.addTo (job);
        VocAction.addTo (job);

    });}

    @Override
    public JsonObject getVocs () {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        VocPaymentDocumentType.addTo (jb);
        VocAction.addTo (jb);
        
        return jb.build ();
        
    }
/*    
    @Override
    public JsonObject doApprove (String id, User user) {return doAction ((db) -> {

        db.update (getTable (), HASH (EnTable.c.UUID,               id,
            PaymentDocument.c.ID_CTR_STATUS, VocGisStatus.i.PENDING_RQ_PLACING.getId ()
        ));

        logAction (db, user, id, VocAction.i.APPROVE);

    });}
    
    @Override
    public JsonObject doAlter (String id, User user) {return doAction ((db) -> {
                
        final Map<String, Object> r = HASH (
            EnTable.c.UUID,               id,
            PaymentDocument.c.ID_CTR_STATUS,  VocGisStatus.i.PROJECT.getId ()
        );
                
        db.update (getTable (), r);
        
        logAction (db, user, id, VocAction.i.ALTER);
        
    });}    
*/    
}