package ru.eludia.products.mosgis.jms.base;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import javax.ejb.EJB;
import javax.jms.Queue;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.ws.WsMessages;
import ru.eludia.products.mosgis.jms.UUIDPublisher;
import ru.eludia.products.mosgis.ws.soap.impl.base.Fault;
import ru.eludia.products.mosgis.ws.soap.tools.SOAPTools;
import ru.gosuslugi.dom.schema.integration.base.AckRequest.Ack;

/**
 *
 * @author Aleksei
 */
public abstract class WsProxyMDB extends UUIDMDB<WsMessages>{
    
	@EJB
    protected UUIDPublisher uuidPublisher;
	
	protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "*")
        		.toMaybeOne(VocOrganization.class, "AS org", VocOrganization.c.ORGPPAGUID.lc()).on ();
    }
	
	protected abstract Queue getPollQueue ();
	
	protected abstract JAXBContext getJAXBContext() throws JAXBException;
        
    protected abstract Ack sendRequest (UUID orgPPAGuid, UUID messageGUID, Object request) throws Exception;    
    
	protected void handleRequest(DB db, Map<String, Object> r, Object request) throws Exception {

		try {
			UUID orgPPAGuid = (UUID) r.get("org.orgppaguid");
			UUID messageGUID = (UUID) r.get("uuid");
			
			Ack ack = sendRequest(orgPPAGuid, messageGUID, request);
			
			OutSoap.registerAck(db, ack);
			uuidPublisher.publish(getPollQueue(), ack.getRequesterMessageGUID());
		} catch (Fault ex) {
			logger.log(Level.SEVERE, "Can't invoke WS", ex);
			OutSoap.registerFault(db, r.get(WsMessages.c.UUID.lc()), ex.getFaultInfo());
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Cannot invoke WS", ex);
			OutSoap.registerException(db, r.get(WsMessages.c.UUID.lc()), r.get(WsMessages.c.SERVICE.lc()).toString(),
					r.get(WsMessages.c.OPERATION.lc()).toString(), ex);
		}

	}
    
    @Override
    protected void handleRecord(DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        try {
            r.put(WsMessages.c.ID_STATUS.lc(), VocAsyncRequestState.i.IN_PROGRESS.getId());
            db.update(WsMessages.class, r);

            JAXBContext jaxbContext = getJAXBContext();

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            String request = r.get(WsMessages.c.REQUEST.lc()).toString();
            
            handleRequest (db, r, unmarshaller.unmarshal(SOAPTools.getSoapBodyNode(request)));
            
        } catch (Exception ex) {
        	WsMessages.registerException(db, uuid, ex);
        }

    }
          
}
