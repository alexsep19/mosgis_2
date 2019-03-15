package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
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
import ru.eludia.products.mosgis.jms.UUIDPublisher;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisOrgCommonClient;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.eludia.products.mosgis.ws.soap.tools.SOAPTools;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiRef;
import ru.gosuslugi.dom.schema.integration.organizations_registry_base.SubsidiaryType;
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
            
            GetStateResult rp = null;
            
            try {
                rp = wsGisOrgClient.getState ((UUID) r.get ("uuid_ack"));
            }
            catch (Fault e) {
                
                db.update (OutSoap.class, HASH (
                    "uuid", uuid,
                    "id_status", DONE.getId (),
                    "is_failed", 1,
                    "err_code",  e.getFaultInfo ().getErrorCode (),
                    "err_text",  e.getFaultInfo ().getErrorMessage ()
                ));

                return;
                
            }
            catch (Exception e) {
                logger.log (Level.SEVERE, "wsGisOrgClient.getState failed, will retry", e);
                uuidPublisher.publish (ownDestination, uuid);
                return;
            }
            
            final byte requestState = rp.getRequestState ();
        
            if (requestState < DONE.getId ()) {                
                logger.info ("requestState = " + requestState + ", retrying request");
                uuidPublisher.publish (ownDestination, uuid);
                return;        
            }        

            db.begin ();

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
        catch (SQLException ex) {
            logger.log (Level.SEVERE, null, ex);
        }
        
    }

    private boolean handleOrgRegistryResult (ExportOrgRegistryResultType.OrgVersion orgVersion, GetStateResult rp, ExportOrgRegistryResultType i, DB db, UUID logUuid) throws SQLException {
        
        Object o = null;
        String parentOrgGuid = null;
        SubsidiaryType.SourceName source = null;
        
        if (orgVersion.getLegal () != null) o = orgVersion.getLegal ();
        if (orgVersion.getEntrp () != null) o = orgVersion.getEntrp ();
        if (orgVersion.getSubsidiary () != null) {
            o = orgVersion.getSubsidiary ();
            
            source = orgVersion.getSubsidiary().getSourceName();
            
            String parentOrgVersionGuid = orgVersion.getSubsidiary().getParentOrg().getRegOrgVersion().getOrgVersionGUID();
            parentOrgGuid = db.getString(db.getModel()
                    .select(VocOrganization.class, "uuid")
                    .where(VocOrganization.c.ORGVERSIONGUID, parentOrgVersionGuid));
        }
        if (orgVersion.getForeignBranch () != null) o = orgVersion.getForeignBranch ();
        
        if (o == null) {
            logger.warning ("Cannot import org: " + SOAPTools.toJSON (rp));
            return true;        
        }
        
logger.info ("" + o.getClass ().getSimpleName ());
        
        JsonObject jo = DB.to.JsonObject (SOAPTools.toJSON (o));
        
logger.info ("" + jo);
        
        final String uuid = i.getOrgRootEntityGUID ();
        
        Map<String, Object> record = DB.HASH (
            VocOrganization.c.ID_TYPE,           VocOrganizationTypes.i.valueOf (o).getId (),
            VocOrganization.c.ORGROOTENTITYGUID, uuid,
            VocOrganization.c.ORGVERSIONGUID,    orgVersion.getOrgVersionGUID(),
            VocOrganization.c.ORGPPAGUID,        i.getOrgPPAGUID (),
            VocOrganization.c.LASTEDITINGDATE,   orgVersion.getLastEditingDate(),
            VocOrganization.c.ISACTUAL,          orgVersion.isIsActual(),
            VocOrganization.c.PARENT,            parentOrgGuid,
            VocOrganization.c.ISREGISTERED,      i.isIsRegistered() != null ? i.isIsRegistered() : false
        );
        
        if (source != null) {
            record.put(VocOrganization.c.SOURCENAME.lc(), source.getValue());
            record.put(VocOrganization.c.SOURCEDATE.lc(), source.getDate());
        }
        
        jo.forEach ((k, v) -> {
            if (!(v instanceof JsonString)) return;
            String f = k.toLowerCase ();
            if ("ogrnip".equals (f) || "nza".equals (f)) f = "ogrn";
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