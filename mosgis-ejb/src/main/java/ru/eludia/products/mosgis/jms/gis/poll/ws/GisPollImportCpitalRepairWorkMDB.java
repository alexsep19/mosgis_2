package ru.eludia.products.mosgis.jms.gis.poll.ws;

import static ru.eludia.base.DB.HASH;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOverhaulWorkType;
import ru.eludia.products.mosgis.db.model.ws.WsMessages;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollWsProxyMDB;
import ru.eludia.products.mosgis.jms.ws.ImportCapitalRepairWork;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisNsiClient;
import ru.eludia.products.mosgis.ws.soap.tools.SOAPTools;
import ru.gosuslugi.dom.schema.integration.base.CommonResultType;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.nsi.GetStateResult;
import ru.gosuslugi.dom.schema.integration.nsi.ImportCapitalRepairWorkRequest;
import ru.gosuslugi.dom.schema.integration.nsi.ImportCapitalRepairWorkType;
import ru.gosuslugi.dom.schema.integration.nsi_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outImportCapitalRepairWork")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollImportCpitalRepairWorkMDB extends GisPollWsProxyMDB<GetStateResult> {
    
	@EJB
    private WsGisNsiClient wsGisNsiClient;
    
	@Override
	protected JAXBContext getJAXBContext() throws JAXBException {
		return ImportCapitalRepairWork.JAXB_CONTEXT;
	}
    
	@Override
	protected GetStateResult getState (UUID orgPPAGuid, Map<String, Object> r) throws GisPollRetryException, GisPollException {

        GetStateResult rp;
        
        try {
            rp = wsGisNsiClient.getState (orgPPAGuid, (UUID) r.get ("uuid_ack"));
        }
        catch (Fault ex) {
            throw new GisPollException (ex.getFaultInfo ());
        }
        catch (Throwable ex) {            
            throw new GisPollException (ex);
        }
        
        checkIfResponseReady (rp);
        
        return rp;
        
    }
    
	@Override
	protected void saveResponse(DB db, GetStateResult response, Map<String, Object> r) throws SQLException, GisPollException {
		ErrorMessageType errorMessage = response.getErrorMessage();
		if (errorMessage != null)
			throw new GisPollException(errorMessage);

		List<CommonResultType> importResult = response.getImportResult();
		if (importResult.isEmpty())
			throw new GisPollException("0", "Сервис ГИС ЖКХ вернул пустой результат");

		Map<String, CommonResultType> resultByTransportGuid = importResult.stream()
				.collect(Collectors.toMap(CommonResultType::getTransportGUID, item -> item));
		
		ImportCapitalRepairWorkRequest importCapitalRepairWorkRequest;
		try {
			Unmarshaller unmarshaller = getJAXBContext().createUnmarshaller();
			
			importCapitalRepairWorkRequest =
					  (ImportCapitalRepairWorkRequest) unmarshaller.unmarshal(SOAPTools.getSoapBodyNode(r.get("rq").toString()));
		} catch (Exception e) {
			throw new GisPollException("0", "Не удалось преобразовать запрос из XML");
		}

		for (ImportCapitalRepairWorkType capitalRepairWork : importCapitalRepairWorkRequest.getImportCapitalRepairWork()) {
			
			CommonResultType result = resultByTransportGuid.get(capitalRepairWork.getTransportGUID());
			
			Map<String, Object> workType = HASH(
					VocOverhaulWorkType.c.GUID,              result.getGUID(),
					VocOverhaulWorkType.c.CODE,              result.getUniqueNumber(),
					VocOverhaulWorkType.c.SERVICENAME,       capitalRepairWork.getServiceName(),
					VocOverhaulWorkType.c.CODE_VC_NSI_218,   capitalRepairWork.getWorkGroupRef().getCode(),
					VocOverhaulWorkType.c.ISACTUAL,          true,
					VocOverhaulWorkType.c.ID_OWT_STATUS,     VocGisStatus.i.APPROVED.getId(),
					VocOverhaulWorkType.c.ID_OWT_STATUS_GIS, VocGisStatus.i.APPROVED.getId(),
					VocOverhaulWorkType.c.ID_STATUS,         VocAsyncEntityState.i.OK.getId(),
					VocOverhaulWorkType.c.UUID_ORG,          r.get("ws." + WsMessages.c.UUID_ORG.lc())
					);
			
			db.upsert(VocOverhaulWorkType.class, workType, VocOverhaulWorkType.c.CODE.lc());
		}
		//TODO Реализовать
		importCapitalRepairWorkRequest.getDeleteCapitalRepairWork();
		importCapitalRepairWorkRequest.getRecoverCapitalRepairWork();
	}

}
