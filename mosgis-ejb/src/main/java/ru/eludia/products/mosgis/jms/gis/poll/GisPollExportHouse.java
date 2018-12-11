package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.base.db.util.TypeConverter;
import ru.eludia.products.mosgis.db.model.tables.Block;
import ru.eludia.products.mosgis.db.model.tables.Entrance;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.tables.HouseLog;
import ru.eludia.products.mosgis.db.model.tables.LivingRoom;
import ru.eludia.products.mosgis.db.model.tables.NonResidentialPremise;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.ResidentialPremise;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocBuildingAddress;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.wsc.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.gosuslugi.dom.schema.integration.house_management.BlockCategoryType;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.eludia.products.mosgis.util.StringUtils;
import ru.gosuslugi.dom.schema.integration.house_management.ExportHouseResultType;
import ru.gosuslugi.dom.schema.integration.house_management.GetStateResult;
import ru.gosuslugi.dom.schema.integration.house_management.HouseBasicExportType;
import ru.gosuslugi.dom.schema.integration.house_management.OGFData;
import ru.gosuslugi.dom.schema.integration.house_management.OGFDataValue;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportHouseQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportHouse extends GisPollMDB {
    
    @EJB
    protected WsGisHouseManagementClient wsGisHouseManagementClient;

    private GetStateResult getState(UUID orgPPAGuid, Map<String, Object> r) throws GisPollRetryException, GisPollException {

        GetStateResult rp;

        try {
            rp = wsGisHouseManagementClient.getState(orgPPAGuid, (UUID) r.get("uuid_ack"));
        } catch (Fault ex) {
            throw new GisPollException(ex.getFaultInfo());
        } catch (Throwable ex) {
            throw new GisPollException(ex);
        }

        checkIfResponseReady(rp);

        return rp;
    }
    
    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ()
            .get   (getTable (), uuid, "AS root", "*")
            .toOne (HouseLog.class,     "AS log", "uuid").on ("log.uuid_out_soap=root.uuid")
            .toOne (House.class,     "AS house", "uuid", "fiashouseguid").on()
            .toOne (VocOrganization.class, "AS org", "orgppaguid").on()
        ;        
    }

    @Override
    protected void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        UUID orgPPAGuid = (UUID) r.get("org.orgppaguid");
        UUID houseUUID  = (UUID) r.get("house.uuid");

        try {
            GetStateResult rp = getState(orgPPAGuid, r);

            if (rp.getErrorMessage() != null) {
                throw new GisPollException(rp.getErrorMessage());
            }

            db.begin();
            handleExportHouseResult(db, houseUUID, rp.getExportHouseResult());
            db.update(OutSoap.class, HASH(
                    "uuid", uuid,
                    "id_status", DONE.getId()
            ));
            db.commit();

        } catch (GisPollRetryException ex) {
        } catch (GisPollException ex) {
            ex.register(db, uuid, r);
        }
    }
    
    private void handleExportHouseResult (DB db, UUID houseUuid, ExportHouseResultType result) throws SQLException {

        Model m = ModelHolder.getModel ();
             
        Map<String, Object> record = DB.HASH (
            "uuid",                  houseUuid,
            "gis_unique_number",     result.getHouseUniqueNumber(),
            "gis_modification_date", result.getModificationDate(),
            "is_condo",              result.getApartmentHouse() != null
        );
        
        if (result.getApartmentHouse() != null) {
            saveApartmentHouse(db, m, houseUuid, record, result.getApartmentHouse());
        } else {
           saveLivingHouse(db, m, houseUuid, record, result.getLivingHouse()); 
        }
    }
    
    private void saveApartmentHouse(DB db, Model m, UUID houseUuid, Map<String, Object> record, ExportHouseResultType.ApartmentHouse house) throws SQLException {
        String fiasHouseGuid = house.getBasicCharacteristicts().getFIASHouseGuid();
            
        addBasicCharacteristic(record, house.getBasicCharacteristicts());

        record.putAll(DB.HASH(
            "code_vc_nsi_25",        house.getHouseManagementType() != null ? house.getHouseManagementType().getCode() : null,
            "minfloorcount",         house.getMinFloorCount(),
            "code_vc_nsi_241",       house.getOverhaulFormingKind(),
            "undergroundfloorcount", house.getUndergroundFloorCount(),
            "address",               db.getString(VocBuilding.class, fiasHouseGuid, "label")
        ));
          
        Map<String, Object> dbHouseData = db.getMap(House.class, houseUuid);
        Map<String, Object> houseDataForSave = getIfDbDataNullOrEmpty(record, dbHouseData);
        houseDataForSave.put("uuid", houseUuid);
        
        if (record.get("kad_n") != null)
            houseDataForSave.put("kad_n", record.get("kad_n"));
        db.update(House.class, houseDataForSave);
         
        dbHouseData.put ("uuid", getUuid ());
        db.update (HouseLog.class, dbHouseData);
        
        //Подъезды
        Map<String, Object> entranceUuidByNum = new HashMap<>();
        
        Map<Object, Map<String, Object>> entrancesDb = db.getIdx(m
                    .select(Entrance.class, "*")
                    .where("uuid_house", houseUuid)
                    .and("entranceguid IS NOT NULL"),
                "entranceguid");
        
        for (ExportHouseResultType.ApartmentHouse.Entrance entrance : house.getEntrance()) {
            Map<String, Object> data = HASH(
                    "entrancenum",   entrance.getEntranceNum(),
                    "storeyscount",  entrance.getStoreysCount(),
                    "creationyear",  entrance.getCreationYear(),
                    "annulmentinfo", entrance.getAnnulmentInfo()
            );
            if (entrance.getAnnulmentReason() != null) {
                data.put("code_vc_nsi_330", entrance.getAnnulmentReason().getCode());
            }
            Map<String, Object> entranceDb = entrancesDb.get(UUID.fromString(entrance.getEntranceGUID()));
            
            if (entranceDb == null)
                entranceDb = db.getMap(m.select(Entrance.class, "*").where("uuid_house", houseUuid).and("entrancenum", entrance.getEntranceNum()));
            
            if (entranceDb != null || entranceDb.get("entranceguid") != null)
                entranceDb = entrancesDb.get(entranceDb.get("entranceguid"));
            
            Map<String, Object> entranceDataForSave = getIfDbDataNullOrEmpty(data, entranceDb);
            
            if (entranceDb != null) { 
                entranceDataForSave.put("uuid", entranceDb.get("uuid"));
                entranceDb.put("is_used", true);
            }
            entranceDataForSave.putAll(HASH(
                    "entranceguid",          entrance.getEntranceGUID(),
                    "fiaschildhouseguid",    entrance.getFIASChildHouseGuid(),
                    "terminationdate",       entrance.getTerminationDate(),
                    "gis_modification_date", entrance.getModificationDate(),
                    "informationconfirmed",  Boolean.TRUE.equals(entrance.isInformationConfirmed())
            ));
            if (entrance.getAnnulmentReason() != null) {
                entranceDataForSave.put("is_annuled_in_gis", true);
            } else {
                entranceDataForSave.put("is_annuled_in_gis", false);
            }
            
            Object entranceUuid;
            if(entranceDataForSave.containsKey("uuid")) {
                entranceUuid = entranceDataForSave.get("uuid");
                db.update(Entrance.class, entranceDataForSave);
            } else {
                entranceUuid = db.insertId(Entrance.class, entranceDataForSave);
            }
            entranceUuidByNum.put(entrance.getEntranceNum(), entranceUuid);
        }
        
        for (Map.Entry<Object, Map<String, Object>> entry : entrancesDb.entrySet()) {
            Map<String, Object> entrance = entry.getValue();
            if (entrance.containsKey("is_used"))
                continue;
            entrance.put("code_vc_nsi_330", "4"); //4 - Дублирование информации, на проде поменять на 6 - В связи с ошибкой ввода данных
            entrance.put("is_annuled_in_gis", true);
            entrance.put("terminationdate", new Date());
            db.update(Entrance.class, entrance);
        }
        
        //Нежилые помещения
        Map<Object, Map<String, Object>> nonResidentialPremisesDb = db.getIdx(m
                    .select(NonResidentialPremise.class, "*")
                    .where("uuid_house", houseUuid)
                    .and("premisesguid IS NOT NULL"),
                "premisesguid");
        
        for (ExportHouseResultType.ApartmentHouse.NonResidentialPremises nonResidentialPremise : house.getNonResidentialPremises()) {
            Map<String, Object> data = HASH(
                "cadastralnumber",       nonResidentialPremise.getCadastralNumber(),
                "premisesnum",           nonResidentialPremise.getPremisesNum(),
                "floor",                 nonResidentialPremise.getFloor(),
                "totalarea",             nonResidentialPremise.getTotalArea(),
                "iscommonproperty",      Boolean.TRUE.equals(nonResidentialPremise.isIsCommonProperty())
            );
            
            addOGFData(data, nonResidentialPremise.getOGFData());
            
            if (nonResidentialPremise.getAnnulmentReason() != null) {
                data.put("code_vc_nsi_330", nonResidentialPremise.getAnnulmentReason().getCode());
            }
            Map<String, Object> nonResidentialPremiseDb = nonResidentialPremisesDb.get(UUID.fromString(nonResidentialPremise.getPremisesGUID()));
            
            if (nonResidentialPremiseDb == null && StringUtils.isNotBlank(nonResidentialPremise.getCadastralNumber()))
                nonResidentialPremiseDb = db.getMap(m
                        .select(NonResidentialPremise.class, "*")
                        .where("uuid_house", houseUuid)
                        .and("cadastralnumber", nonResidentialPremise.getCadastralNumber())
                        .and("premisesguid IS NULL")
                );
            
            if (nonResidentialPremiseDb == null)
                nonResidentialPremiseDb = db.getMap(m
                        .select(NonResidentialPremise.class, "*")
                        .where("uuid_house", houseUuid)
                        .and("premisesnum", nonResidentialPremise.getPremisesNum())
                        .and("premisesguid IS NULL")
                );
            
            Map<String, Object> nonResidentialPremiseForSave = getIfDbDataNullOrEmpty(data, nonResidentialPremiseDb);
            
            if (nonResidentialPremiseDb != null) { 
                nonResidentialPremiseForSave.put("uuid", nonResidentialPremiseDb.get("uuid"));
                nonResidentialPremiseDb.put("is_used", true);
            }
            nonResidentialPremiseForSave.putAll(HASH(
                    "premisesguid",          nonResidentialPremise.getPremisesGUID(),
                    "fiaschildhouseguid",    nonResidentialPremise.getFIASChildHouseGuid(),
                    "gis_unique_number",     nonResidentialPremise.getPremisesUniqueNumber(),
                    "terminationdate",       nonResidentialPremise.getTerminationDate(),
                    "gis_modification_date", nonResidentialPremise.getModificationDate(),
                    "informationconfirmed",  Boolean.TRUE.equals(nonResidentialPremise.isInformationConfirmed())
            ));
            
            if (nonResidentialPremise.getAnnulmentReason() != null) {
                nonResidentialPremiseForSave.put("is_annuled_in_gis", true);
            } else {
                nonResidentialPremiseForSave.put("is_annuled_in_gis", false);
            }
            
            if(nonResidentialPremiseForSave.containsKey("uuid")) {
                db.update(Entrance.class, nonResidentialPremiseForSave);
            } else {
                db.insertId(Entrance.class, nonResidentialPremiseForSave);
            }
        }
        
        for (Map.Entry<Object, Map<String, Object>> entry : nonResidentialPremisesDb.entrySet()) {
            Map<String, Object> nonResidentialPremise = entry.getValue();
            if (nonResidentialPremise.containsKey("is_used"))
                continue;
            nonResidentialPremise.put("code_vc_nsi_330", "4"); //4 - Дублирование информации, на проде поменять на 6 - В связи с ошибкой ввода данных
            nonResidentialPremise.put("is_annuled_in_gis", true);
            nonResidentialPremise.put("terminationdate", new Date());
            db.update(NonResidentialPremise.class, nonResidentialPremise);
        }
        
        //Жилые помещения
        Map<Object, Map<String, Object>> residentialPremisesDb = db.getIdx(m
                    .select(ResidentialPremise.class, "*")
                    .where("uuid_house", houseUuid)
                    .and("premisesguid IS NOT NULL"),
                "premisesguid");
        
        for (ExportHouseResultType.ApartmentHouse.ResidentialPremises residentialPremise : house.getResidentialPremises()) {
            Map<String, Object> data = HASH(
                "cadastralnumber",       residentialPremise.getCadastralNumber(),
                "premisesnum",           residentialPremise.getPremisesNum(),
                "floor",                 residentialPremise.getFloor(),
                "uuid_entrance",         entranceUuidByNum.get(residentialPremise.getEntranceNum()),
                "code_vc_nsi_30",        residentialPremise.getPremisesCharacteristic() != null ? residentialPremise.getPremisesCharacteristic().getCode() : null,
                "totalarea",             residentialPremise.getTotalArea(),
                "grossarea",             residentialPremise.getGrossArea()
            );
            
            addOGFData(data, residentialPremise.getOGFData());
            
            if (residentialPremise.getAnnulmentReason() != null) {
                data.put("code_vc_nsi_330", residentialPremise.getAnnulmentReason().getCode());
            }
            Map<String, Object> residentialPremiseDb = nonResidentialPremisesDb.get(UUID.fromString(residentialPremise.getPremisesGUID()));
            
            if (residentialPremiseDb == null && StringUtils.isNotBlank(residentialPremise.getCadastralNumber()))
                residentialPremiseDb = db.getMap(m
                        .select(ResidentialPremise.class, "*")
                        .where("uuid_house", houseUuid)
                        .and("cadastralnumber", residentialPremise.getCadastralNumber())
                        .and("premisesguid IS NULL")
                );
            
            if (residentialPremiseDb == null)
                residentialPremiseDb = db.getMap(m
                        .select(ResidentialPremise.class, "*")
                        .where("uuid_house", houseUuid)
                        .and("premisesnum", residentialPremise.getPremisesNum())
                        .and("premisesguid IS NULL")
                );
            
            Map<String, Object> residentialPremiseForSave = getIfDbDataNullOrEmpty(data, residentialPremiseDb);
            
            if (residentialPremiseDb != null) { 
                residentialPremiseForSave.put("uuid", residentialPremiseDb.get("uuid"));
                residentialPremiseDb.put("is_used", true);
            }
            residentialPremiseForSave.putAll(HASH(
                    "premisesguid",          residentialPremise.getPremisesGUID(),
                    "fiaschildhouseguid",    residentialPremise.getFIASChildHouseGuid(),
                    "gis_unique_number",     residentialPremise.getPremisesUniqueNumber(),
                    "terminationdate",       residentialPremise.getTerminationDate(),
                    "gis_modification_date", residentialPremise.getModificationDate(),
                    "informationconfirmed",  Boolean.TRUE.equals(residentialPremise.isInformationConfirmed())
            ));
            
            if (residentialPremise.getAnnulmentReason() != null) {
                residentialPremiseForSave.put("is_annuled_in_gis", true);
            } else {
                residentialPremiseForSave.put("is_annuled_in_gis", false);
            }
            
            Object premiseUuid;
            if(residentialPremiseForSave.containsKey("uuid")) {
                premiseUuid = residentialPremiseForSave.get("uuid");
                db.update(ResidentialPremise.class, residentialPremiseForSave);
            } else {
                premiseUuid = db.insertId(ResidentialPremise.class, residentialPremiseForSave);
            }  
            
            Map<Object, Map<String, Object>> livingRoomsDb = db.getIdx(m
                    .select(LivingRoom.class, "*")
                    .where("uuid_premise", premiseUuid)
                    .and("livingroomguid IS NOT NULL"),
                "livingroomguid");
            
            for (ExportHouseResultType.ApartmentHouse.ResidentialPremises.LivingRoom livingRoom : residentialPremise.getLivingRoom()) {
                Map<String, Object> roomData = HASH(
                    "cadastralnumber", livingRoom.getCadastralNumber(),
                    "roomnumber",      livingRoom.getRoomNumber(),
                    "floor",           livingRoom.getFloor(),
                    "square",          livingRoom.getSquare()
                );

                addOGFData(roomData, livingRoom.getOGFData());

                if (livingRoom.getAnnulmentReason() != null) {
                    roomData.put("code_vc_nsi_330", livingRoom.getAnnulmentReason().getCode());
                }
                Map<String, Object> livingRoomDb = livingRoomsDb.get(UUID.fromString(livingRoom.getLivingRoomGUID()));

                if (livingRoomDb == null && StringUtils.isNotBlank(livingRoom.getCadastralNumber()))
                    livingRoomDb = db.getMap(m
                            .select(LivingRoom.class, "*")
                            .where("uuid_house", premiseUuid)
                            .and("cadastralnumber", livingRoom.getCadastralNumber())
                            .and("livingroomguid IS NULL")
                    );

                if (livingRoomDb == null)
                    livingRoomDb = db.getMap(m
                            .select(ResidentialPremise.class, "*")
                            .where("uuid_house", premiseUuid)
                            .and("premisesnum", livingRoom.getRoomNumber())
                            .and("livingroomguid IS NULL")
                    );

                Map<String, Object> livingRoomForSave = getIfDbDataNullOrEmpty(roomData, livingRoomDb);

                if (livingRoomDb != null) { 
                    livingRoomForSave.put("uuid", livingRoomDb.get("uuid"));
                    livingRoomDb.put("is_used", true);
                }
                livingRoomForSave.putAll(HASH(
                        "livingroomguid",        livingRoom.getLivingRoomGUID(),
                        "gis_unique_number",     livingRoom.getLivingRoomUniqueNumber(),
                        "terminationdate",       livingRoom.getTerminationDate(),
                        "gis_modification_date", livingRoom.getModificationDate(),
                        "informationconfirmed",  Boolean.TRUE.equals(residentialPremise.isInformationConfirmed())
                ));

                if (livingRoom.getAnnulmentReason() != null) {
                    livingRoomForSave.put("is_annuled_in_gis", true);
                } else {
                    livingRoomForSave.put("is_annuled_in_gis", false);
                }

                if(livingRoomForSave.containsKey("uuid")) {
                    db.update(LivingRoom.class, livingRoomForSave);
                } else {
                    db.insertId(LivingRoom.class, livingRoomForSave);
                }  
            }
            
            for (Map.Entry<Object, Map<String, Object>> entry : livingRoomsDb.entrySet()) {
                Map<String, Object> livingRoom = entry.getValue();
                if (livingRoom.containsKey("is_used"))
                    continue;
                livingRoom.put("code_vc_nsi_330", "4"); //4 - Дублирование информации, на проде поменять на 6 - В связи с ошибкой ввода данных
                livingRoom.put("is_annuled_in_gis", true);
                livingRoom.put("terminationdate", new Date());
                db.update(LivingRoom.class, livingRoom);
            }
        }
        
        for (Map.Entry<Object, Map<String, Object>> entry : residentialPremisesDb.entrySet()) {
                Map<String, Object> residentialPremise = entry.getValue();
                if (residentialPremise.containsKey("is_used"))
                    continue;
                residentialPremise.put("code_vc_nsi_330", "4"); //4 - Дублирование информации, на проде поменять на 6 - В связи с ошибкой ввода данных
                residentialPremise.put("is_annuled_in_gis", true);
                residentialPremise.put("terminationdate", new Date());
                db.update(ResidentialPremise.class, residentialPremise);
            }

        /*
        //Подъезды
        Map<UUID, Map<String, Object>> entrances = new HashMap<>();

            db.dupsert (
                Entrance.class, 
                HASH ("uuid_house", houseUuid),
                    house.getEntrance().stream().map(entrance -> {
                        Map<String, Object> data = HASH(
                            "entranceguid",          entrance.getEntranceGUID(),
                            "fiaschildhouseguid",    entrance.getFIASChildHouseGuid(),
                            "entrancenum",           entrance.getEntranceNum(),
                            "storeyscount",          entrance.getStoreysCount(),
                            "creationyear",          entrance.getCreationYear(),
                            "terminationdate",       entrance.getTerminationDate(),
                            "annulmentinfo",         entrance.getAnnulmentInfo(),
                            "gis_modification_date", entrance.getModificationDate(),
                            "informationconfirmed",  Boolean.TRUE.equals(entrance.isInformationConfirmed())
                        );
                        if (entrance.getAnnulmentReason() != null) {
                            data.put("code_vc_nsi_330", entrance.getAnnulmentReason().getCode());
                            data.put("is_annuled_in_gis", true);
                        } else {
                            data.put("code_vc_nsi_330", null);
                            data.put("is_annuled_in_gis", false);
                        }
                        entrances.put(UUID.fromString(entrance.getEntranceGUID()), data);

                        return data;
                    }).collect(Collectors.toList()),
                "entranceguid"
            );
            
            Map<String, Object> entranceUuidByNum = new HashMap<>();
            
            db.forEach(m.select(Entrance.class, "uuid", "entranceguid", "entrancenum").where("uuid_house", houseUuid), (rs) -> {
                UUID uuid = TypeConverter.UUID(rs.getBytes("uuid"));
                entranceUuidByNum.put(rs.getString("entrancenum"), uuid);
                
                entrances.get(TypeConverter.UUID(rs.getBytes("entranceguid"))).put("uuid", uuid);
            });
            
            //Нежилые помещения
            db.dupsert (
                NonResidentialPremise.class, 
                HASH ("uuid_house", houseUuid), 
                house.getNonResidentialPremises().stream().map(nonResisentialPremise -> {
                    Map<String, Object> data = HASH(
                        "premisesguid",          nonResisentialPremise.getPremisesGUID(),
                        "fiaschildhouseguid",    nonResisentialPremise.getFIASChildHouseGuid(),
                        "gis_unique_number",     nonResisentialPremise.getPremisesUniqueNumber(),
                        "cadastralnumber",       nonResisentialPremise.getCadastralNumber(),
                        "premisesnum",           nonResisentialPremise.getPremisesNum(),
                        "terminationdate",       nonResisentialPremise.getTerminationDate(),
                        "floor",                 nonResisentialPremise.getFloor(),
                        "totalarea",             nonResisentialPremise.getTotalArea(),
                        "iscommonproperty",      Boolean.TRUE.equals(nonResisentialPremise.isIsCommonProperty()),
                        "gis_modification_date", nonResisentialPremise.getModificationDate(),
                        "informationconfirmed",  Boolean.TRUE.equals(nonResisentialPremise.isInformationConfirmed())
                    );
                    if (nonResisentialPremise.getAnnulmentReason() != null) {
                        data.put("code_vc_nsi_330", nonResisentialPremise.getAnnulmentReason().getCode());
                        data.put("is_annuled_in_gis", true);
                    } else {
                        data.put("code_vc_nsi_330", null);
                        data.put("is_annuled_in_gis", false);
                    }
                    
                    addOGFData(data, nonResisentialPremise.getOGFData());
                    
                    return data;
                }).collect(Collectors.toList()),
                "premisesguid"
            );
            //Жилые помещения
            Map<UUID, Map<String, Object>> premises = new HashMap<>();
            house.getResidentialPremises().forEach( premise -> {
                
                List<Map<String, Object>> rooms = premise.getLivingRoom().stream().map(room -> {
                    Map<String, Object> data = HASH(
                        "livingroomguid",        room.getLivingRoomGUID(),
                        "gis_unique_number",     room.getLivingRoomUniqueNumber(),
                        "cadastralnumber",       room.getCadastralNumber(),
                        "roomnumber",            room.getRoomNumber(),
                        "terminationdate",       room.getTerminationDate(),
                        "floor",                 room.getFloor(),
                        "square",                room.getSquare(),
                        "gis_modification_date", room.getModificationDate(),
                        "informationconfirmed",  Boolean.TRUE.equals(room.isInformationConfirmed())
                    );
                    if (room.getAnnulmentReason() != null) {
                        data.put("code_vc_nsi_330", room.getAnnulmentReason().getCode());
                        data.put("is_annuled_in_gis", true);
                    } else {
                        data.put("code_vc_nsi_330", null);
                        data.put("is_annuled_in_gis", false);
                    }
                    
                    addOGFData(data, room.getOGFData());
                    
                    return data;
                }).collect(Collectors.toList());
                
                Map<String, Object> premisesHash = HASH(
                    "premisesguid",          premise.getPremisesGUID(),
                    "fiaschildhouseguid",    premise.getFIASChildHouseGuid(),
                    "gis_unique_number",     premise.getPremisesUniqueNumber(),
                    "cadastralnumber",       premise.getCadastralNumber(),
                    "premisesnum",           premise.getPremisesNum(),
                    "terminationdate",       premise.getTerminationDate(),
                    "floor",                 premise.getFloor(),
                    "uuid_entrance",         entranceUuidByNum.get(premise.getEntranceNum()),
                    "code_vc_nsi_30",        premise.getPremisesCharacteristic() != null ? premise.getPremisesCharacteristic().getCode() : null,
                    "totalarea",             premise.getTotalArea(),
                    "grossarea",             premise.getGrossArea(),
                    "gis_modification_date", premise.getModificationDate(),
                    "informationconfirmed",  Boolean.TRUE.equals(premise.isInformationConfirmed()),
                    "livingrooms",           rooms
                );
                
                if (premise.getAnnulmentReason() != null) {
                    premisesHash.put("code_vc_nsi_330", premise.getAnnulmentReason().getCode());
                    premisesHash.put("is_annuled_in_gis", true);
                } else {
                    premisesHash.put("code_vc_nsi_330", null);
                    premisesHash.put("is_annuled_in_gis", false);
                }
                
                addOGFData(premisesHash, premise.getOGFData());
                
                premises.put(UUID.fromString(premise.getPremisesGUID()), premisesHash);
            });
            
            db.dupsert(
                    ResidentialPremise.class,
                    HASH("uuid_house", houseUuid),
                    new ArrayList<>(premises.values()), "premisesguid"
            );
            
            db.forEach(m.select(ResidentialPremise.class, "uuid", "premisesguid").where("uuid_house", houseUuid), (rs) -> {
                premises.get(TypeConverter.UUID(rs.getBytes("premisesguid"))).put("uuid", TypeConverter.UUID(rs.getBytes("uuid")));
            });
            
            premises.values().forEach((premise) -> {
                try {
                    db.dupsert (
                        LivingRoom.class,
                        HASH (
                            "uuid_premise",   premise.get("uuid")
                        ),
                        (List<Map<String, Object>>) premise.get("livingrooms"), "livingroomguid"
                    );
                } catch (SQLException ex) {
                    throw new RuntimeException(ex.getMessage(), ex);
                }
            });
            //Лифты
            db.dupsert (
                Lift.class, 
                HASH ("uuid_house", houseUuid), 
                    house.getLift().stream().map(lift -> {
                        Map<String, Object> data = HASH(
                            "liftguid",              lift.getLiftGUID(),
                            "fiaschildhouseguid",    lift.getFIASChildHouseGuid(),
                            "uuid_entrance",         entranceUuidByNum.get(lift.getEntranceNum()),
                            "entrancenum",           entranceUuidByNum.get(lift.getEntranceNum()) == null ? lift.getEntranceNum() : null,
                            "factorynum",            lift.getFactoryNum(),
                            "code_vc_nsi_192",       lift.getType().getCode(),
                            "terminationdate",       lift.getTerminationDate(),
                            "annulmentinfo",         lift.getAnnulmentInfo(),
                            "gis_modification_date", lift.getModificationDate()
                        );
                        if (lift.getAnnulmentReason() != null) {
                            data.put("code_vc_nsi_330", lift.getAnnulmentReason().getCode());
                            data.put("is_annuled_in_gis", true);
                        } else {
                            data.put("code_vc_nsi_330", null);
                            data.put("is_annuled_in_gis", false);
                        }
                        
                        addOGFData(data, lift.getOGFData());
                        
                        return data;
                    }).collect(Collectors.toList()),
                "liftguid"
            );
            */
    }
    
    private void saveLivingHouse(DB db, Model m, UUID houseUuid, Map<String, Object> record, ExportHouseResultType.LivingHouse house) throws SQLException {
        String fiasHouseGuid = house.getBasicCharacteristicts().getFIASHouseGuid();
            
        addBasicCharacteristic(record, house.getBasicCharacteristicts());
            
        record.putAll(DB.HASH(
            "hasblocks",                     house.isHasBlocks() != null ? house.isHasBlocks() : Boolean.FALSE,
            "hasmultiplehouseswithsameadres", house.isHasMultipleHousesWithSameAddress() != null ? house.isHasMultipleHousesWithSameAddress() : Boolean.FALSE,
            "address", db.getJsonObject(VocBuildingAddress.class, fiasHouseGuid).getString("label")
        ));
            
        Map<String, Object> dbHouseData = db.getMap(House.class, houseUuid);
        Map<String, Object> houseDataForSave = getIfDbDataNullOrEmpty(record, dbHouseData);
        if (record.get("kad_n") != null)
            houseDataForSave.put("kad_n", record.get("kad_n"));
        db.update(House.class, dbHouseData);
         
        dbHouseData.put ("uuid", getUuid ());
        db.update (HouseLog.class, dbHouseData);
            
            //Помещения
            Map<UUID, Map<String, Object>> blocks = new HashMap<>();
            house.getBlock().forEach( block -> {
                
                List<Map<String, Object>> rooms = block.getLivingRoom().stream().map(room -> {
                    Map<String, Object> data = HASH(
                        "livingroomguid",        room.getLivingRoomGUID(),
                        "gis_unique_number",     room.getLivingRoomUniqueNumber(),
                        "cadastralnumber",       room.getCadastralNumber(),
                        "roomnumber",            room.getRoomNumber(),
                        "terminationdate",       room.getTerminationDate(),
                        "floor",                 room.getFloor(),
                        "square",                room.getSquare(),
                        "gis_modification_date", room.getModificationDate(),
                        "informationconfirmed",  Boolean.TRUE.equals(room.isInformationConfirmed())
                    );
                    if (room.getAnnulmentReason() != null) {
                        data.put("code_vc_nsi_330", room.getAnnulmentReason().getCode());
                        data.put("is_annuled_in_gis", true);
                    } else {
                        data.put("code_vc_nsi_330", null);
                        data.put("is_annuled_in_gis", false);
                    }
                    
                    addOGFData(data, room.getOGFData());
                    
                    return data;
                }).collect(Collectors.toList());
                
                Map<String, Object> blocksHash = HASH(
                    "blockguid",             block.getBlockGUID(),
                    "gis_unique_number",     block.getBlockUniqueNumber(),
                    "cadastralnumber",       block.getCadastralNumber(),
                    "blocknum",              block.getBlockNum(),
                    "terminationdate",       block.getTerminationDate(),
                    "code_vc_nsi_30",        block.getPremisesCharacteristic() != null ? block.getPremisesCharacteristic().getCode() : null,
                    "totalarea",             block.getTotalArea(),
                    "grossarea",             block.getGrossArea(),
                    "gis_modification_date", block.getModificationDate(),
                    "informationconfirmed",  Boolean.TRUE.equals(block.isInformationConfirmed()),
                    "is_nrs",                BlockCategoryType.NON_RESIDENTIAL.equals(block.getCategory()),
                    "livingrooms",           rooms
                );
                
                if (block.getAnnulmentReason() != null) {
                    blocksHash.put("code_vc_nsi_330", block.getAnnulmentReason().getCode());
                    blocksHash.put("is_annuled_in_gis", true);
                } else {
                    blocksHash.put("code_vc_nsi_330", null);
                    blocksHash.put("is_annuled_in_gis", false);
                }
                
                addOGFData(blocksHash, block.getOGFData());
                
                blocks.put(UUID.fromString(block.getBlockGUID()), blocksHash);
            });
            
            db.dupsert (
                Block.class, 
                HASH (
                    "uuid_house", houseUuid
                ), 
                new ArrayList<> (blocks.values()), "blockguid"
            );
            
            db.forEach(m.select(Block.class, "uuid", "blockguid").where("uuid_house", houseUuid), (rs) -> {
                blocks.get(TypeConverter.UUID(rs.getBytes("blockguid"))).put("uuid", TypeConverter.UUID(rs.getBytes("uuid")));
            });
            
            blocks.values().forEach((block) -> {
                try {
                    db.dupsert (
                        LivingRoom.class,
                        HASH (
                            "uuid_premise",   block.get("uuid")
                        ),
                        (List<Map<String, Object>>) block.get("livingrooms"), "livingroomguid"
                    );
                } catch (SQLException ex) {
                    throw new RuntimeException(ex.getMessage(), ex);
                }
            });
            
            db.dupsert (
                LivingRoom.class,
                HASH (
                    "uuid_house", houseUuid
                ),
                house.getLivingRoom().stream().map(room -> {
                    Map<String, Object> data = HASH(
                        "livingroomguid",        room.getLivingRoomGUID(),
                        "gis_unique_number",     room.getLivingRoomUniqueNumber(),
                        "cadastralnumber",       room.getCadastralNumber(),
                        "roomnumber",            room.getRoomNumber(),
                        "terminationdate",       room.getTerminationDate(),
                        "floor",                 room.getFloor(),
                        "square",                room.getSquare(),
                        "gis_modification_date", room.getModificationDate(),
                        "informationconfirmed",  Boolean.TRUE.equals(room.isInformationConfirmed())
                    );
                    
                    if (room.getAnnulmentReason() != null) {
                        data.put("code_vc_nsi_330", room.getAnnulmentReason().getCode());
                        data.put("is_annuled_in_gis", true);
                    } else {
                        data.put("code_vc_nsi_330", null);
                        data.put("is_annuled_in_gis", false);
                    }
                    
                    addOGFData(data, room.getOGFData());
                    
                    return data;
                }).collect(Collectors.toList()), 
                "livingroomguid"
            );
    }
    
    private void addBasicCharacteristic (Map<String, Object> r, HouseBasicExportType basic) {
        r.putAll(DB.HASH (
            "annulmentinfo",    basic.getAnnulmentInfo(),
            "code_vc_nsi_330",  basic.getAnnulmentReason() != null ? basic.getAnnulmentReason().getCode() : null,
            "kad_n",            basic.getCadastralNumber(),
            "fiashouseguid",    basic.getFIASHouseGuid(),
            "floorcount",       basic.getFloorCount(),
            "code_vc_nsi_32",   basic.getOlsonTZ() != null ? basic.getOlsonTZ().getCode() : null,
            "code_vc_nsi_24",   basic.getState() != null ? basic.getState().getCode() : null,
            "terminationdate",  basic.getTerminationDate(),
            "totalsquare",      basic.getTotalSquare(),
            "usedyear",         basic.getUsedYear(),
            "culturalheritage", Boolean.TRUE.equals(basic.isCulturalHeritage()),
            "code_vc_nsi_336",  basic.getLifeCycleStage() != null ? basic.getLifeCycleStage().getCode() : null
        ));
        addOGFData(r, basic.getOGFData());
    }
    
    private void addOGFData (Map<String, Object> r, List<OGFData> ogfData) {
        if (ogfData.isEmpty())
            return;
        
        ogfData.forEach(ogf -> {
            OGFDataValue ogfDataValue = ogf.getValue();
            Object value = null;
            if (ogfDataValue.isBooleanValue() != null)  value = ogfDataValue.isBooleanValue();
            if (ogfDataValue.getDateTimeValue()!= null) value = ogfDataValue.getDateTimeValue();
            if (ogfDataValue.getFile() != null)         value = ogfDataValue.getFile(); //TODO Скачать файл
            if (ogfDataValue.getFloatValue() != null)   value = ogfDataValue.getFloatValue();
            if (ogfDataValue.getIntegerValue() != null) value = ogfDataValue.getIntegerValue();
            if (ogfDataValue.getNsiCode() != null)      value = ogfDataValue.getNsiCode();
            if (ogfDataValue.getStringValue() != null)  value = ogfDataValue.getStringValue();
            r.put("f_" + ogf.getCode(), value);
        });
    }
    
    private Map<String, Object> getIfDbDataNullOrEmpty(Map<String, Object> newData, Map<String, Object> dbData) {
        if (dbData == null)
            return newData;
        
        Map<String, Object> result = new HashMap<>();
        
        newData.forEach((key, value) -> {
            Object o = dbData.get(key);
            if (o != null && !(o instanceof String && StringUtils.isEmpty((String)o)))
                return;
            result.put(key, value);
            dbData.put(key, value);
        });
        return result;
    }
}