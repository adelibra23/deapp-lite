# Deapp Lite 1.2 — Clean Native UI

Pembaruan ini fokus pada tampilan aplikasi Android agar lebih minimal dan konsisten dengan pola aplikasi sosial native.

## Perubahan

1. Scrollbar kanan/kiri WebView dimatikan dan scrollbar HTML/CSS disembunyikan tanpa mematikan scrolling.
2. Popup menu Android diganti menjadi custom bottom sheet.
3. Modal `.modal-overlay` pada Deapp dipaksa menjadi bottom sheet khusus ketika User-Agent Deapp Lite aktif.
4. Splash screen menampilkan logo Deapp sebagai fokus utama, tanpa teks loading tambahan.
5. Header menjadi dua elemen: logo Deapp di kiri dan avatar pengguna di kanan.
6. Avatar diambil dari akun Deapp yang sedang login melalui bridge JavaScript satu arah yang hanya mengirim URL avatar/profil ke shell Android.
7. Bottom navigation dibuat icon-only: Beranda, Jelajah, Buat, Video, dan Notifikasi.
8. Profil, Pesan, Live, Deapp Tap, Pengaturan, Share, Refresh, dan Ganti Server dipindahkan ke bottom sheet akun agar tampilan lebih bersih.
9. Konfirmasi keluar dan pengaturan server memakai bottom sheet.
10. VersionCode dinaikkan menjadi 3 dan versionName menjadi `1.2.0-lite`.

Tidak ada perubahan database dan tidak ada migration PHP/MySQL.
