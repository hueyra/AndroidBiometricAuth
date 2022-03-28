package com.github.hueyra.biometricauth.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util {
    private static final String TAG = "SoterDemo.DemoUtil";

    public static boolean isNullOrNil(final String object) {
        return (object == null) || (object.length() <= 0);
    }

    public static boolean isNullOrNil(final byte[] object) {
        return (object == null) || (object.length <= 0);
    }

    public static boolean isNullOrNil(final int[] object) {
        return (object == null) || (object.length <= 0);
    }

    // Only for demo use. Do not use only md5 as the password digest.
    public static String calcPwdDigest(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean saveTextToFile(String content, String fileName) {
        MyLogger.d(TAG, "hy: saving text");
        if (isNullOrNil(fileName)) {
            return false;
        }
        if (isNullOrNil(content)) {
            return false;
        }
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        FileWriter writer = null;
        try {
            writer = new FileWriter(fileName, false);
            writer.write(content);
            writer.flush();
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean saveBinaryToFile(byte[] bin, String fileName) {
        MyLogger.d(TAG, "hy: saving binary");
        if (isNullOrNil(fileName)) {
            return false;
        }
        if (isNullOrNil(bin)) {
            return false;
        }
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        FileOutputStream outputStream = null;
        try {
            file.createNewFile();
            outputStream = new FileOutputStream(fileName, false);
            outputStream.write(bin);
            outputStream.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
