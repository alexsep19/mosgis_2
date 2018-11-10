package ru.eludia.products.mosgis.proxy;

import javax.ejb.Local;

@Local
public interface ProxyLoggerLocal {
    
    GisWsAddress getGisWsAddress ();
    void logRequest  (String uuid, String svc, String op, String rq, String orgppaguid, String uuid_ack);
    void logPending  (String uuid_ack);
    void logResponse (String uuid_ack, String rp, String err_code, String err_text);

}