# DeApp Lite Android v1.9.7 — Bottom Sheet + Startup Splash

Patch ini melanjutkan v1.9.6 dan merapikan perilaku modal/bottom sheet serta layar pembuka aplikasi.

## Perubahan utama

- Navigasi menu bawah Android otomatis disembunyikan ketika bottom sheet native atau bottom sheet/modal web sedang aktif.
- Footer section khusus seperti Notifikasi, Live, DeApp AI, Toko & Dompet, Pengaturan, dan Video Pendek ikut disembunyikan ketika sheet aktif.
- Header tetap terlihat tetapi diberi efek gelap/dim ringan seperti latar belakang modal bottom sheet Threads.
- Bottom sheet native menggunakan scrim yang lebih lembut supaya halaman di belakang tetap terbaca tanpa terasa terlalu gelap.
- Deteksi bottom sheet diperluas ke Story, komentar/more Video Pendek, modal web, menu postingan, dan sheet profil.
- Floating composer ikut disembunyikan selama bottom sheet aktif agar tidak bertabrakan dengan modal.
- Splash pembuka baru menampilkan animasi logo DeApp selama sekitar 5 detik.
- WebView langsung memuat server di belakang splash sehingga durasi splash tidak menunda awal koneksi halaman.
- Splash hanya tampil sekali per pembukaan Activity/aplikasi, bukan setiap perpindahan halaman.
- Semua perbaikan v1.9.6 tetap dipertahankan: section chrome, Story, Chat, Notifikasi, Live, DeApp AI, Toko & Dompet, Pengaturan, post 250 karakter, Reels, komentar ala Threads, composer, scroll/tap recovery, dan optimasi loading.

Versi Android: **1.9.7-lite (Build 17)**.

## File patch

- `app/build.gradle`
- `app/src/main/java/id/deapp/lite/MainActivity.java`
- `app/src/main/assets/deapp_native_v19.js`

Ekstrak ZIP ke root project `~/deapp-build/deapp-lite-android`, lalu commit dan push ke GitHub seperti versi sebelumnya.
