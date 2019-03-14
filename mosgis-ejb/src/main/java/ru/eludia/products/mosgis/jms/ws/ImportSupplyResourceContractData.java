package ru.eludia.products.mosgis.jms.ws;

import java.sql.SQLException;
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
import javax.xml.datatype.XMLGregorianCalendar;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract.c;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisSupplyResourceContractCustomerType;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;
import ru.eludia.products.mosgis.db.model.ws.WsMessages;
import ru.eludia.products.mosgis.jms.base.WsMDB;
import ru.eludia.products.mosgis.ws.soap.impl.base.Errors;
import ru.gosuslugi.dom.schema.integration.base.BaseAsyncResponseType;
import ru.eludia.products.mosgis.ws.soap.impl.base.Fault;
import ru.eludia.products.mosgis.ws.soap.tools.SOAPTools;
import ru.gosuslugi.dom.schema.integration.base.CommonResultType;
import ru.gosuslugi.dom.schema.integration.house_management.DRSOIndType;
import ru.gosuslugi.dom.schema.integration.house_management.DRSORegOrgType;
import ru.gosuslugi.dom.schema.integration.house_management.ImportSupplyResourceContractRequest;
import ru.gosuslugi.dom.schema.integration.house_management.GetStateResult;
import ru.gosuslugi.dom.schema.integration.house_management.ImportResult;
import ru.gosuslugi.dom.schema.integration.house_management.SupplyResourceContractType;
import ru.gosuslugi.dom.schema.integration.individual_registry_base.ID;

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
        
        final ImportResult.CommonResult.ImportSupplyResourceContract importSupplyResourceContract = toImportSupplyResourceContract (r, i);
        
        try {
            result.setImportSupplyResourceContract (importSupplyResourceContract);
            result.setGUID (importSupplyResourceContract.getContractGUID ());
            result.setUpdateDate (SOAPTools.xmlNow ());
        }
        catch (Exception ex) {
            logger.log (Level.WARNING, i.getTransportGUID (), ex);
            final CommonResultType.Error error = new CommonResultType.Error ();
            error.setErrorCode (Errors.FMT001300.name ());
            error.setDescription (ex.getMessage ());
            result.getError ().add (error);
        }
        
        return result;
        
    }


    private ImportResult.CommonResult.ImportSupplyResourceContract toImportSupplyResourceContract (Map<String, Object> r, ImportSupplyResourceContractRequest.Contract i) throws Exception {
        String contractGUID = i.getContractGUID ();
        if (i.getSupplyResourceContract () != null) return toImportSupplyResourceContract (r, contractGUID, i.getSupplyResourceContract ());
        throw new UnsupportedOperationException ("Not supported yet."); 
    }

    private ImportResult.CommonResult.ImportSupplyResourceContract toImportSupplyResourceContract (Map<String, Object> wsr, String contractGUID, SupplyResourceContractType src) throws Exception {
        
        if (contractGUID != null) throw new UnsupportedOperationException ("Updating is not supported yet.");

        MosGisModel m = ModelHolder.getModel ();
        
        final Map<String, Object> r;
        
        SupplyResourceContractType.IsNotContract isNotContract = src.getIsNotContract ();
        if (isNotContract != null) {
            final JsonObject jsonObject = toJsonObject (isNotContract);
logger.info ("jsonObject=" + jsonObject);
            r = DB.to.Map (jsonObject);
            r.put (c.IS_CONTRACT.lc (), 0);
        }
        else {
            throw new UnsupportedOperationException ("Not supported yet, only isNotContract please");
        }        

logger.info ("r=" + r);

        r.put (SupplyResourceContract.c.UUID_ORG.lc (), wsr.get (WsMessages.c.UUID_ORG.lc ()));

        setCustomer (r, src);
        
logger.info ("r=" + r);

        ImportResult.CommonResult.ImportSupplyResourceContract result = new ImportResult.CommonResult.ImportSupplyResourceContract ();
        
        try (DB db = m.getDb ()) {
            
            UUID uuid = (UUID) db.insertId (SupplyResourceContract.class, r);
            
            result.setContractRootGUID (uuid.toString ());
            
            String idLog = m.createIdLogWs (db, m.get (SupplyResourceContract.class), (UUID) wsr.get ("uuid"), uuid, VocAction.i.CREATE);
            
            result.setContractGUID (idLog);

            return result;

        }
        
    }

    protected void setCustomer (final Map<String, Object> r, DRSORegOrgType regOrg) {
        final String orgRootEntityGUID = regOrg.getOrgRootEntityGUID ();
        if (orgRootEntityGUID == null) throw new IllegalArgumentException ("Не указан orgRootEntityGUID для DRSORegOrgType");
        r.put (c.UUID_ORG_CUSTOMER.lc (), orgRootEntityGUID);
    }    
    
    private void setCustomer (Map<String, Object> r, DRSOIndType ind) throws Exception {
        
        Map<String, Object> person = DB.to.Map (toJsonObject (ind));
        
        String snils = ind.getSNILS ();
        
        if (snils == null) {                
            setCustomer (r, person, ind.getID ());
        }
        else {
            
            person.put (VocPerson.c.SNILS.lc (), snils);
            
            final MosGisModel m = ModelHolder.getModel ();
            
            try (DB db = m.getDb ()) {

                db.upsert (VocPerson.class, person, VocPerson.c.SNILS.lc ());
                
                r.put (c.UUID_PERSON_CUSTOMER.lc (),
                    db.getString (m
                        .select (VocPerson.class, EnTable.c.UUID.lc ())
                        .where (VocPerson.c.SNILS, snils)
                    )
                );                

            }

        }
        
    }
    
    private void setCustomer (Map<String, Object> r, Map<String, Object> person, ID id) throws Exception {
        
        if (id == null) throw new IllegalArgumentException ("Не указан ни СНИЛС, ни документ");
        
        person.put (VocPerson.c.CODE_VC_NSI_95.lc (), id.getType ().getCode ());
        person.put (VocPerson.c.SERIES.lc (), id.getSeries ());
        person.put (VocPerson.c.NUMBER_.lc (), id.getNumber ());
        person.put (VocPerson.c.ISSUEDATE.lc (), id.getIssueDate ());

        final MosGisModel m = ModelHolder.getModel ();

        try (DB db = m.getDb ()) {
/*
            db.upsert (VocPerson.class, person, VocPerson.c.SNILS.lc ());

            r.put (c.UUID_PERSON_CUSTOMER.lc (),
                db.getString (m
                    .select (VocPerson.class, EnTable.c.UUID.lc ())
                    .where (VocPerson.c.SNILS, snils)
                )
            );                
*/
        }
        
    }

    protected void setCustomer (final Map<String, Object> r, DRSORegOrgType regOrg, DRSOIndType ind) throws Exception {
        
        if (regOrg != null) {
            setCustomer (r, regOrg);
        }
        else if (ind != null) {
            setCustomer (r, ind);
        }
        else {
            throw new IllegalArgumentException ("No regOrg nor ind provided");
        }
                        
    }
    
    protected void setCustomer (final Map<String, Object> r, SupplyResourceContractType src) throws Exception {
        
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
