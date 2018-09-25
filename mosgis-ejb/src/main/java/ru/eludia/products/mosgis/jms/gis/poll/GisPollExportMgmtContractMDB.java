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
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.ContractLog;
import ru.eludia.products.mosgis.db.model.tables.ContractObject;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.ejb.wsc.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.gosuslugi.dom.schema.integration.base.CommonResultType;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
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
    
    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")                
            .toOne (ContractLog.class,     "AS log", "uuid"      ).on ("log.uuid_out_soap=root.uuid")
            .toOne (Contract.class,        "AS ctr", "uuid"      ).on ()
            .toOne (VocOrganization.class, "AS org", "orgppaguid").on ("ctr.uuid_org")
        ;
        
    }
        
    private void assertLen1 (List l, String msg, VocGisStatus.i status) throws FU {
        if (l == null || l.isEmpty ()) throw new FU ("0", msg + " вернулся пустой список", status);
        int len = l.size ();
        if (len != 1) throw new FU ("0", msg + " вернулось " + len, status);
    }
    
    private ImportContractResultType digImportContract (GetStateResult rp) throws FU {
        
        ErrorMessageType e1 = rp.getErrorMessage (); if (e1 != null) throw new FU (e1, VocGisStatus.i.FAILED_PLACING);
            
        List<ImportResult> importResult = rp.getImportResult ();            
        assertLen1 (importResult, "Вместо 1 результата (importResult)", VocGisStatus.i.FAILED_PLACING);
            
        ImportResult result = importResult.get (0);
            
        final ErrorMessageType e2 = result.getErrorMessage (); if (e2 != null) throw new FU (e2, VocGisStatus.i.FAILED_PLACING);

        List<ImportResult.CommonResult> commonResult = result.getCommonResult ();
        assertLen1 (importResult, "Вместо 1 результата (commonResult)", VocGisStatus.i.FAILED_PLACING);
        
        ImportResult.CommonResult icr = commonResult.get (0);
        List<CommonResultType.Error> e3 = icr.getError ();
        
        if (e3 != null && !e3.isEmpty ()) throw new FU (e3.get (0), VocGisStatus.i.FAILED_PLACING);
            
        ImportContractResultType importContract = icr.getImportContract ();
        if (importContract == null) throw new FU ("0", "Тип ответа не соответствует передаче договора управления", VocGisStatus.i.FAILED_PLACING);
            
        ErrorMessageType e4 = importContract.getError (); if (e4 != null) throw new FU (e4, VocGisStatus.i.FAILED_PLACING);
        
        return importContract;
        
    }

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
                
        UUID orgPPAGuid = (UUID) r.get ("org.orgppaguid");        
        
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

            ImportContractResultType importContract = digImportContract (rp);            

            db.begin ();            
            
                final Table t = db.getModel ().t (ContractObject.class);
                                
                ContractObject contractObjectTableDefinition = (ContractObject) t;
                
                for (ExportStatusCAChResultType.ContractObject co: importContract.getContractObject ()) 
                    
                    db.d0 (contractObjectTableDefinition.updateStatus ((UUID) r.get ("ctr.uuid"), co));            

                VocGisStatus.i state = VocGisStatus.i.forName (importContract.getState ());                
                if (state == null) state = VocGisStatus.i.NOT_RUNNING;
                
                final byte status = VocGisStatus.i.forName (importContract.getContractStatus ().value ()).getId ();

                db.update (Contract.class, HASH ("uuid", r.get ("ctr.uuid"),
                    "contractguid", importContract.getContractGUID (),
                    "id_ctr_status", status,
                    "id_ctr_status_gis", status,
                    "id_ctr_state_gis",  state.getId ()
                ));

                db.update (ContractLog.class, HASH (
                    "uuid", r.get ("log.uuid"),
                    "contractversionguid", importContract.getContractVersionGUID ()
                ));

                db.update (OutSoap.class, HASH (
                    "uuid", uuid,
                    "id_status", DONE.getId ()
                ));
                                                    
            db.commit ();
            
        }
        catch (FU fu) {
            fu.register (db, uuid, r);
        }
        
    }
    
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
