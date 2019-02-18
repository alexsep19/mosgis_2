package ru.eludia.products.mosgis.rest.impl;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.MeteringDevice;
import ru.eludia.products.mosgis.db.model.tables.MeteringDeviceLog;
import ru.eludia.products.mosgis.db.model.tables.Premise;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocMeteringDeviceType;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi27;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi2;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.MeteringDeviceLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class MeteringDeviceImpl extends BaseCRUD<MeteringDevice> implements MeteringDeviceLocal {
/*
    @Resource (mappedName = "mosgis.inMeteringDevicesQueue")
    Queue queue;

    @Override
    public Queue getQueue () {
        return queue;
    }
*/
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
    
    private void checkFilter (JsonObject data, MeteringDevice.c field, Select select) {
        String key = field.lc ();
        String value = data.getString (key, null);
        if (value == null) return;
        select.and (field, value);
    }

    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {                

        Select select = ModelHolder.getModel ().select (getTable (), "AS root", "*", "uuid AS id")
            .toMaybeOne (Premise.class, "AS premise", Premise.c.LABEL.lc ()).on ()
//            .toMaybeOne (MeteringDeviceLog.class               ).on ()
//            .toMaybeOne (OutSoap.class,       "err_text").on ()
            .orderBy ("root.meteringdevicenumber")
            .limit (p.getInt ("offset"), p.getInt ("limit"));

        JsonObject data = p.getJsonObject ("data");

        checkFilter (data, MeteringDevice.c.FIASHOUSEGUID, select);
        checkFilter (data, MeteringDevice.c.UUID_ORG, select);

        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);

    });}

    @Override
    public JsonObject getItem (String id) {return fetchData ((db, job) -> {

        job.add ("item", db.getJsonObject (ModelHolder.getModel ()
            .get (getTable (), id, "AS root", "*")
            .toMaybeOne (Premise.class, "AS premise", Premise.c.LABEL.lc ()).on ()
            .toOne (VocBuilding.class, "AS building", "label AS address").on ("root.fiashouseguid=building.houseguid")
            .toMaybeOne (House.class, "AS house", "uuid").on ("root.fiashouseguid=house.fiashouseguid")
            .toMaybeOne (MeteringDeviceLog.class, "AS log").on ()
            .toMaybeOne (OutSoap.class, "err_text").on ("log.uuid_out_soap=out_soap.uuid")
            .toOne (VocOrganization.class, "AS org", "label").on ("root.uuid_org=org.uuid")
        ));
        
        Nsi2.i.addMeteringTo (job);
        Nsi27.i.addTo (job);
        VocGisStatus.addTo (job);
        VocAction.addTo (job);
        VocMeteringDeviceType.addTo (job);

    });}

    @Override
    public JsonObject getVocs () {
        
        JsonObjectBuilder job = Json.createObjectBuilder ();
        
        Nsi27.i.addTo (job);
        Nsi2.i.addMeteringTo (job);
        VocAction.addTo (job);
        VocMeteringDeviceType.addTo (job);
        
        return job.build ();
        
    }
/*    
    @Override
    public JsonObject doApprove (String id, User user) {return doAction ((db) -> {

        db.update (getTable (), HASH (EnTable.c.UUID,               id,
            MeteringDevice.c.ID_CTR_STATUS, VocGisStatus.i.PENDING_RQ_PLACING.getId ()
        ));

        logAction (db, user, id, VocAction.i.APPROVE);

    });}
    
    @Override
    public JsonObject doAlter (String id, User user) {return doAction ((db) -> {
                
        final Map<String, Object> r = HASH (
            EnTable.c.UUID,               id,
            MeteringDevice.c.ID_CTR_STATUS,  VocGisStatus.i.PROJECT.getId ()
        );
                
        db.update (getTable (), r);
        
        logAction (db, user, id, VocAction.i.ALTER);
        
    });}    
*/    
}