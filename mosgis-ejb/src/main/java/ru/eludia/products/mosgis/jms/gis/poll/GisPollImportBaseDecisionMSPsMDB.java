package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.incoming.InBaseDecisionMSP;
import ru.eludia.products.mosgis.db.model.incoming.InVocDiff;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.gosuslugi.dom.schema.integration.nsi_service_async.Fault;
import ru.eludia.products.mosgis.db.model.tables.BaseDecisionMSP;
import ru.eludia.products.mosgis.db.model.tables.BaseDecisionMSPLog;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisNsiClient;
import ru.gosuslugi.dom.schema.integration.nsi.GetStateResult;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiItemType;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outImportNsiBaseDecisionMSPsQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollImportBaseDecisionMSPsMDB extends GisPollMDB {

    @EJB
    WsGisNsiClient wsGisNsiClient;
    
    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")
                                            .toOne (VocOrganization.class, "AS org", "uuid AS uuid_org").on ("org.orgppaguid = root.orgppaguid");
    }

    @Override
    protected void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        UUID orgPPAGuid = (UUID) r.get ("orgppaguid");
                        
        try {
            
            GetStateResult state = getState (orgPPAGuid, r);
            
            NsiItemType nsiItem = state.getNsiItem ();
            
            if (nsiItem == null) throw new GisPollException ("0", "Сервис ГИС вернул пустой результат");
            
            List <String> guids = new ArrayList <> ();
            List <Map <String, Object>> items = new ArrayList <> ();
            
            for (NsiElementType nsiElement: nsiItem.getNsiElement ()) {
                final Map<String, Object> h = BaseDecisionMSP.toHASH (nsiElement);
                h.put ("uuid_org", r.get ("uuid_org"));
                h.put ("id_status", VocAsyncEntityState.i.OK.getId ());
                h.put ("id_ctr_status", VocGisStatus.i.APPROVED.getId ());
                h.put ("id_ctr_status_gis", VocGisStatus.i.APPROVED.getId ());
                items.add (h);
                guids.add (h.get (BaseDecisionMSP.c.GUID.lc()).toString ());
            }
            
            db.upsert (BaseDecisionMSP.class, items, BaseDecisionMSP.c.GUID.lc());
            
            Map <Object, Map <String, Object>> idxs = db.getIdx(
                    ModelHolder.getModel ().select (BaseDecisionMSP.class, "uuid")
                                           .where  (BaseDecisionMSP.c.GUID.lc() + " IN", guids.toArray ())
            );
            
            for (Object uuid_object: idxs.keySet ()) {
                
                String id_log = db.insertId(BaseDecisionMSPLog.class, DB.HASH(
		    "action", VocAction.i.IMPORT_BASE_DECISION_MSPS.getName (),
		    "uuid_object", uuid_object,
		    "uuid_out_soap", uuid
                )).toString ();
                
                db.update (BaseDecisionMSP.class, DB.HASH (
                        "uuid", uuid_object,
                        "id_log", id_log
                ));
            }

	    db.update(OutSoap.class, HASH(
		"uuid", uuid,
		"id_status", DONE.getId()
	    ));

	    db.update(InBaseDecisionMSP.class, HASH(
		"uuid", uuid,
		InBaseDecisionMSP.c.IS_OVER, 1
	    ));
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
