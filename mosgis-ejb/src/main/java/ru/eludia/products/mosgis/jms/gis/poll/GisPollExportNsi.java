package ru.eludia.products.mosgis.jms.gis.poll;

import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.concurrent.locks.ReentrantLock;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.build.QP;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.model.voc.VocNsiList;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.wsc.WsGisNsiCommonClient;
import ru.eludia.products.mosgis.jmx.NsiMBean;
import ru.gosuslugi.dom.schema.integration.nsi_common.GetStateResult;
import ru.gosuslugi.dom.schema.integration.nsi_common_service_async.Fault;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportNsiQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportNsi extends UUIDMDB<OutSoap> {
            
    private volatile static boolean nsiOut = false;
    private volatile static boolean nsiraoOut = false;
    
    @EJB
    protected WsGisNsiCommonClient wsGisNsiCommonClient;

    @EJB
    NsiMBean nsi;

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
            
            db.upsert (VocNsiList.class, 
                    
                rp.getNsiList ().getNsiItemInfo ().stream ().map (i -> HASH (
                        
                    "registrynumber", i.getRegistryNumber (),
                    "name",           i.getName (),
                    "listgroup",      listGroup
                        
                )).collect (Collectors.toList ())
                    
            , null);
            
            db.update (getTable (), HASH (
                "uuid",      uuid,
                "id_status", DONE.getId ()
            ));
            
            exportItems(db);

        }        
        catch (Fault ex) {
            logger.log (Level.SEVERE, null, ex);
        }
    }
    
    private void exportItems(DB db) throws SQLException {
        
        ReentrantLock locker = new ReentrantLock();
        
        locker.lock();
        
        try {
            
            if (!nsiOut) nsiOut = true;
            else nsiraoOut = true;
            
            if (nsiraoOut) {
                
                db.forEach(new QP("SELECT registrynumber FROM vc_nsi_list"), rs -> {
                    nsi.importNsiItems (rs.getInt(1));
                });
                
                nsiOut = false;
                nsiraoOut = false;
            }
        }
        finally {
            locker.unlock();
        }
    }
}