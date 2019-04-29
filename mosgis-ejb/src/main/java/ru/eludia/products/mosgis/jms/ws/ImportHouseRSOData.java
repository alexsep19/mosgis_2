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
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocUnom;
import ru.eludia.products.mosgis.db.model.voc.VocUnomStatus;
import ru.eludia.products.mosgis.jms.base.WsMDB;
import ru.eludia.products.mosgis.ws.soap.impl.base.Errors;
import ru.eludia.products.mosgis.ws.soap.impl.base.Fault;
import ru.eludia.products.mosgis.ws.soap.tools.SOAPTools;
import ru.gosuslugi.dom.schema.integration.base.BaseAsyncResponseType;
import ru.gosuslugi.dom.schema.integration.house_management.GetStateResult;
import ru.gosuslugi.dom.schema.integration.house_management.HouseBasicRSOType;
import ru.gosuslugi.dom.schema.integration.house_management.HouseBasicUpdateRSOType;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseRSORequest;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseRSORequest.ApartmentHouse;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseRSORequest.ApartmentHouse.ApartmentHouseToCreate;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseRSORequest.ApartmentHouse.ApartmentHouseToUpdate;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseRSORequest.LivingHouse;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseRSORequest.LivingHouse.LivingHouseToCreate;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseRSORequest.LivingHouse.LivingHouseToUpdate;
import ru.gosuslugi.dom.schema.integration.house_management.ImportResult;
import ru.gosuslugi.dom.schema.integration.house_management.ImportResult.CommonResult;

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
	    	if (house.getApartmentHouseToCreate() != null) {
	    		isCreate = true;
	    		ApartmentHouseToCreate houseToCreate = house.getApartmentHouseToCreate();
	    		commonResult.setTransportGUID(houseToCreate.getTransportGUID());
	    		
	    		HouseBasicRSOType basicCharacteristic = houseToCreate.getBasicCharacteristicts();
	        	
	        	Map<String, Object> vocBuilding = getBuilding(db, basicCharacteristic.getFIASHouseGuid());
	        	
	        	Map<String, Object> houseDb = db.getMap(db.getModel().select(House.class, "*")
	        			.where(House.c.FIASHOUSEGUID, vocBuilding.get("houseguid")));
	        	if (houseDb != null && !houseDb.isEmpty())
	        		throw new Fault("Дом уже размещен", Errors.INT004132);
	        	
	        	UUID houseId = (UUID) db.insertId(House.class, HASH(
	        			House.c.FIASHOUSEGUID, vocBuilding.get("houseguid"),
	        			House.c.ADDRESS,       vocBuilding.get("label"),
	        			House.c.KAD_N.lc(),    basicCharacteristic.getCadastralNumber(),
	        			House.c.IS_CONDO,      true
	        			));
	        	
	        	commonResult.setGUID(houseId.toString());
	        	commonResult.setUpdateDate(SOAPTools.xmlNow());
	        } else if (house.getApartmentHouseToUpdate() != null) {
	        	ApartmentHouseToUpdate houseToUpdate = house.getApartmentHouseToUpdate();
	        	commonResult.setTransportGUID(houseToUpdate.getTransportGUID());
	        	
	        	HouseBasicUpdateRSOType basicCharacteristic = houseToUpdate.getBasicCharacteristicts();
	        	
	        	Map<String, Object> vocBuilding = getBuilding(db, basicCharacteristic.getFIASHouseGuid());
	        	
	        	Map<String, Object> houseDb = db.getMap(db.getModel().select(House.class, "*")
	        			.where(House.c.FIASHOUSEGUID, vocBuilding.get("houseguid")));
	        	if (houseDb == null || houseDb.isEmpty())
	        		throw new Fault("Дом еще не размещен");
	        	
	        	houseDb.put(House.c.KAD_N.lc(), basicCharacteristic.getCadastralNumber());
	        	
				commonResult.setGUID(((UUID) houseDb.get("uuid")).toString());
	        	commonResult.setUpdateDate(SOAPTools.xmlNow());
	        }
	        
	        
	        house.getEntranceToCreate();
	        house.getEntranceToUpdate();
	        house.getNonResidentialPremiseToCreate();
	        house.getNonResidentialPremiseToUpdate();
	        house.getResidentialPremises();
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
	    	if (house.getLivingHouseToCreate() != null) {
	    		isCreate = true;
	    		LivingHouseToCreate houseToCreate = house.getLivingHouseToCreate();
	    		commonResult.setTransportGUID(houseToCreate.getTransportGUID());
	    		
	    		HouseBasicRSOType basicCharacteristic = houseToCreate.getBasicCharacteristicts();
	        	
	        	Map<String, Object> vocBuilding = getBuilding(db, basicCharacteristic.getFIASHouseGuid());
	        	
	        	Map<String, Object> houseDb = db.getMap(db.getModel().select(House.class, "*")
	        			.where(House.c.FIASHOUSEGUID, vocBuilding.get("houseguid")));
	        	if (houseDb != null && !houseDb.isEmpty())
	        		throw new Fault("Дом уже размещен", Errors.INT004132);
	        	
	        	UUID houseId = (UUID) db.insertId(House.class, HASH(
	        			House.c.FIASHOUSEGUID, vocBuilding.get("houseguid"),
	        			House.c.ADDRESS,       vocBuilding.get("label"),
	        			House.c.KAD_N.lc(),    basicCharacteristic.getCadastralNumber(),
	        			House.c.IS_CONDO,      true
	        			));
	        	
	        	commonResult.setGUID(houseId.toString());
	        	commonResult.setUpdateDate(SOAPTools.xmlNow());
	        } else if (house.getLivingHouseToUpdate() != null) {
	        	LivingHouseToUpdate houseToUpdate = house.getLivingHouseToUpdate();
	        	commonResult.setTransportGUID(houseToUpdate.getTransportGUID());
	        	
	        	HouseBasicUpdateRSOType basicCharacteristic = houseToUpdate.getBasicCharacteristicts();
	        	
	        	Map<String, Object> vocBuilding = getBuilding(db, basicCharacteristic.getFIASHouseGuid());
	        	
	        	Map<String, Object> houseDb = db.getMap(db.getModel().select(House.class, "*")
	        			.where(House.c.FIASHOUSEGUID, vocBuilding.get("houseguid")));
	        	if (houseDb == null || houseDb.isEmpty())
	        		throw new Fault("Дом еще не размещен");
	        	
	        	houseDb.put(House.c.KAD_N.lc(), basicCharacteristic.getCadastralNumber());
	        	
				commonResult.setGUID(((UUID) houseDb.get("uuid")).toString());
	        	commonResult.setUpdateDate(SOAPTools.xmlNow());
	        }
	        
	        
	        house.getBlocks();
	        house.getLivingHouseToCreate();
	        house.getLivingRoomToUpdate();
    	} catch (Fault fault) {
    		commonResult.getError().add(fault.toCommonResultError());
    	} catch (Exception e) {
    		Fault fault = new Fault(e);
    		commonResult.getError().add(fault.toCommonResultError());
    	}
    	return importResult;
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
    
    @Override
	protected Class<GetStateResult> getGetStateResultClass() {
		return GetStateResult.class;
	}

}