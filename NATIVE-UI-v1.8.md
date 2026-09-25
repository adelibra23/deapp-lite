# Deapp Lite Android v1.8.0

Pembaruan ini fokus pada interaksi bottom sheet dan tampilan feed postingan.

## Bottom sheet scroll lock
- Saat bottom sheet berbasis halaman Deapp terbuka, posisi scroll halaman disimpan dan body dikunci sehingga background tidak dapat digeser.
- Drag/scroll tetap diizinkan di dalam isi sheet.
- Backdrop tidak meneruskan gesture ke halaman.
- Bottom sheet native Android juga memanggil lock yang sama ke WebView.
- Saat sheet ditutup, posisi halaman sebelumnya dipulihkan tanpa loncat.
- Pull-to-refresh tetap dinonaktifkan selama sheet aktif.

## Postingan thread-style
- Kartu posting diubah menjadi feed datar tanpa card/shadow.
- Avatar 40px dengan rail vertikal tipis untuk kesan percakapan/thread.
- Konten, media, poll, repost, source passport dan blok tambahan mengikuti kolom isi di kanan avatar.
- Media memakai sudut membulat dan jarak lebih rapat.
- Action posting menjadi icon-first tanpa label teks, sementara fungsi reaction, komentar, share, gift, bookmark tetap utuh.
- Statistik interaksi dipadatkan dan komentar tetap dapat dibuka seperti sebelumnya.
- Embedded/repost tetap mempunyai batas visual agar mudah dibedakan.

Versi aplikasi: `1.8.0-lite` (`versionCode 9`).
