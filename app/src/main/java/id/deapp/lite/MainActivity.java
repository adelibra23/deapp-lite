package id.deapp.lite;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
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

import java.net.URI;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String PREFS = "deapp_lite";
    private static final String KEY_URL = "server_url";
    private static final int REQ_FILE = 2101;
    private static final int REQ_WEBRTC = 2102;
    private static final int REQ_GEO = 2103;

    private static final int MENU_FULL = 1;
    private static final int MENU_HOME = 2;
    private static final int MENU_EXPLORE = 3;
    private static final int MENU_LIVE = 4;
    private static final int MENU_TAP = 5;
    private static final int MENU_SETTINGS = 6;
    private static final int MENU_REFRESH = 7;
    private static final int MENU_SHARE = 8;
    private static final int MENU_BROWSER = 9;
    private static final int MENU_SERVER = 10;

    private FrameLayout root;
    private LinearLayout shell;
    private FrameLayout topContainer;
    private FrameLayout bottomContainer;
    private WebView webView;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progress;
    private FrameLayout offlineOverlay;
    private FrameLayout loadingOverlay;
    private TextView toolbarTitle;
    private View connectionDot;
    private ImageButton backButton;
    private NavItem navHome;
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
        cBg = Color.parseColor(dark ? "#090B10" : "#F6F8FC");
        cSurface = Color.parseColor(dark ? "#11141B" : "#FFFFFF");
        cSurface2 = Color.parseColor(dark ? "#1A1E28" : "#F0F3F9");
        cText = Color.parseColor(dark ? "#F3F5FA" : "#182033");
        cMuted = Color.parseColor(dark ? "#98A2B6" : "#6F7890");
        cBorder = Color.parseColor(dark ? "#252A36" : "#E3E8F1");
        cAccent = Color.parseColor("#3264F5");
        cAccentSoft = Color.parseColor(dark ? "#1E2E62" : "#E8EEFF");
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
        destroyWebView();
        readPalette();

        root = new FrameLayout(this);
        root.setBackgroundColor(cBg);
        setContentView(root);
        applySystemBarAppearance(root);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
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
        applyInsetsToShell();
        configureWebView();
        webView.loadUrl(baseUrl);
    }

    private void buildNativeTopBar() {
        topContainer = new FrameLayout(this);
        topContainer.setBackgroundColor(cSurface);
        shell.addView(topContainer, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(58)));

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(dp(7), 0, dp(7), 0);
        topContainer.addView(toolbar, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(58), Gravity.BOTTOM));

        backButton = iconButton(R.drawable.ic_native_back, "Kembali");
        backButton.setVisibility(View.GONE);
        backButton.setOnClickListener(v -> {
            haptic(v);
            if (webView != null && webView.canGoBack()) webView.goBack();
        });
        toolbar.addView(backButton);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.deapp_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LinearLayout.LayoutParams logoLp = new LinearLayout.LayoutParams(dp(34), dp(34));
        logoLp.leftMargin = dp(3);
        logoLp.rightMargin = dp(10);
        toolbar.addView(logo, logoLp);
        logo.setOnClickListener(v -> loadRelative("index.php"));

        LinearLayout titleWrap = new LinearLayout(this);
        titleWrap.setOrientation(LinearLayout.VERTICAL);
        titleWrap.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams twLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        toolbar.addView(titleWrap, twLp);

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        titleWrap.addView(titleRow, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        toolbarTitle = text("Deapp", 16, cText);
        toolbarTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        toolbarTitle.setMaxLines(1);
        titleRow.addView(toolbarTitle);

        connectionDot = new View(this);
        connectionDot.setBackground(rounded(cMuted, 99));
        LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dp(7), dp(7));
        dotLp.leftMargin = dp(7);
        titleRow.addView(connectionDot, dotLp);

        TextView subtitle = text("Native Lite", 10.5f, cMuted);
        LinearLayout.LayoutParams stLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        stLp.topMargin = dp(3);
        titleWrap.addView(subtitle, stLp);

        ImageButton search = iconButton(R.drawable.ic_native_search, "Jelajah");
        search.setOnClickListener(v -> {
            haptic(v);
            loadRelative("explore.php");
        });
        toolbar.addView(search);

        ImageButton message = iconButton(R.drawable.ic_native_message, "Pesan");
        message.setOnClickListener(v -> {
            haptic(v);
            loadRelative("messages.php");
        });
        toolbar.addView(message);

        ImageButton more = iconButton(R.drawable.ic_native_more, "Menu lainnya");
        more.setOnClickListener(v -> {
            haptic(v);
            showNativeMenu(more);
        });
        toolbar.addView(more);

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
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        layer.addView(box, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.deapp_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        box.addView(logo, new LinearLayout.LayoutParams(dp(74), dp(74)));

        ProgressBar spinner = new ProgressBar(this);
        spinner.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(cAccent));
        LinearLayout.LayoutParams spLp = new LinearLayout.LayoutParams(dp(32), dp(32));
        spLp.topMargin = dp(18);
        box.addView(spinner, spLp);

        TextView load = text("Membuka Deapp…", 12, cMuted);
        LinearLayout.LayoutParams loadLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadLp.topMargin = dp(12);
        box.addView(load, loadLp);
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
                ViewGroup.LayoutParams.MATCH_PARENT, dp(66)));

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(4), dp(3), dp(4), dp(3));
        bottomContainer.addView(nav, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(66), Gravity.TOP));

        View divider = new View(this);
        divider.setBackgroundColor(cBorder);
        bottomContainer.addView(divider, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1), Gravity.TOP));

        navHome = addNav(nav, R.drawable.ic_native_home, "Beranda", () -> loadRelative("index.php"));
        navVideo = addNav(nav, R.drawable.ic_native_video, "Video", () -> loadRelative("reels.php"));
        addComposeNav(nav);
        navNotif = addNav(nav, R.drawable.ic_native_bell, "Notif", () -> loadRelative("notifications.php"));
        navProfile = addNav(nav, R.drawable.ic_native_user, "Profil", this::openOwnProfile);
        updateNativeNav(baseUrl);
    }

    private NavItem addNav(LinearLayout parent, int iconRes, String label, Runnable action) {
        NavItem item = new NavItem(iconRes, label, action);
        parent.addView(item.root, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        return item;
    }

    private void addComposeNav(LinearLayout parent) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setGravity(Gravity.CENTER);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setClickable(true);
        wrap.setFocusable(true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        parent.addView(wrap, lp);

        ImageView plus = new ImageView(this);
        plus.setImageResource(R.drawable.ic_native_add);
        plus.setPadding(dp(12), dp(12), dp(12), dp(12));
        plus.setBackground(rounded(cAccent, 17));
        wrap.addView(plus, new LinearLayout.LayoutParams(dp(48), dp(48)));
        wrap.setOnClickListener(v -> {
            haptic(v);
            openComposer();
        });
    }

    private class NavItem {
        final LinearLayout root;
        final ImageView icon;
        final TextView label;

        NavItem(int iconRes, String labelText, Runnable action) {
            root = new LinearLayout(MainActivity.this);
            root.setOrientation(LinearLayout.VERTICAL);
            root.setGravity(Gravity.CENTER);
            root.setPadding(dp(2), dp(4), dp(2), dp(3));
            root.setClickable(true);
            root.setFocusable(true);

            icon = new ImageView(MainActivity.this);
            icon.setImageResource(iconRes);
            icon.setColorFilter(cMuted);
            LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(24), dp(24));
            root.addView(icon, iconLp);

            label = text(labelText, 10, cMuted);
            label.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            labelLp.topMargin = dp(3);
            root.addView(label, labelLp);

            root.setOnClickListener(v -> {
                haptic(v);
                action.run();
            });
        }

        void setActive(boolean active) {
            icon.setColorFilter(active ? cAccent : cMuted);
            label.setTextColor(active ? cAccent : cMuted);
            root.setBackground(active ? rounded(cAccentSoft, 18) : rounded(Color.TRANSPARENT, 18));
        }
    }

    private void applyInsetsToShell() {
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            if (topContainer != null) {
                topContainer.setPadding(0, bars.top, 0, 0);
                ViewGroup.LayoutParams lp = topContainer.getLayoutParams();
                lp.height = dp(58) + bars.top;
                topContainer.setLayoutParams(lp);
            }
            if (bottomContainer != null) {
                bottomContainer.setPadding(0, 0, 0, bars.bottom);
                ViewGroup.LayoutParams lp = bottomContainer.getLayoutParams();
                lp.height = dp(66) + bars.bottom;
                bottomContainer.setLayoutParams(lp);
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
        s.setUserAgentString(s.getUserAgentString() + " DeappLite/1.1 NativeShell/1");
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
                connectionDot.setBackground(rounded(cMuted, 99));
                progress.setVisibility(View.VISIBLE);
                showOffline(false);
                updateNativeNav(url);
                updateBackButton();
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
                connectionDot.setBackground(rounded(cSuccess, 99));
                progress.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                loadingOverlay.setVisibility(View.GONE);
                updateNativeNav(url);
                updateBackButton();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    swipeRefresh.setRefreshing(false);
                    loadingOverlay.setVisibility(View.GONE);
                    connectionDot.setBackground(rounded(cDanger, 99));
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
            public void onReceivedTitle(WebView view, String title) {
                if (title == null || title.trim().isEmpty()) return;
                String clean = title.replace("· Deapp", "").replace("| Deapp", "").trim();
                if (clean.length() > 28) clean = clean.substring(0, 28) + "…";
                toolbarTitle.setText(clean.isEmpty() ? "Deapp" : clean);
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
                "'body{padding-top:0!important;padding-bottom:0!important;-webkit-tap-highlight-color:transparent}' +" +
                "'.layout,.layout-guest{padding-top:12px!important;padding-bottom:22px!important}' +" +
                "'.tabs,.nx-studio-nav{top:0!important}' +" +
                "'html{overscroll-behavior-y:none}';document.head.appendChild(s);}" +
                "document.documentElement.classList.add('deapp-native-shell');" +
                "return true;})()";
        webView.evaluateJavascript(js, null);
    }

    private void showNativeMenu(View anchor) {
        PopupMenu p = new PopupMenu(this, anchor);
        Menu m = p.getMenu();
        m.add(Menu.NONE, MENU_FULL, 0, "Menu Deapp");
        m.add(Menu.NONE, MENU_HOME, 1, "Beranda");
        m.add(Menu.NONE, MENU_EXPLORE, 2, "Jelajah");
        m.add(Menu.NONE, MENU_LIVE, 3, "Live");
        m.add(Menu.NONE, MENU_TAP, 4, "Deapp Tap");
        m.add(Menu.NONE, MENU_SETTINGS, 5, "Pengaturan");
        m.add(Menu.NONE, MENU_REFRESH, 6, "Muat ulang");
        m.add(Menu.NONE, MENU_SHARE, 7, "Bagikan halaman");
        m.add(Menu.NONE, MENU_BROWSER, 8, "Buka di browser");
        m.add(Menu.NONE, MENU_SERVER, 9, "Ganti server");
        p.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case MENU_FULL: openWebDrawer(); return true;
                case MENU_HOME: loadRelative("index.php"); return true;
                case MENU_EXPLORE: loadRelative("explore.php"); return true;
                case MENU_LIVE: loadRelative("live.php"); return true;
                case MENU_TAP: loadRelative("tap.php"); return true;
                case MENU_SETTINGS: loadRelative("settings.php"); return true;
                case MENU_REFRESH: if (webView != null) webView.reload(); return true;
                case MENU_SHARE: shareCurrentPage(); return true;
                case MENU_BROWSER:
                    if (webView != null) openExternal(webView.getUrl());
                    return true;
                case MENU_SERVER: showServerSetup(true); return true;
            }
            return false;
        });
        p.show();
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
        webView.evaluateJavascript("(function(){var a=document.querySelector('.bottom-nav a.bnav:last-child');return a?a.href:'';})()", value -> {
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
        navVideo.setActive(low.contains("/reels.php"));
        navNotif.setActive(low.contains("/notifications.php"));
        navProfile.setActive(low.contains("/profile.php"));
    }

    private void updateBackButton() {
        if (backButton != null && webView != null) {
            backButton.setVisibility(webView.canGoBack() ? View.VISIBLE : View.GONE);
        }
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
        if (customView != null) {
            hideCustomView();
            return;
        }
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Deapp")
                .setMessage("Tutup Deapp Lite atau ganti alamat server?")
                .setPositiveButton("Tutup", (d, w) -> finish())
                .setNeutralButton("Ganti server", (d, w) -> showServerSetup(true))
                .setNegativeButton("Batal", null)
                .show();
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
