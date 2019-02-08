package ru.eludia.products.mosgis.jms.gis.send;

import java.sql.SQLException;
import java.util.Collections;
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
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationLog;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.ejb.wsc.WsGisHouseManagementClient;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inExportOrgCharterQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportOrgChartersMDB extends UUIDMDB<VocOrganizationLog> {
    
    private static final Logger logger = Logger.getLogger (ExportOrgChartersMDB.class.getName ());

    @EJB
    protected UUIDPublisher UUIDPublisher;

    @EJB
    WsGisHouseManagementClient wsGisHouseManagementClient;

    @Resource (mappedName = "mosgis.outExportOrgChartersQueue")
    Queue q;
    
    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ()
            .get (VocOrganizationLog.class, uuid, "AS log", "*")
            .toOne (VocOrganization.class, "AS org", VocOrganization.c.ORGPPAGUID.lc () + " AS ppa").on ("log.uuid_object=org.uuid");
    }    
    
    @Override
    protected void handleRecord (DB db, UUID uuid, Map r) throws SQLException {
                
        try {

            db.update (OutSoap.class, DB.HASH (
                "uuid",     uuid,
                "uuid_ack", wsGisHouseManagementClient.exportCharterData ((UUID) r.get ("ppa"), uuid, Collections.EMPTY_LIST).getMessageGUID ()
            ));
            
            db.update (getTable (), DB.HASH (
                "uuid",          uuid,
                "uuid_out_soap", uuid
            ));
                
            UUIDPublisher.publish (q, uuid);
            
        }
        catch (Exception ex) {
            logger.log (Level.SEVERE, "Cannot import orgs mgmt contracts", ex);
        }

    }

}