package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.tables.AdditionalService;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.ContractFile;
import ru.eludia.products.mosgis.db.model.tables.ContractLog;
import ru.eludia.products.mosgis.db.model.tables.ContractObject;
import ru.eludia.products.mosgis.db.model.tables.ContractObjectService;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.wsc.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.eludia.products.mosgis.rest.api.MgmtContractLocal;
import ru.gosuslugi.dom.schema.integration.house_management.ExportCAChResultType;
import ru.gosuslugi.dom.schema.integration.house_management.GetStateResult;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportHouseMgmtContractDataQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportMgmtContractDataMDB extends GisPollMDB {
    
    @EJB
    WsGisHouseManagementClient wsGisHouseManagementClient;
    
    @EJB
    MgmtContractLocal mgmtContract;
        
    @Resource (mappedName = "mosgis.outExportHouseMgmtContractFilesQueue")
    Queue outExportHouseMgmtContractFilesQueue;    
    
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
    
    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")                
            .toOne (ContractLog.class,     "AS log", "uuid", "id_ctr_status", "action", "uuid_user").on ("log.uuid_out_soap=root.uuid")
            .toOne (Contract.class,        "AS ctr", "uuid", "contractguid", "contractversionguid", "uuid_org").on ()
            .toOne (VocOrganization.class, "AS org", "orgppaguid"           ).on ("ctr.uuid_org")
        ;
        
    }    
    
    public void download (final UUID uuid) {
        UUIDPublisher.publish (outExportHouseMgmtContractFilesQueue, uuid);
    }

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        boolean isAnonymous = r.get ("log.uuid_user") == null;
        
        UUID orgPPAGuid          = (UUID) r.get ("org.orgppaguid");
        UUID ctrUuid             = (UUID) r.get ("ctr.uuid");
        UUID orgUuid             = (UUID) r.get ("ctr.uuid_org");
        UUID contractversionguid = (UUID) r.get ("ctr.contractversionguid");
        
        if (contractversionguid == null) {
            logger.warning ("Empty contractversionguid, bailing out");
            return;
        }
        
        String scontractversionguid = contractversionguid.toString ();
                
        try {
            
            GetStateResult state = getState (orgPPAGuid, r);        
        
            for (ExportCAChResultType er: state.getExportCAChResult ()) {

                ExportCAChResultType.Contract contract = er.getContract ();

                if (isWrong (contract, scontractversionguid)) continue;                                       

                db.update (Contract.class, HASH (
                    "uuid", ctrUuid,
                    "contractversionguid", null
                ));                

                db.begin ();
                
                    updateContract  (db, orgUuid, ctrUuid, contract, isAnonymous);

                db.commit ();

                break;

            }            
            
        }
        catch (GisPollRetryException ex) {
            return;
        }
        catch (GisPollException ex) {
            ex.register (db, uuid, r);
        }        

    }        

    private void updateContract (DB db, UUID orgUuid, UUID ctrUuid, ExportCAChResultType.Contract contract, boolean isAnonymous) throws SQLException {
        
        Model m = db.getModel ();
        
        AdditionalService.Sync adds = ((AdditionalService) m.get (AdditionalService.class)).new Sync (db, orgUuid);
        adds.reload ();
                
        ContractFile.Sync contractFiles = ((ContractFile) m.get (ContractFile.class)).new Sync (db, ctrUuid, this);
        contractFiles.addFrom (contract);
        contractFiles.sync ();

        ContractObject.Sync contractObjects = ((ContractObject) m.get (ContractObject.class)).new Sync (db, ctrUuid, contractFiles);
        contractObjects.addAll (contract.getContractObject ());
        contractObjects.sync ();
        
        final ContractObjectService srvTable = (ContractObjectService) m.get (ContractObjectService.class);
        ContractObjectService.SyncH contractObjectServicesH = (srvTable).new SyncH (db, ctrUuid, contractFiles);
        ContractObjectService.SyncA contractObjectServicesA = (srvTable).new SyncA (db, ctrUuid, contractFiles, adds);
        
        contract.getContractObject ().forEach ((co) -> {
            Map<String, Object> parent = HASH ("uuid_contract_object", contractObjects.getPk (co));
            contractObjectServicesH.addAll (co.getHouseService (), parent);
            contractObjectServicesA.addAll (co.getAddService (), parent);
        });
        
        contractObjectServicesH.sync ();
        contractObjectServicesA.sync ();
        
        VocGisStatus.i status = VocGisStatus.i.forName (contract.getContractStatus ().value ());
        
        final Map<String, Object> h = HASH (
            "id_ctr_status",       status.getId (),
            "id_ctr_status_gis",   status.getId (),
            "contractversionguid", contract.getContractVersionGUID ()
        );
        
        Contract.setDateFields (h, contract);
        if (!isAnonymous) Contract.setExtraFields (h, contract);
        
        h.put ("uuid", ctrUuid);
        db.update (Contract.class, h);
        
        h.put ("uuid", getUuid ());
        db.update (ContractLog.class, h);
        
        db.update (OutSoap.class, HASH (
            "uuid", getUuid (),
            "id_status", DONE.getId ()
        ));
        
    }

    private boolean isWrong (ExportCAChResultType.Contract contract, String scontractversionguid) {
        
        if (contract == null) {
            logger.warning ("Not a Contract? Bizarre, bizarre...");
            return true;
        }
        
        String v = contract.getContractVersionGUID ();
        
        if (v == null) {
            logger.warning ("Empty ContractVersionGUID? Bizarre, bizarre...");
            return true;
        }
        
        if (!v.equals (scontractversionguid)) {
            logger.warning ("We requested " + scontractversionguid + ". Why did they send back " + v + "?");
            return true;
        }
        
        return false;
        
    }
    
}
