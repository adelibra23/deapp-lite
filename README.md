# Deapp Lite Android

Deapp Lite adalah shell Android ringan untuk aplikasi Deapp PHP/MySQL. UI navigasi utama menggunakan komponen Android native, sedangkan feed, profil, Live, Video Pendek, AI, pesan, dan fitur Deapp tetap berasal dari server melalui WebView.

## v1.3 — Threads-inspired native shell

- Header: logo Deapp + menu fitur.
- Bottom navigation: Beranda, Jelajah, Aktivitas, Profil.
- Floating + button untuk membuat postingan.
- Menu fitur berupa grid bottom sheet berikon.
- Native bottom sheet dan modal web/composer dapat ditutup dengan swipe ke bawah.
- Feed lebih flat/minimal, scrollbar tidak terlihat.
- Dark mode, pull-to-refresh, fullscreen media, WebRTC Live, upload/download, kamera/mikrofon dan offline state tetap didukung.

## URL server

Hosting:

    https://domainanda.com/

XAMPP/LAN:

    http://192.168.1.10/deapp/

Jangan gunakan `localhost` di ponsel karena `localhost` menunjuk ke perangkat Android sendiri.

## Build

GitHub Actions pada `.github/workflows/build-apk.yml` membangun APK debug installable sebagai artifact `deapp-lite-apk`.
