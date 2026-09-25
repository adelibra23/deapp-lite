# Deapp Lite Android

Deapp Lite adalah shell Android ringan untuk aplikasi Deapp PHP/MySQL. Navigasi dan pengalaman mobile dipoles di sisi Android, sementara akun, feed, postingan, komentar, pesan, Live, Reels, komunitas, AI dan data tetap berasal dari server Deapp.

## v1.9.0-lite

Versi ini memperbaiki bottom sheet dan performa loading, mengunci layout agar tidak melebar pada layar mobile, memindahkan pengaturan server/perizinan ke halaman Pengaturan, menyederhanakan area Story, membuat tombol plus posting lebih konsisten, serta mengubah halaman detail posting dan kolom komentarnya menjadi pengalaman yang lebih dekat dengan Threads.

Lihat `NATIVE-UI-v1.9.md` untuk rincian perubahan.

### Build otomatis

Push ke branch `main` akan menjalankan `.github/workflows/build-apk.yml`. Artifact hasil build bernama `deapp-lite-apk`.

### Server

Untuk hosting publik gunakan HTTPS. Untuk XAMPP/LAN gunakan alamat IP perangkat server, bukan `localhost` dari ponsel.
