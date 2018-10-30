package ru.eludia.products.mosgis.jms.gis.send;

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
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;
import javax.json.JsonObject;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.base.db.util.TypeConverter;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.Block;
import ru.eludia.products.mosgis.db.model.tables.Entrance;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.tables.Lift;
import ru.eludia.products.mosgis.db.model.tables.LivingRoom;
import ru.eludia.products.mosgis.db.model.tables.NonResidentialPremise;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.ResidentialPremise;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationNsi20;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.ejb.wsc.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.base.JsonMDB;
import ru.eludia.products.mosgis.util.StringUtils;
import ru.eludia.products.mosgis.util.XmlUtils;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.house_management.BlockCategoryType;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inImportHouseQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ImportHouseMDB extends JsonMDB<House> {

    private static final Logger logger = Logger.getLogger (ImportHouseMDB.class.getName ());

    private static final Pattern CADASTRAL_NUMBER_PATTERN = Pattern.compile("^[0-9]{2}:[0-9]{2}:[0-9]{6,10}:.+"); 
    
    @EJB
    private UUIDPublisher UUIDPublisher;

    @EJB
    private WsGisHouseManagementClient wsGisHouseManagementClient;
    
    @Resource (mappedName = "mosgis.outImportHouseQueue")
    private Queue outImportHouseQueue;

    @Override
    protected Get get(UUID uuid) {
        return (Get) ModelHolder.getModel().get(getTable(), uuid, "*")
                .toOne(VocBuilding.class, "oktmo").on();
    }
    
    @Override
    protected void handleRecord(DB db, JsonObject params, Map<String, Object> r) throws SQLException {
logger.info(params.toString());

        Model model = ModelHolder.getModel();

        UUID orgPPAGuid = UUID.fromString(params.getString("orgPPAGuid"));
        Set<OrgRoles> orgRoles = new HashSet<>();
        db.forEach(model.select(VocOrganization.class, "AS org", "orgppaguid")
                .toMaybeOne(VocOrganizationNsi20.class, "AS roles", "*").on("roles.uuid = org.uuid")
                .where("orgppaguid", orgPPAGuid),
                (rs) -> {
                    Map<String, Object> role = db.HASH(rs);
                    orgRoles.add(OrgRoles.getByNsiCode((String) role.get("roles.code")));
                });
        
        r.put("state", XmlUtils.createNsiRef(24, (String) r.get("code_vc_nsi_24")));
        r.put("lifecyclestage", XmlUtils.createNsiRef(336, (String) r.get("code_vc_nsi_336")));
        r.put("olsontz", XmlUtils.createNsiRef(32, "2")); //Москва(+3)
        r.put("oktmo", XmlUtils.createOKTMORef((Long)r.get("vc_buildings.oktmo")));
        r.putAll(getCadastralNumber((String) r.get("kad_n")));
        r.put("transportguid", r.get("uuid"));
        
        boolean isCondo = TypeConverter.Boolean(r.get("is_condo"));
        boolean hasBlocks =  TypeConverter.Boolean(r.get("hasblocks"));
        
        if (isCondo) {
            addEntrances(r, db);
            addLifts(r, db);
            addResidentialPremises(r, db);
            addNonResidentialPremises(r, db);
        } else {
            if (hasBlocks) addBlocks(r, db);
        }
        addLivingRooms(r, db, isCondo);
        
        if (isCondo)
            r.put("residentialpremises", ((Map<Object, Object>)r.get("residentialpremises")).values());
        else if (hasBlocks)
            r.put("blocks", ((Map<Object, Object>)r.get("blocks")).values());
        try {
            AckRequest.Ack ack = null;
        
            if (orgRoles.contains(OrgRoles.UO)) {
                ack = wsGisHouseManagementClient.importHouseUOData(orgPPAGuid, r);
            } else if (orgRoles.contains(OrgRoles.OMS)) {
                ack = wsGisHouseManagementClient.importHouseOMSData(orgPPAGuid, r);
            } else if (orgRoles.contains(OrgRoles.ESP)) {
                ack = wsGisHouseManagementClient.importHouseESPData(orgPPAGuid, r);
            } else if (orgRoles.contains(OrgRoles.RSO)) {
                ack = wsGisHouseManagementClient.importHouseRSOData(orgPPAGuid, r);
            }
            if (ack == null)
                return;
            
            db.begin ();
            db.update (OutSoap.class, DB.HASH (
                "uuid", ack.getRequesterMessageGUID (),
                "uuid_ack", ack.getMessageGUID ()
            ));
            db.commit ();
                
            UUIDPublisher.publish (outImportHouseQueue, ack.getRequesterMessageGUID ()); 
            
        } catch (Fault ex) {
            logger.log (Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            logger.log (Level.SEVERE, ex.getMessage(), ex);
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
        List<Map<String, Object>> entrances = new ArrayList<>();
        
        db.forEach(db.getModel()
                .select (Entrance.class, "*")
                .where ("uuid_house", r.get ("uuid"))
                .and ("is_deleted", 0)
                .and ("is_annuled_in_gis", 0), (rs) -> {
            
            Map<String, Object> entrance = db.HASH (rs);
            
            entrance.put("transportguid", entrance.get("uuid"));
            
            entrances.add(entrance);
        });
        r.put ("entrances", entrances);
    }
    
    private void addLifts (Map<String, Object> r, DB db) throws SQLException {
        
        NsiTable nsi192 = NsiTable.getNsiTable (192);
        
        List<Map<String, Object>> lifts = new ArrayList<>();
        
        db.forEach(db.getModel()
                .select (Lift.class, "AS root", "*")
                .toOne(Entrance.class, "AS entrance", "entrancenum").on()
                .toOne (nsi192, "AS vc_nsi_192", "guid").on ("vc_nsi_192.code=root.code_vc_nsi_192 AND vc_nsi_192.isactual=1")
                .where ("uuid_house", r.get ("uuid"))
                .and ("is_deleted", 0)
                .and ("is_annuled_in_gis", 0), (rs) -> {
            
            Map<String, Object> lift = db.HASH (rs);
            
            lift.put("type", NsiTable.toDom((String) lift.get("code_vc_nsi_192"), (UUID) lift.get("vc_nsi_192.guid")));
            
            lift.put("entrancenum", lift.get("entrance.entrancenum"));
            lift.put("transportguid", lift.get("uuid"));
            
            lifts.add(lift);
        });
        r.put ("lifts", lifts);
    }
    
    private void addResidentialPremises (Map<String, Object> r, DB db) throws SQLException {
        
        NsiTable nsi30 = NsiTable.getNsiTable (30);
        
        Map <Object, Map <String, Object>> premises = db.getIdx(
                db.getModel()
                .select (ResidentialPremise.class, "AS root","*")
                .toMaybeOne(Entrance.class, "AS entrance", "entrancenum").on()
                .toMaybeOne (nsi30, "AS vc_nsi_30", "guid").on ("vc_nsi_30.code=root.code_vc_nsi_30 AND vc_nsi_30.isactual=1")
                .where ("uuid_house", r.get ("uuid"))
                .and ("is_deleted", 0)
                .and ("is_annuled_in_gis", 0));
        
        premises.values().forEach(premise -> {
            
            String entranceNum = (String)premise.get("entrance.entrancenum");
            if(StringUtils.isBlank(entranceNum))
                premise.put("hasnoentrance", true);
            else
                premise.put("entrancenum", entranceNum);
            
            premise.put("premisescharacteristic", NsiTable.toDom((String) premise.get("code_vc_nsi_30"), (UUID) premise.get("vc_nsi_30.guid")));
            
            if (premise.get("grossarea") == null) 
                premise.put("nogrossares", true);
            
            premise.put("livingrooms", new ArrayList<>());
            
            premise.putAll(getCadastralNumber((String) premise.get("cadastralnumber")));
            premise.put("transportguid", premise.get("uuid"));
        });
        
        
        r.put ("residentialpremises", premises);
    }
    
    private void addNonResidentialPremises (Map<String, Object> r, DB db) throws SQLException {
        
        List<Map<String, Object>> premises = new ArrayList<>();
        db.forEach(db.getModel()
                .select (NonResidentialPremise.class, "*")
                .where ("uuid_house", r.get ("uuid"))
                .and ("is_deleted", 0)
                .and ("is_annuled_in_gis", 0), (rs) -> {
            
            Map<String, Object> premise = db.HASH (rs);
            
            premise.putAll(getCadastralNumber((String) premise.get("cadastralnumber")));
            premise.put("transportguid", premise.get("uuid"));
       
            premises.add(premise);
        });

        r.put ("nonresidentialpremises", premises);
    }
    
    private void addLivingRooms (Map<String, Object> r, DB db, boolean isCondo) throws SQLException {
        
        List<Map<String, Object>> houseRooms = new ArrayList<>();
        
        Map<Object, Map<String, Object>> premises = (Map<Object, Map<String, Object>>) (isCondo ? r.get("residentialpremises") : r.get("blocks"));
        
        db.forEach(db.getModel()
                .select (LivingRoom.class, "*")
                .where ("uuid_house", r.get ("uuid"))
                .and ("is_deleted", 0)
                .and ("is_annuled_in_gis", 0), (rs) -> {
            Map<String, Object> room = db.HASH (rs);
            
            room.putAll(getCadastralNumber((String) room.get("cadastralnumber")));
            room.put("transportguid", room.get("uuid"));
            
            UUID premiseUuid = (UUID)(isCondo ? room.get("uuid_premise") : room.get("uuid_block"));
            
            if (premiseUuid != null)
                ((List<Map<String, Object>>)premises.get(premiseUuid).get("livingrooms")).add(room);
            else
                houseRooms.add(room);
        });
        r.put ("livingrooms", houseRooms);
    }
    
    private void addBlocks (Map<String, Object> r, DB db) throws SQLException {
        
        NsiTable nsi30 = NsiTable.getNsiTable (30);
        
        Map <Object, Map <String, Object>> blocks = db.getIdx(
                db.getModel()
                        .select (Block.class, "AS root", "*")
                        .toMaybeOne (nsi30, "AS vc_nsi_30", "guid").on ("vc_nsi_30.code=root.code_vc_nsi_30 AND vc_nsi_30.isactual=1")
                        .where ("uuid_house", r.get ("uuid"))
                        .and ("is_deleted", 0)
                        .and ("is_annuled_in_gis", 0));
        
        blocks.values().forEach(block -> {
            block.putAll(getCadastralNumber((String) block.get("cadastralnumber")));
            block.put("transportguid", block.get("uuid"));
            block.put("premisescharacteristic", NsiTable.toDom((String) block.get("code_vc_nsi_30"), (UUID) block.get("vc_nsi_30.guid")));
            if (block.get("grossarea") == null) 
                block.put("nogrossares", true);
            if (TypeConverter.Boolean(block.get("is_nrs")))
                block.put("category", BlockCategoryType.NON_RESIDENTIAL);
            
            block.put("livingrooms", new ArrayList<>());
        });
        
        r.put ("blocks", blocks);
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
