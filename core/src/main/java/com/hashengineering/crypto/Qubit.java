package com.hashengineering.crypto;

import com.google.bitcoin.core.Sha512Hash;
import fr.cryptohash.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Hash Engineering on 4/24/14 for the X11 algorithm
 */
public class Qubit {

    private static final Logger log = LoggerFactory.getLogger(Qubit.class);
    private static boolean native_library_loaded = false;

    static {

        try {
            System.loadLibrary("qubit");
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
            return native_library_loaded ? qubit_native(input) : qubit(input);
        } catch (Exception e) {
            return null;
        }
        finally {
            //long time = System.currentTimeMillis()-start;
            //log.info("X11 Hash time: {} ms per block", time);
        }
    }

    static native byte [] qubit_native(byte [] input);


    static byte [] qubit(byte header[])
    {
        //Initialize
        Sha512Hash[] hash = new Sha512Hash[5];

        Luffa512 luffa = new Luffa512();
        hash[0] = new Sha512Hash(luffa.digest(header));

        CubeHash512 cubehash = new CubeHash512();
        hash[1] = new Sha512Hash(cubehash.digest(hash[0].getBytes()));

        SHAvite512 shavite = new SHAvite512();
        hash[2] = new Sha512Hash(shavite.digest(hash[1].getBytes()));

        SIMD512 simd = new SIMD512();
        hash[3] = new Sha512Hash(simd.digest(hash[2].getBytes()));

        ECHO512 echo = new ECHO512();
        hash[4] = new Sha512Hash(echo.digest(hash[3].getBytes()));

        return hash[4].trim256().getBytes();
    }
}
