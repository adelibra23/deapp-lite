# DeApp Android 2.1.1

DeApp Android adalah klien Android native Java untuk backend DeApp PHP/MySQL.

## Build

- versionCode: 46
- versionName: 2.1.1
- namespace: `id.deapp.app`
- applicationId: `id.deapp.app`
- UI: Android native Java
- WebView: tidak digunakan

## Koneksi server

Karena package native adalah `id.deapp.app`, konfigurasi server dari aplikasi Lite lama tidak dapat dibaca otomatis oleh Android. Pada pemasangan pertama, aplikasi menampilkan halaman **Hubungkan ke Server DeApp**.

Contoh:

- Hosting: `https://domain.com/deapp`
- XAMPP/LAN: `192.168.1.10/deapp`

Aplikasi akan menguji koneksi sebelum menyimpan URL. Jika halaman gagal dimuat, state native menyediakan **Coba lagi** dan **Atur server DeApp**.

## Fix v2.1.1

- feed JSON wrapper diperbaiki;
- blank screen diganti native skeleton/error state;
- komentar membaca field API yang benar;
- penggantian server membersihkan cookie lama;
- generic JSON/HTML server response dirender menjadi Android View native;
- workflow memverifikasi source aktif bebas WebView sebelum compile.
