/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.security;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class WebLogin {
    private static final String API_URL = "https://eclipse-site.onrender.com/api.php";
    private static final String HWID_LOGIN_URL = "https://eclipse-site.onrender.com/hwid-login";
    private static final String RESET_URL = "https://eclipse-site.onrender.com/resethwid";
    private static final String UPDATE_MCNAME_URL = "https://eclipse-site.onrender.com/update-mc-name";
    private static final Map<String, Integer> hwidFailCount = new HashMap<String, Integer>();

    private WebLogin() {
    }

    public static LoginResult login(String username, String password, String hwid) {
        return WebLogin.doLogin(username, password, hwid);
    }

    public static LoginResult login(String username, String hwid) {
        return WebLogin.doHwidLogin(username, hwid);
    }

    private static LoginResult doLogin(String username, String password, String hwid) {
        try {
            JsonObject json;
            HttpURLConnection conn = (HttpURLConnection)new URL(API_URL).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.setRequestProperty("User-Agent", "AdvantageClient/1.2");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            String postData = "action=login&username=" + URLEncoder.encode(username, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8") + "&hwid=" + URLEncoder.encode(hwid, "UTF-8");
            byte[] postBytes = postData.getBytes(StandardCharsets.UTF_8);
            conn.setRequestProperty("Content-Length", String.valueOf(postBytes.length));
            try (OutputStream os = conn.getOutputStream();){
                os.write(postBytes);
            }
            String responseStr = WebLogin.readResponse(conn).trim();
            if (responseStr.isEmpty()) {
                return new LoginResult(false, "Empty response from server");
            }
            try {
                json = new JsonParser().parse(responseStr).getAsJsonObject();
            }
            catch (JsonSyntaxException e) {
                return new LoginResult(false, "Invalid JSON: " + responseStr);
            }
            return new LoginResult(json.get("success").getAsBoolean(), json.get("message").getAsString());
        }
        catch (Exception e) {
            return new LoginResult(false, "Connection error: " + e.getMessage());
        }
    }

    private static LoginResult doHwidLogin(String username, String hwid) {
        try {
            JsonObject json;
            HttpURLConnection conn = (HttpURLConnection)new URL(HWID_LOGIN_URL).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.setRequestProperty("User-Agent", "AdvantageClient/1.2");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            String postData = "username=" + URLEncoder.encode(username, "UTF-8") + "&hwid=" + URLEncoder.encode(hwid, "UTF-8");
            byte[] postBytes = postData.getBytes(StandardCharsets.UTF_8);
            conn.setRequestProperty("Content-Length", String.valueOf(postBytes.length));
            try (OutputStream os = conn.getOutputStream();){
                os.write(postBytes);
            }
            String responseStr = WebLogin.readResponse(conn).trim();
            if (responseStr.isEmpty()) {
                return new LoginResult(false, "Empty response from server");
            }
            try {
                json = new JsonParser().parse(responseStr).getAsJsonObject();
            }
            catch (JsonSyntaxException e) {
                return new LoginResult(false, "Invalid JSON: " + responseStr);
            }
            return new LoginResult(json.get("success").getAsBoolean(), json.get("message").getAsString());
        }
        catch (Exception e) {
            return new LoginResult(false, "Connection error: " + e.getMessage());
        }
    }

    public static boolean resetHwid(String username, String password) {
        try {
            HttpURLConnection conn = (HttpURLConnection)new URL(RESET_URL).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.setRequestProperty("User-Agent", "AdvantageClient/1.0");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            String postData = "username=" + URLEncoder.encode(username, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8");
            byte[] postBytes = postData.getBytes(StandardCharsets.UTF_8);
            conn.setRequestProperty("Content-Length", String.valueOf(postBytes.length));
            try (OutputStream os = conn.getOutputStream();){
                os.write(postBytes);
            }
            JsonObject json = new JsonParser().parse(WebLogin.readResponse(conn).trim()).getAsJsonObject();
            return json.get("success").getAsBoolean();
        }
        catch (Exception ignored) {
            return false;
        }
    }

    public static void updateMcName(String advantageUser, String mcName) {
        try {
            HttpURLConnection conn = (HttpURLConnection)new URL(UPDATE_MCNAME_URL).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", "AdvantageClient/1.0");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            String postData = "username=" + URLEncoder.encode(advantageUser, "UTF-8") + "&mc_name=" + URLEncoder.encode(mcName, "UTF-8");
            byte[] postBytes = postData.getBytes(StandardCharsets.UTF_8);
            conn.setRequestProperty("Content-Length", String.valueOf(postBytes.length));
            try (OutputStream os = conn.getOutputStream();){
                os.write(postBytes);
            }
            conn.getResponseCode();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static String readResponse(HttpURLConnection conn) throws Exception {
        int responseCode = conn.getResponseCode();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream(), StandardCharsets.UTF_8));){
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String string = sb.toString();
            return string;
        }
    }

    public static class LoginResult {
        public final boolean success;
        public final String message;

        public LoginResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}

