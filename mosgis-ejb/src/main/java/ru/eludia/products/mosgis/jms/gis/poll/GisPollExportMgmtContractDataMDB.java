package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
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
            .toOne (Contract.class,        "AS ctr", "uuid", "contractguid", "contractversionguid").on ()
            .toOne (VocOrganization.class, "AS org", "orgppaguid"           ).on ("ctr.uuid_org")
        ;
        
    }    
    
    private void refecthContractFiles (DB db, UUID ctrUuid, Map<UUID, Map<String, Object>> attachmentguid2file, List<UUID> uuidsToDelete, List<UUID> uuidsToDownload) throws SQLException {
        
        db.forEach (db.getModel ()
                
            .select (ContractFile.class, "attachmentguid", "uuid", "uuid_contract_object", ATTACHMENTHASH)
                .and ("uuid_contract", ctrUuid)
                .and ("id_status", 1)
                .and ("attachmentguid IS NOT NULL")

            , (rs) -> {

                Map<String, Object> asIs = db.HASH (rs);

                Map<String, Object> toBe = attachmentguid2file.get (asIs.get ("attachmentguid"));
                
                final UUID uuid = (UUID) asIs.get ("uuid");
                
                if (toBe == null) {
                    uuidsToDelete.add (uuid);
                }
                else {
                    
                    Object hashAsIs = asIs.get (ATTACHMENTHASH);
                    Object hashToBe = toBe.get (ATTACHMENTHASH).toString ().toUpperCase ();
                    
                    if (!DB.eq (hashAsIs, hashToBe)) {
                        logger.info ("Scheduling download for " + uuid + ": " + hashToBe + " <> " + hashAsIs);
                        uuidsToDownload.add (uuid);
                    }
                    
                    toBe.putAll (asIs);
                    
                }

            }
                
        );
        
    }    
    private static final String ATTACHMENTHASH = "attachmenthash";

    private void download (final UUID uuid) {
        UUIDPublisher.publish (outExportHouseMgmtContractFilesQueue, uuid);
    }

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        boolean isAnonymous = r.get ("log.uuid_user") == null;
        
        UUID orgPPAGuid          = (UUID) r.get ("org.orgppaguid");
        UUID ctrUuid             = (UUID) r.get ("ctr.uuid");
        UUID contractversionguid = (UUID) r.get ("ctr.contractversionguid");
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
                
                    updateContract  (db, ctrUuid, contract, isAnonymous);

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

    private void updateContract (DB db, UUID ctrUuid, ExportCAChResultType.Contract contract, boolean isAnonymous) throws SQLException {
        
        Map<Object, Map<String, Object>> uuid2contractObject = new HashMap <> ();
        Map<String, Map<String, Object>> fias2contractObject = new HashMap <> ();
        
        fetchObjects  (db, ctrUuid, uuid2contractObject, fias2contractObject);
        fetchServices (db, ctrUuid, uuid2contractObject);
        
        logger.info ("uuid2contractObject = " + uuid2contractObject);
        
        List<Map<String, Object>> objectRecords = new ArrayList ();
        for (ExportCAChResultType.Contract.ContractObject co: contract.getContractObject ()) addObject   (fias2contractObject, co, objectRecords);
        db.update (ContractObject.class, objectRecords);
        
        List<Map<String, Object>> serviceRecords = new ArrayList ();
        for (ExportCAChResultType.Contract.ContractObject co: contract.getContractObject ()) addServices (fias2contractObject, co, serviceRecords);
        db.update (ContractObjectService.class, serviceRecords);
        
        if (!isAnonymous) {
            
            Map<UUID, Map<String, Object>> attachmentguid2file = ContractFile.toHashes (contract, HASH (
                "uuid_contract", ctrUuid
            ));
            
            logger.info ("attachmentguid2file = " + attachmentguid2file);
            
            db.upsert (ContractFile.class, attachmentguid2file.entrySet ().stream ().map ((t) -> t.getValue ()), "attachmentguid");
            
            List<UUID> uuidsToDelete = new ArrayList<> ();
            List<UUID> uuidsToDownload = new ArrayList<> ();
            
            refecthContractFiles (db, ctrUuid, attachmentguid2file, uuidsToDelete, uuidsToDownload);
            
            if (!uuidsToDelete.isEmpty ()) db.update (ContractFile.class, uuidsToDelete.stream ().map (id -> HASH (
                "uuid",      id,
                "id_status", 2
            )).collect (Collectors.toList ()));
            
            uuidsToDownload.forEach (this::download);
            
            logger.info ("attachmentguid2file = " + attachmentguid2file);
            
        }
        
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

    private void addObject (Map<String, Map<String, Object>> fias2contractObject, ExportCAChResultType.Contract.ContractObject co, List<Map<String, Object>> objectRecords) {
        
        final Map<String, Object> h = HASH (
            "uuid", ContractObject.getByKey (fias2contractObject, co).get ("uuid").toString ()
        );
        
        ContractObject.setDateFields (h, co);
                        
        objectRecords.add (h);
        
    }
    
    private void addServices (Map<String, Map<String, Object>> fias2contractObject, ExportCAChResultType.Contract.ContractObject co, List<Map<String, Object>> serviceRecords) {
        
        Map<String, Object> contract_object = ContractObject.getByKey (fias2contractObject, co);
                                        
        for (ExportCAChResultType.Contract.ContractObject.HouseService hs: co.getHouseService ()) {
            
            final Map<String, Object> h = HASH (
                "uuid", ((Map<String, String>) contract_object.get ("nsi2uuid")).get (hs.getServiceType ().getCode ())
            );
            
            ContractObjectService.setDateFields (h, hs);

            serviceRecords.add (h);            
            
        }
        
        for (ExportCAChResultType.Contract.ContractObject.AddService as: co.getAddService ()) {

            final Map<String, Object> h = HASH (
                "uuid", ((Map<String, String>) contract_object.get ("un2uuid")).get (as.getServiceType ().getCode ())
            );

            ContractObjectService.setDateFields (h, as);

            serviceRecords.add (h);

        }

    }

    private void fetchServices (DB db, UUID ctrUuid, Map<Object, Map<String, Object>> uuid2contractObject) throws SQLException {
        
        db.forEach (db.getModel ()
                
        .select (ContractObjectService.class, "uuid", "uuid_contract_object", "code_vc_nsi_3", "is_additional")
        .where ("uuid_contract", ctrUuid)
        .toMaybeOne (AdditionalService.class, "AS a", "uniquenumber").on ()

        , (rs) -> {

            DB.ResultGet rg = new DB.ResultGet (rs);

            final String uuid_contract_object = rg.getUUIDString ("uuid_contract_object");

            Map<String, Object> contract_object = uuid2contractObject.get (uuid_contract_object);

            if (contract_object == null) {
                logger.warning ("contract_object not found: " + uuid_contract_object);
                return;
            }

            if (rs.getInt ("is_additional") == 1) {

                ((Map<String, String>) contract_object.get ("un2uuid")).put (
                    rs.getString ("a.uniquenumber"),
                    rg.getUUIDString ("uuid")
                );

            }
            else {

                ((Map<String, String>) contract_object.get ("nsi2uuid")).put (
                    rs.getString ("code_vc_nsi_3"),
                    rg.getUUIDString ("uuid")
                );

            }

        });
        
    }

    private void fetchObjects (DB db, UUID ctrUuid, Map<Object, Map<String, Object>> uuid2contractObject, Map<String, Map<String, Object>> fias2contractObject) throws SQLException {
        
        db.forEach (db.getModel ()
                
            .select (ContractObject.class, "*")
            .where ("uuid_contract", ctrUuid),

            (rs) -> {

                Map<String, Object> i = db.HASH (rs);

                i.put ("nsi2uuid", HASH ());
                i.put ("un2uuid", HASH ());

                uuid2contractObject.put (i.get ("uuid").toString (), i);
                fias2contractObject.put (i.get ("fias_start").toString (), i);

            }
                
        );
    }
    
}
