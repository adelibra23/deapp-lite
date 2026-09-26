# Audit DeApp Lite Android v1.9.18

## Fokus perubahan

- Percakapan aktif tidak lagi tampil sebagai card/container terpisah. `layout`, `messenger`, dan `msg-chat` dipaksa full-width, tanpa border, radius, atau shadow.
- Body chat menggunakan kanvas bergaya Telegram dengan latar biru-abu lembut dan pola sangat halus; dark mode memakai latar biru gelap.
- Bubble pesan dibuat lebih compact. Pesan masuk berwarna putih/netral, pesan keluar beraksen biru muda; dark mode memakai pasangan warna Telegram-style biru gelap.
- Composer bawah dibuat ala Telegram: area dasar transparan, input putih/gelap berbentuk pill, tombol attachment netral, dan tombol kirim bulat biru.
- Quick reaction row pada composer disembunyikan untuk mengurangi kepadatan visual. Fitur inti pesan tetap memakai handler web DeApp asli.
- Label `Welcome` pada startup splash dihapus. Splash sekarang hanya menampilkan animasi logo DeApp selama durasi yang sama.

## Validasi

- JavaScript: `node --check`.
- XML drawable: parsing seluruh drawable XML.
- ZIP: `unzip -t`.
- Versi: `1.9.18-lite`, Build `28`, UA `DeappLite/1.9.18 NativeMobile/9.18`.
