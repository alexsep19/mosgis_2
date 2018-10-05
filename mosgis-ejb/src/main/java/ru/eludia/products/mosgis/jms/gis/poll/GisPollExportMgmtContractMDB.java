package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.ContractLog;
import ru.eludia.products.mosgis.db.model.tables.ContractObject;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.ejb.wsc.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.eludia.products.mosgis.rest.api.MgmtContractLocal;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.base.CommonResultType;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.house_management.ExportCAChResultType;
import ru.gosuslugi.dom.schema.integration.house_management.ExportStatusCAChResultType;
import ru.gosuslugi.dom.schema.integration.house_management.GetStateResult;
import ru.gosuslugi.dom.schema.integration.house_management.ImportContractResultType;
import ru.gosuslugi.dom.schema.integration.house_management.ImportResult;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportHouseMgmtContractsQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportMgmtContractMDB  extends UUIDMDB<OutSoap> {

    @EJB
    WsGisHouseManagementClient wsGisHouseManagementClient;

    @Resource (mappedName = "mosgis.outExportHouseMgmtContractsQueue")
    Queue queue;
    
    @EJB
    protected UUIDPublisher UUIDPublisher;
    
    @EJB
    MgmtContractLocal mgmtContract;

    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")                
            .toOne (ContractLog.class,     "AS log", "uuid", "id_ctr_status", "action").on ("log.uuid_out_soap=root.uuid")
            .toOne (Contract.class,        "AS ctr", "uuid", "contractguid" ).on ()
            .toOne (VocOrganization.class, "AS org", "orgppaguid"           ).on ("ctr.uuid_org")
        ;
        
    }
        
    private void assertLen1 (List l, String msg, VocGisStatus.i failStatus) throws FU {
        if (l == null || l.isEmpty ()) throw new FU ("0", msg + " вернулся пустой список", failStatus);
        int len = l.size ();
        if (len != 1) throw new FU ("0", msg + " вернулось " + len, failStatus);
    }
    
    private ImportContractResultType digImportContract (GetStateResult rp, VocGisStatus.i failStatus) throws FU {
        
        ErrorMessageType e1 = rp.getErrorMessage (); if (e1 != null) throw new FU (e1, failStatus);
            
        List<ImportResult> importResult = rp.getImportResult ();            
        assertLen1 (importResult, "Вместо 1 результата (importResult)", failStatus);
            
        ImportResult result = importResult.get (0);
            
        final ErrorMessageType e2 = result.getErrorMessage (); if (e2 != null) throw new FU (e2, failStatus);

        List<ImportResult.CommonResult> commonResult = result.getCommonResult ();
        assertLen1 (importResult, "Вместо 1 результата (commonResult)", failStatus);
        
        ImportResult.CommonResult icr = commonResult.get (0);
        List<CommonResultType.Error> e3 = icr.getError ();
        
        if (e3 != null && !e3.isEmpty ()) throw new FU (e3.get (0), failStatus);
                    
        ImportContractResultType importContract = icr.getImportContract ();
        if (importContract == null) throw new FU ("0", "Тип ответа не соответствует передаче договора управления", failStatus);
            
        ErrorMessageType e4 = importContract.getError (); if (e4 != null) throw new FU (e4, failStatus);

        return importContract;
        
    }

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {

        UUID orgPPAGuid = (UUID) r.get ("org.orgppaguid");
        
        final VocGisStatus.i lastStatus = VocGisStatus.i.forId (r.get ("log.id_ctr_status"));
        
        if (lastStatus == null) {
            logger.severe ("Cannot detect last status for " + r);
            return;
        }

        final Contract.Action action = Contract.Action.forStatus (lastStatus);

        if (action == null) {
            logger.severe ("Cannot detect expected action for " + r);
            return;
        }

        try {

            GetStateResult rp;

            try {
                rp = wsGisHouseManagementClient.getState (orgPPAGuid, (UUID) r.get ("uuid_ack"));
            }
            catch (Fault ex) {
                final ru.gosuslugi.dom.schema.integration.base.Fault faultInfo = ex.getFaultInfo ();
                throw new FU (faultInfo.getErrorCode (), faultInfo.getErrorMessage (), VocGisStatus.i.FAILED_STATE);
            }

            if (rp.getRequestState () < DONE.getId ()) {
                logger.info ("requestState = " + rp.getRequestState () + ", retry...");
                UUIDPublisher.publish (queue, uuid);
                return;
            }

            ImportContractResultType importContract = digImportContract (rp, action.getFailStatus ());            

            db.begin ();            
            
                final Table t = db.getModel ().t (ContractObject.class);
                                
                ContractObject contractObjectTableDefinition = (ContractObject) t;
                
                for (ExportStatusCAChResultType.ContractObject co: importContract.getContractObject ()) 
                    
                    db.d0 (contractObjectTableDefinition.updateStatus ((UUID) r.get ("ctr.uuid"), co));            

                VocGisStatus.i state = VocGisStatus.i.forName (importContract.getState ());                
                if (state == null) state = VocGisStatus.i.NOT_RUNNING;
                
                final byte status = VocGisStatus.i.forName (importContract.getContractStatus ().value ()).getId ();
            
                final UUID uuidContract = (UUID) r.get ("ctr.uuid");

                db.update (Contract.class, HASH (
                    "uuid",                uuidContract,
                    "rolltodate",          null,    
                    "contractguid",        importContract.getContractGUID (),
                    "contractversionguid", importContract.getContractVersionGUID (),
                    "versionnumber",       importContract.getVersionNumber (),
                    "id_ctr_status",       status,
                    "id_ctr_status_gis",   status,
                    "id_ctr_state_gis",    state.getId ()
                ));

                db.update (ContractLog.class, HASH (
                    "uuid",                r.get ("log.uuid"),
                    "versionnumber",       importContract.getVersionNumber (),
                    "contractversionguid", importContract.getContractVersionGUID ()
                ));

                db.update (OutSoap.class, HASH (
                    "uuid", uuid,
                    "id_status", DONE.getId ()
                ));
                
                if (state == VocGisStatus.i.REVIEWED) mgmtContract.doPromote (uuidContract.toString ());
                                                    
            db.commit ();
            
        }
        catch (FU fu) {
            fu.register (db, uuid, r);
        }

        if (VocAction.i.ROLLOVER.getName ().equals (r.get ("log.action"))) {
            
            db.getConnection ().setAutoCommit (true);

            updateDates (db, orgPPAGuid, (UUID) r.get ("ctr.uuid"), (UUID) r.get ("ctr.contractguid"));

        }

    }

    private void updateDates (DB db, UUID orgPPAGuid, UUID ctrUuid, UUID contractGUID) throws SQLException {
        
        UUID rUID = UUID.randomUUID ();

        AckRequest.Ack exportContractStatus = null;
        
        try {
            exportContractStatus = wsGisHouseManagementClient.exportContractStatus (orgPPAGuid, rUID, Collections.singletonList (contractGUID));
        }
        catch (Exception ex) {
            logger.log (Level.SEVERE, "wsGisHouseManagementClient.exportContractStatus", ex);
        }                
                
        UUID mGUID = UUID.fromString (exportContractStatus.getMessageGUID ());

        db.update (Contract.class, HASH (
            "uuid", ctrUuid,
            "contractversionguid", null
        ));

        for (int i = 0; i < 10; i ++) {
            
            try {
                
                Thread.sleep (2000L);
                
                GetStateResult state = null;
                
                try {
                    state = wsGisHouseManagementClient.getState (orgPPAGuid, mGUID);
                }
                catch (Exception ex) {
                    logger.log (Level.SEVERE, "wsGisHouseManagementClient.getState", ex);
                }
                
                if (state.getRequestState () < 2) continue;
                
                GisPollExportMgmtContractStatusMDB.processGetStateResponse (state, db, rUID, true);
                
                break;
                                
            }
            catch (InterruptedException ex) {
                logger.log (Level.WARNING, "It's futile", ex);
            }
            
        }  
        
        UUID contractversionguid = DB.to.UUIDFromHex (db.getString (Contract.class, ctrUuid, "contractversionguid"));
        
        String scontractversionguid = contractversionguid.toString ();
        

        
        
        rUID = UUID.randomUUID ();        

        try {
            exportContractStatus = wsGisHouseManagementClient.exportContractData (orgPPAGuid, rUID, Collections.singletonList (contractversionguid));
        }
        catch (Exception ex) {
            logger.log (Level.SEVERE, "wsGisHouseManagementClient.exportContractStatus", ex);
        }                
                
        mGUID = UUID.fromString (exportContractStatus.getMessageGUID ());

        db.update (Contract.class, HASH (
            "uuid", ctrUuid,
            "contractversionguid", null
        ));

        for (int i = 0; i < 10; i ++) {
            
            try {
                
                Thread.sleep (2000L);
                
                GetStateResult state = null;
                
                try {
                    state = wsGisHouseManagementClient.getState (orgPPAGuid, mGUID);
                }
                catch (Exception ex) {
                    logger.log (Level.SEVERE, "wsGisHouseManagementClient.getState", ex);
                }
                
                if (state.getRequestState () < 2) continue;
                
                List<Map<String, Object>> objectRecords = new ArrayList ();
                                
                for (ExportCAChResultType er: state.getExportCAChResult ()) {
                    
                    ExportCAChResultType.Contract contract = er.getContract ();
                    
                    if (contract == null) {
                        logger.warning ("Not a Contract? Bizarre, bizarre...");
                        continue;
                    }                                       
                    
                    String v = contract.getContractVersionGUID ();
                    
                    if (v == null) {
                        logger.warning ("Empty ContractVersionGUID? Bizarre, bizarre...");
                        continue;
                    }                                       
                    
                    if (!v.equals (scontractversionguid)) {
                        logger.warning ("We requested " + scontractversionguid + ". Why did they send back " + v + "?");
                        continue;
                    }                                       
                                        
                    for (ExportCAChResultType.Contract.ContractObject co: contract.getContractObject ()) {

                        final Map<String, Object> or = HASH (
                            "uuid_contract",             ctrUuid,
                            "fiashouseguid",             co.getFIASHouseGuid (),
                            "startdate",                 co.getStartDate (),
                            "enddate",                   co.getEndDate ()
                        );

                        objectRecords.add (or);

                    }            
                    
                    db.begin ();
                    
                        db.upsert (ContractObject.class, objectRecords, objectKey);

                        db.update (Contract.class, HASH (
                            "uuid",                ctrUuid,
                            "contractversionguid", scontractversionguid,
                            "signingdate",         contract.getSigningDate (),      
                            "effectivedate",       contract.getEffectiveDate (),      
                            "plandatecomptetion",  contract.getPlanDateComptetion ()
                        ));
                    
                    db.commit ();
                    
                    break;
                    
                }
                
                break;
                                
            }
            catch (InterruptedException ex) {
                logger.log (Level.WARNING, "It's futile", ex);
            }
            
        }  
        
    }    
    
    private static String [] objectKey   = {"uuid_contract", "fiashouseguid"};
    
    private class FU extends Exception {
        
        String code;
        String text;
        VocGisStatus.i status;

        FU (String code, String text, VocGisStatus.i status) {
            super (code + " " + text);
            this.code = code;
            this.text = text;
            this.status = status;
        }
        
        FU (ErrorMessageType errorMessage, VocGisStatus.i status) {
            this (errorMessage.getErrorCode (), errorMessage.getDescription (), status);
        }
        
        private void register (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
            
            logger.warning (getMessage ());
            
            db.update (OutSoap.class, HASH (
                "uuid", uuid,
                "id_status", DONE.getId (),
                "is_failed", 1,
                "err_code",  code,
                "err_text",  text
            ));

            db.update (Contract.class, HASH (
                "uuid",          r.get ("ctr.uuid"),
                "id_ctr_status", status.getId ()
            ));

            db.commit ();            
            
        }
        
    }
    
}
