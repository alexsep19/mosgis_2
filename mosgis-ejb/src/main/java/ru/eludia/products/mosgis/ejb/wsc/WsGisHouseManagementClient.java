package ru.eludia.products.mosgis.ejb.wsc;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.jws.HandlerChain;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceRef;
import ru.eludia.base.DB;
import ru.eludia.base.db.util.TypeConverter;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.Block;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.ContractFile;
import ru.eludia.products.mosgis.db.model.tables.ContractObject;
import ru.eludia.products.mosgis.db.model.tables.Entrance;
import ru.eludia.products.mosgis.db.model.tables.Lift;
import ru.eludia.products.mosgis.db.model.tables.LivingRoom;
import ru.eludia.products.mosgis.db.model.tables.NonResidentialPremise;
import ru.eludia.products.mosgis.db.model.tables.ResidentialPremise;
import ru.eludia.products.mosgis.db.model.voc.VocContractDocType;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.util.StringUtils;
import ru.eludia.products.mosgis.ws.base.LoggingOutMessageHandler;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.base.AttachmentType;
import ru.gosuslugi.dom.schema.integration.base.GetStateRequest;
import ru.gosuslugi.dom.schema.integration.house_management.ApartmentHouseESPType;
import ru.gosuslugi.dom.schema.integration.house_management.ApartmentHouseOMSType;
import ru.gosuslugi.dom.schema.integration.house_management.ApartmentHouseUOType;
import ru.gosuslugi.dom.schema.integration.house_management.ExportHouseRequest;
import ru.gosuslugi.dom.schema.integration.house_management.ExportStatusCAChRequest;
import ru.gosuslugi.dom.schema.integration.house_management.GetStateResult;
import ru.gosuslugi.dom.schema.integration.house_management.HouseBasicRSOType;
import ru.gosuslugi.dom.schema.integration.house_management.HouseBasicUOType;
import ru.gosuslugi.dom.schema.integration.house_management.HouseBasicUpdateESPType;
import ru.gosuslugi.dom.schema.integration.house_management.HouseBasicUpdateOMSType;
import ru.gosuslugi.dom.schema.integration.house_management.HouseBasicUpdateRSOType;
import ru.gosuslugi.dom.schema.integration.house_management.HouseBasicUpdateUOType;
import ru.gosuslugi.dom.schema.integration.house_management.ImportContractRequest;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseESPRequest;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseOMSRequest;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseRSORequest;
import ru.gosuslugi.dom.schema.integration.house_management.ImportHouseUORequest;
import ru.gosuslugi.dom.schema.integration.house_management.LivingHouseOMSType;
import ru.gosuslugi.dom.schema.integration.house_management.ObjectFactory;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.HouseManagementPortsTypeAsync;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.HouseManagementServiceAsync;

@Stateless
public class WsGisHouseManagementClient {

    private static final ObjectFactory of = new ObjectFactory ();
    private static final Logger logger = Logger.getLogger (WsGisHouseManagementClient.class.getName ());

    @HandlerChain (file="handler-chain-out.xml")
    @WebServiceRef(wsdlLocation = "META-INF/wsdl/house-management/hcs-house-management-service-async.wsdl")
    private HouseManagementServiceAsync service;

    private HouseManagementPortsTypeAsync getPort () {
        HouseManagementPortsTypeAsync port = service.getHouseManagementPortAsync();
        VocSetting.setPort (port, "WS_GIS_HOUSE_MANAGEMENT");
        return port;
    }

    private HouseManagementPortsTypeAsync getPort (UUID orgPPAGuid, UUID messageGUID) {
        HouseManagementPortsTypeAsync port = service.getHouseManagementPortAsync();
        VocSetting.setPort (port, "WS_GIS_HOUSE_MANAGEMENT");
        final Map<String, Object> requestContext = ((BindingProvider)port).getRequestContext ();
        requestContext.put (LoggingOutMessageHandler.FIELD_MESSAGE_GUID, messageGUID);
        requestContext.put (LoggingOutMessageHandler.FIELD_ORG_PPA_GUID, orgPPAGuid);
        return port;
    }

