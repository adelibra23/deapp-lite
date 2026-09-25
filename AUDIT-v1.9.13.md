# Audit DeApp Lite Android v1.9.13

## Postingan

Statistik reaction, komentar, dan bagikan sebelumnya berada pada bar ringkasan terpisah di atas action bar. Patch v1.9.13 membaca nilai dari elemen statistik asli lalu menampilkannya pada tombol aksi terkait. Elemen statistik asli ditandai sebagai dipindahkan, bukan dihapus dari backend, sehingga update AJAX DeApp tetap menjadi sumber data utama.

Jika sebuah postingan memiliki statistik lain seperti cabang, statistik tersebut tetap dapat tampil pada bar ringkasan. Bar ringkasan hanya disembunyikan bila semua statistik yang tersisa sudah dipindahkan.

## Header Profil

Header native Android tetap menjadi satu-satunya header Profil. Judul di tengah sekarang mengambil handle `.profile-username` (`@username`) dengan fallback ke display name. Logo DeApp tetap berada di kiri dan menu opsi profil tiga titik tetap di kanan.

## Notifikasi

Footer kategori Notifikasi khusus dihapus. Tab filter asli `notifications.php` ditampilkan kembali, dipindahkan ke atas form filter, dibuat horizontal-scroll, dan tab aktif otomatis diposisikan mendekati tengah.

Bottom navigation umum Android sekarang tetap tampil pada halaman Notifikasi. Header Notifikasi khusus di WebView tetap dipakai agar judul dan tombol Preferensi Notifikasi tidak bertumpuk dengan toolbar Android.

## Validasi

- JavaScript diperiksa menggunakan `node --check`.
- Struktur kurung Java seimbang setelah patch.
- `versionCode`: `23`.
- `versionName`: `1.9.13-lite`.
