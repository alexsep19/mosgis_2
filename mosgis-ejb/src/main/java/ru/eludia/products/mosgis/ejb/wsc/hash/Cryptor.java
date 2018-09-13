package ru.eludia.products.mosgis.ejb.wsc.hash;

public class Cryptor {
    
    public final static long TOP_UINT = 0xFFFFFFFFL + 1L;
    public final static int TOP_UBYTE = 0xFF + 1;
    
    final int k[] = new int[8];

    final int k87[] = new int[256];
    final int k65[] = new int[256];
    final int k43[] = new int[256];
    final int k21[] = new int[256];

    void init (SubstitutionBlock b) {
        
        for (int i = 0; i < 256; i++) {
            k87[i] = (b.k8[i >>> 4] << 4 | b.k7[i & 15]) << 24;
            k65[i] = (b.k6[i >>> 4] << 4 | b.k5[i & 15]) << 16;
            k43[i] = (b.k4[i >>> 4] << 4 | b.k3[i & 15]) << 8;
            k21[i] = (b.k2[i >>> 4] << 4 | b.k1[i & 15]);
        }
    }
    
    static long intToLong (int n) {
        
        if (n >= 0) { return (long) n; }
        else { return TOP_UINT + n; }
    }
  
    static int byteToInt(byte n) {
        
        if ( n >= 0 ) { return (int) n; }
        else { return TOP_UBYTE + n; }
    }

    /* Low-level encryption routine - encrypts one 64 bit block (GOST 28147)*/
    final void crypt (byte[] in, int inpos, byte[] out, int outpos) {
        
        int n1, n2;
      
        n1 = byteToInt(in[inpos + 0])        |
            (byteToInt(in[inpos + 1]) <<  8) |
            (byteToInt(in[inpos + 2]) << 16) |
            (byteToInt(in[inpos + 3]) << 24);
        
        n2 = byteToInt(in[inpos + 4])        |
            (byteToInt(in[inpos + 5]) <<  8) |
            (byteToInt(in[inpos + 6]) << 16) |
            (byteToInt(in[inpos + 7]) << 24);

        n2 ^= f (n1, k[0]); n1 ^= f (n2, k[1]);
        n2 ^= f (n1, k[2]); n1 ^= f (n2, k[3]);
        n2 ^= f (n1, k[4]); n1 ^= f (n2, k[5]);
        n2 ^= f (n1, k[6]); n1 ^= f (n2, k[7]);

        n2 ^= f (n1, k[0]); n1 ^= f (n2, k[1]);
        n2 ^= f (n1, k[2]); n1 ^= f (n2, k[3]);
        n2 ^= f (n1, k[4]); n1 ^= f (n2, k[5]);
        n2 ^= f (n1, k[6]); n1 ^= f (n2, k[7]);

        n2 ^= f (n1, k[0]); n1 ^= f (n2, k[1]);
        n2 ^= f (n1, k[2]); n1 ^= f (n2, k[3]);
        n2 ^= f (n1, k[4]); n1 ^= f (n2, k[5]);
        n2 ^= f (n1, k[6]); n1 ^= f (n2, k[7]);

        n2 ^= f (n1, k[7]); n1 ^= f (n2, k[6]);
        n2 ^= f (n1, k[5]); n1 ^= f (n2, k[4]);
        n2 ^= f (n1, k[3]); n1 ^= f (n2, k[2]);
        n2 ^= f (n1, k[1]); n1 ^= f (n2, k[0]);

        out[outpos + 0] = (byte)( n2 & 0xff);
        out[outpos + 1] = (byte)((n2 >>>  8) & 0xff);
        out[outpos + 2] = (byte)((n2 >>> 16) & 0xff);
        out[outpos + 3] = (byte)( n2 >>> 24);
        out[outpos + 4] = (byte)( n1 & 0xff);
        out[outpos + 5] = (byte)((n1 >>>  8) & 0xff);
        out[outpos + 6] = (byte)((n1 >>> 16) & 0xff);
        out[outpos + 7] = (byte)( n1 >>> 24);
    }
    
    /* Part of GOST 28147 algorithm moved into separate function */
    final int f (int n, int x)  {
        
        long tmp = intToLong (n) + intToLong (x);
        
        if ( tmp >= TOP_UINT )
            tmp -= TOP_UINT;
        
        x = k87[ ((int)(tmp >>> 24)) & 255] |
            k65[ ((int)(tmp >>> 16)) & 255] |
            k43[ ((int)(tmp >>>  8)) & 255] |
            k21[ ((int)tmp) & 255];
        
        return ((int)(intToLong (x) << 11)) | (x >>> (32 - 11));
    }

    /* Set 256 bit  key into context */
    final void setKey (byte[] xk) {
        
        int i, j;
        
        for(i = 0, j = 0; i < 8; i++, j += 4) {
            k[i] = byteToInt (xk[j]) |
                  (byteToInt (xk[j + 1]) <<  8 ) |
                  (byteToInt (xk[j + 2]) << 16 ) |
                  (byteToInt (xk[j + 3]) << 24 );
        }
    }

    final void encrypt (byte[] key, byte[] inblock, int inpos, byte[] outblock, int outpos) {
        
        setKey(key);
        crypt(inblock, inpos, outblock, outpos);
    }
}