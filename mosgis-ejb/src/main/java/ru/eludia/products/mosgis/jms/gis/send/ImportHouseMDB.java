package ru.eludia.products.mosgis.jms.gis.send;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
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
import ru.gosuslugi.dom.schema.integration.house_management.ApartmentHouseUOType;
import ru.gosuslugi.dom.schema.integration.house_management.HouseBasicUOType;
import ru.gosuslugi.dom.schema.integration.house_management.HouseBasicUpdateUOType;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseUORequest;
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
        r.put("transportguid", UUID.randomUUID());
        
        try {
            AckRequest.Ack ack = null;
        
            if (orgRoles.contains(OrgRoles.UO)) {
                ack = wsGisHouseManagementClient.importHouseUOData(orgPPAGuid, r);
            } else if (orgRoles.contains(OrgRoles.OMS)) {

            } else if (orgRoles.contains(OrgRoles.ESP)) {

            } else if (orgRoles.contains(OrgRoles.RSO)) {

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
            
        } catch (Exception ex) {
            
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
        }
        return result;
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
