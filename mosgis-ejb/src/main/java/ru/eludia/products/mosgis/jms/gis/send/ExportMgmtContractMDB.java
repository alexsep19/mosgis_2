package ru.eludia.products.mosgis.jms.gis.send;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.ContractLog;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.ejb.wsc.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.house_management_service_async.Fault;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inHouseMgmtContractsQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportMgmtContractMDB extends UUIDMDB<ContractLog> {
    
    @EJB
    WsGisHouseManagementClient wsGisHouseManagementClient;

    @Resource (mappedName = "mosgis.outExportHouseMgmtContractsQueue")
    Queue queue;
    
    @EJB
    protected UUIDPublisher UUIDPublisher;
    
    protected Get get (UUID uuid) {        
        
        final MosGisModel m = ModelHolder.getModel ();
        
        try (DB db = m.getDb ()) {
            
            NsiTable nsi58 = NsiTable.getNsiTable (db, 58);
            
            return (Get) m
                .get (getTable (), uuid, "*")
                .toOne (Contract.class, "AS ctr", "uuid_org").on ()
                .toOne (nsi58, "AS vc_nsi_58", "guid").on ("vc_nsi_58.code=ctr.code_vc_nsi_58 AND vc_nsi_58.isactual=1")
            ;
            
        }
        catch (SQLException ex) {
            throw new IllegalStateException (ex);
        }
                
    }

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
                
        try {
            
            AckRequest.Ack ack = wsGisHouseManagementClient.placeContractData ((UUID) r.get ("ctr.uuid_org"), r);
            
            db.update (getTable (), DB.HASH (
                "uuid",          uuid,
                "uuid_out_soap", ack.getRequesterMessageGUID (),
                "uuid_message",  ack.getMessageGUID ()
            ));

            UUIDPublisher.publish (queue, ack.getRequesterMessageGUID ());
            
        }
        catch (Fault ex) {
            logger.log (Level.SEVERE, "Can't place management contract", ex);
            return;
        }

    }
    
}
