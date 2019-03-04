package ru.eludia.products.mosgis.rest.impl;

import java.util.Map;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import javax.json.JsonObject;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.MeteringDeviceValue;
import ru.eludia.products.mosgis.db.model.tables.MeteringDeviceValueLog;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocMeteringDeviceValueType;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi27;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi2;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.MeteringDeviceValueLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class MeteringDeviceValueImpl extends BaseCRUD<MeteringDeviceValue> implements MeteringDeviceValueLocal {

    @Resource (mappedName = "mosgis.inExportMeteringDeviceValuesQueue")
    Queue queue;

    @Override
    protected Queue getQueue (VocAction.i action) {
        switch (action) {
            case APPROVE:
                return queue;
            default:
                return null;
        }
    }
    
/*    
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

        select.and ("meteringdevicenumber LIKE ?%", searchString.toUpperCase ());
        
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
    
*/

    private void checkFilter (JsonObject data, MeteringDeviceValue.c field, Select select) {
        String key = field.lc ();
        String value = data.getString (key, null);
        if (value == null) return;
        select.and (field, value);
    }

    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {                

        Select select = ModelHolder.getModel ().select (getTable (), "AS root", "*", "uuid AS id")
            .toMaybeOne (MeteringDeviceValueLog.class).on ()
            .toMaybeOne (OutSoap.class, "ts", "ts_rp", "err_text", "uuid_ack").on ()
            .where (EnTable.c.IS_DELETED, 0)
            .orderBy ("root.datevalue DESC")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        JsonObject data = p.getJsonObject ("data");

        checkFilter (data, MeteringDeviceValue.c.UUID_METER, select);

//        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);

    });}

    @Override
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {
        
        final MosGisModel m = ModelHolder.getModel ();
        
        final JsonObject item = db.getJsonObject (m
            .get (getTable (), id, "AS root", "*")
        );
        
        job.add ("item", item);
        
    });}

    @Override
    public JsonObject getVocs () {return fetchData ((db, job) -> {

        Nsi27.i.addTo (job);
        Nsi2.i.addMeteringTo (job);
        VocAction.addTo (job);
        VocMeteringDeviceValueType.addTo (job, false);

    });}

    @Override
    public JsonObject doApprove (String id, User user) {return doAction ((db) -> {

        db.update (getTable (), HASH (
            EnTable.c.UUID,                      id,
            MeteringDeviceValue.c.ID_CTR_STATUS, VocGisStatus.i.PENDING_RQ_PLACING.getId ()
        ));

        logAction (db, user, id, VocAction.i.APPROVE);

    });}
/*    
    @Override
    public JsonObject doAlter (String id, User user) {return doAction ((db) -> {
                
        final Map<String, Object> r = HASH (
            EnTable.c.UUID,                       id,
            MeteringDeviceValue.c.ID_CTR_STATUS,  VocGisStatus.i.PROJECT.getId ()
        );
                
        db.update (getTable (), r);
        
        logAction (db, user, id, VocAction.i.ALTER);
        
    });}    
*/
}