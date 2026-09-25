# Deapp Lite Android 1.2 — Clean Native Shell

Deapp Lite adalah wrapper Android ringan untuk server Deapp PHP/MySQL. Versi 1.2 memoles shell Android agar terasa lebih seperti aplikasi sosial native tanpa membawa backend PHP/MySQL ke dalam APK.

## Native UI 1.2

- Splash screen fokus pada logo Deapp, termasuk Android 12+ serta fallback perangkat lama.
- Header sangat ringkas: hanya logo Deapp di kiri dan avatar akun aktif di kanan.
- Avatar header mengikuti foto profil akun Deapp yang sedang login.
- Bottom navigation icon-only: Beranda, Jelajah, Buat, Video Pendek, Notifikasi.
- Menu akun dipindahkan ke bottom sheet saat avatar header diketuk.
- Ganti server dan konfirmasi keluar juga memakai bottom sheet, bukan dialog popup klasik.
- Modal web Deapp (`.modal-overlay`) ditampilkan sebagai bottom sheet ketika dibuka melalui APK.
- Scrollbar WebView dan scrollbar HTML disembunyikan, tetapi halaman tetap dapat digulir normal.
- Pull-to-refresh, offline screen, upload file, download, kamera/mikrofon WebRTC, geolocation, fullscreen video, haptic dan dark mode tetap didukung.
- Header dan bottom navigation milik website tetap disembunyikan hanya di APK; tampilan browser biasa tidak berubah.

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

- versionCode: 3
- versionName: 1.2.0-lite
