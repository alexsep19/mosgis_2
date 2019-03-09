package ru.eludia.products.mosgis.ws.rest.impl;

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
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Operator;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.ActualPublicPropertyContract;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.tables.PublicPropertyContract;
import ru.eludia.products.mosgis.db.model.tables.PublicPropertyContractLog;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.PublicPropertyContractFile;
import ru.eludia.products.mosgis.db.model.tables.PublicPropertyContractFileLog;
import ru.eludia.products.mosgis.db.model.tables.PublicPropertyContractVotingProtocol;
import ru.eludia.products.mosgis.db.model.tables.PublicPropertyContractVotingProtocolLog;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocPublicPropertyContractFileType;
import ru.eludia.products.mosgis.db.model.voc.VocUserOktmo;
import ru.eludia.products.mosgis.db.model.voc.VocVotingForm;
import ru.eludia.products.mosgis.db.model.voc.VocVotingMeetingEligibility;
import ru.eludia.products.mosgis.db.model.voc.VocVotingType;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.PublicPropertyContractLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.ws.rest.impl.tools.ComplexSearch;
import ru.eludia.products.mosgis.ws.rest.impl.tools.Search;
import ru.eludia.products.mosgis.ws.rest.impl.tools.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class PublicPropertyContractImpl extends BaseCRUD<PublicPropertyContract> implements PublicPropertyContractLocal {

    @Resource (mappedName = "mosgis.inHousePublicPropertyContractsQueue")
    Queue queue;

    @Override
    public Queue getQueue () {
        return queue;
    }

    private static final Logger logger = Logger.getLogger (PublicPropertyContractImpl.class.getName ());    
       
    private void filterOffDeleted (Select select) {
        select.and (EnTable.c.IS_DELETED, Operator.EQ, 0);
    }

    private void applyComplexSearch (final ComplexSearch search, Select select) {

        search.filter (select, "");
        if (!search.getFilters ().containsKey ("is_deleted")) filterOffDeleted (select);
        
    }
    
    private void applySimpleSearch (final SimpleSearch search, Select select) {

        final String s = search.getSearchString ();
        
        if (s != null) select.and ("address_uc LIKE ?%", s.toUpperCase ().replace (' ', '%'));

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

        Select select = m.select (ActualPublicPropertyContract.class, "*")
            .orderBy (PublicPropertyContract.c.STARTDATE.lc ())
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        applySearch (Search.from (p), select);
        
        JsonObject data = p.getJsonObject ("data");
        
        String k = PublicPropertyContract.c.UUID_ORG.lc ();
        String v = data.getString (k, null);
        if (DB.ok (v)) select.and (k, v);
                      
        if (data.containsKey ("is_oms")) select.and ("oktmo", m.select (VocUserOktmo.class, "oktmo").where ("uuid_user", user.getId ()));
        
        db.addJsonArrayCnt (job, select);

    });}
    
    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {

        final MosGisModel m = ModelHolder.getModel ();
        
        final JsonObject item = db.getJsonObject (m
            .get (ActualPublicPropertyContract.class, id, "AS root", "*")
            .toMaybeOne (PublicPropertyContractLog.class, "AS cpl").on ()
            .toMaybeOne (OutSoap.class, "err_text").on ("cpl.uuid_out_soap=out_soap.uuid")
            .toMaybeOne (House.class, "AS house", "uuid").on ("root.fiashouseguid=house.fiashouseguid")
        );

        switch (VocGisStatus.i.forId (item.getInt (PublicPropertyContract.c.ID_CTR_STATUS.lc (), 10))) {
            
            case ANNUL:
            case PENDING_RQ_ANNULMENT:
            case PENDING_RP_ANNULMENT:
            case FAILED_ANNULMENT:
            
                JsonObject lastAnnul = db.getJsonObject (m
                    .select  (PublicPropertyContractLog.class, "AS root", "*")
                    .and    ("uuid_object", id)
                    .and    ("action",      VocAction.i.ANNUL.getName ())
                    .orderBy ("root.ts DESC")
                    .toMaybeOne (OutSoap.class, "AS soap")
                    .on ()
                );

                if (lastAnnul != null) job.add ("last_annul", lastAnnul);                                
                
                break;
             
            default:
                
        }        

        job.add ("item", item);        

        VocGisStatus.addLiteTo (job);
        VocAction.addTo (job);
        VocVotingForm.addTo (job);
        VocVotingType.addTo (job);
        VocVotingMeetingEligibility.addTo (job);
        VocPublicPropertyContractFileType.addTo (job);

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
    public JsonObject getVocs (JsonObject p) {
        
        JsonObjectBuilder job = Json.createObjectBuilder ();
        
        VocGisStatus.addLiteTo (job);
        
        return job.build ();
        
    }

    @Override
    public JsonObject doAddVotingProtocols (String id, JsonObject p, User user) {return doAction ((db) -> {
        
        for (JsonValue t: p.getJsonObject ("data").getJsonArray ("ids")) {
            
            String uuid = db.insertId (PublicPropertyContractVotingProtocol.class, HASH (
                 PublicPropertyContractVotingProtocol.c.UUID_CTR.lc (), id,
                 PublicPropertyContractVotingProtocol.c.UUID_VP.lc (), ((JsonString) t).getString ()
            )).toString ();
            
            String id_log = db.insertId (PublicPropertyContractVotingProtocolLog.class, HASH (
                "action", VocAction.i.CREATE.getName (),
                "uuid_object", uuid,
                "uuid_user", user.getId ()
            )).toString ();

            db.update (PublicPropertyContractVotingProtocol.class, HASH (
                "uuid",      uuid,
                "id_log",    id_log
            ));
            
        }        
        
    });}

    @Override
    public JsonObject doApprove (String id, User user) {return doAction ((db) -> {

        db.update (getTable (), HASH (
            EnTable.c.UUID,                   id,
            PublicPropertyContract.c.ID_CTR_STATUS, VocGisStatus.i.PENDING_RQ_PLACING.getId ()
        ));

        logAction (db, user, id, VocAction.i.APPROVE);

        List<UUID> ids = new ArrayList<> ();        
        db.forEach (db.getModel ().select (PublicPropertyContractFile.class, "uuid").where (PublicPropertyContractFile.c.UUID_CTR.lc (), id).and (EnTable.c.IS_DELETED.lc (), 0), (rs) -> {
            final Object u = db.getValue (rs, 1);
            if (u != null) ids.add ((UUID) u);
        });

        for (UUID idFile: ids) {

            String idFileLog = db.insertId (PublicPropertyContractFileLog.class, HASH (
                "action", VocAction.i.APPROVE.getName (),
                "uuid_object", idFile,
                "uuid_user", user == null ? null : user.getId ()
            )).toString ();

            db.update (PublicPropertyContractFile.class, HASH (
                "uuid",           idFile,
                AttachTable.c.ATTACHMENTGUID, null,
                "id_log",         idFileLog
            ));

        }

    });}

    @Override
    public JsonObject doAlter (String id, JsonObject p, User user) {return doAction ((db) -> {
                
        final Map<String, Object> r = HASH (
            EnTable.c.UUID,                    id,
            PublicPropertyContract.c.ID_CTR_STATUS,  VocGisStatus.i.PROJECT.getId ()
        );
                
        db.update (getTable (), r);
        
        logAction (db, user, id, VocAction.i.ALTER);
        
    });}
    
    @Override
    public JsonObject doAnnul (String id, JsonObject p, User user) {return doAction ((db) -> {
        
        db.update (getTable (), getData (p,
            EnTable.c.UUID, id,
            PublicPropertyContract.c.ID_CTR_STATUS,     VocGisStatus.i.PENDING_RQ_ANNULMENT.getId (),
            PublicPropertyContract.c.REASONOFANNULMENT, p.getJsonObject ("data").getString (PublicPropertyContract.c.REASONOFANNULMENT.lc ())
        ));
        
        logAction (db, user, id, VocAction.i.ANNUL);
                        
    });}

}