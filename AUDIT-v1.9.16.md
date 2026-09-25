# Audit DeApp Lite Android v1.9.16

## Pengaturan

- `settings.php` tanpa parameter sekarang menjadi halaman indeks Pengaturan berbentuk list.
- Daftar asli DeApp tetap dipakai sehingga Profil, Tampilan, Pengalaman aplikasi, Bahasa, DeApp AI, Aksesibilitas, Premium, Top Up, Toko & dompet, Ruang Fokus, Notifikasi, Privasi, keamanan, bantuan, dan menu lain tidak kehilangan handler/link aslinya.
- Setiap `settings.php?tab=...` diperlakukan sebagai subhalaman mandiri: sidebar/list utama disembunyikan, isi pengaturan tampil penuh, dan tombol kembali menuju indeks Pengaturan.
- Menu native Ganti server, Perizinan aplikasi, dan Tentang aplikasi tetap ada.
- `Logout akun` ditambahkan sebagai opsi terakhir dan tetap memakai endpoint logout asli DeApp.

## DeApp AI

- Floating button DeApp AI di kanan bawah dihapus.
- Burger menu dan subtitle header dihapus; header hanya fokus pada judul DeApp AI dan tombol kembali.
- Navigasi AI tambahan, thread rail, pin, rename, trash, mode/konteks/memory strip disembunyikan dari UI Lite.
- Tampilan difokuskan ke Chat AI aktif, daftar pesan, dan composer.
- Composer dibuat fixed di bawah, lebih lebar, rounded, mendukung multiline, lampiran, dikte bila tersedia, regenerate, dan tombol kirim bulat.
- Handler/API AI asli tidak diganti.

## Build

- versionCode: 26
- versionName: 1.9.16-lite
- User Agent: DeappLite/1.9.16 NativeMobile/9.16
