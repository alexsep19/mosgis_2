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
import ru.eludia.products.mosgis.db.model.tables.Charter;
import ru.eludia.products.mosgis.db.model.tables.CharterLog;
import ru.eludia.products.mosgis.db.model.tables.CharterObject;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.ejb.wsc.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.eludia.products.mosgis.rest.api.CharterLocal;
import ru.gosuslugi.dom.schema.integration.base.CommonResultType;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.house_management.ExportStatusCAChResultType;
import ru.gosuslugi.dom.schema.integration.house_management.GetStateResult;
import ru.gosuslugi.dom.schema.integration.house_management.ImportCharterResultType;
import ru.gosuslugi.dom.schema.integration.house_management.ImportResult;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportHouseChartersQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportCharterMDB  extends UUIDMDB<OutSoap> {

    @EJB
    WsGisHouseManagementClient wsGisHouseManagementClient;

    @Resource (mappedName = "mosgis.outExportHouseChartersQueue")
    Queue queue;
    
    @EJB
    protected UUIDPublisher UUIDPublisher;
    
    @EJB
    CharterLocal charter;

    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")                
            .toOne (CharterLog.class,     "AS log", "uuid", "id_ctr_status", "action").on ("log.uuid_out_soap=root.uuid")
            .toOne (Charter.class,        "AS ctr", "uuid", "charterguid" ).on ()
            .toOne (VocOrganization.class, "AS org", "orgppaguid"         ).on ("ctr.uuid_org=org.uuid")
        ;
        
    }
        
    private void assertLen1 (List l, String msg, VocGisStatus.i failStatus) throws FU {
        if (l == null || l.isEmpty ()) throw new FU ("0", msg + " вернулся пустой список", failStatus);
        int len = l.size ();
        if (len != 1) throw new FU ("0", msg + " вернулось " + len, failStatus);
    }
    
    private ImportCharterResultType digImportCharter (GetStateResult rp, VocGisStatus.i failStatus) throws FU {
        
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
                    
        ImportCharterResultType importCharter = icr.getImportCharter ();
        if (importCharter == null) throw new FU ("0", "Тип ответа не соответствует передаче договора управления", failStatus);
            
        ErrorMessageType e4 = importCharter.getError (); if (e4 != null) throw new FU (e4, failStatus);

        return importCharter;
        
    }

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {

        UUID orgPPAGuid = (UUID) r.get ("org.orgppaguid");
        
        final VocGisStatus.i lastStatus = VocGisStatus.i.forId (r.get ("log.id_ctr_status"));
        
        if (lastStatus == null) {
            logger.severe ("Cannot detect last status for " + r);
            return;
        }

        final Charter.Action action = Charter.Action.forStatus (lastStatus);

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

            ImportCharterResultType importCharter = digImportCharter (rp, action.getFailStatus ());            

            db.begin ();            
            
                final Table t = db.getModel ().t (CharterObject.class);                               

                VocGisStatus.i state = VocGisStatus.i.forName (importCharter.getState ());                
                if (state == null) state = VocGisStatus.i.NOT_RUNNING;
                
                final byte status = VocGisStatus.i.forName (importCharter.getCharterStatus ().value ()).getId ();
            
                final UUID uuidCharter = (UUID) r.get ("ctr.uuid");
                
                for (ExportStatusCAChResultType.ContractObject co: importCharter.getContractObject ()) {

                    db.update (CharterObject.class, HASH (

                        "uuid_charter",              uuidCharter,
                        "fiashouseguid",             co.getFIASHouseGuid (),
                        "is_deleted",                0,

                        "id_ctr_status_gis",         VocGisStatus.i.forName (co.getManagedObjectStatus ().value ()).getId (),
                        "contractobjectversionguid", co.getContractObjectVersionGUID ()                  

                    ), "uuid_charter", "fiashouseguid", "is_deleted");

                }                

                db.update (Charter.class, HASH (
                    "uuid",                uuidCharter,
                    "rolltodate",          null,    
                    "charterguid",        importCharter.getCharterGUID (),
                    "charterversionguid", importCharter.getCharterVersionGUID (),
                    "versionnumber",       importCharter.getVersionNumber (),
                    "id_ctr_status",       status,
                    "id_ctr_status_gis",   status,
                    "id_ctr_state_gis",    state.getId ()
                ));

                db.update (CharterLog.class, HASH (
                    "uuid",                r.get ("log.uuid"),
                    "versionnumber",       importCharter.getVersionNumber (),
                    "charterversionguid", importCharter.getCharterVersionGUID ()
                ));

                db.update (OutSoap.class, HASH (
                    "uuid", uuid,
                    "id_status", DONE.getId ()
                ));
                
                if (state == VocGisStatus.i.REVIEWED) charter.doPromote (uuidCharter.toString ());
                                                    
            db.commit ();
            
            db.getConnection ().setAutoCommit (true);
            
            switch (VocAction.i.forName (r.get ("log.action").toString ())) {
                case ROLLOVER:
                case TERMINATE:
                    charter.doReload (r.get ("ctr.uuid").toString (), null);
                default:
                    // do nothing
            }            
            
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

            db.update (Charter.class, HASH (
                "uuid",          r.get ("ctr.uuid"),
                "id_ctr_status", status.getId ()
            ));

            db.commit ();            
            
        }
        
    }
    
}
