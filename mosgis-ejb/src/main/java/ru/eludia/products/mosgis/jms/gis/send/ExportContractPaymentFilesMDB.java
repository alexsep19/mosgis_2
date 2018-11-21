package ru.eludia.products.mosgis.jms.gis.send;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Get;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.ContractPayment;
import ru.eludia.products.mosgis.db.model.tables.ContractPaymentFile;
import ru.eludia.products.mosgis.db.model.tables.ContractPaymentFileLog;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.wsc.GisRestStream;
import ru.eludia.products.mosgis.ejb.wsc.RestGisFilesClient;
import ru.eludia.products.mosgis.ejb.wsc.RestGisFilesClient.Context;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inHouseContractPaymentFilesQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportContractPaymentFilesMDB extends UUIDMDB<ContractPaymentFile> {

    @EJB
    RestGisFilesClient restGisFilesClient;

    protected Get get (UUID uuid) {        

        return (Get) ModelHolder.getModel ()
            .get (getTable (), uuid, "*")
            .toOne (ContractPayment.class, "AS ctr").on ()
            .toOne (Contract.class, "AS ctrt", "contractversionguid").on ()
            .toOne (VocOrganization.class, "AS org", "orgppaguid").on ("ctrt.uuid_org=org.uuid")
        ;

    }

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        final UUID orgppaguid = (UUID) r.get ("org.orgppaguid");
        
        try {

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

                        db.update (ContractPaymentFile.class, r);

                        db.update (ContractPaymentFileLog.class, HASH (
                            "uuid",           r.get ("id_log"),
                            "attachmentguid", uploadId,
                            "attachmenthash", attachmentHash
                        ));

                    }
                )

            ) {
                db.getStream (ModelHolder.getModel ().get (ContractPaymentFile.class, uuid, "body"), out);
            }
            
        }
        catch (Exception ex) {
            
            db.update (ContractPaymentFileLog.class, HASH (
                "uuid",     r.get ("id_log"),
                "ts_error", NOW,
                "err_text", "Ошибка загрузки файла " + ex.getMessage ()
            ));
            
            logger.log (Level.SEVERE, "Cannot upload " + r, ex);
            
            return;
            
        }
                                
    }
    
}