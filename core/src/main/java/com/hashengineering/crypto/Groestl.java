package com.hashengineering.crypto;

import fr.cryptohash.Groestl512;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Hash Engineering on 4/24/14 for the Groestl algorithm
 */
public class Groestl {

    private static final Logger log = LoggerFactory.getLogger(Groestl.class);
    private static boolean native_library_loaded = false;
    private static final MessageDigest digestSHA256;
    private static final Groestl512 digestGroestl = new Groestl512();

    public static byte[] sha256_digest(byte[] input, int offset, int length) {
        synchronized (digestSHA256) {
            digestSHA256.reset();
            digestSHA256.update(input, offset, length);
            return digestSHA256.digest();
        }
    }
    static {

            try {
                digestSHA256 = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);  // Can't happen.
            }

        try {
            System.loadLibrary("myriad");
            native_library_loaded = true;
        }
        catch(UnsatisfiedLinkError x)
        {
            native_library_loaded = false;
        }
        catch(Exception e)
        {
            native_library_loaded = false;
        }
    }

    public static byte[] digest(byte[] input, int offset, int length)
    {
        //long start = System.currentTimeMillis();
        try {
            return native_library_loaded ? groestl_native(input, offset, length) : groestl(input, offset, length);
        } catch (Exception e) {
            return null;
        }
        finally {
            //long time = System.currentTimeMillis()-start;
            //log.info("groestl Hash time: {} ms per block", time);
        }

   }

    public static byte[] digest(byte[] input) {
        return digest(input, 0, input.length);
    }

    static native byte [] groestl_native(byte [] input, int offset, int len);

    static byte [] groestl(byte header[])
    {
        byte [] hash512 = digestGroestl.digest(header);
        //Initialize
        return sha256_digest(hash512, 0, 64);
    }

    static byte [] groestl(byte header[], int offset, int length)
    {
        digestGroestl.reset();
        digestGroestl.update(header, offset, length);
        byte [] hash512 = digestGroestl.digest();
        //Initialize

        return sha256_digest(hash512, 0, 64);
    }

}
