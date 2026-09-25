# DeApp Lite Native UI — v1.9.4

- Story tray Beranda diaktifkan kembali.
- Terjemahan postingan dihilangkan dari UI Android Lite.
- Loader memakai logo DeApp dengan animasi pulse yang ringan.
- Startup tidak lagi ditahan splash Welcome lama.
- Composer publish diperbaiki menggunakan form submit native (`requestSubmit`) dan fallback aman.
- Scan DOM dan bridge Android diturunkan frekuensinya untuk mengurangi lag.
- Cache/render WebView diperbaiki tanpa mengubah server DeApp.

---

# Native UI v1.9.3 Stability Patch

Patch v1.9.3 memprioritaskan fungsi asli DeApp: native shell hanya mengubah presentasi dan state yang diperlukan tanpa mengambil alih event klik/submit halaman secara global. Gesture tab tetap horizontal namun tidak lagi memblokir scroll vertikal. Observer DOM di-throttle, comment node tidak diganti, dan lifecycle WebView membersihkan overlay serta callback lama.
