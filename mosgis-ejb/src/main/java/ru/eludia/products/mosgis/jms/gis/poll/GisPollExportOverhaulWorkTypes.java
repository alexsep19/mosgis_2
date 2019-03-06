package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.incoming.InOverhaulWorkType;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.gosuslugi.dom.schema.integration.nsi_service_async.Fault;
import ru.eludia.products.mosgis.db.model.voc.VocOverhaulWorkType;
import ru.eludia.products.mosgis.ejb.wsc.WsGisNsiClient;
import ru.gosuslugi.dom.schema.integration.nsi.GetStateResult;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiItemType;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportOverhaulWorkTypesQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportOverhaulWorkTypes extends GisPollMDB {

    @EJB
    WsGisNsiClient wsGisNsiClient;

    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ().get (InOverhaulWorkType.class, uuid, "*");
    }

    @Override
    protected void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        if (DB.ok (r.get ("is_failed"))) throw new IllegalStateException (r.get ("err_text").toString ());
        
        UUID orgPPAGuid = (UUID) r.get ("uuid_org");
                        
        try {            
            
            GetStateResult state = getState (orgPPAGuid, r);
            
            NsiItemType nsiItem = state.getNsiItem ();
            
            if (nsiItem == null) throw new GisPollException ("0", "Сервис ГИС вернул пустой результат");
            
            nsiItem.getNsiElement ().stream ().map ((t) -> {
                    final Map<String, Object> h = VocOverhaulWorkType.toHASH (t); 
                    h.put ("uuid_org", r.get ("uuid_org"));
                    return h;
            }).collect (Collectors.toList ());
                        
//            db.upsert (VocOverhaulWorkType.class, 
//                    
//                nsiItem.getNsiElement ().stream ().map ((t) -> {
//                    final Map<String, Object> h = VocOverhaulWorkType.toHASH (t); 
//                    h.put ("uuid_org", r.get ("uuid_org"));
//                    return h;
//                }).collect (Collectors.toList ()),
//
//            "guid");

        }
        catch (GisPollRetryException ex) {
            return;
        }
        catch (GisPollException ex) {            
            ex.register (db, uuid, r);
        }
        
    }

    private GetStateResult getState (UUID orgPPAGuid, Map<String, Object> r) throws GisPollRetryException, GisPollException {

        GetStateResult rp;
        
        try {
            rp = wsGisNsiClient.getState (orgPPAGuid, (UUID) r.get ("uuid_ack"));
        }
        catch (Fault ex) {
            throw new GisPollException (ex.getFaultInfo ());
        }
        catch (Throwable ex) {            
            throw new GisPollException (ex);
        }
        
        checkIfResponseReady (rp);
        
        return rp;
        
    }    
    
}
