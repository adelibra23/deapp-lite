# Deapp Lite Native UI v1.9

v1.9 memoles alur mobile agar lebih stabil dan lebih dekat dengan pola aplikasi native/Threads.

## Perubahan utama

- Bottom sheet native dibatasi tinggi layar, kontennya dapat discroll sendiri, dan gesture tarik-turun hanya dimulai dari area handle/header. Scroll halaman WebView di belakang tetap dikunci selama sheet terbuka.
- Bottom sheet berbasis web memakai aturan gesture yang sama: drag hanya dari bagian atas sheet supaya isi sheet tidak berebut gesture dengan penutupan sheet.
- Loading navigasi tidak langsung menutupi halaman. Spinner tengah baru tampil bila navigasi benar-benar lambat dan disembunyikan sejak konten pertama sudah terlihat. Cache WebView dan preraster diaktifkan bila didukung.
- Splash Welcome dipercepat tanpa menghilangkan animasi pembuka.
- Menu Ganti server dan Perizinan aplikasi dipindahkan ke halaman Pengaturan. Menu fitur utama hanya menyisakan pintasan fitur dan Pengaturan.
- Layout WebView dikunci ke lebar viewport mobile dan overflow horizontal halaman utama dicegah.
- Story tray di bagian atas feed disembunyikan pada Deapp Lite.
- Tombol plus posting mengambang tersedia pada semua halaman normal setelah login, kecuali halaman auth, composer, detail postingan, fullscreen, atau ketika keyboard aktif.
- Composer memakai tombol kembali/batal di kiri, judul Buat postingan di tengah, dan tombol terbit di kanan.
- Detail posting memakai tombol kembali di kiri, nama penulis di tengah, dan sisi kanan header kosong. Bottom navigation native disembunyikan pada halaman ini.
- Bagian metadata Diterbitkan serta rail Analitik, Profil penulis, postingan lain, dan Sedang tren disembunyikan pada detail posting mobile.
- Kolom komentar detail posting dipindahkan menjadi composer tetap di bagian bawah seperti Threads. Input visual berupa textarea auto-grow sampai beberapa baris, sedangkan input asli tetap disinkronkan agar API komentar Deapp lama tetap kompatibel.
- Saat mulai mengetik komentar, kontrol tambahan seperti jenis komentar dan emoji disembunyikan agar kolom teks mendapat ruang lebih besar. Tombol kirim tetap tersedia.

## Versi

- versionCode: 10
- versionName: 1.9.0-lite
