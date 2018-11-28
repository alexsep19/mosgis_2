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
import ru.eludia.products.mosgis.db.model.tables.Charter;
import ru.eludia.products.mosgis.db.model.tables.CharterPaymentLog;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.ejb.wsc.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.eludia.products.mosgis.rest.api.CharterPaymentLocal;
import ru.gosuslugi.dom.schema.integration.base.CommonResultType;
import ru.gosuslugi.dom.schema.integration.house_management.GetStateResult;
import ru.gosuslugi.dom.schema.integration.house_management.ImportResult;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;
import ru.eludia.products.mosgis.db.model.tables.CharterPayment.c;
import ru.eludia.products.mosgis.db.model.tables.CharterPayment;
import ru.eludia.products.mosgis.db.model.voc.VocAction;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportHouseCharterPaymentsQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportCharterPaymentMDB  extends GisPollMDB {

    @EJB
    WsGisHouseManagementClient wsGisHouseManagementClient;

    @Resource (mappedName = "mosgis.outExportHouseCharterPaymentsQueue")
    Queue queue;
    
    @EJB
    protected UUIDPublisher UUIDPublisher;
    
    @EJB
    CharterPaymentLocal back;

    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")                
            .toOne (CharterPaymentLog.class,     "AS log", "uuid", "id_ctr_status", "action").on ("log.uuid_out_soap=root.uuid")
            .toOne (CharterPayment.class,        "AS ctr", "uuid").on ()
            .toOne (Charter.class, "AS ctrt", "charterversionguid").on ()
            .toOne (VocOrganization.class, "AS org", "orgppaguid").on ("ctrt.uuid_org=org.uuid")
        ;
    }

    @Override
    protected void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        if (DB.ok (r.get ("is_failed"))) throw new IllegalStateException (r.get ("err_text").toString ());
        
        UUID orgPPAGuid          = (UUID) r.get ("org.orgppaguid");
                
        CharterPayment.Action action = CharterPayment.Action.forLogAction (VocAction.i.forName (r.get ("log.action").toString ()));

        try {
            
            GetStateResult state = getState (orgPPAGuid, r);
            
            final List<ImportResult> importResult = state.getImportResult ();
            
            if (importResult == null || importResult.isEmpty ()) throw new GisPollException ("0", "Сервис ГИС ЖКХ вернул пустой результат");
            
            final List<ImportResult.CommonResult> commonResult = importResult.get (0).getCommonResult ();
            
            if (commonResult == null || commonResult.isEmpty ()) throw new GisPollException ("0", "Сервис ГИС ЖКХ вернул пустой результат");
            
            for (CommonResultType.Error err: commonResult.get (0).getError ()) throw new GisPollException (err);
            
            final Map<String, Object> h = statusHash (action.getOkStatus ());

            final String guid = commonResult.get (0).getGUID ();
            if (DB.ok (guid)) h.put (c.VERSIONGUID.lc (), guid);
                        
            update (db, uuid, r, h);

            db.update (OutSoap.class, HASH (
                "uuid", getUuid (),
                "id_status", DONE.getId ()
            ));
            
        }
        catch (GisPollRetryException ex) {
            return;
        }
        catch (GisPollException ex) {
            update (db, uuid, r, statusHash (action.getFailStatus ()));
            ex.register (db, uuid, r);
        }
        
    }
    
    private static Map<String, Object> statusHash (VocGisStatus.i status) {
        
        final byte id = status.getId ();
        
        return HASH (
            c.ID_CTR_STATUS,     id,
            c.ID_CTR_STATUS_GIS, id
        );
        
    }

    private void update (DB db, UUID uuid, Map<String, Object> r, Map<String, Object> h) throws SQLException {
        
logger.info ("h=" + h);
        
        h.put ("uuid", r.get ("ctr.uuid"));
        db.update (CharterPayment.class, h);
        
        h.put ("uuid", uuid);
        db.update (CharterPaymentLog.class, h);
        
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
    
}
