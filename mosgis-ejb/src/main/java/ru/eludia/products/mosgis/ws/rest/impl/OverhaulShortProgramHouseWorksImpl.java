package ru.eludia.products.mosgis.ws.rest.impl;

import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import javax.json.JsonObject;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.tables.OverhaulShortProgram;
import ru.eludia.products.mosgis.db.model.tables.OverhaulShortProgramHouse;
import ru.eludia.products.mosgis.db.model.tables.OverhaulShortProgramHouseWork;
import ru.eludia.products.mosgis.db.model.tables.OverhaulShortProgramHouseWorksImport;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOverhaulWorkType;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.ValidationException;
import ru.eludia.products.mosgis.rest.api.OverhaulShortProgramHouseWorksLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class OverhaulShortProgramHouseWorksImpl extends BaseCRUD<OverhaulShortProgramHouseWork> implements OverhaulShortProgramHouseWorksLocal {
    
    @Resource (mappedName = "mosgis.inExportOverhaulShortProgramHouseWorksManyQueue")
    Queue inExportOverhaulShortProgramHouseWorksManyQueue;
    
    @Resource (mappedName = "mosgis.inExportOverhaulShortProgramHouseWorksOneQueue")
    Queue inExportOverhaulShortProgramHouseWorksOneQueue;
    
    @Override
    protected void publishMessage (VocAction.i action, String id_log) {
        
        switch (action) {
            case ANNUL:
                super.publishMessage (action, id_log);
            default:
                return;
        }
        
    }
    
    @Override
    protected Queue getQueue (VocAction.i action) {
        switch (action) {
            case ANNUL:
                return inExportOverhaulShortProgramHouseWorksOneQueue;
            default: return null;
        }
    }
    
    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {
        
        Select select = ModelHolder.getModel ().select (OverhaulShortProgramHouseWork.class, "AS root", "*")
                .toOne      (VocOverhaulWorkType.class, "AS type", "code_vc_nsi_218 AS code_nsi_218").where ("isactual", 1).on ("type.code = root.work")
                .where      ("house_uuid", p.getString ("house_uuid"))
                .where      ("is_deleted", 0)
                .limit      (p.getInt ("offset"), p.getInt ("limit"));
        
        db.addJsonArrayCnt (job, select);
            
    });}
    
    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {
        
        job.add ("item", db.getJsonObject (ModelHolder.getModel ()
            .get        (getTable (), id, "*")
        ));
        
    });}

    @Override
    public JsonObject doDelete (String id, User user) {return doAction ((db) -> {
        
        VocGisStatus.i status = VocGisStatus.i.forId (
            db.getInteger (OverhaulShortProgramHouseWork.class, id, OverhaulShortProgramHouseWork.c.ID_OSPHW_STATUS.lc ())
        );
        
        VocGisStatus.i nextStatus;
        VocAction.i action;
        
        switch (status) {
            case PROJECT:
                db.update (getTable (), HASH (
                    "uuid",        id,
                    "is_deleted",  1
                ));
                nextStatus = VocGisStatus.i.PROJECT;
                action = VocAction.i.DELETE;
                break;
            case APPROVED:
                nextStatus = VocGisStatus.i.PENDING_RQ_ANNULMENT;
                action = VocAction.i.ANNUL;
                break;
            default:
                throw new ValidationException ("foo", "Операция запрещена");
        }
        
        db.update (OverhaulShortProgramHouseWork.class, HASH (
            "uuid",                                                   id,
            OverhaulShortProgramHouseWork.c.ID_OSPHW_STATUS.lc (), nextStatus.getId ()
        ));        
        logAction (db, user, id, action);
                
    });}
    
    @Override
    public JsonObject doApprove (String id, User user) {return doAction ((db) -> {
        
        Map <String, Object> programData = db.getMap (db.getModel ()
            .get (OverhaulShortProgram.class, id, "AS program", "planguid AS planguid")
                .toOne (VocOrganization.class, "AS org", "orgppaguid AS orgppaguid").on ()
        );
        
        String importWorksId = db.insertId (OverhaulShortProgramHouseWorksImport.class, HASH (
            OverhaulShortProgramHouseWorksImport.c.PROGRAM_UUID.lc (), id,
            OverhaulShortProgramHouseWorksImport.c.ORGPPAGUID.lc (),   programData.get ("orgppaguid")
        )).toString ();
        List <Map <String, Object>> works = db.getList (db.getModel ()
            .select (OverhaulShortProgramHouseWork.class, "AS works", "*")
                .toOne (OverhaulShortProgramHouse.class, "AS houses").on ()
                    .toOne (OverhaulShortProgram.class, "AS programs").where ("uuid", id).on ("programs.uuid=houses.program_uuid")
            .where ("is_deleted", 0)
            .and   ("id_osphw_status <>", VocGisStatus.i.APPROVED.getId ())
        );
        works.stream ().forEach ((map) -> {
            map.put (OverhaulShortProgramHouseWork.c.IMPORT_UUID.lc (), importWorksId);
        });
        db.update (OverhaulShortProgramHouseWork.class, works);
        UUIDPublisher.publish (inExportOverhaulShortProgramHouseWorksManyQueue, importWorksId);
        
    });}
    
}
