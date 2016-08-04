package com.hashengineering.crypto;

import com.google.bitcoin.core.Sha512Hash;
import fr.cryptohash.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Hash Engineering on 8/1/16 for the yescrypt algorithm
 */
public class Yescrypt {

    private static final Logger log = LoggerFactory.getLogger(Yescrypt.class);
    private static boolean native_library_loaded = false;

    static {

        try {
            System.loadLibrary("myriad");
            native_library_loaded = true;
        }
        catch(UnsatisfiedLinkError x)
        {

        }
        catch(Exception e)
        {
            native_library_loaded = false;
        }
    }

    public static byte[] digest(byte[] input, int offset, int length)
    {
        byte [] buf = new byte[length];
        for(int i = 0; i < length; ++i)
        {
            buf[i] = input[offset + i];
        }
        return digest(buf);
    }

    public static byte[] digest(byte[] input) {
        //long start = System.currentTimeMillis();
        try {
            return native_library_loaded ? yescrypt_native(input) : yescrypt(input);
        } catch (Exception e) {
            return null;
        }
        finally {
            //long time = System.currentTimeMillis()-start;
            //log.info("X11 Hash time: {} ms per block", time);
        }
    }

    static native byte [] yescrypt_native(byte [] input);


    static byte [] yescrypt(byte [] input)
    {
        return new byte[32];
    }
}
