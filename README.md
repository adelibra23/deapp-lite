# Deapp Lite Android v1.4

Deapp Lite adalah Android native shell ringan untuk Deapp. Backend, login, postingan, pesan, notifikasi, Live, Reels, komunitas, dan data tetap berasal dari aplikasi PHP/MySQL Deapp di server.

## Tampilan v1.4

- Header native: logo Deapp kiri, wordmark Deapp tengah, menu fitur kanan.
- Bottom navigation 5 menu: Beranda, Pesan, +, Notifikasi, Profil.
- Bottom navigation hanya muncul setelah pengguna login.
- Floating + hanya muncul di Beranda.
- Menu opsi postingan menjadi draggable bottom sheet.
- Composer dan modal lain tetap tampil sebagai bottom sheet yang bisa ditarik turun.
- Profil dibuat edge-to-edge/full mobile.
- Tab di seluruh aplikasi bisa digeser horizontal dengan touch atau mouse.
- Pull-to-refresh memakai indikator Android yang lebih besar dan lebih jelas.
- Interaksi web diberi native pressed feedback dan haptic, tanpa tap-highlight bawaan browser.
- Scrollbar visual disembunyikan.

## Build APK

Workflow GitHub Actions berada di `.github/workflows/build-apk.yml` dan otomatis berjalan setiap push ke branch `main`.

Artifact hasil build bernama `deapp-lite-apk` dan berisi `deapp-lite.apk`.

## Versi

`1.4.0-lite` — versionCode `5`
