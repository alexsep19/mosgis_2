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
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.tables.HouseLog;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisHouseManagementClient;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.house_management.ExportAccountResultType;
import ru.gosuslugi.dom.schema.integration.house_management.GetStateResult;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportAccountsByFiasHouseGuidQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollImportAccountsByFiasHouseGuidMDB  extends GisPollMDB {

    @EJB
    private WsGisHouseManagementClient wsGisHouseManagementClient;

    @Resource (mappedName = "mosgis.inExportOrgAccountsQueue")
    Queue inExportOrgAccountsQueue;
    
    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")                
            .toOne (HouseLog.class,     "AS log", "uuid", "action", "uuid_vc_org_log").on ("log.uuid_out_soap=root.uuid")
            .toOne (VocOrganization.class, "AS org", VocOrganization.c.ORGPPAGUID.lc () + " AS orgppaguid").on ("log.uuid_org=org.uuid")
;
    }
    
    @Override
    protected void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        if (DB.ok (r.get ("is_failed"))) throw new IllegalStateException (r.get ("err_text").toString ());
                                
        try {
            
            GetStateResult state = getState (r);
            
            ErrorMessageType errorMessage = state.getErrorMessage ();
            
            if (errorMessage != null) throw new GisPollException (errorMessage);
            
            List<ExportAccountResultType> exportAccountResult = state.getExportAccountResult ();
            
            if (exportAccountResult == null || exportAccountResult.isEmpty ()) throw new GisPollException ("0", "Сервис ГИСЖКХ вернул пустой результат");

            for (ExportAccountResultType i: exportAccountResult) store (db, i);
            
            uuidPublisher.publish (inExportOrgAccountsQueue, (UUID) r.get ("log.uuid_vc_org_log"));

        }
        catch (GisPollRetryException ex) {
            return;
        }
        catch (GisPollException ex) {            
            ex.register (db, uuid, r);
        }
        
    }

    private void store (DB db, ExportAccountResultType acc) throws SQLException {

        logger.info ("Handling account " + acc.getAccountGUID () + " / " + acc.getAccountNumber () + " / " + acc.getUnifiedAccountNumber () + "...");
        
        if (DB.ok (acc.isIsTKOAccount ())) {
            logger.info ("isTKOAccount set: bailing out");
            return;
        }
        
        if (DB.ok (acc.isIsCRAccount ())) {
            logger.info ("isCRAccount set: bailing out");
            return;
        }
        
    }
    
    private GetStateResult getState (Map<String, Object> r) throws GisPollRetryException, GisPollException {

        GetStateResult rp;
        
        try {
            rp = wsGisHouseManagementClient.getState ((UUID) r.get ("orgppaguid"), (UUID) r.get ("uuid_ack"));
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