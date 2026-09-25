# Deapp Lite Android

## v1.9.0-lite

Versi ini memindahkan Ganti server dan Perizinan ke Pengaturan, menyederhanakan header composer/post detail, menambahkan kolom komentar sticky ala Threads, menyembunyikan story tray atas, memperketat lebar mobile, memperbaiki bottom sheet, dan mempercepat loading.

Deapp Lite adalah shell Android ringan untuk aplikasi Deapp PHP/MySQL. UI Android memberi pengalaman seperti aplikasi native, sedangkan feed, akun, profil, pesan, Live, Reels, AI, komunitas dan data tetap dilayani server Deapp.

## v1.8.0-lite

Versi ini mengunci scroll halaman belakang saat bottom sheet aktif dan memoles kartu postingan menjadi feed thread-style yang lebih minimal: avatar rail, konten sejajar, media membulat, serta action icon-first. Fitur posting, reaction, komentar, gift, share, bookmark, terjemahan, dan menu tetap memakai fungsi server Deapp yang sama.

### Build otomatis

Push ke branch `main` akan menjalankan `.github/workflows/build-apk.yml`. Artifact hasil build bernama `deapp-lite-apk`.

### Server

Untuk hosting publik gunakan HTTPS. Untuk XAMPP/LAN gunakan alamat IP perangkat server, bukan `localhost` dari ponsel.


## Native UI v1.8.0
Lihat `NATIVE-UI-v1.8.md` untuk background scroll lock pada bottom sheet dan tampilan postingan thread-style.

## Native UI v1.7.0
Lihat `NATIVE-UI-v1.7.md` untuk refresh spinner tunggal, isolasi gesture bottom sheet, loading spinner tengah, dan penghapusan progress bar atas.

## Native UI v1.6.0
Lihat `NATIVE-UI-v1.6.md` untuk Pusat Perizinan, feedback tap tanpa visual browser, suara sukses posting, dan animasi Deapp pull-to-refresh.
