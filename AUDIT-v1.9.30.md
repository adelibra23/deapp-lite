# DeApp Lite v1.9.30 — Auth & Registration Onboarding Audit

## Versi
- versionCode: 40
- versionName: 1.9.30-lite
- User-Agent: DeappLite/1.9.30 NativeMobile/9.30

## Perubahan
- Cold start selalu membuka `login.php`. Jika cookie/sesi masih valid, backend DeApp otomatis mengalihkan ke `index.php`; jika sesi tidak valid, layar Masuk langsung menjadi halaman pertama.
- App Shortcut tidak dijalankan sebelum sesi login valid.
- `register.php` diubah di layer Lite menjadi wizard 3 langkah tanpa mengubah backend:
  1. Nama tampilan + email.
  2. Kata sandi + konfirmasi.
  3. Username + submit akun.
- Validasi HTML5 tetap berjalan per langkah dan validasi backend/CSRF/rate limit tetap menggunakan `register.php` asli.
- Error backend mengarahkan wizard ke langkah yang relevan berdasarkan jenis error.
- Registrasi sukses tetap auto-login menggunakan backend asli.
- Setelah redirect registrasi sukses ke Beranda, JS memanggil bridge native `registrationWelcome()` satu kali.
- Android menampilkan overlay animasi native berisi logo DeApp, `Welcome to DeApp`, dan teks sambutan sebelum feed kembali terlihat.
- Auth page dipoles native-first: brand-side desktop disembunyikan, form mobile dipusatkan, input/button/tabs dibuat lebih ringan, footer web disembunyikan.

## Kompatibilitas
- Tidak ada perubahan skema database.
- Tidak ada perubahan endpoint PHP.
- Login 2FA tetap memakai UI/backend asli.
- Social login pada halaman Login tetap tersedia; pada wizard Register disembunyikan agar onboarding tidak bercampur dengan langkah data akun.
