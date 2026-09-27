package id.deapp.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * DeApp Android 2.x — native UI shell.
 *
 * Deliberately contains no WebView. PHP/MySQL remains the server, while Android owns
 * navigation, toolbar, lists, forms, chat bubbles, notifications, feed cards and dialogs.
 */
public class MainActivity extends Activity {
    private static final String PREFS = "deapp_native";
    private static final String KEY_SERVER = "server_url";

    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService io = Executors.newFixedThreadPool(4);
    private final ArrayDeque<Screen> history = new ArrayDeque<>();

    private SharedPreferences prefs;
    private DeappClient client;
    private String baseUrl = "";
    private String ownProfilePath = "";
    private Screen current = new Screen("boot", "", "");

    private FrameLayout root;
    private LinearLayout appShell;
    private LinearLayout topBar;
    private FrameLayout content;
    private LinearLayout bottomBar;
    private TextView topTitle;
    private ImageButton topBack;
    private ImageButton topAction;
    private ToneGenerator tone;
    private static final int REQ_PICK_PROFILE_IMAGE = 2101;
    private String pendingImageUpload = "";
    private byte[] pendingStoryImage = null;
    private String pendingStoryMime = "image/jpeg";

    private int bg, surface, surface2, text, muted, border, accent, accentSoft, danger, success;
    private boolean dark;

    static final class Screen {
        final String key;
        final String arg;
        final String title;
        Screen(String key, String arg, String title) {
            this.key = key == null ? "" : key;
            this.arg = arg == null ? "" : arg;
            this.title = title == null ? "" : title;
        }
    }

