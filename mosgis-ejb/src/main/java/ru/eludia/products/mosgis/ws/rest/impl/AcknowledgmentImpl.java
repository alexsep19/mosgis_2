package ru.eludia.products.mosgis.ws.rest.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.db.model.tables.Acknowledgment;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.tables.Account;
import ru.eludia.products.mosgis.db.model.tables.PaymentDocument;
import ru.eludia.products.mosgis.db.model.tables.Payment;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.AcknowledgmentLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class AcknowledgmentImpl extends BaseCRUD<Acknowledgment> implements AcknowledgmentLocal {
    
    @Override
    public JsonObject select (JsonObject p, User user) {return EMPTY_JSON_OBJECT;}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {

        job.add ("item", db.getJsonObject (ModelHolder.getModel ()

            .get   (getTable (), id, "AS root", "*")

            .toOne (PaymentDocument.class, "AS pd"
                , PaymentDocument.c.ID_CTR_STATUS.lc ()
                , PaymentDocument.c.PAYMENTDOCUMENTNUMBER.lc ()
                , PaymentDocument.c.DT_PERIOD.lc ()
                , PaymentDocument.c.AMOUNT_ACK.lc ()
                , PaymentDocument.c.AMOUNT_NACK.lc ()
            ).on ()

            .toOne (Account.class, "AS acct"
                , Account.c.ID_CTR_STATUS.lc ()
                , Account.c.ACCOUNTNUMBER.lc ()
            ).on ()

            .toOne (Payment.class, "AS pay"
                , Payment.c.ID_CTR_STATUS.lc ()
                , Payment.c.ORDERNUM.lc ()
                , Payment.c.ORDERDATE.lc ()
                , Payment.c.DT_PERIOD.lc ()
                , Payment.c.AMOUNT.lc ()
                , Payment.c.UUID_ACCOUNT.lc ()
                , Payment.c.AMOUNT_ACK.lc ()
                , Payment.c.AMOUNT_NACK.lc ()
                , Payment.c.UUID_ORG.lc () + " AS uuid_org"
            ).on ()

            .toMaybeOne (VocOrganization.class, "AS org_customer"
                , VocOrganization.c.LABEL.lc ()
            ).on ("acct.uuid_org_customer=org_customer.uuid")

            .toMaybeOne (VocPerson.class, "AS ind_customer"
                , VocPerson.c.LABEL.lc ()
            ).on ("acct.uuid_person_customer=ind_customer.uuid")

        ));

        VocGisStatus.addTo (job);

    });}

}