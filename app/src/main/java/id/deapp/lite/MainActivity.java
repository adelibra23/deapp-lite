package id.deapp.lite;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.MimeTypeMap;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URI;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String PREFS = "deapp_lite";
    private static final String KEY_URL = "server_url";
    private static final int REQ_FILE = 2101;
    private static final int REQ_WEBRTC = 2102;
    private static final int REQ_GEO = 2103;

    private FrameLayout root;
    private WebView webView;
    private ProgressBar progress;
    private SharedPreferences prefs;
    private ValueCallback<Uri[]> fileCallback;
    private PermissionRequest pendingPermissionRequest;
    private GeolocationPermissions.Callback pendingGeoCallback;
    private String pendingGeoOrigin;
    private String baseUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        root = new FrameLayout(this);
        setContentView(root);

        baseUrl = prefs.getString(KEY_URL, "").trim();
        if (baseUrl.isEmpty()) {
            showServerSetup(false);
        } else {
            showWebView(baseUrl);
        }
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView text(String value, float sp, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(sp);
        t.setTextColor(color);
        return t;
    }

    private void showServerSetup(boolean changing) {
        root.removeAllViews();
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }

        LinearLayout outer = new LinearLayout(this);
        outer.setOrientation(LinearLayout.VERTICAL);
        outer.setGravity(Gravity.CENTER);
        outer.setPadding(dp(28), dp(28), dp(28), dp(28));
        outer.setBackgroundColor(Color.rgb(247, 249, 255));
        root.addView(outer, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ImageView logo = new ImageView(this);
        logo.setImageResource(id.deapp.lite.R.drawable.deapp_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        LinearLayout.LayoutParams lpLogo = new LinearLayout.LayoutParams(dp(110), dp(110));
        lpLogo.bottomMargin = dp(12);
        outer.addView(logo, lpLogo);

        TextView title = text("Deapp", 30, Color.rgb(28, 35, 62));
        title.setGravity(Gravity.CENTER);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
        outer.addView(title, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView sub = text(changing ? "Ganti alamat server Deapp" : "Hubungkan APK Lite ke server Deapp", 15, Color.rgb(92, 102, 130));
        sub.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lpSub = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpSub.topMargin = dp(6); lpSub.bottomMargin = dp(24);
        outer.addView(sub, lpSub);

        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(baseUrl);
        input.setHint("https://domain.com atau http://192.168.1.10/deapp");
        input.setTextSize(15);
        input.setPadding(dp(16), dp(4), dp(16), dp(4));
        input.setBackgroundResource(android.R.drawable.edit_text);
        LinearLayout.LayoutParams lpInput = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56));
        outer.addView(input, lpInput);

        TextView hint = text("Untuk XAMPP di laptop, gunakan IP Wi-Fi laptop — jangan localhost. Untuk hosting publik, gunakan HTTPS.", 12, Color.rgb(113, 122, 148));
        hint.setPadding(dp(4), dp(10), dp(4), dp(18));
        outer.addView(hint, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Button open = new Button(this);
        open.setText("Simpan & Buka Deapp");
        open.setAllCaps(false);
        open.setTextSize(15);
        open.setTextColor(Color.WHITE);
        open.setBackgroundColor(Color.rgb(50, 100, 245));
        LinearLayout.LayoutParams lpBtn = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52));
        outer.addView(open, lpBtn);

        if (changing) {
            Button cancel = new Button(this);
            cancel.setText("Batal");
            cancel.setAllCaps(false);
            LinearLayout.LayoutParams lpCancel = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
            lpCancel.topMargin = dp(8);
            outer.addView(cancel, lpCancel);
            cancel.setOnClickListener(v -> {
                String old = prefs.getString(KEY_URL, "");
                if (!old.isEmpty()) showWebView(old); else finish();
            });
        }

        open.setOnClickListener(v -> {
            String u = normalizeUrl(input.getText().toString());
            if (u.isEmpty()) {
                input.setError("Masukkan alamat server Deapp");
                input.requestFocus();
                return;
            }
            baseUrl = u;
            prefs.edit().putString(KEY_URL, u).apply();
            showWebView(u);
        });
    }

    private String normalizeUrl(String raw) {
        String s = raw == null ? "" : raw.trim();
        if (s.isEmpty()) return "";
        if (!s.matches("(?i)^https?://.*")) {
            String low = s.toLowerCase(Locale.US);
            boolean local = low.startsWith("localhost") || low.startsWith("127.") || low.startsWith("10.") || low.startsWith("192.168.") || low.matches("^172\\.(1[6-9]|2[0-9]|3[0-1])\\..*");
            s = (local ? "http://" : "https://") + s;
        }
        return s.replaceAll("/+$", "") + "/";
    }

    private void showWebView(String url) {
        root.removeAllViews();
        baseUrl = normalizeUrl(url);
        prefs.edit().putString(KEY_URL, baseUrl).apply();

        webView = new WebView(this);
        root.addView(webView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(3));
        pp.gravity = Gravity.TOP;
        root.addView(progress, pp);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(true);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setLoadsImagesAutomatically(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        s.setUserAgentString(s.getUserAgentString() + " DeappLite/1.0");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) s.setSafeBrowsingEnabled(true);

        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return routeUrl(request.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return routeUrl(url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) showOfflinePage();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progress.setProgress(newProgress);
                progress.setVisibility(newProgress >= 100 ? View.GONE : View.VISIBLE);
            }

            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                try {
                    Intent intent = params.createIntent();
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, REQ_FILE);
                } catch (ActivityNotFoundException e) {
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("*/*");
                    startActivityForResult(Intent.createChooser(i, "Pilih file"), REQ_FILE);
                }
                return true;
            }

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                runOnUiThread(() -> handleWebPermission(request));
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                if (Build.VERSION.SDK_INT < 23 || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    callback.invoke(origin, true, false);
                } else {
                    pendingGeoCallback = callback;
                    pendingGeoOrigin = origin;
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQ_GEO);
                }
            }
        });

        webView.setDownloadListener((downloadUrl, userAgent, contentDisposition, mimetype, contentLength) -> {
            try {
                DownloadManager.Request req = new DownloadManager.Request(Uri.parse(downloadUrl));
                req.setMimeType(mimetype);
                req.addRequestHeader("User-Agent", userAgent);
                String cookie = CookieManager.getInstance().getCookie(downloadUrl);
                if (cookie != null) req.addRequestHeader("Cookie", cookie);
                String name = android.webkit.URLUtil.guessFileName(downloadUrl, contentDisposition, mimetype);
                req.setTitle(name);
                req.setDescription("Mengunduh dari Deapp");
                req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
                ((DownloadManager) getSystemService(DOWNLOAD_SERVICE)).enqueue(req);
                Toast.makeText(this, "Unduhan dimulai", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                openExternal(downloadUrl);
            }
        });

        webView.loadUrl(baseUrl);
    }

    private boolean routeUrl(String url) {
        if (url == null) return false;
        if (url.startsWith("deapp://settings")) {
            showServerSetup(true);
            return true;
        }
        if (url.startsWith("tel:") || url.startsWith("mailto:") || url.startsWith("sms:") || url.startsWith("geo:")) {
            openExternal(url);
            return true;
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            try {
                URI base = URI.create(baseUrl);
                URI target = URI.create(url);
                if (base.getHost() != null && base.getHost().equalsIgnoreCase(target.getHost())) return false;
            } catch (Exception ignored) {}
            openExternal(url);
            return true;
        }
        return false;
    }

    private void openExternal(String url) {
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
        catch (Exception e) { Toast.makeText(this, "Tidak ada aplikasi untuk membuka tautan", Toast.LENGTH_SHORT).show(); }
    }

    private void handleWebPermission(PermissionRequest request) {
        ArrayList<String> need = new ArrayList<>();
        for (String r : request.getResources()) {
            if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(r) && Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) need.add(Manifest.permission.CAMERA);
            if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(r) && Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) need.add(Manifest.permission.RECORD_AUDIO);
        }
        if (!need.isEmpty()) {
            pendingPermissionRequest = request;
            requestPermissions(need.toArray(new String[0]), REQ_WEBRTC);
        } else {
            request.grant(request.getResources());
        }
    }

    private void showOfflinePage() {
        if (webView == null) return;
        String html = "<!doctype html><meta name='viewport' content='width=device-width,initial-scale=1'>" +
                "<style>body{font-family:system-ui;background:#f7f9ff;color:#1c233e;display:grid;place-items:center;min-height:90vh;margin:0}.c{max-width:420px;padding:28px;text-align:center}.i{font-size:54px}.b{display:block;margin:10px 0;padding:14px;border-radius:16px;text-decoration:none;font-weight:700;background:#3264f5;color:#fff}.s{background:#e9edff;color:#3146a5}small{color:#6f7894}</style>" +
                "<div class='c'><div class='i'>📡</div><h2>Server Deapp belum dapat dijangkau</h2><p>Pastikan server aktif dan ponsel berada di jaringan yang benar.</p>" +
                "<a class='b' href='" + baseUrl + "'>Coba lagi</a><a class='b s' href='deapp://settings'>Ganti alamat server</a><small>Deapp Lite · WebView</small></div>";
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_FILE && fileCallback != null) {
            Uri[] result = null;
            if (resultCode == RESULT_OK && data != null) {
                if (data.getClipData() != null) {
                    int n = data.getClipData().getItemCount();
                    result = new Uri[n];
                    for (int i = 0; i < n; i++) result[i] = data.getClipData().getItemAt(i).getUri();
                } else if (data.getData() != null) {
                    result = new Uri[]{data.getData()};
                }
            }
            fileCallback.onReceiveValue(result);
            fileCallback = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_WEBRTC && pendingPermissionRequest != null) {
            boolean ok = true;
            for (int r : grantResults) if (r != PackageManager.PERMISSION_GRANTED) ok = false;
            if (ok) pendingPermissionRequest.grant(pendingPermissionRequest.getResources()); else pendingPermissionRequest.deny();
            pendingPermissionRequest = null;
        } else if (requestCode == REQ_GEO && pendingGeoCallback != null) {
            boolean ok = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            pendingGeoCallback.invoke(pendingGeoOrigin, ok, false);
            pendingGeoCallback = null;
            pendingGeoOrigin = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Deapp")
                .setMessage("Apa yang ingin kamu lakukan?")
                .setPositiveButton("Keluar", (d, w) -> finish())
                .setNeutralButton("Ganti server", (d, w) -> showServerSetup(true))
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    protected void onPause() {
        if (webView != null) webView.onPause();
        CookieManager.getInstance().flush();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) webView.onResume();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
        super.onDestroy();
    }
}
