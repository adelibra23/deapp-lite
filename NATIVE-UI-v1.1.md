# Deapp Lite 1.1 — Native UI

Pembaruan ini mengubah Deapp Lite dari WebView fullscreen sederhana menjadi Android native shell.

## Yang berubah

1. Navigasi atas dan bawah dibuat dengan komponen Android native.
2. Navigasi web `.topbar` dan `.bottom-nav` disembunyikan khusus User-Agent DeappLite melalui CSS injection lokal di WebView; tampilan browser biasa tidak berubah.
3. Pull-to-refresh memakai AndroidX SwipeRefreshLayout.
4. Offline screen bukan lagi HTML internal, tetapi view Android native.
5. Splash, dark mode, system bar insets, haptic, share, file chooser, download, fullscreen video, dan back behavior diperbarui.
6. Live mempertahankan layar menyala saat ruang siaran terbuka.

Tidak ada perubahan database dan tidak ada perubahan yang wajib dilakukan pada source PHP Deapp.
