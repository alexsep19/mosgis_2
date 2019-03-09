package ru.eludia.products.mosgis.ws.rest.clients.tools.hash;

import java.util.Arrays;
import static ru.eludia.products.mosgis.ws.rest.clients.tools.hash.Cryptor.byteToInt;

public class HashContext {
    
    Cryptor cryptor = null;
    
    long length = 0;
    int left = 0;
    
    final byte H[] = new byte[32];
    final byte S[] = new byte[32];
    final byte remainder[] = new byte[32];

    static void xorBlocks (byte[] result, byte[] a, byte[] b, int bstart, int len) {
        
        for (int i = 0; i < len; ++i) {
            result[i] = (byte)(a[i] ^ b[bstart+i]);
        }
    }

    static void swapBytes (byte[] w, byte[] k) {
        
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 8; ++j) {
                k[i + 4 * j] = w[8 * i + j];
            }
        }
    }

    static void circleXor8 (byte[] w, byte[] k) {
        
        circleXor8 (w, 0, k);
    }
    
    static void circleXor8 (byte[] w, int wstart, byte[] k) {
        
        final byte[] buf = new byte[8];
        
        System.arraycopy (w, wstart, buf, 0, 8);
        System.arraycopy (w, wstart+8, k, 0, 24);
        
        for (int i = 0; i < 8; ++i)
            k[i + 24] = (byte) (buf[i] ^ k[i]);
    }

    static void transform3 (byte[] data) {
        final int acc = 
            (byteToInt(data[0]) ^ byteToInt(data[2]) ^ byteToInt(data[4]) ^ byteToInt(data[6]) ^ byteToInt(data[24]) ^ byteToInt(data[30])) |
            ((byteToInt(data[1]) ^ byteToInt(data[3]) ^ byteToInt(data[5]) ^ byteToInt(data[7]) ^ byteToInt(data[25]) ^ byteToInt(data[31])) << 8);
    
        System.arraycopy (data, 2, data, 0, 30);
    
        data[30] = (byte) (acc & 0xff);
        data[31] = (byte) (acc >>> 8);
    }

    static int addBlocks (int n, byte[] left, byte[] right, int rightPos) {
        int carry = 0;
        int sum;
        
        for (int i = 0; i < n; i++) {
            sum = byteToInt (left[i]) + byteToInt (right[rightPos + i]) + carry;
            left[i] = (byte) (sum & 0xff);
            carry = sum >>> 8;
        }
        
        return carry;
    }
    
    /**
     * cleans up temporary structures and
     * set up substitution blocks
     */
    void init (SubstitutionBlock substBlock) {
        
        length = 0;
        cryptor = null;
        left = 0;
        
        Arrays.fill(H,         (byte) 0);
        Arrays.fill(S,         (byte) 0);
        Arrays.fill(remainder, (byte) 0);

        cryptor = new Cryptor();
        cryptor.init(substBlock);
    }

    /**
     * reset state of hash context to begin hashing new message
     */
    final void startHash () {
        
        length = 0L;
        left = 0;
        
        Arrays.fill(H, (byte) 0);
        Arrays.fill(S, (byte) 0);
    }

    /**
     * Hash block of arbitrary length
     */
    final void hashBlock (byte[] block, int pos, int length) {
        
        final int lastPos = pos + length;
        
        if ( left > 0 ) {
            int addBytes = 32 - left;
            
            if ( addBytes > length )
                addBytes = length;
        
            System.arraycopy (block, pos, remainder, left, addBytes);
            left += addBytes;
        
            if ( left < 32 )
                return;
        
            pos += addBytes;
            hashStep (H, remainder, 0);
            addBlocks (32, S, remainder, 0);
            this.length += 32;
            left = 0;
        }

        while ( lastPos - pos >= 32 ) {
            hashStep(H, block, pos);
            addBlocks(32, S, block, pos);
            this.length += 32;
            pos += 32;
        }

        if ( pos != length ) {
            left = lastPos - pos;
            System.arraycopy(block, pos, remainder, 0, left);
        }
    }

    final void hashBlock (byte[] data, int len) {
        hashBlock(data, 0, len);
    }
    final void hashBlock (byte[] data) {
        hashBlock(data, 0, data.length);
    }

    /**
     * Compute hash value from current state of context
     * state of cryptor becomes invalid and cannot be used for further
     * hashing.
     */
    final byte[] finishHash () {
        
        final byte[] buf = new byte[32];
        final byte[] xH = new byte[32];
        final byte[] xS = new byte[32];
        long finalLength = length;

        System.arraycopy (H, 0, xH, 0, 32);
        System.arraycopy (S, 0, xS, 0, 32);
        Arrays.fill(buf, (byte) 0);

        if ( left > 0 ) {
            System.arraycopy (remainder, 0, buf, 0, left);
            hashStep (xH, buf, 0);
            addBlocks (32, xS, buf, 0);
            finalLength += left;
            Arrays.fill (buf, (byte)0);
        }

        finalLength <<= 3;
        int bptr = 0;
        while ( finalLength > 0 ) {
            buf[bptr++] = (byte)(finalLength & 0xFF);
            finalLength >>>= 8;
        }

        hashStep (xH, buf, 0);
        hashStep (xH, xS, 0);
        
        return xH;
    }

    final void hashStep (byte[] xH, byte[] xM, int mstart) {
        
        final byte[] xU = new byte[32];
        final byte[] xW = new byte[32];
        final byte[] xV = new byte[32];
        final byte[] xS = new byte[32];
        final byte[] Key = new byte[32];
        
        int i;
        
        /* Compute first key */
        xorBlocks (xW, xH, xM, mstart, 32);
        swapBytes (xW, Key);
        
        /* Encrypt first 8 bytes of H with first key*/
        cryptor.encrypt (Key, xH, 0, xS, 0);
      
        /* Compute second key*/
        circleXor8 (xH, xU);
        circleXor8 (xM, mstart, xV);
        circleXor8 (xV, xV);
        xorBlocks (xW, xU, xV, 0, 32);
        swapBytes (xW, Key);
      
        /* encrypt second 8 bytes of H with second key*/
        cryptor.encrypt (Key, xH, 8, xS, 8);
      
        /* compute third key */
        circleXor8 (xU, xU);

        xU[31]=(byte) ~xU[31];
        xU[29]=(byte) ~xU[29];
        xU[28]=(byte) ~xU[28];
        xU[24]=(byte) ~xU[24];
        xU[23]=(byte) ~xU[23];
        xU[20]=(byte) ~xU[20];
        xU[18]=(byte) ~xU[18];
        xU[17]=(byte) ~xU[17];
        xU[14]=(byte) ~xU[14];
        xU[12]=(byte) ~xU[12];
        xU[10]=(byte) ~xU[10];
        xU[ 8]=(byte) ~xU[ 8];
        xU[ 7]=(byte) ~xU[ 7];
        xU[ 5]=(byte) ~xU[ 5];
        xU[ 3]=(byte) ~xU[ 3];
        xU[ 1]=(byte) ~xU[ 1];

        circleXor8 (xV, xV);
        circleXor8 (xV, xV);
        xorBlocks (xW, xU, xV, 0, 32);
        swapBytes (xW,Key);
      
        /* encrypt third 8 bytes of H with third key*/
        cryptor.encrypt (Key, xH, 16, xS, 16);
      
        /* Compute fourth key */
        circleXor8 (xU, xU);
        circleXor8 (xV, xV);
        circleXor8 (xV, xV);
        xorBlocks (xW, xU, xV, 0, 32);
        swapBytes (xW,Key);
      
        /* Encrypt last 8 bytes with fourth key */
        cryptor.encrypt(Key, xH, 24, xS, 24);
        for (i = 0; i < 12; i++)
            transform3 (xS);
        xorBlocks (xS, xS, xM, mstart, 32);
        transform3 (xS);
        xorBlocks (xS, xS, xH, 0, 32);
        for (i = 0; i < 61; i++)
            transform3 (xS);
        System.arraycopy (xS, 0, xH, 0, 32);
    }
}