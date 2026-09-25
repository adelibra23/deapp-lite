# Audit DeApp Lite Android v1.9.11

## Story

- Menggunakan elemen asli `#story-viewer`, `#story-progress`, `.story-topbar`, `.story-stage`, `.story-nav`, `.story-bottom`, `.story-reply`, dan `#story-sheet`.
- Tidak mengubah request/API Story, timer, next/previous, reaction, comment maupun viewer list.
- Navigasi kiri/kanan dibuat sebagai touch-zone transparan agar tetap mudah ditap tanpa ikon panah besar.
- Story tray memakai ring hijau ala WhatsApp untuk status yang belum dilihat.

## Pengaturan

- `.deapp-section-footer[data-section="settings"]` dihapus.
- Header dibuat dengan action kanan kosong agar judul tetap simetris di tengah.
- `settingsBack()` lama tetap digunakan, sehingga subhalaman keamanan/privasi tetap kembali ke induk yang benar.
- `.settings-nav` tetap tersedia sebagai navigasi internal, tetapi visualnya diringankan.

## Dark mode

- Observer ikut memantau class pada `<html>`.
- `syncTheme()` mengirim status `is-dark` ke native bridge.
- `MainActivity.applyRuntimeTheme()` memperbarui palet root, WebView, top/bottom chrome, FAB, nav item, status bar, navigation bar dan refresh indicator.

## Scroll & tombol posting

- Scroll pada window maupun panel internal dideteksi menggunakan event capture.
- State scroll didebounce 190 ms.
- FAB/action web disembunyikan ketika scroll lewat class `deapp-native-scrolling`.
- Native compose FAB mengikuti `syncScrollState()`.
- Native compose FAB dan slot `+` bottom navigation hanya ditampilkan pada URL Beranda.

## Validasi

- `node --check` untuk `deapp_native_v19.js`: lulus.
- Kurung `{}`, `()`, `[]` Java dan JavaScript: seimbang.
- `versionCode`: 21.
- `versionName`: `1.9.11-lite`.
- About Android: `Versi 1.9.11-lite · Build 21`.
