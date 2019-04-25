package ru.eludia.products.mosgis.jms.ws;

import java.sql.SQLException;
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
import ru.eludia.base.Model;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.BankAccount;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
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

	if (i.getLoadAccountRegOperator() != null) {
	    r.put(BankAccount.c.ACCOUNTREGOPERATORGUID.lc(), i.getAccountRegOperatorGuid());
	    return toImportAccountRegOperator (r, accountRegOperatorGuid, i.getLoadAccountRegOperator() );
	}

        throw new UnsupportedOperationException ("Не найден LoadAccountRegOperator, такие запросы пока не поддерживаются");
    }

    private ImportAccountRegOperator toImportAccountRegOperator (Map<String, Object> wsr, String accountRegOperatorGuid, LoadAccountRegOperator src) throws Exception {

        MosGisModel m = ModelHolder.getModel ();

        Map<String, Object> r = toMap (src);

	r.put (BankAccount.c.UUID_ORG.lc (), wsr.get (WsMessages.c.UUID_ORG.lc ()));
	r.put (BankAccount.c.ACCOUNTREGOPERATORGUID.lc(), wsr.get(BankAccount.c.ACCOUNTREGOPERATORGUID.lc()));


        ImportAccountRegOperator result = new ImportAccountRegOperator ();

        final UUID uuidInSoap = (UUID) wsr.get ("uuid");

        try (DB db = m.getDb ()) {

	    setCredOrg (db, src, r);

	    String upsertKey = getUpsertKey(db, r);

	    logger.info ("r=" + r);

	    String uuid = db.upsertId (BankAccount.class, r, upsertKey);

            String idLog = m.createIdLogWs (db, BankAccount.class, uuidInSoap, uuid, VocAction.i.CREATE);

            result.setAccountRegOperatorGuid(uuid);

            return result;

        }
    }

    private Map<String, Object> toMap (LoadAccountRegOperator src) throws UnsupportedOperationException {
        
        final Map<String, Object> r = DB.to.Map(src);

	r.put(BankAccount.c.ACCOUNTNUMBER.lc(), src.getNumber());

	r.put(BankAccount.c.IS_ROKR.lc(), 1);

	logger.info("toMap r=" + r);

	return r;
        
    }

    private String getUpsertKey(DB db, Map<String, Object> r) throws SQLException {

	final Model m = db.getModel();

	Object id = r.get(BankAccount.c.ACCOUNTREGOPERATORGUID.lc());

	if (!DB.ok(id)) {
	    r.remove(BankAccount.c.ACCOUNTREGOPERATORGUID.lc());
	    r.remove(EnTable.c.UUID.lc());
	    return null;
	}

	final Map<String, Object> ba = db.getMap (m
	    .select (BankAccount.class, EnTable.c.UUID.lc(), BankAccount.c.ID_CTR_STATUS.lc())
	    .where  (EnTable.c.UUID, id)
	);


	if (ba != null) {
	    checkStatus (ba);
	    r.put(EnTable.c.UUID.lc(), r.get(BankAccount.c.ACCOUNTREGOPERATORGUID.lc()));
	    r.remove(BankAccount.c.ACCOUNTREGOPERATORGUID.lc());
	    return EnTable.c.UUID.lc();
	}

	final Map<String, Object> baApproved = db.getMap (m
	    .select (BankAccount.class, BankAccount.c.ACCOUNTREGOPERATORGUID.lc(), BankAccount.c.ID_CTR_STATUS.lc())
	    .where  (BankAccount.c.ACCOUNTREGOPERATORGUID, id)
	);


	if (baApproved != null) {
	    checkStatus(baApproved);
	    r.remove(EnTable.c.UUID.lc());
	    return BankAccount.c.ACCOUNTREGOPERATORGUID.lc();
	}

	r.remove(BankAccount.c.ACCOUNTREGOPERATORGUID.lc());
	r.remove(EnTable.c.UUID.lc());

	return EnTable.c.UUID.lc();
    }

    private void setCredOrg(DB db, LoadAccountRegOperator src, Map<String, Object> r) throws SQLException {

	RegOrgType credOrg = src.getCredOrganization();

	if (credOrg == null) {
	    throw new UnsupportedOperationException("Не найден CredOrganization, такие запросы не поддерживаются");
	}

	final Model m = db.getModel();

	String uuidCredOrg = db.getString(m
	    .select(VocOrganization.class, EnTable.c.UUID.lc())
	    .where(VocOrganization.c.ORGROOTENTITYGUID, credOrg.getOrgRootEntityGUID())
	);

	if (uuidCredOrg == null) {
	    throw new UnsupportedOperationException("Не найден CredOrganization по orgRootEntityGUID, такие запросы не поддерживаются");
	}

	r.put(BankAccount.c.UUID_CRED_ORG.lc(), uuidCredOrg);
    }

    private void checkStatus(Map<String, Object> ba) {

	switch(VocGisStatus.i.forId(ba.get(BankAccount.c.ID_CTR_STATUS.lc()))) {
	    case PROJECT:
	    case MUTATING:
		break;
	    default:
		throw new UnsupportedOperationException("Операции обновления не поддерживаются на статусе"
		    + VocGisStatus.i.forId(ba.get(BankAccount.c.ID_CTR_STATUS.lc())).getLabel()
		);
	}
    }
}