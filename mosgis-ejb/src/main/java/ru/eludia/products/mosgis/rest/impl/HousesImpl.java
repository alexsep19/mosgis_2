package ru.eludia.products.mosgis.rest.impl;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.PassportKind;
import static ru.eludia.products.mosgis.PassportKind.CONDO;
import static ru.eludia.products.mosgis.PassportKind.COTTAGE;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.rest.api.HousesLocal;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.ActualCaChObject;
import ru.eludia.products.mosgis.db.model.tables.Block;
import ru.eludia.products.mosgis.db.model.tables.CharterLog;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.ContractObject;
import ru.eludia.products.mosgis.db.model.tables.Entrance;
import ru.eludia.products.mosgis.db.model.tables.Lift;
import ru.eludia.products.mosgis.db.model.tables.ResidentialPremise;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.tables.HouseLog;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.dyn.MultipleRefTable;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocBuildingAddress;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOktmo;
import ru.eludia.products.mosgis.db.model.voc.VocHouseStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationTerritory;
import ru.eludia.products.mosgis.db.model.voc.VocPassportFields;
import ru.eludia.products.mosgis.db.model.voc.VocPropertyDocumentType;
import static ru.eludia.products.mosgis.db.model.voc.VocRdColType.i.REF;
import ru.eludia.products.mosgis.db.model.voc.VocVotingForm;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.ValidationException;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class HousesImpl extends BaseCRUD<House> implements HousesLocal {

    private static final Logger logger = Logger.getLogger (HousesImpl.class.getName ());
        
    @Resource (mappedName = "mosgis.inHouseDataQueue")
    private Queue queue;

    @Override
    public Queue getQueue () {
        return queue;
    }
    
    @Override
    protected void publishMessage (VocAction.i action, String id_log) {
        
        switch (action) {
            case CREATE:
            case RELOAD:
            case ALTER:
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

        select.and ("address_uc LIKE %?%", searchString);
        
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

    private void logAction (DB db, User user, Object id, String uuidOrg, String fiasHouseGuid, VocAction.i action) throws SQLException {

        Table logTable = ModelHolder.getModel ().getLogTable (getTable ());

        if (logTable == null) return;

        if (uuidOrg == null && user != null && user.getUuidOrg() != null)
            uuidOrg = user.getUuidOrg();
        
        if (uuidOrg == null) {
            Map<String, Object> mgmtOrg = db.getMap(ModelHolder.getModel ()
                    .select(ContractObject.class, "AS root", "uuid")
                    .toOne (Contract.class, "AS contract", "uuid").on ()
                    .toOne (VocOrganization.class, "AS org", "uuid").on ()
                    .where("fiashouseguid", fiasHouseGuid)
                    .and("startdate <= " + LocalDate.now().format(DateTimeFormatter.ISO_DATE))
                    .and("enddate >= " + LocalDate.now().format(DateTimeFormatter.ISO_DATE))
                    .and("id_ctr_status_gis", VocGisStatus.i.APPROVED));
            if (mgmtOrg.isEmpty())
                //TODO Искать ОМС
                logger.log(Level.SEVERE, "Не найдена управляющая организация для дома с идентификтаором " + fiasHouseGuid);
            else
                uuidOrg = mgmtOrg.get("org.uuid").toString();
        }
            
        
        String id_log = db.insertId (logTable, HASH (
            "action", action,
            "uuid_object", id,
            "uuid_user", user == null ? null : user.getId (),
            "uuid_org", uuidOrg
        )).toString ();
        
        db.update (getTable (), HASH (
            "uuid",      id,
            "id_log",    id_log
        ));

        publishMessage (action, id_log);

    }
    
    @Override
    public JsonObject selectAll (JsonObject p) {
        
        Model m = ModelHolder.getModel ();
        
        Select select = m.select (House.class, "uuid AS id", 
                                               "address", 
                                               "is_condo", 
                                               "fiashouseguid", 
                                               "unom",
                                               "id_status_gis",
                                               "id_status",
                                               "code_vc_nsi_24")
                .toOne (VocBuilding.class, "oktmo AS oktmo").on()
                .orderBy ("address")
                .limit (p.getInt ("offset"), p.getInt ("limit"));
        
        final Search search = Search.from (p);

        if (search != null) select = search.filter (select, simple (search, "unom", "fiashouseguid", "address_uc LIKE %?%"));
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {
            db.addJsonArrayCnt (jb, select);
        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();
    }
    
    @Override
    public JsonObject selectOktmo (JsonObject p, String uuid_org) {
        
        Model m = ModelHolder.getModel ();
        
        Select select = m.select (House.class, "uuid AS id", 
                                               "address", 
                                               "is_condo", 
                                               "fiashouseguid", 
                                               "unom",
                                               "id_status_gis",
                                               "id_status",
                                               "code_vc_nsi_24")
                .toOne (VocBuilding.class, "oktmo AS oktmo")
                .where ("oktmo IN", ModelHolder.getModel ()
                        .select (VocOrganizationTerritory.class)
                        .toOne  (VocOktmo.class, "code").on ()
                        .where  ("uuid_org", uuid_org)
                       ).on ()
                .orderBy ("address")
                .limit (p.getInt ("offset"), p.getInt ("limit"));
        
        final Search search = Search.from (p);

        if (search != null) select = search.filter (select, simple (search, "unom", "fiashouseguid", "address_uc LIKE %?%"));
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {
            db.addJsonArrayCnt (jb, select);
        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();
        
    }
    
    @Override
    public JsonObject select (JsonObject p, User user) {
        
        Model m = ModelHolder.getModel ();
        
        Select select = m.select (House.class, "uuid AS id", 
                                               "address", 
                                               "is_condo", 
                                               "fiashouseguid", 
                                               "unom",
                                               "id_status_gis",
                                               "id_status",
                                               "code_vc_nsi_24")
            .toOne (VocBuilding.class, "oktmo AS oktmo").on()
            .orderBy ("address")
            .limit (p.getInt ("offset"), p.getInt ("limit"));
        
        String uuidOrg = user.getUuidOrg ();
        
        if (DB.ok (uuidOrg)) select.and ("fiashouseguid", m
            .select (ActualCaChObject.class, "fiashouseguid")
            .where ("uuid_org", uuidOrg)
        );
            
        final Search search = Search.from (p);

        if (search != null) select = search.filter (select, simple (search, "unom", "fiashouseguid", "address_uc LIKE %?%"));

        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {
            db.addJsonArrayCnt (jb, select);
        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();

    }
    
    private String simple (Search search, String i, String g, String s) {
    
        if (search == null) return s;
        
        String searchString = search.getSearchString ();
        
        if (searchString == null || searchString.isEmpty ()) return s;
        
        try {
            Integer.parseInt (searchString);
            return i;
        }
        catch (Exception e) {
            // do nothing
        }
        
        try {
            UUID.fromString (searchString);
            return g;
        }
        catch (Exception e) {
            // do nothing
        }
    
        return s;
        
    }

    private static final int NSI_VOC_CONDITION = 24;

    @Override
    public JsonObject getItem (String id) {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        final MosGisModel m = ModelHolder.getModel ();
        
        try (DB db = m.getDb ()) {
            
            JsonObject item = db.getJsonObject (m
                    .get (House.class, id, "*")
                    .toMaybeOne (VocBuilding.class,        "AS fias",      "postalcode", "okato", "oktmo").on ()
                    .toMaybeOne (VocBuildingAddress.class, "AS fias_addr", "label").on ("fias.houseguid = fias_addr.houseguid")
                    .toMaybeOne (HouseLog.class).on ()
                    .toMaybeOne (OutSoap.class, "err_text").on ()
            );
            
            jb.add ("item", item);
            
            if (item.getInt ("is_condo", 1) == 0) {
                
                if (null != db.getString (m
                        
                    .select (Block.class, "uuid")
                    .where ("uuid_house", id)
                    .and ("is_deleted", 0))
                        
                ) jb.add ("has_blocks", 1);
                
            }
            else {
                
                if (null != db.getString (m
                        
                    .select (ResidentialPremise.class, "uuid")
                    .where ("uuid_house", id)
                    .and ("is_deleted", 0)
                    .and ("code_vc_nsi_30", 2)) // коммуналка
                        
                ) jb.add ("has_shared_premises_res", 1);
                
            }
            
            db.addJsonArrays (jb, NsiTable.getNsiTable (NSI_VOC_CONDITION).getVocSelect ());
            
            House housesTable = new House ();
            housesTable.setModel (m);
            housesTable.addNsiFields (db);
            for (MultipleRefTable refTable: housesTable.getRefTables ()) {                
                
                JsonObjectBuilder ids = Json.createObjectBuilder ();
                
                db.forEach (m.select (refTable, "code").where (refTable.getPk ().get (0).getName (), id), 
                    rs -> {ids.add (rs.getString (1), 1);}
                );
                
                jb.add (refTable.getName (), ids);
                
            }            
            
            final String fiashouseguid = item.getString ("fiashouseguid");
            
            VocBuilding.addCaCh (db, jb, fiashouseguid);
            
            db.addJsonArrays (jb,
                m
                    .select (VocPropertyDocumentType.class, "id", "label")
                    .orderBy ("label")
            );
                                    
        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }

        VocAction.addTo (jb);
        VocGisStatus.addTo(jb);
        VocVotingForm.addTo (jb);
        VocHouseStatus.addTo(jb);
        
        return jb.build ();

    }

    @Override
    public JsonObject doUpdate (String id, JsonObject p) {

        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        JsonObject data = p.getJsonObject ("data");

        try (DB db = ModelHolder.getModel ().getDb ()) {
            
            final Map<String, Object> r = HASH (
                    
                "uuid",                  id,
                    
                "code_vc_nsi_24",        data.getString ("code_vc_nsi_24", null),
                    
                "culturalheritage",      data.getString ("culturalheritage", null),
                "floorcount",            data.getString ("floorcount", null),
                "kad_n",                 data.getString ("kad_n", null),
                "minfloorcount",         data.getString ("minfloorcount", null),
                "totalsquare",           data.getString ("totalsquare", null),
                "undergroundfloorcount", data.getString ("undergroundfloorcount", null),
                "usedyear",              data.getString ("usedyear", null),
                    
                "hasblocks",                      data.getInt ("hasblocks", 0),
                "hasmultiplehouseswithsameadres", data.getInt ("hasmultiplehouseswithsameadres", 0)
                    
            );
            
            db.begin ();            
                db.update (House.class, r);                
                checkYear (db, id, r.get ("usedyear"));            
            db.commit ();

        }
        catch (ValidationException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();

    }
    
    private void checkYear (final DB db, String id, Object year) throws SQLException {
        if (year == null) return;
        checkEntranceYear (db, id, year);
        checkLiftYear (db, id, year);
    }
    
    private void checkEntranceYear (final DB db, String id, Object year) throws SQLException {

        JsonObject entrance = db.getJsonObject (ModelHolder.getModel ().select (Entrance.class, "entrancenum", "creationyear")
            .and ("is_deleted",      0)
            .and ("uuid_house",      id)
            .and ("creationyear < ", year)
        );

        if (entrance == null) return;

        throw new ValidationException ("usedyear", 
            "По имеющимся сведениям, подъезд №" + 
            entrance.getString ("entrancenum") + 
            " построен в " + 
            entrance.getInt ("creationyear") + 
            "г. Операция отменена."
        );

    }    

    private void checkLiftYear (final DB db, String id, Object year) throws SQLException {

        JsonObject lift = db.getJsonObject (ModelHolder.getModel ().select (Lift.class, "factorynum", Lift.YEAR_FIELD)
            .and ("is_deleted",            0)
            .and ("uuid_house",            id)
            .and (Lift.YEAR_FIELD + " < ", year)
        );

        if (lift == null) return;

        throw new ValidationException ("usedyear", 
            "По имеющимся сведениям, лифт №" + 
            lift.getString ("factorynum") + 
            " введён в эксплуатацию в " + 
            lift.getInt (Lift.YEAR_FIELD) + 
            "г. Операция отменена."
        );

    }
    
    @Override
    public JsonObject getVocs () {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        final MosGisModel model = ModelHolder.getModel ();

        try (DB db = model.getDb ()) {
            
            db.addJsonArrays (jb, NsiTable.getNsiTable (24).getVocSelect ());            
            VocGisStatus.addTo(jb);
            VocHouseStatus.addTo(jb);

        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }

        return jb.build ();
        
    }

    @Override
    public JsonObject getVocPassportFields (String id, Integer[] ids) {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {
                        
            PassportKind kind = db.getInteger (House.class, id, "is_condo") == 1 ? CONDO : COTTAGE;

            Map<Integer, JsonObject> id2o = new HashMap <> (ids.length);

            db.forEach (ModelHolder.getModel ()
                .select (VocPassportFields.class, "*")
                .where  ("id IN", ids)
                .and    (kind.getFilterFieldName (), 1), 
                rs -> {
                    JsonObject o = db.getJsonObject (rs);
                    id2o.put (Integer.parseInt (o.getString ("id")), o);
                }
            );
            
            JsonArrayBuilder vc_pass_fields = Json.createArrayBuilder ();
            for (Integer i: ids) {
                final JsonObject o = id2o.get (i);
                if (o != null) vc_pass_fields.add (o);
            }
                        
            jb.add ("vc_pass_fields", vc_pass_fields);

            for (JsonObject i: id2o.values ()) {
                
                if (i.getInt ("id_type", -1) != REF.getId ()) continue;
                
                final int voc = i.getInt ("voc", -1);
                
                if (voc == -1) {
                    logger.log (Level.WARNING, "Cannot detect vocabulary id for " + i);
                    continue;
                }
                
                final NsiTable nsiTable = NsiTable.getNsiTable (voc);

                if (nsiTable == null) {
                    logger.log (Level.SEVERE, "Cannot get NSI table vocabulary id for " + voc + ". Available tables are " + ModelHolder.getModel ().getTables ().stream ().map ((t) -> t.getName ()).sorted ().collect (Collectors.toList ()));
                    continue;
                }
                    
                try {
                    db.addJsonArrays (jb, nsiTable.getVocSelect ());
                } 
                catch (Exception ex) {
                    logger.log (Level.SEVERE, "Cannot fetch NSI vocabulary " + voc, ex);
                }                                                

            }

        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();
        
    }

    @Override
    public JsonObject doPatch (String id, JsonObject p) {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        JsonObject data = p.getJsonObject ("data");

        try (DB db = ModelHolder.getModel ().getDb ()) {
            
            final String k = data.getString ("k");
            final String v = data.getString ("v", null);
        
            db.begin ();            
            
                db.update (House.class, HASH (
                    "uuid", id, 
                    k, v)
                );
                
            db.commit ();

        }
        catch (ValidationException ex) {
            throw ex;
        }        
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();
        
    }

    @Override
    public JsonObject doSetMultiple (String id, JsonObject p) {
    
        JsonObjectBuilder jb = Json.createObjectBuilder ();
        
        JsonObject data = p.getJsonObject ("data");

        try (DB db = ModelHolder.getModel ().getDb ()) {
            
            MultipleRefTable refTable = new MultipleRefTable (ModelHolder.getModel ().get (House.class), data.getString ("code"), "");
            
            refTable.setModel (ModelHolder.getModel ());
            
            db.adjustTable (refTable);
            
            db.dupsert (refTable, 
                    
                HASH ("uuid", id), 
                
                data.getJsonArray ("ids")
                    .getValuesAs (JsonNumber.class)
                    .stream ()
                    .map (i -> HASH ("code", i.intValue ()))                    
                    .collect (Collectors.toList ())
                ,
                    
                "code"
            
            );
            
        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return jb.build ();
        
    }
    
    private final String FIASHOUSEGUID = "fiashouseguid";
    
    @Override
    public JsonObject doCreate (JsonObject p, User user) {return fetchData ((db, job) -> {

        final Table table = getTable ();

        Map<String, Object> data = getData (p);
        data.put("id_status", VocGisStatus.i.PENDING_RQ_RELOAD.getId());

        db.upsert (table, data, FIASHOUSEGUID);

        String upsertId = db.getString( new Select (table, "uuid").where(FIASHOUSEGUID, data.get(FIASHOUSEGUID)));
        
        logAction (db, user, upsertId, null, data.get(FIASHOUSEGUID).toString(), VocAction.i.CREATE);
        
        job.add ("id", upsertId);

    });}
    
    @Override
    public JsonObject doReload (String id, String uuidOrg, User user) {return doAction ((db) -> {
        
        db.update (getTable (), HASH (
            "uuid",           id,
            "id_status_gis",  VocGisStatus.i.PENDING_RQ_RELOAD.getId ()
        ));
        
        logAction (db, user, id, uuidOrg, null, VocAction.i.RELOAD);
        
    });}
    
    @Override
    public JsonObject doSend (String id, String uuidOrg, User user) {return doAction ((db) -> {

        db.update (getTable (), HASH (
            "uuid",           id,
            "id_status_gis",  VocGisStatus.i.PENDING_RQ_PLACING.getId ()
        ));
        
        logAction (db, user, id, uuidOrg, null, VocAction.i.ALTER);
        
    });}
    
}