    public GetStateResult getState (UUID uuid) throws Fault {
        GetStateRequest getStateRequest = new GetStateRequest ();
        getStateRequest.setMessageGUID (uuid.toString ());
        return getPort ().getState(getStateRequest);
    }    

    public AckRequest.Ack exportHouseData (String fiasHouseGuid) throws Fault {
        
        if (fiasHouseGuid == null) throw new IllegalArgumentException ("Null FIASHouseGUID passed");
        
        ExportHouseRequest request = of.createExportHouseRequest();
        request.setFIASHouseGuid(fiasHouseGuid);
        
        return getPort ().exportHouseData(request).getAck();
        
    }
    
    public AckRequest.Ack annulContractData (UUID orgPPAGuid, UUID messageGUID,  Map<String, Object> r) throws Fault {

        final ImportContractRequest.Contract.AnnulmentContract ac = (ImportContractRequest.Contract.AnnulmentContract) DB.to.javaBean (ImportContractRequest.Contract.AnnulmentContract.class, r);
        ac.setLicenseRequest (true);
        ac.setContractVersionGUID (r.get ("ctr.contractversionguid").toString ());
        
        ImportContractRequest importContractRequest = of.createImportContractRequest ();
        final ImportContractRequest.Contract c = of.createImportContractRequestContract ();
        c.setAnnulmentContract (ac);
        c.setTransportGUID (UUID.randomUUID ().toString ());
        
        importContractRequest.getContract ().add (c);
        
        return getPort (orgPPAGuid, messageGUID).importContractData (importContractRequest).getAck ();        
        
    }

    public AckRequest.Ack terminateContractData (UUID orgPPAGuid, UUID messageGUID,  Map<String, Object> r) throws Fault {
        
        final ImportContractRequest.Contract.TerminateContract tc = (ImportContractRequest.Contract.TerminateContract) DB.to.javaBean (ImportContractRequest.Contract.TerminateContract.class, r);
        
        tc.setLicenseRequest (true);
        tc.setContractVersionGUID (r.get ("ctr.contractversionguid").toString ());
        
        List<AttachmentType> terminateAttachment = tc.getTerminateAttachment ();
        
        String idTerminationType = String.valueOf (VocContractDocType.i.TERMINATION_ATTACHMENT.getId ());        
        for (Map<String, Object> file: (Collection<Map<String, Object>>) r.get ("files")) {
            if (!idTerminationType.equals (file.get ("id_type").toString ())) continue;
            terminateAttachment.add (ContractFile.toAttachmentType (file));
        }
        
        tc.setReasonRef (NsiTable.toDom (r.get ("code_vc_nsi_54").toString (), (UUID) r.get ("vc_nsi_54.guid")));
        
        ImportContractRequest importContractRequest = of.createImportContractRequest ();
        final ImportContractRequest.Contract c = of.createImportContractRequestContract ();
        c.setTerminateContract (tc);
        c.setTransportGUID (UUID.randomUUID ().toString ());
        
        importContractRequest.getContract ().add (c);

        return getPort (orgPPAGuid, messageGUID).importContractData (importContractRequest).getAck ();
        
    }

    public AckRequest.Ack editContractData (UUID orgPPAGuid, UUID messageGUID,  Map<String, Object> r) throws Fault {
        
        final ImportContractRequest.Contract.EditContract ec = (ImportContractRequest.Contract.EditContract) DB.to.javaBean (ImportContractRequest.Contract.EditContract.class, r);
        
        ec.setLicenseRequest (true);
        Contract.fillContract (ec, r);
        ec.setContractVersionGUID (r.get ("ctr.contractversionguid").toString ());

        for (Map<String, Object> file: (Collection<Map<String, Object>>) r.get ("files"))   ContractFile.add   (ec, file);
        for (Map<String, Object> o:    (Collection<Map<String, Object>>) r.get ("objects")) ContractObject.add (ec, o);
        
        ImportContractRequest importContractRequest = of.createImportContractRequest ();
        final ImportContractRequest.Contract c = of.createImportContractRequestContract ();
        c.setEditContract (ec);
        c.setTransportGUID (UUID.randomUUID ().toString ());
        
        importContractRequest.getContract ().add (c);

        return getPort (orgPPAGuid, messageGUID).importContractData (importContractRequest).getAck ();
        
    }
    
