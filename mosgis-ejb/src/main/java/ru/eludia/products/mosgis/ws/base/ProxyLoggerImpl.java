package ru.eludia.products.mosgis.ws.base;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import ru.eludia.base.DB;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.jmx.Conf;
import ru.eludia.products.mosgis.proxy.GisWsAddress;
import ru.eludia.products.mosgis.proxy.ProxyLoggerLocal;

@Stateless
public class ProxyLoggerImpl implements ProxyLoggerLocal {

    private static final Logger logger = Logger.getLogger (ProxyLoggerImpl.class.getName ());

    @Override
    public GisWsAddress getGisWsAddress () {
        return new GisWsAddress (Conf.get (VocSetting.i.WS_GIS_URL_ROOT), Conf.get (VocSetting.i.WS_GIS_BASIC_LOGIN), Conf.get (VocSetting.i.WS_GIS_BASIC_PASSWORD));
    }

    @Override
    public void logRequest (String uuid, String svc, String op, String rq, String orgppaguid, String uuid_ack) {
        
        MosGisModel m = ModelHolder.getModel ();
        
        try (DB db = m.getDb ()) {
            
            db.upsert (OutSoap.class, DB.HASH (
                "uuid", uuid,
                "svc", svc,
                "op", op,
                "rq", rq,
                "orgppaguid", orgppaguid, 
                "uuid_ack", uuid_ack                            
            ));

        }
        catch (Exception e) {
            logger.log (Level.SEVERE, "Can't log async request", e);
        }

    }

    @Override
    public void logPending (String uuid_ack) {
        logger.info ("Doing nothing for " + uuid_ack);
    }

    @Override
    public void logResponse (String uuid_ack, String rp, String err_code, String err_text) {

        MosGisModel m = ModelHolder.getModel ();
        
        try (DB db = m.getDb ()) {
            
            db.update (OutSoap.class, DB.HASH (
                "uuid_ack", uuid_ack,
                "rp", rp,
                "err_code", err_code, 
                "err_text", err_text,
                "is_failed", err_text.isEmpty () ? 0 : 1,
                "id_status", 3,
                "ts_rp", NOW
            ), "uuid_ack");

        }
        catch (Exception e) {
            logger.log (Level.SEVERE, "Can't log async response", e);
        }
        
    }
    
}