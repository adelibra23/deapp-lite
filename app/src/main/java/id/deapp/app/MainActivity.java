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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
        baseUrl = normalizeBase(prefs.getString(KEY_SERVER, ""));
        if (baseUrl.isEmpty()) {
            String legacy = normalizeBase(getSharedPreferences("deapp_lite", MODE_PRIVATE).getString(KEY_SERVER, ""));
            if (!legacy.isEmpty()) {
                baseUrl = legacy;
                prefs.edit().putString(KEY_SERVER, legacy).apply();
            }
        }
        tone = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 30);
        if (Build.VERSION.SDK_INT >= 33) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT, this::handleBack);
        }
        if (baseUrl.isEmpty()) showServerSetup();
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
        View boot = centeredMessage("DeApp", "Menyiapkan sesi…");
        configureChrome("DeApp", false, 0, "", null); showBottom(false); swap(boot);
        runNet(() -> client.get("/index.php"), r -> {
            if (looksLoggedOut(r)) showLogin();
            else {
                discoverOwnProfile(r.body, r.url);
                goRoot("home", "", "Beranda");
                handleShortcut(getIntent());
            }
        }, e -> showNetworkError("Tidak dapat terhubung ke server DeApp.", this::validateSession));
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

    private void showServerSetup() {
        root = new FrameLayout(this); root.setBackgroundColor(bg);
        ScrollView sc = new ScrollView(this); LinearLayout box = column();
        box.setPadding(dp(24), dp(48), dp(24), dp(28)); sc.addView(box);
        TextView logo = bold("DeApp", 32); box.addView(logo);
        TextView sub = tv("Hubungkan aplikasi native ke server DeApp milikmu.", 15, muted);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2); sp.topMargin = dp(8); box.addView(sub, sp);
        EditText server = field("https://domain.com/deapp");
        server.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(-1, dp(54)); fp.topMargin = dp(28); box.addView(server, fp);
        Button save = primary("Hubungkan"); LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, dp(52)); bp.topMargin = dp(14); box.addView(save, bp);
        TextView note = tv("UI Android versi 2.0 tidak memakai WebView. Server PHP/MySQL tetap menjadi backend DeApp.", 13, muted);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(-1, -2); np.topMargin = dp(16); box.addView(note, np);
        save.setOnClickListener(v -> {
            String value = normalizeBase(server.getText().toString());
            if (!(value.startsWith("http://") || value.startsWith("https://"))) {
                server.setError("Masukkan alamat server lengkap."); return;
            }
            prefs.edit().putString(KEY_SERVER, value).apply(); baseUrl = value;
            client = new DeappClient(baseUrl, prefs); buildShell(); validateSession();
        });
        root.addView(sc, new FrameLayout.LayoutParams(-1, -1)); setContentView(root);
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
            case "post": showPost(parseInt(s.arg)); break;
            case "search": showSearch(); break;
            case "shop": showShop(); break;
            case "settings": showSettings(); break;
            case "features": showAllFeatures(); break;
            case "generic": showGenericNativePage(s.arg,s.title); break;
            default: showHome();
        }
    }

    private void handleBack(){
        if("login".equals(current.key)){finish();return;}
        if("register".equals(current.key)){showLogin();return;}
        if(!history.isEmpty()){Screen s=history.pop();navigate(s,false);return;}
        if(!"home".equals(current.key)){goRoot("home","","Beranda");return;}
        finish();
    }

    private void showHome() {
        configureChrome("DeApp",false,R.drawable.ic_native_search,"Cari",v->navigate(new Screen("search","","Cari"),true));showBottom(true);
        SwipeRefreshLayout refresh=new SwipeRefreshLayout(this);refresh.setColorSchemeColors(text);refresh.setProgressBackgroundColorSchemeColor(surface);ScrollView sc=new ScrollView(this);LinearLayout feed=column();feed.setPadding(0,dp(8),0,dp(18));sc.addView(feed);refresh.addView(sc);swap(refresh);
        LinearLayout composer=rowCard();TextView avatar=bold("+",22);avatar.setGravity(Gravity.CENTER);avatar.setBackground(rounded(surface2,99));composer.addView(avatar,new LinearLayout.LayoutParams(dp(42),dp(42)));TextView ask=tv("Apa yang baru?",15,muted);ask.setGravity(Gravity.CENTER_VERTICAL);LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(0,dp(42),1);ap.leftMargin=dp(12);composer.addView(ask,ap);composer.setOnClickListener(v->navigate(new Screen("compose","","Postingan"),true));feed.addView(composer,cardMargin());
        TextView loading=tv("Memuat kiriman…",13,muted);loading.setGravity(Gravity.CENTER);loading.setPadding(0,dp(24),0,dp(24));feed.addView(loading);
        Runnable load=()->runNet(()->client.get("/api/feed.php?scope=feed&offset=0"),r->{refresh.setRefreshing(false);renderFeed(feed,r.body,r.url);},e->{refresh.setRefreshing(false);renderInlineError(feed,"Feed belum dapat dimuat.",()->showHome());});
        refresh.setOnRefreshListener(load);load.run();
    }

    private void renderFeed(LinearLayout feed,String html,String url){
        while(feed.getChildCount()>1)feed.removeViewAt(1);
        Document d=Jsoup.parse(html,url);Elements cards=d.select("article.post-card");
        if(cards.isEmpty()){TextView empty=tv("Belum ada kiriman untuk ditampilkan.",14,muted);empty.setGravity(Gravity.CENTER);empty.setPadding(dp(20),dp(34),dp(20),dp(34));feed.addView(empty);return;}
        int count=0;for(Element e:cards){if(count++>=30)break;Post p=parsePost(e);feed.addView(postCard(p),cardMargin());}
    }

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

    private void showPost(int id){configureChrome("Postingan",true,0,"",null);showBottom(false);LinearLayout host=column();ScrollView sc=new ScrollView(this);sc.addView(host);swap(sc);runNet(()->client.get("/post.php?id="+id),r->{host.removeAllViews();Document d=Jsoup.parse(r.body,r.url);Element card=d.selectFirst("article.post-card");if(card!=null)host.addView(postCard(parsePost(card)),cardMargin());loadComments(host,id);},e->renderInlineError(host,"Postingan tidak dapat dibuka.",()->showPost(id)));}
    private void loadComments(LinearLayout host,int id){runNet(()->client.get("/api/post_comments.php?post_id="+id),r->{try{JSONObject o=new JSONObject(r.body);JSONArray a=o.optJSONArray("comments");TextView h=bold("Komentar",16);h.setPadding(dp(16),dp(20),dp(16),dp(8));host.addView(h);if(a!=null)for(int i=0;i<a.length();i++){JSONObject c=a.optJSONObject(i);LinearLayout row=column();row.setPadding(dp(16),dp(10),dp(16),dp(10));String name=c.optString("name",c.optString("username","Pengguna"));row.addView(bold(name,13.5f));row.addView(tv(c.optString("body",c.optString("comment","")),14.5f,text));host.addView(row);}}catch(Exception ignored){}},e->{});}

    private void showMessages(){
        configureChrome("Chat",false,R.drawable.ic_native_search,"Cari orang",v->navigate(new Screen("search","","Cari"),true));showBottom(true);LinearLayout list=column();ScrollView sc=new ScrollView(this);sc.addView(list);swap(sc);runNet(()->client.get("/messages.php"),r->{list.removeAllViews();Document d=Jsoup.parse(r.body,r.url);Elements threads=d.select("a.thread");if(threads.isEmpty()){list.addView(emptyState("Belum ada percakapan","Mulai percakapan dari profil seseorang."));return;}for(Element e:threads){String href=relative(e.absUrl("href"));int cid=queryInt(href,"c");String name=textOf(e,".thread-top b","Percakapan");String prev=textOf(e,".thread-preview","");String time=textOf(e,".thread-time","");String av="";Element im=e.selectFirst(".thread-avatar img");if(im!=null)av=im.absUrl("src");String finalAv=av;LinearLayout row=rowCard();ImageView iv=avatarView(48);loadImage(iv,finalAv);row.addView(iv);LinearLayout meta=column();meta.setBackgroundColor(Color.TRANSPARENT);LinearLayout nameRow=new LinearLayout(this);TextView n=bold(name,14.5f);TextView tm=tv(time,11.5f,muted);nameRow.addView(n,new LinearLayout.LayoutParams(0,-2,1));nameRow.addView(tm);meta.addView(nameRow);meta.addView(tv(prev,13.5f,muted));LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(0,-2,1);mp.leftMargin=dp(12);row.addView(meta,mp);row.setOnClickListener(v->navigate(new Screen("chat",String.valueOf(cid),name),true));list.addView(row,cardMargin());}},e->renderInlineError(list,"Daftar chat belum dapat dimuat.",this::showMessages));
    }

    private void showChat(int convoId){
        configureChrome(current.title.isEmpty()?"Chat":current.title,true,0,"",null);showBottom(false);LinearLayout screen=column();FrameLayout messagesFrame=new FrameLayout(this);ScrollView sc=new ScrollView(this);LinearLayout msgs=column();msgs.setPadding(dp(10),dp(10),dp(10),dp(12));sc.addView(msgs);messagesFrame.addView(sc);screen.addView(messagesFrame,new LinearLayout.LayoutParams(-1,0,1));LinearLayout composer=new LinearLayout(this);composer.setGravity(Gravity.BOTTOM|Gravity.CENTER_VERTICAL);composer.setPadding(dp(8),dp(6),dp(8),dp(8));composer.setBackgroundColor(surface);ImageButton media=iconButton(R.drawable.ic_native_photo,"Media");ImageButton emoji=iconButton(R.drawable.ic_native_sparkles,"Emoji");ImageButton mic=iconButton(R.drawable.ic_native_mic,"Voice note");EditText input=field("Pesan");input.setSingleLine(false);input.setMaxLines(5);input.setMinHeight(dp(46));ImageButton send=iconButton(R.drawable.ic_native_send,"Kirim");send.setColorFilter(accent);composer.addView(media);composer.addView(emoji);composer.addView(mic);LinearLayout.LayoutParams inp=new LinearLayout.LayoutParams(0,-2,1);inp.leftMargin=dp(4);inp.rightMargin=dp(4);composer.addView(input,inp);composer.addView(send);screen.addView(composer,new LinearLayout.LayoutParams(-1,-2));swap(screen);
        Runnable load=()->runNet(()->client.get("/api/message_fetch.php?convo_id="+convoId),r->{renderMessages(msgs,r.body);main.postDelayed(()->sc.fullScroll(View.FOCUS_DOWN),80);},e->showNativeNotice("Chat gagal dimuat",e.getMessage(),danger));load.run();
        input.setOnEditorActionListener((v,action,event)->{if(action==EditorInfo.IME_ACTION_SEND){send.performClick();return true;}return false;});
        send.setOnClickListener(v->{String b=input.getText().toString().trim();if(b.isEmpty())return;send.setEnabled(false);runNet(()->{String csrf=client.ensureCsrf();JSONObject j=new JSONObject();j.put("csrf",csrf);j.put("convo_id",convoId);j.put("body",b);return client.postJson("/api/message_send.php",j.toString());},r->{send.setEnabled(true);if(r.ok()){input.setText("");tone.startTone(ToneGenerator.TONE_PROP_ACK,55);load.run();}else showNativeNotice("Pesan belum terkirim",jsonError(r.body,"Coba lagi."),danger);},e->{send.setEnabled(true);showNativeNotice("Gagal",e.getMessage(),danger);});});
        media.setOnClickListener(v->showNativeNotice("Media","Pengunggahan media native disiapkan pada modul Media Picker.",accent));emoji.setOnClickListener(v->input.append("🙂"));mic.setOnClickListener(v->showNativeNotice("Voice note","Perekam native akan menggunakan izin mikrofon perangkat.",accent));
    }

    private void renderMessages(LinearLayout msgs,String body){msgs.removeAllViews();try{JSONObject o=new JSONObject(body);JSONObject partner=o.optJSONObject("partner");if(partner!=null&&!partner.optString("name").isEmpty())topTitle.setText(partner.optString("name"));JSONArray a=o.optJSONArray("messages");if(a==null)return;for(int i=0;i<a.length();i++){JSONObject m=a.optJSONObject(i);boolean mine=m.optBoolean("mine");TextView b=tv(m.optString("body",m.optString("sticker","")),14.5f,text);b.setPadding(dp(12),dp(9),dp(12),dp(9));b.setBackground(rounded(mine?(dark?Color.parseColor("#214667"):Color.parseColor("#DFF2FF")):surface2,16));LinearLayout line=new LinearLayout(this);line.setGravity(mine?Gravity.RIGHT:Gravity.LEFT);line.addView(b,new LinearLayout.LayoutParams(-2,-2));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(5);msgs.addView(line,lp);TextView tm=tv(m.optString("time",""),10.5f,muted);tm.setGravity(mine?Gravity.RIGHT:Gravity.LEFT);msgs.addView(tm);}}catch(Exception e){msgs.addView(tv("Pesan belum dapat ditampilkan.",13,muted));}}

    private void showNotifications(){configureChrome("Notifikasi",false,R.drawable.ic_native_settings,"Pengaturan notifikasi",v->navigate(new Screen("generic","/settings.php?tab=notifications","Pengaturan Notifikasi"),true));showBottom(true);LinearLayout list=column();ScrollView sc=new ScrollView(this);sc.addView(list);swap(sc);runNet(()->client.get("/api/notif_panel.php"),r->{list.removeAllViews();try{JSONObject o=new JSONObject(r.body);JSONArray a=o.optJSONArray("items");if(a==null||a.length()==0){list.addView(emptyState("Belum ada notifikasi","Aktivitas baru akan muncul di sini."));return;}for(int i=0;i<a.length();i++){JSONObject n=a.optJSONObject(i);LinearLayout row=rowCard();String av=n.optString("avatar");ImageView iv=avatarView(44);if(!av.isEmpty())loadImage(iv,av);else iv.setImageResource(R.drawable.ic_native_bell);row.addView(iv);LinearLayout meta=column();meta.setBackgroundColor(Color.TRANSPARENT);TextView msg=tv(n.optString("message","Notifikasi"),14.5f,text);TextView tm=tv((n.optBoolean("unread")?"Belum dibaca · ":"")+n.optString("time",""),11.5f,n.optBoolean("unread")?accent:muted);meta.addView(msg);meta.addView(tm);LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(0,-2,1);mp.leftMargin=dp(12);row.addView(meta,mp);String link=n.optString("link","");row.setOnClickListener(v->openPath(link,"Notifikasi"));list.addView(row,cardMargin());}}catch(Exception x){renderInlineError(list,"Notifikasi belum dapat dibaca.",this::showNotifications);}},e->renderInlineError(list,"Notifikasi belum dapat dimuat.",this::showNotifications));}

    private void showProfile(String path){configureChrome("Profil",false,R.drawable.ic_native_more_vertical,"Menu",v->navigate(new Screen("features","","Semua Fitur"),true));showBottom(true);LinearLayout host=column();ScrollView sc=new ScrollView(this);sc.addView(host);swap(sc);String target=path;if(target==null||target.isEmpty()){runNet(()->client.get("/index.php"),r->{discoverOwnProfile(r.body,r.url);showProfile(ownProfilePath);},e->renderInlineError(host,"Profil belum dapat dibuka.",()->showProfile("")));return;}runNet(()->client.get(target),r->{host.removeAllViews();Document d=Jsoup.parse(r.body,r.url);Element cover=d.selectFirst(".profile-cover");if(cover!=null){ImageView cv=new ImageView(this);cv.setScaleType(ImageView.ScaleType.CENTER_CROP);Element cimg=cover.selectFirst("img");if(cimg!=null)loadImage(cv,cimg.absUrl("src"));else cv.setBackgroundColor(surface2);host.addView(cv,new LinearLayout.LayoutParams(-1,dp(150)));}LinearLayout info=column();info.setPadding(dp(16),dp(12),dp(16),dp(18));Element av=d.selectFirst("img.profile-avatar");ImageView ava=avatarView(86);if(av!=null)loadImage(ava,av.absUrl("src"));info.addView(ava);String name=textOf(d,".profile-name","Profil");info.addView(bold(name,22));String un=textOf(d,".profile-username","");info.addView(tv(un,14,muted));String bio=textOf(d,".profile-bio","");if(!bio.isEmpty()){TextView bv=tv(bio,14.5f,text);LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,-2);bp.topMargin=dp(12);info.addView(bv,bp);}Element stats=d.selectFirst(".profile-stats");if(stats!=null){TextView sv=tv(stats.text(),13,muted);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);sp.topMargin=dp(14);info.addView(sv,sp);}host.addView(info);Elements posts=d.select("article.post-card");for(Element e:posts)host.addView(postCard(parsePost(e)),cardMargin());},e->renderInlineError(host,"Profil belum dapat dimuat.",()->showProfile(target)));}

    private void showSearch(){configureChrome("Cari",true,0,"",null);showBottom(false);LinearLayout box=column();box.setPadding(dp(12),dp(10),dp(12),dp(20));EditText q=field("Cari orang atau tagar");box.addView(q,new LinearLayout.LayoutParams(-1,dp(52)));LinearLayout results=column();LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,0,1);rp.topMargin=dp(8);box.addView(results,rp);swap(box);q.setSingleLine(true);q.setImeOptions(EditorInfo.IME_ACTION_SEARCH);q.setOnEditorActionListener((v,a,e)->{if(a==EditorInfo.IME_ACTION_SEARCH){searchNow(q.getText().toString(),results);return true;}return false;});}
    private void searchNow(String query,LinearLayout results){String q=query.trim();if(q.isEmpty())return;runNet(()->client.get("/api/search_suggest.php?q="+java.net.URLEncoder.encode(q,"UTF-8")),r->{results.removeAllViews();try{JSONObject o=new JSONObject(r.body);JSONArray users=o.optJSONArray("users");if(users!=null)for(int i=0;i<users.length();i++){JSONObject u=users.optJSONObject(i);LinearLayout row=rowCard();ImageView av=avatarView(42);loadImage(av,u.optString("avatar"));row.addView(av);LinearLayout m=column();m.setBackgroundColor(Color.TRANSPARENT);m.addView(bold(u.optString("name","Pengguna"),14.5f));m.addView(tv("@"+u.optString("username"),12,muted));LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(0,-2,1);mp.leftMargin=dp(10);row.addView(m,mp);String path=relative(u.optString("url"));row.setOnClickListener(v->navigate(new Screen("profile",path,"Profil"),true));results.addView(row,cardMargin());}JSONArray tags=o.optJSONArray("tags");if(tags!=null)for(int i=0;i<tags.length();i++){JSONObject t=tags.optJSONObject(i);TextView tr=tv("#"+t.optString("tag")+"   "+t.optString("count"),15,accent);tr.setPadding(dp(16),dp(14),dp(16),dp(14));String path=relative(t.optString("url"));tr.setOnClickListener(v->navigate(new Screen("generic",path,"Tagar"),true));results.addView(tr);}}catch(Exception ex){results.addView(tv("Pencarian belum dapat ditampilkan.",13,muted));}},e->showNativeNotice("Pencarian gagal",e.getMessage(),danger));}

    private void showShop(){configureChrome("Toko & Dompet",true,R.drawable.ic_native_more,"Semua fitur",v->navigate(new Screen("features","","Semua Fitur"),true));showBottom(false);ScrollView sc=new ScrollView(this);LinearLayout box=column();box.setPadding(dp(12),dp(12),dp(12),dp(24));sc.addView(box);box.addView(sectionTitle("Dompet & Koin"));addMenu(box,R.drawable.ic_native_shop,"Dompet","Saldo, transaksi, dan keamanan dompet","/shop.php?tab=wallet","Dompet");addMenu(box,R.drawable.ic_native_add,"Top Up","Isi saldo koin DeApp","/settings.php?tab=topup","Top Up");addMenu(box,R.drawable.ic_native_send,"Kirim Koin","Kirim koin ke pengguna lain","/shop.php?tab=send","Kirim Koin");addMenu(box,R.drawable.ic_native_code,"Kode Promo","Tukarkan kode promo dan hadiah","/shop.php?tab=promo","Kode Promo");box.addView(sectionTitle("Toko & Koleksi"));addMenu(box,R.drawable.ic_native_grid,"Etalase","Jelajahi item dan koleksi","/shop.php?tab=shop","Etalase");addMenu(box,R.drawable.ic_native_sparkles,"Pet","Peliharaan virtual DeApp","/shop.php?tab=pets","Pet");addMenu(box,R.drawable.ic_native_game,"Item Virtual","Aksesori dan item koleksi","/shop.php?tab=virtual","Item Virtual");addMenu(box,R.drawable.ic_native_more,"Menu lainnya","Hadiah, stiker, tiket, koleksi, level, dan lainnya","/shop.php?tab=more","Menu lainnya");swap(sc);}

    private void showSettings(){configureChrome("Pengaturan",true,0,"",null);showBottom(false);ScrollView sc=new ScrollView(this);LinearLayout box=column();box.setPadding(dp(12),dp(12),dp(12),dp(24));sc.addView(box);addMenu(box,R.drawable.ic_native_user,"Profil","Foto, info profil, username","/settings.php?tab=profile","Profil");addMenu(box,R.drawable.ic_native_shield,"Privasi & Keamanan","Kontrol akun, sesi, blokir","/settings.php?tab=privacy","Privasi & Keamanan");addMenu(box,R.drawable.ic_native_sparkles,"Mode Tampilan","Tema dan pengalaman aplikasi","/settings.php?tab=appearance","Mode Tampilan");addMenu(box,R.drawable.ic_native_message,"Bahasa & Terjemahan","Bahasa, terjemahan, aksesibilitas","/settings.php?tab=language","Bahasa & Terjemahan");addMenu(box,R.drawable.ic_native_bell,"Notifikasi","Atur notifikasi dan suara","/settings.php?tab=notifications","Notifikasi");TextView logout=tv("Keluar dari akun",15,danger);logout.setGravity(Gravity.CENTER);logout.setPadding(dp(14),dp(16),dp(14),dp(16));logout.setBackground(outlined(surface,border,14));LinearLayout.LayoutParams lp=cardMargin();lp.topMargin=dp(24);box.addView(logout,lp);logout.setOnClickListener(v->logout());swap(sc);}

    private void showAllFeatures(){configureChrome("Semua Fitur",true,0,"",null);showBottom(false);ScrollView sc=new ScrollView(this);LinearLayout box=column();box.setPadding(dp(12),dp(12),dp(12),dp(24));sc.addView(box);String[][] items={{"Cari","Temukan orang, tagar, dan konten","/explore.php"},{"Video Pendek","Video vertikal dan kreator","/reels.php"},{"Live","Siaran langsung DeApp","/live.php"},{"Komunitas","Grup dan forum topik","/communities.php"},{"ASK","Pertanyaan dan jawaban","/inbox.php"},{"Tersimpan","Bookmark dan koleksi","/bookmarks.php"},{"Memori","Postingan yang kamu ingat","/memories.php"},{"Toko & Dompet","Koin, pet, item, koleksi","/shop.php"},{"DeApp AI","Asisten AI DeApp","/deapp-ai.php"},{"Games","Mini game sosial","/games.php"},{"Pengaturan","Akun dan pengalaman aplikasi","/settings.php"}};int[] icons={R.drawable.ic_native_search,R.drawable.ic_native_video,R.drawable.ic_native_live,R.drawable.ic_native_community,R.drawable.ic_native_message,R.drawable.ic_native_bookmark,R.drawable.ic_native_sparkles,R.drawable.ic_native_shop,R.drawable.ic_native_sparkles,R.drawable.ic_native_game,R.drawable.ic_native_settings};for(int i=0;i<items.length;i++){String[] it=items[i];addMenu(box,icons[i],it[0],it[1],it[2],it[0]);}swap(sc);}

    private TextView sectionTitle(String s){TextView h=bold(s,13);h.setTextColor(muted);h.setPadding(dp(8),dp(18),dp(8),dp(8));return h;}
    private void addMenu(LinearLayout box,int icon,String title,String subtitle,String path,String pageTitle){LinearLayout row=rowCard();ImageView i=new ImageView(this);i.setImageResource(icon);i.setColorFilter(text);i.setPadding(dp(8),dp(8),dp(8),dp(8));i.setBackground(rounded(surface2,12));row.addView(i,new LinearLayout.LayoutParams(dp(44),dp(44)));LinearLayout m=column();m.setBackgroundColor(Color.TRANSPARENT);m.addView(bold(title,14.5f));m.addView(tv(subtitle,12.3f,muted));LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(0,-2,1);mp.leftMargin=dp(12);row.addView(m,mp);TextView arrow=tv("›",26,muted);row.addView(arrow);row.setOnClickListener(v->{if("/shop.php".equals(path))navigate(new Screen("shop","","Toko & Dompet"),true);else if("/settings.php".equals(path))navigate(new Screen("settings","","Pengaturan"),true);else openPath(path,pageTitle);});box.addView(row,cardMargin());}

    private void showGenericNativePage(String path,String fallbackTitle){
        configureChrome(fallbackTitle.isEmpty()?"DeApp":fallbackTitle,true,0,"",null);showBottom(false);
        // Keep the previous page visible until data is ready; then swap in one short native transition.
        runNet(()->client.get(path),r->{Document d=Jsoup.parse(r.body,r.url);String title=fallbackTitle;Element h=d.selectFirst("main h1, main h2, .page-title, h1");if(h!=null&&!h.text().isEmpty())title=h.text();configureChrome(title,true,0,"",null);ScrollView sc=new ScrollView(this);LinearLayout box=column();box.setPadding(dp(14),dp(8),dp(14),dp(28));sc.addView(box);Element mainEl=d.selectFirst("main#main-content");if(mainEl==null)mainEl=d.body();renderNativeDocument(box,mainEl,r.url);swap(sc);},e->showNetworkError("Halaman belum dapat dimuat.",()->showGenericNativePage(path,fallbackTitle)));
    }

    private void renderNativeDocument(LinearLayout box,Element mainEl,String url){
        if(mainEl==null){box.addView(emptyState("Konten kosong","Tidak ada data untuk ditampilkan."));return;}
        Elements nodes=mainEl.select("h1,h2,h3,p,.alert,.empty-state,a.btn,a.dropdown-item,a.dev-app-row,a.settings-link,form");int rendered=0;
        for(Element e:nodes){if(rendered>90)break;if(hasSelectedAncestor(e,nodes))continue;String tag=e.tagName();if(tag.matches("h1|h2|h3")){TextView h=bold(e.text(),tag.equals("h1")?22:tag.equals("h2")?18:16);LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,-2);hp.topMargin=dp(16);box.addView(h,hp);rendered++;}else if(tag.equals("p")||e.hasClass("alert")||e.hasClass("empty-state")){String t=e.text().trim();if(t.isEmpty())continue;TextView p=tv(t,14,e.hasClass("alert")?text:muted);p.setPadding(dp(4),dp(6),dp(4),dp(6));box.addView(p);rendered++;}else if(tag.equals("a")){String href=relative(e.absUrl("href"));String label=e.text().trim();if(href.isEmpty()||label.isEmpty())continue;TextView a=tv(label,14.5f,accent);a.setPadding(dp(14),dp(14),dp(14),dp(14));a.setBackground(outlined(surface,border,14));LinearLayout.LayoutParams ap=cardMargin();box.addView(a,ap);a.setOnClickListener(v->openPath(href,label));rendered++;}else if(tag.equals("form")){renderSimpleForm(box,e,url);rendered++;}}
        if(rendered==0){String textBody=mainEl.text().trim();if(textBody.length()>1200)textBody=textBody.substring(0,1200)+"…";box.addView(tv(textBody.isEmpty()?"Halaman siap digunakan.":textBody,14.5f,text));}
    }

    private boolean hasSelectedAncestor(Element e,Elements set){Element p=e.parent();while(p!=null){if(set.contains(p))return true;p=p.parent();}return false;}

    private void renderSimpleForm(LinearLayout box,Element form,String pageUrl){
        Elements inputs=form.select("input:not([type=hidden]):not([type=submit]):not([type=file]), textarea, select");if(inputs.isEmpty())return;LinearLayout card=column();card.setPadding(dp(12),dp(10),dp(12),dp(12));card.setBackground(outlined(surface,border,16));LinkedHashMap<String,EditText> fields=new LinkedHashMap<>();LinkedHashMap<String,String> hidden=new LinkedHashMap<>();for(Element h:form.select("input[type=hidden][name]"))hidden.put(h.attr("name"),h.attr("value"));for(Element in:inputs){String name=in.attr("name");if(name.isEmpty())continue;String label=name.replace('_',' ');EditText f=field(label);String value=in.tagName().equals("textarea")?in.text():in.attr("value");f.setText(value);if(in.attr("type").equals("password"))f.setInputType(129);LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,dp(50));fp.topMargin=dp(8);card.addView(f,fp);fields.put(name,f);}String buttonText="Simpan";Element b=form.selectFirst("button[type=submit],button:not([type])");if(b!=null&&!b.text().trim().isEmpty())buttonText=b.text().trim();Button submit=primary(buttonText);LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(48));bp.topMargin=dp(10);card.addView(submit,bp);String action=form.absUrl("action");if(action.isEmpty())action=pageUrl;String finalAction=relative(action);submit.setOnClickListener(v->{submit.setEnabled(false);LinkedHashMap<String,String> data=new LinkedHashMap<>(hidden);for(Map.Entry<String,EditText> en:fields.entrySet())data.put(en.getKey(),en.getValue().getText().toString());runNet(()->client.postForm(finalAction,data),r->{submit.setEnabled(true);if(r.code>=200&&r.code<400){showNativeNotice("Tersimpan","Perubahan berhasil dikirim ke DeApp.",success);showGenericNativePage(finalAction,current.title);}else showNativeNotice("Belum tersimpan",htmlError(r.body,r.url,"Coba lagi."),danger);},e->{submit.setEnabled(true);showNativeNotice("Gagal",e.getMessage(),danger);});});box.addView(card,cardMargin());
    }

    private void openPath(String raw,String title){String path=relative(raw);if(path.isEmpty())return;if(path.contains("index.php")){goRoot("home","","Beranda");return;}if(path.contains("messages.php")){int c=queryInt(path,"c");if(c>0)navigate(new Screen("chat",String.valueOf(c),title),true);else navigate(new Screen("messages","","Chat"),true);return;}if(path.contains("notifications.php")){navigate(new Screen("notifications","","Notifikasi"),true);return;}if(path.contains("profile.php")){navigate(new Screen("profile",path,"Profil"),true);return;}if(path.contains("post.php")){int id=queryInt(path,"id");if(id>0){navigate(new Screen("post",String.valueOf(id),"Postingan"),true);return;}}if(path.contains("shop.php")&&!path.contains("tab=")){navigate(new Screen("shop","","Toko & Dompet"),true);return;}if(path.contains("settings.php")&&!path.contains("tab=")){navigate(new Screen("settings","","Pengaturan"),true);return;}if(path.contains("explore.php")){navigate(new Screen("search","","Cari"),true);return;}navigate(new Screen("generic",path,title),true);}

    private void logout(){runNet(()->client.get("/api/logout.php"),r->{client.clearSession();ownProfilePath="";showLogin();},e->{client.clearSession();showLogin();});}

    private void showNativeNotice(String title,String message,int toneColor){
        if(root==null)return;FrameLayout overlay=new FrameLayout(this);overlay.setClickable(false);LinearLayout card=new LinearLayout(this);card.setGravity(Gravity.CENTER_VERTICAL);card.setPadding(dp(14),dp(11),dp(14),dp(11));card.setBackground(outlined(surface,border,16));View dot=new View(this);dot.setBackground(rounded(toneColor,99));card.addView(dot,new LinearLayout.LayoutParams(dp(9),dp(9)));LinearLayout words=column();words.setBackgroundColor(Color.TRANSPARENT);words.addView(bold(title,13.5f));TextView msg=tv(message==null?"":message,12.5f,muted);words.addView(msg);LinearLayout.LayoutParams wp=new LinearLayout.LayoutParams(0,-2,1);wp.leftMargin=dp(10);card.addView(words,wp);FrameLayout.LayoutParams cp=new FrameLayout.LayoutParams(-1,-2,Gravity.CENTER);cp.leftMargin=dp(24);cp.rightMargin=dp(24);overlay.addView(card,cp);root.addView(overlay,new FrameLayout.LayoutParams(-1,-1));card.setAlpha(0f);card.setScaleX(.96f);card.setScaleY(.96f);card.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(140).start();main.postDelayed(()->{card.animate().alpha(0f).scaleX(.98f).scaleY(.98f).setDuration(160).withEndAction(()->root.removeView(overlay)).start();},2200);
    }

    private void showNetworkError(String text,Runnable retry){LinearLayout l=column();l.setGravity(Gravity.CENTER);l.setPadding(dp(28),dp(28),dp(28),dp(28));l.addView(bold("Tidak terhubung",20));TextView b=tv(text,14,muted);b.setGravity(Gravity.CENTER);LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,-2);bp.topMargin=dp(8);l.addView(b,bp);Button r=primary("Coba lagi");LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(50));rp.topMargin=dp(20);l.addView(r,rp);r.setOnClickListener(v->retry.run());swap(l);}
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

    private void handleShortcut(Intent intent){if(intent==null||intent.getData()==null)return;String d=intent.getData().toString();if(d.endsWith("/post"))navigate(new Screen("compose","","Postingan"),true);else if(d.endsWith("/chat"))goRoot("messages","","Chat");else if(d.endsWith("/video"))navigate(new Screen("generic","/reels.php","Video Pendek"),true);else if(d.endsWith("/story"))navigate(new Screen("generic","/story-create.php","Cerita"),true);}

    private <T> void runNet(NetWork<T> work, NetDone<T> ok, NetDone<Exception> fail){io.execute(()->{try{T v=work.run();main.post(()->{if(!isFinishing())ok.done(v);});}catch(Exception e){main.post(()->{if(!isFinishing())fail.done(e);});}});}
}
