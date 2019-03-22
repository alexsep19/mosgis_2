package ru.eludia.products.mosgis.jms.gis.poll.base;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import javax.ejb.EJB;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.jms.UUIDPublisher;
import ru.eludia.products.mosgis.jms.base.UUIDMDB;
import ru.gosuslugi.dom.schema.integration.base.BaseAsyncResponseType;

public abstract class GisPollMDB extends UUIDMDB<OutSoap> {

    @EJB
    public UUIDPublisher uuidPublisher;
    
    @Override
    protected Class getTableClass () {
        return OutSoap.class;
    }    

    protected void checkIfResponseReady (BaseAsyncResponseType rp) throws GisPollRetryException {
        
        final byte requestState = rp.getRequestState ();

        if (requestState >= DONE.getId ()) return;
        
        logger.info ("requestState = " + requestState + ", retrying request");
        
        uuidPublisher.publish (getOwnQueue (), getUuid ());
        
        throw new GisPollRetryException ();

    }
    
    @Override
    protected final void handleRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
                
        if (DB.ok (r.get ("is_failed"))) throw new IllegalStateException (r.get ("err_text").toString ());
        
        handleOutSoapRecord (db, uuid, r);
    
    }

    protected abstract void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException;
    
}