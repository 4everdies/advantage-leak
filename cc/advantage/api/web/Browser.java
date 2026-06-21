/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.api.web;

import cc.advantage.api.web.ConstructableEntry;
import cc.advantage.api.web.EnvironmentConstants;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;
import javax.net.ssl.HttpsURLConnection;

public class Browser
implements EnvironmentConstants {
    public static String getResponse(String getParameters) throws IOException {
        String lineBuffer;
        HttpsURLConnection connection = (HttpsURLConnection)new URL("https://github.com/x0lumie/Advantage" + getParameters).openConnection();
        connection.addRequestProperty("User-Agent", "Advantage-API/1.0 Advantage");
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        while ((lineBuffer = reader.readLine()) != null) {
            response.append(lineBuffer);
        }
        return response.toString();
    }

    @SafeVarargs
    public static String postResponse(String getParameters, ConstructableEntry<String, String> ... post) throws IOException {
        String lineBuffer;
        HttpsURLConnection connection = (HttpsURLConnection)new URL("https://github.com/x0lumie/Advantage" + getParameters).openConnection();
        connection.addRequestProperty("User-Agent", "Advantage-API/1.0 Advantage");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        StringJoiner sj = new StringJoiner("&");
        for (ConstructableEntry<String, String> entry : post) {
            sj.add(URLEncoder.encode((String)entry.getKey(), "UTF-8") + "=" + URLEncoder.encode((String)entry.getValue(), "UTF-8"));
        }
        byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
        int length = out.length;
        connection.setFixedLengthStreamingMode(length);
        connection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        connection.connect();
        try (OutputStream os = connection.getOutputStream();){
            os.write(out);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        while ((lineBuffer = reader.readLine()) != null) {
            response.append(lineBuffer);
        }
        return response.toString();
    }

    public static String postExternal(String url, String post, boolean json) {
        try {
            String lineBuffer;
            InputStream stream;
            HttpsURLConnection connection = (HttpsURLConnection)new URL(url).openConnection();
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            byte[] out = post.getBytes(StandardCharsets.UTF_8);
            int length = out.length;
            connection.setFixedLengthStreamingMode(length);
            connection.addRequestProperty("Content-Type", json ? "application/json" : "application/x-www-form-urlencoded; charset=UTF-8");
            connection.addRequestProperty("Accept", "application/json");
            connection.connect();
            try (OutputStream os = connection.getOutputStream();){
                os.write(out);
            }
            int responseCode = connection.getResponseCode();
            InputStream inputStream = stream = responseCode / 100 == 2 || responseCode / 100 == 3 ? connection.getInputStream() : connection.getErrorStream();
            if (stream == null) {
                System.err.println(responseCode + ": " + url);
                return null;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder response = new StringBuilder();
            while ((lineBuffer = reader.readLine()) != null) {
                response.append(lineBuffer);
            }
            reader.close();
            return response.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getBearerResponse(String url, String bearer) {
        try {
            String lineBuffer;
            HttpsURLConnection connection = (HttpsURLConnection)new URL(url).openConnection();
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36");
            connection.addRequestProperty("Accept", "application/json");
            connection.addRequestProperty("Authorization", "Bearer " + bearer);
            if (connection.getResponseCode() == 200) {
                String lineBuffer2;
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                while ((lineBuffer2 = reader.readLine()) != null) {
                    response.append(lineBuffer2);
                }
                return response.toString();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            StringBuilder response = new StringBuilder();
            while ((lineBuffer = reader.readLine()) != null) {
                response.append(lineBuffer);
            }
            return response.toString();
        }
        catch (Exception e) {
            return null;
        }
    }
}

