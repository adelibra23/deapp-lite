# Audit DeApp Lite Android v1.9.12

## Fokus perubahan

- Navigasi bottom Android memakai ikon outline modern dan state aktif yang lebih bersih.
- Grid Menu Fitur DeApp memakai ikon native yang konsisten, bubble warna lebih rapi, dan spacing seragam.
- Toko & Dompet tidak lagi memakai footer navigasi khusus di bagian bawah.
- Daftar Chat tidak lagi memakai footer navigasi khusus di bagian bawah.
- Riwayat Chat dirombak menjadi list full-width bergaya WhatsApp: avatar lebih besar, separator inset, badge unread hijau, search dan filter yang lebih ringan.
- Body Chat dibuat bergaya WhatsApp dengan wallpaper lembut, bubble hijau untuk pesan sendiri, bubble netral untuk lawan bicara, dan dukungan dark mode.
- Pull-to-refresh memakai indikator ikon refresh outline dengan animasi rotasi halus.
- Refresh memakai `WebView.reload()` pada URL aktif sehingga query/hash dan halaman aktif dipertahankan tanpa menambah history baru.
- Pull-to-refresh diperluas ke halaman aman seperti Beranda, Profil, Notifikasi, Toko, Live, dan daftar Chat; dinonaktifkan pada form berat, chat aktif, Story, Reels, AI, dan halaman detail yang berisiko kehilangan input.

## Stabilitas

- Handler asli Chat (kirim, gambar, emoji, stiker, voice note, hadiah, reply) tidak diganti.
- Route Toko, Chat, dan subhalaman tetap memakai URL/backend DeApp yang sama.
- JavaScript lolos `node --check`.
- Seluruh XML drawable baru lolos parsing XML.
- Struktur kurung Java seimbang dan `javac` mencapai tahap resolusi Android imports tanpa error sintaks awal.

## Versi

- `versionCode`: 22
- `versionName`: `1.9.12-lite`
