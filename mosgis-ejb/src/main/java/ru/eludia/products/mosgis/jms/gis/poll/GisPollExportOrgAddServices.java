package ru.eludia.products.mosgis.jms.gis.poll;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.base.db.util.TypeConverter;
import ru.eludia.products.mosgis.db.model.tables.AdditionalService;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.tables.AdditionalServiceLog;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.gosuslugi.dom.schema.integration.nsi_service_async.Fault;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationLog;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisNsiClient;
import ru.gosuslugi.dom.schema.integration.nsi.GetStateResult;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiItemType;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState.i.OK;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementType;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.outExportOrgAddServicesQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportOrgAddServices extends GisPollMDB {

    @EJB
    WsGisNsiClient wsGisNsiClient;

    @Override
    protected Get get (UUID uuid) {

        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")
            .toOne (VocOrganizationLog.class, "AS log", "uuid", "action", "uuid_object", "uuid_user").on ("log.uuid_out_soap=root.uuid")
            .toOne (VocOrganization.class, "AS org", VocOrganization.c.ORGPPAGUID.lc () + " AS ppa").on ("log.uuid_object=org.uuid")
        ;
        
    }

    @Override
    protected void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        if (DB.ok (r.get ("is_failed"))) throw new IllegalStateException (r.get ("err_text").toString ());
        
        UUID orgPPAGuid = (UUID) r.get ("ppa");
                        
        try {            
            
            GetStateResult state = getState (orgPPAGuid, r);
            
            NsiItemType nsiItem = state.getNsiItem ();
            
            if (nsiItem == null) throw new GisPollException ("0", "Сервис ГИС вернул пустой результат");

            Map<Object, Map<String, Object>> vocOkeiIdx = db.getIdx(db.getModel().select(VocOkei.class, "*"));

            for (NsiElementType i: nsiItem.getNsiElement()) {

                final Map<String, Object> h = AdditionalService.toHASH(i);
                h.put("uuid_org", r.get("log.uuid_object"));
                h.put("id_status", OK.getId());

                if (h.get("okei") != null && vocOkeiIdx.get(h.get("okei")) == null) {

                    db.upsert(VocOkei.class, DB.HASH(
                        "code", h.get("okei"),
                        "name", h.get("okei"),
                        "national", h.get("okei")
                    ), "code");

                    vocOkeiIdx.put(h.get("okei"), DB.HASH("code", h.get("okei")));

                }

                UUID uuidAddUuiditionalService = TypeConverter.UUIDFromHex(db.upsertId(AdditionalService.class, h, AdditionalService.c.ELEMENTGUID.lc()));

                db.insert(AdditionalServiceLog.class, DB.HASH(
                    "elementguid_new", h.get("elementguid"),
                    "uuid_out_soap", uuid,
                    "uuid_object", uuidAddUuiditionalService,
                    "action", r.get("log.action"),
                    "uuid_user", r.get("log.uuid_user")
                ));
                        
            }

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
