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
import ru.eludia.products.mosgis.db.model.tables.Infrastructure;
import ru.eludia.products.mosgis.db.model.tables.InfrastructureLog;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.gosuslugi.dom.schema.integration.infrastructure_service_async.Fault;
import ru.eludia.products.mosgis.db.model.tables.Infrastructure.c;
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi33;
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi38;
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi40;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import static ru.eludia.products.mosgis.db.ModelHolder.getModel;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisInfrastructureClient;
import ru.gosuslugi.dom.schema.integration.base.CommonResultType;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.infrastructure.ExportOKIResultType;
import ru.gosuslugi.dom.schema.integration.infrastructure.GetStateResult;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportInfrastructuresQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollImportInfrastructureMDB  extends GisPollMDB {

    @EJB
    WsGisInfrastructureClient wsGisInfrastructureClient;

    @Override
    protected Get get (UUID uuid) {
        
        final VocNsi33 nsi_33 = (VocNsi33) getModel ().get (VocNsi33.class);
        final NsiTable nsi_34 = NsiTable.getNsiTable (34);
        final NsiTable nsi_35 = NsiTable.getNsiTable (35);
        final NsiTable nsi_37 = NsiTable.getNsiTable (37);
        final VocNsi38 nsi_38 = (VocNsi38) getModel ().get (VocNsi38.class);
        final NsiTable nsi_39 = NsiTable.getNsiTable (39);
        final VocNsi40 nsi_40 = (VocNsi40) getModel ().get (VocNsi40.class);
        
        return (Get) getModel ().get (getTable (), uuid, "AS root", "*")                
                
            .toOne (InfrastructureLog.class,     "AS log", "uuid", "action").on ("log.uuid_out_soap=root.uuid")
            .toOne (Infrastructure.class,        "AS r", "uuid").on ()

            .toOne      (VocOrganization.class, "AS org", "orgppaguid").on ()
            .toOne      (nsi_33, "label AS vc_nsi_33", "code", "guid").on ("(r.code_vc_nsi_33 = vc_nsi_33.code AND vc_nsi_33.isactual=1)")
            .toOne      (nsi_39, nsi_39.getLabelField ().getfName () + " AS vc_nsi_39", "code", "guid").on ("(r.code_vc_nsi_39 = vc_nsi_39.code AND vc_nsi_39.isactual=1)")
            .toMaybeOne (nsi_37, nsi_37.getLabelField ().getfName () + " AS vc_nsi_37", "code", "guid").on ("(r.code_vc_nsi_37 = vc_nsi_37.code AND vc_nsi_37.isactual=1)")
            .toMaybeOne (nsi_38, "label AS vc_nsi_38", "code", "guid").on ("(r.code_vc_nsi_38 = vc_nsi_38.code AND vc_nsi_38.isactual=1)")
            .toMaybeOne (nsi_34, nsi_34.getLabelField ().getfName () + " AS vc_nsi_34", "code", "guid").on ("(r.code_vc_nsi_34 = vc_nsi_34.code AND vc_nsi_34.isactual=1)")
            .toMaybeOne (nsi_40, "label AS vc_nsi_40", "code", "guid").on ("(r.code_vc_nsi_40 = vc_nsi_40.code AND vc_nsi_40.isactual=1)")
            .toMaybeOne (nsi_35, nsi_35.getLabelField ().getfName () + " AS vc_nsi_35", "code", "guid").on ("(r.code_vc_nsi_35 = vc_nsi_35.code AND vc_nsi_35.isactual=1)")
        ;
    }

    @Override
    protected void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        if (DB.ok (r.get ("is_failed"))) throw new IllegalStateException (r.get ("err_text").toString ());
        
        UUID orgPPAGuid = (UUID) r.get ("orgppaguid");
        
        Infrastructure.Action action = Infrastructure.Action.forLogAction (VocAction.i.forName (r.get ("log.action").toString ()));
                
        try {
            
            GetStateResult state = getState (orgPPAGuid, r);
            
            List<ErrorMessageType> errorMessages = state.getErrorMessage ();
            
            if (!errorMessages.isEmpty()) throw new GisPollException (errorMessages.get(0));
            
            List<CommonResultType> importResult = state.getImportResult();
            
            if (importResult == null || importResult.isEmpty ()) throw new GisPollException ("0", "Сервис ГИС ЖКХ вернул пустой результат");
            
            CommonResultType result = importResult.get (0);

            if (result == null) throw new GisPollException ("0", "Сервис ГИС ЖКХ вернул пустой результат");
            
            List<CommonResultType.Error> resultErrors = result.getError();
            
            if (!resultErrors.isEmpty()) throw new GisPollException (resultErrors.get(0));
            
            String uniqueNumber = result.getUniqueNumber ();
            String OKIGuid = result.getGUID ();

            if (uniqueNumber != null && OKIGuid != null) {
                db.update (Infrastructure.class, HASH (
                    "uuid", r.get ("r.uuid"),
                    "uniquenumber", uniqueNumber,
                    "okiguid", OKIGuid
                ));
                db.update (InfrastructureLog.class, HASH (
                    "uuid", r.get ("log.uuid"),
                    "uniquenumber", uniqueNumber,
                    "okiguid", OKIGuid
                ));
            }
                        
            final Map<String, Object> h = statusHash (action.getOkStatus ());
            
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
        
        return HASH (c.ID_IS_STATUS,     id,
            c.ID_IS_STATUS_GIS, id
        );
        
    }

    private void update (DB db, UUID uuid, Map<String, Object> r, Map<String, Object> h) throws SQLException {
        
logger.info ("h=" + h);
        
        h.put ("uuid", r.get ("r.uuid"));
        db.update (Infrastructure.class, h);
        
        h.put ("uuid", uuid);
        db.update (InfrastructureLog.class, h);
        
    }
    
    private GetStateResult getState (UUID orgPPAGuid, Map<String, Object> r) throws GisPollRetryException, GisPollException {

        GetStateResult rp;
        
        try {
            rp = wsGisInfrastructureClient.getState (orgPPAGuid, (UUID) r.get ("uuid_ack"));
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
