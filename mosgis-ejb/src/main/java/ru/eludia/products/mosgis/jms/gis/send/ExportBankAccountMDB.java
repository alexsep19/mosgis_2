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
import ru.eludia.products.mosgis.db.model.tables.BankAccount;
import ru.eludia.products.mosgis.db.model.tables.BankAccountLog;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisCapitalRepairClient;
import ru.eludia.products.mosgis.jms.gis.send.base.GisExportMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.capital_repair_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inExportBankAccountQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportBankAccountMDB extends GisExportMDB<BankAccountLog> {
    
    @EJB
    WsGisCapitalRepairClient wsGisCapitalRepairClient;

    @Resource (mappedName = "mosgis.outExportBankAccountQueue")
    Queue outExportBankAccountQueue;

    @Override
    protected Get get (UUID uuid) {        
        return ((BankAccountLog) ModelHolder.getModel ().get (BankAccountLog.class)).getForExport (uuid.toString ());
    }
        
    AckRequest.Ack invoke (DB db, BankAccount.Action action, UUID messageGUID,  Map<String, Object> r) throws Fault, SQLException {
            
        UUID orgPPAGuid = (UUID) r.get ("orgppaguid");
            
        switch (action) {
            case PLACING:
            case EDITING:
                return wsGisCapitalRepairClient.importRegionalOperatorAccount(orgPPAGuid, messageGUID, r);
	    case TERMINATION:
		return wsGisCapitalRepairClient.terminateRegionalOperatorAccount(orgPPAGuid, messageGUID, r);
            case ANNULMENT:
                return wsGisCapitalRepairClient.deleteRegionalOperatorAccount(orgPPAGuid, messageGUID, r);
            default: 
                throw new IllegalArgumentException ("No action implemented for " + action.name ());
        }
    }
    
    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        VocGisStatus.i status = VocGisStatus.i.forId (r.get ("r." + BankAccount.c.ID_CTR_STATUS.lc ()));
        BankAccount.Action action = BankAccount.Action.forStatus (status);

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
            logger.log (Level.SEVERE, "Can't place bank accounts", ex);
            fail (db, ex.getFaultInfo (), r, action.getFailStatus ());
            return;
        }
        catch (Exception ex) {            
            logger.log (Level.SEVERE, "Cannot invoke WS", ex);            
            fail (db, action.toString (), action.getFailStatus (), ex, r);
            return;            
        }
        
    }
    
    Queue getQueue (BankAccount.Action action) {
        return outExportBankAccountQueue;
    }

    @Override
    protected final Queue getFilesQueue () {
        return null;
    }

    @Override
    protected Col getStatusCol () {
        return BankAccount.c.ID_CTR_STATUS.getCol ();
    }

    @Override
    protected Table getFileLogTable () {
        return null;
    }

}