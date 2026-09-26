# DeApp Lite Android v1.9.28 — Instant Navigation, Visible Refresh, Threads Typography

Build: 38

Perubahan utama:
- Setelah sesi login diketahui, perpindahan halaman tidak lagi menampilkan loading overlay.
- WebView mempertahankan frame halaman lama sampai halaman baru commit, kemudian melakukan micro reveal singkat ala Threads.
- Pull-to-refresh tetap hanya memakai satu indikator custom; indikator dibuat lebih kontras dan terlihat sejak tarikan awal.
- Ikon refresh native diperbarui menjadi outline circular-arrow yang lebih bersih.
- Tipografi WebView dirapikan dengan stack Android/system yang mendekati Threads, ukuran dasar 15px, weight lebih natural, dan letter spacing normal.
- Judul toolbar native diperkecil agar proporsinya lebih dekat ke pola Threads.
- Semua link menghapus underline pada seluruh state browser.
- Mention, hashtag, dan URL di konten memakai accent biru konsisten, termasuk dark mode, bukan warna blue/purple bawaan browser.

Validasi:
- node --check deapp_native_v19.js: lulus
- XML resources: valid
- MainActivity.java brace structure: seimbang
- versionCode 38 / versionName 1.9.28-lite
