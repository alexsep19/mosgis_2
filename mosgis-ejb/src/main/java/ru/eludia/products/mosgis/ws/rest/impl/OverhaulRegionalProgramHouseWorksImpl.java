package ru.eludia.products.mosgis.ws.rest.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import javax.json.JsonObject;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgram;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramHouse;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramHouseWork;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramHouseWorksImport;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOverhaulWorkType;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.ValidationException;
import ru.eludia.products.mosgis.rest.api.OverhaulRegionalProgramHouseWorksLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.BaseCRUD;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class OverhaulRegionalProgramHouseWorksImpl extends BaseCRUD<OverhaulRegionalProgramHouseWork> implements OverhaulRegionalProgramHouseWorksLocal {

    @Resource (mappedName = "mosgis.inExportOverhaulRegionalProgramHouseWorksQueue")
    Queue inExportOverhaulRegionalProgramHouseWorksQueue;
    
    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {
        
        Select select = ModelHolder.getModel ().select (OverhaulRegionalProgramHouseWork.class, "AS root", "*")
                .toOne      (VocOverhaulWorkType.class, "AS type", "code_vc_nsi_218 AS code_nsi_218").on ("type.code = root.work")
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
    
    private void checkDuplicates (JsonObject p) throws SQLException {
        
        Map<String, Object> data = getData (p);
        
        DB db = ModelHolder.getModel ().getDb ();
        
        List <Map <String, Object>> duplicates = db.getList (db.getModel ()
            .select (OverhaulRegionalProgramHouseWork.class, "*")
            .where  (OverhaulRegionalProgramHouseWork.c.HOUSE_UUID.lc (), data.get ("house_uuid"))
            .where  (OverhaulRegionalProgramHouseWork.c.WORK.lc (),       data.get ("work"))
            .where  (OverhaulRegionalProgramHouseWork.c.ENDMONTH.lc (),   data.get ("endmonth"))
            .where  (OverhaulRegionalProgramHouseWork.c.ENDYEAR.lc (),    data.get ("endyear"))
            .where  (OverhaulRegionalProgramHouseWork.c.STARTMONTH.lc (), data.get ("startmonth"))
            .where  (OverhaulRegionalProgramHouseWork.c.STARTYEAR.lc (),  data.get ("startyear"))
            .and    ("is_deleted", 0)
        );
        
        if (!duplicates.isEmpty ()) throw new ValidationException ("foo", "Данный вид работ с указанным периодом уже существует");
        
    }
    
    @Override
    public JsonObject doCreate (JsonObject p, User user) {return doAction ((db, job) -> {

        Map<String, Object> data = getData (p);
        
        checkDuplicates (p);

        Object insertId = db.insertId (getTable (), data);
        
        job.add ("id", insertId.toString ());
        
        logAction (db, user, insertId, VocAction.i.CREATE);

    });}
    
    @Override
    public JsonObject doUpdate (String id, JsonObject p, User user) {return doAction ((db) -> {
        
        checkDuplicates (p);
        
        db.update (getTable (), getData (p,
            "uuid", id
        ));
        
        logAction (db, user, id, VocAction.i.UPDATE);
                        
    });}
    
    @Override
    public JsonObject doApprove (String id, User user) {return doAction ((db) -> {
        
        Map <String, Object> programData = db.getMap (db.getModel ()
            .get (OverhaulRegionalProgram.class, id, "AS program", "regionalprogramguid AS regionalprogramguid")
                .toOne (VocOrganization.class, "AS org", "orgppaguid AS orgppaguid").on ()
        );
        
        String importWorksId = db.insertId (OverhaulRegionalProgramHouseWorksImport.class, HASH (
            OverhaulRegionalProgramHouseWorksImport.c.PROGRAM_UUID.lc (), id,
            OverhaulRegionalProgramHouseWorksImport.c.ORGPPAGUID.lc (),   programData.get ("orgppaguid")
        )).toString ();
        List <Map <String, Object>> works = db.getList (db.getModel ()
            .select (OverhaulRegionalProgramHouseWork.class, "AS works", "*")
                .toOne (OverhaulRegionalProgramHouse.class, "AS houses").on ()
                    .toOne (OverhaulRegionalProgram.class, "AS programs").where ("uuid", id).on ("programs.uuid=houses.program_uuid")
            .where ("is_deleted", 0)
            .and   ("id_orphw_status <>", VocGisStatus.i.APPROVED.getId ())
        );
        works.stream ().forEach ((map) -> {
            map.put (OverhaulRegionalProgramHouseWork.c.IMPORT_UUID.lc (), importWorksId);
        });
        db.update (OverhaulRegionalProgramHouseWork.class, works);
        UUIDPublisher.publish (inExportOverhaulRegionalProgramHouseWorksQueue, importWorksId);
        
    });}
    
}