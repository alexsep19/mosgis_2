package ru.eludia.products.mosgis.ws.rest.impl;

import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import javax.json.JsonObject;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Operator;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.Charter;
import ru.eludia.products.mosgis.db.model.tables.AgreementPayment;
import ru.eludia.products.mosgis.db.model.tables.AgreementPaymentLog;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.AgreementPaymentLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.ws.rest.impl.tools.ComplexSearch;
import ru.eludia.products.mosgis.ws.rest.impl.tools.Search;
import ru.eludia.products.mosgis.ws.rest.impl.tools.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class AgreementPaymentImpl extends BaseCRUD<AgreementPayment> implements AgreementPaymentLocal {

    @Resource (mappedName = "mosgis.inHouseAgreementPaymentsQueue")
    Queue queue;

    @Override
    public Queue getQueue () {
        return queue;
    }

    private static final Logger logger = Logger.getLogger (AgreementPaymentImpl.class.getName ());    
       
    private void filterOffDeleted (Select select) {
        select.and (EnTable.c.IS_DELETED, Operator.EQ, 0);
        select.and (AgreementPayment.c.ID_AP_STATUS.lc () + " NOT IN", VocGisStatus.i.ANNUL);
    }

    private void applyComplexSearch (final ComplexSearch search, Select select) {
        search.filter (select, "");
        if (!search.getFilters ().containsKey ("id_ap_status")) filterOffDeleted (select);
    }
    
    private void applySimpleSearch (final SimpleSearch search, Select select) {
/*
        final String s = search.getSearchString ();
        
        if (s == null || s.isEmpty ()) return;
        
        final String uc = s.toUpperCase ();

        select.andEither ("owner_label_uc LIKE %?%", uc).or ("label", s);
*/
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

        Select select = m.select (AgreementPayment.class, "AS root", "*", "uuid AS id")
            .orderBy ("root." + AgreementPayment.c.DATEFROM.lc () + " DESC")
            .toMaybeOne (AgreementPaymentLog.class, "AS cpl").on ()
            .toMaybeOne (OutSoap.class, "AS soap", "id_status", "is_failed", "ts", "ts_rp", "err_text", "uuid_ack").on ("cpl.uuid_out_soap=soap.uuid")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        applySearch (Search.from (p), select);
        
        select.and (AgreementPayment.c.UUID_CTR.lc (), p.getJsonObject ("data").getString ("uuid_ctr"));

        db.addJsonArrayCnt (job, select);

    });}
    
    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {

        final MosGisModel m = ModelHolder.getModel ();
        
        final JsonObject item = db.getJsonObject (m
            .get (getTable (), id, "AS root", "*")
            .toOne (Charter.class, "AS ctr", "*").on ()
            .toOne (VocOrganization.class, "AS org", "label").on ("ctr.uuid_org")
            .toMaybeOne (AgreementPaymentLog.class, "AS cpl").on ()
            .toMaybeOne (OutSoap.class, "err_text").on ("cpl.uuid_out_soap=out_soap.uuid")
        );

        job.add ("item", item);
        
        VocGisStatus.addTo (job);
        VocAction.addTo (job);

    });}            
    
    @Override
    protected void publishMessage (VocAction.i action, String id_log) {
        
        switch (action) {
            case APPROVE:
            case PROMOTE:
            case REFRESH:
            case TERMINATE:
            case ANNUL:
            case ROLLOVER:
            case RELOAD:
                super.publishMessage (action, id_log);
            default:
                return;
        }
        
    }
    
    @Override
    public JsonObject doApprove (String id, User user) {return doAction ((db) -> {

        db.update (getTable (), HASH (EnTable.c.UUID,               id,
            AgreementPayment.c.ID_AP_STATUS, VocGisStatus.i.PENDING_RQ_PLACING.getId ()
        ));

        logAction (db, user, id, VocAction.i.APPROVE);

    });}
    
    @Override
    public JsonObject doAlter (String id, JsonObject p, User user) {return doAction ((db) -> {
                
        final Map<String, Object> r = HASH (
            EnTable.c.UUID,                   id,
            AgreementPayment.c.ID_AP_STATUS,  VocGisStatus.i.PROJECT.getId ()
        );
                
        db.update (getTable (), r);
        
        logAction (db, user, id, VocAction.i.ALTER);
        
    });}    
    
    @Override
    public JsonObject doAnnul (String id, JsonObject p, User user) {return doAction ((db) -> {
        
        db.update (getTable (), getData (p,
            EnTable.c.UUID, id,
            AgreementPayment.c.ID_AP_STATUS,     VocGisStatus.i.PENDING_RQ_ANNULMENT.getId (),
            AgreementPayment.c.REASONOFANNULMENT, p.getJsonObject ("data").getString (AgreementPayment.c.REASONOFANNULMENT.lc ())
        ));
        
        logAction (db, user, id, VocAction.i.ANNUL);
                        
    });}
    
    
}