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
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.gis.EntranceGis;
import ru.eludia.products.mosgis.db.model.gis.HouseGis;
import ru.eludia.products.mosgis.db.model.gis.LiftGis;
import ru.eludia.products.mosgis.db.model.gis.PremiseGis;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.wsc.WsGisExportHouseClient;
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
    protected WsGisExportHouseClient wsGisExportHouseClient;

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

        Map<String, Object> record = DB.HASH (
            "unique_number",         result.getHouseUniqueNumber(),
            "gis_modification_date", result.getModificationDate(),
            "is_condo",              result.getApartmentHouse() != null
        );
        
        if (result.getApartmentHouse() != null) {
            ExportHouseResultType.ApartmentHouse house = result.getApartmentHouse();
            String fiasHouseGuid = house.getBasicCharacteristicts().getFIASHouseGuid();
            record.putAll(basicCharacteristicToMap(house.getBasicCharacteristicts()));
            record.putAll(DB.HASH(
                "code_vc_nsi_25",          house.getHouseManagementType() != null ? house.getHouseManagementType().getCode() : null,
                "min_floor_count",         house.getMinFloorCount(),
                "code_vc_nsi_241",         house.getOverhaulFormingKind(),
                "underground_floor_count", house.getUndergroundFloorCount()
            ));
            
            db.upsert (HouseGis.class, record);
            //Подъезды
            db.dupsert (
                EntranceGis.class, 
                HASH ("uuid_house_gis", fiasHouseGuid), 
                    house.getEntrance().stream().map(item -> {
                        return HASH(
                            "uuid",                  item.getEntranceGUID(),
                            "fias_child_house_guid", item.getFIASChildHouseGuid(),
                            "entrance_num",          item.getEntranceNum(),
                            "storeys_count",         item.getStoreysCount(),
                            "creation_year",         item.getCreationYear(),
                            "termination_date",      item.getTerminationDate(),
                            "code_vc_nsi_330",       item.getAnnulmentReason() != null ? item.getAnnulmentReason().getCode() : null,
                            "annulment_info",        item.getAnnulmentInfo(),
                            "gis_modification_date", item.getModificationDate(),
                            "information_confirmed", item.isInformationConfirmed()
                        );
                    }).collect(Collectors.toList()),
                "uuid"
            );
            //Нежилые помещения
            db.dupsert (
                PremiseGis.class, 
                HASH (
                    "uuid_house_gis", fiasHouseGuid,
                    "code_vc_nsi_11", "2" //Нежилое помещение
                ), 
                house.getNonResidentialPremises().stream().map(item -> {
                    return HASH(
                        "uuid",                  item.getPremisesGUID(),
                        "fias_child_house_guid", item.getFIASChildHouseGuid(),
                        "unique_number",         item.getPremisesUniqueNumber(),
                        "cadastral_number",      item.getCadastralNumber(),
                        "num",                   item.getPremisesNum(),
                        "termination_date",      item.getTerminationDate(),
                        "code_vc_nsi_330",       item.getAnnulmentReason() != null ? item.getAnnulmentReason().getCode() : null,
                        "floor",                 item.getFloor(),
                        "total_area",            item.getTotalArea(),
                        "is_common_property",    item.isIsCommonProperty(),
                        "gis_modification_date", item.getModificationDate(),
                        "information_confirmed", item.isInformationConfirmed()
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
                    "unique_number",         premise.getPremisesUniqueNumber(),
                    "cadastral_number",      premise.getCadastralNumber(),
                    "num",                   premise.getPremisesNum(),
                    "termination_date",      premise.getTerminationDate(),
                    "code_vc_nsi_330",       premise.getAnnulmentReason() != null ? premise.getAnnulmentReason().getCode() : null,
                    "floor",                 premise.getFloor(),
                    "entrance_num",          premise.getEntranceNum(),
                    "code_vc_nsi_30",        premise.getPremisesCharacteristic() != null ? premise.getPremisesCharacteristic().getCode() : null,
                    "total_area",            premise.getTotalArea(),
                    "gross_area",            premise.getGrossArea(),
                    "gis_modification_date", premise.getModificationDate(),
                    "information_confirmed", premise.isInformationConfirmed()
                ));
                if (!premise.getLivingRoom().isEmpty()) {
                    rooms.put(premise.getPremisesGUID(), 
                            premise.getLivingRoom().stream().map(room -> {
                                return HASH("uuid",          room.getLivingRoomGUID(),
                                    "unique_number",         room.getLivingRoomUniqueNumber(),
                                    "cadastral_number",      room.getCadastralNumber(),
                                    "num",                   room.getRoomNumber(),
                                    "termination_date",      room.getTerminationDate(),
                                    "code_vc_nsi_330",       room.getAnnulmentReason() != null ? room.getAnnulmentReason().getCode() : null,
                                    "floor",                 room.getFloor(),
                                    "total_area",            room.getSquare(),
                                    "gis_modification_date", room.getModificationDate(),
                                    "information_confirmed", room.isInformationConfirmed()
                                );
                            }).collect(Collectors.toList()));
                }
            });
            
            db.dupsert (
                PremiseGis.class, 
                HASH (
                    "uuid_house_gis", fiasHouseGuid,
                    "code_vc_nsi_11",          "1", //Жилое помещение
                    "code_vc_nsi_231",         "2" //Квартира    
                ), 
                premises, "uuid"
            );
            
            rooms.forEach((premiseGuid, roomList) -> {
                try {
                    db.dupsert (
                        PremiseGis.class,
                        HASH (
                            "uuid_house_gis", fiasHouseGuid,
                            "code_vc_nsi_11",          "1", //Жилое помещение
                            "code_vc_nsi_231",         "1", //Комната
                            "parent",                  premiseGuid
                        ),
                        roomList, "uuid"
                    );
                } catch (SQLException ex) {
                    throw new RuntimeException(ex.getMessage(), ex);
                }
            });
            //Лифты
            db.dupsert (
                LiftGis.class, 
                HASH ("uuid_house_gis", fiasHouseGuid), 
                    house.getLift().stream().map(item -> {
                        return HASH(
                            "uuid",                  item.getLiftGUID(),
                            "fias_child_house_guid", item.getFIASChildHouseGuid(),
                            "entrance_num",          item.getEntranceNum(),
                            "factory_num",           item.getFactoryNum(),
                            "code_vc_nsi_192",       item.getType().getCode(),
                            "termination_date",      item.getTerminationDate(),
                            "code_vc_nsi_330",       item.getAnnulmentReason() != null ? item.getAnnulmentReason().getCode() : null,
                            "annulment_info",        item.getAnnulmentInfo(),
                            "gis_modification_date", item.getModificationDate()
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
                "has_blocks",                     house.isHasBlocks() != null ? house.isHasBlocks() : Boolean.FALSE,
                "has_multi_houses_with_same_adr", house.isHasMultipleHousesWithSameAddress() != null ? house.isHasMultipleHousesWithSameAddress() : Boolean.FALSE
            ));
            
            db.upsert (HouseGis.class, record);
            
            //Помещения
            List<Map<String, Object>> blocks = new ArrayList<>();
            Map<String, List<Map<String, Object>>> rooms = new HashMap<>();
            house.getBlock().forEach( block -> {
                blocks.add(HASH("uuid",      block.getBlockGUID(),
                    "unique_number",         block.getBlockUniqueNumber(),
                    "cadastral_number",      block.getCadastralNumber(),
                    "num",                   block.getBlockNum(),
                    "termination_date",      block.getTerminationDate(),
                    "code_vc_nsi_330",       block.getAnnulmentReason() != null ? block.getAnnulmentReason().getCode() : null,
                    "code_vc_nsi_30",        block.getPremisesCharacteristic() != null ? block.getPremisesCharacteristic().getCode() : null,
                    "total_area",            block.getTotalArea(),
                    "gross_area",            block.getGrossArea(),
                    "gis_modification_date", block.getModificationDate(),
                    "information_confirmed", block.isInformationConfirmed(),
                    "code_vc_nsi_11",        block.getCategory() == null || BlockCategoryType.RESIDENTIAL.equals(block.getCategory()) ? "1" : "2"
                ));
                if (!block.getLivingRoom().isEmpty()) {
                    rooms.put(block.getBlockGUID(), 
                        block.getLivingRoom().stream().map(room -> {
                            return HASH("uuid",          room.getLivingRoomGUID(),
                                "unique_number",         room.getLivingRoomUniqueNumber(),
                                "cadastral_number",      room.getCadastralNumber(),
                                "num",                   room.getRoomNumber(),
                                "termination_date",      room.getTerminationDate(),
                                "code_vc_nsi_330",       room.getAnnulmentReason() != null ? room.getAnnulmentReason().getCode() : null,
                                "floor",                 room.getFloor(),
                                "total_area",            room.getSquare(),
                                "gis_modification_date", room.getModificationDate(),
                                "information_confirmed", room.isInformationConfirmed(),
                                "code_vc_nsi_11",        "1" //Жилое помещение
                            );
                        }).collect(Collectors.toList()));
                }
            });
            
            db.dupsert (
                PremiseGis.class, 
                HASH (
                    "uuid_house_gis", fiasHouseGuid,
                    "code_vc_nsi_231",         "2" //Квартира    
                ), 
                blocks, "uuid"
            );
            
            rooms.forEach((premiseGuid, roomList) -> {
                try {
                    db.dupsert (
                        PremiseGis.class,
                        HASH (
                            "uuid_house_gis", fiasHouseGuid,
                            "code_vc_nsi_231",         "1", //Комната
                            "parent",                  premiseGuid
                        ),
                        roomList, "uuid"
                    );
                } catch (SQLException ex) {
                    throw new RuntimeException(ex.getMessage(), ex);
                }
            });
            
            db.dupsert (
                PremiseGis.class,
                HASH (
                    "uuid_house_gis", fiasHouseGuid,
                    "code_vc_nsi_11",          "1", //Жилое помещение
                    "code_vc_nsi_231",         "1" //Комната
                ),
                house.getLivingRoom().stream().map(room -> {
                    return HASH("uuid",          room.getLivingRoomGUID(),
                        "unique_number",         room.getLivingRoomUniqueNumber(),
                        "cadastral_number",      room.getCadastralNumber(),
                        "num",                   room.getRoomNumber(),
                        "termination_date",      room.getTerminationDate(),
                        "code_vc_nsi_330",       room.getAnnulmentReason() != null ? room.getAnnulmentReason().getCode() : null,
                        "floor",                 room.getFloor(),
                        "total_area",            room.getSquare(),
                        "gis_modification_date", room.getModificationDate(),
                        "information_confirmed", room.isInformationConfirmed(),
                        "code_vc_nsi_11",        "1" //Жилое помещение
                    );
                }).collect(Collectors.toList()), 
                "uuid"
            );
        }
    }
    
    private Map<String, Object> basicCharacteristicToMap (HouseBasicExportType basic) {
        basic.getOGFData();
        return DB.HASH (
            "annulment_info",    basic.getAnnulmentInfo(),
            "code_vc_nsi_330",   basic.getAnnulmentReason() != null ? basic.getAnnulmentReason().getCode() : null,
            "cadastral_number",  basic.getCadastralNumber(),
            "fias_house_guid",   basic.getFIASHouseGuid(),
            "floor_count",       basic.getFloorCount(),
            "oktmo",             basic.getOKTMO() != null ? basic.getOKTMO().getCode() : null,
            "code_vc_nsi_32",    basic.getOlsonTZ() != null ? basic.getOlsonTZ().getCode() : null,
            "code_vc_nsi_24",    basic.getState() != null ? basic.getState().getCode() : null,
            "termination_date",  basic.getTerminationDate(),
            "total_square",      basic.getTotalSquare(),
            "used_year",         basic.getUsedYear(),
            "cultural_heritage", basic.isCulturalHeritage() != null ? basic.isCulturalHeritage() : Boolean.FALSE
        );
    }

}