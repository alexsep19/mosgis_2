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
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.incoming.InLicenses;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.jms.UUIDPublisher;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisLicenseClient;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.licenses_service_async.Fault;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inExportLicenseQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportLicenseMDB extends UUIDMDB<InLicenses> {

    private static final Logger logger = Logger.getLogger (ExportLicenseMDB.class.getName ());

    @EJB
    protected UUIDPublisher UUIDPublisher;

    @EJB
    protected WsGisLicenseClient wsGisLicenseClient;
    
    @Resource (mappedName = "mosgis.outExportLicenseQueue")
    private Queue queue;

    @Override
    protected Get get(UUID uuid) {
        return (Get) ModelHolder.getModel().get(getTable(), uuid, "AS root", "*")
                .toOne (VocOrganization.class, "AS org", "ogrn", "orgppaguid").on();
    }
    
    @Override
    protected void handleRecord(DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        try {
            
            AckRequest.Ack ack = wsGisLicenseClient.exportLicenses(r.get ("org.ogrn").toString (), (UUID) r.get("org.orgppaguid"), uuid);
            
            db.update (OutSoap.class, DB.HASH (
                "uuid",     uuid,
                "uuid_ack", ack.getMessageGUID ()
            ));
            
            UUIDPublisher.publish (queue, uuid);
            
        }
        catch (Fault ex) {
            
            db.update (OutSoap.class, HASH (
                "uuid", uuid,
                "id_status", DONE.getId (),
                "is_failed", 1,
                "err_code",  ex.getFaultInfo ().getErrorCode (),
                "err_text",  ex.getFaultInfo ().getErrorMessage ()
            ));
            
            Logger.getLogger (ExportLicenseMDB.class.getName()).log (Level.SEVERE, null, ex);
        }
        catch (Exception ex) {
            
            db.update (OutSoap.class, HASH (
                "uuid", uuid,
                "id_status", DONE.getId (),
                "is_failed", 1,
                "err_code",  "0",
                "err_text",  ex.getMessage ()
            ));            
            
            Logger.getLogger (ExportLicenseMDB.class.getName()).log (Level.SEVERE, null, ex);
            
        }
    }

    

}
