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
import ru.eludia.products.mosgis.db.model.tables.OverhaulAddressProgram;
import ru.eludia.products.mosgis.db.model.tables.OverhaulAddressProgramHouse;
import ru.eludia.products.mosgis.db.model.tables.OverhaulAddressProgramHouseWork;
import ru.eludia.products.mosgis.db.model.tables.OverhaulAddressProgramHouseWorksImport;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOverhaulWorkType;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.ValidationException;
import ru.eludia.products.mosgis.rest.api.OverhaulAddressProgramHouseWorksLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class OverhaulAddressProgramHouseWorksImpl extends BaseCRUD<OverhaulAddressProgramHouseWork> implements OverhaulAddressProgramHouseWorksLocal {
    
    @Resource (mappedName = "mosgis.inExportOverhaulAddressProgramHouseWorksManyQueue")
    Queue inExportOverhaulAddressProgramHouseWorksManyQueue;
    
    @Resource (mappedName = "mosgis.inExportOverhaulAddressProgramHouseWorksOneQueue")
    Queue inExportOverhaulAddressProgramHouseWorksOneQueue;
    
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
                return inExportOverhaulAddressProgramHouseWorksOneQueue;
            default: return null;
        }
    }
    
    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {
        
        Select select = ModelHolder.getModel ().select (OverhaulAddressProgramHouseWork.class, "AS root", "*")
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
            db.getInteger (OverhaulAddressProgramHouseWork.class, id, OverhaulAddressProgramHouseWork.c.ID_OAPHW_STATUS.lc ())
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
        
        db.update (OverhaulAddressProgramHouseWork.class, HASH (
            "uuid",                                                   id,
            OverhaulAddressProgramHouseWork.c.ID_OAPHW_STATUS.lc (), nextStatus.getId ()
        ));        
        logAction (db, user, id, action);
                
    });}
    
    @Override
    public JsonObject doApprove (String id, User user) {return doAction ((db) -> {
        
        Map <String, Object> programData = db.getMap (db.getModel ()
            .get (OverhaulAddressProgram.class, id, "AS program", "planguid AS planguid")
                .toOne (VocOrganization.class, "AS org", "orgppaguid AS orgppaguid").on ()
        );
        
        String importWorksId = db.insertId (OverhaulAddressProgramHouseWorksImport.class, HASH (
            OverhaulAddressProgramHouseWorksImport.c.PROGRAM_UUID.lc (), id,
            OverhaulAddressProgramHouseWorksImport.c.ORGPPAGUID.lc (),   programData.get ("orgppaguid")
        )).toString ();
        List <Map <String, Object>> works = db.getList (db.getModel ()
            .select (OverhaulAddressProgramHouseWork.class, "AS works", "*")
                .toOne (OverhaulAddressProgramHouse.class, "AS houses").on ()
                    .toOne (OverhaulAddressProgram.class, "AS programs").where ("uuid", id).on ("programs.uuid=houses.program_uuid")
            .where ("is_deleted", 0)
            .and   ("id_oaphw_status <>", VocGisStatus.i.APPROVED.getId ())
        );
        works.stream ().forEach ((map) -> {
            map.put (OverhaulAddressProgramHouseWork.c.IMPORT_UUID.lc (), importWorksId);
        });
        db.update (OverhaulAddressProgramHouseWork.class, works);
        UUIDPublisher.publish (inExportOverhaulAddressProgramHouseWorksManyQueue, importWorksId);
        
    });}
    
}
