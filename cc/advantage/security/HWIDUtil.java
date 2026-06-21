/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public final class HWIDUtil {
    private static final String SECRET_KEY = "a1b2c3d4e5f6g7h8";

    private HWIDUtil() {
    }

    public static String getHWID() {
        String raw = HWIDUtil.getRawIdentifier();
        return HWIDUtil.sha256(raw);
    }

    public static String getEncryptedHWID() {
        try {
            String rawHWID = HWIDUtil.getHWID();
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(1, key);
            byte[] encrypted = cipher.doFinal(rawHWID.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        }
        catch (Exception e) {
            return "unknown";
        }
    }

    private static String getRawIdentifier() {
        try {
            Class<?> buildClass = Class.forName("android.os.Build");
            String serial = (String)buildClass.getField("SERIAL").get(null);
            if (serial != null && !serial.isEmpty() && !serial.equalsIgnoreCase("unknown")) {
                return "android-serial-" + serial;
            }
            String board = (String)buildClass.getField("BOARD").get(null);
            String brand = (String)buildClass.getField("BRAND").get(null);
            String model = (String)buildClass.getField("MODEL").get(null);
            String device = (String)buildClass.getField("DEVICE").get(null);
            return "android-" + board + "-" + brand + "-" + model + "-" + device;
        }
        catch (ClassNotFoundException buildClass) {
        }
        catch (Exception buildClass) {
            // empty catch block
        }
        String os = System.getProperty("os.name", "");
        String arch = System.getProperty("os.arch", "");
        String cores = String.valueOf(Runtime.getRuntime().availableProcessors());
        String host = System.getenv("COMPUTERNAME");
        if (host == null) {
            host = System.getenv("HOSTNAME");
        }
        if (host == null) {
            host = "unknown-host";
        }
        return os + arch + cores + host;
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        }
        catch (NoSuchAlgorithmException e) {
            return "hash-error";
        }
    }
}

