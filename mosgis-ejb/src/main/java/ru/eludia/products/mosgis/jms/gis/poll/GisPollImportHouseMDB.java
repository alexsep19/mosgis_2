package ru.eludia.products.mosgis.jms.gis.poll;

import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.tables.HouseLog;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocHouseStatus;
import ru.eludia.products.mosgis.db.model.voc.VocNsi197Roles;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPassportFields;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.wsc.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.eludia.products.mosgis.util.StringUtils;
import ru.gosuslugi.dom.schema.integration.base.CommonResultType;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.house_management.GetStateResult;
import ru.gosuslugi.dom.schema.integration.house_management.ImportResult;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outImportHouseQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollImportHouseMDB  extends GisPollMDB {

    @EJB
    private WsGisHouseManagementClient wsGisHouseManagementClient;
    
    @Override
    protected Get get(UUID uuid) {
        return (Get) ModelHolder.getModel().get(getTable(), uuid, "AS root", "*")
            .toOne (HouseLog.class,     "AS log", "uuid", "id_status", "id_status_gis").on ("log.uuid_out_soap=root.uuid")
            .toOne (House.class,     "AS house", "uuid", "fiashouseguid").on()
            .toOne (VocOrganization.class, "AS org", "orgppaguid").on()
        ;
    }
    
    private void assertLen1 (List l, String msg, VocGisStatus.i failStatus) throws FU {
        if (l == null || l.isEmpty ()) throw new FU ("0", msg + " вернулся пустой список", failStatus);
        int len = l.size ();
        if (len != 1) throw new FU ("0", msg + " вернулось " + len, failStatus);
    }

    @Override
    protected void handleOutSoapRecord(DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        UUID orgPPAGuid = (UUID) r.get("org.orgppaguid");

        final VocGisStatus.i lastStatus = VocGisStatus.i.forId (r.get ("log.id_status_gis"));
        
        if (lastStatus == null) {
            logger.severe ("Cannot detect last status for " + r);
            return;
        }

        final House.Action action = House.Action.forStatus (lastStatus);

        if (action == null) {
            logger.severe ("Cannot detect expected action for " + r);
            return;
        }
        
        try {
            GetStateResult rp = getState (orgPPAGuid, r);
            
            ErrorMessageType em1 = rp.getErrorMessage();
            if (em1 != null) {
                throw new FU (em1, action.getFailStatus());
            }
            
            List<ImportResult> importResult = rp.getImportResult (); 
            assertLen1 (importResult, "Вместо 1 результата (importResult)", action.getFailStatus());
            
            ImportResult result = importResult.get (0);
            
            ErrorMessageType em2 = result.getErrorMessage();
            if (em2 != null) {
                throw new FU (em2, action.getFailStatus());
            }
            
            String objectByTransportGuidStr = r.get("object_by_transport_guid").toString();
            JsonObject objectByTransportGuid;
            try (JsonReader jsonReader = Json.createReader(new StringReader(objectByTransportGuidStr))) {
                objectByTransportGuid = jsonReader.readObject();
            }
            
            List<ImportResult.CommonResult> commonResultList = result.getCommonResult ();
            String commonResultErrors = "";
            
            db.begin ();
                Set<String> accessDeniedOgfDataCodes = new HashSet<>();
                for (ImportResult.CommonResult commonResult : commonResultList) {
                    String err = saveObject(db, objectByTransportGuid, commonResult, accessDeniedOgfDataCodes);
                    if (StringUtils.isNotBlank(err)) {
                        if (StringUtils.isNotBlank(commonResultErrors)) commonResultErrors += "\n";
                        commonResultErrors += err;
                    }
                }
                if (StringUtils.isNotBlank(commonResultErrors)) {
                    if (!accessDeniedOgfDataCodes.isEmpty())
                        r.put("accessDeniedOgfDataCodes", accessDeniedOgfDataCodes);
                    throw new FU ("0", commonResultErrors, action.getFailStatus());
                }
                
                db.update (House.class, HASH (
                    "uuid", r.get("house.uuid"),
                    "id_status", VocHouseStatus.i.PUBLISHED.getId(),
                    "id_status_gis", null
                ));
                
                db.update (OutSoap.class, HASH (
                    "uuid", uuid,
                    "id_status", DONE.getId ()
                ));
                
            db.commit ();

            db.getConnection ().setAutoCommit (true);
        }
        catch (FU fu) {
            
            db.getConnection ().setAutoCommit (true);
            
            fu.register (db, uuid, r);
            
        }
        catch (GisPollRetryException ex) {
        }
    }
    
    private GetStateResult getState (UUID orgPPAGuid, Map<String, Object> r) throws GisPollRetryException, FU {
        
        GetStateResult rp;
        
        try {
            rp = wsGisHouseManagementClient.getState (orgPPAGuid, (UUID) r.get ("uuid_ack"));
        }
        catch (Fault ex) {
            throw new FU (ex.getFaultInfo (), VocGisStatus.i.FAILED_PLACING);
        }
        catch (Throwable ex) {            
            throw new FU (ex, VocGisStatus.i.FAILED_PLACING);
        }
        
        checkIfResponseReady (rp);
        
        return rp;
        
    }
    
    private String saveObject(DB db, JsonObject objectByTransportGuid, ImportResult.CommonResult commonResult, Set<String> accessDeniedOgfDataCodes) throws SQLException {
        
        JsonObject object = objectByTransportGuid.getJsonObject(commonResult.getTransportGUID());
        if (object == null) return "Из ГИС ЖКХ вернулся неизвестный идентификатор " + commonResult.getTransportGUID();
        
        House.Object houseObject = House.Object.valueOf(object.getString("object"));

        if (!commonResult.getError().isEmpty()) {
            String err = "";
            for (CommonResultType.Error error : commonResult.getError()) {
                if (StringUtils.isNotBlank(err)) err += "; ";
                err += (error.getErrorCode() + " " + error.getDescription());
                
                if("INT004129".equals(error.getErrorCode())) {
                    String nsi197Codes = error.getDescription().split(":")[1];
                    accessDeniedOgfDataCodes.addAll(Arrays.stream(nsi197Codes.split(",")).map(String::trim).collect(Collectors.toSet()));
                }
            }
            
            if (object.containsKey("key"))
                err = houseObject.getName() + " № " + object.getString("key") + ": " + err;
            
            if (object.containsKey("parent")) {
                JsonObject parentJO = objectByTransportGuid.getJsonObject(object.getString("parent"));
                House.Object parentObject = House.Object.valueOf(parentJO.getString("object"));

                if (parentJO.containsKey("key"))
                    err = parentObject.getName() + " № " + parentJO.getString("key") + " " + err;
            }
            return err;
        }
        
        Map<String, Object> record = HASH(
                "uuid", commonResult.getTransportGUID(),
                "gis_unique_number", commonResult.getUniqueNumber(),
                "gis_modification_date", commonResult.getUpdateDate(),
                houseObject.getGisKey(), commonResult.getGUID()
        );
        if (object.containsKey("is_annuled"))
            record.put("is_annuled_in_gis", object.getBoolean("is_annuled"));
        
        db.update(houseObject.getClazz(), record);
        
        return "";
    }
    
    private class FU extends GisPollException {
        
        private VocGisStatus.i status;

        FU (String code, String text, VocGisStatus.i status) {
            super (code, text);
            this.status = status;
        }
        
        FU (ErrorMessageType errorMessage, VocGisStatus.i status) {
            super (errorMessage);
            this.status = status;
        }

        FU (ru.gosuslugi.dom.schema.integration.base.Fault fault, VocGisStatus.i status) {
            super (fault);
            this.status = status;
        }
        
        FU (Throwable t, VocGisStatus.i status) {
            super (t);
            this.status = status;
        }
        
        @Override
        public void register (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
            
            super.register (db, uuid, r);

            db.update (House.class, HASH (
                "uuid",          r.get ("house.uuid"),
                "id_status_gis", status.getId ()
            ));
            
            if(r.containsKey("accessDeniedOgfDataCodes")) {
                
                String op = r.get("op").toString();
                
                for (String code : (HashSet<String>) r.get("accessDeniedOgfDataCodes")) {
                    
                    Map <String, Object> nsiRole = new HashMap<>();
                    nsiRole.put("code", code);
                    
                    switch (op) {
                        case "importHouseUOData":
                            nsiRole.put("is_for_uo", 0);
                            break;
                        case "importHouseOMSData":
                            nsiRole.put("is_for_oms", 0);
                            break;
                        case "importHouseESPData":
                            nsiRole.put("is_for_esp", 0);
                            break;
                        default:
                            break;
                    }
                        
                    db.update(VocNsi197Roles.class, nsiRole);
                }
            }
            
        }
        
    }
    
}
