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
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationProposal;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationProposalLog;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationTypes;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.wsc.WsGisOrgClient;
import ru.eludia.products.mosgis.jms.gis.send.base.GisExportMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.organizations_registry_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inVocOrganizationProposalsQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportVocOrganizationProposalMDB extends GisExportMDB<VocOrganizationProposalLog> {
    
    @EJB
    WsGisOrgClient wsGisOrgClient;
    
    @Resource (mappedName = "mosgis.outExportVocOrganizationProposalsQueue")
    Queue outExportHouseVocOrganizationProposalsQueue;
                    
    @Override
    protected Get get (UUID uuid) {        
        return ((VocOrganizationProposalLog) ModelHolder.getModel ().get (VocOrganizationProposalLog.class)).getForExport (uuid.toString ());
    }
        
    AckRequest.Ack invoke (DB db, VocOrganizationProposal.Action action, UUID messageGUID,  Map<String, Object> r) throws Fault, SQLException {
            
        UUID orgPPAGuid = (UUID) r.get ("o.orgppaguid");
            
        switch (action) {
            
            case PLACING: switch (VocOrganizationTypes.i.forId (r.get (VocOrganizationProposal.c.ID_TYPE))) {
                case SUBSIDIARY:     return wsGisOrgClient.importSubsidiary (orgPPAGuid, messageGUID, r);
                case FOREIGN_BRANCH: return wsGisOrgClient.importForeignBranch (orgPPAGuid, messageGUID, r);
                default: throw new IllegalArgumentException ("Neither a Subsidiary nor a ForeignBranch: " + r);
            }
            
            default: throw new IllegalArgumentException ("No action implemented for " + action);
            
        }

    }
    
    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        VocGisStatus.i status = VocGisStatus.i.forId (r.get ("r." + VocOrganizationProposal.c.ID_ORG_PR_STATUS.lc ()));
        VocOrganizationProposal.Action action = VocOrganizationProposal.Action.forStatus (status);        

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
            logger.log (Level.SEVERE, "Can't place subsidiary", ex);
            fail (db, ex.getFaultInfo (), r, action.getFailStatus ());
            return;
        }
        catch (Exception ex) {            
            logger.log (Level.SEVERE, "Cannot invoke WS", ex);            
            fail (db, action.toString (), action.getFailStatus (), ex, r);
            return;            
        }
        
    }
    
    Queue getQueue (VocOrganizationProposal.Action action) {
        return outExportHouseVocOrganizationProposalsQueue;        
    }

    @Override
    protected final Queue getFilesQueue () {
        return null;
    }

    @Override
    protected Col getStatusCol () {
        return VocOrganizationProposal.c.ID_ORG_PR_STATUS.getCol ();
    }

    @Override
    protected Table getFileLogTable () {
        return null;
    }

}