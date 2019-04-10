package ru.eludia.products.mosgis.ws.rest.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.OverhaulShortProgram;
import ru.eludia.products.mosgis.db.model.tables.OverhaulShortProgramDocument;
import ru.eludia.products.mosgis.db.model.tables.OverhaulShortProgramFile;
import ru.eludia.products.mosgis.db.model.tables.OverhaulShortProgramHouse;
import ru.eludia.products.mosgis.db.model.tables.OverhaulShortProgramHouseWork;
import ru.eludia.products.mosgis.db.model.tables.OverhaulShortProgramLog;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.OverhaulShortProgramsLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class OverhaulShortProgramsImpl extends BaseCRUD <OverhaulShortProgram> implements OverhaulShortProgramsLocal {

    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {
        
        Select select = ModelHolder.getModel ().select (OverhaulShortProgram.class, "AS root", "*")
                .toMaybeOne (VocOrganization.class, "AS org", "label").on ()
                .toMaybeOne (OverhaulShortProgramLog.class).on ()
                .toMaybeOne (OutSoap.class, "err_text AS err_text").on ()
                .orderBy    ("root.programname")
                .orderBy    ("root.startmonthyear")
                .orderBy    ("root.endmonthyear")
                .limit      (p.getInt ("offset"), p.getInt ("limit"));
        
        db.addJsonArrayCnt (job, select);
            
    });}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {
        
        job.add ("item", db.getJsonObject (ModelHolder.getModel ()
            .get   (getTable (), id, "*")
        ));
        
        JsonArray works = db.getJsonArray (ModelHolder.getModel ()
           .select (OverhaulShortProgramHouseWork.class, "AS work", "*")
                .toOne (OverhaulShortProgramHouse.class, "AS house").where ("is_deleted", 0).on ()
                    .toOne (OverhaulShortProgram.class,  "AS program").where ("uuid", id).and ("is_deleted", 0).on ("house.program_uuid=program.uuid")
            .where ("is_deleted", 0)
        );
        
        JsonArray documents = db.getJsonArray (ModelHolder.getModel ()
            .select (OverhaulShortProgramFile.class, "label")
                .toOne (OverhaulShortProgramDocument.class, "AS document").where ("is_deleted", 0).on ()
                    .toOne (OverhaulShortProgram.class,     "AS program").where ("uuid", id).and ("is_deleted", 0).on ("document.program_uuid=program.uuid")
        );
        
        job.add ("works", works);
        job.add ("documents", documents);
        
        VocGisStatus.addLiteTo (job);
        VocAction.addTo (job);
        
    });}

    @Override
    public JsonObject doApprove(String id, User user) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JsonObject getVocs () {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        VocGisStatus.addLiteTo (jb);
        VocAction.addTo (jb);
        
        return jb.build ();
        
    }
    
}
