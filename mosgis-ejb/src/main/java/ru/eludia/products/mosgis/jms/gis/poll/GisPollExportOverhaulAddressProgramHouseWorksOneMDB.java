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
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.OverhaulAddressProgram;
import ru.eludia.products.mosgis.db.model.tables.OverhaulAddressProgramHouse;
import ru.eludia.products.mosgis.db.model.tables.OverhaulAddressProgramHouseWork;
import ru.eludia.products.mosgis.db.model.tables.OverhaulAddressProgramHouseWorkLog;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
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
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportOverhaulAddressProgramHouseWorksOneQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportOverhaulAddressProgramHouseWorksOneMDB extends GisPollMDB {
    
    @EJB
    protected UUIDPublisher UUIDPublisher;
    
    @EJB
    WsGisCapitalRepairClient wsGisCapitalRepairClient;
    
    @Override
    protected Get get (UUID uuid) {
        
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")                
            .toOne (OverhaulAddressProgramHouseWorkLog.class,       "AS log", "uuid", "action", "id_oaphw_status", "uuid_user AS user").on ("log.uuid_out_soap=root.uuid")
                .toOne (OverhaulAddressProgramHouseWork.class,      "AS works", "uuid").on ("log.uuid_object=works.uuid")
                    .toOne (OverhaulAddressProgramHouse.class,      "AS houses").on ("works.house_uuid=houses.uuid")
                        .toOne (OverhaulAddressProgram.class,       "AS program", "uuid").on ("houses.program_uuid=program.uuid")
                            .toOne (VocOrganization.class,           "AS org", "orgppaguid").on ("program.org_uuid=org.uuid")
        ;
        
    }
    
    @Override
    protected void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        UUID orgPPAGuid          = (UUID) r.get ("org.orgppaguid");
        
        OverhaulAddressProgramHouseWork.Action action = OverhaulAddressProgramHouseWork.Action.forStatus (VocGisStatus.i.forId (r.get ("log.id_oaphw_status")));
        
        try {
            
            GetStateResult state = getState (orgPPAGuid, r);
            
            final ErrorMessageType error = state.getErrorMessage ();
            
            if (error != null) throw new GisPollException (error);
            
            final List<CapRemCommonResultType> importResult = state.getImportResult ();
            
            if (importResult == null || importResult.isEmpty ()) throw new GisPollException ("0", "Сервис ГИС ЖКХ вернул пустой результат");
            
            for (CapRemCommonResultType.Error err: importResult.get (0).getError ()) throw new GisPollException (err);
            
            final Map<String, Object> h = statusHash (action.getOkStatus ());
            
            if (action == OverhaulAddressProgramHouseWork.Action.ANNUL)
                h.put ("is_deleted", 1);
            
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
            OverhaulAddressProgramHouseWork.c.ID_OAPHW_STATUS,          id,
            OverhaulAddressProgramHouseWork.c.ID_OAPHW_STATUS_GIS,      id
        );
        
    }

    private void update (DB db, UUID uuid, Map<String, Object> r, Map<String, Object> h) throws SQLException {
        
logger.info ("h=" + h);
        
        h.put ("uuid", r.get ("works.uuid"));
        db.update (OverhaulAddressProgramHouseWork.class, h);
        
        h.put ("uuid", uuid);
        db.update (OverhaulAddressProgramHouseWorkLog.class, h);
        
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
