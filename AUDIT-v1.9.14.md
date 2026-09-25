# Audit DeApp Lite Android v1.9.14

## Bottom navigation

Pada v1.9.13, halaman Notifikasi sudah dikecualikan dari section footer khusus, tetapi Chat masih dianggap sebagai section yang memiliki chrome bawah sendiri. v1.9.14 mengubah kepemilikan footer sehingga Notifikasi dan Chat menggunakan bottom navigation umum Android. Profil tetap menggunakan chrome native umum seperti sebelumnya. Keyboard tetap dapat menyembunyikan bottom navigation sementara agar composer pesan tidak tertutup.

Ikon navigation item non-primary dinaikkan dari container 40dp/padding 9dp menjadi 42dp/padding 7dp sehingga glyph terlihat lebih besar tetapi tinggi bottom bar tetap stabil.

## Statistik postingan

Sumber angka tetap berasal dari `.post-stats` asli DeApp dan dipindahkan secara visual ke action button. CSS v1.9.14 mengecilkan angka menjadi 11px, mengurangi gap ikon-angka menjadi 3px, dan menghilangkan kesan pill besar. Tidak ada label teks tambahan.

## Notifikasi

Wrapper `.notif-card` dibuat transparan dan tanpa border/radius. `.notif-item` menjadi baris flat dengan separator setelah area avatar. Unread state memakai warna aksen tipis. `.notif-side` disembunyikan agar pengalaman mobile fokus pada tab filter dan daftar utama. Search/filter tetap tersedia dan tampil tanpa card.

## Profil

Aksi kanan header profil memakai drawable baru `ic_native_more_vertical.xml`, sedangkan perilaku menu opsi profil tetap sama.

## Splash

Splash tetap berjalan sekitar lima detik sementara WebView memuat di belakang. Logo sekarang dimulai dari skala kecil + rotasi ringan, masuk dengan OvershootInterpolator, settle ke ukuran normal, lalu melakukan breathing animation halus. Label `Welcome` muncul 330ms setelah animasi dimulai dengan fade dan translationY.

## Versi

- `versionCode`: `24`
- `versionName`: `1.9.14-lite`
- User agent native: `DeappLite/1.9.14 NativeMobile/9.14`
