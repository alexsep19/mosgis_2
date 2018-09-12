package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.ContractLog;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.wsc.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
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

    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")                
            .toOne (ContractLog.class, "AS log").on ("log.uuid_out_soap=root.uuid")
            .toOne (Contract.class, "AS ctr").on ()
            .toOne (VocOrganization.class, "AS org", "orgppaguid").on ("ctr.uuid_org")                
        ;
        
    }
        
    private void assertLen1 (List l, String msg) throws FU {
        if (l == null || l.isEmpty ()) throw new FU ("0", msg + " вернулся пустой список");
        int len = l.size ();
        if (len != 1) throw new FU ("0", msg + " вернулось " + len);
    }
    
    private ImportContractResultType digImportContract (GetStateResult rp) throws FU {
        
        ErrorMessageType e1 = rp.getErrorMessage (); if (e1 != null) throw new FU (e1);
            
        List<ImportResult> importResult = rp.getImportResult ();            
        assertLen1 (importResult, "Вместо 1 результата (importResult)");
            
        ImportResult result = importResult.get (0);
            
        final ErrorMessageType e2 = result.getErrorMessage (); if (e2 != null) throw new FU (e2);

        List<ImportResult.CommonResult> commonResult = result.getCommonResult ();
        assertLen1 (importResult, "Вместо 1 результата (commonResult)");
            
        ImportContractResultType importContract = commonResult.get (0).getImportContract ();
        if (importContract == null) throw new FU ("0", "Тип ответа не соответствует передаче договора управления");
            
        ErrorMessageType e3 = importContract.getError (); if (e3 != null) throw new FU (e3);
        
        return importContract;
        
    }

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        final Object uuidObject = r.get ("log.uuid_object");
        
        UUID orgPPAGuid = (UUID) r.get ("org.orgppaguid");        
        
        try {
                       
            GetStateResult rp;
            
            try {
                rp = wsGisHouseManagementClient.getState (orgPPAGuid, (UUID) r.get ("uuid_ack"));
            }
            catch (Fault ex) {
                final ru.gosuslugi.dom.schema.integration.base.Fault faultInfo = ex.getFaultInfo ();
                throw new FU (faultInfo.getErrorCode (), faultInfo.getErrorMessage ());
            }

            ImportContractResultType importContract = digImportContract (rp);
                                    
            db.begin ();            
            
                db.update (Contract.class, HASH (
                    "uuid", uuidObject,
                    "id_ctr_status_gis", VocGisStatus.i.forName (importContract.getContractStatus ().value ()),
                    "id_ctr_state_gis",  VocGisStatus.i.forName (importContract.getState ())
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

        FU (String code, String text) {
            super (code + " " + text);
            this.code = code;
            this.text = text;
        }
        
        FU (ErrorMessageType errorMessage) {
            this (errorMessage.getErrorCode (), errorMessage.getDescription ());
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
/*
            db.update (Contract.class, HASH (
                "uuid",         uuidObject,
                "id_status",    FAIL.getId ()
            ));
*/
            db.commit ();            
            
        }
        
    }
    
}
