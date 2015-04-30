package com.washappkorea.corp.cleanbasket.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class HashGenerator {
    public static String makeHash(String input) {
        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            return input;
        }

        md.reset();
        byte[] buffer = input.getBytes();
        md.update(buffer);
        byte[] digest = md.digest();

        String hexStr = "";
        for (int i = 0; i < digest.length; i++) {
            hexStr +=  Integer.toString( ( digest[i] & 0xff ) + 0x100, 16).substring( 1 );
        }

        return hexStr;
    }
}