    public AckRequest.Ack placeContractData (UUID orgPPAGuid, UUID messageGUID,  Map<String, Object> r) throws Fault {
        
        final ImportContractRequest.Contract.PlacingContract pc = (ImportContractRequest.Contract.PlacingContract) DB.to.javaBean (ImportContractRequest.Contract.PlacingContract.class, r);
        pc.setLicenseRequest (true);
        Contract.fillContract (pc, r);
        
        for (Map<String, Object> file: (Collection<Map<String, Object>>) r.get ("files"))   ContractFile.add   (pc, file);
        for (Map<String, Object> o:    (Collection<Map<String, Object>>) r.get ("objects")) ContractObject.add (pc, o);

        ImportContractRequest importContractRequest = of.createImportContractRequest ();
        
        final ImportContractRequest.Contract c = of.createImportContractRequestContract ();
        c.setPlacingContract (pc);        
        c.setTransportGUID (UUID.randomUUID ().toString ());
        
        importContractRequest.getContract ().add (c);

        return getPort (orgPPAGuid, messageGUID).importContractData (importContractRequest).getAck ();
        
    }
    
    public AckRequest.Ack approveContractData (UUID orgPPAGuid, UUID messageGUID, UUID contractVersionGUID) throws Fault {
        
        final ImportContractRequest.Contract.ApprovalContract ac = of.createImportContractRequestContractApprovalContract ();
        ac.setApproval (true);
        ac.setContractVersionGUID (contractVersionGUID.toString ());
        
        ImportContractRequest importContractRequest = of.createImportContractRequest ();
        
        final ImportContractRequest.Contract c = of.createImportContractRequestContract ();
        c.setApprovalContract (ac);
        c.setTransportGUID (UUID.randomUUID ().toString ());
        
        importContractRequest.getContract ().add (c);

        return getPort (orgPPAGuid, messageGUID).importContractData (importContractRequest).getAck ();
        
    }
    
    public AckRequest.Ack exportContractStatus (UUID orgPPAGuid, UUID messageGUID, List<UUID> ids) throws Fault {

        final ExportStatusCAChRequest r = of.createExportStatusCAChRequest ();

        List<ExportStatusCAChRequest.Criteria> criteria = r.getCriteria ();

        for (UUID uuid: ids) {
            final ExportStatusCAChRequest.Criteria c = of.createExportStatusCAChRequestCriteria ();            
            c.setContractGUID (uuid.toString ());            
            criteria.add (c);            
        }

        return getPort (orgPPAGuid, messageGUID).exportStatusCAChData (r).getAck ();

    }

    public GetStateResult getState (UUID orgPPAGuid, UUID uuid) throws Fault {
        GetStateRequest getStateRequest = new GetStateRequest ();
        getStateRequest.setMessageGUID (uuid.toString ());
        return getPort (orgPPAGuid, UUID.randomUUID ()).getState (getStateRequest);
    }
    
