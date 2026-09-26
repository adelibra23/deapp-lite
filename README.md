# DeApp Android 2.1.0 — Full Native Java

DeApp Android 2.1.0 adalah aplikasi Android native penuh. UI aplikasi tidak menggunakan WebView.

## Identitas
- Application ID: `id.deapp.app`
- Namespace: `id.deapp.app`
- Version code: 45
- Version name: `2.1.0`
- Bahasa UI Android: Java 17
- Backend: PHP + MySQL melalui HTTP/API

> Karena application ID berubah dari paket Lite lama menjadi `id.deapp.app`, Android memasangnya sebagai aplikasi baru. Ini disengaja agar DeApp 2.x benar-benar terpisah dari identitas Lite.

## Native UI
Screen utama dibuat dengan komponen Android seperti `FrameLayout`, `LinearLayout`, `ScrollView`, `SwipeRefreshLayout`, `TextView`, `EditText`, `Spinner`, `Switch`, `ImageView`, dan `ImageButton`.

Tidak ada `android.webkit.WebView`, `WebViewClient`, atau `WebChromeClient` pada source aktif.

## Screen native utama
- Login, 2FA, registrasi bertahap, Welcome
- Beranda dan feed
- Orang yang mungkin Anda kenal
- Sponsor native
- Composer postingan
- Detail postingan dan komentar
- Chat dan percakapan
- Notifikasi
- Profil
- Pencarian
- Story composer native
- Toko & Dompet
- Pengaturan
- Semua Fitur DeApp
- Halaman modul server dirender ulang sebagai Android View native

## Toko & Dompet
Dashboard Toko adalah halaman induk. Halaman berikut berdiri sendiri:
- Dompet
- Top Up
- Kirim Koin
- Kode Promo
- Etalase
- Pet
- Item Virtual
- VIP
- Menu lainnya

Menu lainnya berisi halaman native untuk Keinginan, Pet Care 3D, Boost Lab, Showroom 3D, Hadiah, Tema, Bingkai, Gelembung, Stiker, Efek Nama, Tiket, Tas Barang, Koleksiku, dan Level.

## Pengaturan Profil
- Foto Profil & Sampul: pemilih file Android + upload multipart native
- Info Profil: form native, Simpan di toolbar
- Username: form native, Simpan/Ganti di toolbar
- Custom Profile Studio: data server dibentuk ulang menjadi komponen native

Mode Tampilan, Pengalaman Aplikasi, Bahasa & Terjemahan, Aksesibilitas, Notifikasi, Privasi, AI, dan Karakter juga dirender dengan kontrol Android native. Form utama menggunakan tombol Simpan pada toolbar.

## Legacy server compatibility
Sebagian backend lama masih mengembalikan HTML. DeApp Android tidak menampilkan HTML tersebut. Jsoup hanya dipakai untuk mengekstrak data dari response server lama, lalu data dirender ulang sebagai Android View native. Ketika endpoint JSON tersedia, aplikasi menggunakan JSON secara langsung.

## GitHub Actions
Workflow diganti menjadi `Build DeApp Android APK` dan artifact menjadi `deapp-android-apk` / `deapp-android.apk`.
