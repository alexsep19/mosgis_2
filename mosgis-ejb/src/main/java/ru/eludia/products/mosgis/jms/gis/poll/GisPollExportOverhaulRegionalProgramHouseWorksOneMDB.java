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
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgram;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramHouse;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramHouseWork;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramHouseWorkLog;
import ru.eludia.products.mosgis.db.model.tables.OverhaulRegionalProgramLog;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.jms.UUIDPublisher;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisCapitalRepairClient;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.capital_repair.CapRemCommonResultType;
import ru.gosuslugi.dom.schema.integration.capital_repair.GetStateResult;
import ru.gosuslugi.dom.schema.integration.capital_repair_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportOverhaulRegionalProgramHouseWorksOneQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportOverhaulRegionalProgramHouseWorksOneMDB extends GisPollMDB {
    
    @EJB
    protected UUIDPublisher UUIDPublisher;
    
    @EJB
    WsGisCapitalRepairClient wsGisCapitalRepairClient;
    
    @Override
    protected Get get (UUID uuid) {
        
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")                
            .toOne (OverhaulRegionalProgramHouseWorkLog.class,       "AS log", "uuid", "action", "id_orphw_status", "uuid_user AS user").on ("log.uuid_out_soap=root.uuid")
                .toOne (OverhaulRegionalProgramHouseWork.class,      "AS works").on ("log.uuid_object=works.uuid")
                    .toOne (OverhaulRegionalProgramHouse.class,      "AS houses").on ("works.house_uuid=houses.uuid")
                        .toOne (OverhaulRegionalProgram.class,       "AS program", "uuid").on ("houses.program_uuid=program.uuid")
                            .toOne (VocOrganization.class,           "AS org", "orgppaguid").on ("program.org_uuid=org.uuid")
        ;
        
    }
    
    @Override
    protected void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        UUID orgPPAGuid          = (UUID) r.get ("org.orgppaguid");
        
        OverhaulRegionalProgramHouseWork.Action action = OverhaulRegionalProgramHouseWork.Action.forStatus (VocGisStatus.i.forId (r.get ("log.id_orphw_status")));
        
        try {
            
            GetStateResult state = getState (orgPPAGuid, r);
            
            final ErrorMessageType error = state.getErrorMessage ();
            
            if (error != null) throw new GisPollException (error);
            
            final List<CapRemCommonResultType> importResult = state.getImportResult ();
            
            if (importResult == null || importResult.isEmpty ()) throw new GisPollException ("0", "Сервис ГИС ЖКХ вернул пустой результат");
            
            for (CapRemCommonResultType.Error err: importResult.get (0).getError ()) throw new GisPollException (err);
            
            final Map<String, Object> h = statusHash (action.getOkStatus ());
            
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
            OverhaulRegionalProgramHouseWork.c.ID_ORPHW_STATUS,          id,
            OverhaulRegionalProgramHouseWork.c.ID_ORPHW_STATUS_GIS,      id
        );
        
    }

    private void update (DB db, UUID uuid, Map<String, Object> r, Map<String, Object> h) throws SQLException {
        
logger.info ("h=" + h);
        
        h.put ("uuid", r.get ("program.uuid"));
        db.update (OverhaulRegionalProgramHouseWork.class, h);
        
        h.put ("uuid", uuid);
        db.update (OverhaulRegionalProgramHouseWorkLog.class, h);
        
    }
    
    private GetStateResult getState (UUID orgPPAGuid, Map<String, Object> r) throws GisPollRetryException, GisPollException {

        GetStateResult rp;
        
        try {
            rp = wsGisCapitalRepairClient.getState (orgPPAGuid, (UUID) r.get ("uuid_ack"));
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
