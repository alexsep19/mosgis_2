package ru.eludia.products.mosgis.ws.base;

import javax.ejb.Stateless;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.jmx.Conf;
import ru.eludia.products.mosgis.proxy.GisWsAddress;
import ru.eludia.products.mosgis.proxy.ProxyLoggerLocal;

@Stateless
public class ProxyLoggerImpl implements ProxyLoggerLocal {

    @Override
    public GisWsAddress getGisWsAddress () {
        return new GisWsAddress (Conf.get (VocSetting.i.WS_GIS_URL_ROOT), Conf.get (VocSetting.i.WS_GIS_BASIC_LOGIN), Conf.get (VocSetting.i.WS_GIS_BASIC_PASSWORD));
    }

    @Override
    public void logRequest (String uuid, String svc, String op, String rq, String orgppaguid, String uuid_ack) {
        throw new UnsupportedOperationException ("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void logPending (String uuid_ack) {
        throw new UnsupportedOperationException ("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void logResponse (String uuid_ack, String rp, String err_code, String err_text) {
        throw new UnsupportedOperationException ("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
