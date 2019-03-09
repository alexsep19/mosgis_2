package ru.eludia.products.mosgis.ws.rest.clients.tools;

import ru.eludia.products.mosgis.ws.rest.clients.RestGisFilesClient;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.UUID;
import ru.eludia.base.db.util.JDBCBiConsumer;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.ws.rest.clients.tools.hash.Gost341194;

public class GisRestStream extends OutputStream {
        
    byte [] body = new byte [RestGisFilesClient.CHUNK_SIZE];
    int cnt = 0;
    int part = 1;
    String name;
    long len;
    boolean isLong;
    UUID orgPPAGUID;
    RestGisFilesClient.Context context;
    UUID uploadId;
    JDBCBiConsumer<UUID, String> setId;
    RestGisFilesClient restGisFilesClient;
    Gost341194 gost = new Gost341194 ();

    public GisRestStream (RestGisFilesClient restGisFilesClient, RestGisFilesClient.Context context, UUID orgPPAGUID, String name, long len, JDBCBiConsumer<UUID, String> setId) throws Exception {
        if (len == 0) throw new Exception ("Zero file length is not allowed");
        this.restGisFilesClient = restGisFilesClient;
        this.len = len;
        this.name = name;
        this.orgPPAGUID = orgPPAGUID;
        this.setId = setId;
        this.context = context;
        isLong = (len > RestGisFilesClient.CHUNK_SIZE);
        if (isLong) setUploadId (restGisFilesClient.getUploadId (orgPPAGUID, context, name, len));
    }

    public void setUploadId (UUID uploadId) throws SQLException {
        this.uploadId = uploadId;        
    }
    
    @Override
    public void flush () throws IOException {
        
        try {
            
            if (isLong)
                restGisFilesClient.sendPart (orgPPAGUID, context, uploadId, part ++, body, cnt);
            else            
                setUploadId (restGisFilesClient.sendFull (orgPPAGUID, context, name, body, cnt));
            
        }
        catch (Exception ex) {
            throw new IOException (ex);
        }
        finally {
            cnt = 0;
        }
        
    }

    @Override
    public void write (int b) throws IOException {
        body [cnt++] = (byte) b;
        gost.update ((byte) b);
        if (cnt == RestGisFilesClient.CHUNK_SIZE) flush ();
    }

    @Override
    public void write (byte [] b, int off, int len) throws IOException {

        if (b == null) throw new NullPointerException ();
        if (off < 0 || len < 0 || (off + len) > b.length) throw new IndexOutOfBoundsException ();

        int free = RestGisFilesClient.CHUNK_SIZE - cnt;
        
        gost.update (b, off, len);

        if (len <= free) {

            System.arraycopy (b, off, body, cnt, len);
            cnt += len;
            if (cnt == RestGisFilesClient.CHUNK_SIZE) flush ();

        }
        else {
            
            write (b, off, free);
            off += free;
            len -= free;
            
            while (len > 0) {
                int chunk = len;
                if (chunk > RestGisFilesClient.CHUNK_SIZE) chunk = RestGisFilesClient.CHUNK_SIZE;
                write (b, off, chunk);
                off += chunk;
                len -= chunk;
            }
            
        }

    }

    @Override
    public void close () throws IOException {
        
        flush ();
        
        try {
            
            if (isLong) restGisFilesClient.closeUpload (orgPPAGUID, context, uploadId);
            
            setId.accept (uploadId, DB.to.hex (gost.digest ()));
            
        }
        catch (Exception ex) {
            
            throw new IOException (ex);
            
        }        
        
    }

}
