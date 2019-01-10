package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;
import javax.json.JsonObject;
import javax.json.JsonString;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.products.mosgis.db.model.incoming.InLicenses;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationLog;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationNsi20;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationTypes;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.ejb.wsc.WsGisOrgCommonClient;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.eludia.products.mosgis.ws.base.AbstactServiceAsync;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiRef;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common.ExportOrgRegistryResultType;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common.GetStateResult;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common_service_async.Fault;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportOrgByGUIDQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportOrgByGuid extends UUIDMDB<OutSoap> {
    
    @EJB
    public UUIDPublisher uuidPublisher;
    
    @EJB
    protected WsGisOrgCommonClient wsGisOrgClient;

    @Resource (mappedName = "mosgis.inExportLicenseQueue")
    private Queue inExportLicenseQueue;
    
    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {

        try {
            
            db.begin ();

            GetStateResult rp = wsGisOrgClient.getState ((UUID) r.get ("uuid_ack"));

            ErrorMessageType errorMessage = rp.getErrorMessage ();

            if (errorMessage != null) {

                logger.warning (errorMessage.getErrorCode () + " " + errorMessage.getDescription ());

                db.update (OutSoap.class, HASH (
                    "uuid", uuid,
                    "id_status", DONE.getId (),
                    "is_failed", 1,
                    "err_code",  errorMessage.getErrorCode (),
                    "err_text",  errorMessage.getDescription ()
                ));

                return;

            }

            for (ExportOrgRegistryResultType i: rp.getExportOrgRegistryResult ()) if (handleOrgRegistryResult (i.getOrgVersion (), rp, i, db, uuid)) continue;

            db.update (OutSoap.class, HASH (
                "uuid", uuid,
                "id_status", DONE.getId ()
            ));
            
            db.commit ();

        }
        catch (Fault ex) {
            logger.log (Level.SEVERE, null, ex);
        }
        
    }

    private boolean handleOrgRegistryResult (ExportOrgRegistryResultType.OrgVersion orgVersion, GetStateResult rp, ExportOrgRegistryResultType i, DB db, UUID logUuid) throws SQLException {
        
        Object o = null;
        
        if (orgVersion.getLegal () != null) o = orgVersion.getLegal ();
        if (orgVersion.getEntrp () != null) o = orgVersion.getEntrp ();
        if (orgVersion.getSubsidiary () != null) o = orgVersion.getSubsidiary ();
        if (orgVersion.getForeignBranch () != null) o = orgVersion.getForeignBranch ();
        
        if (o == null) {
            logger.warning ("Cannot import org: " + AbstactServiceAsync.toJSON (rp));
            return true;        
        }
        
logger.info ("" + o.getClass ().getSimpleName ());
        
        JsonObject jo = DB.to.JsonObject (AbstactServiceAsync.toJSON (o));
        
logger.info ("" + jo);
        
        final String uuid = i.getOrgRootEntityGUID ();
        
        Map<String, Object> record = DB.HASH (
            "id_type",           VocOrganizationTypes.i.valueOf (o).getId (),
            "orgrootentityguid", uuid,
            "orgppaguid",        i.getOrgPPAGUID ()
        );
        
        jo.forEach ((k, v) -> {
            if (!(v instanceof JsonString)) return;
            String f = k.toLowerCase ();
            if ("ogrnip".equals (f)) f = "ogrn";
            record.put (f, ((JsonString) v).getString ());
        });
        
        db.update (VocOrganization.class, record);        
        
        record.put ("uuid", logUuid);        

        db.update (VocOrganizationLog.class, record);
        
        boolean isUo = false;
        
        List<Map<String, Object>> roles = new ArrayList<>();
        for (NsiRef role : i.getOrganizationRoles()) {
            if ("1".equals(role.getCode()))
                isUo = true;
            
            roles.add(HASH(
                "is_deleted", 0,
                "code", role.getCode()
            ));
        }
        
        db.dupsert (
            VocOrganizationNsi20.class, 
            HASH ("uuid", uuid), 
            roles,
            "code"
        );

        if (isUo) {
            uuidPublisher.publish(inExportLicenseQueue, (UUID) db.insertId(InLicenses.class, DB.HASH("uuid_org", uuid)));
        }

        return false;

    }

}