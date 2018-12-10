package ru.eludia.products.mosgis.rest.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.Premise;
import ru.eludia.products.mosgis.db.model.tables.PropertyDocument;
import ru.eludia.products.mosgis.db.model.tables.VoteInitiator;
import ru.eludia.products.mosgis.db.model.tables.VotingProtocol;
import ru.eludia.products.mosgis.db.model.tables.VotingProtocolFile;
import ru.eludia.products.mosgis.db.model.tables.VotingProtocolFileLog;
import ru.eludia.products.mosgis.db.model.tables.VotingProtocolLog;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;
import ru.eludia.products.mosgis.db.model.voc.VocVotingForm;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.VotingProtocolsLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class VotingProtocolsImpl extends BaseCRUD<VotingProtocol> implements VotingProtocolsLocal {

    @Resource (mappedName = "mosgis.inHouseVotingProtocolsQueue")
    Queue queue;

    @Override
    public Queue getQueue () {
        return queue;
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

        select.and ("label_form_uc LIKE %?%", searchString);
        
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
       
        Select select = ModelHolder.getModel ().select (getTable (), "AS root", "*", "uuid AS id")
            .where ("fiashouseguid", p.getJsonObject("data").getJsonString("uuid_house").getString ())
            .toOne (VocGisStatus.class, "label AS status_label").on("id_prtcl_status_gis")
            .toMaybeOne (VotingProtocolLog.class         ).on ()
            .and ("uuid_org", user.getUuidOrg ())
            .orderBy ("root.id_prtcl_status_gis")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);

    });}
    
    @Override
    public JsonObject getVocs () {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        VocAction.addTo (jb);
        VocVotingForm.addTo (jb);
        
        try (DB db = ModelHolder.getModel ().getDb ()) {
            
            db.addJsonArrays (jb,
                
                NsiTable.getNsiTable ( 25).getVocSelect (),
                NsiTable.getNsiTable ( 63).getVocSelect (),
                NsiTable.getNsiTable (241).getVocSelect (),
                    
                ModelHolder.getModel ()
                    .select (VocOrganization.class, "uuid AS id", "label")
                    .orderBy ("label"),

                ModelHolder.getModel ()
                    .select (VocAsyncEntityState.class, "id", "label")
                    .orderBy ("label"),
                    
                ModelHolder.getModel ()
                    .select(VocGisStatus.class, "id", "label")
                    .orderBy ("id")

            );

        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }

        return jb.build ();
        
    }

    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {

        JsonObject item = db.getJsonObject (ModelHolder.getModel ()
            .get (VotingProtocol.class, id, "AS root", "*")
            .toOne (VocGisStatus.class, "label AS status_label").on("id_prtcl_status_gis")
            .toOne (VocBuilding.class, "label AS address_label").on ()
            .toMaybeOne (House.class, "AS house", "uuid AS house_uuid", "fiashouseguid").on ("root.fiashouseguid=house.fiashouseguid")
            .toMaybeOne (VotingProtocolLog.class           ).on ()
            .toMaybeOne (OutSoap.class,             "err_text").on ()
        ); 
        
        job.add ("item", item);
        
        db.addJsonArrays(job,
                ModelHolder.getModel ()
                    .select (PropertyDocument.class, "AS owners", "uuid AS id")
                    .toOne (Premise.class).where ("uuid_house", item.getJsonString("house_uuid").getString ()).on ()
                    .and ("uuid_person_owner IS NOT NULL")
                    .toMaybeOne (VocPerson.class, "label AS label").on ()
        );
        
        final String fiashouseguid = item.getString ("fiashouseguid");    
        VocBuilding.addCaCh (db, job, fiashouseguid);

    });}
    
    @Override
    public JsonObject doCreate (JsonObject p, User user) {return doAction ((db, job) -> {

        final Table table = getTable ();

        Map<String, Object> data = getData (p);

        if (table.getColumn (UUID_ORG) != null && !data.containsKey (UUID_ORG)) data.put (UUID_ORG, user.getUuidOrg ());

        Object insertId = db.insertId (table, data);
        
        job.add ("id", insertId.toString ());
        
        Map<String, Object> initiator_org = new HashMap<>();
        initiator_org.put ("uuid_protocol", insertId);
        initiator_org.put ("uuid_org", data.get(UUID_ORG));
        db.insert (VoteInitiator.class, initiator_org);
        
        logAction (db, user, insertId, VocAction.i.CREATE);

    });}
    
    @Override
    public JsonObject doApprove (String id, User user) {return doAction ((db) -> {

        db.update (getTable (), HASH (
            EnTable.c.UUID,                   id,
            VotingProtocol.c.ID_PRTCL_STATUS, VocGisStatus.i.PENDING_RQ_PLACING.getId ()
        ));

        logAction (db, user, id, VocAction.i.APPROVE);

        List<UUID> ids = new ArrayList<> ();        
        db.forEach (db.getModel ().select (VotingProtocolFile.class, "uuid").where (VotingProtocolFile.c.UUID_PROTOCOL.lc (), id).and (EnTable.c.IS_DELETED.lc (), 0), (rs) -> {
            final Object u = db.getValue (rs, 1);
            if (u != null) ids.add ((UUID) u);
        });

        for (UUID idFile: ids) {

            String idFileLog = db.insertId (VotingProtocolFileLog.class, HASH (
                "action", VocAction.i.APPROVE.getName (),
                "uuid_object", idFile,
                "uuid_user", user == null ? null : user.getId ()
            )).toString ();

            db.update (VotingProtocolFile.class, HASH (
                "uuid",      idFile,
                "id_log",    idFileLog
            ));

        }

    });}  
    
    @Override
    protected void publishMessage (VocAction.i action, String id_log) {
        
        switch (action) {
            case APPROVE:
                super.publishMessage (action, id_log);
            default:
                return;
        }
        
    }

    @Override
    public JsonObject doAlter (String id, User user) {return doAction ((db) -> {
        
        db.update (getTable (), HASH (
            EnTable.c.UUID,                    id,
            VotingProtocol.c.ID_PRTCL_STATUS,  VocGisStatus.i.PROJECT.getId ()
        ));
        
        logAction (db, user, id, VocAction.i.ALTER);
        
    });}

}