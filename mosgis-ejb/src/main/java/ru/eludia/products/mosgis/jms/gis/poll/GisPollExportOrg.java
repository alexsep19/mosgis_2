package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.json.JsonObject;
import javax.json.JsonString;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.incoming.InVocOrganization;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationLog;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationNsi20;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationTypes;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.wsc.WsGisOrgClient;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.eludia.products.mosgis.ws.base.AbstactServiceAsync;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common.ExportOrgRegistryResultType;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common.GetStateResult;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common_service_async.Fault;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportOrgQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportOrg extends UUIDMDB<OutSoap> {
    
    @EJB
    protected WsGisOrgClient wsGisOrgClient;

    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ()
            .get   (getTable (), uuid, "AS root", "*")
            .toOne (InVocOrganization.class, "AS i", "uuid_user").on ("root.uuid=i.uuid")
        ;        
    }

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {

        try {
            
            db.begin ();

            GetStateResult rp = wsGisOrgClient.getState ((UUID) r.get ("uuid_ack"));

            ErrorMessageType errorMessage = rp.getErrorMessage ();

            if (errorMessage != null) {

                if ("INT002012".equals (errorMessage.getErrorCode ())) {
                    
                    logger.warning ("OGRN not found");
                
                    db.update (OutSoap.class, HASH (
                        "uuid", uuid,
                        "id_status", DONE.getId ()
                    ));
                    
                }
                else {

                    logger.warning (errorMessage.getErrorCode () + " " + errorMessage.getDescription ());

                    db.update (OutSoap.class, HASH (
                        "uuid", uuid,
                        "id_status", DONE.getId (),
                        "is_failed", 1,
                        "err_code",  errorMessage.getErrorCode (),
                        "err_text",  errorMessage.getDescription ()
                    ));

                }                
                
                return;

            }

            for (ExportOrgRegistryResultType i: rp.getExportOrgRegistryResult ()) if (handleOrgRegistryResult (i.getOrgVersion (), rp, i, db, uuid, (UUID) r.get ("i.uuid_user"))) continue;

            db.update (OutSoap.class, HASH (
                "uuid", uuid,
                "id_status", DONE.getId ()
            ));
            
        }
        catch (Fault ex) {
            logger.log (Level.SEVERE, null, ex);
        }
        finally {

            db.commit ();
            
        }
        
    }

    private boolean handleOrgRegistryResult (ExportOrgRegistryResultType.OrgVersion orgVersion, GetStateResult rp, ExportOrgRegistryResultType i, DB db, UUID uuidOutSoap, UUID uuidUser) throws SQLException {
        
        Object o = null;
        
        if (orgVersion.getLegal () != null) o = orgVersion.getLegal ();
        if (orgVersion.getEntrp () != null) o = orgVersion.getEntrp ();
        if (orgVersion.getSubsidiary () != null) o = orgVersion.getSubsidiary ();
        if (orgVersion.getForeignBranch () != null) o = orgVersion.getForeignBranch ();
        
        if (o == null) {
            logger.warning ("Cannot import org: " + AbstactServiceAsync.toJSON (rp));
            return true;        
        }
                
        JsonObject jo = DB.to.JsonObject (AbstactServiceAsync.toJSON (o));
                
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
                
        db.upsert (VocOrganization.class, record);
        
        record.put ("uuid_object", uuid);
        record.put ("uuid_user", uuidUser);
        record.put ("uuid_out_soap", uuidOutSoap);
        record.put ("action", VocAction.i.REFRESH);
        
        db.insert (VocOrganizationLog.class, record);
        
        db.dupsert (
            VocOrganizationNsi20.class, 
            HASH ("uuid", uuid), 
            i.getOrganizationRoles ().stream ().map ((t) -> {return HASH (
                "is_deleted", 0,
                "code",       t.getCode ()
            );}).collect (Collectors.toList ()),
            "code"
        );

        return false;

    }

}