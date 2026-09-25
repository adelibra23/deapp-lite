# Deapp Lite Android

Wrapper Android ringan untuk Deapp v2.74+. Aplikasi Android hanya memuat UI melalui WebView; PHP/MySQL tetap berjalan di server Deapp.

## Kenapa model ini ringan?

WebView berasal dari Android System WebView/Chrome yang sudah ada di perangkat. APK tidak membawa Chromium, PHP, MySQL, atau seluruh source Deapp ke dalam APK.

## Fitur wrapper

- Server URL dapat diatur saat pertama kali membuka app.
- Mendukung HTTPS dan HTTP LAN/XAMPP.
- JavaScript, cookies, DOM storage, service worker/WebView modern.
- Upload foto/video/file melalui input web.
- Kamera + mikrofon untuk WebRTC/Live.
- Geolocation jika halaman meminta izin.
- Download melalui Download Manager Android.
- Tautan eksternal dibuka di aplikasi/browser eksternal.
- Tombol Back Android mengikuti history WebView.
- Saat di root, Back menyediakan opsi Keluar atau Ganti server.
- Ikon Deapp sendiri.

## URL server

Hosting:

    https://domainanda.com/

XAMPP dari ponsel satu Wi-Fi dengan laptop:

    http://192.168.1.10/deapp/

Jangan memakai `http://localhost/deapp/` di ponsel, karena `localhost` akan menunjuk ke ponsel itu sendiri.

## Build di Android Studio

1. Buka folder ini sebagai project Android Studio.
2. Pastikan Android SDK 35 tersedia.
3. Build > Generate App Bundles or APKs > Generate APKs.
4. Untuk APK rilis, buat signing key milik Anda sendiri.

## Build dari command line

Project memakai Android Gradle Plugin 8.7.3 dan compileSdk 35.

    gradle assembleDebug

Hasil:

    app/build/outputs/apk/debug/app-debug.apk

## Catatan produksi

Untuk server publik gunakan HTTPS agar kamera, mikrofon, geolocation dan keamanan session lebih konsisten. HTTP sengaja diizinkan untuk pengujian LAN/XAMPP.

## Build otomatis dengan GitHub Actions

Project sudah menyertakan `.github/workflows/build-apk.yml`. Jika repository di-push ke GitHub, workflow akan membuild `deapp-lite.apk` dan menyimpannya sebagai artifact `deapp-lite-apk`.
