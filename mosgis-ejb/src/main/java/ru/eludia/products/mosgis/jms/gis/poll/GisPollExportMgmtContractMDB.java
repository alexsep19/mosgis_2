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
import ru.eludia.products.mosgis.db.model.tables.ContractObject;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.wsc.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.eludia.products.mosgis.rest.api.MgmtContractLocal;
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
public class GisPollExportMgmtContractMDB extends GisPollMDB {

    @EJB
    WsGisHouseManagementClient wsGisHouseManagementClient;
        
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

        UUID orgPPAGuid   = (UUID) r.get ("org.orgppaguid");
        UUID uuidContract = (UUID) r.get ("ctr.uuid");
        
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

            GetStateResult rp = getState (orgPPAGuid, r);

            ImportContractResultType importContract = digImportContract (rp, action.getFailStatus ());            

            VocGisStatus.i state = VocGisStatus.i.forName (importContract.getState ());                
            if (state == null) state = VocGisStatus.i.NOT_RUNNING;

            final byte status = VocGisStatus.i.forName (importContract.getContractStatus ().value ()).getId ();

            db.begin ();

                for (ExportStatusCAChResultType.ContractObject co: importContract.getContractObject ()) {

                    db.update (ContractObject.class, HASH (

                        "uuid_contract",             uuidContract,
                        "fiashouseguid",             co.getFIASHouseGuid (),
                        "is_deleted",                0,

                        "id_ctr_status_gis",         VocGisStatus.i.forName (co.getManagedObjectStatus ().value ()).getId (),
                        "contractobjectversionguid", co.getContractObjectVersionGUID ()                  

                    ), "uuid_contract", "fiashouseguid", "is_deleted");

                }
            
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
                                                                    
            db.commit ();

            db.getConnection ().setAutoCommit (true);

            if (state == VocGisStatus.i.REVIEWED) mgmtContract.doPromote (uuidContract.toString ());
            
            if (VocAction.i.ROLLOVER.getName ().equals (r.get ("log.action"))) mgmtContract.doReload (r.get ("ctr.uuid").toString (), null);
            
        }
        catch (FU fu) {
            
            db.getConnection ().setAutoCommit (true);
            
            fu.register (db, uuid, r);
            
        }
        catch (GisPollRetryException ex) {
            return;
        }

    }

    private GetStateResult getState (UUID orgPPAGuid, Map<String, Object> r) throws GisPollRetryException, FU {
        
        GetStateResult rp;
        
        try {
            rp = wsGisHouseManagementClient.getState (orgPPAGuid, (UUID) r.get ("uuid_ack"));
        }
        catch (Fault ex) {
            throw new FU (ex.getFaultInfo (), VocGisStatus.i.FAILED_STATE);
        }
        catch (Throwable ex) {            
            throw new FU (ex, VocGisStatus.i.FAILED_STATE);
        }
        
        checkIfResponseReady (rp);
        
        return rp;
        
    }
        
    private class FU extends GisPollException {
        
        String code;
        String text;
        VocGisStatus.i status;

        FU (String code, String text, VocGisStatus.i status) {
            super (code, text);
            this.status = status;
        }
        
        FU (ErrorMessageType errorMessage, VocGisStatus.i status) {
            super (errorMessage);
            this.status = status;
        }

        FU (ru.gosuslugi.dom.schema.integration.base.Fault fault, VocGisStatus.i status) {
            super (fault);
            this.status = status;
        }
        
        FU (Throwable t, VocGisStatus.i status) {
            super (t);
            this.status = status;
        }
        
        @Override
        public void register (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
            
            super.register (db, uuid, r);

            db.update (Contract.class, HASH (
                "uuid",          r.get ("ctr.uuid"),
                "id_ctr_status", status.getId ()
            ));
            
        }
        
    }
    
}
