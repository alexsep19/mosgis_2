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
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.tables.CitizenCompensationLog;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.CitizenCompensation;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.gosuslugi.dom.schema.integration.msp_service_async.Fault;
import ru.eludia.products.mosgis.db.model.tables.CitizenCompensation.c;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisMSPClient;
import ru.gosuslugi.dom.schema.integration.base.CommonResultType;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.msp.GetStateResult;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportCitizenCompensationsQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportCitizenCompensationMDB  extends GisPollMDB {

    @EJB
    WsGisMSPClient wsGisMSPClient;

    @Override
    protected Get get (UUID uuid) {
        
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")                

            .toOne (CitizenCompensationLog.class, "AS log", "uuid", "action").on ("log.uuid_out_soap=root.uuid")

            .toOne (CitizenCompensation.class, "AS r"
                , EnTable.c.UUID.lc ()
                , CitizenCompensation.c.ID_CTR_STATUS.lc ()
            ).on ()
                
            .toMaybeOne (VocOrganization.class, "AS org", "orgppaguid AS orgppaguid").on ("r.uuid_org=org.uuid")
                                                
        ;
        
    }

    @Override
    protected void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        if (DB.ok (r.get ("is_failed"))) throw new IllegalStateException (r.get ("err_text").toString ());
        
        UUID orgPPAGuid = (UUID) r.get ("orgppaguid");
        
        CitizenCompensation.Action action = CitizenCompensation.Action.forStatus (
            VocGisStatus.i.forId (r.get ("r.id_ctr_status"))                
        );
                
        try {

            GetStateResult state = getState (orgPPAGuid, r);

            ErrorMessageType errorMessage = state.getErrorMessage ();

            if (errorMessage != null) throw new GisPollException (errorMessage);

            List<CommonResultType> commonResult = state.getImportResult ();

            if (commonResult == null || commonResult.isEmpty ()) throw new GisPollException ("0", "Сервис ГИС ЖКХ вернул пустой результат");

            final CommonResultType cr = commonResult.get (0);
            
            List<CommonResultType.Error> error = cr.getError ();
            
            if (error != null && !error.isEmpty ()) throw new GisPollException (error.get (0));
            
//            ImportResult.CommonResult.ImportCitizenCompensation importCitizenCompensation = cr.getImportCitizenCompensation ();
//
//            if (importCitizenCompensation == null) throw new GisPollException ("0", "Сервис ГИС ЖКХ вернул результат без элемента importCitizenCompensation");
            
            final Map<String, Object> h = statusHash (action.getOkStatus ());

            h.put (CitizenCompensation.c.CITIZENCOMPENSATIONGUID.lc (), cr.getGUID ());

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
        
        return HASH (c.ID_CTR_STATUS,     id,
            c.ID_CTR_STATUS_GIS, id
        );
        
    }

    private void update (DB db, UUID uuid, Map<String, Object> r, Map<String, Object> h) throws SQLException {
        
logger.info ("h=" + h);
        
        h.put ("uuid", r.get ("r.uuid"));
        db.update (CitizenCompensation.class, h);
        
        h.put ("uuid", uuid);
        db.update (CitizenCompensationLog.class, h);
        
    }
    
    private GetStateResult getState (UUID orgPPAGuid, Map<String, Object> r) throws GisPollRetryException, GisPollException {

        GetStateResult rp;
        
        try {
            rp = wsGisMSPClient.getState (orgPPAGuid, (UUID) r.get ("uuid_ack"));
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
