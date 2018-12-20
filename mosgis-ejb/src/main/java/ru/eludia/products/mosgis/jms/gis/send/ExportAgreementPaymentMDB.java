package ru.eludia.products.mosgis.jms.gis.send;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.tables.AgreementPayment;
import ru.eludia.products.mosgis.db.model.tables.AgreementPaymentLog;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.wsc.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.gis.send.base.GisExportMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inHouseAgreementPaymentsQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportAgreementPaymentMDB extends GisExportMDB<AgreementPaymentLog> {
    
    @EJB
    WsGisHouseManagementClient wsGisHouseManagementClient;
    
    @Resource (mappedName = "mosgis.outExportHouseAgreementPaymentsQueue")
    Queue outExportHouseAgreementPaymentsQueue;
    
    @Resource (mappedName = "mosgis.inHouseAgreementPaymentFilesQueue")
    Queue inHouseAgreementPaymentFilesQueue;
                
    protected Get get (UUID uuid) {        
        return ((AgreementPaymentLog) ModelHolder.getModel ().get (AgreementPaymentLog.class)).getForExport (uuid);
    }
        
    AckRequest.Ack invoke (DB db, AgreementPayment.Action action, UUID messageGUID,  Map<String, Object> r) throws Fault, SQLException {
            
        UUID orgPPAGuid = (UUID) r.get ("org.orgppaguid");
            
        switch (action) {
            case PLACING:     return wsGisHouseManagementClient.importPublicPropertyContractAgreementPayment (orgPPAGuid, messageGUID, r);
            default: throw new IllegalArgumentException ("No action implemented for " + action);
        }

    }
    
    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        VocGisStatus.i status = VocGisStatus.i.forId (r.get ("ctr." + AgreementPayment.c.ID_STATUS.lc ()));
        AgreementPayment.Action action = AgreementPayment.Action.forStatus (status);        

        if (action == null) {
            logger.warning ("No action is implemented for " + status);
            return;
        }
                                
        logger.info ("r=" + DB.to.json (r));
       
        try {            
            AckRequest.Ack ack = invoke (db, action, uuid, r);
            store (db, ack, r, action.getNextStatus ());
            uuidPublisher.publish (getQueue (action), ack.getRequesterMessageGUID ());
        }
        catch (Fault ex) {
            logger.log (Level.SEVERE, "Can't place voting protocol", ex);
            fail (db, ex.getFaultInfo (), r, action.getFailStatus ());
            return;
        }
        catch (Exception ex) {            
            logger.log (Level.SEVERE, "Cannot invoke WS", ex);            
            fail (db, action.toString (), action.getFailStatus (), ex, r);
            return;            
        }
        
    }
    
    Queue getQueue (AgreementPayment.Action action) {        
        return outExportHouseAgreementPaymentsQueue;        
    }

    @Override
    protected final Queue getFilesQueue () {
        return inHouseAgreementPaymentFilesQueue;
    }

    @Override
    protected Col getStatusCol () {
        return AgreementPayment.c.ID_STATUS.getCol ();
    }

    @Override
    protected Table getFileLogTable () {
        throw new UnsupportedOperationException ("No files here");
    }

}