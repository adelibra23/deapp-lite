# Deapp Lite Android 1.1 — Native Shell

Deapp Lite adalah wrapper Android ringan untuk server Deapp PHP/MySQL. Versi 1.1 mengganti chrome WebView dengan shell Android native sehingga pengalaman pemakaian terasa seperti aplikasi sungguhan tanpa membawa seluruh backend ke dalam APK.

## Native UI 1.1

- Native splash screen Android 12+ dan loading screen Deapp untuk versi lama.
- Edge-to-edge system bars dengan dukungan light/dark mode.
- Native top app bar dengan judul halaman, status koneksi, Jelajah, Pesan, tombol Back, dan overflow menu.
- Native bottom navigation: Beranda, Video, Buat, Notif, Profil.
- Header dan bottom navigation web otomatis disembunyikan hanya ketika dibuka dari APK.
- Native pull-to-refresh.
- Native offline/server error screen dengan Coba lagi dan Ganti server.
- Native share sheet, external browser intent, download manager, popup menu, dan haptic feedback.
- Fullscreen HTML5 video support.
- Keep-screen-on otomatis untuk Live/fullscreen video.
- File picker Android, kamera/mikrofon WebRTC, geolocation, cookies/session.
- Predictive/back callback Android 13+.
- Server URL tetap bisa diganti dari dalam aplikasi.

## Server

Hosting:

    https://domainanda.com/

XAMPP/LAN:

    http://192.168.1.10/deapp/

Jangan gunakan `localhost` di ponsel karena localhost menunjuk ke perangkat Android itu sendiri.

## Build otomatis

Workflow `.github/workflows/build-apk.yml` membangun debug APK yang dapat langsung dipasang untuk pengujian.

Artifact:

    deapp-lite-apk / deapp-lite.apk

Konfigurasi build:

- Java 17
- Gradle 8.9
- Android Gradle Plugin 8.7.3
- compileSdk / targetSdk 35
- minSdk 24

## Versi

- versionCode: 2
- versionName: 1.1.0-lite
