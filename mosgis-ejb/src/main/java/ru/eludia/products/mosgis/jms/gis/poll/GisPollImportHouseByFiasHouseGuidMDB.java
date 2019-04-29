package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.eludia.products.mosgis.db.model.voc.VocBuildingLog;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisHouseManagementClient;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.house_management.ExportHouseResultType;
import ru.gosuslugi.dom.schema.integration.house_management.GetStateResult;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportHouseDataByFiasHouseGuidQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollImportHouseByFiasHouseGuidMDB  extends GisPollMDB {

    @EJB
    private WsGisHouseManagementClient wsGisHouseManagementClient;

    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")                
            .toOne (VocBuildingLog.class,     "AS log", "uuid", "action").on ("log.uuid_out_soap=root.uuid")
            .toOne (VocBuilding.class,        "AS r", "uuid").on ()
        ;
    }

    @Override
    protected void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        if (DB.ok (r.get ("is_failed"))) throw new IllegalStateException (r.get ("err_text").toString ());
                                
        try {
            
            GetStateResult state = getState (r);
            
            ErrorMessageType errorMessage = state.getErrorMessage ();
            
            if (errorMessage != null) throw new GisPollException (errorMessage);
            
            ExportHouseResultType exportHouseResult = state.getExportHouseResult ();
            
            if (exportHouseResult == null) throw new GisPollException ("0", "Сервис ГИСЖКХ вернул пустой результат");
            
            String houseUniqueNumber = exportHouseResult.getHouseUniqueNumber ();
logger.info ("houseUniqueNumber=" + houseUniqueNumber);

            ExportHouseResultType.ApartmentHouse apartmentHouse = exportHouseResult.getApartmentHouse ();
logger.info ("apartmentHouse=" + apartmentHouse);

            ExportHouseResultType.LivingHouse livingHouse = exportHouseResult.getLivingHouse ();
logger.info ("livingHouse=" + livingHouse);
            
            db.update (OutSoap.class, HASH (
                "uuid", getUuid (),
                "id_status", DONE.getId ()
            ));

        }
        catch (GisPollRetryException ex) {
            return;
        }
        catch (GisPollException ex) {            
            ex.register (db, uuid, r);
        }
        
    }


    
    private GetStateResult getState (Map<String, Object> r) throws GisPollRetryException, GisPollException {

        GetStateResult rp;
        
        try {
            rp =  wsGisHouseManagementClient.getState ((UUID) r.get ("uuid_ack"));
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