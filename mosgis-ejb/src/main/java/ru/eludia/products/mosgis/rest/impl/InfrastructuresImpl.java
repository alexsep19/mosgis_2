package ru.eludia.products.mosgis.rest.impl;

import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.build.QP;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.Infrastructure;
import ru.eludia.products.mosgis.db.model.tables.InfrastructureLog;
import ru.eludia.products.mosgis.db.model.tables.InfrastructureNsi3;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi33;
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi38;
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi40;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocInfrastructureFileType;
import ru.eludia.products.mosgis.db.model.voc.VocNsi33Ref3;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.InfrastructuresLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class InfrastructuresImpl extends BaseCRUD<Infrastructure> implements InfrastructuresLocal {
    
    private final String LABEL_FIELD_NAME_NSI_3 = "f_d966dd6cbc";
    private final String NSI_2_REF_FIELD_NAME_NSI_3 = "f_1587117ecc";
    
    private final String LABEL_FIELD_NAME_NSI_2 = "f_c8d77ddad5";
    private final String OKEI_FIELD_NAME_NSI_2 = "f_c6e5a29665";
    
    @Resource (mappedName = "mosgis.inInfrastructuresQueue")
    Queue queue;

    @Override
    public Queue getQueue () {
        return queue;
    }

    @Override
    protected void publishMessage (VocAction.i action, String id_log) {
        
        switch (action) {
            case APPROVE:
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

        select.and ("name LIKE %?%", searchString);
        
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
    public JsonObject select(JsonObject p, User user) {return fetchData ((db, job) -> {
        
        Select select = ModelHolder.getModel ().select(getTable (), "AS root", "*", "uuid AS id")
                .orderBy ("root.name")
                .limit (p.getInt ("offset"), p.getInt ("limit"));
        
        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);
        
    });}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((DB db, JsonObjectBuilder job) -> {

        final MosGisModel model = ModelHolder.getModel ();
        
        final Select get = model.get (getTable (), id, "*")
                .toOne (InfrastructureLog.class).on ()
                .toMaybeOne (OutSoap.class, "err_text").on ();
        
        QP qp = db.toQP (get);
        
        JsonObjectBuilder [] jobs = new JsonObjectBuilder [1];
        
        db.forFirst (qp, (rs) -> {
            jobs [0] = db.getJsonObjectBuilder (rs);
        });
        
        JsonArrayBuilder a = Json.createArrayBuilder ();
        
        db.forEach (model.select (InfrastructureNsi3.class, "code").where ("uuid", id), (rs) -> {
            a.add (rs.getString (1));
        });

        jobs [0].add ("codes_nsi_3", a);

        job.add ("item", jobs [0]);

    });}

    @Override
    public JsonObject getVocs () {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        VocAction.addTo (jb);
        VocInfrastructureFileType.addTo (jb);
        
        final MosGisModel model = ModelHolder.getModel ();
        
        NsiTable nsi_2  = NsiTable.getNsiTable (2);
        NsiTable nsi_3  = NsiTable.getNsiTable (3);
        NsiTable nsi_34 = NsiTable.getNsiTable (34);
        NsiTable nsi_35 = NsiTable.getNsiTable (35);
        NsiTable nsi_36 = NsiTable.getNsiTable (36);
        NsiTable nsi_37 = NsiTable.getNsiTable (37);
        NsiTable nsi_39 = NsiTable.getNsiTable (39);
        NsiTable nsi_41 = NsiTable.getNsiTable (41);
        NsiTable nsi_45 = NsiTable.getNsiTable (45);

        try (DB db = model.getDb ()) {
            
            db.addJsonArrays (jb,
                    
                ModelHolder.getModel ()
                    .select  (VocOkei.class, "code AS id", "national AS label")
                    .orderBy ("national"),

                nsi_34.getVocSelect (),
                nsi_35.getVocSelect (),
                nsi_36.getVocSelect (),
                nsi_37.getVocSelect (),
                nsi_39.getVocSelect (),
                nsi_41.getVocSelect (),
                nsi_45.getVocSelect (),
                
                VocNsi33.getVocSelect (),
                VocNsi38.getVocSelect (),
                VocNsi40.getVocSelect (),
                
                model
                    .select  (nsi_2, "code AS id", LABEL_FIELD_NAME_NSI_2 + " AS label", OKEI_FIELD_NAME_NSI_2 + " AS okei")
                    .where   ("is_actual", 1)
                    .orderBy ("code"),
                
                model
                    .select  (nsi_3, "code AS id", LABEL_FIELD_NAME_NSI_3 + " AS label")
                    .toOne   (nsi_2, "code AS nsi_2").on (nsi_3.getName () + "." + NSI_2_REF_FIELD_NAME_NSI_3 + "=" + nsi_2.getName () + ".guid")
                    .where   ("is_actual", 1)
                    .orderBy (nsi_3.getName () + ".code"),
                    
                model
                    .select  (VocGisStatus.class, "id", "label")
                    .orderBy ("id"),
                
                VocNsi33Ref3.getRefs ()

            );

        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }

        return jb.build ();
        
    }
    
    protected void setNsi3 (DB db, Object insertId, JsonObject p) throws SQLException {
        
        db.dupsert (
            InfrastructureNsi3.class,
            HASH ("uuid", insertId),
            p.getJsonObject ("data").getJsonArray ("code_vc_nsi_3").stream ().map ((t) -> {return HASH ("code", ((JsonString) t).getString ());}).collect (Collectors.toList ()),
            "code"
        );
        
    }
    
    @Override
    public JsonObject doCreate (JsonObject p, User user) {return doAction ((db, job) -> {

        final Table table = getTable ();

        Map<String, Object> data = getData (p);

        Object insertId = db.insertId (table, data);
        
        setNsi3 (db, insertId, p);
        
        job.add ("id", insertId.toString ());
        
        logAction (db, user, insertId, VocAction.i.CREATE);

    });}
    
    @Override
    public JsonObject doUpdate (String id, JsonObject p, User user) {return doAction ((db) -> {
        
        setNsi3 (db, id, p);
        
        db.update (getTable (), getData (p,
            "uuid", id
        ));

        logAction (db, user, id, VocAction.i.UPDATE);
                        
    });}

    @Override
    public JsonObject doApprove (String id, User user) {return doAction ((db) -> {

        db.update (getTable (), HASH (
            EnTable.c.UUID,               id,
            Infrastructure.c.ID_IS_STATUS, VocGisStatus.i.PENDING_RQ_PLACING.getId ()
        ));

        logAction (db, user, id, VocAction.i.APPROVE);

    });}
    
    @Override
    public JsonObject doAlter (String id, User user) {return doAction ((db) -> {
                
        final Map<String, Object> r = HASH (
            EnTable.c.UUID,               id,
            Infrastructure.c.ID_IS_STATUS,  VocGisStatus.i.PROJECT.getId ()
        );
                
        db.update (getTable (), r);
        
        logAction (db, user, id, VocAction.i.ALTER);
        
    });}
    
}
