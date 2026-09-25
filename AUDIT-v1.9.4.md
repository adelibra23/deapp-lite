# Audit DeApp Lite Android v1.9.4

## Temuan dan perbaikan

1. **Story tidak tampil**
   - Penyebab: patch v1.9 sebelumnya memaksa `.page-home .story-tray{display:none!important}`.
   - Perbaikan: Story tray diaktifkan kembali dan tetap mendukung swipe/scroll horizontal.

2. **Terjemahan postingan masih muncul**
   - Perbaikan: komponen hasil/aksi terjemahan di dalam `.post-card` disembunyikan dan dibersihkan dari DOM. Pengaturan bahasa umum di luar postingan tidak disentuh.

3. **Loading terasa lama**
   - Penyebab: startup memakai splash Welcome terpisah sebelum pengguna melihat halaman, lalu WebView masih memuat halaman.
   - Perbaikan: splash lama dihapus dari alur startup. WebView mulai load langsung dan loader hanya memakai logo DeApp non-blocking.
   - Tambahan: cache WebView, hardware rendering, offscreen preraster, lazy image di bawah viewport, async image decoding, preload metadata video, serta scan DOM yang didebounce.

4. **Ikon pesawat composer tidak mengirim**
   - Penyebab potensial: native toolbar hanya memanggil `click()` pada elemen dengan ID tertentu dan state composer bisa terlambat tersinkron.
   - Perbaikan: form dicari secara fleksibel, validasi dipertahankan, `requestSubmit()` dipakai bila tersedia, lalu fallback ke tombol submit asli. State native composer juga diaktifkan segera setelah composer berhasil dibuka.

5. **Risiko halaman tidak responsif**
   - Pengaman scroll-lock/backdrop v1.9.3 dipertahankan.
   - Sinkronisasi bridge session/chrome/composer sekarang hanya dikirim saat state berubah.
   - MutationObserver tidak lagi menjalankan scan per frame; perubahan DOM didebounce 64 ms.
   - Loader tidak clickable/focusable sehingga tidak dapat menyerap tap halaman.
   - Delayed loader memakai generation token agar overlay lama tidak muncul kembali setelah halaman sudah terlihat.

## Validasi statis

- `node --check` untuk `deapp_native_v19.js`: lulus.
- Pemeriksaan parsing Java dengan `javac` tidak menemukan error sintaks; error dependency Android normal bila file dikompilasi tanpa Android SDK/classpath.
- Kurung kurawal Java seimbang.
- Nomor versi disinkronkan ke `1.9.4-lite`, `versionCode 14`, `NativeMobile/9.4`.

## Catatan

Patch ini mengubah sisi **Android Lite/native shell**. Backend/server DeApp tidak diubah. Fitur Story menggunakan Story yang memang sudah disediakan aplikasi DeApp di server.
