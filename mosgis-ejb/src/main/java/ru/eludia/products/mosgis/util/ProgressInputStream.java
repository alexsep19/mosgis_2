package ru.eludia.products.mosgis.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import ru.eludia.base.db.util.JDBCBiConsumer;

public class ProgressInputStream extends FilterInputStream {
    
    long size;
    long step;
    JDBCBiConsumer<Long, Long> callback = null;
    
    long pos;
    long next;
        
    public ProgressInputStream (InputStream in, long size, int steps, JDBCBiConsumer<Long, Long> callback) {
        super (in);
        this.pos = 0;
        this.size = size;
        setSteps (steps);
        this.callback = callback;
    }
    
    public ProgressInputStream (InputStream in, long size, JDBCBiConsumer<Long, Long> callback, long step) {
        super (in);
        this.pos = 0;
        this.size = size;
        setStep (step);
        this.callback = callback;
    }
    
    private void calcNext () {
        next = pos + step;
    }

    public final void setStep (long step) {
        this.step = step;
        calcNext ();
    }
    public final void setSteps (int steps) {
        setStep (size / steps);
    }
    
    private void check () throws IOException {
        if (pos < next) return;
        try {
            callback.accept (pos, size);
        }
        catch (SQLException ex) {
            throw new IOException (ex);
        }
        calcNext ();
    }

    @Override
    public void close () throws IOException {
        super.close ();
        pos = size;
        next = 0;
        check ();
    }

    @Override
    public int read () throws IOException {
        pos ++;
        check ();
        return super.read ();
    }

    @Override
    public int read (byte[] b) throws IOException {
        int result = super.read (b);
        pos += b.length;
        check ();
        return result;
    }

    @Override
    public int read (byte[] b, int off, int len) throws IOException {
        int result = super.read (b, off, len);
        pos += result;
        check ();
        return result;
    }
    
}
