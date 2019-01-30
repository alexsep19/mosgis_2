package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationLog;
import ru.eludia.products.mosgis.ejb.wsc.WsGisHouseManagementClient;
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
                        
        try {            
            
            GetStateResult state = getState (orgPPAGuid, r);                        
logger.info ("state=" + state);

            List<ExportCAChResultType> exportCAChResult = state.getExportCAChResult ();
            
            if (exportCAChResult == null) throw new GisPollException ("0", "Сервис ГИС вернул пустой результат");
            
            try (DB db0 = ModelHolder.getModel ().getDb ()) {                
                process (db0, (UUID) r.get ("log.uuid_object"), exportCAChResult);
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
logger.log (Level.SEVERE, "ZZZZ", ex);
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

    void process (DB db, UUID uuidOrg, List<ExportCAChResultType> exportCAChResult) throws Exception {
        
        final MosGisModel model = ModelHolder.getModel ();
        
        Table table = model.get (Contract.class);
        
        db.begin ();
        
        List <Map<String, Object>> contracts = new ArrayList<> ();
        
        for (ExportCAChResultType cach: exportCAChResult) {
            
            ExportCAChResultType.Contract contract = cach.getContract ();
            
            if (contract == null) continue;
            
            final Map<String, Object> h = Contract.toHASH (contract);
            
            h.put (Contract.c.UUID_ORG.lc (), uuidOrg);
            h.put (Contract.c.ID_CONTRACT_TYPE.lc (), 1);
            h.put (Contract.c.ID_LOG.lc (), null);
            
            contracts.add (h);
            
        }
        
        for (Map<String, Object> h: contracts) {
            
logger.info ("h=" + h);
            
            db.upsert (Contract.class, h, Contract.c.CONTRACTGUID.lc ());
            
            String uuid = db.getString (db.getModel ().select (Contract.class, "uuid").where (Contract.c.CONTRACTGUID, h.get (Contract.c.CONTRACTGUID.lc ())));
            
            model.createIdLog (db, table, null, uuid, VocAction.i.IMPORT_MGMT_CONTRACTS);

        }
        
        db.commit ();
        
    }
    
}