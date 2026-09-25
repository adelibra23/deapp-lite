# Audit DeApp Lite Android v1.9.7

## Temuan

1. Bottom navigation Android sebelumnya hanya mengikuti state login, keyboard, composer, detail post, Reels, dan section khusus. State `activeSheetOverlay` / `webSheetOpen` belum ikut menentukan visibilitas menu bawah.
2. Web bottom sheet hidup di dalam WebView. Scrim web tidak menutupi toolbar Android, sehingga header dapat tetap terang ketika modal aktif.
3. Footer section khusus dibuat dengan elemen fixed di dalam WebView dan perlu ikut disembunyikan ketika modal aktif.
4. Deteksi sheet generik belum mencakup `#story-sheet` dan panel komentar/more pada Video Pendek.
5. Loader logo sebelumnya adalah indikator loading konten, bukan splash pembuka full-screen berdurasi tetap.

## Perbaikan

- `updateChromeVisibility()` sekarang memakai state sheet untuk menyembunyikan bottom navigation dan floating composer.
- `syncWebSheetState()` memanggil pembaruan chrome Android, sehingga perubahan state modal web langsung tercermin di shell native.
- Toolbar Android diberi foreground gelap tipis ketika web sheet aktif.
- Scrim native disesuaikan menjadi lebih lembut (`#52000000`).
- JavaScript menambahkan state `deapp-bottom-sheet-active` untuk meredupkan header section dan menyembunyikan footer section.
- Deteksi sheet mencakup Story sheet dan Reels comments/more sheet.
- Splash full-screen baru memakai logo DeApp dengan animasi pulse/scale dan fade-out. Splash berjalan sekitar 5 detik sementara WebView tetap loading di belakangnya.
- Splash hanya dijalankan sekali per Activity agar pergantian server/navigasi internal tidak terus memutar splash.

## Validasi

- `node --check` untuk `deapp_native_v19.js`: lolos.
- Struktur kurung source Java: seimbang.
- VersionCode/VersionName: `17 / 1.9.7-lite`.
