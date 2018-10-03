package ru.eludia.products.mosgis.jms.gis.send;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;
import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonString;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.tables.OrganizationWork;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.tables.OrganizationWorkLog;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.ejb.wsc.WsGisNsiClient;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.gosuslugi.dom.schema.integration.base.AckRequest;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiRef;
import ru.gosuslugi.dom.schema.integration.nsi_service_async.Fault;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inNsiOrganizationWorksQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ExportOrganizationWorkLog extends UUIDMDB<OrganizationWorkLog> {

    private final Logger logger = Logger.getLogger (ExportOrganizationWorkLog.class.getName ());

    @EJB
    WsGisNsiClient wsGisNsiClient;
    
    @Resource (mappedName = "mosgis.outExportNsiOrganizationWorksQueue")
    Queue queue;
    
    @EJB
    protected UUIDPublisher UUIDPublisher;
    
    @Override
    protected Get get (UUID uuid) {
        
        final MosGisModel model = ModelHolder.getModel ();
        
        try (DB db = model.getDb ()) {
            
            return (Get) model
                .get (getTable (), uuid, "AS root", "*")
                .toOne (NsiTable.getNsiTable (56), "guid").on ("(root.code_vc_nsi_56=vc_nsi_56.code AND vc_nsi_56.isactual=1)")
            ;

        }
        catch (SQLException ex) {
            throw new IllegalStateException (ex);
        }
        
    }

    @Override
    protected void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {

        final MosGisModel model = ModelHolder.getModel ();

        Map<String, Object> data = db.getMap (model
            .get   (OrganizationWork.class, r.get ("uuid_object"))
            .toOne (VocOrganization.class, "AS org", "orgppaguid").on ()
        );

        try (Reader sr = new StringReader ((String) r.get ("codes_vc_nsi_67"))) {

            try (JsonReader jr = Json.createReader (sr)) {                
                
                final Object [] codes = jr.readArray ().stream ().map ((t) -> ((JsonString) t).getString ()).toArray ();
                
                int len = codes.length;
                
                List <NsiRef> refs = len == 0 ? Collections.EMPTY_LIST : new ArrayList <> (len);
                
                if (len > 0) db.forEach (model.select (NsiTable.getNsiTable (67), "code", "guid")
                        .and ("code IN", codes)
                        .and ("isactual", 1)
                    , (rs) -> {                        
                        refs.add (NsiTable.toDom (rs.getString (1), DB.to.UUID (rs.getBytes (2))));                                                
                    }
                        
                );
                
                r.put ("codes_vc_nsi_67", refs);
                
            }

        }
        catch (IOException ex) {
            throw new IllegalStateException (ex);
        }
        

logger.info ("data = " + data);
        
        UUID orgPPAGuid = (UUID) data.get ("org.orgppaguid");        

        AckRequest.Ack ack;

        try {
            ack = wsGisNsiClient.importOrganizationWorks (orgPPAGuid, r);
        }
        catch (Fault ex) {
            logger.log (Level.SEVERE, null, ex);
            return;
        }

        db.update (OutSoap.class, DB.HASH (
            "uuid",     ack.getRequesterMessageGUID (),
            "uuid_ack", ack.getMessageGUID ()
        ));

        db.update (getTable (), DB.HASH (
            "uuid",          uuid,
            "uuid_out_soap", ack.getRequesterMessageGUID (),
            "uuid_message",  ack.getMessageGUID ()
        ));
        
        UUIDPublisher.publish (queue, ack.getRequesterMessageGUID ());

    }

}