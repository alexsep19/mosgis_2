package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.base.model.def.Bool;
import ru.eludia.products.mosgis.db.model.tables.Block;
import ru.eludia.products.mosgis.db.model.tables.Entrance;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.tables.Lift;
import ru.eludia.products.mosgis.db.model.tables.LivingRoom;
import ru.eludia.products.mosgis.db.model.tables.NonResidentialPremise;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.ResidentialPremise;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocBuildingAddress;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.wsc.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.house_management.BlockCategoryType;
import ru.gosuslugi.dom.schema.integration.house_management.ExportHouseResultType;
import ru.gosuslugi.dom.schema.integration.house_management.GetStateResult;
import ru.gosuslugi.dom.schema.integration.house_management.HouseBasicExportType;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportHouseQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportHouse extends UUIDMDB<OutSoap> {
    
    @EJB
    protected WsGisHouseManagementClient wsGisExportHouseClient;

    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ()
            .get    (getTable (), uuid, "*")
        ;        
    }

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {

        try {
            
            db.begin ();

            GetStateResult rp = wsGisExportHouseClient.getState ((UUID) r.get ("uuid_ack"));

            ErrorMessageType errorMessage = rp.getErrorMessage ();

            if (errorMessage != null) {

                if ("INT002000".equals (errorMessage.getErrorCode ())) {
                    
                    logger.warning ("House not found");
                
                    db.update (OutSoap.class, HASH (
                        "uuid", uuid,
                        "id_status", DONE.getId ()
                    ));
                    
                }
                else {

                    logger.warning (errorMessage.getErrorCode () + " " + errorMessage.getDescription ());

                    db.update (OutSoap.class, HASH (
                        "uuid", uuid,
                        "id_status", DONE.getId (),
                        "is_failed", 1,
                        "err_code",  errorMessage.getErrorCode (),
                        "err_text",  errorMessage.getDescription ()
                    ));

                }                

                return;

            }
            handleExportHouseResult (rp.getExportHouseResult(), db);

            db.update (OutSoap.class, HASH (
                "uuid", uuid,
                "id_status", DONE.getId ()
            ));
            
            db.commit ();

        }
        catch (Fault ex) {
            logger.log (Level.SEVERE, null, ex);
        }
        
    }

    private void handleExportHouseResult (ExportHouseResultType result, DB db) throws SQLException {

        Model m = ModelHolder.getModel ();
        
        Map<String, Object> record = DB.HASH (
            "gis_unique_number",     result.getHouseUniqueNumber(),
            "gis_modification_date", result.getModificationDate(),
            "is_condo",              result.getApartmentHouse() != null
        );
        
        if (result.getApartmentHouse() != null) {
            ExportHouseResultType.ApartmentHouse house = result.getApartmentHouse();
            String fiasHouseGuid = house.getBasicCharacteristicts().getFIASHouseGuid();
            record.putAll(basicCharacteristicToMap(house.getBasicCharacteristicts()));
            house.getEntrance();
            record.putAll(DB.HASH(
                "code_vc_nsi_25",        house.getHouseManagementType() != null ? house.getHouseManagementType().getCode() : null,
                "minfloorcount",         house.getMinFloorCount(),
                "code_vc_nsi_241",       house.getOverhaulFormingKind(),
                "undergroundfloorcount", house.getUndergroundFloorCount(),
                "address", db.getJsonObject(VocBuildingAddress.class, fiasHouseGuid).getString("label")
            ));
            
            db.upsert (House.class, record, "fiashouseguid");
            String houseUuid = db.getString(m.select (House.class, "uuid").where ("fiashouseguid", fiasHouseGuid));
                    
            //Подъезды
            Map<String, String> entranceGuidByNum = house.getEntrance().stream().collect(
                    Collectors.toMap(
                            ExportHouseResultType.ApartmentHouse.Entrance::getEntranceNum, 
                            ExportHouseResultType.ApartmentHouse.Entrance::getEntranceGUID
                    )
            );
            
            db.dupsert (
                Entrance.class, 
                HASH ("uuid_house", houseUuid), 
                    house.getEntrance().stream().map(entrance -> {
                        return HASH(
                            "uuid",                  entrance.getEntranceGUID(),
                            "fias_child_house_guid", entrance.getFIASChildHouseGuid(),
                            "entrancenum",           entrance.getEntranceNum(),
                            "storeyscount",          entrance.getStoreysCount(),
                            "creationyear",          entrance.getCreationYear(),
                            "terminationdate",       entrance.getTerminationDate(),
                            "code_vc_nsi_330",       entrance.getAnnulmentReason() != null ? entrance.getAnnulmentReason().getCode() : null,
                            "annulmentinfo",         entrance.getAnnulmentInfo(),
                            "gis_modification_date", entrance.getModificationDate(),
                            "information_confirmed", entrance.isInformationConfirmed()
                        );
                    }).collect(Collectors.toList()),
                "uuid"
            );
            //Нежилые помещения
            db.dupsert (
                NonResidentialPremise.class, 
                HASH ("uuid_house", houseUuid), 
                house.getNonResidentialPremises().stream().map(nonResisentialPremise -> {
                    return HASH(
                        "uuid",                  nonResisentialPremise.getPremisesGUID(),
                        "fias_child_house_guid", nonResisentialPremise.getFIASChildHouseGuid(),
                        "gis_unique_number",     nonResisentialPremise.getPremisesUniqueNumber(),
                        "cadastralnumber",       nonResisentialPremise.getCadastralNumber(),
                        "premisesnum",           nonResisentialPremise.getPremisesNum(),
                        "terminationdate",       nonResisentialPremise.getTerminationDate(),
                        "code_vc_nsi_330",       nonResisentialPremise.getAnnulmentReason() != null ? nonResisentialPremise.getAnnulmentReason().getCode() : null,
                        "floor",                 nonResisentialPremise.getFloor(),
                        "totalarea",             nonResisentialPremise.getTotalArea(),
                        "iscommonproperty",      nonResisentialPremise.isIsCommonProperty(),
                        "gis_modification_date", nonResisentialPremise.getModificationDate(),
                        "information_confirmed", nonResisentialPremise.isInformationConfirmed()
                    );
                }).collect(Collectors.toList()),
                "uuid"
            );
            //Жилые помещения
            List<Map<String, Object>> premises = new ArrayList<>();
            Map<String, List<Map<String, Object>>> rooms = new HashMap<>();
            house.getResidentialPremises().forEach( premise -> {
                premises.add(HASH("uuid",    premise.getPremisesGUID(),
                    "fias_child_house_guid", premise.getFIASChildHouseGuid(),
                    "gis_unique_number",     premise.getPremisesUniqueNumber(),
                    "cadastralnumber",       premise.getCadastralNumber(),
                    "premisesnum",           premise.getPremisesNum(),
                    "terminationdate",       premise.getTerminationDate(),
                    "code_vc_nsi_330",       premise.getAnnulmentReason() != null ? premise.getAnnulmentReason().getCode() : null,
                    "floor",                 premise.getFloor(),
                    "uuid_entrance",         entranceGuidByNum.get(premise.getEntranceNum()),
                    "code_vc_nsi_30",        premise.getPremisesCharacteristic() != null ? premise.getPremisesCharacteristic().getCode() : null,
                    "totalarea",             premise.getTotalArea(),
                    "grossarea",             premise.getGrossArea(),
                    "gis_modification_date", premise.getModificationDate(),
                    "information_confirmed", premise.isInformationConfirmed()
                ));
                if (!premise.getLivingRoom().isEmpty()) {
                    rooms.put(premise.getPremisesGUID(), 
                            premise.getLivingRoom().stream().map(room -> {
                                return HASH("uuid",          room.getLivingRoomGUID(),
                                    "gis_unique_number",     room.getLivingRoomUniqueNumber(),
                                    "cadastralnumber",       room.getCadastralNumber(),
                                    "roomnumber",            room.getRoomNumber(),
                                    "terminationdate",       room.getTerminationDate(),
                                    "code_vc_nsi_330",       room.getAnnulmentReason() != null ? room.getAnnulmentReason().getCode() : null,
                                    "floor",                 room.getFloor(),
                                    "totalarea",             room.getSquare(),
                                    "gis_modification_date", room.getModificationDate(),
                                    "information_confirmed", room.isInformationConfirmed()
                                );
                            }).collect(Collectors.toList()));
                }
            });
            
            db.dupsert (
                ResidentialPremise.class, 
                HASH (
                    "uuid_house", houseUuid
                ), 
                premises, "uuid"
            );
            
            rooms.forEach((premiseGuid, roomList) -> {
                try {
                    db.dupsert (
                        LivingRoom.class,
                        HASH (
                            "uuid_premise",   premiseGuid
                        ),
                        roomList, "uuid"
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
                        return HASH("uuid",          lift.getLiftGUID(),
                            "fias_child_house_guid", lift.getFIASChildHouseGuid(),
                            "uuid_entrance",         entranceGuidByNum.get(lift.getEntranceNum()),
                            "entrancenum",           entranceGuidByNum.get(lift.getEntranceNum()) == null ? lift.getEntranceNum() : null,
                            "factorynum",            lift.getFactoryNum(),
                            "code_vc_nsi_192",       lift.getType().getCode(),
                            "terminationdate",       lift.getTerminationDate(),
                            "code_vc_nsi_330",       lift.getAnnulmentReason() != null ? lift.getAnnulmentReason().getCode() : null,
                            "annulmentinfo",         lift.getAnnulmentInfo(),
                            "gis_modification_date", lift.getModificationDate()
                        );
                    }).collect(Collectors.toList()),
                "uuid"
            );
        } else {
            //ЖД
            ExportHouseResultType.LivingHouse house = result.getLivingHouse();
            String fiasHouseGuid = house.getBasicCharacteristicts().getFIASHouseGuid();
            record.putAll(basicCharacteristicToMap(house.getBasicCharacteristicts()));
            record.putAll(DB.HASH(
                "hasblocks",                     house.isHasBlocks() != null ? house.isHasBlocks() : Boolean.FALSE,
                "hasmultiplehouseswithsameadres", house.isHasMultipleHousesWithSameAddress() != null ? house.isHasMultipleHousesWithSameAddress() : Boolean.FALSE,
                "address", db.getJsonObject(VocBuildingAddress.class, fiasHouseGuid).getString("label")
            ));
            
            db.upsert (House.class, record, "fiashouseguid");
            String houseUuid = db.getString(m.select (House.class, "uuid").where ("fiashouseguid", fiasHouseGuid));
            
            //Помещения
            List<Map<String, Object>> blocks = new ArrayList<>();
            Map<String, List<Map<String, Object>>> rooms = new HashMap<>();
            house.getBlock().forEach( block -> {
                blocks.add(HASH("uuid",      block.getBlockGUID(),
                    "gis_unique_number",     block.getBlockUniqueNumber(),
                    "cadastralnumber",       block.getCadastralNumber(),
                    "blocknum",              block.getBlockNum(),
                    "terminationdate",       block.getTerminationDate(),
                    "code_vc_nsi_330",       block.getAnnulmentReason() != null ? block.getAnnulmentReason().getCode() : null,
                    "code_vc_nsi_30",        block.getPremisesCharacteristic() != null ? block.getPremisesCharacteristic().getCode() : null,
                    "totalarea",             block.getTotalArea(),
                    "grossarea",             block.getGrossArea(),
                    "gis_modification_date", block.getModificationDate(),
                    "information_confirmed", block.isInformationConfirmed(),
                    "is_nrs",        BlockCategoryType.NON_RESIDENTIAL.equals(block.getCategory()) ? Bool.TRUE : Bool.FALSE
                ));
                if (!block.getLivingRoom().isEmpty()) {
                    rooms.put(block.getBlockGUID(), 
                        block.getLivingRoom().stream().map(room -> {
                            return HASH("uuid",          room.getLivingRoomGUID(),
                                "gis_unique_number",     room.getLivingRoomUniqueNumber(),
                                "cadastralnumber",       room.getCadastralNumber(),
                                "roomnumber",            room.getRoomNumber(),
                                "terminationdate",       room.getTerminationDate(),
                                "code_vc_nsi_330",       room.getAnnulmentReason() != null ? room.getAnnulmentReason().getCode() : null,
                                "floor",                 room.getFloor(),
                                "totalarea",             room.getSquare(),
                                "gis_modification_date", room.getModificationDate(),
                                "information_confirmed", room.isInformationConfirmed()
                            );
                        }).collect(Collectors.toList()));
                }
            });
            
            db.dupsert (
                Block.class, 
                HASH (
                    "uuid_house", houseUuid 
                ), 
                blocks, "uuid"
            );
            
            rooms.forEach((premiseGuid, roomList) -> {
                try {
                    db.dupsert (
                        LivingRoom.class,
                        HASH (
                            "uuid_block",     premiseGuid
                        ),
                        roomList, "uuid"
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
                    return HASH("uuid",          room.getLivingRoomGUID(),
                        "gis_unique_number",     room.getLivingRoomUniqueNumber(),
                        "cadastralnumber",       room.getCadastralNumber(),
                        "roomnumber",            room.getRoomNumber(),
                        "terminationdate",       room.getTerminationDate(),
                        "code_vc_nsi_330",       room.getAnnulmentReason() != null ? room.getAnnulmentReason().getCode() : null,
                        "floor",                 room.getFloor(),
                        "totalarea",             room.getSquare(),
                        "gis_modification_date", room.getModificationDate(),
                        "information_confirmed", room.isInformationConfirmed()
                    );
                }).collect(Collectors.toList()), 
                "uuid"
            );
        }
    }
    
    private Map<String, Object> basicCharacteristicToMap (HouseBasicExportType basic) {
        basic.getOGFData();
        return DB.HASH (
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
            "culturalheritage", basic.isCulturalHeritage() != null ? basic.isCulturalHeritage() : Boolean.FALSE
        );
    }

}