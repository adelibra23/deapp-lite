# Audit DeApp Lite Android v1.9.15

## Koreksi bottom navigation

Pada v1.9.14, slot `+` di tengah bottom navigation masih ikut disembunyikan saat pengguna berada di luar Beranda. Itu tidak sesuai klarifikasi terbaru. v1.9.15 memisahkan dua jenis tombol plus:

- **Bottom navigation `+ Postingan`**: selalu tampil selama bottom navigation umum aktif.
- **Floating Action Button/FAB `+` di pojok kanan bawah**: hanya tampil di Beranda.

Susunan bottom navigation umum sekarang konsisten:

**Beranda · Chat · + Postingan · Notifikasi · Profil**

Bottom navigation masih boleh tersembunyi sementara ketika keyboard, modal/bottom sheet, composer, layar immersive, atau halaman yang memang memiliki chrome khusus aktif.

## Versi

- `versionCode`: `25`
- `versionName`: `1.9.15-lite`
- User agent native: `DeappLite/1.9.15 NativeMobile/9.15`
