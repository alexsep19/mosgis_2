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
import ru.eludia.products.mosgis.db.model.tables.MainMunicipalService;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.MainMunicipalServiceLog;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.ejb.wsc.WsGisNsiClient;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.nsi_service_async.Fault;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inNsiMainMunicipalServicesQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportMainMunicipalServiceLogMDB extends UUIDMDB<MainMunicipalServiceLog> {

    private final Logger logger = Logger.getLogger (ExportMainMunicipalServiceLogMDB.class.getName ());

    @EJB
    WsGisNsiClient wsGisNsiClient;
    
    @Resource (mappedName = "mosgis.outExportNsiMainMunicipalServicesQueue")
    Queue queue;
    
    @EJB
    protected UUIDPublisher UUIDPublisher;
    
    @Override
    protected Get get (UUID uuid) {
        
        final MosGisModel model = ModelHolder.getModel ();
        
        try (DB db = model.getDb ()) {
            
            return (Get) model
                .get (getTable (), uuid, "AS root", "*")
                .toOne (NsiTable.getNsiTable (db, 2), "guid").on ("(root.code_vc_nsi_2=vc_nsi_2.code AND vc_nsi_2.isactual=1)")
                .toOne (NsiTable.getNsiTable (db, 3), "guid").on ("(root.code_vc_nsi_3=vc_nsi_3.code AND vc_nsi_3.isactual=1)")
            ;

        }
        catch (SQLException ex) {
            throw new IllegalStateException (ex);
        }
        
    }

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        Map<String, Object> data = db.getMap (
            ModelHolder.getModel ()
                .get   (MainMunicipalService.class, r.get ("uuid_object"))
                .toOne (VocOrganization.class, "AS org", "orgppaguid").on ()
        );

logger.info ("data = " + data);
        
        UUID orgPPAGuid = (UUID) data.get ("org.orgppaguid");        

        AckRequest.Ack ack;

        try {
            ack = wsGisNsiClient.importMunicipalServices (orgPPAGuid, r);
        }
        catch (Fault ex) {
            logger.log (Level.SEVERE, null, ex);
            return;
        }

        db.update (OutSoap.class, DB.HASH (
            "uuid",     ack.getRequesterMessageGUID (),
            "uuid_ack", ack.getMessageGUID ()
        ));

        db.update (getTable (), DB.HASH (
            "uuid",          uuid,
            "uuid_out_soap", ack.getRequesterMessageGUID (),
            "uuid_message",  ack.getMessageGUID ()
        ));
        
        UUIDPublisher.publish (queue, ack.getRequesterMessageGUID ());

    }

}