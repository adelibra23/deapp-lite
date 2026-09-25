package id.deapp.lite;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.GeolocationPermissions;
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
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String PREFS = "deapp_lite";
    private static final String KEY_URL = "server_url";
    private static final int REQ_FILE = 2101;
    private static final int REQ_WEBRTC = 2102;
    private static final int REQ_GEO = 2103;

    private FrameLayout root;
    private LinearLayout shell;
    private FrameLayout topContainer;
    private FrameLayout bottomContainer;
    private WebView webView;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progress;
    private FrameLayout offlineOverlay;
    private FrameLayout loadingOverlay;
    private ImageView profileAvatar;
    private ImageButton featureMenuButton;
    private ImageButton composeFab;
    private String profileUrl = "";
    private View activeSheetOverlay;
    private View activeSheetPanel;
    private NavItem navHome;
    private NavItem navExplore;
    private NavItem navVideo;
    private NavItem navNotif;
    private NavItem navProfile;

    private SharedPreferences prefs;
    private ValueCallback<Uri[]> fileCallback;
    private PermissionRequest pendingPermissionRequest;
    private GeolocationPermissions.Callback pendingGeoCallback;
    private String pendingGeoOrigin;
    private String baseUrl = "";

    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;

    private boolean dark;
    private int cBg;
    private int cSurface;
    private int cSurface2;
    private int cText;
    private int cMuted;
    private int cBorder;
    private int cAccent;
    private int cAccentSoft;
    private int cDanger;
    private int cSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        readPalette();
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        baseUrl = prefs.getString(KEY_URL, "").trim();

        if (Build.VERSION.SDK_INT >= 33) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                    this::handleBack
            );
        }

        if (baseUrl.isEmpty()) {
            showServerSetup(false);
        } else {
            showWebView(baseUrl);
        }
    }

    private void readPalette() {
        dark = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        // v1.3: palet monokrom ala aplikasi sosial native modern / Threads-inspired.
        // Biru Deapp tetap dipakai untuk aksi primer dan progress agar identitas brand tidak hilang.
        cBg = Color.parseColor(dark ? "#000000" : "#FFFFFF");
        cSurface = Color.parseColor(dark ? "#000000" : "#FFFFFF");
        cSurface2 = Color.parseColor(dark ? "#171717" : "#F5F5F5");
        cText = Color.parseColor(dark ? "#F5F5F5" : "#0A0A0A");
        cMuted = Color.parseColor(dark ? "#8F8F8F" : "#777777");
        cBorder = Color.parseColor(dark ? "#262626" : "#E7E7E7");
        cAccent = Color.parseColor("#3264F5");
        cAccentSoft = Color.parseColor(dark ? "#171C2B" : "#EEF3FF");
        cDanger = Color.parseColor("#E5484D");
        cSuccess = Color.parseColor("#22A06B");
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable rounded(int color, int radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radiusDp));
        return d;
    }

    private GradientDrawable bordered(int color, int borderColor, int radiusDp) {
        GradientDrawable d = rounded(color, radiusDp);
        d.setStroke(dp(1), borderColor);
        return d;
    }

    private TextView text(String value, float sp, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(sp);
        t.setTextColor(color);
        t.setIncludeFontPadding(false);
        return t;
    }

    private void applySystemBarAppearance(View anchor) {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), anchor);
        controller.setAppearanceLightStatusBars(!dark);
        controller.setAppearanceLightNavigationBars(!dark);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
    }

    private ImageButton iconButton(int drawable, String description) {
        ImageButton b = new ImageButton(this);
        b.setImageResource(drawable);
        b.setColorFilter(cText);
        b.setContentDescription(description);
        b.setBackground(rounded(Color.TRANSPARENT, 99));
        b.setPadding(dp(10), dp(10), dp(10), dp(10));
        b.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(44), dp(44));
        b.setLayoutParams(lp);
        return b;
    }

    private void haptic(View v) {
        if (v != null) v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
    }

    private void showServerSetup(boolean changing) {
        if (changing && webView != null) {
            showServerBottomSheet();
            return;
        }
        destroyWebView();
        readPalette();

        root = new FrameLayout(this);
        root.setBackgroundColor(cBg);
        setContentView(root);
        applySystemBarAppearance(root);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setHorizontalScrollBarEnabled(false);
        root.addView(scroll, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout outer = new LinearLayout(this);
        outer.setOrientation(LinearLayout.VERTICAL);
        outer.setGravity(Gravity.CENTER);
        outer.setPadding(dp(20), dp(28), dp(20), dp(28));
        scroll.addView(outer, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(Gravity.CENTER);
        hero.setPadding(dp(22), dp(26), dp(22), dp(22));
        hero.setBackground(bordered(cSurface, cBorder, 28));
        LinearLayout.LayoutParams heroLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        heroLp.setMargins(0, dp(24), 0, dp(24));
        outer.addView(hero, heroLp);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.deapp_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        LinearLayout.LayoutParams logoLp = new LinearLayout.LayoutParams(dp(88), dp(88));
        logoLp.bottomMargin = dp(14);
        hero.addView(logo, logoLp);

        TextView badge = text("DEAPP LITE · NATIVE SHELL", 10, cAccent);
        badge.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        badge.setLetterSpacing(.10f);
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(dp(12), dp(7), dp(12), dp(7));
        badge.setBackground(rounded(cAccentSoft, 99));
        hero.addView(badge);

        TextView title = text(changing ? "Ganti server Deapp" : "Hubungkan Deapp Lite", 27, cText);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleLp.topMargin = dp(18);
        hero.addView(title, titleLp);

        TextView sub = text(
                changing ? "Masukkan alamat server yang baru. Tampilan native tetap sama." :
                        "APK ini menjadi aplikasi Android native di bagian navigasi, sementara data tetap berasal dari server Deapp.",
                14, cMuted);
        sub.setGravity(Gravity.CENTER);
        sub.setLineSpacing(0, 1.12f);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subLp.topMargin = dp(9);
        subLp.bottomMargin = dp(22);
        hero.addView(sub, subLp);

        TextView label = text("Alamat server", 12, cText);
        label.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        hero.addView(label, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(baseUrl);
        input.setHint("https://domain.com atau 192.168.1.10/deapp");
        input.setHintTextColor(cMuted);
        input.setTextColor(cText);
        input.setTextSize(14);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        input.setImeOptions(EditorInfo.IME_ACTION_GO);
        input.setPadding(dp(16), 0, dp(16), 0);
        input.setBackground(bordered(cSurface2, cBorder, 16));
        LinearLayout.LayoutParams inputLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(56));
        inputLp.topMargin = dp(8);
        hero.addView(input, inputLp);

        TextView hint = text("XAMPP/LAN: gunakan IP Wi-Fi laptop. Hosting publik: gunakan HTTPS.", 11.5f, cMuted);
        hint.setLineSpacing(0, 1.12f);
        LinearLayout.LayoutParams hintLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hintLp.topMargin = dp(10);
        hero.addView(hint, hintLp);

        Button open = new Button(this);
        open.setText("Simpan & buka Deapp");
        open.setAllCaps(false);
        open.setTextColor(Color.WHITE);
        open.setTextSize(14.5f);
        open.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        open.setBackground(rounded(cAccent, 16));
        LinearLayout.LayoutParams openLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(54));
        openLp.topMargin = dp(20);
        hero.addView(open, openLp);

        if (changing) {
            Button cancel = new Button(this);
            cancel.setText("Batal");
            cancel.setAllCaps(false);
            cancel.setTextColor(cText);
            cancel.setTextSize(14);
            cancel.setBackground(bordered(cSurface2, cBorder, 16));
            LinearLayout.LayoutParams cancelLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(50));
            cancelLp.topMargin = dp(9);
            hero.addView(cancel, cancelLp);
            cancel.setOnClickListener(v -> {
                String old = prefs.getString(KEY_URL, "");
                if (!old.isEmpty()) showWebView(old); else finish();
            });
        }

        TextView privacy = text("Tidak ada isi Deapp yang disimpan di APK. Login, postingan, Live, foto, dan pesan tetap berasal dari server.", 11, cMuted);
        privacy.setGravity(Gravity.CENTER);
        privacy.setLineSpacing(0, 1.15f);
        outer.addView(privacy, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            scroll.setPadding(0, bars.top, 0, bars.bottom);
            return insets;
        });

        View.OnClickListener submit = v -> {
            haptic(v);
            String u = normalizeUrl(input.getText().toString());
            if (u.isEmpty()) {
                input.setError("Masukkan alamat server Deapp");
                input.requestFocus();
                return;
            }
            baseUrl = u;
            prefs.edit().putString(KEY_URL, u).apply();
            showWebView(u);
        };
        open.setOnClickListener(submit);
        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                submit.onClick(input);
                return true;
            }
            return false;
        });
    }

    private String normalizeUrl(String raw) {
        String s = raw == null ? "" : raw.trim();
        if (s.isEmpty()) return "";
        if (!s.matches("(?i)^https?://.*")) {
            String low = s.toLowerCase(Locale.US);
            boolean local = low.startsWith("localhost") || low.startsWith("127.") ||
                    low.startsWith("10.") || low.startsWith("192.168.") ||
                    low.matches("^172\\.(1[6-9]|2[0-9]|3[0-1])\\..*");
            s = (local ? "http://" : "https://") + s;
        }
        return s.replaceAll("/+$", "") + "/";
    }

    private void showWebView(String url) {
        destroyWebView();
        readPalette();
        baseUrl = normalizeUrl(url);
        prefs.edit().putString(KEY_URL, baseUrl).apply();

        root = new FrameLayout(this);
        root.setBackgroundColor(cBg);
        setContentView(root);
        applySystemBarAppearance(root);

        shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setBackgroundColor(cBg);
        root.addView(shell, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        buildNativeTopBar();
        buildWebContent();
        buildNativeBottomBar();
        buildFloatingComposer();
        applyInsetsToShell();
        configureWebView();
        webView.loadUrl(baseUrl);
    }

    private void buildNativeTopBar() {
        topContainer = new FrameLayout(this);
        topContainer.setBackgroundColor(cSurface);
        shell.addView(topContainer, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(56)));

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(dp(15), 0, dp(10), 0);
        topContainer.addView(toolbar, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(56), Gravity.BOTTOM));

        // Logo menjadi satu-satunya identitas di sisi kiri agar header ringan.
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.deapp_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        logo.setContentDescription("Beranda Deapp");
        LinearLayout.LayoutParams logoLp = new LinearLayout.LayoutParams(dp(35), dp(35));
        toolbar.addView(logo, logoLp);
        logo.setOnClickListener(v -> {
            haptic(v);
            loadRelative("index.php");
        });

        View spacer = new View(this);
        toolbar.addView(spacer, new LinearLayout.LayoutParams(0, dp(1), 1f));

        // Tombol menu fitur: satu ikon di kanan atas, membuka feature grid bottom sheet.
        featureMenuButton = iconButton(R.drawable.ic_native_grid, "Menu fitur Deapp");
        featureMenuButton.setColorFilter(cText);
        featureMenuButton.setBackground(rounded(Color.TRANSPARENT, 99));
        toolbar.addView(featureMenuButton);
        featureMenuButton.setOnClickListener(v -> {
            haptic(v);
            showFeatureMenuSheet();
        });

        View divider = new View(this);
        divider.setBackgroundColor(cBorder);
        FrameLayout.LayoutParams divLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1), Gravity.BOTTOM);
        topContainer.addView(divider, divLp);
    }

    private void buildWebContent() {
        FrameLayout content = new FrameLayout(this);
        LinearLayout.LayoutParams contentLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        shell.addView(content, contentLp);

        swipeRefresh = new SwipeRefreshLayout(this);
        swipeRefresh.setColorSchemeColors(cAccent);
        swipeRefresh.setProgressBackgroundColorSchemeColor(cSurface);
        content.addView(swipeRefresh, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        webView = new WebView(this);
        webView.setBackgroundColor(cBg);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setScrollbarFadingEnabled(true);
        webView.addJavascriptInterface(new NativeBridge(), "DeappNative");
        swipeRefresh.addView(webView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        swipeRefresh.setOnRefreshListener(() -> {
            haptic(swipeRefresh);
            if (webView != null) webView.reload();
        });
        swipeRefresh.setOnChildScrollUpCallback((parent, child) ->
                webView != null && webView.canScrollVertically(-1));

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(cAccent));
        progress.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.TRANSPARENT));
        FrameLayout.LayoutParams progressLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(2), Gravity.TOP);
        content.addView(progress, progressLp);

        loadingOverlay = buildLoadingOverlay();
        content.addView(loadingOverlay, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        offlineOverlay = buildOfflineOverlay();
        offlineOverlay.setVisibility(View.GONE);
        content.addView(offlineOverlay, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private FrameLayout buildLoadingOverlay() {
        FrameLayout layer = new FrameLayout(this);
        layer.setBackgroundColor(cBg);
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.deapp_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(86), dp(86), Gravity.CENTER);
        layer.addView(logo, lp);
        return layer;
    }

    private FrameLayout buildOfflineOverlay() {
        FrameLayout layer = new FrameLayout(this);
        layer.setBackgroundColor(cBg);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER_HORIZONTAL);
        box.setPadding(dp(28), dp(36), dp(28), dp(36));
        FrameLayout.LayoutParams boxLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        layer.addView(box, boxLp);

        ImageView ico = new ImageView(this);
        ico.setImageResource(R.drawable.ic_native_wifi_off);
        ico.setColorFilter(cMuted);
        box.addView(ico, new LinearLayout.LayoutParams(dp(58), dp(58)));

        TextView title = text("Server Deapp belum terjangkau", 20, cText);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleLp.topMargin = dp(18);
        box.addView(title, titleLp);

        TextView desc = text("Periksa internet, Wi-Fi, alamat server, atau pastikan XAMPP/hosting sedang aktif.", 13, cMuted);
        desc.setGravity(Gravity.CENTER);
        desc.setLineSpacing(0, 1.15f);
        LinearLayout.LayoutParams descLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        descLp.topMargin = dp(9);
        descLp.bottomMargin = dp(20);
        box.addView(desc, descLp);

        Button retry = new Button(this);
        retry.setText("Coba lagi");
        retry.setAllCaps(false);
        retry.setTextColor(Color.WHITE);
        retry.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        retry.setBackground(rounded(cAccent, 16));
        box.addView(retry, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));
        retry.setOnClickListener(v -> {
            haptic(v);
            showOffline(false);
            if (webView != null) webView.loadUrl(baseUrl);
        });

        Button server = new Button(this);
        server.setText("Ganti alamat server");
        server.setAllCaps(false);
        server.setTextColor(cText);
        server.setBackground(bordered(cSurface, cBorder, 16));
        LinearLayout.LayoutParams serverLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(50));
        serverLp.topMargin = dp(9);
        box.addView(server, serverLp);
        server.setOnClickListener(v -> showServerSetup(true));
        return layer;
    }

    private void buildNativeBottomBar() {
        bottomContainer = new FrameLayout(this);
        bottomContainer.setBackgroundColor(cSurface);
        shell.addView(bottomContainer, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(58)));

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(10), dp(4), dp(10), dp(4));
        bottomContainer.addView(nav, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(58), Gravity.TOP));

        View divider = new View(this);
        divider.setBackgroundColor(cBorder);
        bottomContainer.addView(divider, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1), Gravity.TOP));

        // Navigasi inti ala Threads: fitur sekunder dipindah ke menu kanan atas.
        navHome = addNav(nav, R.drawable.ic_native_home, "Beranda", () -> loadRelative("index.php"));
        navExplore = addNav(nav, R.drawable.ic_native_search, "Jelajah", () -> loadRelative("explore.php"));
        navNotif = addNav(nav, R.drawable.ic_native_bell, "Aktivitas", () -> loadRelative("notifications.php"));
        navProfile = addNav(nav, R.drawable.ic_native_user, "Profil", this::openOwnProfile);
        navVideo = null;
        updateNativeNav(baseUrl);
    }

    private NavItem addNav(LinearLayout parent, int iconRes, String description, Runnable action) {
        NavItem item = new NavItem(iconRes, description, action);
        parent.addView(item.root, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        return item;
    }

    private void buildFloatingComposer() {
        composeFab = new ImageButton(this);
        composeFab.setImageResource(R.drawable.ic_native_add);
        composeFab.setColorFilter(dark ? Color.BLACK : Color.WHITE);
        composeFab.setContentDescription("Buat kiriman");
        composeFab.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        composeFab.setPadding(dp(16), dp(16), dp(16), dp(16));
        composeFab.setBackground(rounded(dark ? Color.WHITE : Color.BLACK, 19));
        composeFab.setElevation(dp(9));
        composeFab.setStateListAnimator(null);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(58), dp(58), Gravity.END | Gravity.BOTTOM);
        lp.rightMargin = dp(18);
        lp.bottomMargin = dp(70);
        root.addView(composeFab, lp);
        composeFab.setOnClickListener(v -> {
            haptic(v);
            openComposer();
        });
    }

    private class NavItem {
        final FrameLayout root;
        final ImageView icon;

        NavItem(int iconRes, String description, Runnable action) {
            root = new FrameLayout(MainActivity.this);
            root.setClickable(true);
            root.setFocusable(true);
            root.setContentDescription(description);

            icon = new ImageView(MainActivity.this);
            icon.setImageResource(iconRes);
            icon.setColorFilter(cMuted);
            icon.setPadding(dp(10), dp(10), dp(10), dp(10));
            icon.setBackground(rounded(Color.TRANSPARENT, 16));
            FrameLayout.LayoutParams iconLp = new FrameLayout.LayoutParams(dp(44), dp(44), Gravity.CENTER);
            root.addView(icon, iconLp);

            root.setOnClickListener(v -> {
                haptic(v);
                action.run();
            });
        }

        void setActive(boolean active) {
            // Threads-inspired: status aktif dibaca dari kontras ikon, tanpa pill berwarna.
            icon.setColorFilter(active ? cText : cMuted);
            icon.setAlpha(active ? 1f : .72f);
            icon.setBackground(rounded(Color.TRANSPARENT, 16));
            icon.setScaleX(active ? 1.05f : 1f);
            icon.setScaleY(active ? 1.05f : 1f);
        }
    }

    private void applyInsetsToShell() {
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            boolean imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            if (topContainer != null) {
                topContainer.setPadding(0, bars.top, 0, 0);
                ViewGroup.LayoutParams lp = topContainer.getLayoutParams();
                lp.height = dp(56) + bars.top;
                topContainer.setLayoutParams(lp);
            }
            if (bottomContainer != null) {
                bottomContainer.setPadding(0, 0, 0, bars.bottom);
                ViewGroup.LayoutParams lp = bottomContainer.getLayoutParams();
                lp.height = dp(58) + bars.bottom;
                bottomContainer.setLayoutParams(lp);
                bottomContainer.setVisibility(imeVisible ? View.GONE : View.VISIBLE);
            }
            if (composeFab != null) {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) composeFab.getLayoutParams();
                lp.rightMargin = dp(18);
                lp.bottomMargin = dp(70) + bars.bottom;
                composeFab.setLayoutParams(lp);
                composeFab.setVisibility(imeVisible ? View.GONE : View.VISIBLE);
            }
            return insets;
        });
    }

    private void configureWebView() {
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
        s.setUserAgentString(s.getUserAgentString() + " DeappLite/1.3 ThreadsShell/3");
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
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progress.setVisibility(View.VISIBLE);
                showOffline(false);
                updateNativeNav(url);
                updateKeepScreenOn(url);
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                super.onPageCommitVisible(view, url);
                injectNativeShell();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                injectNativeShell();
                progress.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                loadingOverlay.setVisibility(View.GONE);
                updateNativeNav(url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    swipeRefresh.setRefreshing(false);
                    loadingOverlay.setVisibility(View.GONE);
                    showOffline(true);
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progress.setProgress(newProgress);
                progress.setVisibility(newProgress >= 100 ? View.GONE : View.VISIBLE);
                if (newProgress >= 100) swipeRefresh.setRefreshing(false);
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
                    i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    startActivityForResult(Intent.createChooser(i, "Pilih file untuk Deapp"), REQ_FILE);
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

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                customView = view;
                customViewCallback = callback;
                shell.setVisibility(View.GONE);
                if (composeFab != null) composeFab.setVisibility(View.GONE);
                root.addView(customView, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }

            @Override
            public void onHideCustomView() {
                hideCustomView();
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
    }

    private void injectNativeShell() {
        if (webView == null) return;
        String js = "(function(){" +
                "var id='deapp-native-shell-style';var s=document.getElementById(id);" +
                "if(!s){s=document.createElement('style');s.id=id;" +
                "s.textContent=':root{--topbar-h:0px!important;--bnav-h:0px!important}' +" +
                "'.topbar,.bottom-nav{display:none!important}' +" +
                "'html,body{scrollbar-width:none!important;overscroll-behavior-y:none!important;background:var(--surface)!important}' +" +
                "'html::-webkit-scrollbar,body::-webkit-scrollbar,*::-webkit-scrollbar{display:none!important;width:0!important;height:0!important;background:transparent!important}' +" +
                "'body{padding-top:0!important;padding-bottom:0!important;-webkit-tap-highlight-color:transparent}' +" +
                "'.layout,.layout-guest{padding-top:0!important;padding-bottom:14px!important}' +" +
                "'.tabs,.nx-studio-nav{top:0!important}' +" +
                "'.page-home .composer-trigger{display:none!important}' +" +
                "'.page-home .feed-tabs{border-left:0!important;border-right:0!important;border-top:0!important;border-radius:0!important;box-shadow:none!important;background:var(--surface)!important}' +" +
                "'.page-home .feed{border:0!important;border-radius:0!important;box-shadow:none!important;background:var(--surface)!important}' +" +
                "'.page-home .feed>.post-card,.page-home .feed>.qa-card{border-radius:0!important;box-shadow:none!important}' +" +
                "'.post-card{box-shadow:none!important}' +" +
                "'.post-card .post-actions{border-top-color:transparent!important}' +" +
                "'.modal-overlay{align-items:flex-end!important;justify-content:center!important;padding:0!important;overflow:hidden!important}' +" +
                "'.modal-overlay .modal-box{width:100%!important;max-width:100%!important;margin:0!important;border-left:0!important;border-right:0!important;border-bottom:0!important;border-radius:24px 24px 0 0!important;max-height:94dvh!important;overflow:auto!important;padding-bottom:max(20px,env(safe-area-inset-bottom))!important;box-shadow:0 -12px 38px rgba(0,0,0,.16)!important;animation:deappNativeSheetIn .22s cubic-bezier(.2,.8,.2,1)!important;will-change:transform}' +" +
                "'.modal-overlay .modal-box:before,.deapp-cookie-modal .cookie-modal-card:before,dialog.c-modal[open] .c-modal-box:before{content:\"\";display:block;width:36px;height:4px;border-radius:99px;background:color-mix(in srgb,var(--text-muted) 48%,transparent);margin:8px auto 10px;flex:none}' +" +
                "'.modal-overlay.photo-studio-modal .modal-box,.modal-overlay.reel-create-modal .modal-box{max-height:97dvh!important}' +" +
                "'.deapp-cookie-modal{align-items:flex-end!important;padding:0!important}' +" +
                "'.deapp-cookie-modal .cookie-modal-card{width:100%!important;max-width:100%!important;margin:0!important;border-radius:24px 24px 0 0!important;max-height:94dvh!important;overflow:auto!important;will-change:transform}' +" +
                "'dialog.c-modal[open]{position:fixed!important;inset:auto 0 0 0!important;width:100%!important;max-width:none!important;margin:0!important;border-radius:24px 24px 0 0!important;max-height:94dvh!important}' +" +
                "'dialog.c-modal .c-modal-box{border-radius:24px 24px 0 0!important;will-change:transform}' +" +
                "'@keyframes deappNativeSheetIn{from{transform:translateY(100%);opacity:.7}to{transform:translateY(0);opacity:1}}';" +
                "document.head.appendChild(s);}" +
                "document.documentElement.classList.add('deapp-native-shell','deapp-threads-shell');" +
                "function closeSheet(box){var ov=box.closest('.modal-overlay');var dlg=box.closest('dialog');var btn=box.querySelector('.modal-close,[data-close-modal],[aria-label=\\\"Tutup\\\"]');if(btn){btn.click();return;}if(dlg&&dlg.close){dlg.close();return;}if(ov){ov.click();}}" +
                "function wire(box){if(!box||box.dataset.deappSwipeSheet==='1')return;box.dataset.deappSwipeSheet='1';var sy=0,last=0,drag=false;" +
                "box.addEventListener('touchstart',function(e){if(e.touches.length!==1)return;sy=e.touches[0].clientY;last=sy;drag=false;box.style.transition='none';},{passive:true});" +
                "box.addEventListener('touchmove',function(e){if(!e.touches.length)return;var y=e.touches[0].clientY,dy=y-sy;last=y;if(dy>5&&box.scrollTop<=0){drag=true;box.style.transform='translateY('+Math.min(dy,window.innerHeight*.72)+'px)';if(dy>10)e.preventDefault();}},{passive:false});" +
                "box.addEventListener('touchend',function(){if(!drag){box.style.transition='';return;}var dy=last-sy;box.style.transition='transform .20s cubic-bezier(.2,.8,.2,1)';if(dy>92){box.style.transform='translateY(110%)';setTimeout(function(){closeSheet(box);box.style.transform='';box.style.transition='';},155);}else{box.style.transform='translateY(0)';setTimeout(function(){box.style.transform='';box.style.transition='';},210);}drag=false;},{passive:true});}" +
                "function scan(){document.querySelectorAll('.modal-overlay .modal-box,.deapp-cookie-modal .cookie-modal-card,dialog.c-modal[open] .c-modal-box').forEach(wire);}" +
                "scan();if(!window.__deappNativeSheetObserver){window.__deappNativeSheetObserver=new MutationObserver(scan);window.__deappNativeSheetObserver.observe(document.body,{subtree:true,childList:true,attributes:true,attributeFilter:['class','open']});}" +
                "try{var av=document.querySelector('#user-menu-btn img.avatar-mini,.bottom-nav .bnav-avatar');var pr=document.querySelector('#user-dropdown .dropdown-user,.bottom-nav a.bnav:last-child');if(window.DeappNative){window.DeappNative.syncProfile(av?av.src:'',pr?pr.href:'');}}catch(e){}" +
                "return true;})()";
        webView.evaluateJavascript(js, null);
    }

    private class DraggableSheet extends LinearLayout {
        private final int touchSlop = ViewConfiguration.get(MainActivity.this).getScaledTouchSlop();
        private float downX;
        private float downY;
        private boolean dragging;

        DraggableSheet() {
            super(MainActivity.this);
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    downX = ev.getX();
                    downY = ev.getY();
                    dragging = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    float dx = Math.abs(ev.getX() - downX);
                    float dy = ev.getY() - downY;
                    if (dy > touchSlop * 1.4f && dy > dx * 1.15f) {
                        dragging = true;
                        getParent().requestDisallowInterceptTouchEvent(true);
                        return true;
                    }
                    break;
            }
            return super.onInterceptTouchEvent(ev);
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_MOVE:
                    if (dragging) {
                        float dy = Math.max(0, ev.getY() - downY);
                        setTranslationY(dy);
                        if (activeSheetOverlay != null) {
                            float progress = Math.min(1f, dy / Math.max(dp(260), getHeight()));
                            activeSheetOverlay.setAlpha(1f - progress * .45f);
                        }
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (dragging) {
                        float dy = getTranslationY();
                        dragging = false;
                        if (dy > Math.max(dp(84), getHeight() * .20f)) {
                            dismissBottomSheet(true);
                        } else {
                            animate().translationY(0).setDuration(180).start();
                            if (activeSheetOverlay != null) activeSheetOverlay.animate().alpha(1f).setDuration(180).start();
                        }
                        return true;
                    }
                    break;
            }
            return super.onTouchEvent(ev);
        }
    }

    private GradientDrawable sheetBackground(int color) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        float r = dp(28);
        d.setCornerRadii(new float[]{r, r, r, r, 0, 0, 0, 0});
        return d;
    }

    private void showBottomSheet(String title, View content) {
        dismissBottomSheet(false);
        if (root == null) return;

        FrameLayout overlay = new FrameLayout(this);
        overlay.setBackgroundColor(Color.parseColor("#70000000"));
        overlay.setClickable(true);
        overlay.setFocusable(true);
        overlay.setOnClickListener(v -> dismissBottomSheet(true));

        DraggableSheet sheet = new DraggableSheet();
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setPadding(dp(18), dp(10), dp(18), dp(18));
        sheet.setBackground(sheetBackground(cSurface));
        sheet.setClickable(true);
        sheet.setOnClickListener(v -> {});

        View handle = new View(this);
        handle.setBackground(rounded(cBorder, 99));
        LinearLayout.LayoutParams handleLp = new LinearLayout.LayoutParams(dp(38), dp(4));
        handleLp.gravity = Gravity.CENTER_HORIZONTAL;
        handleLp.bottomMargin = dp(14);
        sheet.addView(handle, handleLp);

        if (title != null && !title.isEmpty()) {
            TextView tv = text(title, 19, cText);
            tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tlp.bottomMargin = dp(14);
            sheet.addView(tv, tlp);
        }
        sheet.addView(content, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        FrameLayout.LayoutParams sheetLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);
        overlay.addView(sheet, sheetLp);
        root.addView(overlay, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        activeSheetOverlay = overlay;
        activeSheetPanel = sheet;

        ViewCompat.setOnApplyWindowInsetsListener(sheet, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(dp(18), dp(10), dp(18), dp(18) + bars.bottom);
            return insets;
        });
        sheet.post(() -> {
            sheet.setTranslationY(Math.max(sheet.getHeight(), dp(360)));
            sheet.animate().translationY(0).setDuration(220).start();
        });
    }

    private void dismissBottomSheet(boolean animate) {
        if (activeSheetOverlay == null || root == null) return;
        View overlay = activeSheetOverlay;
        View panel = activeSheetPanel;
        activeSheetOverlay = null;
        activeSheetPanel = null;
        if (animate && panel != null) {
            panel.animate().translationY(Math.max(panel.getHeight(), dp(420))).setDuration(180)
                    .withEndAction(() -> root.removeView(overlay)).start();
            overlay.animate().alpha(0f).setDuration(180).start();
        } else {
            root.removeView(overlay);
        }
    }

    private View sheetAction(String label, String subtitle, Runnable action) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(12), dp(14), dp(12));
        row.setBackground(rounded(cSurface2, 16));
        row.setClickable(true);
        row.setFocusable(true);

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        row.addView(copy, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView t = text(label, 14.5f, cText);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        copy.addView(t);
        if (subtitle != null && !subtitle.isEmpty()) {
            TextView st = text(subtitle, 11.5f, cMuted);
            LinearLayout.LayoutParams stLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            stLp.topMargin = dp(3);
            copy.addView(st, stLp);
        }

        TextView arrow = text("›", 24, cMuted);
        row.addView(arrow);
        row.setOnClickListener(v -> {
            haptic(v);
            dismissBottomSheet(true);
            v.postDelayed(action, 120);
        });
        return row;
    }

    private void addSheetAction(LinearLayout list, String label, String subtitle, Runnable action) {
        View row = sheetAction(label, subtitle, action);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(8);
        list.addView(row, lp);
    }

    private View featureTile(int iconRes, String label, Runnable action) {
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(dp(8), dp(13), dp(8), dp(11));
        tile.setBackground(rounded(cSurface2, 18));
        tile.setClickable(true);
        tile.setFocusable(true);
        tile.setContentDescription(label);

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setColorFilter(cText);
        icon.setPadding(dp(7), dp(7), dp(7), dp(7));
        tile.addView(icon, new LinearLayout.LayoutParams(dp(38), dp(38)));

        TextView name = text(label, 11.5f, cText);
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        name.setGravity(Gravity.CENTER);
        name.setMaxLines(1);
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nlp.topMargin = dp(6);
        tile.addView(name, nlp);

        tile.setOnClickListener(v -> {
            haptic(v);
            dismissBottomSheet(true);
            v.postDelayed(action, 120);
        });
        return tile;
    }

    private void addFeatureTile(GridLayout grid, int iconRes, String label, Runnable action) {
        int width = (getResources().getDisplayMetrics().widthPixels - dp(52)) / 3;
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = width;
        lp.height = dp(82);
        lp.setMargins(dp(3), dp(4), dp(3), dp(4));
        grid.addView(featureTile(iconRes, label, action), lp);
    }

    private void showFeatureMenuSheet() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);

        TextView hint = text("Semua fitur Deapp", 12, cMuted);
        LinearLayout.LayoutParams hintLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hintLp.bottomMargin = dp(10);
        content.addView(hint, hintLp);

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(3);
        grid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        grid.setUseDefaultMargins(false);
        content.addView(grid, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        addFeatureTile(grid, R.drawable.ic_native_user, "Profil", this::openOwnProfile);
        addFeatureTile(grid, R.drawable.ic_native_message, "Pesan", () -> loadRelative("messages.php"));
        addFeatureTile(grid, R.drawable.ic_native_live, "Live", () -> loadRelative("live.php"));
        addFeatureTile(grid, R.drawable.ic_native_video, "Video", () -> loadRelative("reels.php"));
        addFeatureTile(grid, R.drawable.ic_native_community, "Komunitas", () -> loadRelative("community.php"));
        addFeatureTile(grid, R.drawable.ic_native_bookmark, "Tersimpan", () -> loadRelative("bookmarks.php"));
        addFeatureTile(grid, R.drawable.ic_native_tap, "Deapp Tap", () -> loadRelative("tap.php"));
        addFeatureTile(grid, R.drawable.ic_native_sparkles, "Deapp AI", () -> loadRelative("ai.php"));
        addFeatureTile(grid, R.drawable.ic_native_code, "Developer", () -> loadRelative("developer.php"));
        addFeatureTile(grid, R.drawable.ic_native_shop, "Toko", () -> loadRelative("shop.php"));
        addFeatureTile(grid, R.drawable.ic_native_game, "Mini Game", () -> loadRelative("games.php"));
        addFeatureTile(grid, R.drawable.ic_native_settings, "Pengaturan", () -> loadRelative("settings.php"));

        View actions = sheetAction("Ganti server", "Hosting, XAMPP atau alamat Deapp lain", this::showServerBottomSheet);
        LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        alp.topMargin = dp(10);
        content.addView(actions, alp);

        showBottomSheet("Menu", content);
    }

    private void showAccountSheet() {
        showFeatureMenuSheet();
    }

    private void showServerBottomSheet() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);

        TextView desc = text("Masukkan alamat server Deapp. Gunakan HTTPS untuk hosting publik atau IP LAN untuk XAMPP.", 12.5f, cMuted);
        desc.setLineSpacing(0, 1.12f);
        box.addView(desc);

        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(baseUrl);
        input.setHint("https://domain.com atau 192.168.1.10/deapp");
        input.setTextColor(cText);
        input.setHintTextColor(cMuted);
        input.setTextSize(14);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        input.setImeOptions(EditorInfo.IME_ACTION_GO);
        input.setPadding(dp(16), 0, dp(16), 0);
        input.setBackground(bordered(cSurface2, cBorder, 16));
        LinearLayout.LayoutParams inputLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(56));
        inputLp.topMargin = dp(14);
        box.addView(input, inputLp);

        Button save = new Button(this);
        save.setText("Simpan & buka");
        save.setAllCaps(false);
        save.setTextColor(Color.WHITE);
        save.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        save.setBackground(rounded(cAccent, 16));
        LinearLayout.LayoutParams saveLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(52));
        saveLp.topMargin = dp(12);
        box.addView(save, saveLp);

        View.OnClickListener submit = v -> {
            String u = normalizeUrl(input.getText().toString());
            if (u.isEmpty()) {
                input.setError("Masukkan alamat server Deapp");
                return;
            }
            baseUrl = u;
            prefs.edit().putString(KEY_URL, u).apply();
            dismissBottomSheet(true);
            if (webView != null) webView.postDelayed(() -> webView.loadUrl(u), 160);
        };
        save.setOnClickListener(submit);
        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                submit.onClick(input);
                return true;
            }
            return false;
        });
        showBottomSheet("Ganti server", box);
        input.postDelayed(input::requestFocus, 260);
    }

    private void showExitSheet() {
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        addSheetAction(list, "Tetap di Deapp", "Tutup lembar ini dan lanjut menggunakan aplikasi", () -> {});
        addSheetAction(list, "Ganti server", "Gunakan hosting atau server Deapp lain", this::showServerBottomSheet);
        addSheetAction(list, "Tutup aplikasi", "Keluar dari Deapp Lite", this::finish);
        showBottomSheet("Keluar dari Deapp?", list);
    }

    public class NativeBridge {
        @JavascriptInterface
        public void syncProfile(String avatarUrl, String href) {
            runOnUiThread(() -> syncNativeProfile(avatarUrl, href));
        }
    }

    private void syncNativeProfile(String avatarUrl, String href) {
        profileUrl = href == null ? "" : href.trim();
        if (profileAvatar == null) return;
        if (avatarUrl == null || avatarUrl.trim().isEmpty() || !(avatarUrl.startsWith("http://") || avatarUrl.startsWith("https://"))) {
            profileAvatar.setImageResource(R.drawable.ic_native_user);
            profileAvatar.setColorFilter(cMuted);
            profileAvatar.setPadding(dp(7), dp(7), dp(7), dp(7));
            return;
        }
        loadProfileAvatar(avatarUrl);
    }

    private void loadProfileAvatar(String avatarUrl) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(avatarUrl).openConnection();
                conn.setConnectTimeout(7000);
                conn.setReadTimeout(7000);
                conn.setInstanceFollowRedirects(true);
                String cookie = CookieManager.getInstance().getCookie(avatarUrl);
                if (cookie != null && !cookie.isEmpty()) conn.setRequestProperty("Cookie", cookie);
                conn.setRequestProperty("User-Agent", "DeappLite/1.3");
                try (InputStream in = conn.getInputStream()) {
                    Bitmap bitmap = BitmapFactory.decodeStream(in);
                    if (bitmap != null) runOnUiThread(() -> {
                        if (profileAvatar == null) return;
                        profileAvatar.clearColorFilter();
                        profileAvatar.setPadding(0, 0, 0, 0);
                        profileAvatar.setImageBitmap(bitmap);
                    });
                }
            } catch (Exception ignored) {
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    private void openWebDrawer() {
        if (webView == null) return;
        webView.evaluateJavascript("(function(){var b=document.getElementById('drawer-open');if(b){b.click();return true;}return false;})()", value -> {
            if (!"true".equals(value)) Toast.makeText(this, "Menu tersedia setelah login", Toast.LENGTH_SHORT).show();
        });
    }

    private void openComposer() {
        if (webView == null) return;
        webView.evaluateJavascript("(function(){var b=document.querySelector('[data-open-modal=\\\"composer-modal\\\"]');if(b){b.click();return true;}return false;})()", value -> {
            if (!"true".equals(value)) {
                Toast.makeText(this, "Login dulu untuk membuat kiriman", Toast.LENGTH_SHORT).show();
                loadRelative("login.php");
            }
        });
    }

    private void openOwnProfile() {
        if (webView == null) return;
        if (profileUrl != null && !profileUrl.isEmpty()) {
            webView.loadUrl(profileUrl);
            return;
        }
        webView.evaluateJavascript("(function(){var a=document.querySelector('#user-dropdown .dropdown-user,.bottom-nav a.bnav:last-child');return a?a.href:'';})()", value -> {
            String profile = jsonString(value);
            if (profile.isEmpty()) loadRelative("login.php"); else webView.loadUrl(profile);
        });
    }

    private String jsonString(String value) {
        try { return new JSONArray("[" + value + "]").getString(0); }
        catch (Exception e) { return ""; }
    }

    private void shareCurrentPage() {
        String url = webView != null && webView.getUrl() != null ? webView.getUrl() : baseUrl;
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, url);
        startActivity(Intent.createChooser(share, "Bagikan dari Deapp"));
    }

    private void loadRelative(String path) {
        if (webView == null) return;
        webView.loadUrl(baseUrl + path.replaceFirst("^/+", ""));
    }

    private void updateNativeNav(String url) {
        if (navHome == null || url == null) return;
        String low = url.toLowerCase(Locale.US);
        boolean home = low.endsWith("/") || low.contains("/index.php");
        navHome.setActive(home);
        if (navExplore != null) navExplore.setActive(low.contains("/explore.php") || low.contains("/hashtag.php"));
        if (navVideo != null) navVideo.setActive(low.contains("/reels.php"));
        if (navNotif != null) navNotif.setActive(low.contains("/notifications.php"));
        if (navProfile != null) navProfile.setActive(low.contains("/profile.php") || low.contains("/connections.php"));
    }

    private void updateKeepScreenOn(String url) {
        if (url != null && url.contains("/live")) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else if (customView == null) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void showOffline(boolean show) {
        if (offlineOverlay != null) offlineOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
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
        if (url == null || url.isEmpty()) return;
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
        catch (Exception e) { Toast.makeText(this, "Tidak ada aplikasi untuk membuka tautan", Toast.LENGTH_SHORT).show(); }
    }

    private void handleWebPermission(PermissionRequest request) {
        ArrayList<String> need = new ArrayList<>();
        for (String r : request.getResources()) {
            if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(r) && Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                need.add(Manifest.permission.CAMERA);
            }
            if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(r) && Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                need.add(Manifest.permission.RECORD_AUDIO);
            }
        }
        if (!need.isEmpty()) {
            pendingPermissionRequest = request;
            requestPermissions(need.toArray(new String[0]), REQ_WEBRTC);
        } else {
            request.grant(request.getResources());
        }
    }

    private void hideCustomView() {
        if (customView == null) return;
        root.removeView(customView);
        customView = null;
        shell.setVisibility(View.VISIBLE);
        if (composeFab != null) composeFab.setVisibility(View.VISIBLE);
        if (customViewCallback != null) customViewCallback.onCustomViewHidden();
        customViewCallback = null;
        updateKeepScreenOn(webView != null ? webView.getUrl() : "");
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
            boolean ok = grantResults.length > 0;
            for (int r : grantResults) if (r != PackageManager.PERMISSION_GRANTED) ok = false;
            if (ok) pendingPermissionRequest.grant(pendingPermissionRequest.getResources());
            else pendingPermissionRequest.deny();
            pendingPermissionRequest = null;
        } else if (requestCode == REQ_GEO && pendingGeoCallback != null) {
            boolean ok = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            pendingGeoCallback.invoke(pendingGeoOrigin, ok, false);
            pendingGeoCallback = null;
            pendingGeoOrigin = null;
        }
    }

    private void handleBack() {
        if (activeSheetOverlay != null) {
            dismissBottomSheet(true);
            return;
        }
        if (customView != null) {
            hideCustomView();
            return;
        }
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return;
        }
        showExitSheet();
    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT < 33) handleBack();
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

    private void destroyWebView() {
        composeFab = null;
        featureMenuButton = null;
        profileAvatar = null;
        navHome = null;
        navExplore = null;
        navVideo = null;
        navNotif = null;
        navProfile = null;
        if (webView != null) {
            webView.stopLoading();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.destroy();
            webView = null;
        }
    }

    @Override
    protected void onDestroy() {
        destroyWebView();
        super.onDestroy();
    }
}
