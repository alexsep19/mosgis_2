package ru.eludia.products.mosgis.ws.rest.impl;

import java.util.Map;
import java.util.UUID;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import javax.json.JsonObject;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.tables.Acknowledgment;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.Account;
import ru.eludia.products.mosgis.db.model.tables.AcknowledgmentItem;
import ru.eludia.products.mosgis.db.model.tables.AcknowledgmentLog;
import ru.eludia.products.mosgis.db.model.tables.AnyChargeInfo;
import ru.eludia.products.mosgis.db.model.tables.ChargeInfo;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
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
    
    @Resource (mappedName = "mosgis.inExportAcknowledgmentsQueue")
    Queue queue;

    @Override
    public Queue getQueue () {
        return queue;
    }
    
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
                , PaymentDocument.c.TOTALPAYABLEBYPDWITH_DA.lc ()
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
                
            .toMaybeOne (AcknowledgmentLog.class).on ()
            .toMaybeOne (OutSoap.class, "err_text").on ()
                
        );

        job.add ("item", item);
        
        String uuidPayDoc = item.getString (Acknowledgment.c.UUID_PAY_DOC.lc ());
        
        db.addJsonArrays (job
                
            , m
            .select (AcknowledgmentItem.class, "AS items", "*")
            .where  (AcknowledgmentItem.c.UUID_ACK, id)
            .where  (EnTable.c.IS_DELETED, 0)

            , m
            .select  (AnyChargeInfo.class, "AS charges", "*")
            .where   (ChargeInfo.c.UUID_PAY_DOC, uuidPayDoc)
            .where   (ChargeInfo.c.TOTALPAYABLE.lc () + ">", 0)
            .where   (EnTable.c.IS_DELETED, 0)
            .orderBy ("charges." + ChargeInfo.c.ID_TYPE)
            .orderBy ("charges." + AnyChargeInfo.c.LABEL.lc ())

            , m
            .select (PenaltiesAndCourtCosts.class, "AS penalties", "*")
            .where  (PenaltiesAndCourtCosts.c.UUID_PAY_DOC, uuidPayDoc)
            .where  (PenaltiesAndCourtCosts.c.TOTALPAYABLE.lc () + ">", 0)
            .where  (EnTable.c.IS_DELETED, 0)
            .orderBy ("penalties." + PenaltiesAndCourtCosts.c.CODE_VC_NSI_329)
            .orderBy ("penalties." + PenaltiesAndCourtCosts.c.CAUSE)

            , Nsi329.getVocSelect ()

        );        

        VocGisStatus.addTo (job);
        VocAction.addTo (job);

    });}

    @Override
    public JsonObject doPatch (String id, JsonObject p, User user) {return doAction ((db, job) -> {

        final MosGisModel m = ModelHolder.getModel ();

        Table t = m.get (AcknowledgmentItem.class);
        
        final Map<String, Object> data = t.HASH (p.getJsonObject ("data"),
            AcknowledgmentItem.c.UUID_ACK, id
        );
        
        boolean isCharge = DB.ok (data.get (AcknowledgmentItem.c.UUID_CHARGE.lc ()));

        AcknowledgmentItem.c key = isCharge ? AcknowledgmentItem.c.UUID_CHARGE : AcknowledgmentItem.c.UUID_PENALTY;

        UUID uuidLine = UUID.fromString (data.get (key.lc ()).toString ()) ;

        String itemId = db.upsertId (t, data, AcknowledgmentItem.c.UUID_ACK.lc (), key.lc ());
        
        m.createIdLog (db, getTable (), user, id, VocAction.i.UPDATE);

        job.add ("item", db.getJsonObject (m
        
            .get   (getTable (), id, "AS root", "*")

            .toOne (PaymentDocument.class, "AS pd"
                , PaymentDocument.c.AMOUNT_ACK.lc ()
                , PaymentDocument.c.AMOUNT_NACK.lc ()
                , PaymentDocument.c.TOTALPAYABLEBYPDWITH_DA.lc ()
            ).on ()

            .toOne (Payment.class, "AS pay"
                , Payment.c.AMOUNT_ACK.lc ()
                , Payment.c.AMOUNT_NACK.lc ()
                , Payment.c.AMOUNT.lc ()
            ).on ()
        
        ));

        if (isCharge) {
            m.createIdLog (db, m.get (ChargeInfo.class), user, uuidLine, VocAction.i.UPDATE);
            job.add ("line", db.getJsonObject (m.get (ChargeInfo.class, uuidLine, "*")));
        }
        else {
            m.createIdLog (db, m.get (PenaltiesAndCourtCosts.class), user, uuidLine, VocAction.i.UPDATE);
            job.add ("line", db.getJsonObject (m.get (PenaltiesAndCourtCosts.class, uuidLine, "*")));
        }

    });}
    
    @Override
    public JsonObject doDistribute (String id, JsonObject p, User user) {return doAction ((db, job) -> {

        final MosGisModel m = ModelHolder.getModel ();
        
        db.getConnection ().setAutoCommit (true);
        
        Map<String, Object> item = db.getMap (m.get (Acknowledgment.class, id, "*"));
        
        Table t = m.get (AcknowledgmentItem.class);       
        
        final Object uuidPayDoc = item.get (Acknowledgment.c.UUID_PAY_DOC.lc ());
        
        db.update (AcknowledgmentItem.class, DB.HASH (
            AcknowledgmentItem.c.UUID_ACK,    id,
            AcknowledgmentItem.c.AMOUNT,      null
        ), AcknowledgmentItem.c.UUID_ACK.lc ());
        
        for (Map<String, Object> r: 
                
            db.getList (m
                .select  (AnyChargeInfo.class, "*")
                .where   (ChargeInfo.c.UUID_PAY_DOC, uuidPayDoc)
                .where   (ChargeInfo.c.TOTALPAYABLE.lc () + ">", 0)
                .where   (EnTable.c.IS_DELETED, 0)
            )
                
        ) {
            
            db.upsert (t, DB.HASH (
                EnTable.c.IS_DELETED,             0,
                AcknowledgmentItem.c.UUID_ACK,    id,
                AcknowledgmentItem.c.UUID_CHARGE, r.get ("uuid"),
                AcknowledgmentItem.c.AMOUNT,      r.get ("amount_nack")
            ), 
                AcknowledgmentItem.c.UUID_ACK.lc (), 
                AcknowledgmentItem.c.UUID_CHARGE.lc ()
            );
            
        }
        
        for (Map<String, Object> r: 
                
            db.getList (m
                .select (PenaltiesAndCourtCosts.class, "*")
                .where  (PenaltiesAndCourtCosts.c.UUID_PAY_DOC, uuidPayDoc)
                .where  (PenaltiesAndCourtCosts.c.TOTALPAYABLE.lc () + ">", 0)
                .where  (EnTable.c.IS_DELETED, 0)
            )
                
        ) {
            
            db.upsert (t, DB.HASH (
                EnTable.c.IS_DELETED,             0,
                AcknowledgmentItem.c.UUID_ACK,    id,
                AcknowledgmentItem.c.UUID_PENALTY,r.get ("uuid"),
                AcknowledgmentItem.c.AMOUNT,      r.get ("amount_nack")
            ), 
                AcknowledgmentItem.c.UUID_ACK.lc (), 
                AcknowledgmentItem.c.UUID_PENALTY.lc ()
            );
            
        }        
        
        m.createIdLog (db, getTable (), user, id, VocAction.i.UPDATE);

    });}
    
    @Override
    public JsonObject doApprove (String id, User user) {return doAction ((db) -> {

        db.update (getTable (), HASH (EnTable.c.UUID,               id,
            Acknowledgment.c.ID_CTR_STATUS, VocGisStatus.i.PENDING_RQ_PLACING.getId ()
        ));

        logAction (db, user, id, VocAction.i.APPROVE);

    });}
        
    @Override
    public JsonObject doAlter (String id, User user) {return doAction ((db) -> {
                
        final Map<String, Object> r = HASH (
            EnTable.c.UUID,               id,
            Acknowledgment.c.ID_CTR_STATUS,  VocGisStatus.i.PROJECT.getId ()
        );
                
        db.update (getTable (), r);
        
        logAction (db, user, id, VocAction.i.ALTER);
        
    });}

}