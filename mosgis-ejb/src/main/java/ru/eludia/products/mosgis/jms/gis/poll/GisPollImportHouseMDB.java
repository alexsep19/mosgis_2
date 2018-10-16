package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
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
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.ejb.wsc.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.eludia.products.mosgis.rest.api.MgmtContractLocal;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.house_management.GetStateResult;
import ru.gosuslugi.dom.schema.integration.house_management.ImportResult;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outImportHouseQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollImportHouseMDB  extends UUIDMDB<OutSoap> {

    @EJB
    private WsGisHouseManagementClient wsGisHouseManagementClient;
    
    @EJB
    private UUIDPublisher UUIDPublisher;
    
    @EJB
    private MgmtContractLocal mgmtContract;
    
    @Resource (mappedName = "mosgis.outImportHouseQueue")
    private Queue queue;
    
    @Override
    protected Get get(UUID uuid) {
        return (Get) ModelHolder.getModel().get(getTable(), uuid, "*");
    }

    @Override
    protected void handleRecord(DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        UUID orgPPAGuid = (UUID) r.get("orgppaguid");

        try {
            GetStateResult rp = wsGisHouseManagementClient.getState(orgPPAGuid, (UUID) r.get("uuid_ack"));
            if (rp.getRequestState() < DONE.getId()) {
                logger.log(Level.INFO, "requestState = {0}, retry...", rp.getRequestState());
                UUIDPublisher.publish(queue, uuid);
                return;
            }
            ErrorMessageType errorMessage = rp.getErrorMessage();
            if ((rp.getErrorMessage()) != null) {
                logger.log(Level.SEVERE, "{0} {1}", new Object[]{errorMessage.getErrorCode(), errorMessage.getDescription()});
                return;
            }
            
            List<ImportResult> importResult = rp.getImportResult();
            logger.log(Level.INFO, importResult.toString());
        } catch (Fault ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
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
