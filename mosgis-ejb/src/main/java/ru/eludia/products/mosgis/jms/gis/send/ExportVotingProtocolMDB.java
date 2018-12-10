package ru.eludia.products.mosgis.jms.gis.send;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.VotingProtocol;
import ru.eludia.products.mosgis.db.model.tables.VotingProtocolFileLog;
import ru.eludia.products.mosgis.db.model.tables.VotingProtocolLog;
import ru.eludia.products.mosgis.db.model.tables.VoteDecisionList;
import ru.eludia.products.mosgis.db.model.tables.VoteInitiator;
import ru.eludia.products.mosgis.db.model.tables.VotingProtocolFile;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.wsc.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.gis.send.base.GisExportMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inHouseVotingProtocolsQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportVotingProtocolMDB extends GisExportMDB<VotingProtocolLog> {
    
    @EJB
    WsGisHouseManagementClient wsGisHouseManagementClient;
    
    @Resource (mappedName = "mosgis.outExportHouseVotingProtocolsQueue")
    Queue outExportHouseVotingProtocolsQueue;
    
    @Resource (mappedName = "mosgis.inHouseVotingProtocolFilesQueue")
    Queue inHouseVotingProtocolFilesQueue;
                
    protected Get get (UUID uuid) {        
        
        final MosGisModel m = ModelHolder.getModel ();
        
        try (DB db = m.getDb ()) {
                        
            return (Get) m
                .get (getTable (), uuid, "*")
                .toOne (getEnTable (), "AS vp", VotingProtocol.c.ID_PRTCL_STATUS.lc ()).on ()
                .toOne (VocOrganization.class, "AS org", "orgppaguid").on ()
            ;
            
        }
        catch (SQLException ex) {
            throw new IllegalStateException (ex);
        }
                
    }
        
    AckRequest.Ack invoke (DB db, VotingProtocol.Action action, UUID messageGUID,  Map<String, Object> r) throws Fault, SQLException {
            
        UUID orgPPAGuid = (UUID) r.get ("org.orgppaguid");
            
        switch (action) {
            case PLACING:     return wsGisHouseManagementClient.placeVotingProtocol (orgPPAGuid, messageGUID, r);
            default: throw new IllegalArgumentException ("No action implemented for " + action);
        }

    }
    
    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        VocGisStatus.i status = VocGisStatus.i.forId (r.get ("vp." + VotingProtocol.c.ID_PRTCL_STATUS.lc ()));
        VotingProtocol.Action action = VotingProtocol.Action.forStatus (status);        
        if (action == null) {
            logger.warning ("No action is implemented for " + status);
            return;
        }
        
        final UUID uuidObject = (UUID) r.get ("uuid_object");

        Model m = db.getModel ();
        
        final List<Map<String, Object>> files = db.getList (m
            .select (VotingProtocolFile.class, "*")
            .toOne  (VotingProtocolFileLog.class, "AS log", "ts_start_sending", "err_text").on ()
            .where  (VotingProtocolFile.c.UUID_PROTOCOL.lc (), r.get ("uuid_object"))
            .and    ("id_status", 1)
        );
        
        if (isWaiting (files, db, action.getFailStatus (), r)) return;
        
        r.put ("files", files);
        
        r.put ("initiators", db.getList (m
            .select (VoteInitiator.class, "*")
            .where  (VoteInitiator.c.UUID_PROTOCOL.lc (), uuidObject)
            .and    (EnTable.c.IS_DELETED.lc (), 0)
        ));
        
        r.put ("decisions", db.getList (m
            .select (VoteDecisionList.class, "AS root", "*")
            .where  (VoteDecisionList.c.PROTOCOL_UUID.lc (), uuidObject)
            .and    (EnTable.c.IS_DELETED.lc (), 0)
            .toMaybeOne (NsiTable.getNsiTable (25),  "guid", "code").on ("(root." + VoteDecisionList.c.MANAGEMENTTYPE_VC_NSI_25.lc () + "=vc_nsi_25.code AND vc_nsi_25.isactual=1)")
            .toMaybeOne (NsiTable.getNsiTable (63),  "guid", "code").on ("(root." + VoteDecisionList.c.DECISIONTYPE_VC_NSI_63.lc () + "=vc_nsi_63.code AND vc_nsi_63.isactual=1)")
            .toMaybeOne (NsiTable.getNsiTable (241), "guid", "code").on ("(root." + VoteDecisionList.c.FORMINGFUND_VC_NSI_241.lc () + "=vc_nsi_241.code AND vc_nsi_241.isactual=1)")
        ));
        
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
    
    Queue getQueue (VotingProtocol.Action action) {        
        return outExportHouseVotingProtocolsQueue;        
    }

    @Override
    protected final Queue getFilesQueue () {
        return inHouseVotingProtocolFilesQueue;
    }

    @Override
    protected Table getFileLogTable () {
        return ModelHolder.getModel ().get (VotingProtocolFileLog.class);
    }

    @Override
    protected Col getStatusCol () {
        return VotingProtocol.c.ID_PRTCL_STATUS.getCol ();
    }

}