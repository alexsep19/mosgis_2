package ru.eludia.products.mosgis.rest.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Operator;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.Charter;
import ru.eludia.products.mosgis.db.model.tables.CharterObject;
import ru.eludia.products.mosgis.db.model.tables.CharterPayment;
import ru.eludia.products.mosgis.db.model.tables.CharterPaymentFile;
import ru.eludia.products.mosgis.db.model.tables.CharterPaymentFileLog;
import ru.eludia.products.mosgis.db.model.tables.CharterPaymentLog;
import ru.eludia.products.mosgis.db.model.tables.OrganizationWork;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.ServicePayment;
import ru.eludia.products.mosgis.db.model.tables.ServicePaymentLog;
import ru.eludia.products.mosgis.db.model.tables.VotingProtocol;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.CharterPaymentLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class CharterPaymentImpl extends BaseCRUD<CharterPayment> implements CharterPaymentLocal {

    @Resource (mappedName = "mosgis.inHouseCharterPaymentsQueue")
    Queue queue;

    @Override
    public Queue getQueue () {
        return queue;
    }

    private static final Logger logger = Logger.getLogger (CharterPaymentImpl.class.getName ());    
       
    private void filterOffDeleted (Select select) {
        select.and (EnTable.c.IS_DELETED, Operator.EQ, 0);
    }

    private void applyComplexSearch (final ComplexSearch search, Select select) {

        search.filter (select, "");
        if (!search.getFilters ().containsKey ("is_deleted")) filterOffDeleted (select);
        
    }
    
    private void applySimpleSearch (final SimpleSearch search, Select select) {

        final String s = search.getSearchString ();
        
        if (s == null || s.isEmpty ()) return;
        
        final String uc = s.toUpperCase ();

        select.andEither ("owner_label_uc LIKE %?%", uc).or ("label", s);

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

        Select select = m.select (CharterPayment.class, "*", "uuid AS id")
            .toMaybeOne (CharterObject.class).on ()
            .toMaybeOne (VocBuilding.class, "label").on ()
            .orderBy (CharterPayment.c.BEGINDATE.lc ())
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        applySearch (Search.from (p), select);
        
        select.and (CharterPayment.c.UUID_CHARTER.lc (), p.getJsonObject ("data").getString ("uuid_charter"));

        db.addJsonArrayCnt (job, select);

    });}
    
    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {

        final MosGisModel m = ModelHolder.getModel ();
        
        final JsonObject item = db.getJsonObject (m
            .get (getTable (), id, "AS root", "*")
            .toOne (Charter.class, "AS ctr", "*").on ()
            .toOne (VocOrganization.class, "AS org", "label").on ("ctr.uuid_org")
            .toMaybeOne (CharterObject.class).on ()
            .toMaybeOne (VocBuilding.class, "AS fias", "label").on ()
            .toMaybeOne (CharterPaymentFile.class, "AS doc_0", "label").on ("root.uuid_file_0=doc_0.uuid")
            .toMaybeOne (CharterPaymentFile.class, "AS doc_1", "label").on ("root.uuid_file_1=doc_1.uuid")
            .toMaybeOne (CharterPaymentLog.class, "AS cpl").on ()
            .toMaybeOne (OutSoap.class, "err_text").on ("cpl.uuid_out_soap=out_soap.uuid")
        );

        job.add ("item", item);
        
        VocGisStatus.addTo (job);
                
        db.addJsonArrays (job,

            m
                .select     (OrganizationWork.class, "AS org_works", "uuid AS id", "label")
                .toMaybeOne (VocOkei.class, "AS okei", "national").on ()
                .where      ("uuid_org", item.getString ("ctr.uuid_org"))
                .and        ("id_status", VocAsyncEntityState.i.OK.getId ())
                .orderBy    ("org_works.label")
            
        );

        VocAction.addTo (job);

    });}        
    
    @Override
    public JsonObject doAddItems (String id, JsonObject p, User user) {return doAction ((db) -> {
        
        for (JsonValue t: p.getJsonObject ("data").getJsonArray ("ids")) {
            
            String uuid = db.insertId (ServicePayment.class, HASH (
                 ServicePayment.c.UUID_CHARTER_PAYMENT.lc (), id,
                 ServicePayment.c.UUID_ORG_WORK.lc (), ((JsonString) t).getString ()
            )).toString ();
            
            String id_log = db.insertId (ServicePaymentLog.class, HASH (
                "action", VocAction.i.CREATE.getName (),
                "uuid_object", uuid,
                "uuid_user", user.getId ()
            )).toString ();

            db.update (ServicePayment.class, HASH (
                "uuid",      uuid,
                "id_log",    id_log
            ));
            
        }        
        
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

        db.update (getTable (), HASH (
            "uuid",           id,
            "id_ctr_status",  VocGisStatus.i.PENDING_RQ_PLACING.getId ()
        ));
        
        logAction (db, user, id, VocAction.i.APPROVE);

        List<UUID> ids = new ArrayList<> ();        
        
        db.forEach (db.getModel ().select (CharterPayment.class, "uuid_file_0", "uuid_file_1").where ("uuid", id), (rs) -> {
            Object u = db.getValue (rs, 1);
            if (u != null) ids.add ((UUID) u);
            u = db.getValue (rs, 2);
            if (u != null) ids.add ((UUID) u);
        });
        
        for (UUID idFile: ids) {
            
            String idFileLog = db.insertId (CharterPaymentFileLog.class, HASH (
                "action", VocAction.i.APPROVE.getName (),
                "uuid_object", idFile,
                "uuid_user", user == null ? null : user.getId ()
            )).toString ();
            
            db.update (CharterPaymentFile.class, HASH (
                "uuid",      idFile,
                "id_log",    idFileLog
            ));
            
        }        

    });}    

    @Override
    public JsonObject doAlter (String id, User user) {return doAction ((db) -> {
        
        db.update (getTable (), HASH (
            "uuid",           id,
            "id_ctr_status",  VocGisStatus.i.PROJECT.getId ()
        ));
        
        logAction (db, user, id, VocAction.i.ALTER);
        
    });}
    
    @Override
    public JsonObject doAnnul (String id, JsonObject p, User user) {return doAction ((db) -> {
        
        db.update (getTable (), getData (p,
            "uuid", id,
            "id_ctr_status", VocGisStatus.i.PENDING_RQ_ANNULMENT.getId ()
        ));
        
        logAction (db, user, id, VocAction.i.ANNUL);
                        
    });}
    
}