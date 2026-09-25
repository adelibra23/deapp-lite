# DeApp Lite Android — v1.9.3 Full Stability Fix

Patch ini berfokus pada stabilitas interaksi aplikasi Android DeApp Lite setelah audit v1.9.2.

## Perbaikan utama

- Memperbaiki tab/tombol yang kadang tidak merespons karena gesture patch terlalu agresif.
- Mengubah `touch-action` tab agar scroll vertikal halaman tetap bekerja saat jari mulai menyentuh area tab horizontal.
- Mengurangi intervensi global pada klik/touch halaman agar handler asli DeApp tidak terganggu.
- MutationObserver sekarang di-throttle maksimal satu kali per animation frame untuk mencegah scan DOM berlebihan dan lag/interaksi tersendat.
- Menghapus recovery handler pada setiap `pointerdown/touchstart` yang berpotensi mengubah DOM tepat sebelum event klik diproses.
- Kolom komentar tidak lagi mengganti elemen `<input>` asli menjadi `<textarea>` sehingga listener submit/mention/upload milik DeApp tidak hilang.
- Auto-grow komentar tetap digunakan bila server memang sudah menyediakan `<textarea>`.
- SwipeRefreshLayout hanya aktif di Home/Feed agar tidak merebut gesture pada form, profil, pengaturan, detail postingan, pesan, dan tab lainnya.
- Overlay/backdrop hidden dipastikan tidak menerima pointer event.
- Overlay native lama dibersihkan saat WebView dibuat ulang atau server diganti.
- Pending file chooser dan WebRTC permission request dibersihkan saat WebView dihancurkan.
- State geolocation lama dibersihkan saat WebView dihancurkan.
- Scroll-lock dan backdrop self-recovery dari v1.9.1/v1.9.2 tetap dipertahankan.
- Bottom sheet native tetap memiliki fallback removal agar lapisan transparan tidak tertinggal di atas WebView.
- Nomor versi di User-Agent, menu Tentang, dan halaman Pengaturan disinkronkan.

## Versi

- versionCode: **13**
- versionName: **1.9.3-lite**
- NativeMobile: **9.3**

## File patch

- `app/build.gradle`
- `app/src/main/java/id/deapp/lite/MainActivity.java`
- `app/src/main/assets/deapp_native_v19.js`

Ekstrak ZIP ini ke root project `~/deapp-build/deapp-lite-android`, kemudian commit dan push ke GitHub seperti versi sebelumnya.
