package ru.eludia.products.mosgis.rest.impl;

import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import javax.json.JsonObject;
import javax.json.JsonString;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.Account;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.MeteringDevice;
import ru.eludia.products.mosgis.db.model.tables.MeteringDeviceAccount;
import ru.eludia.products.mosgis.db.model.tables.MeteringDeviceLog;
import ru.eludia.products.mosgis.db.model.tables.MeteringDeviceMeteringDevice;
import ru.eludia.products.mosgis.db.model.tables.Premise;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocMeteringDeviceFileType;
import ru.eludia.products.mosgis.db.model.voc.VocMeteringDeviceInstallationPlace;
import ru.eludia.products.mosgis.db.model.voc.VocMeteringDeviceType;
import ru.eludia.products.mosgis.db.model.voc.VocMeteringDeviceValueType;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi16;
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

    @Resource (mappedName = "mosgis.inExportMeteringDevicesQueue")
    Queue queue;

    @Override
    protected Queue getQueue (VocAction.i action) {        
        switch (action) {
            case APPROVE: return queue;
            default: return null;
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
    public JsonObject getItem (String id, User user) {return fetchData ((db, job) -> {
        
        final MosGisModel m = ModelHolder.getModel ();
        
        final JsonObject item = db.getJsonObject (m
            .get (getTable (), id, "AS root", "*")
            .toMaybeOne (Premise.class, "AS premise", Premise.c.LABEL.lc ()).on ()
            .toOne (VocBuilding.class, "AS building", "label AS address").on ("root.fiashouseguid=building.houseguid")
            .toMaybeOne (House.class, "AS house", "uuid").on ("root.fiashouseguid=house.fiashouseguid")
            .toMaybeOne (MeteringDeviceLog.class, "AS log").on ()
            .toMaybeOne (OutSoap.class, "err_text").on ("log.uuid_out_soap=out_soap.uuid")
            .toOne (VocOrganization.class, "AS org", "label").on ("root.uuid_org=org.uuid")
        );

        job.add ("item", item);

        db.addJsonArrays (job, 

            Nsi16.getVocSelect (),

            m
                .select (MeteringDeviceAccount.class, "AS accs")
                .where ("uuid", id)
                .toOne (Account.class, "AS acc", "*").on ()
                .toMaybeOne (VocOrganization.class, "AS org", "label").on ("acc.uuid_org_customer=org.uuid")
                .toMaybeOne (VocPerson.class,       "AS ind", "label").on ("acc.uuid_person_customer=ind.uuid")
                .orderBy ("acc.accountnumber"),

            m
                .select (MeteringDeviceMeteringDevice.class, "AS meters", "uuid_meter")
                .where ("uuid", id)
                .toOne (MeteringDevice.class, "AS meter"
                    , MeteringDevice.c.METERINGDEVICENUMBER.lc ()
                    , MeteringDevice.c.METERINGDEVICESTAMP.lc ()
                    , MeteringDevice.c.METERINGDEVICEMODEL.lc ()
                ).on ("meters.uuid_meter=meter.uuid")
                .orderBy ("meter.meteringdevicenumber")

        );

        Nsi2.i.addMeteringTo (job);
        Nsi27.i.addTo (job);
        VocGisStatus.addLiteTo (job);
        VocAction.addTo (job);
        VocMeteringDeviceType.addTo (job);
        VocMeteringDeviceValueType.addTo (job, DB.ok (item.getInt (MeteringDevice.c.CONSUMEDVOLUME.lc ())));
        VocMeteringDeviceInstallationPlace.addTo (job, item.getInt (MeteringDevice.c.MASK_VC_NSI_2.lc ()) == Nsi2.i.POWER.getId ());
        VocMeteringDeviceFileType.addTo          (job, item.getInt (MeteringDevice.c.ID_TYPE.lc ())       == VocMeteringDeviceType.i.COLLECTIVE.getId ());

    });}

    @Override
    public JsonObject getVocs () {return fetchData ((db, job) -> {

        Nsi27.i.addTo (job);
        Nsi2.i.addMeteringTo (job);
        VocAction.addTo (job);
        VocMeteringDeviceType.addTo (job);
        
        db.addJsonArrays (job, 
           Nsi16.getVocSelect ()
        );

    });}

    @Override
    public JsonObject doSetAccounts (String id, JsonObject p, User user) {return doAction ((db) -> {

        db.dupsert (
            MeteringDeviceAccount.class,
            HASH ("uuid", id),
            p.getJsonObject ("data").getJsonArray ("uuid_account").stream ().map ((t) -> {return HASH ("uuid_account", ((JsonString) t).getString ());}).collect (Collectors.toList ()),
            "uuid_account"
        );

        logAction (db, user, id, VocAction.i.UPDATE);

    });}    
    
    @Override
    public JsonObject doUnsetAccounts (String id, JsonObject p, User user) {return doAction ((db) -> {
        
        Object [] ids = p
            .getJsonObject ("data")
            .getJsonArray ("uuid_account")
            .stream ()
            .map ((t) -> ((JsonString) t).getString ())
            .toArray ();
        
        db.delete (db.getModel ().select (MeteringDeviceAccount.class, "*")
            .where ("uuid", id)
            .where ("uuid_account", ids)
        );

        logAction (db, user, id, VocAction.i.UPDATE);

    });}    

    
    @Override
    public JsonObject doSetMeters (String id, JsonObject p, User user) {return doAction ((db) -> {

        db.dupsert (
            MeteringDeviceMeteringDevice.class,
            HASH ("uuid", id),
            p.getJsonObject ("data").getJsonArray ("uuid_meter").stream ().map ((t) -> {return HASH ("uuid_meter", ((JsonString) t).getString ());}).collect (Collectors.toList ()),
            "uuid_meter"
        );

        logAction (db, user, id, VocAction.i.UPDATE);

    });}    
    
    @Override
    public JsonObject doUnsetMeters (String id, JsonObject p, User user) {return doAction ((db) -> {
        
        Object [] ids = p
            .getJsonObject ("data")
            .getJsonArray ("uuid_meter")
            .stream ()
            .map ((t) -> ((JsonString) t).getString ())
            .toArray ();
        
        db.delete (db.getModel ().select (MeteringDeviceMeteringDevice.class, "*")
            .where ("uuid", id)
            .where ("uuid_meter", ids)
        );

        logAction (db, user, id, VocAction.i.UPDATE);

    });}    
    
    
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

}