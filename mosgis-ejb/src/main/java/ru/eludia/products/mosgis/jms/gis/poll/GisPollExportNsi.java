package ru.eludia.products.mosgis.jms.gis.poll;

import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import java.sql.SQLException;
import java.util.ArrayList;
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
import ru.eludia.products.mosgis.db.model.incoming.InNsiItem;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocNsiList;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.wsc.WsGisNsiCommonClient;
import ru.eludia.products.mosgis.jmx.NsiLocal;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiItemInfoType;
import ru.gosuslugi.dom.schema.integration.nsi_common.GetStateResult;
import ru.gosuslugi.dom.schema.integration.nsi_common_service_async.Fault;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportNsiQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportNsi extends UUIDMDB<OutSoap> {

    @EJB
    protected WsGisNsiCommonClient wsGisNsiCommonClient;

    @EJB
    NsiLocal nsi;
    
    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ()
            .get    (getTable (), uuid, "*")
        ;        
    }
    
    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {       
                
        try {            
            
            final GetStateResult rp = wsGisNsiCommonClient.getState ((UUID) r.get ("uuid_ack"));
            
            final String listGroup = rp.getNsiList ().getListGroup ();
            
            List vocNsiList = new ArrayList ();
            List inNsiItem  = new ArrayList ();
            
            for (NsiItemInfoType i: rp.getNsiList ().getNsiItemInfo ()) {
                
                vocNsiList.add (HASH (                        
                    "registrynumber", i.getRegistryNumber (),
                    "name",           i.getName (),
                    "listgroup",      listGroup                        
                ));
                
                inNsiItem.add (HASH (                        
                    "uuid", UUID.randomUUID (),
                    "uuid_in_nsi_group", uuid,
                    "registrynumber", i.getRegistryNumber ()
                ));
                
            }
            
            db.upsert (VocNsiList.class, vocNsiList, null);
            
            db.insert (InNsiItem.class, inNsiItem);
            
            db.update (getTable (), HASH (
                "uuid",      uuid,
                "id_status", DONE.getId ()
            ));
            
            if ("NSI".equals (listGroup)) nsi.checkForPending ();

        }        
        catch (Fault ex) {
            logger.log (Level.SEVERE, null, ex);
        }
    }
}