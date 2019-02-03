package ru.eludia.products.mosgis.jms.gis.send;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.base.db.util.TypeConverter;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.Block;
import ru.eludia.products.mosgis.db.model.tables.Entrance;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.tables.HouseLog;
import ru.eludia.products.mosgis.db.model.tables.Lift;
import ru.eludia.products.mosgis.db.model.tables.LivingRoom;
import ru.eludia.products.mosgis.db.model.tables.NonResidentialPremise;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.ResidentialPremise;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOktmo;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationNsi20;
import ru.eludia.products.mosgis.db.model.voc.VocPassportFields;
import ru.eludia.products.mosgis.db.model.voc.VocRdColType;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.ejb.wsc.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.eludia.products.mosgis.util.StringUtils;
import ru.eludia.products.mosgis.util.XmlUtils;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.house_management.BlockCategoryType;
import ru.gosuslugi.dom.schema.integration.house_management.OGFData;
import ru.gosuslugi.dom.schema.integration.house_management.OGFDataValue;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inHouseDataQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class HouseDataMDB extends UUIDMDB<HouseLog> {

    private static final Pattern CADASTRAL_NUMBER_PATTERN = Pattern.compile("^[0-9]{2}:[0-9]{2}:[0-9]{6,10}:.+"); 
    
    @EJB
    private UUIDPublisher UUIDPublisher;

    @EJB
    private WsGisHouseManagementClient wsGisHouseManagementClient;
    
    @Resource (mappedName = "mosgis.outImportHouseQueue")
    private Queue importQueue;
    
    @Resource (mappedName = "mosgis.outExportHouseQueue")
    private Queue exportQueue;

    @Override
    protected Get get(UUID uuid) {
        return (Get) ModelHolder.getModel().get(getTable(), uuid, "AS root", "*")
                .toOne (VocOrganization.class, "AS org", "orgppaguid").on()
                .toOne (House.class, "AS house", "fiashouseguid", "id_status", "id_status_gis", "gis_unique_number").on()
                .toOne(VocBuilding.class, "oktmo").on();
    }
    
    @Override
    protected void handleRecord(DB db, UUID uuid, Map<String, Object> r) throws SQLException {
      
        VocGisStatus.i status = VocGisStatus.i.forId (r.get ("house.id_status_gis"));
        
        House.Action action = House.Action.forStatus (status);
        
        if (action == null) {
            logger.warning ("No action is implemented for " + status);
            return;
        }
        
        try {
            AckRequest.Ack ack = invoke(db, action, uuid, r);
        
            db.begin();
            
            Map<String, Object> outSoap = DB.HASH(
                    "uuid",     uuid,
                    "uuid_ack", ack.getMessageGUID());
            if (r.containsKey("object_by_transport_guid")) {
                outSoap.put("object_by_transport_guid", r.get("object_by_transport_guid"));
            }
            db.update(OutSoap.class, outSoap);

            db.update(getTable(), DB.HASH(
                    "uuid",          uuid,
                    "uuid_out_soap", uuid,
                    "uuid_message",  ack.getMessageGUID()
            ));
            
            db.update (House.class, DB.HASH (
                    "uuid",          r.get ("uuid_object"),
                    "id_status_gis", action.getNextStatus ().getId ()
                ));
            
            db.commit();
            
            UUIDPublisher.publish (getQueue (action), ack.getRequesterMessageGUID ());
        }
        catch (Fault ex) {

            logger.log (Level.SEVERE, "Can't get house data from GIS ZKH", ex);

            ru.gosuslugi.dom.schema.integration.base.Fault faultInfo = ex.getFaultInfo ();

            db.begin ();

                db.update (OutSoap.class, HASH (
                    "uuid",      uuid,
                    "id_status", DONE.getId (),
                    "is_failed", 1,
                    "err_code",  faultInfo.getErrorCode (),
                    "err_text",  faultInfo.getErrorMessage ()
                ));

                db.update (getTable (), DB.HASH (
                    "uuid",          uuid,
                    "uuid_out_soap", uuid
                ));

                db.update (House.class, DB.HASH (
                    "uuid",          r.get ("uuid_object"),
                    "id_status_gis", action.getFailStatus ().getId ()
                ));

            db.commit ();
        }
        catch (Exception ex) {
            
            logger.log (Level.SEVERE, "Cannot invoke WS", ex);
            
            db.begin ();

                db.upsert (OutSoap.class, HASH (
                    "uuid",      uuid,
                    "svc",       getClass ().getName (),
                    "op",        action.toString (),
                    "is_out",    1,
                    "id_status", DONE.getId (),
                    "is_failed", 1,
                    "err_code",  "0",
                    "err_text",  ex.getMessage ()
                ));

                db.update (getTable (), DB.HASH (
                    "uuid",          uuid,
                    "uuid_out_soap", uuid
                ));

                db.update (House.class, DB.HASH (
                    "uuid",          r.get ("uuid_object"),
                    "id_status_gis", action.getFailStatus ().getId ()
                ));

            db.commit ();
        }
    }
    
    private Queue getQueue (House.Action action) {
        switch (action) {
            case RELOADING:  return exportQueue;
            default:         return importQueue;
        } 
    }
    
    AckRequest.Ack invoke (DB db, House.Action action, UUID messageGUID,  Map<String, Object> r) throws Fault, SQLException {
            
        UUID orgPPAGuid = (UUID) r.get("org.orgppaguid");
            
        switch (action) {
            case RELOADING: return wsGisHouseManagementClient.exportHouseData(orgPPAGuid, messageGUID, (UUID) r.get("house.fiashouseguid"));
            case EDITING:   return invokeImport(db, messageGUID, r);
            default: throw new IllegalArgumentException ("No action implemented for " + action);
        }

    }
    
    AckRequest.Ack invokeImport (DB db, UUID messageGUID,  Map<String, Object> r) throws Fault, SQLException {
       
        UUID orgPPAGuid = (UUID) r.get("org.orgppaguid");
        
        Model model = db.getModel();
        
        Set<OrgRoles> orgRoles = new HashSet<>();
        db.forEach(model.select(VocOrganization.class, "AS org", "orgppaguid")
                .toMaybeOne(VocOrganizationNsi20.class, "AS roles", "*").on("roles.uuid = org.uuid")
                .where("orgppaguid", orgPPAGuid),
                (rs) -> {
                    Map<String, Object> role = db.HASH(rs);
                    OrgRoles orgRole = OrgRoles.getByNsiCode((String) role.get("roles.code"));
                    if (orgRole != null)
                        orgRoles.add(orgRole);
                });
        
        if (orgRoles.isEmpty())
            throw new IllegalArgumentException ("Method is available only to organizations with roles UO, RSO, OMS, ESP");       
        
        OrgRoles orgRole = null;
        
        String passportFieldForColumn = "";
        
        if (orgRoles.contains(OrgRoles.UO)) {
            orgRole = OrgRoles.UO;
            passportFieldForColumn = "is_for_uo";
        } else if (orgRoles.contains(OrgRoles.OMS)) {
            orgRole = OrgRoles.OMS;
            passportFieldForColumn = "is_for_oms";
        } else if (orgRoles.contains(OrgRoles.ESP)) {
            orgRole = OrgRoles.ESP;
            passportFieldForColumn = "is_for_esp";
        } else if (orgRoles.contains(OrgRoles.RSO)) {
            orgRole = OrgRoles.RSO;
        }
        
        Map <Object, Map <String, Object>> passportFields = new HashMap<>();
        if (StringUtils.isNotBlank(passportFieldForColumn))
            passportFields = db.getIdx(model.select (VocPassportFields.class, "*").where(passportFieldForColumn, 1));
        
        boolean isCondo = TypeConverter.Boolean(r.get("is_condo"));
        boolean hasBlocks =  TypeConverter.Boolean(r.get("hasblocks"));
        
        Map <String, Object> houseData = db.getMap(House.class, r.get("uuid_object"));
        addOGFData(houseData, passportFields, isCondo);
        r.put("ogfdata", houseData.get("ogfdata"));
        
        JsonObjectBuilder objectByTransportGuid = Json.createObjectBuilder();
        r.put("object_by_transport_guid", objectByTransportGuid);
        
        r.put("state", XmlUtils.createNsiRef(24, (String) r.get("code_vc_nsi_24")));
        r.put("lifecyclestage", XmlUtils.createNsiRef(336, (String) r.get("code_vc_nsi_336")));
        r.put("olsontz", XmlUtils.createNsiRef(32, "2")); //Москва(+3)
        r.put("oktmo", VocOktmo.createOKTMORef((Long)r.get("vc_buildings.oktmo")));
        r.putAll(getCadastralNumber((String) r.get("kad_n")));
        
        r.put("transportguid", r.get("uuid_object"));
        objectByTransportGuid.add(r.get("uuid_object").toString(), Json.createObjectBuilder().add("object", House.Object.HOUSE.name()).build());
                
        if (isCondo) {
            addEntrances(r, db);
            addLifts(r, passportFields, db);
            addResidentialPremises(r, passportFields, db);
            addNonResidentialPremises(r, passportFields, db);
        } else {
            if (hasBlocks) addBlocks(r, passportFields, db);
        }
        addLivingRooms(r, passportFields, db, isCondo);
        
        if (isCondo)
            r.put("residentialpremises", ((Map<Object, Object>)r.get("residentialpremises")).values());
        else if (hasBlocks)
            r.put("blocks", ((Map<Object, Object>)r.get("blocks")).values());
        
        String objectByTransportGuidStr = objectByTransportGuid.build().toString();
        r.put("object_by_transport_guid", objectByTransportGuidStr);
        
        switch (orgRole) {
            case UO:
                return wsGisHouseManagementClient.importHouseUOData(orgPPAGuid, messageGUID, r);
            case OMS:
                return wsGisHouseManagementClient.importHouseOMSData(orgPPAGuid, messageGUID, r);
            case ESP:
                return wsGisHouseManagementClient.importHouseESPData(orgPPAGuid, messageGUID, r);
            case RSO:
                return wsGisHouseManagementClient.importHouseRSOData(orgPPAGuid, messageGUID, r);
            default:
                throw new IllegalArgumentException ("Method is available only to organizations with roles UO, RSO, OMS, ESP");
        }
    }
    
    private boolean checkCadastralNumber(String cadastralNumber) {
        if (StringUtils.isBlank(cadastralNumber)) {
            return false;
        }
        cadastralNumber = cadastralNumber.trim();
        Matcher m = CADASTRAL_NUMBER_PATTERN.matcher(cadastralNumber);
        return m.matches();
    }
    
    private Map<String, Object> getCadastralNumber(String cadastralNumber) {

        Map<String, Object> result = new HashMap<>();

        if (StringUtils.isNotBlank(cadastralNumber) 
                && checkCadastralNumber(cadastralNumber)) {
            result.put("cadastralnumber", cadastralNumber.trim());
        } else {
            result.put("norsogknegrpregistered", true);
            result.put("cadastralnumber", null);
        }
        return result;
    }
    
    private void addEntrances (Map<String, Object> r, DB db) throws SQLException {
        
        NsiTable nsi330 = NsiTable.getNsiTable (330);
        
        List<Map<String, Object>> entrances = new ArrayList<>();
        
        JsonObjectBuilder objectByTransportGuid = (JsonObjectBuilder)r.get("object_by_transport_guid");
        
        db.forEach(db.getModel()
                .select (Entrance.class, "AS root", "*")
                .toMaybeOne (nsi330, "AS vc_nsi_330", "guid").on ("vc_nsi_330.code=root.code_vc_nsi_330 AND vc_nsi_330.isactual=1")
                .where ("uuid_house", r.get ("uuid_object"))
                .and ("is_deleted", 0)
                .and ("is_annuled_in_gis", 0), (rs) -> {
            
            Map<String, Object> entrance = db.HASH (rs);
            
            entrance.put("annulmentreason", NsiTable.toDom((String) entrance.get("code_vc_nsi_330"), (UUID) entrance.get("vc_nsi_330.guid")));
            entrance.put("transportguid", entrance.get("uuid"));
            
            objectByTransportGuid.add(entrance.get("uuid").toString(), 
                    Json.createObjectBuilder()
                            .add("object", House.Object.ENTRANCE.name())
                            .add("key", entrance.get("entrancenum").toString())
                            .add("is_annuled", entrance.get("annulmentreason") != null)
                            .build()
            );
                    
            entrances.add(entrance);
        });
        r.put ("entrances", entrances);
    }
    
    private void addLifts (Map<String, Object> r, Map <Object, Map <String, Object>> passportFields, DB db) throws SQLException {
        
        NsiTable nsi192 = NsiTable.getNsiTable (192);
        NsiTable nsi330 = NsiTable.getNsiTable (330);
        
        List<Map<String, Object>> lifts = new ArrayList<>();
        
        JsonObjectBuilder objectByTransportGuid = (JsonObjectBuilder)r.get("object_by_transport_guid");
        
        db.forEach(db.getModel()
                .select (Lift.class, "AS root", "*")
                .toOne(Entrance.class, "AS entrance", "entrancenum").on()
                .toOne (nsi192, "AS vc_nsi_192", "guid").on ("vc_nsi_192.code=root.code_vc_nsi_192 AND vc_nsi_192.isactual=1")
                .toMaybeOne (nsi330, "AS vc_nsi_330", "guid").on ("vc_nsi_330.code=root.code_vc_nsi_330 AND vc_nsi_330.isactual=1")
                .where ("uuid_house", r.get ("uuid_object"))
                .and ("is_deleted", 0)
                .and ("is_annuled_in_gis", 0), (rs) -> {
            
            Map<String, Object> lift = db.HASH (rs);
            
            addOGFData(lift, passportFields);
            
            lift.put("type", NsiTable.toDom((String) lift.get("code_vc_nsi_192"), (UUID) lift.get("vc_nsi_192.guid")));
            lift.put("annulmentreason", NsiTable.toDom((String) lift.get("code_vc_nsi_330"), (UUID) lift.get("vc_nsi_330.guid")));
            lift.put("entrancenum", lift.get("entrance.entrancenum"));
            lift.put("transportguid", lift.get("uuid"));

            objectByTransportGuid.add(lift.get("uuid").toString(), 
                    Json.createObjectBuilder()
                            .add("object", House.Object.LIFT.name())
                            .add("key", lift.get("factorynum").toString())
                            .add("is_annuled", lift.get("annulmentreason") != null)
                            .build()
            );
            
            lifts.add(lift);
        });
        r.put ("lifts", lifts);
    }
    
    private void addResidentialPremises (Map<String, Object> r, Map <Object, Map <String, Object>> passportFields, DB db) throws SQLException {
        
        NsiTable nsi30 = NsiTable.getNsiTable (30);
        NsiTable nsi330 = NsiTable.getNsiTable (330);
        
        JsonObjectBuilder objectByTransportGuid = (JsonObjectBuilder)r.get("object_by_transport_guid");
        
        Map <Object, Map <String, Object>> premises = db.getIdx(
                db.getModel()
                .select (ResidentialPremise.class, "AS root","*")
                .toMaybeOne(Entrance.class, "AS entrance", "entrancenum").on()
                .toMaybeOne (nsi30, "AS vc_nsi_30", "guid").on ("vc_nsi_30.code=root.code_vc_nsi_30 AND vc_nsi_30.isactual=1")
                .toMaybeOne (nsi330, "AS vc_nsi_330", "guid").on ("vc_nsi_330.code=root.code_vc_nsi_330 AND vc_nsi_330.isactual=1")
                .where ("uuid_house", r.get ("uuid_object"))
                .and ("is_deleted", 0)
                .and ("is_annuled_in_gis", 0));
        
        premises.values().forEach(premise -> {
            
            addOGFData(premise, passportFields);
            
            String entranceNum = (String)premise.get("entrance.entrancenum");
            if(StringUtils.isBlank(entranceNum))
                premise.put("hasnoentrance", true);
            else
                premise.put("entrancenum", entranceNum);
            
            premise.put("premisescharacteristic", NsiTable.toDom((String) premise.get("code_vc_nsi_30"), (UUID) premise.get("vc_nsi_30.guid")));
            premise.put("annulmentreason", NsiTable.toDom((String) premise.get("code_vc_nsi_330"), (UUID) premise.get("vc_nsi_330.guid")));
            
            if (premise.get("grossarea") == null) 
                premise.put("nogrossares", true);
            
            premise.put("livingrooms", new ArrayList<>());
            
            premise.putAll(getCadastralNumber((String) premise.get("cadastralnumber")));
            premise.put("transportguid", premise.get("uuid"));
            
            objectByTransportGuid.add(premise.get("uuid").toString(), 
                    Json.createObjectBuilder()
                            .add("object", House.Object.RESIDENTIAL_PREMISE.name())
                            .add("key", premise.get("premisesnum").toString())
                            .add("is_annuled", premise.get("annulmentreason") != null)
                            .build()
            );
            
        });
        
        
        r.put ("residentialpremises", premises);
    }
    
    private void addNonResidentialPremises (Map<String, Object> r, Map <Object, Map <String, Object>> passportFields, DB db) throws SQLException {
        
        NsiTable nsi330 = NsiTable.getNsiTable (330);
        
        List<Map<String, Object>> premises = new ArrayList<>();
        
        JsonObjectBuilder objectByTransportGuid = (JsonObjectBuilder)r.get("object_by_transport_guid");
        
        db.forEach(db.getModel()
                .select (NonResidentialPremise.class, "AS root", "*")
                .toMaybeOne (nsi330, "AS vc_nsi_330", "guid").on ("vc_nsi_330.code=root.code_vc_nsi_330 AND vc_nsi_330.isactual=1")
                .where ("uuid_house", r.get ("uuid_object"))
                .and ("is_deleted", 0)
                .and ("is_annuled_in_gis", 0), (rs) -> {
            
            Map<String, Object> premise = db.HASH (rs);
            
            addOGFData(premise, passportFields);
            
            premise.putAll(getCadastralNumber((String) premise.get("cadastralnumber")));
            premise.put("annulmentreason", NsiTable.toDom((String) premise.get("code_vc_nsi_330"), (UUID) premise.get("vc_nsi_330.guid")));
            premise.put("transportguid", premise.get("uuid"));
            objectByTransportGuid.add(premise.get("uuid").toString(), House.Object.NON_RESIDENTIAL_PREMISE.name());
            
            objectByTransportGuid.add(premise.get("uuid").toString(), 
                    Json.createObjectBuilder()
                            .add("object", House.Object.NON_RESIDENTIAL_PREMISE.name())
                            .add("key", premise.get("premisesnum").toString())
                            .add("is_annuled", premise.get("annulmentreason") != null)
                            .build()
            );
            
            premises.add(premise);
        });

        r.put ("nonresidentialpremises", premises);
    }
    
    private void addLivingRooms (Map<String, Object> r, Map <Object, Map <String, Object>> passportFields, DB db, boolean isCondo) throws SQLException {
        
        NsiTable nsi330 = NsiTable.getNsiTable (330);
        
        List<Map<String, Object>> houseRooms = new ArrayList<>();
        
        Map<Object, Map<String, Object>> premises = (Map<Object, Map<String, Object>>) (isCondo ? r.get("residentialpremises") : r.get("blocks"));
        
        JsonObjectBuilder objectByTransportGuid = (JsonObjectBuilder)r.get("object_by_transport_guid");
        
        db.forEach(db.getModel()
                .select (LivingRoom.class, "AS root", "*")
                .toMaybeOne (nsi330, "AS vc_nsi_330", "guid").on ("vc_nsi_330.code=root.code_vc_nsi_330 AND vc_nsi_330.isactual=1")
                .where ("uuid_house", r.get ("uuid_object"))
                .and ("is_deleted", 0)
                .and ("is_annuled_in_gis", 0), (rs) -> {
            Map<String, Object> room = db.HASH (rs);
            
            addOGFData(room, passportFields);
            
            room.putAll(getCadastralNumber((String) room.get("cadastralnumber")));
            room.put("annulmentreason", NsiTable.toDom((String) room.get("code_vc_nsi_330"), (UUID) room.get("vc_nsi_330.guid")));
            room.put("transportguid", room.get("uuid"));
            
            UUID premiseUuid = (UUID)(isCondo ? room.get("uuid_premise") : room.get("uuid_block"));
            
            JsonObjectBuilder objectData = Json.createObjectBuilder()
                            .add("object", House.Object.LIVING_ROOM.name())
                            .add("key", room.get("roomnumber").toString())
                            .add("is_annuled", room.get("annulmentreason") != null);
            
            if (premiseUuid != null) {
                ((List<Map<String, Object>>)premises.get(premiseUuid).get("livingrooms")).add(room);
                objectData.add("parent", premiseUuid.toString());
            }
            else
                houseRooms.add(room);
            
            objectByTransportGuid.add(room.get("uuid").toString(), objectData);
        });
        r.put ("livingrooms", houseRooms);
    }
    
    private void addBlocks (Map<String, Object> r, Map <Object, Map <String, Object>> passportFields, DB db) throws SQLException {
        
        NsiTable nsi30 = NsiTable.getNsiTable (30);
        NsiTable nsi330 = NsiTable.getNsiTable (330);
        
        JsonObjectBuilder objectByTransportGuid = (JsonObjectBuilder)r.get("object_by_transport_guid");
        
        Map <Object, Map <String, Object>> blocks = db.getIdx(
                db.getModel()
                        .select (Block.class, "AS root", "*")
                        .toMaybeOne (nsi30, "AS vc_nsi_30", "guid").on ("vc_nsi_30.code=root.code_vc_nsi_30 AND vc_nsi_30.isactual=1")
                        .toMaybeOne (nsi330, "AS vc_nsi_330", "guid").on ("vc_nsi_330.code=root.code_vc_nsi_330 AND vc_nsi_330.isactual=1")
                        .where ("uuid_house", r.get ("uuid_object"))
                        .and ("is_deleted", 0)
                        .and ("is_annuled_in_gis", 0));
        
        blocks.values().forEach(block -> {
            
            addOGFData(block, passportFields);
            
            block.putAll(getCadastralNumber((String) block.get("cadastralnumber")));
            block.put("transportguid", block.get("uuid"));
            
            objectByTransportGuid.add(block.get("uuid").toString(), 
                    Json.createObjectBuilder()
                            .add("object", House.Object.BLOCK.name())
                            .add("key", block.get("blocknum").toString())
                            .add("is_annuled", block.get("annulmentreason") != null)
                            .build()
            );
            
            block.put("premisescharacteristic", NsiTable.toDom((String) block.get("code_vc_nsi_30"), (UUID) block.get("vc_nsi_30.guid")));
            block.put("annulmentreason", NsiTable.toDom((String) block.get("code_vc_nsi_330"), (UUID) block.get("vc_nsi_330.guid")));
            if (block.get("grossarea") == null) 
                block.put("nogrossares", true);
            if (TypeConverter.Boolean(block.get("is_nrs")))
                block.put("category", BlockCategoryType.NON_RESIDENTIAL);
            
            block.put("livingrooms", new ArrayList<>());
        });
        
        r.put ("blocks", blocks);
    }
    
    private void addOGFData (Map<String, Object> r, Map <Object, Map <String, Object>> passportFields) {
        addOGFData(r, passportFields, null);
    }
    
    private void addOGFData (Map<String, Object> r, Map <Object, Map <String, Object>> passportFields, Boolean isCondo) {
        if (passportFields.isEmpty())
           return;
        
        List<OGFData> ogfDataList = new ArrayList<>();
        
        r.forEach((key, value) -> {
            if (!key.startsWith("f_")) return;
            if (value == null) return;
            if (value instanceof String && StringUtils.isBlank((String)value)) return;
            
            String code = key.substring(2, key.length());

            Map <String, Object> ogfDataType = passportFields.get(code);
            if (ogfDataType == null) {
                logger.log (Level.SEVERE, "В справочнике НСИ 197 не найдена значение с кодом " + code);
                return;
            }
            if (isCondo != null) {
                if (isCondo && !TypeConverter.Boolean(ogfDataType.get("is_for_condo"))) return;
                if (!isCondo && !TypeConverter.Boolean(ogfDataType.get("is_for_cottage"))) return;
            }
            
            Long idType = (Long)ogfDataType.get("id_type");
            if (idType == null) {
                logger.log (Level.SEVERE, "В справочнике НСИ 197 не указан тип для значения с кодом " + code);
                return;
            }
            
            OGFData ogf = new OGFData();
            ogfDataList.add(ogf);
            ogf.setCode(code);
            OGFDataValue ogfDataValue = new OGFDataValue();
            ogf.setValue(ogfDataValue);
            
            switch (VocRdColType.i.forId(idType.intValue())) {
                case BOOL:
                    ogfDataValue.setBooleanValue(TypeConverter.Boolean(value));
                    break;
                case FLOAT:
                    ogfDataValue.setFloatValue((BigDecimal) value);
                    break;
                case DATA:
                    //TODO реализовать
                    logger.log (Level.SEVERE, "Отправка файлов не поддерживается");
                    //ogfDataValue.setFile(file);
                    break;
                case INT:
                case YEAR:
                    ogfDataValue.setIntegerValue(((Long) value).intValue());
                    break;    
                case REF:
                    ogfDataValue.setNsiCode(value.toString());
                    break;
                case TEXT:
                    ogfDataValue.setStringValue((String) value);
                    break;
                case TIME:
                    ogfDataValue.setDateTimeValue(TypeConverter.XMLGregorianCalendar(value.toString().replace (' ', 'T')));
                    break;
            }
        });
        
        r.put("ogfdata", ogfDataList);
    }
    
    private enum OrgRoles {
        ESP("34"),
        OMS("8"),
        RSO("2"),
        UO("1","19","20","21","22");
        
        private OrgRoles (String... nsiCodes) {
            this.nsiCodes = Arrays.asList(nsiCodes);
        }
        
        private final List<String> nsiCodes;
        
        public List<String> getNsiCodes () {
            return nsiCodes;
        }
        
        public static OrgRoles getByNsiCode(String nsiCode) {
            return Arrays.stream(values())
                    .filter(role -> role.getNsiCodes().contains(nsiCode))
                    .findFirst().orElse(null);
        }
        
    }

}
