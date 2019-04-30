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
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationLog;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.ActualSomeContractObject;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.db.model.tables.HouseLog;
import ru.eludia.products.mosgis.jms.UUIDPublisher;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inExportOrgAccountsQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportOrgAccountsMDB extends UUIDMDB<VocOrganizationLog> {
    
    private static final Logger logger = Logger.getLogger (ExportOrgAccountsMDB.class.getName ());

    @EJB
    protected UUIDPublisher UUIDPublisher;

    @Resource (mappedName = "mosgis.inExportAccountsByFiasHouseGuidQueue")
    Queue inExportAccountsByFiasHouseGuidQueue;

    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ()
            .get (VocOrganizationLog.class, uuid, "AS log", "*")
        ;
    }    
    
    @Override
    protected void handleRecord (DB db, UUID uuid, Map r) throws SQLException {
        
        MosGisModel m = ModelHolder.getModel ();

        try {
            
            final Object uuidOrg = r.get ("uuid_object");
            
            String uuidHouse = db.getString (m
                .select (ActualSomeContractObject.class, "AS co")
                .where  (ActualSomeContractObject.c.UUID_ORG, uuidOrg)
                .where  (ActualSomeContractObject.c.FIASHOUSEGUID.lc () + " NOT IN", m
                    .select (HouseLog.class, "AS log")
                    .toOne  (House.class, "fiashouseguid AS fiashouseguid").on ()
                    .where ("uuid_vc_org_log", uuid)
                )
                .toOne  (House.class, "AS h", "uuid AS uuid").on ("co.fiashouseguid=h.fiashouseguid")
            );
            
            if (uuidHouse == null) {
                logger.info ("No houses found, import complete.");
                return;
            }

            UUID idLog = (UUID) db.insertId (HouseLog.class, HASH (
                "uuid_vc_org_log", uuid,
                "uuid_object",     uuidHouse,
                "uuid_org",        uuidOrg,
                "action",          r.get ("action"),
                "uuid_user",       r.get ("uuid_user")
            ));
            
            db.update (House.class, DB.HASH (
                "uuid",   uuidHouse,
                "id_log", idLog
            ));
            
            UUIDPublisher.publish (inExportAccountsByFiasHouseGuidQueue, idLog);
            
        }
        catch (Exception ex) {
            logger.log (Level.SEVERE, "Cannot import accounts", ex);
        }

    }

}