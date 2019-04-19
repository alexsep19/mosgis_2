package ru.eludia.products.mosgis.ws.rest.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.db.model.tables.Acknowledgment;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.Account;
import ru.eludia.products.mosgis.db.model.tables.ActualBankAccount;
import ru.eludia.products.mosgis.db.model.tables.AnyChargeInfo;
import ru.eludia.products.mosgis.db.model.tables.ChargeInfo;
import ru.eludia.products.mosgis.db.model.tables.PaymentDocument;
import ru.eludia.products.mosgis.db.model.tables.Payment;
import ru.eludia.products.mosgis.db.model.tables.PenaltiesAndCourtCosts;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi329;
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
        
        final MosGisModel m = ModelHolder.getModel ();
        
        final JsonObject item = db.getJsonObject (m
                
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
                
        );

        job.add ("item", item);
        
        String uuidPayDoc = item.getString (Acknowledgment.c.UUID_PAY_DOC.lc ());
        
        db.addJsonArrays (job

            , m
            .select (AnyChargeInfo.class, "AS charges", "*")
            .toMaybeOne (ActualBankAccount.class, "AS ba", ActualBankAccount.c.LABEL.lc ()).on ("ba.uuid=charges." + ChargeInfo.c.UUID_BNK_ACCT.lc ())
            .toMaybeOne (VocOrganization.class, "AS org_bank_acct", "label").on ("ba.uuid_org=org_bank_acct.uuid")
            .where   (ChargeInfo.c.UUID_PAY_DOC, uuidPayDoc)
            .where   (ChargeInfo.c.TOTALPAYABLE.lc () + ">", 0)
            .orderBy ("charges." + ChargeInfo.c.ID_TYPE)
            .orderBy ("charges." + AnyChargeInfo.c.LABEL.lc ())

            , m
            .select (PenaltiesAndCourtCosts.class, "AS penalties", "*")
            .where  (PenaltiesAndCourtCosts.c.UUID_PAY_DOC, uuidPayDoc)
            .where  (PenaltiesAndCourtCosts.c.TOTALPAYABLE.lc () + ">", 0)
                
            , Nsi329.getVocSelect ()

        );        

        VocGisStatus.addTo (job);
        VocAction.addTo (job);

    });}
    
}