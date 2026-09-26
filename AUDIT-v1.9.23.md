# Audit DeApp Lite v1.9.23

Versi: **1.9.23-lite (Build 33)**

## Fokus pembaruan

- Tipografi global web dan native diseragamkan ke stack font sistem modern dengan ritme/weight yang lebih dekat ke Threads tanpa menyertakan font eksternal.
- Back header dibuat route-aware: halaman utama section kembali ke Beranda, sedangkan detail/subhalaman kembali ke induk section yang konsisten.
- Notifikasi memakai dua tingkat navigasi yang terpisah: kategori di atas dan status `Semua / Belum dibaca` di bawahnya. Form filter lama disembunyikan agar tidak saling tumpang tindih.
- Scrim saat bottom sheet aktif diperkuat dan disamakan antara header native dan header web khusus.
- Menu `Dompet, Top Up, Kirim koin, Kode promo` dipisahkan ke kelompok **Dompet & Koin**. Etalase, Pet, Item Virtual, VIP, Stiker, koleksi, dan item toko lain masuk kelompok **Toko & Koleksi**.
- Halaman Top Up Koin dirapikan untuk mobile: hero lebih ringkas, paket 2 kolom, layout satu kolom, metode pembayaran lebih mudah dipindai, serta riwayat lebih flat.
- Pet mendapatkan motion berbeda sesuai karakter emoji (kucing, anjing, burung, panda, rubah, naga). Stiker dan item virtual memakai gerak ringan yang berbeda dan otomatis dihentikan pada reduced-motion/visual-saver.

## Navigasi Back header

- Chat detail → daftar Chat.
- Daftar Chat → Beranda.
- Notifikasi → Beranda, tidak mengikuti history dari Chat/Profil/halaman lain.
- Live detail/studio/tab → Live utama; Live utama → Beranda.
- Sub-Toko → Dompet; Dompet → Beranda.
- Pengaturan Notifikasi → Notifikasi.
- Sub-Pengaturan → Pengaturan utama; Pengaturan utama → Beranda.
- Video Pendek → Beranda.
- Detail postingan pada header native → Beranda.

## Validasi

- `node --check` pada `deapp_native_v19.js`: lulus.
- Struktur `{}` Java: seimbang.
- `versionCode`: 33.
- `versionName`: 1.9.23-lite.
