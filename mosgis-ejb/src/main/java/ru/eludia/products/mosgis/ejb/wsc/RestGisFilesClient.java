package ru.eludia.products.mosgis.ejb.wsc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateless;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import javax.mail.internet.MimeUtility;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import javax.ws.rs.core.Response;
import java.util.Base64;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.DatatypeConverter;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;
import ru.eludia.products.mosgis.jmx.Conf;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class RestGisFilesClient {
    
    @EJB
    public UUIDPublisher uuidPublisher;

    @Resource (mappedName = "mosgis.outExportHouseMgmtContractFilesQueue")
    Queue outExportHouseMgmtContractFilesQueue;    
    
    public void download (final UUID uuid) {
        uuidPublisher.publish (outExportHouseMgmtContractFilesQueue, uuid);
    }
    
    private class Authenticator implements ClientRequestFilter {

        public void filter (ClientRequestContext requestContext) throws IOException {
            MultivaluedMap<String, Object> headers = requestContext.getHeaders ();
            final String basicAuthentication = getBasicAuthentication ();
            if (basicAuthentication == null) return;
logger.info ("setting Authorization=" + basicAuthentication);
            headers.add ("Authorization", basicAuthentication);
        }

        private String getBasicAuthentication () {
            
            final String login = Conf.get (VocSetting.i.WS_GIS_BASIC_LOGIN);
            
            if (!DB.ok (login)) return null;
            
            final String password = Conf.get (VocSetting.i.WS_GIS_BASIC_PASSWORD);            
            
            String token = login + ":" + password;
            
            try {               
                return "BASIC " + DatatypeConverter.printBase64Binary (token.getBytes ("UTF-8"));
            } 
            catch (UnsupportedEncodingException ex) {
                throw new IllegalStateException("Cannot encode with UTF-8", ex);
            }
            
        }
        
    }    
    
    private static final Logger logger = Logger.getLogger (RestGisFilesClient.class.getName ());

    public static final int CHUNK_SIZE = 5242880;
    
    private Client client;
            
    @PostConstruct
    public void init () {
        
        client = ClientBuilder.newClient ();
        
        client.register (new Authenticator ());        
        
        client.register ((ClientRequestFilter) (ClientRequestContext requestContext) -> {
            logger.info ("rq: " + requestContext.getStringHeaders ());
        });
        
        client.register ((ClientResponseFilter) (ClientRequestContext requestContext, ClientResponseContext responseContext) -> {
            logger.info ("rp: " + responseContext.getHeaders ());
        });

    }

    @PreDestroy
    public void done () {
        client.close ();
    }

    private WebTarget getWebTarget (Context context, UUID uploadId) {
        return getWebTarget (context, uploadId, "");
    }
    
    private WebTarget getWebTarget (Context context, UUID uploadId, String postfix) {

        client.property ("jersey.config.client.connectTimeout", Conf.getInt (VocSetting.i.WS_GIS_FILES_TMT_CONN));
        client.property ("jersey.config.client.readTimeout", Conf.getInt (VocSetting.i.WS_GIS_FILES_TMT_RESP));
                
        StringBuilder sb = new StringBuilder (Conf.get (VocSetting.i.WS_GIS_FILES_URL));
        if (sb.charAt (sb.length () - 1) != '/') sb.append ('/');
        sb.append (context.name ().toLowerCase ());
        sb.append ('/');        
        if (uploadId != null) {
            sb.append (uploadId.toString ());
            sb.append ('/');
        }
        
        sb.append (postfix);

        return client.target (sb.toString ());

    }

    private long getPartCount (long len) {
        long result = len / CHUNK_SIZE;
        if (len % CHUNK_SIZE != 0) result ++;
        return result;
    }
        
    public UUID getUploadId (UUID orgPPAGUID, Context context, String name, long len) throws Exception {
        
        final Response rp = getWebTarget (context, null)
            .queryParam ("upload")
            .request ()
            .header ("X-Upload-OrgPPAGUID", orgPPAGUID.toString ())
            .header ("X-Upload-Filename", MimeUtility.encodeWord (name))
            .header ("X-Upload-Length", len)
            .header ("X-Upload-Part-Count", getPartCount (len))
            .build  ("POST")
            .invoke ();
        
        if (rp.getStatus () != 200) {
            throw new Exception ("Can't start upload. Response code is " + rp.getStatus () + ". Headers are " + rp.getHeaders ());
        }
        
        try {
            return UUID.fromString (rp.getStringHeaders ().getFirst ("X-Upload-UploadID"));
        }
        catch (Exception ex) {
            logger.severe ("OK, but invalid response returned: " + rp.getHeaders ());
            throw ex;
        }
        
    }
    
    private String md5b64 (byte [] b, int len) throws NoSuchAlgorithmException {
        final MessageDigest md5 = MessageDigest.getInstance ("MD5");        
        md5.update (b, 0, len);
        return Base64.getEncoder ().encodeToString (md5.digest ());
    }
    
    public Response get (UUID orgPPAGUID, Context context, UUID uploadId) {
        
        return getWebTarget (context, uploadId, "?getfile")
            .request ()
            .header ("X-Upload-OrgPPAGUID", orgPPAGUID.toString ())
            .get ();

    }
    
    public UUID sendFull (UUID orgPPAGUID, Context context, String name, byte [] b, int len) throws Exception {
                
        final Response rp = getWebTarget (context, null)
            .request ()
            .header ("Content-Length", len)
            .header ("Content-MD5", md5b64 (b, len))
            .header ("X-Upload-OrgPPAGUID", orgPPAGUID.toString ())
            .header ("X-Upload-Filename", MimeUtility.encodeWord (name))
            .put (Entity.entity (new ByteArrayInputStream (b, 0, len), APPLICATION_OCTET_STREAM));
        
        if (rp.getStatus () != 200) {
            throw new Exception ("Can't start upload. Response code is " + rp.getStatus () + ". Headers are " + rp.getHeaders ());
        }
        
        try {
            return UUID.fromString (rp.getStringHeaders ().getFirst ("X-Upload-UploadID"));
        }
        catch (Exception ex) {
            logger.severe ("OK, but invalid response returned: " + rp.getHeaders ());
            throw ex;
        }
        
    }    
                        
    public void sendPart (UUID orgPPAGUID, Context context, UUID uploadId, int n, byte [] b, int len) throws Exception {
                
        final Response rp = getWebTarget (context, uploadId)
            .request ()
            .header ("Content-Length", len)
            .header ("Content-MD5", md5b64 (b, len))
            .header ("X-Upload-Partnumber", n)
            .header ("X-Upload-OrgPPAGUID", orgPPAGUID.toString ())
            .put (Entity.entity (new ByteArrayInputStream (b, 0, len), APPLICATION_OCTET_STREAM));
        
        if (rp.getStatus () != 200) {
            throw new Exception ("Can't start upload. Response code is " + rp.getStatus () + ". Headers are " + rp.getHeaders ());
        }
        
    }
    
    public void closeUpload (UUID orgPPAGUID, Context context, UUID uploadId) throws Exception {
        
        final Response rp = getWebTarget (context, uploadId)
            .queryParam ("completed")
            .request ()
            .header ("X-Upload-OrgPPAGUID", orgPPAGUID.toString ())
            .build  ("POST")
            .invoke ();
        
        if (rp.getStatus () != 200) {
            throw new Exception ("Can't finish upload. Response code is " + rp.getStatus () + ". Headers are " + rp.getHeaders ());
        }
        
    }
    
    public enum Context {
        
        HOMEMANAGEMENT,         // Подсистема Управление домами, Лицевые счета
        RKI,                    // Подсистема Реестр коммунальной инфраструктуры
        VOTING,                 // Подсистема Голосования
        INSPECTION,             // Подсистема Инспектирование жилищного фонда
        INFORMING,              // Подсистема Оповещения
        BILLS,                  // Подсистема Электронные счета
        LICENSES,               // Подсистема Лицензии
        AGREEMENTS,             // Подсистема Договора (ДУ, уставы, ДПОИ)
        NSI,                    // Подсистема Нормативно-справочная информации
        DISCLOSURE,             // Подсистема Раскрытие деятельности УО
        CAPITALREPAIRPROGRAMS,  // Подсистема Капитальный ремонт
        MSP;                    // Подсистема Меры социальной поддержки        
        
    }
    
}
