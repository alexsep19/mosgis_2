package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationLog;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationTypes;
import ru.eludia.products.mosgis.ejb.wsc.RestGisFilesClient;
import ru.eludia.products.mosgis.ejb.wsc.WsGisHouseManagementClient;
import static ru.eludia.products.mosgis.jms.gis.poll.GisPollExportMgmtContractDataMDB.updateContract;
import ru.gosuslugi.dom.schema.integration.house_management.ExportCAChResultType;
import ru.gosuslugi.dom.schema.integration.house_management.GetStateResult;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportOrgMgmtContractsQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportOrgMgmtContracts extends GisPollMDB {

    @EJB
    WsGisHouseManagementClient wsGisHouseManagementClient;
    
    @EJB
    RestGisFilesClient filesClient;
    
    @Resource (mappedName = "mosgis.inOrgByGUIDQueue")
    Queue inOrgByGUIDQueue;

    @Override
    protected Get get (UUID uuid) {

        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")
            .toOne (VocOrganizationLog.class, "AS log", "uuid", "action", "uuid_object").on ("log.uuid_out_soap=root.uuid")
            .toOne (VocOrganization.class, "AS org", VocOrganization.c.ORGPPAGUID.lc () + " AS ppa").on ("log.uuid_object=org.uuid")
        ;
        
    }

    @Override
    protected void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        if (DB.ok (r.get ("is_failed"))) throw new IllegalStateException (r.get ("err_text").toString ());
        
        UUID orgPPAGuid = (UUID) r.get ("ppa");
        UUID uuidOrg    = (UUID) r.get ("log.uuid_object");
                        
        try {            
            
            GetStateResult state = getState (orgPPAGuid, r);                        
logger.info ("state=" + state);

            List<ExportCAChResultType> exportCAChResult = state.getExportCAChResult ();
            
            if (exportCAChResult == null) throw new GisPollException ("0", "Сервис ГИС вернул пустой результат");
            
            List <Map<String, Object>> contracts = toHashList (exportCAChResult, uuidOrg);
                        
            try {                
                importMissingOrgs (db, contracts);
            }
            catch (Exception ex) {
                logger.log (Level.SEVERE, "Cannot import contract related entity", ex);
                throw new GisPollException (ex);
            }

            try (DB db0 = ModelHolder.getModel ().getDb ()) {                
                process (db0, uuidOrg, contracts);                
            }
            catch (Exception ex) {
                logger.log (Level.SEVERE, "Cannot parse exportCAChResult", ex);
                throw new GisPollException (ex);
            }

        }
        catch (GisPollRetryException ex) {
            return;
        }
        catch (GisPollException ex) {            
            ex.register (db, uuid, r);
        }
        
    }

    private GetStateResult getState (UUID orgPPAGuid, Map<String, Object> r) throws GisPollRetryException, GisPollException {

        GetStateResult rp;
        
        try {
            rp = wsGisHouseManagementClient.getState (orgPPAGuid, (UUID) r.get ("uuid_ack"));
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

    void process (DB db, UUID uuidOrg, List <Map<String, Object>> contracts) throws Exception {

        final MosGisModel model = ModelHolder.getModel ();

        Table table = model.get (Contract.class);

        for (Map<String, Object> h: contracts) {
logger.info ("h=" + h);
            db.begin ();
            upsertContract (db, h, model, table, uuidOrg);
            db.commit ();
        }

    }
    
    void importMissingOrgs (DB db, List<Map<String, Object>> contracts) throws Exception {
        
        Set<String> uuidOrgs = new HashSet <> ();
        
        for (Map<String, Object> h: contracts) {
            Object uuid = h.get (Contract.c.UUID_ORG_CUSTOMER.lc ());
            if (uuid != null) uuidOrgs.add (uuid.toString ());
        }
        
        if (uuidOrgs.isEmpty ()) return;
        
        MosGisModel model = ModelHolder.getModel ();
        Table t = model.get (VocOrganization.class);
        
        db.forEach (model
            .select (t, "uuid")
            .where ("uuid IN", uuidOrgs.toArray ()), (rs) -> {
                uuidOrgs.remove (DB.to.UUIDFromHex (rs.getString (1)).toString ());
            }
        );

        final Map<String, Object> h = DB.HASH (
            EnTable.c.IS_DELETED, 1,
            VocOrganization.c.ID_TYPE, VocOrganizationTypes.i.LEGAL.getId (),
            VocOrganization.c.SHORTNAME, "[Загрузка в процессе...]"
        );
    
        for (String uuid: uuidOrgs) {

            try {
                h.put (VocOrganization.c.ORGROOTENTITYGUID.lc (), uuid);
                db.insert (t, h);
                String idLog = model.createIdLog (db, t, null, uuid, VocAction.i.REFRESH);
                if (uuidPublisher != null) uuidPublisher.publish (inOrgByGUIDQueue, idLog);
            }
            catch (SQLException ex) {
                if (ex.getErrorCode () != 1) logger.log (Level.SEVERE, "Cannot register org #" + uuid, ex);
            }
            
        }
                        
    }

    void upsertContract (DB db, Map<String, Object> h, final MosGisModel model, Table table, UUID uuidOrg) throws SQLException {        
        h.put (Contract.c.ID_CTR_STATUS.lc (), VocGisStatus.i.PENDING_RQ_RELOAD);
        db.upsert (Contract.class, h, Contract.c.CONTRACTGUID.lc ());        
        String ctrUuid = db.getString (db.getModel ().select (Contract.class, "uuid").where (Contract.c.CONTRACTGUID, h.get (Contract.c.CONTRACTGUID.lc ())));        
        updateContract (db, uuidOrg, UUID.fromString (ctrUuid), filesClient, (ExportCAChResultType.Contract) h.get ("_"), true);
        model.createIdLog (db, table, null, ctrUuid, VocAction.i.IMPORT_MGMT_CONTRACTS);
    }

    List<Map<String, Object>> toHashList (List<ExportCAChResultType> exportCAChResult, UUID uuidOrg) {
        List <Map<String, Object>> contracts = new ArrayList<> ();
        for (ExportCAChResultType cach: exportCAChResult) addhash (cach.getContract (), uuidOrg, contracts);
        return contracts;
    }

    void addhash (ExportCAChResultType.Contract contract, UUID uuidOrg, List<Map<String, Object>> contracts) {
        
        if (contract == null) return;

        final Map<String, Object> h = Contract.toHASH (contract);
        
        h.put (Contract.c.UUID_ORG.lc (), uuidOrg);
        h.put (Contract.c.ID_CONTRACT_TYPE.lc (), 1);
        h.put (Contract.c.ID_LOG.lc (), null);
        h.put ("_", contract);
        
        contracts.add (h);
        
    }
                
}