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
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOverhaulWorkType;
import ru.eludia.products.mosgis.db.model.voc.VocOverhaulWorkTypeLog;
import ru.eludia.products.mosgis.db.model.voc.VocOverhaulWorkType.c;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisNsiClient;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.gosuslugi.dom.schema.integration.base.CommonResultType;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.nsi.GetStateResult;
import ru.gosuslugi.dom.schema.integration.nsi_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportOverhaulWorkTypesQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportOverhaulWorkTypeMDB extends GisPollMDB {
    
    @EJB
    WsGisNsiClient wsGisNsiClient;
    
    @Override
    protected Get get (UUID uuid) {
        
        final NsiTable nsi_218 = NsiTable.getNsiTable (218);
        
        return (Get) ModelHolder.getModel ()
                .get    (getTable (), uuid, "AS root", "*")
                .toOne  (VocOverhaulWorkTypeLog.class, "AS log", "*").on ("log.uuid_out_soap=root.uuid")
                .toOne  (nsi_218, nsi_218.getLabelField ().getfName () + " AS vc_nsi_218", "code", "guid").on ("(log.code_vc_nsi_218 = vc_nsi_218.code AND vc_nsi_218.isactual=1)");
        
    }
    
    @Override
    protected void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        UUID orgPPAGuid = (UUID) r.get ("orgppaguid");
        VocOverhaulWorkType.Action action = VocOverhaulWorkType.Action.forLogAction (VocAction.i.forName (r.get ("log.action").toString ()));
        
        try {
            
            GetStateResult state = getState (orgPPAGuid, r);
            
            ErrorMessageType errorMessage = state.getErrorMessage ();
            if (errorMessage != null) throw new GisPollException (errorMessage);
            
            List<CommonResultType> importResult = state.getImportResult();
            if (importResult == null || importResult.isEmpty ()) throw new GisPollException ("0", "Сервис ГИС ЖКХ вернул пустой результат");
            
            CommonResultType result = importResult.get (0);
            if (result == null) throw new GisPollException ("0", "Сервис ГИС ЖКХ вернул пустой результат");
            
            List<CommonResultType.Error> resultErrors = result.getError();
            if (!resultErrors.isEmpty()) throw new GisPollException (resultErrors.get(0));
            
            String uniqueNumber = result.getUniqueNumber ();
            String guid = result.getGUID ();
            
            if (uniqueNumber != null && guid != null) {
                db.update (VocOverhaulWorkType.class, HASH (
                    "uuid", r.get ("log.uuid_object"),
                    "uniquenumber", uniqueNumber,
                    "guid", guid
                ));
                db.update (VocOverhaulWorkTypeLog.class, HASH (
                    "uuid", r.get ("log.uuid"),
                    "uniquenumber", uniqueNumber,
                    "guid", guid
                ));
            }
            
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
        
        return HASH (c.ID_OWT_STATUS,     id,
                     c.ID_OWT_STATUS_GIS, id
        );
        
    }
    
    private void update (DB db, UUID uuid, Map<String, Object> r, Map<String, Object> h) throws SQLException {
        
logger.info ("h=" + h);
        
        h.put ("uuid", r.get ("log.uuid_object"));
        db.update (VocOverhaulWorkType.class, h);
        
        h.put ("uuid", uuid);
        db.update (VocOverhaulWorkTypeLog.class, h);
        
    }
    
    private GetStateResult getState (UUID orgPPAGuid, Map<String, Object> r) throws GisPollRetryException, GisPollException {

        GetStateResult rp;
        
        try {
            rp = wsGisNsiClient.getState (orgPPAGuid, (UUID) r.get ("uuid_ack"));
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
