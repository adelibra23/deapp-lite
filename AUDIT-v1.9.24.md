# Audit DeApp Lite v1.9.24

Versi: **1.9.24-lite (Build 34)**

## Perubahan

- Pull-to-refresh tetap memakai gesture `SwipeRefreshLayout`, tetapi `CircleImageView`/spinner/arrow bawaan tidak digambar. Hanya satu indikator logo DeApp yang terlihat.
- Indikator refresh custom dibuat tanpa bubble tambahan agar terasa lebih ringan.
- Transisi halaman native memakai fade + sedikit gerakan vertikal saat halaman baru mulai terlihat, tanpa mengubah route/history.
- Ikon bottom navigation Beranda, Chat, Buat, Notifikasi, dan Profil diganti vector outline yang lebih konsisten; state aktif memakai tint/scale ringan tanpa pill latar besar.
- Beranda menambahkan discovery rails horizontal berbasis data asli DeApp:
  - Orang yang mungkin Anda kenal, diambil dari suggestion rail.
  - Disarankan untuk Anda, berupa teaser post yang sudah ada pada feed.
  - Bersponsor, memakai ad placement rail dari Ads Manager dan tetap mempertahankan label/kontrol sponsor asli.
- Semua discovery rail memakai horizontal swipe, scroll snapping, dan tidak membuat user/ad/post palsu.
- Dark mode dan reduced-motion dari versi sebelumnya tetap dipertahankan.

## Validasi

- JavaScript lolos `node --check`.
- XML drawable baru valid.
- Struktur kurung source Java seimbang.
- ZIP lolos integrity test.
