/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.interfaces.menu.alt.microsoft;
// NOTE: CLIENT_SECRET was originally exposed in this file. GitHub secret scanning blocked the push,
// so we replaced it with "REDACTED". Register your own Azure AD app if you need Microsoft login.

import cc.advantage.api.web.Browser;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class MicrosoftOAuthTranslation {
    static ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final String CLIENT_ID = "9fbc7315-7200-4b2b-a655-bb38c865da17";
    private static final String CLIENT_SECRET = "REDACTED";
    private static final int PORT = 8247;
    private static HttpServer server;
    private static Consumer<String> callback;
    static Gson gson;

    static void browse(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        }
        catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void getRefreshToken(Consumer<String> callback) {
        MicrosoftOAuthTranslation.callback = callback;
        MicrosoftOAuthTranslation.startServer();
        MicrosoftOAuthTranslation.browse("https://login.live.com/oauth20_authorize.srf?client_id=9fbc7315-7200-4b2b-a655-bb38c865da17&client_secret=REDACTED&response_type=code&redirect_uri=http://localhost:8247&scope=XboxLive.signin%20offline_access");
    }

    public static LoginData login(String refreshToken) {
        AuthTokenResponse res = gson.fromJson(Browser.postExternal("https://login.live.com/oauth20_token.srf", "client_id=9fbc7315-7200-4b2b-a655-bb38c865da17&client_secret=REDACTED&refresh_token=" + refreshToken + "&grant_type=refresh_token&redirect_uri=http://localhost:8247", false), AuthTokenResponse.class);
        if (res == null) {
            return new LoginData();
        }
        String accessToken = res.access_token;
        refreshToken = res.refresh_token;
        XblXstsResponse xblRes = gson.fromJson(Browser.postExternal("https://user.auth.xboxlive.com/user/authenticate", "{\"Properties\":{\"AuthMethod\":\"RPS\",\"SiteName\":\"user.auth.xboxlive.com\",\"RpsTicket\":\"d=" + accessToken + "\"},\"RelyingParty\":\"http://auth.xboxlive.com\",\"TokenType\":\"JWT\"}", true), XblXstsResponse.class);
        if (xblRes == null) {
            return new LoginData();
        }
        XblXstsResponse xstsRes = gson.fromJson(Browser.postExternal("https://xsts.auth.xboxlive.com/xsts/authorize", "{\"Properties\":{\"SandboxId\":\"RETAIL\",\"UserTokens\":[\"" + xblRes.Token + "\"]},\"RelyingParty\":\"rp://api.minecraftservices.com/\",\"TokenType\":\"JWT\"}", true), XblXstsResponse.class);
        if (xstsRes == null) {
            return new LoginData();
        }
        McResponse mcRes = gson.fromJson(Browser.postExternal("https://api.minecraftservices.com/authentication/login_with_xbox", "{\"identityToken\":\"XBL3.0 x=" + xblRes.DisplayClaims.xui[0].uhs + ";" + xstsRes.Token + "\"}", true), McResponse.class);
        if (mcRes == null) {
            return new LoginData();
        }
        GameOwnershipResponse gameOwnershipRes = gson.fromJson(Browser.getBearerResponse("https://api.minecraftservices.com/entitlements/mcstore", mcRes.access_token), GameOwnershipResponse.class);
        if (gameOwnershipRes == null || !gameOwnershipRes.hasGameOwnership()) {
            return new LoginData();
        }
        ProfileResponse profileRes = gson.fromJson(Browser.getBearerResponse("https://api.minecraftservices.com/minecraft/profile", mcRes.access_token), ProfileResponse.class);
        if (profileRes == null) {
            return new LoginData();
        }
        return new LoginData(mcRes.access_token, refreshToken, profileRes.id, profileRes.name);
    }

    private static void startServer() {
        if (server != null) {
            return;
        }
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", 8247), 0);
            server.createContext("/", new Handler());
            server.setExecutor(executor);
            server.start();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void stopServer() {
        if (server == null) {
            return;
        }
        server.stop(0);
        server = null;
        callback = null;
    }

    static {
        gson = new Gson();
    }

    private static class AuthTokenResponse {
        @Expose
        @SerializedName(value="access_token")
        public String access_token;
        @Expose
        @SerializedName(value="refresh_token")
        public String refresh_token;

        private AuthTokenResponse() {
        }
    }

    public static class LoginData {
        public String mcToken;
        public String newRefreshToken;
        public String uuid;
        public String username;

        public LoginData() {
        }

        public LoginData(String mcToken, String newRefreshToken, String uuid, String username) {
            this.mcToken = mcToken;
            this.newRefreshToken = newRefreshToken;
            this.uuid = uuid;
            this.username = username;
        }

        public boolean isGood() {
            return this.mcToken != null;
        }
    }

    private static class XblXstsResponse {
        @Expose
        @SerializedName(value="Token")
        public String Token;
        @Expose
        @SerializedName(value="DisplayClaims")
        public DisplayClaims DisplayClaims;

        private XblXstsResponse() {
        }

        private static class DisplayClaims {
            @Expose
            @SerializedName(value="xui")
            private Claim[] xui;

            private DisplayClaims() {
            }

            private static class Claim {
                @Expose
                @SerializedName(value="uhs")
                private String uhs;

                private Claim() {
                }
            }
        }
    }

    private static class McResponse {
        @Expose
        @SerializedName(value="access_token")
        public String access_token;

        private McResponse() {
        }
    }

    private static class GameOwnershipResponse {
        @Expose
        @SerializedName(value="items")
        private Item[] items;

        private GameOwnershipResponse() {
        }

        private boolean hasGameOwnership() {
            boolean hasProduct = false;
            boolean hasGame = false;
            for (Item item : this.items) {
                if (item.name.equals("product_minecraft")) {
                    hasProduct = true;
                    continue;
                }
                if (!item.name.equals("game_minecraft")) continue;
                hasGame = true;
            }
            return hasProduct && hasGame;
        }

        private static class Item {
            @Expose
            @SerializedName(value="name")
            private String name;

            private Item() {
            }
        }
    }

    private static class ProfileResponse {
        @Expose
        @SerializedName(value="id")
        public String id;
        @Expose
        @SerializedName(value="name")
        public String name;

        private ProfileResponse() {
        }
    }

    private static class Handler
    implements HttpHandler {
        private Handler() {
        }

        @Override
        public void handle(HttpExchange req) throws IOException {
            if (req.getRequestMethod().equals("GET")) {
                String query = req.getRequestURI().getQuery();
                Map<String, String> params = this.parseQuery(query);
                String code = params.get("code");
                if (code != null) {
                    this.handleCode(code);
                    this.writeText(req, "<html>You may now close this page.<script>close()</script></html>");
                } else {
                    this.writeText(req, "Cannot authenticate.");
                }
            }
            MicrosoftOAuthTranslation.stopServer();
        }

        private Map<String, String> parseQuery(String query) {
            HashMap<String, String> params = new HashMap<String, String>();
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length != 2) continue;
                    params.put(pair[0], pair[1]);
                }
            }
            return params;
        }

        private void handleCode(String code) {
            String response = Browser.postExternal("https://login.live.com/oauth20_token.srf", "client_id=9fbc7315-7200-4b2b-a655-bb38c865da17&code=" + code + "&client_secret=REDACTED&grant_type=authorization_code&redirect_uri=http://localhost:8247", false);
            AuthTokenResponse res = gson.fromJson(response, AuthTokenResponse.class);
            if (res == null) {
                callback.accept(null);
            } else {
                callback.accept(res.refresh_token);
            }
        }

        private void writeText(HttpExchange req, String text) throws IOException {
            OutputStream out = req.getResponseBody();
            req.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            req.sendResponseHeaders(200, text.length());
            out.write(text.getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.close();
        }
    }
}

