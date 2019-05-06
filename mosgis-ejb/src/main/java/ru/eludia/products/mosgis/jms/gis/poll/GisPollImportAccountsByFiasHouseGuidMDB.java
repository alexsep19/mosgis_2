package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.tables.Account;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.HouseLog;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.voc.VocAccountType;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisHouseManagementClient;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.house_management.ExportAccountResultType;
import ru.gosuslugi.dom.schema.integration.house_management.GetStateResult;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportAccountsByFiasHouseGuidQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollImportAccountsByFiasHouseGuidMDB  extends GisPollMDB {

    @EJB
    private WsGisHouseManagementClient wsGisHouseManagementClient;

    @Resource (mappedName = "mosgis.inExportOrgAccountsQueue")
    Queue inExportOrgAccountsQueue;
    
    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")                
            .toOne (HouseLog.class, "AS log", "uuid", "action", "uuid_vc_org_log").on ("log.uuid_out_soap=root.uuid")
            .toOne (House.class,    "AS r", House.c.FIASHOUSEGUID.lc () + " AS fiashouseguid").on ("log.uuid_object=r.uuid")
            .toOne (VocOrganization.class, "AS org", VocOrganization.c.ORGPPAGUID.lc () + " AS orgppaguid").on ("log.uuid_org=org.uuid")
;
    }
    
    @Override
    protected void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        if (DB.ok (r.get ("is_failed"))) throw new IllegalStateException (r.get ("err_text").toString ());
                                
        try {
            
            GetStateResult state = getState (r);
            
            if (isEmpty (state)) return;
            
            List<ExportAccountResultType> exportAccountResult = state.getExportAccountResult ();
            
            if (exportAccountResult == null || exportAccountResult.isEmpty ()) throw new GisPollException ("0", "Сервис ГИС ЖКХ вернул пустой результат");

            for (ExportAccountResultType i: exportAccountResult) store (db, i, r);
            
        }
        catch (GisPollRetryException ex) {
            return;
        }
        catch (GisPollException ex) {            
            ex.register (db, uuid, r);
        }
        finally {
            uuidPublisher.publish (inExportOrgAccountsQueue, (UUID) r.get ("log.uuid_vc_org_log"));
        }
        
    }

    private boolean isEmpty (GetStateResult state) throws GisPollException {
        
        ErrorMessageType errorMessage = state.getErrorMessage ();
        
        if (errorMessage == null) return false;
        
        if (!"INT002012".equals (errorMessage.getErrorCode ())) throw new GisPollException (errorMessage);

        logger.info ("No account found for this fiashouseguid, OK.");
        
        return true;
            
    }

    void store (DB db, ExportAccountResultType acc, Map<String, Object> r) throws SQLException {

        logger.info ("Handling account " + acc.getAccountGUID () + " / " + acc.getAccountNumber () + " / " + acc.getUnifiedAccountNumber () + "...");
        
        if (DB.ok (acc.isIsTKOAccount ())) {
            logger.info ("isTKOAccount set: bailing out");
            return;
        }
        
        if (DB.ok (acc.isIsCRAccount ())) {
            logger.info ("isCRAccount set: bailing out");
            return;
        }
        
        if (DB.ok (acc.isIsUOAccount ())) {
            storeUOAccount (db, acc, r);
            return;
        }

    }
    
    void storeUOAccount (DB db, ExportAccountResultType acc, Map<String, Object> r) throws SQLException {
        
        final String contractGUID = acc.getAccountReasons ().getContract ().getContractGUID ();
        
        Map<String, Object> contract = db.getMap (db.getModel ()
            .select (Contract.class, "*")
            .where  (Contract.c.CONTRACTGUID, contractGUID)
        );
        
        if (contract == null) {
            logger.warning ("Contract not found by contractGUID=" + contractGUID);
            return;
        }
        
        final Map<String, Object> h = HASH (
            Account.c.ID_TYPE,              VocAccountType.i.UO.getId (),
            Account.c.UUID_CONTRACT,        contract.get ("uuid"),
            Account.c.UUID_ORG,             contract.get (Contract.c.UUID_ORG.lc ()),
            Account.c.FIASHOUSEGUID,        r.get ("fiashouseguid"),
            Account.c.ACCOUNTNUMBER,        acc.getAccountNumber (),
            Account.c.SERVICEID,            acc.getServiceID (),
            Account.c.UNIFIEDACCOUNTNUMBER, acc.getUnifiedAccountNumber (),
            Account.c.ACCOUNTGUID,          acc.getAccountGUID ()
        );
        
        if (acc.getTotalSquare () != null) h.put (Account.c.TOTALSQUARE.lc (), acc.getTotalSquare ());
        if (acc.getLivingPersonsNumber () != null) h.put (Account.c.LIVINGPERSONSNUMBER.lc (), acc.getLivingPersonsNumber ());
        if (acc.getResidentialSquare () != null) h.put (Account.c.RESIDENTIALSQUARE.lc (), acc.getResidentialSquare ());
        if (acc.getHeatedArea () != null) h.put (Account.c.HEATEDAREA.lc (), acc.getHeatedArea ());
        
        db.upsert (Account.class, h, Account.c.ACCOUNTGUID.lc ());

    }
        
    private GetStateResult getState (Map<String, Object> r) throws GisPollRetryException, GisPollException {

        GetStateResult rp;
        
        try {
            rp = wsGisHouseManagementClient.getState ((UUID) r.get ("orgppaguid"), (UUID) r.get ("uuid_ack"));
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
    
}