    public AckRequest.Ack importHouseUOData (UUID orgPPAGuid, Map<String, Object> r) throws Fault {
        
        ImportHouseUORequest importHouseUORequest = of.createImportHouseUORequest();
        
        if (TypeConverter.bool(r.get("is_condo"))) {
            //МКД
            ImportHouseUORequest.ApartmentHouse house = new ImportHouseUORequest.ApartmentHouse();
            importHouseUORequest.setApartmentHouse(house);
            if (StringUtils.isBlank((String) r.get("gis_unique_number"))) {
                //Создание
                ImportHouseUORequest.ApartmentHouse.ApartmentHouseToCreate apartmentHouse
                        = TypeConverter.javaBean(ImportHouseUORequest.ApartmentHouse.ApartmentHouseToCreate.class, r);
                apartmentHouse.setBasicCharacteristicts(TypeConverter.javaBean(ApartmentHouseUOType.BasicCharacteristicts.class, r));
                house.setApartmentHouseToCreate(apartmentHouse);
            } else {
                //Обновление
                ImportHouseUORequest.ApartmentHouse.ApartmentHouseToUpdate apartmentHouse
                        = TypeConverter.javaBean(ImportHouseUORequest.ApartmentHouse.ApartmentHouseToUpdate.class, r);
                apartmentHouse.setBasicCharacteristicts(TypeConverter.javaBean(HouseBasicUpdateUOType.class, r));
                house.setApartmentHouseToUpdate(apartmentHouse);
            }
            ((Collection<Map<String, Object>>) r.get("entrances")).forEach((item) -> Entrance.add(house, item));
            ((Collection<Map<String, Object>>) r.get("lifts")).forEach((item) -> Lift.add(house, item));
            ((Collection<Map<String, Object>>) r.get("residentialpremises")).forEach((item) -> ResidentialPremise.add(house, item));
            ((Collection<Map<String, Object>>) r.get("nonresidentialpremises")).forEach((item) -> NonResidentialPremise.add(house, item));
        } else {
            //ЖД
            ImportHouseUORequest.LivingHouse house = new ImportHouseUORequest.LivingHouse();
            importHouseUORequest.setLivingHouse(house);
            if (r.get("gis_unique_number") == null) {
                //Создание
                ImportHouseUORequest.LivingHouse.LivingHouseToCreate livingHouse
                        = TypeConverter.javaBean(ImportHouseUORequest.LivingHouse.LivingHouseToCreate.class, r);
                livingHouse.setBasicCharacteristicts(TypeConverter.javaBean(HouseBasicUOType.class, r));
                house.setLivingHouseToCreate(livingHouse);
            } else {
                //Обновление
                ImportHouseUORequest.LivingHouse.LivingHouseToUpdate livingHouse
                        = TypeConverter.javaBean(ImportHouseUORequest.LivingHouse.LivingHouseToUpdate.class, r);
                livingHouse.setBasicCharacteristicts(TypeConverter.javaBean(HouseBasicUpdateUOType.class, r));
                house.setLivingHouseToUpdate(livingHouse);
            }
            if (TypeConverter.bool(r.get("hasblocks")))
                ((Collection<Map<String, Object>>) r.get("blocks")).forEach((item) -> Block.add(house, item));
            ((Collection<Map<String, Object>>) r.get("livingrooms")).forEach((item) -> LivingRoom.add(house, item));
        }
        
        return getPort (orgPPAGuid, UUID.randomUUID()).importHouseUOData(importHouseUORequest).getAck ();       
    }
    
