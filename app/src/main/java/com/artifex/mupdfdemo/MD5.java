package com.artifex.mupdfdemo;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/* loaded from: classes7.dex */
public class MD5 {
    public static boolean checkMD5(String str, File file) throws IOException {
        String calculateMD5;
        if (str == null || str == "" || file == null || (calculateMD5 = calculateMD5(file)) == null) {
            return false;
        }
        return calculateMD5.equalsIgnoreCase(str);
    }

    public static String calculateMD5(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        try (java.io.InputStream is = new java.io.FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String MD5Hash(String str) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(str.getBytes(), 0, str.length());
            return new BigInteger(1, messageDigest.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }
}
