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
import ru.eludia.products.mosgis.db.model.tables.MeteringDevice;
import ru.eludia.products.mosgis.db.model.tables.MeteringDeviceValue;
import ru.eludia.products.mosgis.db.model.tables.MeteringDeviceValueLog;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.gosuslugi.dom.schema.integration.base.CommonResultType;
import ru.gosuslugi.dom.schema.integration.device_metering_service_async.Fault;
import ru.eludia.products.mosgis.db.model.tables.MeteringDeviceValue.c;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisMeteringClient;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.device_metering.GetStateResult;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportMeteringDeviceValuesQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportMeteringDeviceValueMDB  extends GisPollMDB {

    @EJB
    WsGisMeteringClient wsGisMeteringClient;

    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")                
            .toOne (MeteringDeviceValueLog.class,     "AS log", "uuid", "action", "id_ctr_status").on ("log.uuid_out_soap=root.uuid")
            .toOne (MeteringDeviceValue.class,        "AS r", "uuid").on ()
            .toOne (MeteringDevice.class,             "AS ctr", "uuid").on ()
            .toOne (VocOrganization.class,            "AS org", "orgppaguid").on ("ctr.uuid_org=org.uuid")
        ;
    }

    @Override
    protected void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        if (DB.ok (r.get ("is_failed"))) throw new IllegalStateException (r.get ("err_text").toString ());
        
        UUID orgPPAGuid          = (UUID) r.get ("org.orgppaguid");
        
        MeteringDeviceValue.Action action = MeteringDeviceValue.Action.forStatus (VocGisStatus.i.forId (r.get ("log.id_ctr_status")));
                
        try {
            
            GetStateResult state = getState (orgPPAGuid, r);
            
            ErrorMessageType errorMessage = state.getErrorMessage ();
            
            if (errorMessage != null) throw new GisPollException (errorMessage);
            
            List<CommonResultType> importResult = state.getImportResult ();
            
            if (importResult == null || importResult.isEmpty ()) throw new GisPollException ("0", "Сервис ГИС ЖКХ вернул пустой результат");
            
            CommonResultType cr = importResult.get (0);
            
            final Map<String, Object> h = statusHash (action.getOkStatus ());
            h.put (c.GIS_GUID.lc (), cr.getGUID ());
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
        
        h.put ("uuid", r.get ("r.uuid"));
        db.update (MeteringDeviceValue.class, h);
        
        h.put ("uuid", uuid);
        db.update (MeteringDeviceValueLog.class, h);
        
    }
    
    private GetStateResult getState (UUID orgPPAGuid, Map<String, Object> r) throws GisPollRetryException, GisPollException {

        GetStateResult rp;
        
        try {
            rp = wsGisMeteringClient.getState (orgPPAGuid, (UUID) r.get ("uuid_ack"));
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