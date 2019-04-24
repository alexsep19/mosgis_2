package ru.eludia.products.mosgis.jms.ws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.BankAccount;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.ws.WsMessages;
import ru.eludia.products.mosgis.jms.base.WsMDB;
import ru.eludia.products.mosgis.ws.soap.impl.base.Errors;
import ru.gosuslugi.dom.schema.integration.base.BaseAsyncResponseType;
import ru.eludia.products.mosgis.ws.soap.impl.base.Fault;
import ru.eludia.products.mosgis.ws.soap.tools.SOAPTools;
import ru.gosuslugi.dom.schema.integration.base.CommonResultType;
import ru.gosuslugi.dom.schema.integration.capital_repair.ImportAccountRegionalOperatorRequest;
import ru.gosuslugi.dom.schema.integration.capital_repair.ImportAccountRegionalOperatorRequest.ImportAccountRegOperator;
import ru.gosuslugi.dom.schema.integration.capital_repair.ImportAccountRegionalOperatorRequest.ImportAccountRegOperator.LoadAccountRegOperator;
import ru.gosuslugi.dom.schema.integration.capital_repair.GetStateResult;
import ru.gosuslugi.dom.schema.integration.capital_repair.CapRemCommonResultType;
import ru.gosuslugi.dom.schema.integration.organizations_registry_base.RegOrgType;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inSoapImportRegionalOperatorAccounts")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ImportRegionalOperatorAccounts extends WsMDB {
    
    private final static JAXBContext jc;
    
    static {
        try {
            jc = JAXBContext.newInstance (
                GetStateResult.class,
                ImportAccountRegionalOperatorRequest.class
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
        if (!(request instanceof ImportAccountRegionalOperatorRequest)) throw new Fault (Errors.FMT001300);
        final GetStateResult result = new GetStateResult ();
        fill (result, r, (ImportAccountRegionalOperatorRequest) request);
        return result;
    }

    private void fill (GetStateResult result, Map<String, Object> r, ImportAccountRegionalOperatorRequest importAccountRegionalOperatorRequest) {
        
        List<CapRemCommonResultType> commonResult = new ArrayList <>();
        
        try {
            for (ImportAccountRegionalOperatorRequest.ImportAccountRegOperator i: importAccountRegionalOperatorRequest.getImportAccountRegOperator()) commonResult.add (toCommonResult (r, i));
            result.getImportResult ().addAll (commonResult);
        }
        catch (Exception ex) {
            logger.log (Level.WARNING, "Problem with ImportAccountRegionalOperatorRequest", ex);
            final CommonResultType.Error error = new CommonResultType.Error ();
            error.setErrorCode ("EXP001000");
            error.setDescription (ex.getMessage ());
            error.setStackTrace (Arrays.toString (ex.getStackTrace ()));
            result.setErrorMessage (error);
        }
        
    }

    private CapRemCommonResultType toCommonResult (Map<String, Object> r, ImportAccountRegionalOperatorRequest.ImportAccountRegOperator i) throws Exception {
        
        final CapRemCommonResultType result = new CapRemCommonResultType ();

        result.setTransportGUID (i.getTransportGuid());
        
        try {
	    final ImportAccountRegOperator importAccountRegOperator = toImportAccountRegOperator (r, i);
            result.setGUID (importAccountRegOperator.getAccountRegOperatorGuid());
            result.setUpdateDate (SOAPTools.xmlNow ());
        }
        catch (Exception ex) {
            logger.log (Level.WARNING, i.getTransportGuid(), ex);
            final CommonResultType.Error error = new CommonResultType.Error ();
            error.setErrorCode (Errors.FMT001300.name ());
            error.setDescription (i.getTransportGuid () + ": " + ex.getMessage ());
            result.getError ().add (error);
        }
        
        return result;
        
    }

    private ImportAccountRegOperator toImportAccountRegOperator (Map<String, Object> r, ImportAccountRegionalOperatorRequest.ImportAccountRegOperator i) throws Exception {
        String accountRegOperatorGuid = i.getAccountRegOperatorGuid();
        if (i.getLoadAccountRegOperator() != null) return toImportAccountRegOperator (r, accountRegOperatorGuid, i.getLoadAccountRegOperator() );
        throw new UnsupportedOperationException ("Не найден AccountRegionalOperator, такие запросы пока не поддерживаются");
    }

    private ImportAccountRegOperator toImportAccountRegOperator (Map<String, Object> wsr, String accountRegOperatorGuid, LoadAccountRegOperator src) throws Exception {

        if (accountRegOperatorGuid != null) throw new UnsupportedOperationException ("Операции обновления пока не поддерживаются");

        MosGisModel m = ModelHolder.getModel ();

        Map<String, Object> r = toMap (src);

	r.put (BankAccount.c.IS_ROKR.lc(), 1);
        r.put (BankAccount.c.UUID_ORG.lc (), wsr.get (WsMessages.c.UUID_ORG.lc ()));

logger.info ("r=" + r);

        ImportAccountRegOperator result = new ImportAccountRegOperator ();

        final UUID uuidInSoap = (UUID) wsr.get ("uuid");

        try (DB db = m.getDb ()) {

            UUID uuid = (UUID) db.insertId (BankAccount.class, r);

            String idLog = m.createIdLogWs (db, BankAccount.class, uuidInSoap, uuid, VocAction.i.CREATE);

            result.setAccountRegOperatorGuid(idLog);

            return result;

        }
    }

    private Map<String, Object> toMap (LoadAccountRegOperator src) throws UnsupportedOperationException {
        
        final Map<String, Object> r = DB.to.Map(src);
        
        RegOrgType credOrg = src.getCredOrganization();
        if (credOrg != null) {
            r.put("orgppaguid", credOrg.getOrgRootEntityGUID());
        }
        else {
            throw new UnsupportedOperationException ("Не найден credOrg, такие запросы не поддерживаются");
        }

	r.put(BankAccount.c.ACCOUNTNUMBER.lc(), src.getNumber());
        
        return r;
        
    }
}