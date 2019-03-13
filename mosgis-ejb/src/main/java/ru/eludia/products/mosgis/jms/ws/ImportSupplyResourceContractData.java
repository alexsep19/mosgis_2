package ru.eludia.products.mosgis.jms.ws;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.json.JsonObject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract.c;
import ru.eludia.products.mosgis.db.model.voc.VocGisSupplyResourceContractCustomerType;
import ru.eludia.products.mosgis.db.model.ws.WsMessages;
import ru.eludia.products.mosgis.jms.base.WsMDB;
import ru.eludia.products.mosgis.ws.soap.impl.base.Errors;
import ru.gosuslugi.dom.schema.integration.base.BaseAsyncResponseType;
import ru.eludia.products.mosgis.ws.soap.impl.base.Fault;
import ru.gosuslugi.dom.schema.integration.base.CommonResultType;
import ru.gosuslugi.dom.schema.integration.house_management.DRSOIndType;
import ru.gosuslugi.dom.schema.integration.house_management.DRSORegOrgType;
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
        result.setUpdateDate (DB.to.XMLGregorianCalendar (new Timestamp (System.currentTimeMillis ())));
        result.setGUID (getUUID (r, i));
        return result;
    }

    private String getUUID (Map<String, Object> r, ImportSupplyResourceContractRequest.Contract i) throws Exception {
        String contractGUID = i.getContractGUID ();
        if (i.getSupplyResourceContract () != null) return getUUID (r, contractGUID, i.getSupplyResourceContract ());
        throw new UnsupportedOperationException ("Not supported yet."); 
    }

    private String getUUID (Map<String, Object> wsr, String contractGUID, SupplyResourceContractType src) throws Exception {
        
        if (contractGUID != null) throw new UnsupportedOperationException ("Updating is not supported yet.");

        MosGisModel m = ModelHolder.getModel ();
        
        SupplyResourceContractType.IsNotContract isNotContract = src.getIsNotContract ();
        if (isNotContract == null) throw new UnsupportedOperationException ("Not supported yet, only isNotContract please");
        
        final JsonObject jsonObject = toJsonObject (isNotContract);
logger.info ("jsonObject=" + jsonObject);

        final Map<String, Object> r = DB.to.Map (jsonObject);
logger.info ("r=" + r);

        r.put (SupplyResourceContract.c.UUID_ORG.lc (), wsr.get (WsMessages.c.UUID_ORG.lc ()));

        setCustomer (r, src);
        
logger.info ("r=" + r);

        try (DB db = m.getDb ()) {
            UUID uuid = (UUID) db.insertId (SupplyResourceContract.class, r);
            return uuid.toString ();
        }
        
    }

    protected void setCustomer (final Map<String, Object> r, DRSORegOrgType regOrg, DRSOIndType ind) {
        if (regOrg != null) {
            r.put (c.UUID_ORG_CUSTOMER.lc (), regOrg.getOrgRootEntityGUID ());
            return;
        }
    }
    
    protected void setCustomer (final Map<String, Object> r, SupplyResourceContractType src) {
        
        if (DB.ok (src.isOffer ())) {
            r.put (c.ID_CUSTOMER_TYPE.lc (), VocGisSupplyResourceContractCustomerType.i.OFFER.getId ());
            return;
        }
        
        final SupplyResourceContractType.ApartmentBuildingOwner abo = src.getApartmentBuildingOwner ();        
        if (abo != null) {
            r.put (c.ID_CUSTOMER_TYPE.lc (), VocGisSupplyResourceContractCustomerType.i.OWNER.getId ());
            if (!DB.ok (abo.isNoData ())) setCustomer (r, abo.getRegOrg (), abo.getInd ());
            return;
        }

        final SupplyResourceContractType.ApartmentBuildingRepresentativeOwner abro = src.getApartmentBuildingRepresentativeOwner ();
        if (abro != null) {
            r.put (c.ID_CUSTOMER_TYPE.lc (), VocGisSupplyResourceContractCustomerType.i.REPRESENTATIVEOWNER.getId ());
            if (!DB.ok (abro.isNoData ())) setCustomer (r, abro.getRegOrg (), abro.getInd ());
        }

        final SupplyResourceContractType.ApartmentBuildingSoleOwner abso = src.getApartmentBuildingSoleOwner ();        
        if (abso != null) {
            r.put (c.ID_CUSTOMER_TYPE.lc (), VocGisSupplyResourceContractCustomerType.i.SOLEOWNER.getId ());            
            if (!DB.ok (abso.isNoData ())) setCustomer (r, abso.getRegOrg (), abso.getInd ());
        }
        
        final SupplyResourceContractType.LivingHouseOwner lho = src.getLivingHouseOwner ();        
        if (lho != null) {
            r.put (c.ID_CUSTOMER_TYPE.lc (), VocGisSupplyResourceContractCustomerType.i.LIVINGHOUSEOWNER.getId ());
            if (!DB.ok (lho.isNoData ())) setCustomer (r, lho.getRegOrg (), lho.getInd ());
        }
                
        if (DB.ok (src.getOrganization ())) {
            r.put (c.ID_CUSTOMER_TYPE.lc (), VocGisSupplyResourceContractCustomerType.i.ORGANIZATION.getId ());
            r.put (c.UUID_ORG_CUSTOMER.lc (), src.getOrganization ().getOrgRootEntityGUID ());
        }
        
    }

}
