# DeApp Lite Android v1.9.10 — Home & Reaction Visual Refresh

Patch ini melanjutkan DeApp Lite v1.9.9 dan berfokus pada tampilan Beranda, menu fitur, serta feedback reaction.

## Perubahan utama

- Menu fitur DeApp pada bottom sheet native sekarang memakai ikon dengan warna berbeda per fitur dan bubble warna lembut agar lebih hidup serta cepat dikenali.
- Ringkasan harian di Beranda (`daily-strip`) disembunyikan di DeApp Lite. Bagian koin, absen/streak, misi harian, tiket di tas, dan level tidak lagi memenuhi area sebelum Story. Fitur aslinya tetap tersedia di Toko & Dompet/halaman terkait.
- Story langsung naik ke bagian atas Beranda setelah header.
- Tab Beranda **Untukmu, Mengikuti, Topik, Populer, Media, Tanya-jawab** dibuat sedikit lebih besar, berbentuk pill, dan mendapat aksen warna berbeda.
- Tab aktif memiliki border/aksen yang lebih jelas tanpa mengubah URL atau handler tab asli DeApp.
- Tombol reaction postingan dan pilihan emoji reaction memberi feedback haptic/getaran Android ketika ditap.
- Haptic dipicu lewat bridge `DeappNative.tap()`, sehingga tidak mengganggu request reaction atau gesture scroll.

## Stabilitas yang dipertahankan

Patch tetap membawa DeApp AI burger + FAB, universal bottom-sheet detection, UI chat Threads-style, Story/Reels chrome, posting 250 karakter, splash logo 5 detik, composer post fix, serta optimasi WebView dari versi sebelumnya.

Versi Android: **1.9.10-lite (Build 20)**.
