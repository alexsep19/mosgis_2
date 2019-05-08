package ru.eludia.products.mosgis.ws.rest.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.build.QP;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.phys.PhysicalCol;
import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.ActualBankAccount;
import ru.eludia.products.mosgis.db.model.tables.BankAccount;
import ru.eludia.products.mosgis.db.model.tables.CharterLog;
import ru.eludia.products.mosgis.db.model.tables.Charter;
import ru.eludia.products.mosgis.db.model.tables.CharterFile;
import ru.eludia.products.mosgis.db.model.tables.CharterFileLog;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState;
import ru.eludia.products.mosgis.db.model.voc.VocBic;
import ru.eludia.products.mosgis.db.model.voc.VocCharterObjectReason;
import ru.eludia.products.mosgis.db.model.voc.VocContractDocType;
import ru.eludia.products.mosgis.db.model.voc.VocGisCustomerType;
import static ru.eludia.products.mosgis.db.model.voc.VocGisCustomerType.i.OWNERS;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocGisCustomerTypeNsi58;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.ws.rest.impl.tools.ComplexSearch;
import ru.eludia.products.mosgis.ws.rest.impl.tools.Search;
import ru.eludia.products.mosgis.ws.rest.impl.tools.SimpleSearch;
import ru.eludia.products.mosgis.rest.api.CharterLocal;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class CharterImpl extends BaseCRUD<Charter> implements CharterLocal {

    @Resource (mappedName = "mosgis.inHouseChartersQueue")
    Queue queue;

    @Override
    public Queue getQueue () {
        return queue;
    }

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

        select.and ("label_uc LIKE %?%", searchString.toUpperCase ());
        
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

    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {
        
        final MosGisModel model = ModelHolder.getModel ();

        Select select = model.select (Charter.class, "AS root", "*", "uuid AS id")
            .toOne (VocOrganization.class, "AS org", "label").on ("uuid_org")
            .toMaybeOne (CharterLog.class         ).on ()
            .toMaybeOne (OutSoap.class,           "err_text").on ()
            .and ("uuid_org", p.getJsonObject ("data").getString ("uuid_org", null))
            .orderBy ("org.label")
            .orderBy ("root.docnum")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);

    });}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {
        
        Model m = ModelHolder.getModel ();
        
        JsonObject item = db.getJsonObject (m
            .get (Charter.class, id, "*")
            .toOne      (VocOrganization.class, "label", "stateregistrationdate").on ("uuid_org")
            .toMaybeOne (CharterLog.class                                       ).on ()
            .toMaybeOne (OutSoap.class,                               "err_text").on ()
            .toMaybeOne (BankAccount.class,     "AS bank_acct"
                , BankAccount.c.ACCOUNTNUMBER.lc ()
                , BankAccount.c.UUID_ORG.lc ()
            ).on ()
            .toMaybeOne (VocBic.class,                                 "*").on ()
            .toMaybeOne (VocOrganization.class, "AS org_bank_acct","label").on ("bank_acct.uuid_org=org_bank_acct.uuid")
        ); 

        job.add ("item", item);
        ActualBankAccount.addTo (job, db, item.getString (Charter.c.UUID_ORG.lc ()));

        JsonObject lastApprove = db.getJsonObject (m
            .select  (CharterLog.class, "AS root", "*")
            .and    ("uuid_object", id)
            .and    ("action",      VocAction.i.APPROVE.getName ())
            .orderBy ("root.ts DESC")
            .toOne  (OutSoap.class, "AS soap")
            .and ("id_status", VocAsyncRequestState.i.DONE.getId ())
            .and ("is_failed", 0)
            .on ()
        );       

        if (lastApprove != null) job.add ("last_approve", lastApprove);
        
        if (item.getInt ("id_ctr_status_gis", 0) == VocGisStatus.i.TERMINATED.getId ()) {
            
            JsonObject lastTermination = db.getJsonObject (m
                .select  (CharterLog.class, "AS root", "*")
                .and    ("uuid_object", id)
                .and    ("action",      VocAction.i.TERMINATE.getName ())
                .orderBy ("root.ts DESC")
                .toOne  (OutSoap.class, "AS soap")
                .and ("id_status", VocAsyncRequestState.i.DONE.getId ())
                .and ("is_failed", 0)
                .on ()
            );
            
            if (lastTermination != null) job.add ("last_termination", lastTermination);
            
        }        

    });}

    @Override
    public JsonObject getVocs () {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        final MosGisModel model = ModelHolder.getModel ();
        
        VocAction.addTo (jb);

        try (DB db = model.getDb ()) {
            
            db.addJsonArrays (jb,

                NsiTable.getNsiTable (58).getVocSelect ()
                    .toMaybeOne (VocGisCustomerTypeNsi58.class, "AS it", "isdefault")
                        .when ("id", OWNERS.getId ())
                    .on ("vc_nsi_58.code=it.code")
                .where ("f_7d0f481f17", 1),
                    
                NsiTable.getNsiTable (54).getVocSelect (),
                
                model
                    .select (VocAsyncEntityState.class, "id", "label")                    
                    .orderBy ("label"),
                
                model
                    .select (VocCharterObjectReason.class, "id", "label")                    
                    .orderBy ("label"),
                
                model
                    .select (VocGisCustomerType.class, "id", "label")                    
                    .orderBy ("label"),
                
                model
                    .select (VocContractDocType.class, "id", "label")                    
                    .where ("id IN", 
                        VocContractDocType.i.CHARTER.getId (),
                        VocContractDocType.i.OTHER.getId ())
                    .orderBy ("label"),

                model
                    .select (VocGisStatus.class, "id", "label")                    
                    .orderBy ("id")
                
            );

        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }

        return jb.build ();
        
    }
    
    @Override
    public JsonObject doApprove (String id, User user) {return doAction ((db) -> {

        db.update (getTable (), HASH (
            "uuid",           id,
            "id_ctr_status",  VocGisStatus.i.PENDING_RQ_PLACING.getId ()
        ));
        
        logAction (db, user, id, VocAction.i.APPROVE);
        
        List<UUID> ids = new ArrayList<> ();
        
        db.forEach (db.getModel ().select (CharterFile.class, "uuid").where ("uuid_charter", id), (rs) -> {
            ids.add ((UUID) db.getValue (rs, 1));
        });
        
        for (UUID idFile: ids) {
            
            String idFileLog = db.insertId (CharterFileLog.class, HASH (
                "action", VocAction.i.APPROVE.getName (),
                "uuid_object", idFile,
                "uuid_user", user == null ? null : user.getId ()
            )).toString ();
            
            db.update (CharterFile.class, HASH (
                "uuid",      idFile,
                AttachTable.c.ATTACHMENTGUID, null,
                "id_log",    idFileLog
            ));
            
        }        
        
    });}
    
    @Override
    public JsonObject doPromote (String id) {return doAction ((db) -> {

        db.update (getTable (), HASH (
            "uuid",           id,
            "id_ctr_status",  VocGisStatus.i.PENDING_RQ_APPROVAL.getId ()
        ));
        
        logAction (db, null, id, VocAction.i.PROMOTE);
        
    });}

    @Override
    public JsonObject doAlter (String id, User user) {return doAction ((db) -> {

        db.update (getTable (), HASH (
            "uuid",           id,
            "id_ctr_status",  VocGisStatus.i.MUTATING.getId (),
            "uuid_out_soap",  null
        ));

        Table t = db.getModel ().t (CharterFile.class);
        final PhysicalCol uCol = t.getColumn ("uuid_charter").toPhysical ();
        
        QP qp = new QP ("UPDATE ");
        qp.append (t.getName ());
        qp.append (" SET attachmentguid = NULL ");
        qp.add ("WHERE uuid_charter = ?", id, uCol);

        db.d0 (qp);        

        Table tl = db.getModel ().t (CharterFileLog.class);
        qp = new QP ("UPDATE ");
        qp.append (tl.getName ());
        qp.append (" SET ts_start_sending = NULL WHERE uuid_object IN (SELECT uuid FROM ");
        qp.append (t.getName ());
        qp.add (" WHERE uuid_charter = ?", id, uCol);
        qp.append (")");
        
        db.d0 (qp);

        logAction (db, user, id, VocAction.i.ALTER);
        
    });}

    @Override
    public JsonObject doRefresh (String id, User user) {return doAction ((db) -> {
        
        db.update (getTable (), HASH (
            "uuid",           id,
            "id_ctr_status",  VocGisStatus.i.PENDING_RQ_REFRESH.getId ()
        ));
        
        logAction (db, user, id, VocAction.i.REFRESH);
        
    });}
    
    @Override
    public JsonObject doTerminate (String id, JsonObject p, User user) {return doAction ((db) -> {
        
        db.update (getTable (), getData (p,
            "uuid", id,
            "id_ctr_status", VocGisStatus.i.PENDING_RQ_TERMINATE.getId ()
        ));
        
        logAction (db, user, id, VocAction.i.TERMINATE);
                        
    });}
    
    @Override
    public JsonObject doAnnul (String id, JsonObject p, User user) {return doAction ((db) -> {
        
        db.update (getTable (), getData (p,
            "uuid", id,
            "id_ctr_status", VocGisStatus.i.PENDING_RQ_ANNULMENT.getId ()
        ));
        
        logAction (db, user, id, VocAction.i.ANNUL);
                        
    });}
    
    @Override
    public JsonObject doRollover (String id, JsonObject p, User user) {return doAction ((db) -> {
        
        db.update (getTable (), getData (p,
            "uuid", id,
            "id_ctr_status", VocGisStatus.i.PENDING_RQ_ROLLOVER.getId ()
        ));
        
        logAction (db, user, id, VocAction.i.ROLLOVER);
                        
    });}
    
    @Override
    public JsonObject doReload (String id, User user) {return doAction ((db) -> {
        
        db.update (getTable (), HASH (
            "uuid",           id,
            "id_ctr_status",  VocGisStatus.i.PENDING_RQ_RELOAD.getId ()
        ));
        
        logAction (db, user, id, VocAction.i.RELOAD);
        
    });}

}