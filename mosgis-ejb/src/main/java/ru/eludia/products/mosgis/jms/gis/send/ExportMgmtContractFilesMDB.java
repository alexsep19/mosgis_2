package ru.eludia.products.mosgis.jms.gis.send;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.ContractFile;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.wsc.GisRestStream;
import ru.eludia.products.mosgis.ejb.wsc.RestGisFilesClient;
import ru.eludia.products.mosgis.ejb.wsc.RestGisFilesClient.Context;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inHouseMgmtContractFilesQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportMgmtContractFilesMDB extends UUIDMDB<ContractFile> {

    @EJB
    RestGisFilesClient restGisFilesClient;

    protected Get get (UUID uuid) {        

        return (Get) ModelHolder.getModel ()
            .get (getTable (), uuid, "*")
            .toOne (Contract.class).on ("AS ctr")
            .toOne (VocOrganization.class, "AS org", "orgppaguid").on ("ctr.uuid_org")
        ;

    }

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        final UUID orgppaguid = (UUID) r.get ("org.orgppaguid");
        
        try (
                
            GisRestStream out = new GisRestStream (
                restGisFilesClient,
                Context.HOMEMANAGEMENT,
                orgppaguid, 
                r.get ("label").toString (), 
                Long.parseLong (r.get ("len").toString ()),
                (uploadId, attachmentHash) -> {
                    r.put ("attachmentguid", uploadId);
                    r.put ("attachmenthash", attachmentHash);
                    db.update (ContractFile.class, r);
                }
            )
                
        ) {
            db.getStream (ModelHolder.getModel ().get (ContractFile.class, uuid, "body"), out);
        }
        catch (Exception ex) {
            logger.log (Level.SEVERE, "Cannot upload " + r, ex);
            return;
        }
                        
    }
    
}