# Audit DeApp Lite Android v1.9.5

## 1. Postingan panjang

**Masalah:** feed menampilkan seluruh isi postingan sehingga kartu menjadi terlalu panjang pada mobile.

**Perbaikan:** native patch menghitung isi `.post-content` dan membuat preview 250 karakter hanya pada feed. Node rich text/link/mention diklon secara bertahap agar struktur HTML tidak berubah menjadi plain text. Halaman detail postingan tidak dipotong.

## 2. Video Pendek

**Masalah:** halaman `reels.php` sebelumnya tetap memakai header/footer Android umum sehingga pengalaman video pendek tidak terasa fullscreen dan kontrol video bisa berdesakan dengan navigasi.

**Perbaikan:** `reels.php` dikenali sebagai page type khusus. Native top bar, bottom bar, dan floating composer disembunyikan hanya pada halaman ini. WebView menambahkan header/footer khusus Video Pendek yang memakai tab asli DeApp sebagai sumber URL, sehingga handler/feed backend tetap utuh.

## 3. Komentar ala Threads

**Masalah:** komentar memakai bubble besar dan input satu baris. Mengganti input asli secara langsung berisiko memutus handler AJAX DeApp.

**Perbaikan:** input asli `name="comment_text"` tetap berada di form sebagai sumber data. Textarea visual baru disinkronkan ke input asli dan tetap mendukung mention, balasan, emoji, serta submit AJAX. Tampilan komentar dibuat flat dengan avatar rail, separator, metadata ringan, dan composer fixed di bawah.

## 4. Komentar Video Pendek

Sheet komentar Reels dirapikan menjadi bottom sheet mobile yang lebih tinggi, dengan item komentar yang lebih lega dan composer kapsul + tombol kirim bulat. Sheet dipastikan berada di atas header/footer video khusus.

## 5. Stabilitas

- Scroll/tap recovery dari versi sebelumnya tetap aktif.
- Custom chrome Reels hilang otomatis saat komentar, menu lainnya, atau Creator Studio terbuka.
- Native system bar menggunakan ikon terang di halaman Reels agar kontras dengan latar hitam.
- Swipe refresh tetap hanya aktif di Home/Feed.

## Validasi statis

- `node --check app/src/main/assets/deapp_native_v19.js` lulus.
- Kurung kurawal `MainActivity.java` seimbang.
- Nomor versi disinkronkan ke `1.9.5-lite`, Build `15`, `NativeMobile/9.5`.
