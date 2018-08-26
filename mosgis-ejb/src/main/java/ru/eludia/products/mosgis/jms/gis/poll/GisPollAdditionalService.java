package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.AdditionalService;
import ru.eludia.products.mosgis.db.model.tables.AdditionalServiceLog;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState.i.OK;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState.i.FAIL;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.wsc.WsGisNsiClient;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.gosuslugi.dom.schema.integration.base.CommonResultType;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.nsi.GetStateResult;
import ru.gosuslugi.dom.schema.integration.nsi_service_async.Fault;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportNsiAdditionalServicesQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollAdditionalService extends UUIDMDB<OutSoap> {
    
    @EJB
    WsGisNsiClient wsGisNsiClient;
    
    @Override
    protected Get get (UUID uuid) {
        
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")
            .toOne (AdditionalServiceLog.class, "AS log", "uuid", "elementguid", "uuid_object").on ("log.uuid_out_soap=root.uuid")
            .toOne (AdditionalService.class).on ()
            .toOne (VocOrganization.class, "AS org", "orgppaguid").on ();
        
    }
    
    private void fail (DB db, UUID uuid, Object uuidObject, String code, String text) throws SQLException {
        
        logger.warning (code + " " + text);
        
        db.update (OutSoap.class, HASH (
            "uuid", uuid,
            "id_status", DONE.getId (),
            "is_failed", 1,
            "err_code",  code,
            "err_text",  text
        ));

        db.update (AdditionalService.class, HASH (
            "uuid",         uuidObject,
            "id_status",    FAIL.getId ()
        ));

        db.commit ();
        
    }    

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {

logger.info ("" + r);
        
        final Object uuidObject = r.get ("log.uuid_object");
        UUID orgPPAGuid = (UUID) r.get ("org.orgppaguid");
    
        try {
            
            db.begin ();

            GetStateResult rp = wsGisNsiClient.getState (orgPPAGuid, (UUID) r.get ("uuid_ack"));

            ErrorMessageType errorMessage = rp.getErrorMessage ();

            if (errorMessage != null) {
/*
                if ("INT002012".equals (errorMessage.getErrorCode ())) {
                    
                    logger.warning ("OGRN not found");
                
                    db.update (OutSoap.class, HASH (
                        "uuid", uuid,
                        "id_status", DONE.getId ()
                    ));
                    
                }
                else {
*/
                    logger.warning (errorMessage.getErrorCode () + " " + errorMessage.getDescription ());
                    
                    fail (db, uuid, uuidObject, errorMessage.getErrorCode (), errorMessage.getDescription ());
/*
                }                
*/
                return;

            }
            
            List<CommonResultType> importResult = rp.getImportResult ();
            
            int len = importResult == null ? 0 : importResult.size ();
            
            if (len != 1) {                
                fail (db, uuid, uuidObject, "0", "Вместо 1 результата вернулось " + len);                
                return;                    
            }
            
            CommonResultType result = importResult.get (0);
            
            if (result.getError () != null && !result.getError ().isEmpty ()) {                
                CommonResultType.Error error = result.getError ().get (0);
                fail (db, uuid, uuidObject, error.getErrorCode (), error.getDescription ());
                return;
            }
            
            db.update (AdditionalService.class, HASH (
                "uuid",         uuidObject,
                "id_status",    OK.getId (),
                "uniquenumber", result.getUniqueNumber (),
                "elementguid",  result.getGUID ()
            ));            

            db.update (AdditionalServiceLog.class, HASH (
                "uuid",            r.get ("log.uuid"),
                "elementguid_new", result.getGUID ()
            ));

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

}