    interface NetWork<T> { T run() throws Exception; }
    interface NetDone<T> { void done(T value); }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        readPalette();
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        baseUrl = normalizeServerInput(prefs.getString(KEY_SERVER, ""));
        tone = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 30);
        if (Build.VERSION.SDK_INT >= 33) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT, this::handleBack);
        }
        if (baseUrl.isEmpty()) showServerSetup(false);
        else {
            client = new DeappClient(baseUrl, prefs);
            buildShell();
            validateSession();
        }
    }


    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (client != null && current != null && !"login".equals(current.key) && !"register".equals(current.key)) {
            handleShortcut(intent);
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQ_PICK_PROFILE_IMAGE || resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        String target = pendingImageUpload;
        pendingImageUpload = "";
        if ("story".equals(target)) {
            runNet(() -> {
                String mime = getContentResolver().getType(uri); if (mime == null || mime.isEmpty()) mime = "image/jpeg";
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try (InputStream in = getContentResolver().openInputStream(uri)) { if (in == null) throw new IllegalStateException("File tidak dapat dibaca."); byte[] buf = new byte[8192]; int n; while ((n = in.read(buf)) > 0) out.write(buf, 0, n); }
                return new Object[]{mime, out.toByteArray()};
            }, obj -> { pendingStoryMime = (String)obj[0]; pendingStoryImage = (byte[])obj[1]; showNativeNotice("Foto siap", "Foto akan ikut diterbitkan bersama Cerita.", success); }, e -> showNativeNotice("Foto gagal dibaca", e.getMessage(), danger));
            return;
        }
        runNet(() -> {
            String mime = getContentResolver().getType(uri);
            if (mime == null || mime.isEmpty()) mime = "image/jpeg";
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (InputStream in = getContentResolver().openInputStream(uri)) {
                if (in == null) throw new IllegalStateException("File tidak dapat dibaca.");
                byte[] buf = new byte[8192]; int n; while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            }
            LinkedHashMap<String,String> fields = new LinkedHashMap<>();
            fields.put("csrf", client.ensureCsrf());
            String endpoint = "cover".equals(target) ? "/api/upload_cover.php" : "/api/upload_avatar.php";
            String field = "cover".equals(target) ? "cover" : "avatar";
            return client.postMultipart(endpoint, fields, field, field + ".jpg", mime, out.toByteArray());
        }, r -> {
            if (r.ok()) {
                showNativeNotice("Berhasil", "Foto profil diperbarui.", success);
                navigate(new Screen("profile-media", "", "Foto Profil & Sampul"), false);
            } else showNativeNotice("Belum berhasil", jsonError(r.body, "Upload gambar gagal."), danger);
        }, e -> showNativeNotice("Upload gagal", e.getMessage(), danger));
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        io.shutdownNow();
        if (tone != null) tone.release();
    }

    @Override public void onBackPressed() { handleBack(); }

    private void readPalette() {
        dark = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        bg = Color.parseColor(dark ? "#000000" : "#FFFFFF");
        surface = bg;
        surface2 = Color.parseColor(dark ? "#171717" : "#F7F7F7");
        text = Color.parseColor(dark ? "#F5F5F5" : "#101010");
        muted = Color.parseColor(dark ? "#999999" : "#737373");
        border = Color.parseColor(dark ? "#2A2A2A" : "#E8E8E8");
        accent = Color.parseColor(dark ? "#4CB5F9" : "#0095F6");
        accentSoft = Color.parseColor(dark ? "#0F2736" : "#EAF6FF");
        danger = Color.parseColor("#E5484D");
        success = Color.parseColor("#22A06B");
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        if (Build.VERSION.SDK_INT >= 23) {
            int f = getWindow().getDecorView().getSystemUiVisibility();
            if (!dark) f |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR; else f &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            getWindow().getDecorView().setSystemUiVisibility(f);
        }
    }

    private String normalizeBase(String raw) {
        String s = raw == null ? "" : raw.trim();
        while (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        return s;
    }

    private String normalizeServerInput(String raw) {
        String s = normalizeBase(raw);
        if (s.isEmpty()) return "";
        if (!s.startsWith("http://") && !s.startsWith("https://")) {
            boolean local = s.matches("^(localhost|127\\.0\\.0\\.1|10\\..*|192\\.168\\..*|172\\.(1[6-9]|2[0-9]|3[0-1])\\..*|[0-9a-fA-F:]+)([:/].*)?$");
            s = (local ? "http://" : "https://") + s;
        }
        return normalizeBase(s);
    }

    private String connectionHint() {
        return baseUrl.isEmpty() ? "Server belum diatur" : baseUrl;
    }

    private String probeServer(String raw) throws Exception {
        String normalized = normalizeServerInput(raw);
        if (normalized.isEmpty()) throw new IllegalArgumentException("Alamat server DeApp belum diisi.");
        ArrayList<String> candidates = new ArrayList<>();
        candidates.add(normalized);
        try {
            URI u = new URI(normalized);
            String path = u.getPath();
            if ((path == null || path.isEmpty() || "/".equals(path)) && !normalized.endsWith("/deapp")) {
                candidates.add(normalized + "/deapp");
            }
        } catch (Exception ignored) {}
        Exception last = null;
        for (String candidate : candidates) {
            try {
                SharedPreferences probePrefs = getSharedPreferences("deapp_probe", MODE_PRIVATE); probePrefs.edit().clear().apply();
                DeappClient test = new DeappClient(candidate, probePrefs);
                DeappClient.Result r = test.get("/login.php");
                if (r.code >= 200 && r.code < 400 && r.body != null && !r.body.trim().isEmpty()) {
                    Document d = Jsoup.parse(r.body, r.url);
                    String title = d.title() == null ? "" : d.title().toLowerCase(Locale.ROOT);
                    boolean deappPage = d.selectFirst("form.auth-form, input[name=identifier], a[href*=login.php], .topbar, .drawer-user") != null
                            || title.contains("deapp") || d.text().toLowerCase(Locale.ROOT).contains("deapp");
                    if (deappPage) return candidate;
                }
                last = new IllegalStateException("Server merespons HTTP " + r.code + ".");
            } catch (Exception e) {
                last = e;
                if (candidate.startsWith("https://") && !raw.trim().startsWith("http://") && !raw.trim().startsWith("https://")) {
                    String httpCandidate = "http://" + candidate.substring("https://".length());
                    try {
                        SharedPreferences probePrefs = getSharedPreferences("deapp_probe", MODE_PRIVATE); probePrefs.edit().clear().apply();
                        DeappClient test = new DeappClient(httpCandidate, probePrefs);
                        DeappClient.Result r = test.get("/login.php");
                        if (r.code >= 200 && r.code < 400 && r.body != null && !r.body.trim().isEmpty()) return httpCandidate;
                    } catch (Exception ignored) {}
                }
            }
        }
        throw new IllegalStateException(last == null ? "Server DeApp tidak dapat dijangkau." : last.getMessage());
    }

    private int dp(int n) { return Math.round(n * getResources().getDisplayMetrics().density); }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color); g.setCornerRadius(dp(radius));
        return g;
    }

    private GradientDrawable outlined(int color, int stroke, int radius) {
        GradientDrawable g = rounded(color, radius);
        g.setStroke(dp(1), stroke);
        return g;
    }

    private TextView tv(String s, float sp, int color) {
        TextView v = new TextView(this);
        v.setText(s); v.setTextSize(sp); v.setTextColor(color);
        v.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        v.setIncludeFontPadding(false);
        v.setLineSpacing(0, 1.12f);
        return v;
    }

    private TextView bold(String s, float sp) {
        TextView v = tv(s, sp, text);
        v.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        return v;
    }

    private ImageButton iconButton(int res, String desc) {
        ImageButton b = new ImageButton(this);
        b.setImageResource(res); b.setColorFilter(text); b.setContentDescription(desc);
        b.setBackground(rounded(Color.TRANSPARENT, 99));
        b.setPadding(dp(10), dp(10), dp(10), dp(10));
        b.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        b.setLayoutParams(new LinearLayout.LayoutParams(dp(44), dp(44)));
        return b;
    }

    private Button primary(String label) {
        Button b = new Button(this);
        b.setText(label); b.setTextColor(Color.WHITE); b.setTextSize(15); b.setAllCaps(false);
        b.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        b.setBackground(rounded(accent, 14));
        b.setMinHeight(dp(50));
        return b;
    }

    private Button secondaryButton(String label) {
        Button b = new Button(this);
        b.setText(label); b.setTextColor(text); b.setTextSize(14.5f); b.setAllCaps(false);
        b.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        b.setBackground(outlined(surface, border, 14));
        b.setMinHeight(dp(48));
        return b;
    }

    private EditText field(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint); e.setHintTextColor(muted); e.setTextColor(text); e.setTextSize(15);
        e.setSingleLine(true); e.setPadding(dp(14), 0, dp(14), 0);
        e.setBackground(outlined(surface, border, 14));
        e.setMinHeight(dp(52));
        return e;
    }

    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL); l.setBackgroundColor(bg);
        return l;
    }

    private void buildShell() {
        root = new FrameLayout(this); root.setBackgroundColor(bg);
        appShell = new LinearLayout(this); appShell.setOrientation(LinearLayout.VERTICAL); appShell.setBackgroundColor(bg);
        root.addView(appShell, new FrameLayout.LayoutParams(-1, -1));

        topBar = new LinearLayout(this); topBar.setGravity(Gravity.CENTER_VERTICAL); topBar.setPadding(dp(8), 0, dp(8), 0);
        topBar.setBackgroundColor(bg);
        topBack = iconButton(R.drawable.ic_native_back, "Kembali"); topBack.setOnClickListener(v -> handleBack());
        topTitle = bold("DeApp", 17); topTitle.setGravity(Gravity.CENTER);
        topAction = iconButton(R.drawable.ic_native_search, "Aksi");
        topBar.addView(topBack);
        topBar.addView(topTitle, new LinearLayout.LayoutParams(0, dp(56), 1));
        topBar.addView(topAction);
        appShell.addView(topBar, new LinearLayout.LayoutParams(-1, dp(56)));
        View divider = new View(this); divider.setBackgroundColor(border);
        appShell.addView(divider, new LinearLayout.LayoutParams(-1, dp(1)));

        content = new FrameLayout(this); content.setBackgroundColor(bg);
        appShell.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));

        bottomBar = new LinearLayout(this); bottomBar.setGravity(Gravity.CENTER); bottomBar.setBackgroundColor(bg);
        bottomBar.setPadding(dp(4), dp(3), dp(4), dp(3));
        addNav(R.drawable.ic_native_home, "Beranda", () -> goRoot("home", "", "Beranda"));
        addNav(R.drawable.ic_native_message, "Chat", () -> goRoot("messages", "", "Chat"));
        addNav(R.drawable.ic_native_add, "Postingan", () -> navigate(new Screen("compose", "", "Postingan"), true));
        addNav(R.drawable.ic_native_bell, "Notifikasi", () -> goRoot("notifications", "", "Notifikasi"));
        addNav(R.drawable.ic_native_user, "Profil", () -> goRoot("profile", ownProfilePath, "Profil"));
        View bottomDivider = new View(this); bottomDivider.setBackgroundColor(border);
        appShell.addView(bottomDivider, new LinearLayout.LayoutParams(-1, dp(1)));
        appShell.addView(bottomBar, new LinearLayout.LayoutParams(-1, dp(62)));
        setContentView(root);
    }

    private void addNav(int icon, String label, Runnable click) {
        LinearLayout item = new LinearLayout(this); item.setOrientation(LinearLayout.VERTICAL); item.setGravity(Gravity.CENTER);
        item.setBackgroundColor(Color.TRANSPARENT); item.setClickable(true); item.setFocusable(true);
        ImageView i = new ImageView(this); i.setImageResource(icon); i.setColorFilter(text); i.setPadding(dp(6), dp(4), dp(6), 0);
        TextView t = tv(label, 10.5f, muted); t.setGravity(Gravity.CENTER);
        item.addView(i, new LinearLayout.LayoutParams(dp(34), dp(32)));
        item.addView(t, new LinearLayout.LayoutParams(-1, dp(20)));
        item.setOnClickListener(v -> click.run());
        bottomBar.addView(item, new LinearLayout.LayoutParams(0, -1, 1));
    }

    private void configureChrome(String title, boolean back, int actionIcon, String actionDesc, View.OnClickListener action) {
        topTitle.setText(title == null || title.isEmpty() ? "DeApp" : title);
        topBack.setVisibility(back ? View.VISIBLE : View.INVISIBLE);
        if (actionIcon == 0 || action == null) topAction.setVisibility(View.INVISIBLE);
        else {
            topAction.setVisibility(View.VISIBLE); topAction.setImageResource(actionIcon);
            topAction.setContentDescription(actionDesc); topAction.setOnClickListener(action);
        }
    }

    private void showBottom(boolean show) { bottomBar.setVisibility(show ? View.VISIBLE : View.GONE); }

    private void swap(View next) {
        next.setAlpha(0f); next.setTranslationY(dp(6));
        content.removeAllViews(); content.addView(next, new FrameLayout.LayoutParams(-1, -1));
        next.animate().alpha(1f).translationY(0).setDuration(150).start();
    }

    private void validateSession() {
        configureChrome("DeApp", false, 0, "", null); showBottom(false); swap(nativeSkeleton("Menghubungkan ke DeApp", 4));
        runNet(() -> client.get("/index.php"), r -> {
            if (r.code >= 500 || r.body == null || r.body.trim().isEmpty()) {
                showNetworkError("Server merespons tetapi tidak mengirim halaman DeApp.\n" + connectionHint(), this::validateSession);
                return;
            }
            if (looksLoggedOut(r)) showLogin();
            else {
                discoverOwnProfile(r.body, r.url);
                goRoot("home", "", "Beranda");
                handleShortcut(getIntent());
            }
        }, e -> showNetworkError("Tidak dapat terhubung ke server DeApp.\n" + connectionHint() + "\n" + safeMessage(e, ""), this::validateSession));
    }

    private boolean looksLoggedOut(DeappClient.Result r) {
        String u = r.url.toLowerCase(Locale.ROOT);
        if (u.contains("login.php")) return true;
        if (r.code == 401 || r.code == 403) return true;
        Document d = Jsoup.parse(r.body, r.url);
        return d.selectFirst("form.auth-form input[name=identifier]") != null;
    }

    private void discoverOwnProfile(String html, String url) {
        if (html == null || html.isEmpty()) return;
        Document d = Jsoup.parse(html, url);
        Element a = d.selectFirst("a.dropdown-user[href*=profile.php], a.drawer-user[href*=profile.php], a[href*=profile.php][class*=user]");
        if (a != null) ownProfilePath = relative(a.absUrl("href"));
    }

    private void showServerSetup(boolean changing) {
        root = new FrameLayout(this); root.setBackgroundColor(bg);
        ScrollView sc = new ScrollView(this); LinearLayout box = column();
        box.setPadding(dp(24), dp(42), dp(24), dp(28)); sc.addView(box);
        TextView logo = bold("DeApp", 32); box.addView(logo);
        TextView head = bold(changing ? "Server DeApp" : "Hubungkan ke Server DeApp", 23);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-1, -2); hp.topMargin = dp(22); box.addView(head, hp);
        TextView sub = tv(changing
                ? "Ganti alamat backend PHP/MySQL yang digunakan aplikasi native."
                : "Aplikasi native membutuhkan alamat backend DeApp agar Beranda, Chat, Notifikasi, Profil, dan fitur lainnya dapat memuat data.", 14, muted);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2); sp.topMargin = dp(8); box.addView(sub, sp);

        EditText server = field("https://domain.com/deapp atau 192.168.1.10/deapp");
        server.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        server.setSingleLine(true); server.setImeOptions(EditorInfo.IME_ACTION_GO);
        if (!baseUrl.isEmpty()) server.setText(baseUrl);
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(-1, dp(54)); fp.topMargin = dp(24); box.addView(server, fp);

        TextView status = tv("Alamat akan diuji sebelum disimpan.", 12.5f, muted);
        LinearLayout.LayoutParams stp = new LinearLayout.LayoutParams(-1, -2); stp.topMargin = dp(9); box.addView(status, stp);

        Button save = primary("Hubungkan"); LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, dp(52)); bp.topMargin = dp(16); box.addView(save, bp);

        if (changing && client != null) {
            Button cancel = secondaryButton("Batal");
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, dp(50)); cp.topMargin = dp(9); box.addView(cancel, cp);
            cancel.setOnClickListener(v -> {
                buildShell();
                Screen back = current == null || "boot".equals(current.key) ? new Screen("settings", "", "Pengaturan") : current;
                navigate(back, false);
            });
        }

        TextView note = tv("Tidak ada halaman website yang ditampilkan. Server hanya menyediakan data; seluruh tampilan dirender sebagai komponen Android native.", 12.5f, muted);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(-1, -2); np.topMargin = dp(18); box.addView(note, np);

        Runnable connect = () -> {
            final String typed = server.getText().toString().trim();
            if (typed.isEmpty()) { server.setError("Masukkan alamat server DeApp."); return; }
            save.setEnabled(false); save.setText("Menguji koneksi…"); status.setText("Menghubungkan ke server…"); status.setTextColor(muted);
            runNet(() -> probeServer(typed), resolved -> {
                save.setEnabled(true); save.setText("Hubungkan");
                boolean serverChanged = !resolved.equals(baseUrl);
                baseUrl = resolved;
                SharedPreferences.Editor edit = prefs.edit().putString(KEY_SERVER, resolved);
                if (serverChanged) edit.remove("native_cookies");
                edit.apply();
                if (serverChanged) ownProfilePath = "";
                client = new DeappClient(baseUrl, prefs);
                status.setText("Terhubung · " + resolved); status.setTextColor(success);
                main.postDelayed(() -> { buildShell(); validateSession(); }, 220);
            }, e -> {
                save.setEnabled(true); save.setText("Hubungkan");
                status.setText("Tidak terhubung · " + safeMessage(e, "Periksa alamat server, Wi-Fi, XAMPP, atau hosting."));
                status.setTextColor(danger);
            });
        };
        save.setOnClickListener(v -> connect.run());
        server.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) { connect.run(); return true; }
            return false;
        });
        root.addView(sc, new FrameLayout.LayoutParams(-1, -1)); setContentView(root);
    }

    private View nativeSkeleton(String title, int rows) {
        ScrollView sc = new ScrollView(this);
        LinearLayout box = column(); box.setPadding(dp(14), dp(14), dp(14), dp(24)); sc.addView(box);
        TextView h = bold(title == null || title.isEmpty() ? "DeApp" : title, 16);
        h.setTextColor(muted); box.addView(h);
        for (int i = 0; i < Math.max(2, rows); i++) {
            LinearLayout card = column(); card.setPadding(dp(14), dp(14), dp(14), dp(14)); card.setBackground(outlined(surface, border, 16));
            View a = new View(this); a.setBackground(rounded(surface2, 8)); card.addView(a, new LinearLayout.LayoutParams(i % 2 == 0 ? dp(156) : dp(118), dp(14)));
            View b = new View(this); b.setBackground(rounded(surface2, 8)); LinearLayout.LayoutParams bl = new LinearLayout.LayoutParams(-1, dp(11)); bl.topMargin = dp(12); card.addView(b, bl);
            View c = new View(this); c.setBackground(rounded(surface2, 8)); LinearLayout.LayoutParams cl = new LinearLayout.LayoutParams(i % 2 == 0 ? -1 : dp(220), dp(11)); cl.topMargin = dp(8); card.addView(c, cl);
            LinearLayout.LayoutParams cp = cardMargin(); cp.topMargin = dp(10); box.addView(card, cp);
        }
        return sc;
    }

    private String safeMessage(Exception e, String fallback) {
        if (e == null) return fallback == null ? "" : fallback;
        String m = e.getMessage();
        return m == null || m.trim().isEmpty() ? (fallback == null ? "" : fallback) : m.trim();
    }

    private String responseHtml(String body) {
        if (body == null) return "";
        String trimmed = body.trim();
        if (!trimmed.startsWith("{")) return body;
        try {
            JSONObject o = new JSONObject(trimmed);
            String html = o.optString("html", "");
            if (!html.isEmpty()) return html;
            JSONObject data = o.optJSONObject("data");
            if (data != null && !data.optString("html", "").isEmpty()) return data.optString("html", "");
        } catch (Exception ignored) {}
        return body;
    }

    private String responseError(String body, String fallback) {
        if (body != null) {
            try {
                JSONObject o = new JSONObject(body);
                Object err = o.opt("error");
                if (err instanceof String && !((String) err).isEmpty()) return (String) err;
                if (err instanceof JSONObject) {
                    String msg = ((JSONObject) err).optString("message", "");
                    if (!msg.isEmpty()) return msg;
                }
                String msg = o.optString("message", "");
                if (!msg.isEmpty()) return msg;
            } catch (Exception ignored) {}
        }
        return fallback;
    }

    private boolean renderJsonNative(LinearLayout box, String body) {
        if (body == null || !body.trim().startsWith("{")) return false;
        try {
            JSONObject rootJson = new JSONObject(body);
            Object data = rootJson.opt("data");
            if (data == null || data == JSONObject.NULL) data = rootJson.opt("items");
            if (data == null || data == JSONObject.NULL) return false;
            if (data instanceof JSONObject) {
                addJsonObjectCard(box, (JSONObject) data, "Data");
                return true;
            }
            if (data instanceof JSONArray) {
                JSONArray arr = (JSONArray) data;
                if (arr.length() == 0) { box.addView(emptyState("Belum ada data", "Server terhubung dan mengembalikan daftar kosong.")); return true; }
                int max = Math.min(arr.length(), 40);
                for (int i = 0; i < max; i++) {
                    Object item = arr.opt(i);
                    if (item instanceof JSONObject) addJsonObjectCard(box, (JSONObject) item, "Item " + (i + 1));
                    else if (item != null && item != JSONObject.NULL) addNativeLinkRow(box, String.valueOf(item), "");
                }
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private void addJsonObjectCard(LinearLayout box, JSONObject obj, String fallbackTitle) {
        LinearLayout card = column(); card.setPadding(dp(15), dp(14), dp(15), dp(14)); card.setBackground(outlined(surface, border, 18));
        String heading = obj.optString("name", obj.optString("title", obj.optString("username", fallbackTitle)));
        if (!heading.isEmpty()) card.addView(bold(heading, 15.5f));
        int shown = 0;
        java.util.Iterator<String> keys = obj.keys();
        while (keys.hasNext() && shown < 8) {
            String key = keys.next();
            if ("name".equals(key) || "title".equals(key) || "username".equals(key) || "html".equals(key)) continue;
            Object value = obj.opt(key);
            if (value == null || value == JSONObject.NULL || value instanceof JSONObject || value instanceof JSONArray) continue;
            String v = String.valueOf(value).trim(); if (v.isEmpty()) continue;
            TextView row = tv(key.replace('_',' ') + "  ·  " + v, 12.8f, "id".equals(key) ? muted : text);
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1,-2); rp.topMargin = dp(7); card.addView(row, rp); shown++;
        }
        box.addView(card, cardMargin());
    }

    private View centeredMessage(String title, String body) {
        LinearLayout l = column(); l.setGravity(Gravity.CENTER); l.setPadding(dp(28), dp(28), dp(28), dp(28));
        ProgressBar p = new ProgressBar(this); l.addView(p, new LinearLayout.LayoutParams(dp(36), dp(36)));
        TextView h = bold(title, 20); h.setGravity(Gravity.CENTER); LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-1, -2); hp.topMargin = dp(18); l.addView(h, hp);
        TextView b = tv(body, 14, muted); b.setGravity(Gravity.CENTER); LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, -2); bp.topMargin = dp(8); l.addView(b, bp);
        return l;
    }

    private void showLogin() {
        history.clear(); current = new Screen("login", "", "Masuk");
        configureChrome("Masuk", false, 0, "", null); showBottom(false);
        ScrollView sc = new ScrollView(this); LinearLayout box = column(); box.setPadding(dp(24), dp(34), dp(24), dp(34)); sc.addView(box);
        TextView brand = bold("DeApp", 34); box.addView(brand);
        TextView head = bold("Senang bertemu lagi.", 24); LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-1,-2); hp.topMargin=dp(26); box.addView(head,hp);
        TextView sub = tv("Masuk untuk melanjutkan obrolan, kiriman, dan komunitasmu.", 14.5f, muted); LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1,-2); sp.topMargin=dp(8); box.addView(sub,sp);
        EditText ident = field("Username atau email"); LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(-1,dp(54)); ip.topMargin=dp(28); box.addView(ident,ip);
        EditText pass = field("Kata sandi"); pass.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD); LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(-1,dp(54)); pp.topMargin=dp(12); box.addView(pass,pp);
        Button login = primary("Masuk"); LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,dp(52)); lp.topMargin=dp(16); box.addView(login,lp);
        TextView register = tv("Belum punya akun?  Daftar", 14, accent); register.setGravity(Gravity.CENTER); register.setPadding(0,dp(22),0,dp(22)); register.setOnClickListener(v -> showRegisterStep(1,new RegisterState())); box.addView(register);
        login.setOnClickListener(v -> {
            String id = ident.getText().toString().trim(), pw = pass.getText().toString();
            if (id.isEmpty()) { ident.setError("Wajib diisi"); return; }
            if (pw.isEmpty()) { pass.setError("Wajib diisi"); return; }
            login.setEnabled(false); login.setText("Masuk…");
            runNet(() -> {
                DeappClient.Result g = client.get("/login.php"); String csrf = csrfFrom(g.body,g.url);
                LinkedHashMap<String,String> f = new LinkedHashMap<>(); f.put("csrf",csrf); f.put("identifier",id); f.put("password",pw);
                return client.postForm("/login.php",f);
            }, r -> {
                login.setEnabled(true); login.setText("Masuk");
                if (!looksLoggedOut(r)) { discoverOwnProfile(r.body,r.url); goRoot("home","","Beranda"); showNativeNotice("Berhasil masuk","Selamat datang kembali.",success); }
                else if (Jsoup.parse(r.body,r.url).selectFirst("input[name=step][value=twofa]") != null) showTwoFactor(r.body,r.url);
                else showNativeNotice("Gagal masuk", htmlError(r.body,r.url,"Username/email atau kata sandi salah."), danger);
            }, e -> { login.setEnabled(true); login.setText("Masuk"); showNativeNotice("Tidak terhubung",e.getMessage(),danger); });
        });
        swap(sc);
    }

    private void showTwoFactor(String html, String url) {
        configureChrome("Verifikasi", true, 0,"",null); showBottom(false);
        ScrollView sc=new ScrollView(this); LinearLayout box=column(); box.setPadding(dp(24),dp(40),dp(24),dp(30)); sc.addView(box);
        box.addView(bold("Satu langkah lagi",24)); TextView s=tv("Masukkan kode autentikator atau kode cadangan.",14,muted); LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);sp.topMargin=dp(8);box.addView(s,sp);
        EditText code=field("123456"); code.setInputType(InputType.TYPE_CLASS_NUMBER); LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(54));cp.topMargin=dp(24);box.addView(code,cp);
        Button submit=primary("Verifikasi & masuk"); LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(52));bp.topMargin=dp(14);box.addView(submit,bp);
        String csrf=csrfFrom(html,url);
        submit.setOnClickListener(v->{ String c=code.getText().toString().trim(); if(c.isEmpty())return; submit.setEnabled(false);
            runNet(()->{LinkedHashMap<String,String>f=new LinkedHashMap<>();f.put("csrf",csrf);f.put("step","twofa");f.put("code",c);return client.postForm("/login.php",f);},r->{submit.setEnabled(true);if(!looksLoggedOut(r)){discoverOwnProfile(r.body,r.url);goRoot("home","","Beranda");}else showNativeNotice("Kode belum benar",htmlError(r.body,r.url,"Coba kode terbaru."),danger);},e->{submit.setEnabled(true);showNativeNotice("Gagal",e.getMessage(),danger);});
        });
        swap(sc);
    }

    static final class RegisterState { String name="",email="",password="",confirm="",username=""; }

    private void showRegisterStep(int step, RegisterState s) {
        current = new Screen("register", String.valueOf(step), "Daftar");
        configureChrome("Daftar", true, 0,"",null); showBottom(false);
        ScrollView sc=new ScrollView(this);LinearLayout box=column();box.setPadding(dp(24),dp(28),dp(24),dp(30));sc.addView(box);
        TextView progress=tv("Langkah "+step+" dari 3",12,muted);box.addView(progress);
        TextView h=bold(step==1?"Kenalkan dirimu":step==2?"Amankan akunmu":"Pilih username",24);LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,-2);hp.topMargin=dp(10);box.addView(h,hp);
        if(step==1){
            EditText n=field("Nama tampilan");n.setText(s.name);EditText e=field("Alamat email");e.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);e.setText(s.email);addField(box,n,22);addField(box,e,12);
            Button next=primary("Lanjutkan");addButton(box,next,18);next.setOnClickListener(v->{s.name=n.getText().toString().trim();s.email=e.getText().toString().trim();if(s.name.isEmpty()){n.setError("Isi nama tampilan");return;}if(!s.email.contains("@")){e.setError("Email belum valid");return;}showRegisterStep(2,s);});
        } else if(step==2){
            EditText p=field("Kata sandi minimal 8 karakter");p.setInputType(129);p.setText(s.password);EditText c=field("Ulangi kata sandi");c.setInputType(129);c.setText(s.confirm);addField(box,p,22);addField(box,c,12);
            Button next=primary("Lanjutkan");addButton(box,next,18);next.setOnClickListener(v->{s.password=p.getText().toString();s.confirm=c.getText().toString();if(s.password.length()<8){p.setError("Minimal 8 karakter");return;}if(!s.password.equals(s.confirm)){c.setError("Kata sandi tidak sama");return;}showRegisterStep(3,s);});
        } else {
            EditText u=field("username");u.setText(s.username);addField(box,u,22);TextView note=tv("3–20 karakter: huruf kecil, angka, dan garis bawah.",12.5f,muted);LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-1,-2);np.topMargin=dp(8);box.addView(note,np);
            Button create=primary("Buat akun");addButton(box,create,18);create.setOnClickListener(v->{s.username=u.getText().toString().trim().toLowerCase(Locale.ROOT);if(!s.username.matches("[a-z0-9_]{3,20}")){u.setError("Username belum valid");return;}create.setEnabled(false);create.setText("Membuat akun…");
                runNet(()->{DeappClient.Result g=client.get("/register.php");String csrf=csrfFrom(g.body,g.url);LinkedHashMap<String,String>f=new LinkedHashMap<>();f.put("csrf",csrf);f.put("full_name",s.name);f.put("email",s.email);f.put("password",s.password);f.put("confirm_password",s.confirm);f.put("username",s.username);return client.postForm("/register.php",f);},r->{create.setEnabled(true);create.setText("Buat akun");if(!looksLoggedOut(r)&&!r.url.contains("register.php")){discoverOwnProfile(r.body,r.url);showWelcome();}else showNativeNotice("Belum berhasil",htmlError(r.body,r.url,"Periksa data pendaftaran."),danger);},e->{create.setEnabled(true);create.setText("Buat akun");showNativeNotice("Tidak terhubung",e.getMessage(),danger);});
            });
        }
        swap(sc);
    }

    private void addField(LinearLayout box, View v, int top){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(54));p.topMargin=dp(top);box.addView(v,p);}    
    private void addButton(LinearLayout box, View v, int top){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(52));p.topMargin=dp(top);box.addView(v,p);}    

    private void showWelcome() {
        FrameLayout overlay=new FrameLayout(this);overlay.setBackgroundColor(bg);LinearLayout c=column();c.setGravity(Gravity.CENTER);TextView logo=bold("DeApp",38);logo.setGravity(Gravity.CENTER);TextView w=bold("Welcome to DeApp",24);w.setGravity(Gravity.CENTER);c.addView(logo);LinearLayout.LayoutParams wp=new LinearLayout.LayoutParams(-1,-2);wp.topMargin=dp(16);c.addView(w,wp);overlay.addView(c,new FrameLayout.LayoutParams(-1,-1));
        configureChrome("",false,0,"",null);topBar.setVisibility(View.GONE);showBottom(false);swap(overlay);logo.setScaleX(.82f);logo.setScaleY(.82f);logo.setAlpha(0f);w.setAlpha(0f);logo.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(420).start();w.animate().alpha(1f).setStartDelay(220).setDuration(360).start();main.postDelayed(()->{topBar.setVisibility(View.VISIBLE);goRoot("home","","Beranda");},1500);
    }

    private void goRoot(String key,String arg,String title){history.clear();navigate(new Screen(key,arg,title),false);}    

    private void navigate(Screen s, boolean push) {
        if(push && current != null && !"boot".equals(current.key) && !"login".equals(current.key)) history.push(current);
        current=s;
        switch(s.key){
            case "home": showHome(); break;
            case "messages": showMessages(); break;
            case "chat": showChat(parseInt(s.arg)); break;
            case "notifications": showNotifications(); break;
            case "profile": showProfile(s.arg); break;
            case "compose": showComposer(); break;
            case "story-create": showStoryCreate(); break;
            case "post": showPost(parseInt(s.arg)); break;
            case "search": showSearch(); break;
            case "shop": showShop(); break;
            case "shop-detail": showShopDetail(s.arg,s.title); break;
            case "shop-more": showShopMore(); break;
            case "topup": showTopup(); break;
            case "settings": showSettings(); break;
            case "settings-detail": showSettingsDetail(s.arg,s.title); break;
            case "profile-settings": showProfileSettings(); break;
            case "profile-media": showProfileMedia(); break;
            case "features": showAllFeatures(); break;
            case "module": showNativeModule(s.arg,s.title); break;
            default: showHome();
        }
    }

    private void handleBack(){
        if("login".equals(current.key)){finish();return;}
        if("register".equals(current.key)){showLogin();return;}
        if("notifications".equals(current.key) || "shop".equals(current.key) || "settings".equals(current.key)){history.clear();goRoot("home","","Beranda");return;}
        if("shop-detail".equals(current.key) || "shop-more".equals(current.key) || "topup".equals(current.key)){
            history.clear(); navigate(new Screen("shop","","Toko & Dompet"),false); return;
        }
        if("profile-media".equals(current.key)){navigate(new Screen("profile-settings","","Pengaturan Profil"),false);return;}
        if("profile-settings".equals(current.key)){history.clear();navigate(new Screen("settings","","Pengaturan"),false);return;}
        if("settings-detail".equals(current.key)){if(current.arg.contains("native_section=")){history.clear();navigate(new Screen("profile-settings","","Pengaturan Profil"),false);}else{history.clear();navigate(new Screen("settings","","Pengaturan"),false);}return;}
        if(!history.isEmpty()){Screen s=history.pop();navigate(s,false);return;}
        if(!"home".equals(current.key)){goRoot("home","","Beranda");return;}
        finish();
    }

    private void showHome() {
        configureChrome("DeApp",false,R.drawable.ic_native_search,"Cari",v->navigate(new Screen("search","","Cari"),true));showBottom(true);
        SwipeRefreshLayout refresh=new SwipeRefreshLayout(this);refresh.setColorSchemeColors(text);refresh.setProgressBackgroundColorSchemeColor(surface);
        ScrollView sc=new ScrollView(this);LinearLayout feed=column();feed.setPadding(0,dp(8),0,dp(18));sc.addView(feed);refresh.addView(sc);swap(refresh);
        LinearLayout composer=rowCard();TextView avatar=bold("+",22);avatar.setGravity(Gravity.CENTER);avatar.setBackground(rounded(surface2,99));composer.addView(avatar,new LinearLayout.LayoutParams(dp(42),dp(42)));TextView ask=tv("Apa yang baru?",15,muted);ask.setGravity(Gravity.CENTER_VERTICAL);LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(0,dp(42),1);ap.leftMargin=dp(12);composer.addView(ask,ap);composer.setOnClickListener(v->navigate(new Screen("compose","","Postingan"),true));feed.addView(composer,cardMargin());
        LinearLayout discovery=column();feed.addView(discovery);
        LinearLayout loading=column();loading.setPadding(dp(12),dp(10),dp(12),dp(18));for(int i=0;i<3;i++){LinearLayout ph=rowCard();View dot=new View(this);dot.setBackground(rounded(surface2,99));ph.addView(dot,new LinearLayout.LayoutParams(dp(42),dp(42)));LinearLayout lines=column();lines.setBackgroundColor(Color.TRANSPARENT);View l1=new View(this);l1.setBackground(rounded(surface2,8));lines.addView(l1,new LinearLayout.LayoutParams(dp(126),dp(12)));View l2=new View(this);l2.setBackground(rounded(surface2,8));LinearLayout.LayoutParams l2p=new LinearLayout.LayoutParams(-1,dp(10));l2p.topMargin=dp(9);lines.addView(l2,l2p);LinearLayout.LayoutParams lip=new LinearLayout.LayoutParams(0,-2,1);lip.leftMargin=dp(12);ph.addView(lines,lip);loading.addView(ph,cardMargin());}feed.addView(loading);
        Runnable load=()->{
            runNet(()->client.get("/api/feed.php?scope=feed&offset=0"),r->{refresh.setRefreshing(false);if(looksLoggedOut(r)){showLogin();return;}if(!r.ok()){renderFeedError(feed,responseError(r.body,"Feed belum dapat dimuat."));return;}renderFeed(feed,r.body,r.url);},e->{refresh.setRefreshing(false);renderFeedError(feed,"Feed belum dapat dimuat · "+safeMessage(e,"periksa koneksi server."));});
            loadHomeDiscovery(discovery);
        };
        refresh.setOnRefreshListener(load::run);load.run();
    }

    private void renderFeed(LinearLayout feed,String body,String url){
        while(feed.getChildCount()>2)feed.removeViewAt(2);
        String html=responseHtml(body);
        Document d=Jsoup.parse(html,url);Elements cards=d.select("article.post-card");
        if(cards.isEmpty()){
            String message="Belum ada kiriman untuk ditampilkan.";
            try{JSONObject o=new JSONObject(body);if(!o.optBoolean("success",true))message=o.optString("error",message);}catch(Exception ignored){}
            TextView empty=tv(message,14,muted);empty.setGravity(Gravity.CENTER);empty.setPadding(dp(20),dp(34),dp(20),dp(34));feed.addView(empty);return;
        }
        int count=0;for(Element e:cards){if(count++>=30)break;Post p=parsePost(e);feed.addView(postCard(p),cardMargin());}
    }

    private void renderFeedError(LinearLayout feed,String message){
        while(feed.getChildCount()>2)feed.removeViewAt(2);
        LinearLayout card=column();card.setPadding(dp(18),dp(22),dp(18),dp(22));card.setGravity(Gravity.CENTER);card.setBackground(outlined(surface,border,18));
        TextView h=bold("Beranda belum tersambung",16);h.setGravity(Gravity.CENTER);card.addView(h);
        TextView m=tv(message+"\n"+connectionHint(),13,muted);m.setGravity(Gravity.CENTER);LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(-1,-2);mp.topMargin=dp(8);card.addView(m,mp);
        Button retry=primary("Coba lagi");LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(48));rp.topMargin=dp(16);card.addView(retry,rp);retry.setOnClickListener(v->showHome());
        Button server=secondaryButton("Atur server DeApp");LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(46));sp.topMargin=dp(8);card.addView(server,sp);server.setOnClickListener(v->showServerSetup(true));
        feed.addView(card,cardMargin());
    }

    private void loadHomeDiscovery(LinearLayout host){
        runNet(()->client.get("/index.php"),r->{
            if(looksLoggedOut(r)) return;
            host.removeAllViews();
            Document d=Jsoup.parse(responseHtml(r.body),r.url);
            Elements people=d.select(".suggest-item");
            if(!people.isEmpty()) addPeopleRail(host,people);
            Elements ads=d.select(".deapp-ad-card");
            if(!ads.isEmpty()) addSponsoredRail(host,ads);
        },e->{});
    }

    private void addPeopleRail(LinearLayout host,Elements people){
        TextView h=bold("Orang yang mungkin Anda kenal",15.5f);h.setPadding(dp(14),dp(18),dp(14),dp(8));host.addView(h);
        HorizontalScrollView hsv=new HorizontalScrollView(this);hsv.setHorizontalScrollBarEnabled(false);LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);row.setPadding(dp(10),0,dp(10),dp(4));hsv.addView(row);
        int count=0;for(Element e:people){if(count++>=10)break;Element link=e.selectFirst("a.suggest-name");if(link==null)continue;String name=link.text().trim();String href=relative(link.absUrl("href"));String user=textOf(e,".suggest-sub","");Element img=e.selectFirst("img");
            LinearLayout card=column();card.setPadding(dp(12),dp(14),dp(12),dp(12));card.setGravity(Gravity.CENTER_HORIZONTAL);card.setBackground(outlined(surface,border,18));
            ImageView av=avatarView(58);if(img!=null)loadImage(av,img.absUrl("src"));card.addView(av);TextView n=bold(name.isEmpty()?"Pengguna":name,13.5f);n.setGravity(Gravity.CENTER);LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-1,-2);np.topMargin=dp(9);card.addView(n,np);TextView u=tv(user,11.5f,muted);u.setGravity(Gravity.CENTER);card.addView(u);TextView see=tv("Lihat profil",12.5f,accent);see.setGravity(Gravity.CENTER);see.setPadding(0,dp(10),0,0);card.addView(see);card.setOnClickListener(v->navigate(new Screen("profile",href,"Profil"),true));
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(dp(164),dp(166));cp.rightMargin=dp(8);row.addView(card,cp);
        }
        host.addView(hsv,new LinearLayout.LayoutParams(-1,dp(178)));
    }

    private void addSponsoredRail(LinearLayout host,Elements ads){
        TextView h=bold("Bersponsor",15.5f);h.setPadding(dp(14),dp(16),dp(14),dp(8));host.addView(h);
        HorizontalScrollView hsv=new HorizontalScrollView(this);hsv.setHorizontalScrollBarEnabled(false);LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);row.setPadding(dp(10),0,dp(10),dp(6));hsv.addView(row);int count=0;
        for(Element e:ads){if(count++>=6)break;String brand=textOf(e,".ad-brand b","Sponsor");String body=textOf(e,".ad-copy,.ad-body,.ad-text","");Element im=e.selectFirst(".ad-media img,img");Element dest=e.selectFirst("a.js-ad-destination,a[rel~=sponsored]");String href=dest==null?"":dest.absUrl("href");String cta=dest==null?"Pelajari":dest.text().trim();
            LinearLayout card=column();card.setPadding(dp(14),dp(13),dp(14),dp(13));card.setBackground(outlined(surface,border,18));TextView label=tv("Disponsori",11.5f,muted);card.addView(label);TextView b=bold(brand,14.5f);LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,-2);bp.topMargin=dp(4);card.addView(b,bp);if(!body.isEmpty()){TextView tx=tv(body,13.5f,text);tx.setMaxLines(3);LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(-1,-2);tp.topMargin=dp(8);card.addView(tx,tp);}if(im!=null){ImageView image=new ImageView(this);image.setScaleType(ImageView.ScaleType.CENTER_CROP);image.setBackground(rounded(surface2,14));image.setClipToOutline(true);image.setOutlineProvider(roundOutline(14));loadImage(image,im.absUrl("src"));LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(-1,dp(112));ip.topMargin=dp(10);card.addView(image,ip);}TextView action=tv(cta.isEmpty()?"Pelajari":cta,13,accent);action.setPadding(0,dp(10),0,0);card.addView(action);if(!href.isEmpty())card.setOnClickListener(v->openExternal(href));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(dp(270),-2);cp.rightMargin=dp(8);row.addView(card,cp);
        }
        host.addView(hsv,new LinearLayout.LayoutParams(-1,-2));
    }

    private void openExternal(String url){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}catch(Exception e){showNativeNotice("Tidak dapat membuka tautan",url,danger);}}

    static final class Post {int id;String name="",username="",avatar="",time="",content="",media="",reactions="",comments="";}
    private Post parsePost(Element e){Post p=new Post();p.id=parseInt(e.attr("data-post-id"));Element n=e.selectFirst(".post-name");if(n!=null){p.name=n.text();p.username=usernameFromHref(n.absUrl("href"));}Element a=e.selectFirst("img.post-avatar");if(a!=null)p.avatar=a.absUrl("src");Element t=e.selectFirst(".post-time");if(t!=null)p.time=t.text();Element c=e.selectFirst(".post-content,.post-bg-text");if(c!=null)p.content=c.text();Element m=e.selectFirst(".post-media img,.media-grid img,.post-media video[poster]");if(m!=null)p.media=m.hasAttr("src")?m.absUrl("src"):m.absUrl("poster");Element r=e.selectFirst(".js-react-count");if(r!=null)p.reactions=r.text();Element co=e.selectFirst(".js-comment-count");if(co!=null)p.comments=co.text();return p;}

    private View postCard(Post p){
        LinearLayout card=column();card.setPadding(dp(14),dp(14),dp(14),dp(10));card.setBackground(outlined(surface,border,18));
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);ImageView av=avatarView(42);loadImage(av,p.avatar);head.addView(av);LinearLayout meta=column();meta.setBackgroundColor(Color.TRANSPARENT);TextView name=bold(p.name.isEmpty()?"Pengguna":p.name,14.5f);TextView sub=tv((p.username.isEmpty()?"":"@"+p.username+(p.time.isEmpty()?"":" · "))+p.time,12,muted);meta.addView(name);meta.addView(sub);LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(0,-2,1);mp.leftMargin=dp(10);head.addView(meta,mp);card.addView(head);
        if(!p.content.isEmpty()){TextView body=tv(p.content,15,text);body.setTextIsSelectable(false);LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,-2);bp.topMargin=dp(12);card.addView(body,bp);}if(!p.media.isEmpty()){ImageView im=new ImageView(this);im.setScaleType(ImageView.ScaleType.CENTER_CROP);im.setBackground(rounded(surface2,14));im.setClipToOutline(true);im.setOutlineProvider(roundOutline(14));loadImage(im,p.media);LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(-1,dp(220));ip.topMargin=dp(12);card.addView(im,ip);}
        LinearLayout actions=new LinearLayout(this);actions.setGravity(Gravity.CENTER_VERTICAL);TextView like=action("♡  "+(p.reactions.isEmpty()?"":p.reactions));TextView comment=action("◯  "+(p.comments.isEmpty()?"":p.comments));TextView share=action("↗");actions.addView(like,new LinearLayout.LayoutParams(0,dp(42),1));actions.addView(comment,new LinearLayout.LayoutParams(0,dp(42),1));actions.addView(share,new LinearLayout.LayoutParams(0,dp(42),1));LinearLayout.LayoutParams actp=new LinearLayout.LayoutParams(-1,dp(44));actp.topMargin=dp(6);card.addView(actions,actp);
        View.OnClickListener open=v->navigate(new Screen("post",String.valueOf(p.id),"Postingan"),true);card.setOnClickListener(open);comment.setOnClickListener(open);like.setOnClickListener(v->reactToPost(p.id));return card;
    }

    private TextView action(String label){TextView v=tv(label,14,text);v.setGravity(Gravity.CENTER);v.setBackground(rounded(Color.TRANSPARENT,99));return v;}
    private LinearLayout rowCard(){LinearLayout l=new LinearLayout(this);l.setGravity(Gravity.CENTER_VERTICAL);l.setPadding(dp(14),dp(12),dp(14),dp(12));l.setBackground(outlined(surface,border,18));return l;}
    private LinearLayout.LayoutParams cardMargin(){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.leftMargin=dp(10);p.rightMargin=dp(10);p.topMargin=dp(8);return p;}

    private void reactToPost(int id){runNet(()->{String csrf=client.ensureCsrf();JSONObject j=new JSONObject();j.put("csrf",csrf);j.put("post_id",id);j.put("type","like");return client.postJson("/api/post_react.php",j.toString());},r->{if(r.ok()){tone.startTone(ToneGenerator.TONE_PROP_BEEP,45);showNativeNotice("Reaksi dikirim","Kiriman diperbarui.",accent);}else showNativeNotice("Belum berhasil",jsonError(r.body,"Reaksi gagal."),danger);},e->showNativeNotice("Gagal",e.getMessage(),danger));}

    private void showComposer(){
        configureChrome("Postingan",true,R.drawable.ic_native_send,"Terbitkan",v -> {});showBottom(false);LinearLayout box=column();box.setPadding(dp(16),dp(12),dp(16),dp(18));EditText input=new EditText(this);input.setHint("Apa yang baru?");input.setHintTextColor(muted);input.setTextColor(text);input.setTextSize(17);input.setGravity(Gravity.TOP);input.setBackgroundColor(Color.TRANSPARENT);input.setMinLines(7);input.setMaxLines(18);box.addView(input,new LinearLayout.LayoutParams(-1,0,1));TextView hint=tv("Postingan teks · publik",12.5f,muted);box.addView(hint);swap(box);topAction.setOnClickListener(v->{String body=input.getText().toString().trim();if(body.isEmpty()){input.setError("Tulis sesuatu dulu");return;}topAction.setEnabled(false);runNet(()->{String csrf=client.ensureCsrf();JSONObject j=new JSONObject();j.put("csrf",csrf);j.put("content",body);j.put("privacy","public");return client.postJson("/api/post_create.php",j.toString());},r->{topAction.setEnabled(true);if(r.ok()){tone.startTone(ToneGenerator.TONE_PROP_ACK,70);showNativeNotice("Diposting","Kiriman sudah tampil di DeApp.",success);goRoot("home","","Beranda");}else showNativeNotice("Gagal memposting",jsonError(r.body,"Coba lagi."),danger);},e->{topAction.setEnabled(true);showNativeNotice("Gagal",e.getMessage(),danger);});});
    }

    private void showStoryCreate(){
        configureChrome("Buat Cerita",true,R.drawable.ic_native_send,"Terbitkan",v->{});showBottom(false);pendingStoryImage=null;
        LinearLayout box=column();box.setPadding(dp(16),dp(14),dp(16),dp(20));EditText caption=new EditText(this);caption.setHint("Tulis cerita…");caption.setHintTextColor(muted);caption.setTextColor(text);caption.setTextSize(18);caption.setGravity(Gravity.TOP);caption.setBackground(rounded(surface2,18));caption.setPadding(dp(16),dp(16),dp(16),dp(16));caption.setMinLines(8);box.addView(caption,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout opts=new LinearLayout(this);opts.setGravity(Gravity.CENTER_VERTICAL);TextView photo=tv("＋ Foto",14,accent);photo.setGravity(Gravity.CENTER);photo.setBackground(outlined(surface,border,14));photo.setPadding(dp(16),dp(12),dp(16),dp(12));photo.setOnClickListener(v->{pendingImageUpload="story";Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("image/*");startActivityForResult(i,REQ_PICK_PROFILE_IMAGE);});opts.addView(photo,new LinearLayout.LayoutParams(0,-2,1));Switch comments=new Switch(this);comments.setText("Komentar");comments.setTextColor(text);comments.setChecked(true);LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,-2,1);cp.leftMargin=dp(10);opts.addView(comments,cp);LinearLayout.LayoutParams op=new LinearLayout.LayoutParams(-1,-2);op.topMargin=dp(12);box.addView(opts,op);swap(box);
        topAction.setOnClickListener(v->{String textBody=caption.getText().toString().trim();if(textBody.isEmpty()&&pendingStoryImage==null){showNativeNotice("Cerita kosong","Tulis teks atau pilih foto dulu.",danger);return;}topAction.setEnabled(false);runNet(()->{LinkedHashMap<String,String> fields=new LinkedHashMap<>();fields.put("csrf",client.ensureCsrf());fields.put("caption",textBody);fields.put("bg_style","g1");fields.put("allow_comments",comments.isChecked()?"1":"0");return client.postMultipart("/api/story_create.php",fields,pendingStoryImage==null?null:"image","story.jpg",pendingStoryMime,pendingStoryImage);},r->{topAction.setEnabled(true);if(r.ok()){pendingStoryImage=null;showNativeNotice("Cerita diterbitkan","Ceritamu tayang selama 24 jam.",success);goRoot("home","","Beranda");}else showNativeNotice("Belum terbit",jsonError(r.body,"Cerita gagal diterbitkan."),danger);},e->{topAction.setEnabled(true);showNativeNotice("Gagal",e.getMessage(),danger);});});
    }

    private void showPost(int id){configureChrome("Postingan",true,0,"",null);showBottom(false);LinearLayout host=column();ScrollView sc=new ScrollView(this);sc.addView(host);swap(nativeSkeleton("Memuat postingan",3));runNet(()->client.get("/post.php?id="+id),r->{if(looksLoggedOut(r)){showLogin();return;}swap(sc);host.removeAllViews();Document d=Jsoup.parse(responseHtml(r.body),r.url);Element card=d.selectFirst("article.post-card");if(card!=null)host.addView(postCard(parsePost(card)),cardMargin());else host.addView(emptyState("Postingan belum tersedia","Server terhubung, tetapi data postingan tidak ditemukan."));loadComments(host,id);},e->{swap(sc);renderInlineError(host,"Postingan tidak dapat dibuka.",()->showPost(id));});}
    private void loadComments(LinearLayout host,int id){runNet(()->client.get("/api/post_comments.php?post_id="+id),r->{try{JSONObject o=new JSONObject(r.body);JSONArray a=o.optJSONArray("comments");TextView h=bold("Komentar",16);h.setPadding(dp(16),dp(20),dp(16),dp(8));host.addView(h);if(a!=null)for(int i=0;i<a.length();i++){JSONObject c=a.optJSONObject(i);LinearLayout row=column();row.setPadding(dp(16),dp(10),dp(16),dp(10));String name=c.optString("name",c.optString("username","Pengguna"));row.addView(bold(name,13.5f));row.addView(tv(c.optString("text",c.optString("body",c.optString("comment",""))),14.5f,text));host.addView(row);}}catch(Exception ignored){}},e->{});}

    private void showMessages(){
        configureChrome("Chat",false,R.drawable.ic_native_search,"Cari orang",v->navigate(new Screen("search","","Cari"),true));showBottom(true);LinearLayout list=column();ScrollView sc=new ScrollView(this);sc.addView(list);swap(nativeSkeleton("Memuat percakapan",4));runNet(()->client.get("/messages.php"),r->{if(looksLoggedOut(r)){showLogin();return;}swap(sc);list.removeAllViews();Document d=Jsoup.parse(responseHtml(r.body),r.url);Elements threads=d.select("a.thread");if(threads.isEmpty()){list.addView(emptyState("Belum ada percakapan","Mulai percakapan dari profil seseorang."));return;}for(Element e:threads){String href=relative(e.absUrl("href"));int cid=queryInt(href,"c");String name=textOf(e,".thread-top b","Percakapan");String prev=textOf(e,".thread-preview","");String time=textOf(e,".thread-time","");String av="";Element im=e.selectFirst(".thread-avatar img");if(im!=null)av=im.absUrl("src");String finalAv=av;LinearLayout row=rowCard();ImageView iv=avatarView(48);loadImage(iv,finalAv);row.addView(iv);LinearLayout meta=column();meta.setBackgroundColor(Color.TRANSPARENT);LinearLayout nameRow=new LinearLayout(this);TextView n=bold(name,14.5f);TextView tm=tv(time,11.5f,muted);nameRow.addView(n,new LinearLayout.LayoutParams(0,-2,1));nameRow.addView(tm);meta.addView(nameRow);meta.addView(tv(prev,13.5f,muted));LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(0,-2,1);mp.leftMargin=dp(12);row.addView(meta,mp);row.setOnClickListener(v->navigate(new Screen("chat",String.valueOf(cid),name),true));list.addView(row,cardMargin());}},e->{swap(sc);renderInlineError(list,"Daftar chat belum dapat dimuat.",this::showMessages);});
    }

    private void showChat(int convoId){
        configureChrome(current.title.isEmpty()?"Chat":current.title,true,0,"",null);showBottom(false);LinearLayout screen=column();FrameLayout messagesFrame=new FrameLayout(this);ScrollView sc=new ScrollView(this);LinearLayout msgs=column();msgs.setPadding(dp(10),dp(10),dp(10),dp(12));sc.addView(msgs);messagesFrame.addView(sc);screen.addView(messagesFrame,new LinearLayout.LayoutParams(-1,0,1));LinearLayout composer=new LinearLayout(this);composer.setGravity(Gravity.BOTTOM|Gravity.CENTER_VERTICAL);composer.setPadding(dp(8),dp(6),dp(8),dp(8));composer.setBackgroundColor(surface);ImageButton media=iconButton(R.drawable.ic_native_photo,"Media");ImageButton emoji=iconButton(R.drawable.ic_native_sparkles,"Emoji");ImageButton mic=iconButton(R.drawable.ic_native_mic,"Voice note");EditText input=field("Pesan");input.setSingleLine(false);input.setMaxLines(5);input.setMinHeight(dp(46));ImageButton send=iconButton(R.drawable.ic_native_send,"Kirim");send.setColorFilter(accent);composer.addView(media);composer.addView(emoji);composer.addView(mic);LinearLayout.LayoutParams inp=new LinearLayout.LayoutParams(0,-2,1);inp.leftMargin=dp(4);inp.rightMargin=dp(4);composer.addView(input,inp);composer.addView(send);screen.addView(composer,new LinearLayout.LayoutParams(-1,-2));swap(screen);
        Runnable load=()->runNet(()->client.get("/api/message_fetch.php?convo_id="+convoId),r->{renderMessages(msgs,r.body);main.postDelayed(()->sc.fullScroll(View.FOCUS_DOWN),80);},e->showNativeNotice("Chat gagal dimuat",e.getMessage(),danger));load.run();
        input.setOnEditorActionListener((v,action,event)->{if(action==EditorInfo.IME_ACTION_SEND){send.performClick();return true;}return false;});
        send.setOnClickListener(v->{String b=input.getText().toString().trim();if(b.isEmpty())return;send.setEnabled(false);runNet(()->{String csrf=client.ensureCsrf();JSONObject j=new JSONObject();j.put("csrf",csrf);j.put("convo_id",convoId);j.put("body",b);return client.postJson("/api/message_send.php",j.toString());},r->{send.setEnabled(true);if(r.ok()){input.setText("");tone.startTone(ToneGenerator.TONE_PROP_ACK,55);load.run();}else showNativeNotice("Pesan belum terkirim",jsonError(r.body,"Coba lagi."),danger);},e->{send.setEnabled(true);showNativeNotice("Gagal",e.getMessage(),danger);});});
        media.setOnClickListener(v->showNativeNotice("Media","Pengunggahan media native disiapkan pada modul Media Picker.",accent));emoji.setOnClickListener(v->input.append("🙂"));mic.setOnClickListener(v->showNativeNotice("Voice note","Perekam native akan menggunakan izin mikrofon perangkat.",accent));
    }

    private void renderMessages(LinearLayout msgs,String body){msgs.removeAllViews();try{JSONObject o=new JSONObject(body);JSONObject partner=o.optJSONObject("partner");if(partner!=null&&!partner.optString("name").isEmpty())topTitle.setText(partner.optString("name"));JSONArray a=o.optJSONArray("messages");if(a==null)return;for(int i=0;i<a.length();i++){JSONObject m=a.optJSONObject(i);boolean mine=m.optBoolean("mine");TextView b=tv(m.optString("body",m.optString("sticker","")),14.5f,text);b.setPadding(dp(12),dp(9),dp(12),dp(9));b.setBackground(rounded(mine?(dark?Color.parseColor("#214667"):Color.parseColor("#DFF2FF")):surface2,16));LinearLayout line=new LinearLayout(this);line.setGravity(mine?Gravity.RIGHT:Gravity.LEFT);line.addView(b,new LinearLayout.LayoutParams(-2,-2));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(5);msgs.addView(line,lp);TextView tm=tv(m.optString("time",""),10.5f,muted);tm.setGravity(mine?Gravity.RIGHT:Gravity.LEFT);msgs.addView(tm);}}catch(Exception e){msgs.addView(tv("Pesan belum dapat ditampilkan.",13,muted));}}

    private void showNotifications(){configureChrome("Notifikasi",true,R.drawable.ic_native_settings,"Pengaturan notifikasi",v->navigate(new Screen("settings-detail","/settings.php?tab=notifications","Pengaturan Notifikasi"),true));showBottom(true);LinearLayout list=column();ScrollView sc=new ScrollView(this);sc.addView(list);swap(nativeSkeleton("Memuat notifikasi",5));runNet(()->client.get("/api/notif_panel.php"),r->{if(looksLoggedOut(r)){showLogin();return;}swap(sc);list.removeAllViews();try{JSONObject o=new JSONObject(r.body);JSONArray a=o.optJSONArray("items");if(a==null||a.length()==0){list.addView(emptyState("Belum ada notifikasi","Aktivitas baru akan muncul di sini."));return;}for(int i=0;i<a.length();i++){JSONObject n=a.optJSONObject(i);LinearLayout row=rowCard();String av=n.optString("avatar");ImageView iv=avatarView(44);if(!av.isEmpty())loadImage(iv,av);else iv.setImageResource(R.drawable.ic_native_bell);row.addView(iv);LinearLayout meta=column();meta.setBackgroundColor(Color.TRANSPARENT);TextView msg=tv(n.optString("message","Notifikasi"),14.5f,text);TextView tm=tv((n.optBoolean("unread")?"Belum dibaca · ":"")+n.optString("time",""),11.5f,n.optBoolean("unread")?accent:muted);meta.addView(msg);meta.addView(tm);LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(0,-2,1);mp.leftMargin=dp(12);row.addView(meta,mp);String link=n.optString("link","");row.setOnClickListener(v->openPath(link,"Notifikasi"));list.addView(row,cardMargin());}}catch(Exception x){renderInlineError(list,"Notifikasi belum dapat dibaca.",this::showNotifications);}},e->{swap(sc);renderInlineError(list,"Notifikasi belum dapat dimuat.",this::showNotifications);});}

    private void showProfile(String path){configureChrome("Profil",false,R.drawable.ic_native_settings,"Pengaturan profil",v->navigate(new Screen("profile-settings","","Pengaturan Profil"),true));showBottom(true);LinearLayout host=column();ScrollView sc=new ScrollView(this);sc.addView(host);swap(nativeSkeleton("Memuat profil",4));String target=path;if(target==null||target.isEmpty()){runNet(()->client.get("/index.php"),r->{if(looksLoggedOut(r)){showLogin();return;}discoverOwnProfile(r.body,r.url);if(ownProfilePath==null||ownProfilePath.isEmpty()){swap(sc);host.removeAllViews();host.addView(emptyState("Profil belum ditemukan","Server terhubung, tetapi tautan profil akun belum tersedia."));}else showProfile(ownProfilePath);},e->{swap(sc);renderInlineError(host,"Profil belum dapat dibuka.",()->showProfile(""));});return;}runNet(()->client.get(target),r->{if(looksLoggedOut(r)){showLogin();return;}swap(sc);host.removeAllViews();Document d=Jsoup.parse(responseHtml(r.body),r.url);Element cover=d.selectFirst(".profile-cover");if(cover!=null){ImageView cv=new ImageView(this);cv.setScaleType(ImageView.ScaleType.CENTER_CROP);Element cimg=cover.selectFirst("img");if(cimg!=null)loadImage(cv,cimg.absUrl("src"));else cv.setBackgroundColor(surface2);host.addView(cv,new LinearLayout.LayoutParams(-1,dp(150)));}LinearLayout info=column();info.setPadding(dp(16),dp(12),dp(16),dp(18));Element av=d.selectFirst("img.profile-avatar");ImageView ava=avatarView(86);if(av!=null)loadImage(ava,av.absUrl("src"));info.addView(ava);String name=textOf(d,".profile-name","Profil");info.addView(bold(name,22));String un=textOf(d,".profile-username","");info.addView(tv(un,14,muted));String bio=textOf(d,".profile-bio","");if(!bio.isEmpty()){TextView bv=tv(bio,14.5f,text);LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,-2);bp.topMargin=dp(12);info.addView(bv,bp);}Element stats=d.selectFirst(".profile-stats");if(stats!=null){TextView sv=tv(stats.text(),13,muted);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);sp.topMargin=dp(14);info.addView(sv,sp);}host.addView(info);Elements posts=d.select("article.post-card");for(Element e:posts)host.addView(postCard(parsePost(e)),cardMargin());},e->{swap(sc);renderInlineError(host,"Profil belum dapat dimuat.",()->showProfile(target));});}

    private void showSearch(){configureChrome("Cari",true,0,"",null);showBottom(false);LinearLayout box=column();box.setPadding(dp(12),dp(10),dp(12),dp(20));EditText q=field("Cari orang atau tagar");box.addView(q,new LinearLayout.LayoutParams(-1,dp(52)));LinearLayout results=column();LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,0,1);rp.topMargin=dp(8);box.addView(results,rp);swap(box);q.setSingleLine(true);q.setImeOptions(EditorInfo.IME_ACTION_SEARCH);q.setOnEditorActionListener((v,a,e)->{if(a==EditorInfo.IME_ACTION_SEARCH){searchNow(q.getText().toString(),results);return true;}return false;});}
    private void searchNow(String query,LinearLayout results){String q=query.trim();if(q.isEmpty())return;runNet(()->client.get("/api/search_suggest.php?q="+java.net.URLEncoder.encode(q,"UTF-8")),r->{results.removeAllViews();try{JSONObject o=new JSONObject(r.body);JSONArray users=o.optJSONArray("users");if(users!=null)for(int i=0;i<users.length();i++){JSONObject u=users.optJSONObject(i);LinearLayout row=rowCard();ImageView av=avatarView(42);loadImage(av,u.optString("avatar"));row.addView(av);LinearLayout m=column();m.setBackgroundColor(Color.TRANSPARENT);m.addView(bold(u.optString("name","Pengguna"),14.5f));m.addView(tv("@"+u.optString("username"),12,muted));LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(0,-2,1);mp.leftMargin=dp(10);row.addView(m,mp);String path=relative(u.optString("url"));row.setOnClickListener(v->navigate(new Screen("profile",path,"Profil"),true));results.addView(row,cardMargin());}JSONArray tags=o.optJSONArray("tags");if(tags!=null)for(int i=0;i<tags.length();i++){JSONObject t=tags.optJSONObject(i);TextView tr=tv("#"+t.optString("tag")+"   "+t.optString("count"),15,accent);tr.setPadding(dp(16),dp(14),dp(16),dp(14));String path=relative(t.optString("url"));tr.setOnClickListener(v->navigate(new Screen("module",path,"Tagar"),true));results.addView(tr);}}catch(Exception ex){results.addView(tv("Pencarian belum dapat ditampilkan.",13,muted));}},e->showNativeNotice("Pencarian gagal",e.getMessage(),danger));}

    private void showShop(){
        configureChrome("Toko & Dompet",true,R.drawable.ic_native_more,"Semua fitur",v->navigate(new Screen("features","","Semua Fitur"),true));showBottom(false);
        ScrollView sc=new ScrollView(this);LinearLayout box=column();box.setPadding(dp(12),dp(8),dp(12),dp(28));sc.addView(box);
        TextView intro=tv("Kelola koin, transaksi, dan koleksi DeApp dari halaman native terpisah.",13.5f,muted);intro.setPadding(dp(8),dp(6),dp(8),dp(8));box.addView(intro);
        box.addView(sectionTitle("Dompet & Koin"));
        addMenu(box,R.drawable.ic_native_shop,"Dompet","Saldo, riwayat transaksi, dan keamanan dompet","/shop.php?tab=wallet","Dompet");
        addMenu(box,R.drawable.ic_native_add,"Top Up","Isi koin DeApp dan cek riwayat top up","/topup.php","Top Up");
        addMenu(box,R.drawable.ic_native_send,"Kirim Koin","Kirim koin ke pengguna lain","/shop.php?tab=kirim","Kirim Koin");
        addMenu(box,R.drawable.ic_native_code,"Kode Promo","Tukarkan kode promo dan hadiah","/shop.php?tab=promo","Kode Promo");
        box.addView(sectionTitle("Toko & Koleksi"));
        addMenu(box,R.drawable.ic_native_grid,"Etalase","Jelajahi item yang tersedia","/shop.php?tab=etalase","Etalase");
        addMenu(box,R.drawable.ic_native_sparkles,"Pet","Peliharaan virtual DeApp","/shop.php?tab=pet","Pet");
        addMenu(box,R.drawable.ic_native_game,"Item Virtual","Aksesori, booster, dan item pet","/shop.php?tab=virtual","Item Virtual");
        addMenu(box,R.drawable.ic_native_sparkles,"VIP","Paket dan manfaat VIP","/shop.php?tab=vip","VIP");
        LinearLayout more=rowCard();ImageView mi=new ImageView(this);mi.setImageResource(R.drawable.ic_native_more);mi.setColorFilter(text);mi.setPadding(dp(8),dp(8),dp(8),dp(8));mi.setBackground(rounded(surface2,12));more.addView(mi,new LinearLayout.LayoutParams(dp(44),dp(44)));LinearLayout mm=column();mm.setBackgroundColor(Color.TRANSPARENT);mm.addView(bold("Menu lainnya",14.5f));mm.addView(tv("Keinginan, hadiah, tema, stiker, tiket, tas, koleksi, level, dan lainnya",12.3f,muted));LinearLayout.LayoutParams mmp=new LinearLayout.LayoutParams(0,-2,1);mmp.leftMargin=dp(12);more.addView(mm,mmp);more.addView(tv("›",26,muted));more.setOnClickListener(v->navigate(new Screen("shop-more","","Menu lainnya"),true));box.addView(more,cardMargin());
        swap(sc);
    }

    private void showShopMore(){
        configureChrome("Menu lainnya",true,0,"",null);showBottom(false);ScrollView sc=new ScrollView(this);LinearLayout box=column();box.setPadding(dp(12),dp(8),dp(12),dp(28));sc.addView(box);
        String[][] items={{"Keinginan","Item yang kamu simpan untuk dibeli","/shop.php?tab=wishlist"},{"Pet Care 3D","Makanan dan perawatan pet","/shop.php?tab=virtual&vcat=pet_food"},{"Boost Lab","Booster dan XP pet","/shop.php?tab=virtual&vcat=pet_booster"},{"Showroom 3D","Aksesori dan gaya pet","/shop.php?tab=virtual&vcat=pet_accessory"},{"Hadiah","Katalog hadiah virtual","/shop.php?tab=gifts"},{"Tema","Tema profil dan visual","/shop.php?tab=theme"},{"Bingkai","Bingkai profil","/shop.php?tab=frame"},{"Gelembung","Gaya gelembung chat","/shop.php?tab=bubble"},{"Stiker","Paket stiker DeApp","/shop.php?tab=sticker"},{"Efek Nama","Efek nama pengguna","/shop.php?tab=effect"},{"Tiket","Tiket fitur dan rename","/shop.php?tab=ticket"},{"Tas Barang","Inventori item milikmu","/shop.php?tab=bag"},{"Koleksiku","Semua koleksi yang dimiliki","/shop.php?tab=koleksi"},{"Level","Progress dan level akun","/shop.php?tab=level"}};
        int[] icons={R.drawable.ic_native_bookmark,R.drawable.ic_native_sparkles,R.drawable.ic_native_sparkles,R.drawable.ic_native_sparkles,R.drawable.ic_native_sparkles,R.drawable.ic_native_photo,R.drawable.ic_native_sparkles,R.drawable.ic_native_message,R.drawable.ic_native_sparkles,R.drawable.ic_native_sparkles,R.drawable.ic_native_bookmark,R.drawable.ic_native_message,R.drawable.ic_native_bookmark,R.drawable.ic_native_grid};
        for(int i=0;i<items.length;i++)addMenu(box,icons[i],items[i][0],items[i][1],items[i][2],items[i][0]);swap(sc);
    }

    private void showShopDetail(String path,String title){
        configureChrome(title,true,0,"",null);showBottom(false);swap(nativeSkeleton("Memuat "+title,4));
        runNet(()->client.get(path),r->{if(looksLoggedOut(r)){showLogin();return;}Document d=Jsoup.parse(responseHtml(r.body),r.url);ScrollView sc=new ScrollView(this);LinearLayout box=column();box.setPadding(dp(12),dp(8),dp(12),dp(28));sc.addView(box);Element mainEl=d.selectFirst("main#main-content");if(mainEl==null)mainEl=d.body();for(Element nav:mainEl.select(".shop-menu-grid,.wide-side,.site-footer,.topbar,.mobile-bottom-nav"))nav.remove();renderNativeModuleDocument(box,mainEl,r.url,true);swap(sc);},e->showNetworkError("Halaman toko belum dapat dimuat.\n"+safeMessage(e,""),()->showShopDetail(path,title)));
    }

    private void showTopup(){
        configureChrome("Top Up",true,0,"",null);showBottom(false);swap(nativeSkeleton("Memuat Top Up",4));
        runNet(()->client.get("/topup.php"),r->{if(looksLoggedOut(r)){showLogin();return;}Document d=Jsoup.parse(responseHtml(r.body),r.url);ScrollView sc=new ScrollView(this);LinearLayout box=column();box.setPadding(dp(12),dp(8),dp(12),dp(28));sc.addView(box);Element mainEl=d.selectFirst("main#main-content");if(mainEl==null)mainEl=d.body();renderNativeModuleDocument(box,mainEl,r.url,true);swap(sc);},e->showNetworkError("Top Up belum dapat dimuat.\n"+safeMessage(e,""),this::showTopup));
    }

    private void showSettings(){
        configureChrome("Pengaturan",true,0,"",null);showBottom(false);ScrollView sc=new ScrollView(this);LinearLayout box=column();box.setPadding(dp(12),dp(8),dp(12),dp(28));sc.addView(box);
        addMenu(box,R.drawable.ic_native_user,"Profil","Foto profil & sampul, info profil, username","native:profile-settings","Pengaturan Profil");
        addMenu(box,R.drawable.ic_native_sparkles,"Mode Tampilan","Tema, warna, dan tampilan aplikasi","/settings.php?tab=appearance","Mode Tampilan");
        addMenu(box,R.drawable.ic_native_settings,"Pengalaman Aplikasi","Kepadatan, sudut, dan mode hemat visual","/settings.php?tab=experience","Pengalaman Aplikasi");
        addMenu(box,R.drawable.ic_native_contacts,"Bahasa & Terjemahan","Bahasa aplikasi dan terjemahan","/settings.php?tab=language","Bahasa & Terjemahan");
        addMenu(box,R.drawable.ic_native_sparkles,"Aksesibilitas","Animasi, kontras, dan kenyamanan","/settings.php?tab=access","Aksesibilitas");
        addMenu(box,R.drawable.ic_native_bell,"Notifikasi","Notifikasi, suara, dan preferensi","/settings.php?tab=notifications","Pengaturan Notifikasi");
        addMenu(box,R.drawable.ic_native_shield,"Privasi & Keamanan","Privasi akun, blokir, sesi, dan keamanan","/settings.php?tab=privacy","Privasi & Keamanan");
        addMenu(box,R.drawable.ic_native_sparkles,"DeApp AI","Bahasa dan preferensi AI","/settings.php?tab=ai","Pengaturan AI");
        addMenu(box,R.drawable.ic_native_user,"Karakter","Karakter dan asisten virtual","/settings.php?tab=characters","Karakter");
        addMenu(box,R.drawable.ic_native_settings,"Server DeApp","Koneksi backend saat ini: "+connectionHint(),"native:server","Server DeApp");
        TextView logout=tv("Keluar dari akun",15,danger);logout.setGravity(Gravity.CENTER);logout.setPadding(dp(14),dp(16),dp(14),dp(16));logout.setBackground(outlined(surface,border,14));LinearLayout.LayoutParams lp=cardMargin();lp.topMargin=dp(24);box.addView(logout,lp);logout.setOnClickListener(v->logout());swap(sc);
    }

    private void showProfileSettings(){
        configureChrome("Pengaturan Profil",true,0,"",null);showBottom(false);ScrollView sc=new ScrollView(this);LinearLayout box=column();box.setPadding(dp(12),dp(8),dp(12),dp(28));sc.addView(box);
        addMenu(box,R.drawable.ic_native_camera,"Foto Profil & Sampul","Ubah avatar dan foto sampul","native:profile-media","Foto Profil & Sampul");
        addMenu(box,R.drawable.ic_native_user,"Info Profil","Nama tampilan, bio, lokasi, situs, tanggal lahir","native:profile-info","Info Profil");
        addMenu(box,R.drawable.ic_native_user,"Username","Ganti username dan pengaturan alihan","native:username","Username");
        addMenu(box,R.drawable.ic_native_sparkles,"Custom Profile Studio","Tema, pola cover, aura, dan detail profil","/profile-edit.php","Custom Profile Studio");swap(sc);
    }

    private void showProfileMedia(){
        configureChrome("Foto Profil & Sampul",true,0,"",null);showBottom(false);ScrollView sc=new ScrollView(this);LinearLayout box=column();box.setPadding(dp(16),dp(14),dp(16),dp(28));sc.addView(box);
        LinearLayout avatarCard=column();avatarCard.setPadding(dp(16),dp(16),dp(16),dp(16));avatarCard.setBackground(outlined(surface,border,18));ImageView av=avatarView(92);avatarCard.addView(av);avatarCard.addView(bold("Foto profil",16),new LinearLayout.LayoutParams(-1,-2));TextView avSub=tv("JPG, PNG, WEBP atau GIF. Maksimal mengikuti batas server DeApp.",12.5f,muted);avatarCard.addView(avSub);Button avBtn=primary("Pilih foto profil");LinearLayout.LayoutParams abp=new LinearLayout.LayoutParams(-1,dp(48));abp.topMargin=dp(12);avatarCard.addView(avBtn,abp);avBtn.setOnClickListener(v->pickProfileImage("avatar"));box.addView(avatarCard,cardMargin());
        LinearLayout coverCard=column();coverCard.setPadding(dp(16),dp(16),dp(16),dp(16));coverCard.setBackground(outlined(surface,border,18));ImageView cover=new ImageView(this);cover.setScaleType(ImageView.ScaleType.CENTER_CROP);cover.setBackground(rounded(surface2,14));cover.setClipToOutline(true);cover.setOutlineProvider(roundOutline(14));coverCard.addView(cover,new LinearLayout.LayoutParams(-1,dp(150)));TextView ch=bold("Foto sampul",16);LinearLayout.LayoutParams chp=new LinearLayout.LayoutParams(-1,-2);chp.topMargin=dp(12);coverCard.addView(ch,chp);coverCard.addView(tv("Ukuran ideal 1200 × 400 piksel.",12.5f,muted));Button cvBtn=primary("Pilih foto sampul");LinearLayout.LayoutParams cbp=new LinearLayout.LayoutParams(-1,dp(48));cbp.topMargin=dp(12);coverCard.addView(cvBtn,cbp);cvBtn.setOnClickListener(v->pickProfileImage("cover"));box.addView(coverCard,cardMargin());swap(sc);
        String target=ownProfilePath; if(target==null||target.isEmpty())target="/index.php";String finalTarget=target;runNet(()->client.get(finalTarget),r->{Document d=Jsoup.parse(r.body,r.url);Element ai=d.selectFirst("img.profile-avatar,.drawer-user img,.dropdown-user img");if(ai!=null)loadImage(av,ai.absUrl("src"));Element ci=d.selectFirst(".profile-cover img,.profile-cover");if(ci!=null){String src=ci.tagName().equals("img")?ci.absUrl("src"):extractBackgroundUrl(ci.attr("style"),r.url);if(!src.isEmpty())loadImage(cover,src);}},e->{});
    }

    private void pickProfileImage(String type){pendingImageUpload=type;Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("image/*");startActivityForResult(i,REQ_PICK_PROFILE_IMAGE);}

    private String extractBackgroundUrl(String style,String base){if(style==null)return"";int a=style.indexOf("url(");if(a<0)return"";int b=style.indexOf(')',a);if(b<0)return"";String raw=style.substring(a+4,b).replace("\"","").replace("'","").trim();try{return new java.net.URL(new java.net.URL(base),raw).toString();}catch(Exception e){return raw;}}

    private void showSettingsDetail(String path,String title){
        if(path.contains("native_section=info")){showProfileFormSection("profile",title);return;}
        if(path.contains("native_section=username")){showProfileFormSection("username",title);return;}
        showNativeModule(path,title,true);
    }

    private void showProfileFormSection(String actionValue,String title){
        configureChrome(title,true,0,"",null);showBottom(false);swap(nativeSkeleton("Memuat "+title,3));
        runNet(()->client.get("/settings.php?tab=profile"),r->{if(looksLoggedOut(r)){showLogin();return;}Document d=Jsoup.parse(responseHtml(r.body),r.url);Element chosen=null;for(Element f:d.select("form")){Element a=f.selectFirst("input[name=action]");if(a!=null&&actionValue.equals(a.attr("value"))){chosen=f;break;}}ScrollView sc=new ScrollView(this);LinearLayout box=column();box.setPadding(dp(14),dp(10),dp(14),dp(28));sc.addView(box);if(chosen==null){box.addView(emptyState("Belum tersedia","Form "+title+" tidak ditemukan pada server DeApp."));}else{boolean[] bound={false};renderNativeForm(box,chosen,r.url,true,bound);if("username".equals(actionValue)){Element info=chosen.parent();if(info!=null){String hint=textOf(info,"p.muted","Username dipakai pada tautan profil dan mention.");TextView t=tv(hint,12.5f,muted);LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(-1,-2);tp.topMargin=dp(12);box.addView(t,tp);}}}swap(sc);},e->showNetworkError("Pengaturan profil belum dapat dimuat.",()->showProfileFormSection(actionValue,title)));
    }


    private void showAllFeatures(){configureChrome("Semua Fitur",true,0,"",null);showBottom(false);ScrollView sc=new ScrollView(this);LinearLayout box=column();box.setPadding(dp(12),dp(8),dp(12),dp(28));sc.addView(box);String[][] items={{"Cari","Temukan orang, tagar, dan konten","/explore.php"},{"Video Pendek","Video vertikal dan kreator","/reels.php"},{"Live","Siaran langsung DeApp","/live.php"},{"Komunitas","Grup dan forum topik","/community.php"},{"ASK","Pertanyaan dan jawaban","/inbox.php"},{"Tersimpan","Bookmark dan koleksi","/bookmarks.php"},{"Koneksi","Pengikut, mengikuti, dan relasi","/connections.php"},{"Memori","Postingan yang kamu ingat","/memories.php"},{"Toko & Dompet","Koin, pet, item, koleksi","/shop.php"},{"DeApp AI","Asisten AI DeApp","/ai.php"},{"Karakter","Karakter AI dan percakapan","/characters.php"},{"Games","Mini game sosial","/games.php"},{"DeApp Tap","Interaksi cepat","/tap.php"},{"Media","Galeri media akun","/media.php"},{"Iklan","Kampanye dan iklan sponsor","/ads.php"},{"Developer","Aplikasi dan integrasi developer","/developer.php"},{"Bantuan","Pusat bantuan DeApp","/help.php"},{"Pengaturan","Akun dan pengalaman aplikasi","/settings.php"}};int[] icons={R.drawable.ic_native_search,R.drawable.ic_native_video,R.drawable.ic_native_live,R.drawable.ic_native_community,R.drawable.ic_native_message,R.drawable.ic_native_bookmark,R.drawable.ic_native_user,R.drawable.ic_native_sparkles,R.drawable.ic_native_shop,R.drawable.ic_native_sparkles,R.drawable.ic_native_user,R.drawable.ic_native_game,R.drawable.ic_native_tap,R.drawable.ic_native_photo,R.drawable.ic_native_bell,R.drawable.ic_native_code,R.drawable.ic_native_message,R.drawable.ic_native_settings};for(int i=0;i<items.length;i++)addMenu(box,icons[i],items[i][0],items[i][1],items[i][2],items[i][0]);swap(sc);}


    private TextView sectionTitle(String s){TextView h=bold(s,13);h.setTextColor(muted);h.setPadding(dp(8),dp(18),dp(8),dp(8));return h;}
    private void addMenu(LinearLayout box,int icon,String title,String subtitle,String path,String pageTitle){LinearLayout row=rowCard();ImageView i=new ImageView(this);i.setImageResource(icon);i.setColorFilter(text);i.setPadding(dp(8),dp(8),dp(8),dp(8));i.setBackground(rounded(surface2,12));row.addView(i,new LinearLayout.LayoutParams(dp(44),dp(44)));LinearLayout m=column();m.setBackgroundColor(Color.TRANSPARENT);m.addView(bold(title,14.5f));m.addView(tv(subtitle,12.3f,muted));LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(0,-2,1);mp.leftMargin=dp(12);row.addView(m,mp);TextView arrow=tv("›",26,muted);row.addView(arrow);row.setOnClickListener(v->{if("/shop.php".equals(path))navigate(new Screen("shop","","Toko & Dompet"),true);else if("/settings.php".equals(path))navigate(new Screen("settings","","Pengaturan"),true);else openPath(path,pageTitle);});box.addView(row,cardMargin());}

    static final class NativeInputBinding {
        final String name;
        final String kind;
        final View view;
        final ArrayList<String> values;
        NativeInputBinding(String name,String kind,View view,ArrayList<String> values){this.name=name;this.kind=kind;this.view=view;this.values=values;}
    }

    private void showNativeModule(String path,String fallbackTitle){showNativeModule(path,fallbackTitle,false);}

    private void showNativeModule(String path,String fallbackTitle,boolean headerSave){
        configureChrome(fallbackTitle.isEmpty()?"DeApp":fallbackTitle,true,0,"",null);showBottom(false);swap(nativeSkeleton("Memuat "+(fallbackTitle.isEmpty()?"DeApp":fallbackTitle),4));
        runNet(()->client.get(path),r->{
            if(looksLoggedOut(r)){showLogin();return;}
            if(r.body==null||r.body.trim().isEmpty()){showNetworkError("Server terhubung, tetapi halaman tidak mengirim data.\n"+path,()->showNativeModule(path,fallbackTitle,headerSave));return;}
            String html=responseHtml(r.body);ScrollView sc=new ScrollView(this);LinearLayout box=column();box.setPadding(dp(12),dp(8),dp(12),dp(28));sc.addView(box);
            if(r.body.trim().startsWith("{") && html.equals(r.body) && renderJsonNative(box,r.body)){configureChrome(fallbackTitle.isEmpty()?"DeApp":fallbackTitle,true,0,"",null);swap(sc);return;}
            Document d=Jsoup.parse(html,r.url);String title=fallbackTitle;Element h=d.selectFirst("main h1, main h2, .page-title, h1");if(h!=null&&!h.text().trim().isEmpty())title=h.text().trim();
            configureChrome(title,true,0,"",null);
            Element mainEl=d.selectFirst("main#main-content");if(mainEl==null)mainEl=d.selectFirst("main");if(mainEl==null)mainEl=d.body();
            for(Element e:mainEl.select("script,style,nav,.topbar,.mobile-bottom-nav,.wide-side,.settings-nav,.shop-menu-grid,.sidebar,.site-footer"))e.remove();
            renderNativeModuleDocument(box,mainEl,r.url,headerSave);swap(sc);
        },e->showNetworkError("Halaman belum dapat dimuat.",()->showNativeModule(path,fallbackTitle,headerSave)));
    }

    private void renderNativeModuleDocument(LinearLayout box,Element mainEl,String url,boolean headerSave){
        if(mainEl==null){box.addView(emptyState("Konten kosong","Tidak ada data untuk ditampilkan."));return;}
        boolean[] saveBound={false};
        Elements blocks=mainEl.select("section.card,section.settings-section,article,.card,.dev-app-row,.community-card,.game-card,.gift-card,.shop-card,.pet-shop-card,.virtual-shop-card,.reel-card,.credit-row,.notif-row,.empty-state,.alert");
        int rendered=0;
        for(Element block:blocks){
            if(rendered>=70)break;if(hasSelectedAncestor(block,blocks))continue;
            renderNativeBlock(box,block,url,headerSave&&!saveBound[0],saveBound);rendered++;
        }
        if(rendered==0){
            Elements forms=mainEl.select("form");for(Element form:forms){if(rendered++>12)break;renderNativeForm(box,form,url,headerSave&&!saveBound[0],saveBound);}
            Elements links=mainEl.select("a[href]");for(Element a:links){if(rendered++>40)break;String label=a.text().trim();String href=relative(a.absUrl("href"));if(label.isEmpty()||href.isEmpty())continue;addNativeLinkRow(box,label,href);}
            if(rendered==0){String body=mainEl.text().trim();if(body.length()>2600)body=body.substring(0,2600)+"…";box.addView(emptyState(body.isEmpty()?"Data belum tersedia":"DeApp",body.isEmpty()?"Server terhubung, tetapi modul ini belum mengirim konten yang dapat ditampilkan.":body));}
        }
    }

    private void renderNativeBlock(LinearLayout box,Element block,String url,boolean headerSave,boolean[] saveBound){
        LinearLayout card=column();card.setPadding(dp(15),dp(14),dp(15),dp(14));card.setBackground(outlined(surface,border,18));
        Element img=block.selectFirst("img");if(img!=null&&!img.absUrl("src").isEmpty()){ImageView iv=new ImageView(this);iv.setScaleType(ImageView.ScaleType.CENTER_CROP);iv.setBackground(rounded(surface2,14));iv.setClipToOutline(true);iv.setOutlineProvider(roundOutline(14));loadImage(iv,img.absUrl("src"));card.addView(iv,new LinearLayout.LayoutParams(-1,dp(160)));}
        Element head=block.selectFirst("h1,h2,h3,h4,.title,.card-title,.settings-title");if(head!=null&&!head.text().trim().isEmpty()){TextView hv=bold(head.text().trim(),head.tagName().equals("h1")?20:16);LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,-2);hp.topMargin=img==null?0:dp(12);card.addView(hv,hp);}
        Elements paras=block.select("p,.muted,.description,.subtitle,small");int pc=0;for(Element pe:paras){if(pc++>=4)break;if(hasSelectedAncestor(pe,paras))continue;String t=pe.text().trim();if(t.isEmpty()||(head!=null&&t.equals(head.text().trim())))continue;if(t.length()>420)t=t.substring(0,420)+"…";TextView pv=tv(t,13.5f,pe.hasClass("muted")?muted:text);LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,-2);pp.topMargin=dp(7);card.addView(pv,pp);}
        Elements forms=block.select("form");for(Element form:forms){renderNativeForm(card,form,url,headerSave&&!saveBound[0],saveBound);headerSave=false;}
        Elements actions=block.select("a[href],button");int ac=0;for(Element a:actions){if(ac++>=5)break;if(ancestorTag(a,"form")!=null)continue;String label=a.text().trim();if(label.isEmpty())label=a.attr("aria-label");if(label.isEmpty())continue;final String actionLabel=label;TextView action=tv(actionLabel,13.5f,accent);action.setGravity(Gravity.CENTER);action.setPadding(dp(12),dp(12),dp(12),dp(12));action.setBackground(outlined(surface,border,14));LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,-2);ap.topMargin=dp(9);card.addView(action,ap);final String href=a.tagName().equals("a")?relative(a.absUrl("href")):"";if(!href.isEmpty())action.setOnClickListener(v->openPath(href,actionLabel));else if(a.hasClass("js-shop"))action.setOnClickListener(v->performShopAction(a));else if(a.hasClass("js-virtual")||a.hasAttr("data-virtual-action"))action.setOnClickListener(v->performVirtualAction(a));}
        if(card.getChildCount()==0){String t=block.text().trim();if(!t.isEmpty())card.addView(tv(t,14,text));}
        box.addView(card,cardMargin());
    }

    private void addNativeLinkRow(LinearLayout box,String label,String href){
        LinearLayout row=rowCard();TextView t=tv(label,14.5f,text);row.addView(t,new LinearLayout.LayoutParams(0,-2,1));row.addView(tv("›",25,muted));row.setOnClickListener(v->openPath(href,label));box.addView(row,cardMargin());
    }

    private void renderNativeForm(LinearLayout box,Element form,String pageUrl,boolean headerSave,boolean[] saveBound){
        Elements controls=form.select("input[name]:not([type=hidden]):not([type=submit]):not([type=file]),textarea[name],select[name]");if(controls.isEmpty())return;
        LinearLayout formBox=column();formBox.setPadding(dp(2),dp(6),dp(2),dp(2));LinkedHashMap<String,String> hidden=new LinkedHashMap<>();for(Element h:form.select("input[type=hidden][name]"))hidden.put(h.attr("name"),h.attr("value"));
        LinkedHashMap<String,NativeInputBinding> bindings=new LinkedHashMap<>();
        for(Element c:controls){String name=c.attr("name");if(name.isEmpty()||bindings.containsKey(name))continue;String type=c.tagName().equals("input")?c.attr("type").toLowerCase(Locale.ROOT):c.tagName();String label=findFieldLabel(c,name);
            if("checkbox".equals(type)){Switch sw=new Switch(this);sw.setText(label);sw.setTextColor(text);sw.setTextSize(14);sw.setChecked(c.hasAttr("checked"));sw.setPadding(dp(2),dp(8),dp(2),dp(8));formBox.addView(sw,new LinearLayout.LayoutParams(-1,-2));bindings.put(name,new NativeInputBinding(name,"checkbox",sw,null));continue;}
            if("radio".equals(type)){Elements radios=form.select("input[type=radio][name='"+name.replace("'","")+"']");Spinner sp=new Spinner(this);ArrayList<String> labels=new ArrayList<>(),vals=new ArrayList<>();int sel=0,idx=0;for(Element r:radios){String rl=findFieldLabel(r,r.attr("value"));labels.add(rl);vals.add(r.attr("value"));if(r.hasAttr("checked"))sel=idx;idx++;}ArrayAdapter<String> ad=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,labels);sp.setAdapter(ad);sp.setSelection(sel);TextView lh=tv(label,12.5f,muted);lh.setPadding(dp(2),dp(8),dp(2),dp(5));formBox.addView(lh);formBox.addView(sp,new LinearLayout.LayoutParams(-1,dp(52)));bindings.put(name,new NativeInputBinding(name,"select",sp,vals));continue;}
            if("select".equals(type)){Spinner sp=new Spinner(this);ArrayList<String> labels=new ArrayList<>(),vals=new ArrayList<>();int sel=0,idx=0;for(Element o:c.select("option")){labels.add(o.text());vals.add(o.attr("value"));if(o.hasAttr("selected"))sel=idx;idx++;}ArrayAdapter<String> ad=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,labels);sp.setAdapter(ad);sp.setSelection(sel);TextView lh=tv(label,12.5f,muted);lh.setPadding(dp(2),dp(8),dp(2),dp(5));formBox.addView(lh);formBox.addView(sp,new LinearLayout.LayoutParams(-1,dp(52)));bindings.put(name,new NativeInputBinding(name,"select",sp,vals));continue;}
            EditText f=field(label);String value="textarea".equals(type)?c.text():c.attr("value");f.setText(value);if("password".equals(type))f.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);else if("email".equals(type))f.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);else if("number".equals(type))f.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);if("textarea".equals(type)){f.setSingleLine(false);f.setGravity(Gravity.TOP);f.setMinLines(3);f.setPadding(dp(14),dp(12),dp(14),dp(12));}
            TextView lh=tv(label,12.5f,muted);lh.setPadding(dp(2),dp(8),dp(2),dp(5));formBox.addView(lh);LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,"textarea".equals(type)?dp(100):dp(52));formBox.addView(f,fp);bindings.put(name,new NativeInputBinding(name,"text",f,null));
        }
        String action=form.absUrl("action");if(action.isEmpty())action=pageUrl;String finalAction=relative(action);String method=form.attr("method").toLowerCase(Locale.ROOT);String buttonText="Simpan";Element b=form.selectFirst("button[type=submit],input[type=submit],button:not([type])");if(b!=null){String bt=b.tagName().equals("input")?b.attr("value"):b.text();if(!bt.trim().isEmpty())buttonText=bt.trim();}
        final String submitLabel=buttonText;Runnable submit=()->submitNativeForm(finalAction,method,hidden,bindings,submitLabel);
        if(headerSave&&!saveBound[0]){saveBound[0]=true;topAction.setVisibility(View.VISIBLE);topAction.setImageResource(R.drawable.ic_native_save);topAction.setContentDescription("Simpan");topAction.setOnClickListener(v->submit.run());}else{Button sb=primary(submitLabel);LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(48));bp.topMargin=dp(12);formBox.addView(sb,bp);sb.setOnClickListener(v->submit.run());}
        LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,-2);fp.topMargin=dp(4);box.addView(formBox,fp);
    }

    private String findFieldLabel(Element c,String fallback){Element idLabel=null;String id=c.id();if(!id.isEmpty()&&c.ownerDocument()!=null)idLabel=c.ownerDocument().selectFirst("label[for='"+id+"']");if(idLabel==null)idLabel=ancestorTag(c,"label");String label=idLabel==null?"":idLabel.ownText().trim();if(label.isEmpty())label=fallback.replace('_',' ');return label;}
    private Element ancestorTag(Element e,String tag){Element p=e==null?null:e.parent();while(p!=null){if(tag.equalsIgnoreCase(p.tagName()))return p;p=p.parent();}return null;}

    private void submitNativeForm(String action,String method,LinkedHashMap<String,String> hidden,LinkedHashMap<String,NativeInputBinding> bindings,String label){
        LinkedHashMap<String,String> data=new LinkedHashMap<>(hidden);for(NativeInputBinding b:bindings.values()){if("checkbox".equals(b.kind)){if(((Switch)b.view).isChecked())data.put(b.name,"1");}else if("select".equals(b.kind)){Spinner sp=(Spinner)b.view;int pos=sp.getSelectedItemPosition();String val=b.values!=null&&pos>=0&&pos<b.values.size()?b.values.get(pos):String.valueOf(sp.getSelectedItem());data.put(b.name,val);}else data.put(b.name,((EditText)b.view).getText().toString());}
        if("get".equals(method)){StringBuilder q=new StringBuilder();try{for(Map.Entry<String,String> e:data.entrySet()){if(q.length()>0)q.append('&');q.append(java.net.URLEncoder.encode(e.getKey(),"UTF-8")).append('=').append(java.net.URLEncoder.encode(e.getValue()==null?"":e.getValue(),"UTF-8"));}}catch(Exception ignored){}openPath(action+(action.contains("?")?"&":"?")+q,current.title);return;}
        topAction.setEnabled(false);runNet(()->client.postForm(action,data),r->{topAction.setEnabled(true);if(r.code>=200&&r.code<400){showNativeNotice("Tersimpan",label+" berhasil.",success);if("settings-detail".equals(current.key))showSettingsDetail(current.arg,current.title);else if("shop-detail".equals(current.key))showShopDetail(current.arg,current.title);else showNativeModule(current.arg,current.title,false);}else showNativeNotice("Belum tersimpan",htmlError(r.body,r.url,"Coba lagi."),danger);},e->{topAction.setEnabled(true);showNativeNotice("Gagal",e.getMessage(),danger);});
    }

    private void performShopAction(Element e){String rawAction=e.attr("data-action");final String action=rawAction.isEmpty()?"buy":rawAction;String code=e.attr("data-code");String tier=e.attr("data-tier");String slot=e.attr("data-slot");runNet(()->{JSONObject j=new JSONObject();j.put("csrf",client.ensureCsrf());j.put("action",action);if(!code.isEmpty())j.put("code",code);if(!tier.isEmpty())j.put("tier",tier);if(!slot.isEmpty())j.put("slot",slot);return client.postJson("/api/shop_buy.php",j.toString());},r->{if(r.ok()){showNativeNotice("Berhasil",jsonError(r.body,"Toko diperbarui."),success);showShopDetail(current.arg,current.title);}else showNativeNotice("Belum berhasil",jsonError(r.body,"Aksi toko gagal."),danger);},x->showNativeNotice("Gagal",x.getMessage(),danger));}

    private void performVirtualAction(Element e){String action=e.hasAttr("data-virtual-action")?e.attr("data-virtual-action"):e.attr("data-action");String code=e.attr("data-code");String slot=e.attr("data-slot");runNet(()->{JSONObject j=new JSONObject();j.put("csrf",client.ensureCsrf());j.put("action",action);j.put("code",code);if(!slot.isEmpty())j.put("slot",slot);return client.postJson("/api/virtual_item.php",j.toString());},r->{if(r.ok()){showNativeNotice("Berhasil",jsonError(r.body,"Item virtual diperbarui."),success);showShopDetail(current.arg,current.title);}else showNativeNotice("Belum berhasil",jsonError(r.body,"Aksi item virtual gagal."),danger);},x->showNativeNotice("Gagal",x.getMessage(),danger));}

    private boolean hasSelectedAncestor(Element e,Elements set){Element p=e.parent();while(p!=null){if(set.contains(p))return true;p=p.parent();}return false;}

    private void openPath(String raw,String title){
        String direct=raw==null?"":raw.trim();if(direct.isEmpty())return;
        if(direct.startsWith("native:")){String n=direct.substring(7);if("profile-settings".equals(n))navigate(new Screen("profile-settings","","Pengaturan Profil"),true);else if("profile-media".equals(n))navigate(new Screen("profile-media","","Foto Profil & Sampul"),true);else if("profile-info".equals(n))navigate(new Screen("settings-detail","/settings.php?tab=profile&native_section=info","Info Profil"),true);else if("username".equals(n))navigate(new Screen("settings-detail","/settings.php?tab=profile&native_section=username","Username"),true);else if("server".equals(n))showServerSetup(true);return;}
        String path=relative(direct);if(path.isEmpty())return;
        if(path.startsWith("http://")||path.startsWith("https://")){if(!path.startsWith(client.baseUrl())){openExternal(path);return;}path=relative(path);}
        if(path.contains("index.php")){goRoot("home","","Beranda");return;}
        if(path.contains("messages.php")){int c=queryInt(path,"c");if(c>0)navigate(new Screen("chat",String.valueOf(c),title),true);else navigate(new Screen("messages","","Chat"),true);return;}
        if(path.contains("notifications.php")){navigate(new Screen("notifications","","Notifikasi"),true);return;}
        if(path.contains("profile.php")){navigate(new Screen("profile",path,"Profil"),true);return;}
        if(path.contains("post.php")){int id=queryInt(path,"id");if(id>0){navigate(new Screen("post",String.valueOf(id),"Postingan"),true);return;}}
        if(path.contains("shop.php")){String tab=query(path,"tab");if(tab.isEmpty()){navigate(new Screen("shop","","Toko & Dompet"),true);return;}String st=shopTitle(tab);navigate(new Screen("shop-detail",path,st),true);return;}
        if(path.contains("topup.php")||path.contains("settings.php?tab=topup")){navigate(new Screen("topup","","Top Up"),true);return;}
        if(path.contains("settings.php")){String tab=query(path,"tab");if(tab.isEmpty()){navigate(new Screen("settings","","Pengaturan"),true);return;}navigate(new Screen("settings-detail",path,settingsTitle(tab,title)),true);return;}
        if(path.contains("explore.php")){navigate(new Screen("search","","Cari"),true);return;}
        navigate(new Screen("module",path,title==null||title.isEmpty()?routeTitle(path):title),true);
    }

    private String shopTitle(String tab){switch(tab){case"wallet":return"Dompet";case"kirim":case"send":return"Kirim Koin";case"promo":return"Kode Promo";case"etalase":case"shop":return"Etalase";case"wishlist":return"Keinginan";case"pet":case"pets":return"Pet";case"virtual":return"Item Virtual";case"vip":return"VIP";case"gifts":return"Hadiah";case"theme":return"Tema";case"frame":return"Bingkai";case"bubble":return"Gelembung";case"sticker":return"Stiker";case"effect":return"Efek Nama";case"ticket":return"Tiket";case"bag":return"Tas Barang";case"koleksi":return"Koleksiku";case"level":return"Level";default:return"Toko & Koleksi";}}
    private String settingsTitle(String tab,String fallback){switch(tab){case"profile":return fallback!=null&&!fallback.isEmpty()?fallback:"Profil";case"appearance":return"Mode Tampilan";case"experience":return"Pengalaman Aplikasi";case"language":return"Bahasa & Terjemahan";case"access":return"Aksesibilitas";case"notifications":return"Pengaturan Notifikasi";case"privacy":return"Privasi & Keamanan";case"ai":return"Pengaturan AI";case"characters":return"Karakter";case"premium":return"Premium";case"focus":return"Mode Fokus";case"blocked":return"Akun Diblokir";case"data":return"Data Akun";default:return fallback==null||fallback.isEmpty()?"Pengaturan":fallback;}}
    private String routeTitle(String path){if(path.contains("reels"))return"Video Pendek";if(path.contains("live"))return"Live";if(path.contains("communit"))return"Komunitas";if(path.contains("bookmarks"))return"Tersimpan";if(path.contains("memories"))return"Memori";if(path.contains("games"))return"Games";if(path.contains("character"))return"Karakter";if(path.contains("ai.php")||path.contains("deapp-ai"))return"DeApp AI";if(path.contains("developer"))return"Developer";if(path.contains("ads"))return"Iklan";if(path.contains("help"))return"Bantuan";if(path.contains("privacy"))return"Privasi";if(path.contains("policy"))return"Kebijakan";return"DeApp";}

    private void logout(){runNet(()->client.get("/api/logout.php"),r->{client.clearSession();ownProfilePath="";showLogin();},e->{client.clearSession();showLogin();});}

    private void showNativeNotice(String title,String message,int toneColor){
        if(root==null)return;FrameLayout overlay=new FrameLayout(this);overlay.setClickable(false);LinearLayout card=new LinearLayout(this);card.setGravity(Gravity.CENTER_VERTICAL);card.setPadding(dp(14),dp(11),dp(14),dp(11));card.setBackground(outlined(surface,border,16));View dot=new View(this);dot.setBackground(rounded(toneColor,99));card.addView(dot,new LinearLayout.LayoutParams(dp(9),dp(9)));LinearLayout words=column();words.setBackgroundColor(Color.TRANSPARENT);words.addView(bold(title,13.5f));TextView msg=tv(message==null?"":message,12.5f,muted);words.addView(msg);LinearLayout.LayoutParams wp=new LinearLayout.LayoutParams(0,-2,1);wp.leftMargin=dp(10);card.addView(words,wp);FrameLayout.LayoutParams cp=new FrameLayout.LayoutParams(-1,-2,Gravity.CENTER);cp.leftMargin=dp(24);cp.rightMargin=dp(24);overlay.addView(card,cp);root.addView(overlay,new FrameLayout.LayoutParams(-1,-1));card.setAlpha(0f);card.setScaleX(.96f);card.setScaleY(.96f);card.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(140).start();main.postDelayed(()->{card.animate().alpha(0f).scaleX(.98f).scaleY(.98f).setDuration(160).withEndAction(()->root.removeView(overlay)).start();},2200);
    }

    private void showNetworkError(String text,Runnable retry){LinearLayout l=column();l.setGravity(Gravity.CENTER);l.setPadding(dp(28),dp(28),dp(28),dp(28));TextView h=bold("Server DeApp belum terhubung",20);h.setGravity(Gravity.CENTER);l.addView(h);TextView b=tv(text+"\n\n"+connectionHint(),14,muted);b.setGravity(Gravity.CENTER);LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,-2);bp.topMargin=dp(8);l.addView(b,bp);Button r=primary("Coba lagi");LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(50));rp.topMargin=dp(20);l.addView(r,rp);r.setOnClickListener(v->retry.run());Button server=secondaryButton("Atur server DeApp");LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(48));sp.topMargin=dp(9);l.addView(server,sp);server.setOnClickListener(v->showServerSetup(true));swap(l);}
    private void renderInlineError(LinearLayout host,String text,Runnable retry){host.removeAllViews();host.addView(emptyState("Belum dapat dimuat",text));Button b=primary("Coba lagi");LinearLayout.LayoutParams p=cardMargin();host.addView(b,p);b.setOnClickListener(v->retry.run());}
    private View emptyState(String title,String body){LinearLayout l=column();l.setGravity(Gravity.CENTER);l.setPadding(dp(24),dp(36),dp(24),dp(36));TextView h=bold(title,17);h.setGravity(Gravity.CENTER);TextView b=tv(body,13.5f,muted);b.setGravity(Gravity.CENTER);LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,-2);bp.topMargin=dp(8);l.addView(h);l.addView(b,bp);return l;}

    private ImageView avatarView(int size){ImageView i=new ImageView(this);i.setLayoutParams(new LinearLayout.LayoutParams(dp(size),dp(size)));i.setScaleType(ImageView.ScaleType.CENTER_CROP);i.setBackground(rounded(surface2,99));i.setClipToOutline(true);i.setOutlineProvider(new ViewOutlineProvider(){@Override public void getOutline(View view,Outline o){o.setOval(0,0,view.getWidth(),view.getHeight());}});i.setImageResource(R.drawable.ic_native_user);return i;}
    private ViewOutlineProvider roundOutline(int radius){return new ViewOutlineProvider(){@Override public void getOutline(View view,Outline o){o.setRoundRect(0,0,view.getWidth(),view.getHeight(),dp(radius));}};}
    private void loadImage(ImageView view,String url){if(url==null||url.isEmpty())return;runNet(()->client.getBytes(url),bytes->{if(bytes.length>0){try{view.setImageBitmap(BitmapFactory.decodeByteArray(bytes,0,bytes.length));}catch(Exception ignored){}}},e->{});}

    private String csrfFrom(String html,String url){Document d=Jsoup.parse(html==null?"":html,url);Element e=d.selectFirst("input[name=csrf]");return e==null?"":e.attr("value");}
    private String htmlError(String html,String url,String fallback){Document d=Jsoup.parse(html==null?"":html,url);Element e=d.selectFirst(".alert-error,.alert.alert-error,[role=alert]");return e==null||e.text().trim().isEmpty()?fallback:e.text().trim();}
    private String jsonError(String body,String fallback){try{JSONObject o=new JSONObject(body);String e=o.optString("error");if(!e.isEmpty())return e;JSONObject er=o.optJSONObject("error");if(er!=null&&!er.optString("message").isEmpty())return er.optString("message");String m=o.optString("message");return m.isEmpty()?fallback:m;}catch(Exception x){return fallback;}}
    private String textOf(Element root,String selector,String fallback){Element e=root.selectFirst(selector);return e==null||e.text().trim().isEmpty()?fallback:e.text().trim();}
    private String textOf(Document root,String selector,String fallback){Element e=root.selectFirst(selector);return e==null||e.text().trim().isEmpty()?fallback:e.text().trim();}
    private int parseInt(String s){try{return Integer.parseInt(s==null?"0":s.replaceAll("[^0-9-]",""));}catch(Exception e){return 0;}}
    private String usernameFromHref(String href){try{String rel=relative(href);String u=query(rel,"u");if(!u.isEmpty())return u;}catch(Exception ignored){}return "";}
    private int queryInt(String path,String key){return parseInt(query(path,key));}
    private String query(String path,String key){try{String q=new URI(client.absolute(path)).getRawQuery();if(q==null)return"";for(String pair:q.split("&")){String[]kv=pair.split("=",2);if(URLDecoder.decode(kv[0],"UTF-8").equals(key))return kv.length>1?URLDecoder.decode(kv[1],"UTF-8"):"";}}catch(Exception ignored){}return"";}
    private String relative(String raw){if(raw==null)return"";String s=raw.trim();if(s.isEmpty())return"";if(client==null)return s;if(s.startsWith(client.baseUrl())){s=s.substring(client.baseUrl().length());if(s.isEmpty())s="/index.php";}if(!s.startsWith("/")&&!s.startsWith("http"))s="/"+s;return s;}

    private void handleShortcut(Intent intent){if(intent==null||intent.getData()==null)return;String d=intent.getData().toString();if(d.endsWith("/post"))navigate(new Screen("compose","","Postingan"),true);else if(d.endsWith("/chat"))goRoot("messages","","Chat");else if(d.endsWith("/video"))navigate(new Screen("module","/reels.php","Video Pendek"),true);else if(d.endsWith("/story"))navigate(new Screen("story-create","","Buat Cerita"),true);}

    private <T> void runNet(NetWork<T> work, NetDone<T> ok, NetDone<Exception> fail){io.execute(()->{try{T v=work.run();main.post(()->{if(!isFinishing())ok.done(v);});}catch(Exception e){main.post(()->{if(!isFinishing())fail.done(e);});}});}
}
