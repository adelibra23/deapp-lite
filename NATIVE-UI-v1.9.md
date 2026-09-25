# DeApp Lite Native UI — v1.9.7

- Bottom navigation otomatis hilang selama native/web bottom sheet atau modal aktif.
- Footer section web ikut hilang saat sheet aktif supaya tidak bertumpuk dengan modal.
- Header diberi dim ringan saat sheet aktif, menyerupai treatment modal Threads.
- Story sheet dan Video Pendek comments/more sheet ikut terdeteksi sebagai sheet aktif.
- Floating composer ikut disembunyikan saat modal aktif.
- Splash pembuka full-screen menampilkan animasi logo DeApp selama sekitar 5 detik.
- WebView tetap melakukan loading di belakang splash untuk mengurangi waktu tunggu setelah splash selesai.
- Section chrome v1.9.6 dan seluruh stability/performance fix sebelumnya tetap aktif.

Versi: **1.9.7-lite · Build 17 · NativeMobile/9.7**.


## v1.9.9 — DeApp AI Navigation

- Footer navigasi khusus DeApp AI dihapus.
- Header DeApp AI memakai menu burger di kanan atas.
- Burger memuat Chat AI, AI Tools, Kreator, dan Pustaka, tetapi tetap memicu navigasi asli `.deapp-ai-nav`.
- Tombol Chat Baru dipindah ke floating action button DeApp AI di kanan bawah.
- Composer `#ai-chat-form` dibuat tetap pada bawah viewport ketika view Chat AI aktif.
- FAB otomatis naik di atas composer pada Chat AI dan tersembunyi saat bottom sheet aktif.
- View aktif disimpan pada `data-deapp-ai-view` di elemen root untuk styling kontekstual.


## v1.9.10 — Home & Reaction Visual Refresh

- Menu fitur native memakai ikon berwarna per kategori.
- Ringkasan harian Beranda disembunyikan agar Story lebih cepat terlihat.
- Feed tabs diperbesar sedikit dan diberi aksen warna.
- Reaction postingan/QA memberi haptic feedback melalui native bridge.
