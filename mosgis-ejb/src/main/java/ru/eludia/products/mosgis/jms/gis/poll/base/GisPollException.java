package ru.eludia.products.mosgis.jms.gis.poll.base;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.base.Fault;

public class GisPollException extends Exception {
    
    protected Logger logger = java.util.logging.Logger.getLogger (this.getClass ().getName ());
    
    String code;
    String text;

    public GisPollException (String code, String text) {
        super (code + " " + text);
        this.code = code;
        this.text = text;
    }
    
    public GisPollException (ErrorMessageType errorMessage) {
        this (errorMessage.getErrorCode (), errorMessage.getDescription ());
    }
    
    public GisPollException (Fault fault) {
        this (fault.getErrorCode (), fault.getErrorMessage ());
    }        
    
    public GisPollException (Throwable t) {
        this ("0", getRootCauase (t).getMessage ());
    }    
    
    private static Throwable getRootCauase (Throwable t) {
        while (true) {
            Throwable cause = t.getCause ();
            if (cause == null) return t;
            t = cause;
        }
    }
    
    public void register (DB db, UUID uuid, Map<String, Object> r) throws SQLException {

        logger.warning (getMessage ());

        db.update (OutSoap.class, HASH (
            "uuid", uuid,
            "id_status", DONE.getId (),
            "is_failed", 1,
            "err_code",  code,
            "err_text",  text
        ));

    }    
    
}