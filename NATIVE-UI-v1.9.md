# Native UI v1.9.3 Stability Patch

Patch v1.9.3 memprioritaskan fungsi asli DeApp: native shell hanya mengubah presentasi dan state yang diperlukan tanpa mengambil alih event klik/submit halaman secara global. Gesture tab tetap horizontal namun tidak lagi memblokir scroll vertikal. Observer DOM di-throttle, comment node tidak diganti, dan lifecycle WebView membersihkan overlay serta callback lama.
