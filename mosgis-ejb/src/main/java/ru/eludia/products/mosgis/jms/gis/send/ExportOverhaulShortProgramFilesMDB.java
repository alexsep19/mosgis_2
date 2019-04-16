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
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.ws.rest.clients.tools.GisRestStream;
import ru.eludia.products.mosgis.db.model.tables.OverhaulShortProgram;
import ru.eludia.products.mosgis.db.model.tables.OverhaulShortProgramDocument;
import ru.eludia.products.mosgis.db.model.tables.OverhaulShortProgramFile;
import ru.eludia.products.mosgis.db.model.tables.OverhaulShortProgramFileLog;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.eludia.products.mosgis.ws.rest.clients.RestGisFilesClient;
import ru.eludia.products.mosgis.ws.rest.clients.RestGisFilesClient.Context;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inExportOverhaulShortProgramFilesQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportOverhaulShortProgramFilesMDB extends UUIDMDB <OverhaulShortProgramFile> {
    
    @EJB
    RestGisFilesClient restGisFilesClient;
    
    protected Get get (UUID uuid) {
        
        return (Get) ModelHolder.getModel ()
            .get   (getTable (), uuid, "*")
            .toOne (OverhaulShortProgramDocument.class, "AS doc").on ()
            .toOne (OverhaulShortProgram.class, "AS program").on ("doc.program_uuid = program.uuid")
            .toOne (VocOrganization.class, "AS org", "orgppaguid").on ("program.org_uuid = org.uuid")
        ;
        
    }
    
    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        final UUID orgppaguid = (UUID) r.get ("org.orgppaguid");
        
        try {

            try (

                GisRestStream out = new GisRestStream (
                    restGisFilesClient,
                    Context.CAPITALREPAIRPROGRAMS,
                    orgppaguid,
                    r.get ("label").toString (), 
                    Long.parseLong (r.get ("len").toString ()),
                    (uploadId, attachmentHash) -> {

                        r.put ("attachmentguid", uploadId);
                        r.put ("attachmenthash", attachmentHash);

                        db.update (OverhaulShortProgramFile.class, r);

                        db.update (OverhaulShortProgramFileLog.class, HASH (
                            "uuid",           r.get ("id_log"),
                            "attachmentguid", uploadId,
                            "attachmenthash", attachmentHash
                        ));

                    }
                )

            ) {
                db.getStream (ModelHolder.getModel ().get (OverhaulShortProgramFile.class, uuid, "body"), out);
            }
            
        }
        catch (Exception ex) {
            
            db.update (OverhaulShortProgramFileLog.class, HASH (
                "uuid",     r.get ("id_log"),
                "ts_error", NOW,
                "err_text", "Ошибка загрузки файла " + ex.getMessage ()
            ));
            
            logger.log (Level.SEVERE, "Cannot upload " + r, ex);
            
            return;
            
        }
        
    }
    
}
