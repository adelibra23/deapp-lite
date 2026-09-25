# DeApp Lite Android v1.9.6 — Section Chrome

Patch ini melanjutkan v1.9.5 dan memberi header/footer khusus untuk bagian utama aplikasi beserta subhalamannya.

## Perubahan utama

- **Story/Cerita** memakai chrome fullscreen dari viewer asli DeApp: progress, identitas pengguna, tombol tutup, reply, reaction, komentar, dan penonton ditata ulang agar lebih native.
- **Chat**: daftar percakapan memiliki header/footer khusus. Saat percakapan dibuka, header lawan bicara dan composer pesan asli menjadi chrome khusus agar fitur hadiah, opsi, voice note, gambar, emoji, stiker, dan kirim tetap bekerja.
- **Notifikasi**: header kontekstual mengikuti filter dan footer cepat untuk Semua, Sebutan, Balasan, Reaksi, dan Sistem.
- **Live**: header/footer khusus untuk Jelajah Live, Mengikuti, Creator Studio, Live Saya, dan ruang Live.
- **DeApp AI**: header khusus dengan aksi Chat Baru dan footer yang tersinkron dengan Chat AI, AI Tools, Kreator, dan Pustaka.
- **Toko & Dompet**: judul header mengikuti tab aktif; footer mengelompokkan Dompet, Etalase, VIP, Hadiah, dan Koleksi.
- **Pengaturan + subhalaman**: header mengikuti halaman aktif dan footer cepat Akun, Tampilan, Privasi, Keamanan, dan Bantuan. Termasuk Security Center, sandi, email, PIN dompet, 2FA, sesi/perangkat, Help, Privacy, Policy, Cookie, dan Profile Studio.
- Header/footer Android generik disembunyikan otomatis pada section di atas supaya tidak terjadi navigasi ganda.
- System bar Story/Reels memakai ikon terang untuk kontras dengan latar gelap.
- Semua perbaikan v1.9.5 tetap dipertahankan: preview posting 250 karakter, Reels khusus, komentar ala Threads, Story aktif, composer publish, loader logo, optimasi performa, dan recovery scroll/tap.

Versi Android: **1.9.6-lite (Build 16)**.

## File patch

- `app/build.gradle`
- `app/src/main/java/id/deapp/lite/MainActivity.java`
- `app/src/main/assets/deapp_native_v19.js`

Ekstrak ZIP ke root project `~/deapp-build/deapp-lite-android`, lalu commit dan push ke GitHub seperti versi sebelumnya.
