package ru.eludia.products.mosgis.ws.rest.impl;

import java.sql.SQLException;
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
import ru.eludia.products.mosgis.db.model.incoming.InOverhaulWorkType;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOverhaulWorkType;
import ru.eludia.products.mosgis.db.model.voc.VocOverhaulWorkTypeLog;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.ValidationException;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.rest.api.VocOverhaulWorkTypesLocal;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class VocOverhaulWorkTypesImpl extends BaseCRUD<VocOverhaulWorkType> implements VocOverhaulWorkTypesLocal {
    
    @Resource (mappedName = "mosgis.inImportOverhaulWorkTypesQueue")
    Queue inImportOverhaulWorkTypesQueue;
    
    @Resource (mappedName = "mosgis.inExportOverhaulWorkTypesQueue")
    Queue inExportOverhaulWorkTypesQueue;

    @Override
    protected Queue getQueue (VocAction.i action) {

        switch (action) {
            case IMPORT_OVERHAUL_WORK_TYPES: return inImportOverhaulWorkTypesQueue;
            case APPROVE:                    return inExportOverhaulWorkTypesQueue;
            default:                         return null;
        }

    }
    
    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {
        
        Select select = ModelHolder.getModel ().select (VocOverhaulWorkType.class, "AS root", "*")
                .toOne      (VocOrganization.class, "AS org", "label").on ()
                .toMaybeOne (VocOverhaulWorkTypeLog.class, "AS log").on ()
                .toMaybeOne (OutSoap.class, "err_text AS err_text").on ()
                .where      ("is_deleted", 0)
                .orderBy    ("root.code")
                .limit      (p.getInt ("offset"), p.getInt ("limit"));
        
        db.addJsonArrayCnt (job, select);
        
    });}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {

        job.add ("item", db.getJsonObject (ModelHolder.getModel ()
            .get   (getTable (), id, "*")
            .toOne (VocOrganization.class,        "label").on ()
        ));
        
        VocGisStatus.addLiteTo (job);
        VocAction.addTo (job);
        
        db.addJsonArrays (job,

                ModelHolder.getModel ()
                    .select (VocAsyncEntityState.class, "id", "label")                    
                    .orderBy ("label"),
                
                NsiTable.getNsiTable (218).getVocSelect ()

            );

    });}

    @Override
    public JsonObject getVocs() {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        VocGisStatus.addLiteTo (jb);
        VocAction.addTo (jb);
        
        try (DB db = ModelHolder.getModel ().getDb ()) {
            
            db.addJsonArrays (jb,

                ModelHolder.getModel ()
                    .select (VocAsyncEntityState.class, "id", "label")                    
                    .orderBy ("label"),
                
                NsiTable.getNsiTable (218).getVocSelect ()

            );

        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }

        return jb.build ();
        
    }
    
    @Override
    public JsonObject doImport (User user) {
        
        String userOrg = user.getUuidOrg ();
        
        if (userOrg == null) {
            logger.warning ("User has no org set, access prohibited");
            throw new ValidationException ("foo", "Отсутствует организация пользователя, доступ запрещен");
        }
        
        UUID uuid = UUID.randomUUID ();
        
        try {

            ModelHolder.getModel ().getDb ().insert (InOverhaulWorkType.class, HASH (
                "uuid", uuid,
                "uuid_org", userOrg
            ));
            
        }
        catch (SQLException ex) {
            ValidationException vex = ValidationException.wrap (ex);            
            throw vex == null ? new InternalServerErrorException (ex) : vex;                
        }

        publishMessage (VocAction.i.IMPORT_OVERHAUL_WORK_TYPES, uuid.toString ());
        
        return Json.createObjectBuilder ().add ("id", uuid.toString ()).build ();
        
    }
    
    @Override
    public JsonObject doCreate (JsonObject p, User user) {return doAction ((db, job) -> {
        
        String userOrg = user.getUuidOrg ();
        
        if (userOrg == null) {
            logger.warning ("User has no org set, access prohibited");
            throw new ValidationException ("foo", "Отсутствует организация пользователя, доступ запрещен");
        }

        final Table table = getTable ();
        Map<String, Object> data = getData (p);
        Object insertId = db.insertId (table, data);
        job.add ("id", insertId.toString ());
        
        logAction (db, user, insertId, VocAction.i.CREATE);
        
        db.update (table, HASH (
                EnTable.c.UUID, insertId,
                VocOverhaulWorkType.c.ID_OWT_STATUS, VocGisStatus.i.PENDING_RQ_PLACING.getId ()
        ));
        
        logAction (db, user, insertId, VocAction.i.APPROVE);

    });}
    
}
