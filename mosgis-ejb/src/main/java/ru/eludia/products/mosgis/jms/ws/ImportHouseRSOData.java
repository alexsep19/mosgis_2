package ru.eludia.products.mosgis.jms.ws;

import static ru.eludia.base.DB.HASH;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.db.util.TypeConverter;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.tables.Block;
import ru.eludia.products.mosgis.db.model.tables.Entrance;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.tables.LivingRoom;
import ru.eludia.products.mosgis.db.model.tables.NonResidentialPremise;
import ru.eludia.products.mosgis.db.model.tables.ResidentialPremise;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocUnom;
import ru.eludia.products.mosgis.db.model.voc.VocUnomStatus;
import ru.eludia.products.mosgis.db.model.ws.WsMessages;
import ru.eludia.products.mosgis.jms.base.WsMDB;
import ru.eludia.products.mosgis.util.StringUtils;
import ru.eludia.products.mosgis.ws.soap.impl.base.Errors;
import ru.eludia.products.mosgis.ws.soap.impl.base.Fault;
import ru.eludia.products.mosgis.ws.soap.tools.SOAPTools;
import ru.gosuslugi.dom.schema.integration.base.BaseAsyncResponseType;
import ru.gosuslugi.dom.schema.integration.base.OKTMORefType;
import ru.gosuslugi.dom.schema.integration.house_management.BlockCategoryType;
import ru.gosuslugi.dom.schema.integration.house_management.GKNEGRPKeyRSOType;
import ru.gosuslugi.dom.schema.integration.house_management.GetStateResult;
import ru.gosuslugi.dom.schema.integration.house_management.HouseBasicRSOType;
import ru.gosuslugi.dom.schema.integration.house_management.HouseBasicUpdateRSOType;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseRSORequest;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseRSORequest.ApartmentHouse;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseRSORequest.ApartmentHouse.ApartmentHouseToCreate;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseRSORequest.ApartmentHouse.ApartmentHouseToUpdate;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseRSORequest.ApartmentHouse.EntranceToCreate;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseRSORequest.ApartmentHouse.EntranceToUpdate;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseRSORequest.ApartmentHouse.NonResidentialPremiseToCreate;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseRSORequest.ApartmentHouse.NonResidentialPremiseToUpdate;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseRSORequest.ApartmentHouse.ResidentialPremises;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseRSORequest.LivingHouse;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseRSORequest.LivingHouse.Blocks;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseRSORequest.LivingHouse.LivingHouseToCreate;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseRSORequest.LivingHouse.LivingHouseToUpdate;
import ru.gosuslugi.dom.schema.integration.house_management.ImportResult;
import ru.gosuslugi.dom.schema.integration.house_management.ImportResult.CommonResult;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiRef;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inSoapImportHouseRSOData")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ImportHouseRSOData extends WsMDB {
    
	private final static Pattern UNOM_PATTERN = Pattern.compile("([0]){8}-([0]){4}-([0]){4}-([0]){4}-([0-9]){12}");

	private final static JAXBContext jc;
    
    static {
        try {
            jc = JAXBContext.newInstance (
                GetStateResult.class,
                ImportHouseRSORequest.class
            );
        }
        catch (JAXBException ex) {
            throw new IllegalStateException (ex);
        }
    }

    @Override
    protected JAXBContext getJAXBContext() throws JAXBException {
        return jc;
    }
    
    @Override
	protected Class<GetStateResult> getGetStateResultClass() {
		return GetStateResult.class;
	}

    @Override
    protected BaseAsyncResponseType generateResponse (DB db, Map<String, Object> r, Object request) throws Exception {
        if (!(request instanceof ImportHouseRSORequest)) throw new Fault (Errors.FMT001300);
        return fill (db, r, (ImportHouseRSORequest) request);
    }

	private GetStateResult fill(DB db, Map<String, Object> r, ImportHouseRSORequest importHouseRSORequest)
			throws SQLException {
		GetStateResult result = new GetStateResult();
		if (importHouseRSORequest.getApartmentHouse() != null)
			result.getImportResult().add(saveApartmentHouse(db, r, importHouseRSORequest.getApartmentHouse()));
		else
			result.getImportResult().add(saveLivingHouse(db, r, importHouseRSORequest.getLivingHouse()));
		return result;
	}
    
	private ImportResult saveApartmentHouse(DB db, Map<String, Object> r, ApartmentHouse house) throws SQLException {
    	ImportResult importResult = new ImportResult();
    	boolean isCreate = false;
    	
    	CommonResult commonResult = new CommonResult();
    	importResult.getCommonResult().add(commonResult);
    	try {
	    	UUID houseId;
    		if (house.getApartmentHouseToCreate() != null) {
	    		isCreate = true;
	    		ApartmentHouseToCreate houseToCreate = house.getApartmentHouseToCreate();
	    		HouseBasicRSOType basicCharacteristic = houseToCreate.getBasicCharacteristicts();
	        	
	    		houseId = saveHouseData(db, commonResult, houseToCreate.getTransportGUID(),
						basicCharacteristic.getFIASHouseGuid(), basicCharacteristic.getOKTMO(),
						basicCharacteristic.getOlsonTZ(), basicCharacteristic, false, isCreate);
	        } else {
	        	ApartmentHouseToUpdate houseToUpdate = house.getApartmentHouseToUpdate();
	        	HouseBasicUpdateRSOType basicCharacteristic = houseToUpdate.getBasicCharacteristicts();
	        	
	        	houseId = saveHouseData(db, commonResult, houseToUpdate.getTransportGUID(),
						basicCharacteristic.getFIASHouseGuid(), basicCharacteristic.getOKTMO(),
						basicCharacteristic.getOlsonTZ(), basicCharacteristic, false, isCreate);
	        }
	        
			for (EntranceToCreate entranceToCreate : house.getEntranceToCreate()) {
				CommonResult entranceCommonResult = saveEntrance(db, TypeConverter.Map(entranceToCreate), houseId,
						isCreate, r.get(WsMessages.c.UUID_ORG.lc()));
				importResult.getCommonResult().add(entranceCommonResult);
			}
	        
			for (EntranceToUpdate entranceToUpdate : house.getEntranceToUpdate()) {
				CommonResult entranceCommonResult = saveEntrance(db, TypeConverter.Map(entranceToUpdate), houseId,
						isCreate, r.get(WsMessages.c.UUID_ORG.lc()));
				importResult.getCommonResult().add(entranceCommonResult);
			}
	        
			for (NonResidentialPremiseToCreate nonResidentialPremiseToCreate : house.getNonResidentialPremiseToCreate()) {
				CommonResult nonResidentialPremiseCommonResult = saveNonResidentialPremise(db,
						TypeConverter.Map(nonResidentialPremiseToCreate), houseId, isCreate,
						r.get(WsMessages.c.UUID_ORG.lc()));
				importResult.getCommonResult().add(nonResidentialPremiseCommonResult);
			}
	        
			for (NonResidentialPremiseToUpdate nonResidentialPremiseToUpdate : house.getNonResidentialPremiseToUpdate()) {
				CommonResult nonResidentialPremiseCommonResult = saveNonResidentialPremise(db,
						TypeConverter.Map(nonResidentialPremiseToUpdate), houseId, isCreate,
						r.get(WsMessages.c.UUID_ORG.lc()));
				importResult.getCommonResult().add(nonResidentialPremiseCommonResult);
			}
			
			Map<Object, Map<String, Object>> entrances = db.getIdx(db.getModel()
					.select(Entrance.class, "uuid", "entrancenum")
					.where("uuid_house", houseId)
					.and("is_deleted", 0)
					.and("is_annuled", 0),
					"entrancenum"
					);
			
			for (ResidentialPremises residentialPremise : house.getResidentialPremises()) {
				Map<String, Object> residentialPremiseMap = residentialPremise.getResidentialPremisesToCreate() != null
						? TypeConverter.Map(residentialPremise.getResidentialPremisesToCreate())
						: TypeConverter.Map(residentialPremise.getResidentialPremisesToUpdate());

				CommonResult residentialPremiseCommonResult = saveResidentialPremise(db, residentialPremiseMap,
						entrances, houseId, isCreate, r.get(WsMessages.c.UUID_ORG.lc()));
				importResult.getCommonResult().add(residentialPremiseCommonResult);
				
				String premiseId = residentialPremiseCommonResult.getGUID();
				if (StringUtils.isNotBlank(premiseId)) {
					for (ResidentialPremises.LivingRoomToCreate livingRoomToCreate : residentialPremise.getLivingRoomToCreate()) {
						CommonResult livingRoomToCreateCommonResult = saveLivingRoom(db,
								TypeConverter.Map(livingRoomToCreate), premiseId, null, houseId, isCreate,
								r.get(WsMessages.c.UUID_ORG.lc()));
						importResult.getCommonResult().add(livingRoomToCreateCommonResult);
					}
			        
					for (ResidentialPremises.LivingRoomToUpdate livingRoomToUpdate : residentialPremise.getLivingRoomToUpdate()) {
						CommonResult livingRoomToCreateCommonResult = saveLivingRoom(db,
								TypeConverter.Map(livingRoomToUpdate), premiseId, null, houseId, isCreate,
								r.get(WsMessages.c.UUID_ORG.lc()));
						importResult.getCommonResult().add(livingRoomToCreateCommonResult);
					}
				}
			}
    	} catch (Fault fault) {
    		commonResult.getError().add(fault.toCommonResultError());
    	} catch (Exception e) {
    		Fault fault = new Fault(e);
    		commonResult.getError().add(fault.toCommonResultError());
    	}
    	return importResult;
    }
    
    private ImportResult saveLivingHouse(DB db, Map<String, Object> r, LivingHouse house) throws SQLException {
    	ImportResult importResult = new ImportResult();
    	boolean isCreate = false;
    	
    	CommonResult commonResult = new CommonResult();
    	try {
	    	UUID houseId;
    		if (house.getLivingHouseToCreate() != null) {
	    		isCreate = true;
	    		LivingHouseToCreate houseToCreate = house.getLivingHouseToCreate();
	    		HouseBasicRSOType basicCharacteristic = houseToCreate.getBasicCharacteristicts();
	        	
	    		houseId = saveHouseData(db, commonResult, houseToCreate.getTransportGUID(),
						basicCharacteristic.getFIASHouseGuid(), basicCharacteristic.getOKTMO(),
						basicCharacteristic.getOlsonTZ(), basicCharacteristic, false, isCreate);
	        } else {
	        	LivingHouseToUpdate houseToUpdate = house.getLivingHouseToUpdate();
	        	HouseBasicUpdateRSOType basicCharacteristic = houseToUpdate.getBasicCharacteristicts();
				
	        	houseId = saveHouseData(db, commonResult, houseToUpdate.getTransportGUID(),
						basicCharacteristic.getFIASHouseGuid(), basicCharacteristic.getOKTMO(),
						basicCharacteristic.getOlsonTZ(), basicCharacteristic, false, isCreate);
	        }
	        
    		for (Blocks block : house.getBlocks()) {
				Map<String, Object> blockMap = block.getBlockToCreate() != null
						? TypeConverter.Map(block.getBlockToCreate())
						: TypeConverter.Map(block.getBlockToUpdate());
				
				CommonResult blockCommonResult = saveBlock(db, blockMap, houseId, isCreate,
						r.get(WsMessages.c.UUID_ORG.lc()));
				importResult.getCommonResult().add(blockCommonResult);
				
				String blockId = blockCommonResult.getGUID();
				if (StringUtils.isNotBlank(blockId)) {
					for (Blocks.LivingRoomToCreate livingRoomToCreate : block.getLivingRoomToCreate()) {
						CommonResult livingRoomToCreateCommonResult = saveLivingRoom(db,
								TypeConverter.Map(livingRoomToCreate), null, blockId, houseId, isCreate,
								r.get(WsMessages.c.UUID_ORG.lc()));
						importResult.getCommonResult().add(livingRoomToCreateCommonResult);
					}
			        
					for (Blocks.LivingRoomToUpdate livingRoomToUpdate : block.getLivingRoomToUpdate()) {
						CommonResult livingRoomToCreateCommonResult = saveLivingRoom(db,
								TypeConverter.Map(livingRoomToUpdate), null, blockId, houseId, isCreate,
								r.get(WsMessages.c.UUID_ORG.lc()));
						importResult.getCommonResult().add(livingRoomToCreateCommonResult);
					}
				}
			}
    		
    		for (LivingHouse.LivingRoomToCreate livingRoomToCreate : house.getLivingRoomToCreate()) {
				CommonResult livingRoomToCreateCommonResult = saveLivingRoom(db,
						TypeConverter.Map(livingRoomToCreate), null, null, houseId, isCreate,
						r.get(WsMessages.c.UUID_ORG.lc()));
				importResult.getCommonResult().add(livingRoomToCreateCommonResult);
			}
	        
			for (LivingHouse.LivingRoomToUpdate livingRoomToUpdate : house.getLivingRoomToUpdate()) {
				CommonResult livingRoomToCreateCommonResult = saveLivingRoom(db,
						TypeConverter.Map(livingRoomToUpdate), null, null, houseId, isCreate,
						r.get(WsMessages.c.UUID_ORG.lc()));
				importResult.getCommonResult().add(livingRoomToCreateCommonResult);
			}
    		
	        house.getLivingRoomToCreate();
	        house.getLivingRoomToUpdate();
    	} catch (Fault fault) {
    		commonResult.getError().add(fault.toCommonResultError());
    	} catch (Exception e) {
    		Fault fault = new Fault(e);
    		commonResult.getError().add(fault.toCommonResultError());
    	}
    	return importResult;
    }
    
	private UUID saveHouseData(DB db, CommonResult commonResult, String transportGuid, String fiasHouseGuid,
			OKTMORefType oktmo, NsiRef olsonTZ, GKNEGRPKeyRSOType egrpKey, boolean isCondo, boolean isCreate)
			throws Fault, SQLException {
    	commonResult.setTransportGUID(transportGuid);
    	
    	Map<String, Object> vocBuilding = getBuilding(db, fiasHouseGuid);
    	
    	Map<String, Object> houseDb = db.getMap(db.getModel().select(House.class, "*")
    			.where(House.c.FIASHOUSEGUID, vocBuilding.get("houseguid")));
    	UUID houseId;
    	if (isCreate) {
    		if (houseDb != null && !houseDb.isEmpty())
        		throw new Fault("Дом уже размещен", Errors.INT004132);
        	
        	houseId = (UUID) db.insertId(House.class, HASH(
        			House.c.FIASHOUSEGUID, vocBuilding.get("houseguid"),
        			House.c.ADDRESS,       vocBuilding.get("label"),
        			House.c.KAD_N.lc(),    egrpKey.getCadastralNumber(),
        			House.c.IS_CONDO,      isCondo
        			));
    	} else {
	    	if (houseDb == null || houseDb.isEmpty())
	    		throw new Fault("Дом еще не размещен");
	    	
	    	houseId = (UUID) houseDb.get("uuid");
	    	
			db.update(House.class, HASH(
					EnTable.c.UUID, houseId, 
					House.c.KAD_N,  egrpKey.getCadastralNumber()
					));
    	}
    	
		commonResult.setGUID(houseId.toString());
    	commonResult.setUpdateDate(SOAPTools.xmlNow());
    	
    	return houseId;
    }
    
    private Map<String, Object> getBuilding(DB db, String fiasHouseGuid) throws SQLException, Fault {
    	
    	if (UNOM_PATTERN.matcher(fiasHouseGuid).matches()) {
    		Long unom = Long.valueOf(fiasHouseGuid.substring(24));
    		Map<String, Object> vocUnom = db.getMap(VocUnom.class, unom);
    		if (vocUnom == null || vocUnom.isEmpty())
    			throw new Fault("Идентификатор " + unom + " не найден в реестре", Errors.INT002000);
    		
    		VocUnomStatus.i vocUnomStatus = VocUnomStatus.i.forId(vocUnom.get(VocUnom.c.ID_STATUS.lc()));
    		
    		switch (vocUnomStatus) {
				case DUPLICATED_FIAS:
					throw new Fault("Для UNOM " + unom + " найдено более одного идентификатора ФИАС", Errors.INT002000);
				case EMPTY_FIAS:
				case INVALID_FIAS:
				case UNKNOWN_FIAS:
					throw new Fault("Для UNOM " + unom + "  не найден идентификатор ФИАС", Errors.INT002000);
				default:
					break;
			}
    		
    		fiasHouseGuid = vocUnom.get(VocUnom.c.FIASHOUSEGUID.lc()).toString();
    	} 
    	
    	Map<String, Object> vocBuilding = db.getMap(VocBuilding.class, fiasHouseGuid);
    	if (vocBuilding == null || vocBuilding.isEmpty())
    		throw new Fault("Идентификатор " + fiasHouseGuid + " не найден в реестре", Errors.INT002000);
    	return vocBuilding;
    }
    
	private CommonResult saveEntrance(DB db,  Map<String, Object> entranceData,
			UUID houseId, boolean isHouseCreate, Object uuidOrg) {
    	CommonResult commonResult = new CommonResult();
    	commonResult.setTransportGUID(entranceData.get("transportguid").toString());
    	
    	Object entranceGuid = entranceData.get("entranceguid");
    	try {
	    	if (entranceGuid != null && isHouseCreate)
	    		throw new Fault(Errors.INT004026);
	    	
	    	UUID entranceId;
	    	
	    	if (entranceGuid != null) {
	    		Select select = db.getModel().select(Entrance.class, "*")
	    				.where("uuid_house", houseId)
	    				.and("is_deleted", 0);
	    		select.andEither ("uuid", entranceGuid).or("entranceguid", entranceGuid);
	    		
	    		Map<String, Object> entrance = db.getMap(select);
	    		if (entrance.isEmpty())
	    			throw new Fault(Errors.INT002034);
	    		
	    		entranceId = (UUID)entrance.get("uuid");
	    		
	    		entranceData.remove("entranceguid");
	    		entranceData.put("uuid", entranceId);
	    		if (entranceData.containsKey("annulmentreason")) {
	    			entranceData.put("code_vc_nsi_330",((NsiRef)entranceData.get("annulmentreason")).getCode());
	    			entranceData.remove("annulmentreason");
	    		}
	    		
	    		db.update(Entrance.class, entranceData);
	    		
	    	} else {
	    		entranceData.putAll(HASH(
	    				"uuid_org",    uuidOrg,
	    				"uuid_house",  houseId
	    				));
	    		entranceId = (UUID) db.insertId(Entrance.class, entranceData);
	    	}
	    	
	    	commonResult.setGUID(entranceId.toString());
	    	commonResult.setUpdateDate(SOAPTools.xmlNow());
    	
    	} catch (Fault fault) {
    		commonResult.getError().add(fault.toCommonResultError());
    	} catch (Exception e) {
    		Fault fault = new Fault(e);
    		commonResult.getError().add(fault.toCommonResultError());
    	} 
    	return commonResult;
    }
	
	private CommonResult saveNonResidentialPremise(DB db, Map<String, Object> premiseData, UUID houseId, boolean isHouseCreate, Object uuidOrg) {
    	CommonResult commonResult = new CommonResult();
    	commonResult.setTransportGUID(premiseData.get("transportguid").toString());
    	Object premiseGuid = premiseData.get("premisesguid");
    	try {
	    	if (premiseGuid != null && isHouseCreate)
	    		throw new Fault(Errors.INT004025);
	    	
	    	UUID premiseId;
	    	
	    	if (premiseGuid != null) {
	    		Select select = db.getModel().select(NonResidentialPremise.class, "*")
	    				.where("uuid_house", houseId)
	    				.and("is_deleted", 0);
	    		select.andEither ("uuid", premiseGuid).or("premisesguid", premiseGuid);
	    		
	    		Map<String, Object> premise = db.getMap(select);
	    		if (premise.isEmpty())
	    			throw new Fault(Errors.INT002034);
	    		
	    		premiseId = (UUID)premise.get("uuid");
	    		
	    		premiseData.remove("premisesguid");
	    		premiseData.put("uuid", premiseId);
	    		if (premiseData.containsKey("annulmentreason")) {
	    			premiseData.put("code_vc_nsi_330",((NsiRef)premiseData.get("annulmentreason")).getCode());
	    			premiseData.remove("annulmentreason");
	    		}
	    		
	    		db.update(NonResidentialPremise.class, premiseData);
	    		
	    	} else {
	    		premiseData.putAll(HASH(
	    				"uuid_org",    uuidOrg,
	    				"uuid_house",  houseId
	    				));
	    		premiseId = (UUID) db.insertId(NonResidentialPremise.class, premiseData);
	    	}
	    	
	    	commonResult.setGUID(premiseId.toString());
	    	commonResult.setUpdateDate(SOAPTools.xmlNow());
    	
    	} catch (Fault fault) {
    		commonResult.getError().add(fault.toCommonResultError());
    	} catch (Exception e) {
    		Fault fault = new Fault(e);
    		commonResult.getError().add(fault.toCommonResultError());
    	} 
    	return commonResult;
    }
	
	private CommonResult saveResidentialPremise(DB db, Map<String, Object> premiseData,
			Map<Object, Map<String, Object>> entrances, UUID houseId, boolean isHouseCreate, Object uuidOrg) {
    	CommonResult commonResult = new CommonResult();
    	commonResult.setTransportGUID(premiseData.get("transportguid").toString());
    	Object premiseGuid = premiseData.get("premisesguid");
    	try {
	    	if (premiseGuid != null && isHouseCreate)
	    		throw new Fault(Errors.INT004027);
	    	
	    	if (premiseData.containsKey("premisesсharacteristic")) {
    			premiseData.put("code_vc_nsi_30",((NsiRef)premiseData.get("premisesсharacteristic")).getCode());
    		}
    		if ((Boolean) premiseData.containsKey("hasnoentrance")) {
    			premiseData.put("uuid_entrance", null);
    		} else {
    			String entranceNum = premiseData.get("entrancenum").toString();
    			Map<String, Object> entrance = entrances.get(entranceNum);
    			if (entrance == null)
    				throw new Fault(String.format(Errors.INT004100.getMessage(), entranceNum), Errors.INT004100);
    			
    			premiseData.put("uuid_entrance", entrance.get("uuid"));
    		}
	    	
	    	UUID premiseId;
	    	
	    	if (premiseGuid != null) {
	    		Select select = db.getModel().select(ResidentialPremise.class, "*")
	    				.where("uuid_house", houseId)
	    				.and("is_deleted", 0);
	    		select.andEither ("uuid", premiseGuid).or("premisesguid", premiseGuid);
	    		
	    		Map<String, Object> premise = db.getMap(select);
	    		if (premise.isEmpty())
	    			throw new Fault(Errors.INT002034);
	    		
	    		premiseId = (UUID)premise.get("uuid");
	    		
	    		premiseData.remove("premisesguid");
	    		premiseData.put("uuid", premiseId);
	    		if (premiseData.containsKey("annulmentreason")) {
	    			premiseData.put("code_vc_nsi_330",((NsiRef)premiseData.get("annulmentreason")).getCode());
	    			premiseData.remove("annulmentreason");
	    		}
	    		
	    		db.update(ResidentialPremise.class, premiseData);
	    		
	    	} else {
	    		premiseData.putAll(HASH(
	    				"uuid_org",    uuidOrg,
	    				"uuid_house",  houseId
	    				));
	    		premiseId = (UUID) db.insertId(ResidentialPremise.class, premiseData);
	    	}
	    	
	    	commonResult.setGUID(premiseId.toString());
	    	commonResult.setUpdateDate(SOAPTools.xmlNow());
    	
    	} catch (Fault fault) {
    		commonResult.getError().add(fault.toCommonResultError());
    	} catch (Exception e) {
    		Fault fault = new Fault(e);
    		commonResult.getError().add(fault.toCommonResultError());
    	} 
    	return commonResult;
    }
	
	private CommonResult saveBlock(DB db, Map<String, Object> blockData, UUID houseId, boolean isHouseCreate,
			Object uuidOrg) {
    	CommonResult commonResult = new CommonResult();
    	commonResult.setTransportGUID(blockData.get("transportguid").toString());
    	Object blockGuid = blockData.get("blockguid");
    	try {
	    	if (blockGuid != null && isHouseCreate)
	    		throw new Fault(Errors.INT004093);
	    	
	    	if (blockData.containsKey("premisesсharacteristic")) {
    			blockData.put(Block.c.CODE_VC_NSI_30.lc(),((NsiRef)blockData.get("premisesсharacteristic")).getCode());
    		}
	    	if (blockData.containsKey("category")) {
    			blockData.put(Block.c.IS_NRS.lc(),BlockCategoryType.NON_RESIDENTIAL.equals(blockData.get("category")));
    		}
	    	
	    	UUID blockId;
	    	
	    	if (blockGuid != null) {
	    		Select select = db.getModel().select(Block.class, "*")
	    				.where(Block.c.UUID_HOUSE, houseId)
	    				.and(Block.c.IS_DELETED, 0);
				select.andEither(Block.c.UUID.lc(), blockGuid).or(Block.c.BLOCKGUID.lc(), blockGuid);
	    		
	    		Map<String, Object> block = db.getMap(select);
	    		if (block.isEmpty())
	    			throw new Fault(Errors.INT002034);
	    		
	    		blockId = (UUID)block.get(Block.c.UUID.lc());
	    		
	    		blockData.remove(Block.c.BLOCKGUID.lc());
	    		blockData.put(Block.c.UUID.lc(), blockId);
	    		if (blockData.containsKey(Block.c.ANNULMENTREASON.lc())) {
	    			blockData.put(Block.c.CODE_VC_NSI_330.lc(),((NsiRef)blockData.get(Block.c.ANNULMENTREASON.lc())).getCode());
	    			blockData.remove(Block.c.ANNULMENTREASON.lc());
	    		}
	    		
	    		db.update(Block.class, blockData);
	    		
	    	} else {
	    		blockData.putAll(HASH(
	    				Block.c.UUID_ORG,    uuidOrg,
	    				Block.c.UUID_HOUSE,  houseId
	    				));
	    		blockId = (UUID) db.insertId(Block.class, blockData);
	    	}
	    	
	    	commonResult.setGUID(blockId.toString());
	    	commonResult.setUpdateDate(SOAPTools.xmlNow());
    	
    	} catch (Fault fault) {
    		commonResult.getError().add(fault.toCommonResultError());
    	} catch (Exception e) {
    		Fault fault = new Fault(e);
    		commonResult.getError().add(fault.toCommonResultError());
    	} 
    	return commonResult;
    }
	
	private CommonResult saveLivingRoom(DB db, Map<String, Object> roomData, String premiseId, String blockId,
			UUID houseId, boolean isHouseCreate, Object uuidOrg) {
    	CommonResult commonResult = new CommonResult();
    	commonResult.setTransportGUID(roomData.get("transportguid").toString());
    	Object livingRoomGuid = roomData.get("livingroomguid");
    	try {
	    	if (livingRoomGuid != null && isHouseCreate)
	    		throw new Fault(Errors.INT004028);
	    	
	    	UUID livingRoomId;
	    	
	    	if (livingRoomGuid != null) {
	    		Select select = db.getModel().select(LivingRoom.class, "*")
	    				.where(LivingRoom.c.UUID_HOUSE, houseId)
	    				.and(LivingRoom.c.IS_DELETED, 0);
	    		
	    		if (premiseId != null)
	    			select = select.and(LivingRoom.c.UUID_PREMISE, premiseId);
	    		if (blockId != null)
	    			select = select.and(LivingRoom.c.UUID_BLOCK, blockId);
	    		
	    		select.andEither(LivingRoom.c.UUID.lc(), livingRoomGuid).or(LivingRoom.c.LIVINGROOMGUID.lc(), livingRoomGuid);
	    		
	    		Map<String, Object> livingRoom = db.getMap(select);
	    		if (livingRoom.isEmpty())
	    			throw new Fault(Errors.INT002034);
	    		
	    		livingRoomId = (UUID)livingRoom.get(LivingRoom.c.UUID.lc());
	    		
	    		roomData.remove(LivingRoom.c.LIVINGROOMGUID.lc());
	    		roomData.put(LivingRoom.c.UUID.lc(), premiseId);
	    		if (roomData.containsKey(LivingRoom.c.ANNULMENTREASON.lc())) {
	    			roomData.put(LivingRoom.c.CODE_VC_NSI_330.lc(),((NsiRef)roomData.get(LivingRoom.c.ANNULMENTREASON.lc())).getCode());
	    			roomData.remove(LivingRoom.c.ANNULMENTREASON.lc());
	    		}
	    		
	    		db.update(LivingRoom.class, roomData);
	    		
	    	} else {
	    		roomData.putAll(HASH(
	    				LivingRoom.c.UUID_ORG,     uuidOrg,
	    				LivingRoom.c.UUID_HOUSE,   houseId,
	    				LivingRoom.c.UUID_PREMISE, premiseId,
	    				LivingRoom.c.UUID_BLOCK,   blockId
	    				));
	    		livingRoomId = (UUID) db.insertId(LivingRoom.class, roomData);
	    	}
	    	
	    	commonResult.setGUID(livingRoomId.toString());
	    	commonResult.setUpdateDate(SOAPTools.xmlNow());
    	
    	} catch (Fault fault) {
    		commonResult.getError().add(fault.toCommonResultError());
    	} catch (Exception e) {
    		Fault fault = new Fault(e);
    		commonResult.getError().add(fault.toCommonResultError());
    	} 
    	return commonResult;
    }
}