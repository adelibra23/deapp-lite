# Deapp Lite Android

Deapp Lite adalah shell Android ringan untuk aplikasi Deapp PHP/MySQL. UI Android memberi pengalaman seperti aplikasi native, sedangkan feed, akun, profil, pesan, Live, Reels, AI, komunitas dan data tetap dilayani server Deapp.

## v1.7.0-lite

Versi ini memoles shell Android agar lebih stabil saat berpindah halaman dan menggunakan bottom sheet: refresh memakai satu spinner, gesture sheet tidak lagi memicu refresh, loading halaman tampil di tengah layar, dan progress bar atas dihapus.

### Build otomatis

Push ke branch `main` akan menjalankan `.github/workflows/build-apk.yml`. Artifact hasil build bernama `deapp-lite-apk`.

### Server

Untuk hosting publik gunakan HTTPS. Untuk XAMPP/LAN gunakan alamat IP perangkat server, bukan `localhost` dari ponsel.


## Native UI v1.7.0
Lihat `NATIVE-UI-v1.7.md` untuk refresh spinner tunggal, isolasi gesture bottom sheet, loading spinner tengah, dan penghapusan progress bar atas.

## Native UI v1.6.0
Lihat `NATIVE-UI-v1.6.md` untuk Pusat Perizinan, feedback tap tanpa visual browser, suara sukses posting, dan animasi Deapp pull-to-refresh.
