# Audit DeApp Lite v1.9.32

## Toko & Dompet

- Dashboard Toko memakai route shell `shop.php?view=home`.
- Route `shop.php?tab=...` diperlakukan sebagai halaman detail mandiri dan menyembunyikan grid menu, wallet hero global, serta perk strip global.
- `shop.php?view=more` membuat halaman Menu lainnya mandiri menggunakan link backend Toko yang sudah ada.
- Back detail/menu lainnya kembali ke dashboard Toko; Back dashboard kembali ke Beranda.
- Entry Toko dari menu native Android diarahkan ke `shop.php?view=home`.

## Beranda

- Rail `Disarankan untuk Anda` berbasis teaser postingan dihapus.
- `Orang yang mungkin Anda kenal` dan iklan Bersponsor tetap dapat tampil dari data asli DeApp.

## Kompatibilitas

- Tidak ada perubahan skema database atau endpoint PHP.
- Tab Toko asli tetap dipakai sebagai backend konten.
- JavaScript divalidasi dengan `node --check`.
