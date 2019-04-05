package ru.eludia.products.mosgis.jms.gis.poll;

import java.util.UUID;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.jms.UUIDPublisher;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisCapitalRepairClient;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportOverhaulRegionalProgramHouseWorksOneQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportOverhaulregionalProgramHouseWorksOneMDB extends GisPollMDB {
    
    @EJB
    protected UUIDPublisher UUIDPublisher;
    
    @EJB
    WsGisCapitalRepairClient wsGisCapitalRepairClient;
    
    @Override
    protected Get get (UUID uuid) {
        
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")                
            .toOne (OverhaulRegionalProgramHouseWorkLog.class,     "AS log", "uuid", "action", "id_orp_status", "uuid_user AS user").on ("log.uuid_out_soap=root.uuid")
            .toOne (OverhaulRegionalProgramHouse.class,        "AS program", "uuid").on ()
            .toOne (VocOrganization.class, "AS org", "orgppaguid").on ("program.org_uuid=org.uuid")
        ;
        
    }
    
}
