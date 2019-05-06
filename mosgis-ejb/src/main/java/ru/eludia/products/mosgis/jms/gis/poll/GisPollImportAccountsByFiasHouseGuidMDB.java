package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.ColEnum;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.tables.Account;
import ru.eludia.products.mosgis.db.model.tables.AccountItem;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.HouseLog;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.Premise;
import ru.eludia.products.mosgis.db.model.voc.VocAccountType;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisHouseManagementClient;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.house_management.AccountExportType;
import ru.gosuslugi.dom.schema.integration.house_management.AccountIndExportType;
import ru.gosuslugi.dom.schema.integration.house_management.ExportAccountResultType;
import ru.gosuslugi.dom.schema.integration.house_management.GetStateResult;
import ru.gosuslugi.dom.schema.integration.organizations_registry_base.RegOrgVersionType;

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
        
        storeAccount (contract, r, acc, db);

    }

    private void storeAccount (Map<String, Object> contract, Map<String, Object> r, ExportAccountResultType acc, DB db) throws SQLException {
        
        final UUID uuidOrg = (UUID) contract.get (Contract.c.UUID_ORG.lc ());
        final UUID fiasHouseGuid = (UUID) r.get ("fiashouseguid");

        final Map<String, Object> h = HASH (
            Account.c.ID_TYPE,              VocAccountType.i.UO.getId (),
            Account.c.UUID_CONTRACT,        contract.get ("uuid"),
            Account.c.UUID_ORG,             uuidOrg,
            Account.c.FIASHOUSEGUID,        fiasHouseGuid,
            Account.c.ACCOUNTNUMBER,        acc.getAccountNumber (),
            Account.c.SERVICEID,            acc.getServiceID (),
            Account.c.UNIFIEDACCOUNTNUMBER, acc.getUnifiedAccountNumber (),
            Account.c.ACCOUNTGUID,          acc.getAccountGUID ()
        );
        
        AccountExportType.PayerInfo payerInfo = acc.getPayerInfo ();
        
        set (h, Account.c.TOTALSQUARE, acc.getTotalSquare ());
        set (h, Account.c.LIVINGPERSONSNUMBER, acc.getLivingPersonsNumber ());
        set (h, Account.c.RESIDENTIALSQUARE, acc.getResidentialSquare ());
        set (h, Account.c.HEATEDAREA, acc.getHeatedArea ());
        set (h, Account.c.ISRENTER, payerInfo.isIsRenter ());
        
        if (setCustomer (payerInfo, h, db, uuidOrg)) return;
        
        List<Map<String, Object>> items;       
        
        try {
            items = toItems (db, acc.getAccommodation ());
        }
        catch (UnknownSomethingException ex) {
            String msg = "ЛС " + acc.getAccountNumber () + ", " + ex.toString ();
            logger.warning (msg);
            Map<String, Object> map = db.getMap (db.getModel ().get (OutSoap.class, getUuid (), "*"));
            StringBuilder sb = new StringBuilder (DB.to.String (map.get ("err_text")));
            if (sb.length () > 0) sb.append (";\n");
            sb.append (msg);
            db.update (OutSoap.class, HASH (
                "uuid", getUuid (),
                "is_failed", 1,
                "err_code", "0",
                "err_text", sb.toString ()
            ));
            return;
        }
        
        String uuidAccount = db.upsertId (Account.class, h, Account.c.ACCOUNTGUID.lc ());
        
        Map<String, Object> houseItem = null;
        
        for (Map<String, Object> item: items) {
            set (item, AccountItem.c.FIASHOUSEGUID, fiasHouseGuid);
            set (item, AccountItem.c.UUID_ACCOUNT, uuidAccount);
            set (item, EnTable.c.IS_DELETED, 0);
            if (item.get (AccountItem.c.UUID_PREMISE.lc ()) == null) houseItem = item;
        }
        
        if (houseItem != null) {

            items.remove (houseItem);

            houseItem.put ("uuid", db.getString (db.getModel ()
                .select (AccountItem.class, "uuid")
                .where  (AccountItem.c.UUID_ACCOUNT, uuidAccount)
                .and    (AccountItem.c.UUID_PREMISE.lc () + " IS NULL")
            ));

            if (houseItem.get ("uuid") == null) {
                db.insert (AccountItem.class, houseItem);
            }
            else {
                db.update (AccountItem.class, houseItem);
            }            

        }
        
        db.update (AccountItem.class, HASH (
            AccountItem.c.UUID_ACCOUNT, uuidAccount,
            EnTable.c.IS_DELETED, 1
        ), AccountItem.c.UUID_ACCOUNT.lc ());

        db.upsert (AccountItem.class, items
            , AccountItem.c.UUID_ACCOUNT.lc ()
            , AccountItem.c.UUID_PREMISE.lc ()
        );
                
    }
    
    private void set (Map<String, Object> h, ColEnum c, Object v) {
        if (v == null) return;
        h.put (c.lc (), v);
    }

    private boolean setCustomer (AccountExportType.PayerInfo payerInfo, final Map<String, Object> h, DB db, UUID uuidOrg) throws SQLException {
        final RegOrgVersionType org = payerInfo.getOrg ();
        if (org != null) return setOrgCustomer (h, org, db);
        set (h, Account.c.UUID_PERSON_CUSTOMER, getIndCustomerUuid (payerInfo.getInd (), db, uuidOrg));
        return false;
    }
    
    private String getIndCustomerUuid (final AccountIndExportType ind, DB db, UUID uuidOrg) throws SQLException {
        
        final Map<String, Object> p = HASH (
            VocPerson.c.FIRSTNAME, ind.getFirstName (),
            VocPerson.c.PATRONYMIC, ind.getPatronymic (),
            VocPerson.c.UUID_ORG,  uuidOrg
        );
        
        set (p, VocPerson.c.SURNAME, ind.getSurname ());
        set (p, VocPerson.c.FIRSTNAME, ind.getFirstName ());
        set (p, VocPerson.c.PATRONYMIC, ind.getPatronymic ());
        set (p, VocPerson.c.BIRTHDATE, ind.getDateOfBirth ());
        
        if (DB.ok (ind.getSex ())) p.put (VocPerson.c.IS_FEMALE.lc (), "F".equals (ind.getSex ()));
        
        AccountIndExportType.ID id = ind.getID ();
        
        if (id != null) {
            
            set (p, VocPerson.c.CODE_VC_NSI_95, id.getType ().getCode ());
            set (p, VocPerson.c.SERIES, id.getSeries ());
            set (p, VocPerson.c.NUMBER_, id.getNumber ());
            set (p, VocPerson.c.ISSUEDATE, id.getIssueDate ());
            
            return db.upsertId (VocPerson.class, p
                , VocPerson.c.CODE_VC_NSI_95.lc ()
                , VocPerson.c.SERIES.lc ()
                , VocPerson.c.NUMBER_.lc ()
            );
            
        }
        else {

            set (p, VocPerson.c.SNILS, ind.getSNILS ());
            
            return db.upsertId (VocPerson.class, p
                , VocPerson.c.SNILS.lc ()
            );
            
        }
        
    }

    private boolean setOrgCustomer (final Map<String, Object> h, final RegOrgVersionType org, DB db) throws SQLException {
        
        h.put (Account.c.IS_CUSTOMER_ORG.lc (), 1);
        
        final String orgVersionGUID = org.getOrgVersionGUID ();
        
        String uuidOrg = db.getString (db.getModel ().select (VocOrganization.class, "uuid").where (VocOrganization.c.ORGVERSIONGUID, orgVersionGUID));
        
        if (uuidOrg == null) {
            logger.warning ("Org not found by orgVersionGUID=" + orgVersionGUID);
            return true;
        }
        
        h.put (Account.c.UUID_ORG_CUSTOMER.lc (), uuidOrg);
        
        return false;
        
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
    
    List<Map<String, Object>> toItems (DB db, List<AccountExportType.Accommodation> accommodation) throws SQLException, UnknownSomethingException {
        List<Map<String, Object>> result = new ArrayList<> (accommodation.size ());
        for (AccountExportType.Accommodation i: accommodation) result.add (toItem (db, i));
        return result;
    }

    Map<String, Object> toItem (DB db, AccountExportType.Accommodation i) throws SQLException, UnknownSomethingException {
        Map<String, Object> r = HASH ();
        set (r, AccountItem.c.SHAREPERCENT, i.getSharePercent ());
        set (r, AccountItem.c.UUID_PREMISE, getUuidPremise (db, i));
        return r;
    }
    
    private String getUuidPremise (DB db, AccountExportType.Accommodation i) throws SQLException, UnknownSomethingException {

        Select select = db.getModel ().select (Premise.class, "id");

        String livingRoomGUID = i.getLivingRoomGUID ();

        if (livingRoomGUID != null) {
            String result = db.getString (select.where (Premise.c.LIVINGROOMGUID, livingRoomGUID));
            if (result == null) throw new UnknownLivingRoomException (livingRoomGUID);
            return result;
        }

        String premisesGUID = i.getPremisesGUID ();

        if (premisesGUID != null) {
            String result = db.getString (select.where (Premise.c.PREMISESGUID, premisesGUID));
            if (result == null) throw new UnknownPremiseException (premisesGUID);
            return result;
        }
        
        return null;

    }
    
    private abstract class UnknownSomethingException extends Exception {
        
        String uuid;

        public UnknownSomethingException (String uuid) {
            this.uuid = uuid;
        }
        
    }
    
    private class UnknownPremiseException extends UnknownSomethingException {

        public UnknownPremiseException (String uuid) {
            super (uuid);
        }

        @Override
        public String toString () {
            return "Неизвестное помещение: " + uuid;
        }
        
    }
    
    private class UnknownLivingRoomException extends UnknownSomethingException {

        public UnknownLivingRoomException (String uuid) {
            super (uuid);
        }

        @Override
        public String toString () {
            return "Неизвестная комната: " + uuid;
        }
        
    }

}