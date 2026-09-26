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
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.provider.Settings;
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
import android.view.animation.LinearInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
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
import java.io.BufferedReader;
import java.io.InputStreamReader;
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
    private static final int REQ_APP_PERMISSION = 2104;

    private FrameLayout root;
    private LinearLayout shell;
    private FrameLayout topContainer;
    private FrameLayout bottomContainer;
    private WebView webView;
    private SwipeRefreshLayout swipeRefresh;
    private FrameLayout offlineOverlay;
    private FrameLayout loadingOverlay;
    private ImageView loadingLogo;
    private boolean loadingLogoPulseUp = true;
    private FrameLayout refreshIndicator;
    private ImageView refreshIcon;
    private boolean refreshIconAnimating = false;
    private FrameLayout startupSplashOverlay;
    private ImageView startupSplashLogo;
    private boolean startupSplashVisible = false;
    private boolean startupSplashShownThisActivity = false;
    private int loadingGeneration = 0;
    private ImageButton featureMenuButton;
    private ImageButton composeFab;
    private ImageView toolbarLogo;
    private TextView toolbarTitle;
    private String profileUrl = "";
    private String profileDisplayName = "";
    private boolean profilePage = false;
    private boolean ownProfilePage = false;
    private boolean postDetailPage = false;
    private boolean reelsPage = false;
    private String pageChromeType = "";
    private boolean composerOpen = false;
    private boolean webSheetOpen = false;
    private boolean uiScrolling = false;
    private int scrollUiGeneration = 0;
    private View activeSheetOverlay;
    private View activeSheetPanel;
    private NavItem navHome;
    private NavItem navMessage;
    private NavItem navCompose;
    private NavItem navNotif;
    private NavItem navProfile;
    private boolean isLoggedIn = false;
    private boolean imeVisible = false;
    private String currentUrl = "";
    private String nativeScript;

    private SharedPreferences prefs;
    private ValueCallback<Uri[]> fileCallback;
    private PermissionRequest pendingPermissionRequest;
    private GeolocationPermissions.Callback pendingGeoCallback;
    private String pendingGeoOrigin;
    private String pendingPermissionLabel = "";
    private long lastPostPublishedSoundAt = 0L;
    private long lastReactionFeedbackAt = 0L;
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
        boolean systemDark = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        setPalette(systemDark);
    }

    private void setPalette(boolean isDark) {
        dark = isDark;
        // Palet monokrom ala Threads. Mode web Deapp dapat menyinkronkan nilai ini saat runtime.
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
        isLoggedIn = false;
        imeVisible = false;
        currentUrl = baseUrl;
        profileUrl = "";
        profileDisplayName = "";
        profilePage = false;
        ownProfilePage = false;
        postDetailPage = false;
        reelsPage = false;
        pageChromeType = "";
        composerOpen = false;
        webSheetOpen = false;

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
        // v1.9.7: splash awal tampil penuh selama 5 detik. WebView tetap memuat di belakang
        // agar setelah splash selesai pengguna langsung melihat halaman yang sudah siap.
        showStartupSplash();
        showLoading();
        webView.loadUrl(baseUrl);
    }

    private void buildNativeTopBar() {
        topContainer = new FrameLayout(this);
        topContainer.setBackgroundColor(cSurface);
        topContainer.setVisibility(View.GONE); // header hanya muncul setelah status login diketahui
        shell.addView(topContainer, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(56)));

        FrameLayout toolbar = new FrameLayout(this);
        toolbar.setPadding(dp(12), 0, dp(8), 0);
        topContainer.addView(toolbar, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(56), Gravity.BOTTOM));

        toolbarLogo = new ImageView(this);
        toolbarLogo.setImageResource(R.drawable.ic_native_search);
        toolbarLogo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        toolbarLogo.setColorFilter(cText);
        toolbarLogo.setPadding(dp(8), dp(8), dp(8), dp(8));
        toolbarLogo.setContentDescription("Cari di Deapp");
        toolbarLogo.setClickable(true);
        toolbarLogo.setFocusable(true);
        FrameLayout.LayoutParams leftLp = new FrameLayout.LayoutParams(dp(36), dp(36), Gravity.START | Gravity.CENTER_VERTICAL);
        toolbar.addView(toolbarLogo, leftLp);
        toolbarLogo.setOnClickListener(v -> {
            haptic(v);
            if (composerOpen) {
                closeComposer();
            } else if (postDetailPage) {
                if (webView != null && webView.canGoBack()) webView.goBack();
                else loadRelative("index.php");
            } else {
                loadRelative("explore.php");
            }
        });

        toolbarTitle = text("Deapp", 20, cText);
        toolbarTitle.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        toolbarTitle.setLetterSpacing(-0.02f);
        toolbarTitle.setGravity(Gravity.CENTER);
        toolbarTitle.setMaxLines(1);
        toolbarTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
        FrameLayout.LayoutParams wordLp = new FrameLayout.LayoutParams(
                dp(220), dp(56), Gravity.CENTER);
        toolbar.addView(toolbarTitle, wordLp);

        featureMenuButton = iconButton(R.drawable.ic_native_grid, "Menu fitur Deapp");
        featureMenuButton.setColorFilter(cText);
        featureMenuButton.setBackground(rounded(Color.TRANSPARENT, 99));
        FrameLayout.LayoutParams menuLp = new FrameLayout.LayoutParams(dp(44), dp(44), Gravity.END | Gravity.CENTER_VERTICAL);
        toolbar.addView(featureMenuButton, menuLp);
        featureMenuButton.setOnClickListener(v -> {
            haptic(v);
            if (composerOpen) publishComposer();
            else if (postDetailPage) return;
            else if (profilePage) openProfileOptions();
            else showFeatureMenuSheet();
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
        // v1.9.12: gesture refresh tetap native, tetapi spinner bawaan dibuat transparan
        // dan diganti indikator ikon refresh yang lebih bersih di atas WebView.
        swipeRefresh.setSize(SwipeRefreshLayout.DEFAULT);
        swipeRefresh.setColorSchemeColors(Color.TRANSPARENT);
        swipeRefresh.setProgressBackgroundColorSchemeColor(Color.TRANSPARENT);
        swipeRefresh.setDistanceToTriggerSync(dp(72));
        swipeRefresh.setSlingshotDistance(dp(92));
        swipeRefresh.setProgressViewOffset(false, dp(6), dp(58));
        content.addView(swipeRefresh, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        webView = new WebView(this);
        webView.setBackgroundColor(cBg);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setScrollbarFadingEnabled(true);
        webView.addJavascriptInterface(new NativeBridge(), "DeappNative");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            webView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if (Math.abs(scrollY - oldScrollY) < dp(1)) return;
                final int generation = ++scrollUiGeneration;
                setUiScrolling(true);
                webView.postDelayed(() -> {
                    if (generation == scrollUiGeneration) setUiScrolling(false);
                }, 190);
            });
        }
        swipeRefresh.addView(webView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        swipeRefresh.setOnRefreshListener(() -> {
            haptic(swipeRefresh);
            showRefreshIndicator(true);
            refreshCurrentPage();
        });
        swipeRefresh.setOnChildScrollUpCallback((parent, child) ->
                activeSheetOverlay != null || webSheetOpen || composerOpen ||
                        (webView != null && webView.canScrollVertically(-1)));

        refreshIndicator = buildRefreshIndicator();
        FrameLayout.LayoutParams refreshLp = new FrameLayout.LayoutParams(dp(46), dp(46), Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        refreshLp.topMargin = dp(10);
        content.addView(refreshIndicator, refreshLp);

        loadingOverlay = buildLoadingOverlay();
        content.addView(loadingOverlay, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        offlineOverlay = buildOfflineOverlay();
        offlineOverlay.setVisibility(View.GONE);
        content.addView(offlineOverlay, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void showStartupSplash() {
        if (root == null || startupSplashShownThisActivity) return;
        startupSplashShownThisActivity = true;
        if (startupSplashOverlay != null) removeViewFromParent(startupSplashOverlay);

        startupSplashVisible = true;
        FrameLayout layer = new FrameLayout(this);
        layer.setBackgroundColor(cBg);
        layer.setClickable(true);
        layer.setFocusable(true);
        layer.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);

        LinearLayout center = new LinearLayout(this);
        center.setOrientation(LinearLayout.VERTICAL);
        center.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams centerLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        layer.addView(center, centerLp);

        startupSplashLogo = new ImageView(this);
        startupSplashLogo.setImageResource(R.drawable.deapp_logo);
        startupSplashLogo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        startupSplashLogo.setContentDescription("Deapp");
        // v1.9.18: splash fokus pada logo Deapp saja — scale kecil, overshoot, settle, lalu breathing halus.
        startupSplashLogo.setScaleX(.52f);
        startupSplashLogo.setScaleY(.52f);
        startupSplashLogo.setRotation(-12f);
        startupSplashLogo.setAlpha(0f);
        center.addView(startupSplashLogo, new LinearLayout.LayoutParams(dp(100), dp(100)));

        root.addView(layer, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        startupSplashOverlay = layer;

        startupSplashLogo.animate().cancel();
        startupSplashLogo.animate()
                .scaleX(1.08f).scaleY(1.08f).rotation(2f).alpha(1f)
                .setDuration(520)
                .setInterpolator(new OvershootInterpolator(1.18f))
                .withEndAction(() -> {
                    if (!startupSplashVisible || startupSplashLogo == null) return;
                    startupSplashLogo.animate()
                            .scaleX(1f).scaleY(1f).rotation(0f).alpha(1f)
                            .setDuration(230)
                            .setInterpolator(new DecelerateInterpolator())
                            .withEndAction(this::startStartupLogoAnimation)
                            .start();
                }).start();
        // Total splash sekitar 5 detik: fade dimulai pada 4,7 dtk dan selesai tepat di sekitar 5 dtk.
        layer.postDelayed(() -> {
            if (startupSplashOverlay != layer || !startupSplashVisible) return;
            layer.animate().alpha(0f).setDuration(300).withEndAction(() -> {
                if (startupSplashOverlay == layer) {
                    removeViewFromParent(layer);
                    startupSplashOverlay = null;
                    startupSplashLogo = null;
                }
                startupSplashVisible = false;
            }).start();
        }, 4700);
    }

    private void startStartupLogoAnimation() {
        if (!startupSplashVisible || startupSplashLogo == null || startupSplashOverlay == null) return;
        startupSplashLogo.animate().cancel();
        startupSplashLogo.animate()
                .scaleX(1.035f).scaleY(1.035f).alpha(1f)
                .setDuration(900)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    if (!startupSplashVisible || startupSplashLogo == null) return;
                    startupSplashLogo.animate()
                            .scaleX(.985f).scaleY(.985f).alpha(.94f)
                            .setDuration(900)
                            .setInterpolator(new DecelerateInterpolator())
                            .withEndAction(this::startStartupLogoAnimation)
                            .start();
                }).start();
    }

    private FrameLayout buildRefreshIndicator() {
        FrameLayout holder = new FrameLayout(this);
        holder.setBackground(bordered(cSurface, cBorder, 99));
        holder.setElevation(dp(8));
        holder.setVisibility(View.GONE);
        holder.setAlpha(0f);
        holder.setScaleX(.84f);
        holder.setScaleY(.84f);
        holder.setClickable(false);
        holder.setFocusable(false);

        refreshIcon = new ImageView(this);
        refreshIcon.setImageResource(R.drawable.ic_native_refresh);
        refreshIcon.setColorFilter(cText);
        refreshIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        refreshIcon.setPadding(dp(11), dp(11), dp(11), dp(11));
        refreshIcon.setContentDescription("Memperbarui halaman");
        holder.addView(refreshIcon, new FrameLayout.LayoutParams(dp(46), dp(46), Gravity.CENTER));
        return holder;
    }

    private void showRefreshIndicator(boolean show) {
        if (refreshIndicator == null) return;
        refreshIndicator.animate().cancel();
        if (show) {
            refreshIndicator.setVisibility(View.VISIBLE);
            refreshIndicator.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(150).start();
            refreshIconAnimating = true;
            startRefreshIconAnimation();
        } else {
            refreshIconAnimating = false;
            if (refreshIcon != null) refreshIcon.animate().cancel();
            refreshIndicator.animate().alpha(0f).scaleX(.88f).scaleY(.88f).setDuration(140)
                    .withEndAction(() -> {
                        if (!refreshIconAnimating && refreshIndicator != null) refreshIndicator.setVisibility(View.GONE);
                    }).start();
        }
    }

    private void startRefreshIconAnimation() {
        if (!refreshIconAnimating || refreshIcon == null || refreshIndicator == null || refreshIndicator.getVisibility() != View.VISIBLE) return;
        refreshIcon.animate().cancel();
        refreshIcon.animate().rotationBy(360f).setDuration(620).setInterpolator(new LinearInterpolator())
                .withEndAction(this::startRefreshIconAnimation).start();
    }

    private void refreshCurrentPage() {
        if (webView == null) return;
        String active = webView.getUrl();
        if (active != null && !active.trim().isEmpty()) currentUrl = active;
        // reload() mempertahankan URL aktif lengkap (query/hash) tanpa menambah entri history baru.
        webView.reload();
    }

    private FrameLayout buildLoadingOverlay() {
        FrameLayout layer = new FrameLayout(this);
        layer.setBackgroundColor(Color.TRANSPARENT);
        layer.setClickable(false);
        layer.setFocusable(false);

        FrameLayout holder = new FrameLayout(this);
        holder.setBackground(bordered(cSurface, cBorder, 24));
        holder.setElevation(dp(4));
        FrameLayout.LayoutParams holderLp = new FrameLayout.LayoutParams(dp(76), dp(76), Gravity.CENTER);
        layer.addView(holder, holderLp);

        loadingLogo = new ImageView(this);
        loadingLogo.setImageResource(R.drawable.deapp_logo);
        loadingLogo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        loadingLogo.setContentDescription("Memuat Deapp");
        loadingLogo.setScaleX(.88f);
        loadingLogo.setScaleY(.88f);
        loadingLogo.setAlpha(.72f);
        FrameLayout.LayoutParams logoLp = new FrameLayout.LayoutParams(dp(48), dp(48), Gravity.CENTER);
        holder.addView(loadingLogo, logoLp);
        return layer;
    }

    private void startLoadingLogoAnimation() {
        if (loadingLogo == null || loadingOverlay == null || loadingOverlay.getVisibility() != View.VISIBLE) return;
        final float target = loadingLogoPulseUp ? 1.06f : .88f;
        final float alpha = loadingLogoPulseUp ? 1f : .72f;
        loadingLogoPulseUp = !loadingLogoPulseUp;
        loadingLogo.animate().cancel();
        loadingLogo.animate().scaleX(target).scaleY(target).alpha(alpha).setDuration(420)
                .withEndAction(this::startLoadingLogoAnimation).start();
    }

    private void showLoading() {
        if (loadingOverlay == null) return;
        loadingOverlay.setVisibility(View.VISIBLE);
        if (loadingLogo != null) {
            loadingLogo.animate().cancel();
            loadingLogoPulseUp = true;
            startLoadingLogoAnimation();
        }
    }

    private void hideLoading() {
        if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
        if (loadingLogo != null) {
            loadingLogo.animate().cancel();
            loadingLogo.setScaleX(1f);
            loadingLogo.setScaleY(1f);
            loadingLogo.setAlpha(1f);
        }
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
        bottomContainer.setVisibility(View.GONE); // jangan tampil sebelum status login dari halaman Deapp diketahui
        shell.addView(bottomContainer, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(62)));

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(5), dp(4), dp(5), dp(4));
        bottomContainer.addView(nav, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(62), Gravity.TOP));

        View divider = new View(this);
        divider.setBackgroundColor(cBorder);
        bottomContainer.addView(divider, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1), Gravity.TOP));

        // 5 menu utama: Beranda, Pesan, Buat, Notifikasi, Profil.
        navHome = addNav(nav, R.drawable.ic_native_home, "Beranda", false, () -> loadRelative("index.php"));
        navMessage = addNav(nav, R.drawable.ic_native_message, "Pesan", false, () -> loadRelative("messages.php"));
        navCompose = addNav(nav, R.drawable.ic_native_add, "Buat kiriman", true, this::openComposer);
        navNotif = addNav(nav, R.drawable.ic_native_bell, "Notifikasi", false, () -> loadRelative("notifications.php"));
        navProfile = addNav(nav, R.drawable.ic_native_user, "Profil", false, this::openOwnProfile);
        updateNativeNav(baseUrl);
    }

    private NavItem addNav(LinearLayout parent, int iconRes, String description, boolean primary, Runnable action) {
        NavItem item = new NavItem(iconRes, description, primary, action);
        parent.addView(item.root, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        return item;
    }

    private void buildFloatingComposer() {
        composeFab = new ImageButton(this);
        composeFab.setImageResource(R.drawable.ic_native_add);
        composeFab.setColorFilter(dark ? Color.BLACK : Color.WHITE);
        composeFab.setContentDescription("Buat kiriman dari Beranda");
        composeFab.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        composeFab.setPadding(dp(15), dp(15), dp(15), dp(15));
        composeFab.setBackground(rounded(dark ? Color.WHITE : Color.BLACK, 18));
        composeFab.setElevation(dp(8));
        composeFab.setStateListAnimator(null);
        composeFab.setVisibility(View.GONE);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(56), dp(56), Gravity.END | Gravity.BOTTOM);
        lp.rightMargin = dp(17);
        lp.bottomMargin = dp(76);
        root.addView(composeFab, lp);
        composeFab.setOnClickListener(v -> {
            haptic(v);
            openComposer();
        });
    }

    private class NavItem {
        final FrameLayout root;
        final ImageView icon;
        final boolean primary;
        Bitmap avatarBitmap;
        boolean activeState = false;

        NavItem(int iconRes, String description, boolean primary, Runnable action) {
            this.primary = primary;
            root = new FrameLayout(MainActivity.this);
            root.setClickable(true);
            root.setFocusable(true);
            root.setContentDescription(description);

            icon = new ImageView(MainActivity.this);
            icon.setImageResource(iconRes);
            icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            icon.setClipToOutline(true);
            icon.setColorFilter(primary ? cBg : cMuted);
            // v1.9.14: ikon bottom navigation dibuat sedikit lebih besar tanpa menaikkan tinggi bar.
            icon.setPadding(primary ? dp(9) : dp(7), primary ? dp(9) : dp(7), primary ? dp(9) : dp(7), primary ? dp(9) : dp(7));
            icon.setBackground(primary ? rounded(cText, 99) : rounded(Color.TRANSPARENT, 14));
            FrameLayout.LayoutParams iconLp = new FrameLayout.LayoutParams(primary ? dp(44) : dp(42), primary ? dp(44) : dp(42), Gravity.CENTER);
            root.addView(icon, iconLp);

            root.setOnClickListener(v -> {
                haptic(v);
                action.run();
            });
        }

        void setAvatarBitmap(Bitmap bitmap) {
            if (primary || bitmap == null) return;
            avatarBitmap = bitmap;
            icon.clearColorFilter();
            icon.setPadding(0, 0, 0, 0);
            icon.setImageBitmap(bitmap);
            icon.setBackground(bordered(cSurface, cBorder, 99));
        }

        void clearAvatar(int fallbackRes) {
            if (primary) return;
            avatarBitmap = null;
            icon.setImageResource(fallbackRes);
            icon.setPadding(dp(9), dp(9), dp(9), dp(9));
            icon.setBackground(rounded(Color.TRANSPARENT, 14));
        }

        void setActive(boolean active) {
            activeState = active;
            if (primary) {
                icon.setColorFilter(cBg);
                icon.setAlpha(1f);
                icon.setBackground(rounded(cText, 99));
                icon.setScaleX(1f);
                icon.setScaleY(1f);
                return;
            }
            if (avatarBitmap != null) {
                icon.clearColorFilter();
                icon.setAlpha(active ? 1f : .82f);
                icon.setScaleX(active ? 1.04f : 1f);
                icon.setScaleY(active ? 1.04f : 1f);
                icon.setBackground(bordered(cSurface, active ? cText : cBorder, 99));
                return;
            }
            icon.setColorFilter(active ? cText : cMuted);
            icon.setAlpha(active ? 1f : .76f);
            icon.setBackground(active ? rounded(cSurface2, 14) : rounded(Color.TRANSPARENT, 14));
            icon.setScaleX(active ? 1.03f : 1f);
            icon.setScaleY(active ? 1.03f : 1f);
        }

        void refreshTheme() {
            if (avatarBitmap != null && !primary) {
                icon.clearColorFilter();
                icon.setBackground(bordered(cSurface, activeState ? cText : cBorder, 99));
            }
            setActive(activeState);
        }
    }

    private void applyInsetsToShell() {
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            if (topContainer != null) {
                topContainer.setPadding(0, bars.top, 0, 0);
                ViewGroup.LayoutParams lp = topContainer.getLayoutParams();
                lp.height = dp(56) + bars.top;
                topContainer.setLayoutParams(lp);
            }
            if (bottomContainer != null) {
                bottomContainer.setPadding(0, 0, 0, bars.bottom);
                ViewGroup.LayoutParams lp = bottomContainer.getLayoutParams();
                lp.height = dp(62) + bars.bottom;
                bottomContainer.setLayoutParams(lp);
            }
            if (composeFab != null) {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) composeFab.getLayoutParams();
                lp.rightMargin = dp(17);
                lp.bottomMargin = dp(76) + bars.bottom;
                composeFab.setLayoutParams(lp);
            }
            updateChromeVisibility();
            return insets;
        });
    }

    private boolean isHomeUrl(String url) {
        if (url == null || url.trim().isEmpty()) return true;
        try {
            URI u = URI.create(url);
            String path = u.getPath() == null ? "" : u.getPath().toLowerCase(Locale.US);
            String basePath = URI.create(baseUrl).getPath();
            if (basePath == null) basePath = "/";
            String bp = basePath.toLowerCase(Locale.US);
            if (!bp.endsWith("/")) bp += "/";
            return path.equals(bp) || path.equals(bp + "index.php") || path.endsWith("/index.php");
        } catch (Exception e) {
            String low = url.toLowerCase(Locale.US);
            return low.endsWith("/") || low.contains("/index.php");
        }
    }

    private boolean isProfileUrl(String url) {
        if (url == null) return false;
        try {
            String path = URI.create(url).getPath();
            return path != null && path.toLowerCase(Locale.US).endsWith("/profile.php");
        } catch (Exception e) {
            return url.toLowerCase(Locale.US).contains("/profile.php");
        }
    }

    private boolean isPostUrl(String url) {
        if (url == null) return false;
        try {
            String path = URI.create(url).getPath();
            return path != null && path.toLowerCase(Locale.US).endsWith("/post.php");
        } catch (Exception e) {
            return url.toLowerCase(Locale.US).contains("/post.php");
        }
    }

    private boolean isNotificationsUrl(String url) {
        if (url == null) return false;
        try {
            String path = URI.create(url).getPath();
            return path != null && path.toLowerCase(Locale.US).endsWith("/notifications.php");
        } catch (Exception e) {
            return url.toLowerCase(Locale.US).contains("/notifications.php");
        }
    }

    private boolean isReelsUrl(String url) {
        if (url == null) return false;
        try {
            String path = URI.create(url).getPath();
            return path != null && path.toLowerCase(Locale.US).endsWith("/reels.php");
        } catch (Exception e) {
            return url.toLowerCase(Locale.US).contains("/reels.php");
        }
    }

    private boolean isSpecialSectionUrl(String url) {
        if (url == null) return false;
        try {
            String path = URI.create(url).getPath();
            if (path == null) return false;
            String p = path.toLowerCase(Locale.US);
            return p.endsWith("/messages.php") || p.endsWith("/notifications.php") || p.endsWith("/live.php") ||
                    p.endsWith("/ai.php") || p.endsWith("/shop.php") || p.endsWith("/settings.php") ||
                    p.endsWith("/security.php") || p.endsWith("/security-password.php") || p.endsWith("/security-email.php") ||
                    p.endsWith("/security-wallet-pin.php") || p.endsWith("/security-2fa.php") || p.endsWith("/security-sessions.php") ||
                    p.endsWith("/help.php") || p.endsWith("/privacy.php") || p.endsWith("/policy.php") ||
                    p.endsWith("/cookies.php") || p.endsWith("/profile-edit.php");
        } catch (Exception e) {
            String low = url.toLowerCase(Locale.US);
            return low.contains("/messages.php") || low.contains("/notifications.php") || low.contains("/live.php") ||
                    low.contains("/ai.php") || low.contains("/shop.php") || low.contains("/settings.php") ||
                    low.contains("/security.php") || low.contains("/security-password.php") || low.contains("/security-email.php") ||
                    low.contains("/security-wallet-pin.php") || low.contains("/security-2fa.php") || low.contains("/security-sessions.php") ||
                    low.contains("/help.php") || low.contains("/privacy.php") || low.contains("/policy.php") ||
                    low.contains("/cookies.php") || low.contains("/profile-edit.php");
        }
    }

    private boolean isSpecialWebChromeType(String type) {
        if (type == null) return false;
        String t = type.trim().toLowerCase(Locale.US);
        return "story".equals(t) || "messages".equals(t) || "notifications".equals(t) || "live".equals(t) ||
                "ai".equals(t) || "shop".equals(t) || "settings".equals(t);
    }

    private boolean isAuthUrl(String url) {
        if (url == null) return false;
        String low = url.toLowerCase(Locale.US);
        return low.contains("/login.php") || low.contains("/register.php") || low.contains("/forgot-password.php");
    }

    private void updateChromeVisibility() {
        boolean fullscreen = customView != null;
        boolean authPage = isAuthUrl(currentUrl);
        boolean shortVideoPage = reelsPage || isReelsUrl(currentUrl);
        boolean specialWebChrome = isSpecialWebChromeType(pageChromeType) || isSpecialSectionUrl(currentUrl);
        boolean notificationPage = "notifications".equalsIgnoreCase(pageChromeType) || isNotificationsUrl(currentUrl);
        boolean messagesPage = "messages".equalsIgnoreCase(pageChromeType) || (currentUrl != null && currentUrl.toLowerCase(Locale.US).contains("/messages.php"));
        // v1.9.14: Notifikasi dan Chat tetap memakai header web khusus, tetapi footer memakai
        // bottom navigation umum Android. Profil memang sejak awal memakai chrome native umum.
        boolean specialWebOwnsBottom = specialWebChrome && !(notificationPage || messagesPage);
        boolean homePage = isHomeUrl(currentUrl);
        if (refreshIndicator != null && refreshIndicator.getLayoutParams() instanceof FrameLayout.LayoutParams) {
            FrameLayout.LayoutParams rlp = (FrameLayout.LayoutParams) refreshIndicator.getLayoutParams();
            int wantedTop = (isSpecialWebChromeType(pageChromeType) || isSpecialSectionUrl(currentUrl)) ? dp(62) : dp(10);
            if (rlp.topMargin != wantedTop) {
                rlp.topMargin = wantedTop;
                refreshIndicator.setLayoutParams(rlp);
            }
        }

        // Reels serta section khusus v1.9.6+ menggambar header/footer kontekstual di dalam WebView.
        // Chrome Android generik disembunyikan supaya tidak ada dua toolbar/nav yang bertumpuk.
        boolean showTop = isLoggedIn && !fullscreen && !authPage && !shortVideoPage && !specialWebChrome;
        if (topContainer != null) topContainer.setVisibility(showTop ? View.VISIBLE : View.GONE);
        updateTopBarMode();

        boolean sheetActive = activeSheetOverlay != null || webSheetOpen;
        boolean showBottom = isLoggedIn && !imeVisible && !fullscreen && !composerOpen && !authPage && !postDetailPage && !shortVideoPage && !specialWebOwnsBottom && !sheetActive;
        if (bottomContainer != null) bottomContainer.setVisibility(showBottom ? View.VISIBLE : View.GONE);
        // v1.9.16: bottom navigation umum selalu mempertahankan lima slot:
        // Beranda · Chat · + Postingan · Notifikasi · Profil.
        // Pembatasan hanya berlaku untuk FAB mengambang, bukan tombol + di nav tengah.
        if (navCompose != null) navCompose.root.setVisibility(showBottom ? View.VISIBLE : View.GONE);
        boolean showFloatingCompose = homePage && isLoggedIn && !imeVisible && !fullscreen && !composerOpen && !authPage && !postDetailPage && !shortVideoPage && !specialWebChrome && !sheetActive && !uiScrolling;
        if (composeFab != null) {
            composeFab.setVisibility(showFloatingCompose ? View.VISIBLE : View.GONE);
            composeFab.setContentDescription("Buat postingan");
        }

        // Web bottom sheets berada di dalam WebView sehingga tidak otomatis meredupkan
        // toolbar Android. Tambahkan lapisan gelap tipis agar konsisten seperti Threads.
        if (topContainer != null) {
            topContainer.setForeground(webSheetOpen ? new ColorDrawable(Color.parseColor("#24000000")) : null);
        }

        if (root != null) {
            WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), root);
            boolean storyPage = "story".equalsIgnoreCase(pageChromeType);
            boolean immersiveDark = shortVideoPage || storyPage;
            controller.setAppearanceLightStatusBars(!dark && !immersiveDark);
            controller.setAppearanceLightNavigationBars(!dark && !immersiveDark);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(immersiveDark ? Color.BLACK : Color.TRANSPARENT);
        }
        updateRefreshAvailability();
    }

    private void setUiScrolling(boolean scrolling) {
        if (uiScrolling == scrolling) return;
        uiScrolling = scrolling;
        updateChromeVisibility();
    }

    private void applyRuntimeTheme(boolean isDark) {
        if (dark == isDark) {
            if (root != null) applySystemBarAppearance(root);
            return;
        }
        setPalette(isDark);
        if (root != null) root.setBackgroundColor(cBg);
        if (shell != null) shell.setBackgroundColor(cBg);
        if (webView != null) webView.setBackgroundColor(cBg);
        if (topContainer != null) {
            topContainer.setBackgroundColor(cSurface);
            if (topContainer.getChildCount() > 1) topContainer.getChildAt(topContainer.getChildCount() - 1).setBackgroundColor(cBorder);
        }
        if (bottomContainer != null) {
            bottomContainer.setBackgroundColor(cSurface);
            if (bottomContainer.getChildCount() > 1) bottomContainer.getChildAt(bottomContainer.getChildCount() - 1).setBackgroundColor(cBorder);
        }
        if (toolbarTitle != null) toolbarTitle.setTextColor(cText);
        if (featureMenuButton != null) featureMenuButton.setColorFilter(cText);
        if (composeFab != null) {
            composeFab.setColorFilter(dark ? Color.BLACK : Color.WHITE);
            composeFab.setBackground(rounded(dark ? Color.WHITE : Color.BLACK, 18));
        }
        if (swipeRefresh != null) {
            swipeRefresh.setColorSchemeColors(Color.TRANSPARENT);
            swipeRefresh.setProgressBackgroundColorSchemeColor(Color.TRANSPARENT);
        }
        if (refreshIndicator != null) refreshIndicator.setBackground(bordered(cSurface, cBorder, 99));
        if (refreshIcon != null) refreshIcon.setColorFilter(cText);
        if (navHome != null) navHome.refreshTheme();
        if (navMessage != null) navMessage.refreshTheme();
        if (navCompose != null) navCompose.refreshTheme();
        if (navNotif != null) navNotif.refreshTheme();
        if (navProfile != null) navProfile.refreshTheme();
        updateTopBarMode();
        updateChromeVisibility();
    }

    private void updateRefreshAvailability() {
        if (swipeRefresh == null) return;
        String low = currentUrl == null ? "" : currentUrl.toLowerCase(Locale.US);
        boolean messageThread = low.contains("/messages.php") && (low.contains("?c=") || low.contains("&c="));
        boolean formHeavy = low.contains("/settings.php") || low.contains("/security") || low.contains("/profile-edit.php") ||
                low.contains("/ai.php") || low.contains("/login.php") || low.contains("/register.php") || low.contains("/forgot-password.php");
        boolean immersive = reelsPage || isReelsUrl(currentUrl) || "story".equalsIgnoreCase(pageChromeType);
        boolean enabled = isLoggedIn && !messageThread && !formHeavy && !immersive && !postDetailPage &&
                activeSheetOverlay == null && !webSheetOpen && !composerOpen && customView == null;
        if (!enabled && swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(false);
            showRefreshIndicator(false);
        }
        swipeRefresh.setEnabled(enabled);
    }

    private void updateTopBarMode() {
        if (toolbarTitle == null || featureMenuButton == null) return;
        toolbarLogo.setVisibility(View.VISIBLE);
        featureMenuButton.setVisibility(View.VISIBLE);
        if (composerOpen) {
            toolbarLogo.setImageResource(R.drawable.ic_native_back);
            toolbarLogo.setColorFilter(cText);
            toolbarLogo.setContentDescription("Batal");
            toolbarTitle.setText("Buat postingan");
            featureMenuButton.setImageResource(R.drawable.ic_native_send);
            featureMenuButton.setContentDescription("Terbitkan postingan");
            featureMenuButton.setColorFilter(cText);
            return;
        }
        if (postDetailPage || isPostUrl(currentUrl)) {
            toolbarLogo.setImageResource(R.drawable.ic_native_back);
            toolbarLogo.setColorFilter(cText);
            toolbarLogo.setContentDescription("Kembali");
            String title = profileDisplayName == null ? "" : profileDisplayName.trim();
            toolbarTitle.setText(title.isEmpty() ? "Postingan" : title);
            featureMenuButton.setVisibility(View.INVISIBLE);
            return;
        }
        toolbarLogo.setImageResource(R.drawable.ic_native_search);
        toolbarLogo.setColorFilter(cText);
        toolbarLogo.setPadding(dp(8), dp(8), dp(8), dp(8));
        toolbarLogo.setContentDescription("Cari di Deapp");
        if (profilePage || isProfileUrl(currentUrl)) {
            String title = profileDisplayName == null ? "" : profileDisplayName.trim();
            toolbarTitle.setText(title.isEmpty() ? "Profil" : title);
            featureMenuButton.setImageResource(R.drawable.ic_native_more_vertical);
            featureMenuButton.setContentDescription("Opsi profil");
            featureMenuButton.setColorFilter(cText);
            return;
        }
        if (currentUrl != null && currentUrl.toLowerCase(Locale.US).contains("/explore.php")) toolbarTitle.setText("Cari");
        else toolbarTitle.setText("Deapp");
        featureMenuButton.setImageResource(R.drawable.ic_native_grid);
        featureMenuButton.setContentDescription("Menu fitur Deapp");
        featureMenuButton.setColorFilter(cText);
    }

    private void configureWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setDefaultTextEncodingName("UTF-8");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) s.setOffscreenPreRaster(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(true);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setLoadsImagesAutomatically(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        s.setUserAgentString(s.getUserAgentString() + " DeappLite/1.9.18 NativeMobile/9.18");
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
                final int generation = ++loadingGeneration;
                showOffline(false);
                if (loadingOverlay != null) {
                    boolean pullRefreshing = swipeRefresh != null && swipeRefresh.isRefreshing();
                    boolean alreadyShowing = loadingOverlay.getVisibility() == View.VISIBLE;
                    if (!pullRefreshing && !alreadyShowing) {
                        loadingOverlay.postDelayed(() -> {
                            if (generation == loadingGeneration && loadingOverlay != null && webView != null && webView.getProgress() < 80 &&
                                    (swipeRefresh == null || !swipeRefresh.isRefreshing())) {
                                showLoading();
                            }
                        }, 120);
                    }
                }
                currentUrl = url == null ? "" : url;
                profilePage = isProfileUrl(currentUrl);
                postDetailPage = isPostUrl(currentUrl);
                reelsPage = isReelsUrl(currentUrl);
                pageChromeType = isSpecialSectionUrl(currentUrl) ? "section" : "";
                profileDisplayName = "";
                composerOpen = false;
                webSheetOpen = false;
                updateNativeNav(url);
                updateChromeVisibility();
                updateKeepScreenOn(url);
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                super.onPageCommitVisible(view, url);
                loadingGeneration++;
                injectNativeShell();
                view.postDelayed(MainActivity.this::recoverWebScroll, 80);
                if (loadingOverlay != null) hideLoading();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                loadingGeneration++;
                injectNativeShell();
                view.postDelayed(MainActivity.this::recoverWebScroll, 120);
                swipeRefresh.setRefreshing(false);
                showRefreshIndicator(false);
                hideLoading();
                currentUrl = url == null ? "" : url;
                profilePage = isProfileUrl(currentUrl);
                postDetailPage = isPostUrl(currentUrl);
                reelsPage = isReelsUrl(currentUrl);
                if (!isSpecialWebChromeType(pageChromeType)) pageChromeType = isSpecialSectionUrl(currentUrl) ? "section" : "";
                updateNativeNav(url);
                updateChromeVisibility();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    loadingGeneration++;
                    swipeRefresh.setRefreshing(false);
                    showRefreshIndicator(false);
                    hideLoading();
                    showOffline(true);
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
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

    private String readAssetText(String name) {
        StringBuilder out = new StringBuilder();
        try (InputStream in = getAssets().open(name);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) out.append(line).append('\n');
        } catch (Exception ignored) {
            return "";
        }
        return out.toString();
    }

    private void injectNativeShell() {
        if (webView == null) return;
        if (nativeScript == null || nativeScript.isEmpty()) {
            nativeScript = readAssetText("deapp_native_v19.js");
        }
        if (!nativeScript.isEmpty()) {
            webView.evaluateJavascript(nativeScript, value -> {
                if (activeSheetOverlay != null) syncExternalSheetLock(true);
                else recoverWebScroll();
            });
        }
    }

    private void recoverWebScroll() {
        if (webView == null) return;
        String js = "(function(){try{var a=window.__DEAPP_NATIVE_V19__;" +
                "if(a&&a.recoverInteraction){return a.recoverInteraction();}" +
                "if(a&&a.recoverScroll){return a.recoverScroll();}" +
                "document.documentElement.classList.remove('deapp-native-sheet-lock');" +
                "if(document.body){document.body.classList.remove('deapp-native-sheet-lock-body');}" +
                "document.querySelectorAll('.deapp-post-sheet-backdrop,.deapp-native-profile-options-backdrop').forEach(function(n){n.remove();});" +
                "return true;}catch(e){return false;}})();";
        webView.evaluateJavascript(js, null);
    }

    private void syncExternalSheetLock(boolean open) {
        if (webView == null) return;
        String js = "(function(){try{var a=window.__DEAPP_NATIVE_V19__;if(a&&a.setExternalSheetOpen){a.setExternalSheetOpen(" +
                (open ? "true" : "false") + " );}}catch(e){}})();";
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
        overlay.setBackgroundColor(Color.parseColor("#42000000"));
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
        syncExternalSheetLock(true);
        updateChromeVisibility();
        updateRefreshAvailability();

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

    private void removeViewFromParent(View view) {
        if (view == null) return;
        android.view.ViewParent parent = view.getParent();
        if (parent instanceof ViewGroup) ((ViewGroup) parent).removeView(view);
    }

    private void dismissBottomSheet(boolean animate) {
        if (activeSheetOverlay == null || root == null) return;
        View overlay = activeSheetOverlay;
        View panel = activeSheetPanel;
        activeSheetOverlay = null;
        activeSheetPanel = null;

        // Disable the closing layer immediately. The old implementation relied only on
        // an animation end callback; if that callback was cancelled, an invisible full-screen
        // overlay could remain above the WebView and swallow every tap.
        overlay.setClickable(false);
        overlay.setFocusable(false);
        overlay.setEnabled(false);
        if (panel != null) {
            panel.setClickable(false);
            panel.setFocusable(false);
            panel.setEnabled(false);
        }

        syncExternalSheetLock(false);
        if (webView != null) webView.postDelayed(this::recoverWebScroll, 60);
        updateChromeVisibility();
        updateRefreshAvailability();
        if (animate && panel != null) {
            panel.animate().translationY(Math.max(panel.getHeight(), dp(420))).setDuration(180)
                    .withEndAction(() -> removeViewFromParent(overlay)).start();
            overlay.animate().alpha(0f).setDuration(180).start();
            // Fallback removal survives animation cancellation / lifecycle interruption.
            overlay.postDelayed(() -> removeViewFromParent(overlay), 280);
        } else {
            removeViewFromParent(overlay);
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

    private View featureTile(int iconRes, String label, int iconColor, int iconBg, Runnable action) {
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(dp(7), dp(12), dp(7), dp(10));
        tile.setBackground(bordered(cSurface, dark ? Color.parseColor("#292929") : Color.parseColor("#E7E7E7"), 18));
        tile.setElevation(dp(1));
        tile.setClickable(true);
        tile.setFocusable(true);
        tile.setContentDescription(label);

        FrameLayout iconBubble = new FrameLayout(this);
        iconBubble.setBackground(rounded(iconBg, 16));
        LinearLayout.LayoutParams bubbleLp = new LinearLayout.LayoutParams(dp(46), dp(46));
        tile.addView(iconBubble, bubbleLp);

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setColorFilter(iconColor);
        icon.setPadding(dp(11), dp(11), dp(11), dp(11));
        FrameLayout.LayoutParams iconLp = new FrameLayout.LayoutParams(dp(46), dp(46), Gravity.CENTER);
        iconBubble.addView(icon, iconLp);

        TextView name = text(label, 11.8f, cText);
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

    private void addFeatureTile(GridLayout grid, int iconRes, String label, int iconColor, int iconBg, Runnable action) {
        int width = (getResources().getDisplayMetrics().widthPixels - dp(52)) / 3;
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = width;
        lp.height = dp(90);
        lp.setMargins(dp(3), dp(4), dp(3), dp(4));
        grid.addView(featureTile(iconRes, label, iconColor, iconBg, action), lp);
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

        // v1.9.10 — ikon menu diberi identitas warna agar lebih cepat dikenali secara visual.
        addFeatureTile(grid, R.drawable.ic_native_user, "Profil", Color.parseColor("#6C63FF"), Color.parseColor(dark ? "#211F46" : "#EEEDFF"), this::openOwnProfile);
        addFeatureTile(grid, R.drawable.ic_native_message, "Pesan", Color.parseColor("#00A86B"), Color.parseColor(dark ? "#12382A" : "#E8FAF2"), () -> loadRelative("messages.php"));
        addFeatureTile(grid, R.drawable.ic_native_live, "Live", Color.parseColor("#F04464"), Color.parseColor(dark ? "#431923" : "#FFEAF0"), () -> loadRelative("live.php"));
        addFeatureTile(grid, R.drawable.ic_native_video, "Video", Color.parseColor("#FF7A00"), Color.parseColor(dark ? "#432A12" : "#FFF2E5"), () -> loadRelative("reels.php"));
        addFeatureTile(grid, R.drawable.ic_native_community, "Komunitas", Color.parseColor("#008FD5"), Color.parseColor(dark ? "#123348" : "#E7F6FD"), () -> loadRelative("community.php"));
        addFeatureTile(grid, R.drawable.ic_native_bookmark, "Tersimpan", Color.parseColor("#B565E7"), Color.parseColor(dark ? "#352044" : "#F6EBFF"), () -> loadRelative("bookmarks.php"));
        addFeatureTile(grid, R.drawable.ic_native_tap, "Deapp Tap", Color.parseColor("#E94D8A"), Color.parseColor(dark ? "#431B30" : "#FFEAF3"), () -> loadRelative("tap.php"));
        addFeatureTile(grid, R.drawable.ic_native_sparkles, "Deapp AI", Color.parseColor("#4F67FF"), Color.parseColor(dark ? "#1D274E" : "#EAF0FF"), () -> loadRelative("ai.php"));
        addFeatureTile(grid, R.drawable.ic_native_code, "Developer", Color.parseColor("#00A7A0"), Color.parseColor(dark ? "#123B39" : "#E5FAF8"), () -> loadRelative("developer.php"));
        addFeatureTile(grid, R.drawable.ic_native_shop, "Toko", Color.parseColor("#F59E0B"), Color.parseColor(dark ? "#463413" : "#FFF6DC"), () -> loadRelative("shop.php"));
        addFeatureTile(grid, R.drawable.ic_native_game, "Mini Game", Color.parseColor("#21A366"), Color.parseColor(dark ? "#17392B" : "#E9F9F0"), () -> loadRelative("games.php"));
        addFeatureTile(grid, R.drawable.ic_native_settings, "Pengaturan", Color.parseColor("#708090"), Color.parseColor(dark ? "#2A2F34" : "#EEF1F4"), () -> loadRelative("settings.php"));


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
        public void tap() {
            runOnUiThread(() -> haptic(webView != null ? webView : root));
        }

        @JavascriptInterface
        public void reactionFeedback() {
            runOnUiThread(MainActivity.this::playReactionFeedback);
        }

        @JavascriptInterface
        public void syncSessionState(boolean loggedIn, String avatarUrl, String href, String pageUrl) {
            runOnUiThread(() -> {
                isLoggedIn = loggedIn;
                profileUrl = href == null ? "" : href.trim();
                if (pageUrl != null && !pageUrl.trim().isEmpty()) currentUrl = pageUrl.trim();
                profilePage = isProfileUrl(currentUrl);
                syncNativeProfile(avatarUrl, profileUrl);
                updateNativeNav(currentUrl);
                updateChromeVisibility();
            });
        }

        @JavascriptInterface
        public void syncPageChrome(String pageType, String title, boolean ownProfile) {
            runOnUiThread(() -> {
                String type = pageType == null ? "" : pageType.trim().toLowerCase(Locale.US);
                profilePage = "profile".equals(type) || isProfileUrl(currentUrl);
                postDetailPage = "post".equals(type) || isPostUrl(currentUrl);
                reelsPage = "reels".equals(type) || isReelsUrl(currentUrl);
                pageChromeType = type;
                ownProfilePage = profilePage && ownProfile;
                profileDisplayName = title == null ? "" : title.trim();
                updateChromeVisibility();
            });
        }

        @JavascriptInterface
        public void syncComposerState(boolean open) {
            runOnUiThread(() -> {
                composerOpen = open;
                updateChromeVisibility();
            });
        }

        @JavascriptInterface
        public void syncWebSheetState(boolean open) {
            runOnUiThread(() -> {
                webSheetOpen = open;
                updateChromeVisibility();
                updateRefreshAvailability();
            });
        }

        @JavascriptInterface
        public void syncTheme(boolean darkMode) {
            runOnUiThread(() -> applyRuntimeTheme(darkMode));
        }

        @JavascriptInterface
        public void syncScrollState(boolean scrolling) {
            runOnUiThread(() -> setUiScrolling(scrolling));
        }

        @JavascriptInterface
        public void postPublished() {
            runOnUiThread(MainActivity.this::playPostPublishedSound);
        }

        @JavascriptInterface
        public void showPermissions() {
            runOnUiThread(MainActivity.this::showPermissionsSheet);
        }

        @JavascriptInterface
        public void showServerSettings() {
            runOnUiThread(MainActivity.this::showServerBottomSheet);
        }

        @JavascriptInterface
        public void showAboutApp() {
            runOnUiThread(MainActivity.this::showAboutSheet);
        }
    }

    private void syncNativeProfile(String avatarUrl, String href) {
        profileUrl = href == null ? "" : href.trim();
        if (navProfile == null) return;
        if (avatarUrl == null || avatarUrl.trim().isEmpty() || !(avatarUrl.startsWith("http://") || avatarUrl.startsWith("https://"))) {
            navProfile.clearAvatar(R.drawable.ic_native_user);
            navProfile.setActive(currentUrl != null && currentUrl.toLowerCase(Locale.US).contains("/profile.php"));
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
                conn.setRequestProperty("User-Agent", "DeappLite/1.9.18");
                try (InputStream in = conn.getInputStream()) {
                    Bitmap bitmap = BitmapFactory.decodeStream(in);
                    if (bitmap != null) runOnUiThread(() -> {
                        if (navProfile == null) return;
                        navProfile.setAvatarBitmap(bitmap);
                        updateNativeNav(currentUrl);
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
        webView.evaluateJavascript("(function(){if(window.__DEAPP_NATIVE_V19__&&window.__DEAPP_NATIVE_V19__.openComposer){return window.__DEAPP_NATIVE_V19__.openComposer();}var b=document.querySelector('[data-open-modal=\"composer-modal\"]');if(b){b.click();return true;}return false;})()", value -> {
            if (!"true".equals(value)) {
                Toast.makeText(this, "Login dulu untuk membuat postingan", Toast.LENGTH_SHORT).show();
                loadRelative("login.php");
            } else {
                // Sinkronkan toolbar segera: ikon pesawat langsung aktif tanpa menunggu scan DOM berikutnya.
                composerOpen = true;
                updateChromeVisibility();
            }
        });
    }

    private void closeComposer() {
        if (webView == null) return;
        webView.evaluateJavascript("(function(){if(window.__DEAPP_NATIVE_V19__&&window.__DEAPP_NATIVE_V19__.closeComposer){window.__DEAPP_NATIVE_V19__.closeComposer();return true;}return false;})()", null);
    }

    private void publishComposer() {
        if (webView == null) return;
        webView.evaluateJavascript("(function(){try{if(window.__DEAPP_NATIVE_V19__&&window.__DEAPP_NATIVE_V19__.submitComposer){return !!window.__DEAPP_NATIVE_V19__.submitComposer();}var m=document.getElementById('composer-modal');var f=document.getElementById('composer-form')||(m?m.querySelector('form'):null);if(!f)return false;var b=document.getElementById('composer-submit')||f.querySelector('.composer-submit,[data-action=publish],[data-action=submit-post],button[type=submit],input[type=submit]');if(b&&b.disabled)return false;if(typeof f.requestSubmit==='function'){b&&b.form===f?f.requestSubmit(b):f.requestSubmit();return true;}if(b){b.click();return true;}return false;}catch(e){return false;}})()", value -> {
            if (!"true".equals(value)) Toast.makeText(this, "Postingan belum siap diterbitkan", Toast.LENGTH_SHORT).show();
        });
    }

    private void openProfileOptions() {
        if (webView == null) return;
        webView.evaluateJavascript("(function(){if(window.__DEAPP_NATIVE_V19__&&window.__DEAPP_NATIVE_V19__.openProfileOptions){return window.__DEAPP_NATIVE_V19__.openProfileOptions();}return false;})()", value -> {
            if (!"true".equals(value)) Toast.makeText(this, "Opsi profil tidak tersedia", Toast.LENGTH_SHORT).show();
        });
    }

    private void playReactionFeedback() {
        long now = android.os.SystemClock.elapsedRealtime();
        if (now - lastReactionFeedbackAt < 90) return;
        lastReactionFeedbackAt = now;
        haptic(webView != null ? webView : root);
        try {
            ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 44);
            tone.startTone(ToneGenerator.TONE_PROP_ACK, 65);
            if (root != null) root.postDelayed(tone::release, 130); else tone.release();
        } catch (Exception ignored) {}
    }

    private void playPostPublishedSound() {
        long now = android.os.SystemClock.elapsedRealtime();
        if (now - lastPostPublishedSoundAt < 1200) return;
        lastPostPublishedSoundAt = now;
        try {
            ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 68);
            tone.startTone(ToneGenerator.TONE_PROP_ACK, 170);
            if (root != null) root.postDelayed(tone::release, 260); else tone.release();
        } catch (Exception ignored) {}
    }

    private boolean granted(String permission) {
        return Build.VERSION.SDK_INT < 23 || checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean permissionGranted(String key) {
        if ("location".equals(key)) return granted(Manifest.permission.ACCESS_FINE_LOCATION) || granted(Manifest.permission.ACCESS_COARSE_LOCATION);
        if ("contacts".equals(key)) return granted(Manifest.permission.READ_CONTACTS);
        if ("notifications".equals(key)) return Build.VERSION.SDK_INT < 33 || granted(Manifest.permission.POST_NOTIFICATIONS);
        if ("photos".equals(key)) {
            if (Build.VERSION.SDK_INT >= 34) return granted(Manifest.permission.READ_MEDIA_IMAGES) || granted(Manifest.permission.READ_MEDIA_VIDEO) || granted(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED);
            if (Build.VERSION.SDK_INT >= 33) return granted(Manifest.permission.READ_MEDIA_IMAGES) || granted(Manifest.permission.READ_MEDIA_VIDEO);
            return granted(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if ("microphone".equals(key)) return granted(Manifest.permission.RECORD_AUDIO);
        if ("phone".equals(key)) return granted(Manifest.permission.CALL_PHONE);
        if ("camera".equals(key)) return granted(Manifest.permission.CAMERA);
        return false;
    }

    private String[] permissionsFor(String key) {
        if ("location".equals(key)) return new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if ("contacts".equals(key)) return new String[]{Manifest.permission.READ_CONTACTS};
        if ("notifications".equals(key)) return Build.VERSION.SDK_INT >= 33 ? new String[]{Manifest.permission.POST_NOTIFICATIONS} : new String[0];
        if ("photos".equals(key)) {
            if (Build.VERSION.SDK_INT >= 34) return new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED};
            if (Build.VERSION.SDK_INT >= 33) return new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO};
            return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }
        if ("microphone".equals(key)) return new String[]{Manifest.permission.RECORD_AUDIO};
        if ("phone".equals(key)) return new String[]{Manifest.permission.CALL_PHONE};
        if ("camera".equals(key)) return new String[]{Manifest.permission.CAMERA};
        return new String[0];
    }

    private void requestPermissionFromCenter(String key, String label) {
        if (permissionGranted(key)) {
            openSystemAppSettings();
            return;
        }
        String[] perms = permissionsFor(key);
        if (perms.length == 0) {
            Toast.makeText(this, label + " tidak memerlukan izin tambahan di Android ini", Toast.LENGTH_SHORT).show();
            return;
        }
        pendingPermissionLabel = label;
        requestPermissions(perms, REQ_APP_PERMISSION);
    }

    private View permissionRow(int iconRes, String title, String subtitle, String key) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(11), dp(12), dp(11));
        row.setBackground(bordered(cSurface2, cBorder, 16));
        row.setClickable(true);
        row.setFocusable(true);

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setColorFilter(cText);
        icon.setPadding(dp(9), dp(9), dp(9), dp(9));
        icon.setBackground(rounded(cSurface, 99));
        row.addView(icon, new LinearLayout.LayoutParams(dp(42), dp(42)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams copyLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        copyLp.leftMargin = dp(11);
        row.addView(copy, copyLp);

        TextView name = text(title, 14.5f, cText);
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        copy.addView(name);
        TextView sub = text(subtitle, 11.5f, cMuted);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subLp.topMargin = dp(2);
        copy.addView(sub, subLp);

        boolean ok = permissionGranted(key);
        TextView status = text(ok ? "Diizinkan" : "Izinkan", 11.5f, ok ? cSuccess : cAccent);
        status.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        status.setGravity(Gravity.CENTER);
        status.setPadding(dp(10), dp(7), dp(10), dp(7));
        status.setBackground(rounded(ok ? (dark ? Color.parseColor("#11251D") : Color.parseColor("#EAF8F1")) : cAccentSoft, 99));
        row.addView(status);

        row.setOnClickListener(v -> requestPermissionFromCenter(key, title));
        return row;
    }

    private void showPermissionsSheet() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);

        TextView info = text("Izin hanya diminta saat kamu memilihnya atau saat fitur terkait benar-benar digunakan. Izin yang sudah diberikan dapat dicabut dari Pengaturan Android.", 12.3f, cMuted);
        info.setLineSpacing(0, 1.14f);
        LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        infoLp.bottomMargin = dp(12);
        box.addView(info, infoLp);

        Object[][] rows = new Object[][]{
                {R.drawable.ic_native_location, "Lokasi", "Untuk fitur lokasi dan geolokasi", "location"},
                {R.drawable.ic_native_contacts, "Kontak", "Opsional untuk fitur yang memakai daftar kontak", "contacts"},
                {R.drawable.ic_native_bell, "Notifikasi", "Untuk pemberitahuan Deapp", "notifications"},
                {R.drawable.ic_native_photo, "Foto & media", "Untuk memilih foto dan video dari perangkat", "photos"},
                {R.drawable.ic_native_mic, "Mikrofon", "Untuk Live, voice dan media", "microphone"},
                {R.drawable.ic_native_phone, "Telepon", "Opsional untuk fitur panggilan", "phone"},
                {R.drawable.ic_native_camera, "Kamera", "Untuk Live, foto dan video", "camera"}
        };
        for (Object[] item : rows) {
            View row = permissionRow((Integer)item[0], (String)item[1], (String)item[2], (String)item[3]);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = dp(8);
            box.addView(row, lp);
        }

        View settings = sheetAction("Buka pengaturan sistem", "Kelola atau cabut izin aplikasi dari Android", this::openSystemAppSettings);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        slp.topMargin = dp(5);
        box.addView(settings, slp);
        showBottomSheet("Perizinan aplikasi", box);
    }

    private void openSystemAppSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception ignored) {}
    }

    private void showAboutSheet() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER_HORIZONTAL);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.deapp_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        LinearLayout.LayoutParams logoLp = new LinearLayout.LayoutParams(dp(74), dp(74));
        logoLp.bottomMargin = dp(12);
        box.addView(logo, logoLp);

        TextView name = text("Deapp Lite", 20, cText);
        name.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        name.setGravity(Gravity.CENTER);
        box.addView(name);

        TextView version = text("Versi 1.9.18-lite · Build 28", 13, cMuted);
        version.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams versionLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        versionLp.topMargin = dp(5);
        box.addView(version, versionLp);

        TextView info = text("Deapp Lite adalah aplikasi Android ringan dengan antarmuka native yang terhubung ke server Deapp kamu.", 12.5f, cMuted);
        info.setGravity(Gravity.CENTER);
        info.setLineSpacing(0, 1.15f);
        LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        infoLp.topMargin = dp(14);
        box.addView(info, infoLp);

        showBottomSheet("Tentang aplikasi", box);
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
        boolean home = isHomeUrl(url);
        navHome.setActive(home);
        if (navMessage != null) navMessage.setActive(low.contains("/messages.php") || low.contains("/inbox.php"));
        if (navCompose != null) navCompose.setActive(false);
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
        if (customViewCallback != null) customViewCallback.onCustomViewHidden();
        customViewCallback = null;
        updateChromeVisibility();
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
        } else if (requestCode == REQ_APP_PERMISSION) {
            boolean ok = grantResults.length > 0;
            for (int result : grantResults) if (result != PackageManager.PERMISSION_GRANTED) ok = false;
            String label = pendingPermissionLabel == null || pendingPermissionLabel.isEmpty() ? "Izin" : pendingPermissionLabel;
            pendingPermissionLabel = "";
            Toast.makeText(this, ok ? label + " diizinkan" : label + " belum diizinkan", Toast.LENGTH_SHORT).show();
            if (activeSheetOverlay != null) {
                dismissBottomSheet(false);
                root.postDelayed(this::showPermissionsSheet, 140);
            }
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
        if (composerOpen) {
            closeComposer();
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
        if (webView != null) {
            webView.onResume();
            if (activeSheetOverlay == null) webView.postDelayed(this::recoverWebScroll, 80);
        }
    }

    private void destroyWebView() {
        // Bersihkan semua lapisan native sebelum WebView diganti. Referensi overlay lama
        // yang tertinggal dapat membuat refresh/interaksi pada WebView baru tetap dianggap terkunci.
        if (activeSheetOverlay != null) removeViewFromParent(activeSheetOverlay);
        activeSheetOverlay = null;
        activeSheetPanel = null;
        if (fileCallback != null) {
            try { fileCallback.onReceiveValue(null); } catch (Exception ignored) {}
            fileCallback = null;
        }
        if (pendingPermissionRequest != null) {
            try { pendingPermissionRequest.deny(); } catch (Exception ignored) {}
            pendingPermissionRequest = null;
        }
        pendingGeoCallback = null;
        pendingGeoOrigin = null;
        composeFab = null;
        if (loadingLogo != null) loadingLogo.animate().cancel();
        loadingLogo = null;
        refreshIconAnimating = false;
        if (refreshIcon != null) refreshIcon.animate().cancel();
        refreshIcon = null;
        refreshIndicator = null;
        startupSplashVisible = false;
        if (startupSplashLogo != null) startupSplashLogo.animate().cancel();
        startupSplashLogo = null;
        if (startupSplashOverlay != null) {
            startupSplashOverlay.animate().cancel();
            removeViewFromParent(startupSplashOverlay);
        }
        startupSplashOverlay = null;
        featureMenuButton = null;
        toolbarLogo = null;
        toolbarTitle = null;
        profileDisplayName = "";
        profilePage = false;
        ownProfilePage = false;
        postDetailPage = false;
        reelsPage = false;
        pageChromeType = "";
        composerOpen = false;
        webSheetOpen = false;
        navHome = null;
        navMessage = null;
        navCompose = null;
        navNotif = null;
        navProfile = null;
        isLoggedIn = false;
        imeVisible = false;
        currentUrl = "";
        loadingGeneration++;
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
