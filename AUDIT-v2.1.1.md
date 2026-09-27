# DeApp Android v2.1.1 — Native Server Bootstrap & Blank Screen Recovery

Version: 2.1.1
Build: 46
UI runtime: Native Java
Application ID: id.deapp.app

## Perbaikan utama

- Memperbaiki Beranda native yang kosong karena `/api/feed.php` mengembalikan JSON wrapper dengan field `html`.
- Feed sekarang membuka field `html` dari JSON lalu membentuk kartu postingan sebagai Android View native.
- Komentar sekarang membaca field `text` dari API DeApp.
- Menambahkan bootstrap server native yang menguji alamat server sebelum disimpan.
- Input server tanpa scheme otomatis diberi `http://` untuk alamat LAN/localhost dan `https://` untuk domain publik.
- Jika root server dipakai, aplikasi juga mencoba subfolder `/deapp` secara aman.
- Pergantian server membersihkan cookie sesi server lama agar tidak terjadi sesi silang.
- Beranda, Chat, Notifikasi, Profil, Postingan, Toko, Top Up, Pengaturan Profil, dan generic native modules mempunyai skeleton native sehingga tidak pernah menjadi halaman putih kosong saat request berjalan.
- Semua kegagalan jaringan menampilkan state native dengan Coba lagi dan Atur server DeApp.
- Pengaturan kini menyediakan menu Server DeApp.
- Response JSON tanpa HTML dapat dirender sebagai kartu data native, bukan teks browser/WebView.
- Manifest menjamin INTERNET serta HTTP LAN/XAMPP (`usesCleartextTraffic=true`) dan label aplikasi `DeApp`.
- GitHub Actions memiliki pemeriksaan native-only sebelum compile.

## Catatan koneksi

`id.deapp.app` adalah package baru, sehingga Android tidak dapat membaca SharedPreferences privat dari aplikasi lama `id.deapp.lite`. Pada instalasi native pertama, alamat backend DeApp perlu dimasukkan sekali. Setelah berhasil diuji, URL disimpan di SharedPreferences aplikasi native.

## Tidak menggunakan WebView

Source aktif `id/deapp/app/**` tidak menggunakan `android.webkit.WebView`, `WebViewClient`, atau `WebChromeClient`.