    public AckRequest.Ack importHouseOMSData (UUID orgPPAGuid, Map<String, Object> r) throws Fault {
        
        ImportHouseOMSRequest importHouseOMSRequest = of.createImportHouseOMSRequest();
        
        if (TypeConverter.bool(r.get("is_condo"))) {
            //МКД
            ImportHouseOMSRequest.ApartmentHouse house = new ImportHouseOMSRequest.ApartmentHouse();
            importHouseOMSRequest.setApartmentHouse(house);
            if (StringUtils.isBlank((String) r.get("gis_unique_number"))) {
                //Создание
                ImportHouseOMSRequest.ApartmentHouse.ApartmentHouseToCreate apartmentHouse
                        = TypeConverter.javaBean(ImportHouseOMSRequest.ApartmentHouse.ApartmentHouseToCreate.class, r);
                apartmentHouse.setBasicCharacteristicts(TypeConverter.javaBean(ApartmentHouseOMSType.BasicCharacteristicts.class, r));
                house.setApartmentHouseToCreate(apartmentHouse);
            } else {
                //Обновление
                ImportHouseOMSRequest.ApartmentHouse.ApartmentHouseToUpdate apartmentHouse
                        = TypeConverter.javaBean(ImportHouseOMSRequest.ApartmentHouse.ApartmentHouseToUpdate.class, r);
                apartmentHouse.setBasicCharacteristicts(TypeConverter.javaBean(HouseBasicUpdateOMSType.class, r));
                house.setApartmentHouseToUpdate(apartmentHouse);
            }
            ((Collection<Map<String, Object>>) r.get("entrances")).forEach((item) -> Entrance.add(house, item));
            ((Collection<Map<String, Object>>) r.get("lifts")).forEach((item) -> Lift.add(house, item));
            ((Collection<Map<String, Object>>) r.get("residentialpremises")).forEach((item) -> ResidentialPremise.add(house, item));
            ((Collection<Map<String, Object>>) r.get("nonresidentialpremises")).forEach((item) -> NonResidentialPremise.add(house, item));
        } else {
            //ЖД
            ImportHouseOMSRequest.LivingHouse house = new ImportHouseOMSRequest.LivingHouse();
            importHouseOMSRequest.setLivingHouse(house);
            if (r.get("gis_unique_number") == null) {
                //Создание
                ImportHouseOMSRequest.LivingHouse.LivingHouseToCreate livingHouse
                        = TypeConverter.javaBean(ImportHouseOMSRequest.LivingHouse.LivingHouseToCreate.class, r);
                livingHouse.setBasicCharacteristicts(TypeConverter.javaBean(LivingHouseOMSType.BasicCharacteristicts.class, r));
                house.setLivingHouseToCreate(livingHouse);
            } else {
                //Обновление
                ImportHouseOMSRequest.LivingHouse.LivingHouseToUpdate livingHouse
                        = TypeConverter.javaBean(ImportHouseOMSRequest.LivingHouse.LivingHouseToUpdate.class, r);
                livingHouse.setBasicCharacteristicts(TypeConverter.javaBean(HouseBasicUpdateOMSType.class, r));
                house.setLivingHouseToUpdate(livingHouse);
            }
            if (TypeConverter.bool(r.get("hasblocks")))
                ((Collection<Map<String, Object>>) r.get("blocks")).forEach((item) -> Block.add(house, item));
            ((Collection<Map<String, Object>>) r.get("livingrooms")).forEach((item) -> LivingRoom.add(house, item));
        }
        
        return getPort (orgPPAGuid, UUID.randomUUID()).importHouseOMSData(importHouseOMSRequest).getAck ();       
    }
    
