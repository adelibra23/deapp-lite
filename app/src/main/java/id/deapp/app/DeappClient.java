package id.deapp.app;

import android.content.SharedPreferences;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** HTTP session used by the native Android UI. No WebView or browser cookie jar is used. */
public final class DeappClient {
    public static final class Result {
        public final int code;
        public final String body;
        public final String url;
        public final Map<String, List<String>> headers;
        Result(int code, String body, String url, Map<String, List<String>> headers) {
            this.code = code;
            this.body = body == null ? "" : body;
            this.url = url == null ? "" : url;
            this.headers = headers;
        }
        public boolean ok() { return code >= 200 && code < 300; }
    }

    private final String baseUrl;
    private final SharedPreferences prefs;
    private final LinkedHashMap<String, String> cookies = new LinkedHashMap<>();
    private volatile String csrf = "";

    public DeappClient(String baseUrl, SharedPreferences prefs) {
        String b = baseUrl == null ? "" : baseUrl.trim();
        while (b.endsWith("/")) b = b.substring(0, b.length() - 1);
        this.baseUrl = b;
        this.prefs = prefs;
        restoreCookies();
    }

    public String baseUrl() { return baseUrl; }

    public String absolute(String path) {
        if (path == null || path.trim().isEmpty()) return baseUrl;
        String p = path.trim();
        if (p.startsWith("http://") || p.startsWith("https://")) return p;
        if (!p.startsWith("/")) p = "/" + p;
        return baseUrl + p;
    }

    public Result get(String path) throws Exception {
        return request("GET", absolute(path), null, null, 0);
    }

    public Result postForm(String path, Map<String, String> data) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : data.entrySet()) {
            if (sb.length() > 0) sb.append('&');
            sb.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8.name()));
            sb.append('=');
            sb.append(URLEncoder.encode(e.getValue() == null ? "" : e.getValue(), StandardCharsets.UTF_8.name()));
        }
        return request("POST", absolute(path), sb.toString().getBytes(StandardCharsets.UTF_8),
                "application/x-www-form-urlencoded; charset=UTF-8", 0);
    }

    public Result postJson(String path, String json) throws Exception {
        return request("POST", absolute(path), (json == null ? "{}" : json).getBytes(StandardCharsets.UTF_8),
                "application/json; charset=UTF-8", 0);
    }

    public byte[] getBytes(String path) throws Exception {
        String target = absolute(path);
        HttpURLConnection c = (HttpURLConnection) new URL(target).openConnection();
        configure(c);
        c.setRequestMethod("GET");
        c.connect();
        captureCookies(c.getHeaderFields());
        int code = c.getResponseCode();
        if (code < 200 || code >= 300) {
            c.disconnect();
            return new byte[0];
        }
        try (InputStream in = new BufferedInputStream(c.getInputStream()); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            return out.toByteArray();
        } finally {
            c.disconnect();
        }
    }

    public synchronized String ensureCsrf() throws Exception {
        if (!csrf.isEmpty()) return csrf;
        String[] candidates = {"/settings.php", "/messages.php", "/login.php"};
        for (String candidate : candidates) {
            Result r = get(candidate);
            if (r.body.isEmpty()) continue;
            Document d = Jsoup.parse(r.body, r.url);
            Element input = d.selectFirst("input[name=csrf]");
            if (input != null && !input.attr("value").isEmpty()) {
                csrf = input.attr("value");
                return csrf;
            }
        }
        return "";
    }

    public synchronized void clearSession() {
        cookies.clear();
        csrf = "";
        prefs.edit().remove("native_cookies").apply();
    }

    private Result request(String method, String target, byte[] payload, String contentType, int redirects) throws Exception {
        if (redirects > 7) throw new IllegalStateException("Terlalu banyak redirect dari server DeApp.");
        HttpURLConnection c = (HttpURLConnection) new URL(target).openConnection();
        configure(c);
        c.setInstanceFollowRedirects(false);
        c.setRequestMethod(method);
        if (payload != null) {
            c.setDoOutput(true);
            if (contentType != null) c.setRequestProperty("Content-Type", contentType);
            c.setRequestProperty("Content-Length", String.valueOf(payload.length));
            try (OutputStream out = c.getOutputStream()) { out.write(payload); }
        }

        int code = c.getResponseCode();
        Map<String, List<String>> headers = c.getHeaderFields();
        captureCookies(headers);
        if (code == 301 || code == 302 || code == 303 || code == 307 || code == 308) {
            String loc = c.getHeaderField("Location");
            c.disconnect();
            if (loc == null || loc.isEmpty()) return new Result(code, "", target, headers);
            String next = resolve(target, loc);
            boolean preserve = code == 307 || code == 308;
            return request(preserve ? method : "GET", next, preserve ? payload : null,
                    preserve ? contentType : null, redirects + 1);
        }

        InputStream raw = code >= 400 ? c.getErrorStream() : c.getInputStream();
        String body = readString(raw);
        String finalUrl = c.getURL().toString();
        c.disconnect();
        return new Result(code, body, finalUrl, headers);
    }

    private void configure(HttpURLConnection c) {
        c.setConnectTimeout(15000);
        c.setReadTimeout(25000);
        c.setUseCaches(false);
        c.setRequestProperty("Accept", "application/json,text/html,application/xhtml+xml;q=0.9,*/*;q=0.8");
        c.setRequestProperty("Accept-Language", "id-ID,id;q=0.9,en;q=0.6");
        c.setRequestProperty("User-Agent", "DeApp-Android/2.0 NativeAndroid");
        String cookie = cookieHeader();
        if (!cookie.isEmpty()) c.setRequestProperty("Cookie", cookie);
    }

    private static String readString(InputStream in) throws Exception {
        if (in == null) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append('\n');
        }
        return sb.toString();
    }

    private static String resolve(String current, String location) throws Exception {
        URL base = new URL(current);
        return new URL(base, location).toString();
    }

    private synchronized void captureCookies(Map<String, List<String>> headers) {
        if (headers == null) return;
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey() == null || !"set-cookie".equalsIgnoreCase(entry.getKey())) continue;
            for (String line : entry.getValue()) {
                if (line == null || line.isEmpty()) continue;
                String first = line.split(";", 2)[0].trim();
                int eq = first.indexOf('=');
                if (eq <= 0) continue;
                String name = first.substring(0, eq).trim();
                String value = first.substring(eq + 1).trim();
                if (value.isEmpty()) cookies.remove(name); else cookies.put(name, value);
            }
        }
        persistCookies();
    }

    private synchronized String cookieHeader() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : cookies.entrySet()) {
            if (sb.length() > 0) sb.append("; ");
            sb.append(e.getKey()).append('=').append(e.getValue());
        }
        return sb.toString();
    }

    private void restoreCookies() {
        String saved = prefs.getString("native_cookies", "");
        if (saved == null || saved.isEmpty()) return;
        String[] pairs = saved.split("; ");
        for (String pair : pairs) {
            int eq = pair.indexOf('=');
            if (eq > 0) cookies.put(pair.substring(0, eq), pair.substring(eq + 1));
        }
    }

    private void persistCookies() {
        prefs.edit().putString("native_cookies", cookieHeader()).apply();
    }
}
