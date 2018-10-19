package ru.eludia.products.mosgis.jms.gis.poll.base;

import javax.ejb.EJB;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.gosuslugi.dom.schema.integration.base.BaseAsyncResponseType;

public abstract class GisPollMDB extends UUIDMDB<OutSoap> {

    @EJB
    protected UUIDPublisher UUIDPublisher;
    
    @Override
    protected Class getTableClass () {
        return OutSoap.class;
    }    

    protected void checkIfResponseReady (BaseAsyncResponseType rp) throws GisPollRetryException {
        
        final byte requestState = rp.getRequestState ();
        
        if (requestState >= DONE.getId ()) return;
        
        logger.info ("requestState = " + requestState + ", retrying request");
        
        UUIDPublisher.publish (getOwnQueue (), getUuid ());
        
        throw new GisPollRetryException ();

    }
    
}