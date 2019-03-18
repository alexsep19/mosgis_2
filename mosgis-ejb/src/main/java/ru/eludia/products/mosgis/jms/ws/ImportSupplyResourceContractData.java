package ru.eludia.products.mosgis.jms.ws;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract.c;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractSubject;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.ws.WsMessages;
import ru.eludia.products.mosgis.jms.base.WsMDB;
import ru.eludia.products.mosgis.jms.ws.sr_ctr.CustomerSetter;
import ru.eludia.products.mosgis.ws.soap.impl.base.Errors;
import ru.gosuslugi.dom.schema.integration.base.BaseAsyncResponseType;
import ru.eludia.products.mosgis.ws.soap.impl.base.Fault;
import ru.eludia.products.mosgis.ws.soap.tools.SOAPTools;
import ru.gosuslugi.dom.schema.integration.base.CommonResultType;
import ru.gosuslugi.dom.schema.integration.house_management.ImportSupplyResourceContractRequest;
import ru.gosuslugi.dom.schema.integration.house_management.GetStateResult;
import ru.gosuslugi.dom.schema.integration.house_management.ImportResult;
import ru.gosuslugi.dom.schema.integration.house_management.SupplyResourceContractType;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inSoapImportSupplyResourceContractData")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ImportSupplyResourceContractData extends WsMDB {
    
    private final static JAXBContext jc;
    
    static {
        try {
            jc = JAXBContext.newInstance (
                GetStateResult.class,
                ImportSupplyResourceContractRequest.class
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
        if (!(request instanceof ImportSupplyResourceContractRequest)) throw new Fault (Errors.FMT001300);
        final GetStateResult result = new GetStateResult ();
        fill (result, r, (ImportSupplyResourceContractRequest) request);
        return result;
    }

    private void fill (GetStateResult result, Map<String, Object> r, ImportSupplyResourceContractRequest importSupplyResourceContractRequest) {
        
        final ImportResult importResult = new ImportResult ();
        
        List<ImportResult.CommonResult> commonResult = importResult.getCommonResult ();
        
        try {
            for (ImportSupplyResourceContractRequest.Contract i: importSupplyResourceContractRequest.getContract ()) commonResult.add (toCommonResult (r, i));
            result.getImportResult ().add (importResult);
        }
        catch (Exception ex) {
            logger.log (Level.WARNING, "Problem with ImportSupplyResourceContractRequest", ex);
            final CommonResultType.Error error = new CommonResultType.Error ();
            error.setErrorCode ("EXP001000");
            error.setDescription (ex.getMessage ());
            error.setStackTrace (Arrays.toString (ex.getStackTrace ()));
            result.setErrorMessage (error);
        }
        
    }

    private ImportResult.CommonResult toCommonResult (Map<String, Object> r, ImportSupplyResourceContractRequest.Contract i) throws Exception {
        
        final ImportResult.CommonResult result = new ImportResult.CommonResult ();

        result.setTransportGUID (i.getTransportGUID ());
        
        try {
            final ImportResult.CommonResult.ImportSupplyResourceContract importSupplyResourceContract = toImportSupplyResourceContract (r, i);
            result.setImportSupplyResourceContract (importSupplyResourceContract);
            result.setGUID (importSupplyResourceContract.getContractGUID ());
            result.setUpdateDate (SOAPTools.xmlNow ());
        }
        catch (Exception ex) {
            logger.log (Level.WARNING, i.getTransportGUID (), ex);
            final CommonResultType.Error error = new CommonResultType.Error ();
            error.setErrorCode (Errors.FMT001300.name ());
            error.setDescription (i.getTransportGUID () + ": " + ex.getMessage ());
            result.getError ().add (error);
        }
        
        return result;
        
    }

    private ImportResult.CommonResult.ImportSupplyResourceContract toImportSupplyResourceContract (Map<String, Object> r, ImportSupplyResourceContractRequest.Contract i) throws Exception {
        String contractGUID = i.getContractGUID ();
        if (i.getSupplyResourceContract () != null) return toImportSupplyResourceContract (r, contractGUID, i.getSupplyResourceContract ());
        throw new UnsupportedOperationException ("Не найден supplyResourceContract, такие запросы пока не поддерживаются");
    }
    
    private Map<String, Object> toMap (SupplyResourceContractType src) throws UnsupportedOperationException {
        
        final Map<String, Object> r;
        
        SupplyResourceContractType.IsNotContract isNotContract = src.getIsNotContract ();        
        if (isNotContract != null) {
            r = DB.to.Map (toJsonObject (isNotContract));
            r.put (c.IS_CONTRACT.lc (), 0);
        }
        else {
            throw new UnsupportedOperationException ("Не найден isNotContract, такие запросы пока не поддерживаются");
        }
        
        addContractSubjects (r, src.getContractSubject ());
        
        return r;
        
    }
    
    private void addContractSubjects (Map<String, Object> r, List<SupplyResourceContractType.ContractSubject> contractSubject) {
        r.put (SupplyResourceContractSubject.TABLE_NAME, contractSubject.stream ().map (t -> toMap (t)).collect (Collectors.toList ()));
    }    
    
    private Map<String, Object> toMap (SupplyResourceContractType.ContractSubject cs) {
        Map<String, Object> result = DB.to.Map (toJsonObject (cs));
        result.put (SupplyResourceContractSubject.c.CODE_VC_NSI_3.lc (), cs.getServiceType ().getCode ());
        result.put (SupplyResourceContractSubject.c.CODE_VC_NSI_239.lc (), cs.getMunicipalResource ().getCode ());
        return result;
    }

    private ImportResult.CommonResult.ImportSupplyResourceContract toImportSupplyResourceContract (Map<String, Object> wsr, String contractGUID, SupplyResourceContractType src) throws Exception {
        
        if (contractGUID != null) throw new UnsupportedOperationException ("Операции обновления пока не поддерживаются");

        MosGisModel m = ModelHolder.getModel ();
        
        Map<String, Object> r = toMap (src);        

        r.put (SupplyResourceContract.c.UUID_ORG.lc (), wsr.get (WsMessages.c.UUID_ORG.lc ()));

        CustomerSetter.setCustomer (r, src);
        
logger.info ("r=" + r);

        ImportResult.CommonResult.ImportSupplyResourceContract result = new ImportResult.CommonResult.ImportSupplyResourceContract ();
        
        try (DB db = m.getDb ()) {
            
            UUID uuid = (UUID) db.insertId (SupplyResourceContract.class, r);
            
            result.setContractRootGUID (uuid.toString ());
            
            String idLog = m.createIdLogWs (db, m.get (SupplyResourceContract.class), (UUID) wsr.get ("uuid"), uuid, VocAction.i.CREATE);
            
            result.setContractGUID (idLog);            
            
            List<Map<String, Object>> subjects = (List<Map<String, Object>>) r.get (SupplyResourceContractSubject.TABLE_NAME);
            
            for (Map<String, Object> i: subjects) {

                i.put (SupplyResourceContractSubject.c.UUID_SR_CTR.lc (), uuid);

                i.put (EnTable.c.UUID.lc (), db.insertId (SupplyResourceContractSubject.class, i));

            }

            return result;

        }
        
    }

}