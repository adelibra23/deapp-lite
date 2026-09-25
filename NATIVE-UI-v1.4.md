# Deapp Lite Android v1.4 — Native Mobile Navigation

Fokus v1.4 adalah membuat shell Android terasa lebih konsisten seperti aplikasi sosial native, sambil tetap memakai backend PHP/MySQL Deapp v2.74.

## Perubahan utama

- Bottom navigation hanya tampil setelah `window.DEAPP.LOGGED_IN === true`.
- Bottom navigation berisi 5 menu inti: Beranda, Pesan, Buat kiriman (+), Notifikasi, dan Profil.
- Foto profil pengguna otomatis dipakai sebagai ikon menu Profil bila tersedia.
- Floating button (+) di kanan bawah tetap tersedia, tetapi **hanya tampil di halaman Beranda** dan hanya ketika pengguna sudah login.
- Header ditata ulang:
  - kiri: logo/mark Deapp,
  - tengah: wordmark `Deapp`,
  - kanan: menu fitur Deapp.
- Menu fitur kanan atas tetap memakai native bottom sheet berikon untuk Profil, Pesan, Live, Video, Komunitas, Tersimpan, Deapp Tap, AI, Developer, Toko, Mini Game, Pengaturan, dan Ganti server.
- Pull-to-refresh dibuat lebih jelas dengan indikator ukuran besar, jarak tarik yang lebih natural, dan warna yang mengikuti tema Deapp.
- Semua tab horizontal (`.tabs`, `.story-tabs`, `.nx-studio-nav`, `role=tablist`, dll.) dapat digeser ke samping dengan sentuhan maupun drag mouse.
- Menu opsi kiriman (`.post-card .menu-wrap`) diubah menjadi bottom sheet penuh di bagian bawah.
- Bottom sheet opsi kiriman dan modal web dapat ditutup dengan swipe/tarik ke bawah dari area sheet saat posisi scroll sudah di atas.
- Profil dipoles menjadi full-mobile: card utama edge-to-edge, cover penuh, header profil web disembunyikan karena sudah ada header native, action lebih rapat, tab profil horizontal, dan konten kiriman dibuat lebih menyatu dengan layar.
- Tap pada link, tombol, menu, aksi postingan, tab, dan kontrol lain tidak lagi memakai highlight browser. Shell menambahkan pressed-state singkat + haptic Android agar terasa seperti interaksi native.
- Scrollbar web tetap disembunyikan tanpa mematikan kemampuan scroll.
- Keyboard dan video fullscreen otomatis menyembunyikan bottom navigation/FAB agar tidak menutup konten.

## Integrasi Deapp v2.74

v1.4 membaca data sesi dari `window.DEAPP` yang sudah disediakan oleh Deapp v2.74:

- `DEAPP.LOGGED_IN`
- `DEAPP.ME.username`
- `DEAPP.ME.avatar`
- `DEAPP.BASE_URL`

Dengan begitu shell Android tidak menebak status login dari tampilan semata.

## Versi

- versionCode: 5
- versionName: 1.4.0-lite
