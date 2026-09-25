# DeApp Lite Android v1.9.5 — Long Post, Video Pendek & Threads Comments

Patch ini melanjutkan v1.9.4 dengan fokus pada tampilan feed, pengalaman Video Pendek, dan komentar.

## Perubahan utama

- Postingan panjang pada feed sekarang ditampilkan maksimal **250 karakter**.
- Tombol **Lihat selengkapnya** membuka isi postingan penuh tanpa mengubah data/server DeApp.
- Format rich text/link/mention tetap dipertahankan pada preview 250 karakter.
- Halaman **Video Pendek** memiliki header dan footer khusus, terpisah dari navigasi Android utama.
- Header Video Pendek memiliki tombol kembali, judul, dan tombol buat video.
- Footer Video Pendek memiliki navigasi **Untukmu, Mengikuti, Buat, Tersimpan, Saya**.
- Posisi metadata, tombol aksi, progress video, dan volume disesuaikan agar tidak tertutup header/footer khusus.
- Sheet komentar Video Pendek dirapikan dan selalu tampil di atas chrome video.
- Komentar detail postingan dibuat lebih ringan seperti pola Threads: bubble berat dihilangkan, avatar rail, metadata/action lebih sederhana, dan separator lebih rapi.
- Kolom komentar detail postingan memakai textarea visual yang dapat bertambah tinggi beberapa baris.
- Input `name="comment_text"` asli tetap dipertahankan dan disinkronkan, sehingga submit AJAX, balasan, mention, emoji, dan API komentar DeApp tetap menggunakan mekanisme asli.
- Header/footer Android utama dan FAB composer otomatis disembunyikan hanya saat `reels.php` dibuka agar tidak terjadi navigasi ganda.
- Pengaman scroll, tap/click, overlay, composer, Story, loader logo, dan optimasi performa dari v1.9.4 tetap dipertahankan.

Versi Android: **1.9.5-lite (Build 15)**.

## File patch

- `app/build.gradle`
- `app/src/main/java/id/deapp/lite/MainActivity.java`
- `app/src/main/assets/deapp_native_v19.js`

Ekstrak ZIP ke root project `~/deapp-build/deapp-lite-android`, lalu commit dan push ke GitHub seperti versi sebelumnya.