    public AckRequest.Ack importHouseRSOData (UUID orgPPAGuid, Map<String, Object> r) throws Fault {
        
        ImportHouseRSORequest importHouseRSORequest = of.createImportHouseRSORequest();
        
        if (TypeConverter.bool(r.get("is_condo"))) {
            //МКД
            ImportHouseRSORequest.ApartmentHouse house = new ImportHouseRSORequest.ApartmentHouse();
            importHouseRSORequest.setApartmentHouse(house);
            if (StringUtils.isBlank((String) r.get("gis_unique_number"))) {
                //Создание
                ImportHouseRSORequest.ApartmentHouse.ApartmentHouseToCreate apartmentHouse
                        = TypeConverter.javaBean(ImportHouseRSORequest.ApartmentHouse.ApartmentHouseToCreate.class, r);
                apartmentHouse.setBasicCharacteristicts(TypeConverter.javaBean(HouseBasicRSOType.class, r));
                house.setApartmentHouseToCreate(apartmentHouse);
            } else {
                //Обновление
                ImportHouseRSORequest.ApartmentHouse.ApartmentHouseToUpdate apartmentHouse
                        = TypeConverter.javaBean(ImportHouseRSORequest.ApartmentHouse.ApartmentHouseToUpdate.class, r);
                apartmentHouse.setBasicCharacteristicts(TypeConverter.javaBean(HouseBasicUpdateRSOType.class, r));
                house.setApartmentHouseToUpdate(apartmentHouse);
            }
            ((Collection<Map<String, Object>>) r.get("entrances")).forEach((item) -> Entrance.add(house, item));
            ((Collection<Map<String, Object>>) r.get("residentialpremises")).forEach((item) -> ResidentialPremise.add(house, item));
            ((Collection<Map<String, Object>>) r.get("nonresidentialpremises")).forEach((item) -> NonResidentialPremise.add(house, item));
        } else {
            //ЖД
            ImportHouseRSORequest.LivingHouse house = new ImportHouseRSORequest.LivingHouse();
            importHouseRSORequest.setLivingHouse(house);
            if (r.get("gis_unique_number") == null) {
                //Создание
                ImportHouseRSORequest.LivingHouse.LivingHouseToCreate livingHouse
                        = TypeConverter.javaBean(ImportHouseRSORequest.LivingHouse.LivingHouseToCreate.class, r);
                livingHouse.setBasicCharacteristicts(TypeConverter.javaBean(HouseBasicRSOType.class, r));
                house.setLivingHouseToCreate(livingHouse);
            } else {
                //Обновление
                ImportHouseRSORequest.LivingHouse.LivingHouseToUpdate livingHouse
                        = TypeConverter.javaBean(ImportHouseRSORequest.LivingHouse.LivingHouseToUpdate.class, r);
                livingHouse.setBasicCharacteristicts(TypeConverter.javaBean(HouseBasicUpdateRSOType.class, r));
                house.setLivingHouseToUpdate(livingHouse);
            }
            if (TypeConverter.bool(r.get("hasblocks")))
                ((Collection<Map<String, Object>>) r.get("blocks")).forEach((item) -> Block.add(house, item));
            ((Collection<Map<String, Object>>) r.get("livingrooms")).forEach((item) -> LivingRoom.add(house, item));
        }
        
        return getPort (orgPPAGuid, UUID.randomUUID()).importHouseRSOData(importHouseRSORequest).getAck ();       
    }
    
    public AckRequest.Ack importHouseESPData (UUID orgPPAGuid, Map<String, Object> r) throws Exception {
        
        ImportHouseESPRequest importHouseESPRequest = of.createImportHouseESPRequest();
        
        if (TypeConverter.bool(r.get("is_condo"))) {
            //МКД
            ImportHouseESPRequest.ApartmentHouse house = new ImportHouseESPRequest.ApartmentHouse();
            importHouseESPRequest.setApartmentHouse(house);
            if (StringUtils.isBlank((String) r.get("gis_unique_number"))) {
                //Создание
                ImportHouseESPRequest.ApartmentHouse.ApartmentHouseToCreate apartmentHouse
                        = TypeConverter.javaBean(ImportHouseESPRequest.ApartmentHouse.ApartmentHouseToCreate.class, r);
                apartmentHouse.setBasicCharacteristicts(TypeConverter.javaBean(ApartmentHouseESPType.BasicCharacteristicts.class, r));
                house.setApartmentHouseToCreate(apartmentHouse);
            } else {
                //Обновление
                ImportHouseESPRequest.ApartmentHouse.ApartmentHouseToUpdate apartmentHouse
                        = TypeConverter.javaBean(ImportHouseESPRequest.ApartmentHouse.ApartmentHouseToUpdate.class, r);
                apartmentHouse.setBasicCharacteristicts(TypeConverter.javaBean(HouseBasicUpdateESPType.class, r));
                house.setApartmentHouseToUpdate(apartmentHouse);
            }
            ((Collection<Map<String, Object>>) r.get("entrances")).forEach((item) -> Entrance.add(house, item));
            ((Collection<Map<String, Object>>) r.get("lifts")).forEach((item) -> Lift.add(house, item));
            ((Collection<Map<String, Object>>) r.get("residentialpremises")).forEach((item) -> ResidentialPremise.add(house, item));
            ((Collection<Map<String, Object>>) r.get("nonresidentialpremises")).forEach((item) -> NonResidentialPremise.add(house, item));
        } else {
            throw new Exception("Данным методом возможно отправить только МКД");
        }
        
        return getPort (orgPPAGuid, UUID.randomUUID()).importHouseESPData(importHouseESPRequest).getAck ();       
    }

}