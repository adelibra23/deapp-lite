# DeApp Lite Android v1.9.29

Patch Android Lite untuk DeApp dengan fokus **native-first UI audit**. Header yang sebelumnya masih terlihat seperti bagian dari WebView sekarang dikendalikan oleh toolbar Android native, sedangkan backend/form server tetap dipertahankan agar kompatibel.

## Versi
- versionCode: 39
- versionName: 1.9.29-lite

## Update utama
- Toolbar Android native menjadi header utama untuk Messages list, Notifikasi, Live, DeApp AI, Toko & Dompet, Pengaturan, serta route pendukung seperti ASK, Bookmark, Komunitas, Koneksi, Games, Memori, Tap, Ads, Top Up, Developer, Character, Hashtag, Media, dan halaman ekspor.
- Header HTML/WebView pada route tersebut disembunyikan untuk mencegah double header.
- Back dan aksi kanan toolbar native meneruskan ke controller route-aware di halaman, jadi perilaku backend tetap sama.
- Route yang sebelumnya terasa desktop/web dirapikan: page-head, side rail, footer website, lebar desktop, card, form field, dan fokus input disesuaikan ke pola aplikasi.
- Halaman edit dengan satu aksi utama Simpan/Perbarui/Ganti/Terapkan memindahkan aksi itu ke ikon Simpan di pojok kanan atas header.
- Tombol Simpan bawah asli disembunyikan hanya jika sudah dipindahkan ke toolbar native.
- Percakapan Chat aktif tetap memakai header partner khusus di area chat agar informasi lawan bicara tidak hilang.
