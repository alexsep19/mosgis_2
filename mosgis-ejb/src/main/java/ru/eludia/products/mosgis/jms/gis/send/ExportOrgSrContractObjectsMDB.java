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
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractLog;
import ru.eludia.products.mosgis.jms.UUIDPublisher;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inExportOrgSrContractObjectsQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportOrgSrContractObjectsMDB extends UUIDMDB<VocOrganizationLog> {
    
    private static final Logger logger = Logger.getLogger (ExportOrgSrContractObjectsMDB.class.getName ());

    @EJB
    protected UUIDPublisher UUIDPublisher;

    @Resource (mappedName = "mosgis.inExportSupplyResourceContractObjectsQueue")
    Queue inExportSupplyResourceContractObjectsQueue;

    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ()
	    .get (VocOrganizationLog.class, uuid, "AS log", "action", "uuid_user", "uuid_object")
        ;
    }    
    
    @Override
    protected void handleRecord (DB db, UUID uuid, Map r) throws SQLException {
        
        MosGisModel m = ModelHolder.getModel ();

        try {
            
            final Object uuidOrg = r.get ("uuid_object");
            
            String uuidSrCtr = db.getString (m
                .select (SupplyResourceContract.class, "AS ctr", "uuid")
                .where  (SupplyResourceContract.c.UUID_ORG, uuidOrg)
                .where  (EnTable.c.IS_DELETED, 0)
		.and(SupplyResourceContract.c.CONTRACTROOTGUID.lc () + " IN", m
                    .select (SupplyResourceContractLog.class, "AS log")
		    .toOne (SupplyResourceContract.class, "AS sr_ctr", "contractrootguid AS contractrootguid").on()
                    .where ("uuid_vc_org_log", uuid)
		    .and ("action", VocAction.i.IMPORT_SR_CONTRACTS)
                )
		.and(SupplyResourceContract.c.CONTRACTROOTGUID.lc () + " NOT IN", m
                    .select (SupplyResourceContractLog.class, "AS log_done")
		    .toOne (SupplyResourceContract.class, "AS sr_ctr_done", "contractrootguid AS contractrootguid").on()
                    .where ("uuid_vc_org_log", uuid)
		    .and ("action", VocAction.i.IMPORT_SR_CONTRACT_OBJECTS)
                )
		.and(EnTable.c.IS_DELETED, 0)
            );
            
            if (uuidSrCtr == null) {
                logger.info ("No supply resource contracts found, import complete.");
                return;
            }

            UUID idLog = (UUID) db.insertId (SupplyResourceContractLog.class, HASH (
                "uuid_vc_org_log", uuid,
                "uuid_object",     uuidSrCtr,
                "uuid_org",        uuidOrg,
                "action",          VocAction.i.IMPORT_SR_CONTRACT_OBJECTS,
                "uuid_user",       r.get ("uuid_user")
            ));
            
            db.update (SupplyResourceContract.class, DB.HASH (
                "uuid",   uuidSrCtr,
                "id_log", idLog
            ));
            
            UUIDPublisher.publish (inExportSupplyResourceContractObjectsQueue, idLog);
            
        }
        catch (Exception ex) {
            logger.log (Level.SEVERE, "Cannot import org supply resource contract objects", ex);
        }

    }

}