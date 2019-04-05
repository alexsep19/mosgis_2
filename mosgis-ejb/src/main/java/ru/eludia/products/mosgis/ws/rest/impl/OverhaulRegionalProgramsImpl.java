package ru.eludia.products.mosgis.ws.rest.impl;

import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Select;
import static ru.eludia.base.db.util.TypeConverter.JsonArrayBuilder;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgram;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramDocument;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramFile;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramHouse;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramHouseWork;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramLog;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.rest.api.OverhaulRegionalProgramsLocal;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class OverhaulRegionalProgramsImpl extends BaseCRUD <OverhaulRegionalProgram> implements OverhaulRegionalProgramsLocal {

    @Resource (mappedName = "mosgis.inExportOverhaulRegionalProgramsQueue")
    Queue inExportOverhaulRegionalProgramsQueue;
    
    @Override
    protected void publishMessage (VocAction.i action, String id_log) {
        
        switch (action) {
            case PUBLISHANDPROJECT:
            case APPROVE:
                super.publishMessage (action, id_log);
            default:
                return;
        }
        
    }
    
    @Override
    protected Queue getQueue (VocAction.i action) {
        switch (action) {
            case PUBLISHANDPROJECT:
            case APPROVE:
                return inExportOverhaulRegionalProgramsQueue;
            default: return null;
        }
    }
    
    @Override
    public JsonObject doApprove (String id, User user) {return doAction ((db) -> {
        
        VocGisStatus.i lastOkStatus = VocGisStatus.i.forId (db.getString (db.getModel ()
                .get(getTable (), id, OverhaulRegionalProgram.c.LAST_SUCCESFULL_STATUS.lc ())));
        
        VocGisStatus.i nextStatus;
        VocAction.i action;
        
        switch (lastOkStatus) {
            case PROJECT:
                nextStatus = VocGisStatus.i.PENDING_RQ_PUBLISHANDPROJECT;
                action = VocAction.i.PUBLISHANDPROJECT;
                break;
            case REGIONAL_PROGRAM_WORKS_PLACE_FINISHED:
                nextStatus = VocGisStatus.i.PENDING_RQ_PLACING;
                action = VocAction.i.APPROVE;
                break;
            default:
                throw new Exception ("Операция запрещена");
        }
        
        db.update (getTable (), HASH (
            EnTable.c.UUID,               id,
            OverhaulRegionalProgram.c.ID_ORP_STATUS, nextStatus.getId ()
        ));
        logAction (db, user, id, action);

    });}
    
    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {
        
        Select select = ModelHolder.getModel ().select (OverhaulRegionalProgram.class, "AS root", "*")
                .toMaybeOne (VocOrganization.class, "AS org", "label").on ()
                .toMaybeOne (OverhaulRegionalProgramLog.class).on ()
                .toMaybeOne (OutSoap.class, "err_text AS err_text").on ()
                .where      ("is_deleted", 0)
                .orderBy    ("root.programname")
                .orderBy    ("root.startyear")
                .orderBy    ("root.endyear")
                .limit      (p.getInt ("offset"), p.getInt ("limit"));
        
        db.addJsonArrayCnt (job, select);
            
    });}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {
        
        job.add ("item", db.getJsonObject (ModelHolder.getModel ()
            .get   (getTable (), id, "*")
        ));
        
        JsonArray works = db.getJsonArray (ModelHolder.getModel ()
           .select (OverhaulRegionalProgramHouseWork.class, "AS work", "*")
                .toOne (OverhaulRegionalProgramHouse.class, "AS house").where ("is_deleted", 0).on ()
                    .toOne (OverhaulRegionalProgram.class,  "AS program").where ("uuid", id).and ("is_deleted", 0).on ("house.program_uuid=program.uuid")
            .where ("is_deleted", 0)
        );
        
        JsonArray documents = db.getJsonArray (ModelHolder.getModel ()
            .select (OverhaulRegionalProgramFile.class, "label")
                .toOne (OverhaulRegionalProgramDocument.class, "AS document").where ("is_deleted", 0).on ()
                    .toOne (OverhaulRegionalProgram.class,     "AS program").where ("uuid", id).and ("is_deleted", 0).on ("document.program_uuid=program.uuid")
        );
        
        job.add ("works", works);
        job.add ("documents", documents);
        
        VocGisStatus.addLiteTo (job);
        VocAction.addTo (job);
        
    });}

    @Override
    public JsonObject getVocs () {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        VocGisStatus.addLiteTo (jb);
        VocAction.addTo (jb);
        
        return jb.build ();
        
    }
    
}
