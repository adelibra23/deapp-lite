# Deapp Lite Android

## v1.9.2-lite — Tap / Click Fix

Patch ini memperbaiki bug halaman DeApp Lite Android yang dapat tetap bisa tampil atau di-scroll tetapi tidak merespons tap/klik. Penyebab utamanya adalah backdrop menu posting yang dapat tertinggal dan menutupi seluruh WebView ketika status `open` tidak sama dengan kondisi visual dropdown.

Perbaikan:
- backdrop menu posting hanya dibuat ketika dropdown benar-benar terlihat di viewport;
- backdrop posting yang stale/orphan otomatis dibersihkan tanpa menutup modal yang masih aktif;
- backdrop opsi profil yang kehilangan sheet pasangannya otomatis dihapus;
- pemulihan interaksi dijalankan saat scan DOM, `pageshow`, focus, kembali dari background, serta sebelum gesture baru;
- splash native berhenti menerima tap sebelum animasi fade dan memiliki fallback removal agar tidak mungkin tertinggal menutup WebView;
- Android memanggil recovery interaksi setelah `onPageCommitVisible`, `onPageFinished`, dan saat resume;
- build dinaikkan ke **1.9.2-lite (Build 12)**.

## v1.9.1-lite — Scroll Fix

Patch ini memperbaiki bug halaman Android DeApp Lite yang tidak bisa di-scroll setelah modal/bottom sheet dibuka atau ditutup. Scroll lock sekarang hanya berlaku untuk sheet berbasis web yang benar-benar terlihat, tidak lagi mengunci DOM saat bottom sheet native Android aktif, dan memiliki pemulihan otomatis saat sheet ditutup, halaman tampil kembali, aplikasi kembali dari background, atau status modal tidak sinkron.

Perbaikan tambahan:
- `overflow-y` halaman dipulihkan ke mode scroll normal ketika tidak ada sheet aktif.
- `touch-action` root dikembalikan ke `auto` agar gesture vertikal WebView tidak terblokir.
- deteksi modal kini memeriksa visibility ancestor dan posisi viewport untuk mencegah dropdown tersembunyi dianggap masih terbuka.
- gesture guard akan membatalkan scroll lock yang stale secara otomatis.
- build dinaikkan ke **1.9.1-lite (Build 11)**.

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
