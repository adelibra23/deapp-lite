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

## v1.9.11 — WhatsApp Story, Settings & Dark UI

- Story tray memakai aksen hijau untuk story belum dilihat dan viewer dibuat full-screen seperti Status WhatsApp.
- Progress, identitas pengguna, media, area tap navigasi, reply bar, reaction dan sheet komentar disusun ulang tanpa mengganti handler backend Story.
- Footer khusus Pengaturan dihapus; header Pengaturan hanya Back + judul tengah.
- Visual Pengaturan dirapikan dengan chip kategori dan section yang lebih ringan.
- Theme `html.is-dark` disinkronkan ke palet native Android melalui `syncTheme`.
- Floating/action button disembunyikan ketika scroll dan kembali setelah scroll berhenti.
- Tombol compose/post hanya aktif pada Beranda.
- Kartu postingan mendapat penyempurnaan tipografi, media, stats dan action spacing.

Versi: **1.9.11-lite · Build 21**.

## v1.9.12 — Professional Icons, WhatsApp Chat & Refresh

- Bottom navigation memakai ikon outline baru dan active-state yang lebih bersih.
- Menu fitur DeApp memakai ikon native outline yang konsisten.
- Toko & Dompet tidak memakai footer navigasi khusus.
- Daftar Chat tidak memakai footer navigasi khusus.
- Riwayat Chat dan body Chat dipoles ala WhatsApp.
- Refresh memakai indikator ikon profesional dan mempertahankan URL aktif.

Versi: **1.9.12-lite · Build 22**.

## v1.9.13 — Post Counts, Profile Header & Notification Tabs

- Angka reaction, komentar, dan bagikan diposisikan langsung di samping ikon aksi masing-masing.
- Ringkasan statistik lama otomatis disederhanakan setelah angka dipindahkan.
- Header Profil: logo DeApp kiri, `@username` tengah, tiga titik kanan.
- Notifikasi memakai bottom navigation umum Android.
- Navigasi kategori Notifikasi dipindahkan ke tab horizontal-scroll di atas konten.
- Tab memakai link/filter asli DeApp agar seluruh kategori backend tetap berfungsi.

Versi: **1.9.13-lite · Build 23 · NativeMobile/9.13**.


## v1.9.14 — Common Bottom Navigation, Flat Notifications & Welcome Splash

- Notifikasi, Chat/Pesan, dan Profil menggunakan bottom navigation umum Android.
- Ikon bottom navigation sedikit diperbesar.
- Counter reaction/komentar/bagikan diperkecil dan ditempel dekat ikon masing-masing.
- Notifikasi menjadi flat list tanpa card besar, dengan tab filter horizontal di bagian atas.
- Header Profil menggunakan ikon titik tiga vertikal.
- Splash awal menampilkan label Welcome dan animasi logo scale/overshoot lalu breathing halus.

Versi: **1.9.14-lite · Build 24 · NativeMobile/9.14**.


## v1.9.15 — Persistent 5-Item Bottom Navigation

- Bottom navigation umum selalu menampilkan lima menu saat bar aktif: **Beranda · Chat · + Postingan · Notifikasi · Profil**.
- Tombol `+` pada bottom navigation tetap tersedia dari Beranda, Chat, Notifikasi, dan Profil.
- Hanya tombol plus mengambang/FAB di pojok kanan bawah yang dibatasi tampil di Beranda.
- Perilaku keyboard, modal/bottom sheet, dan halaman immersive tetap dapat menyembunyikan bottom bar sementara sesuai konteks.

Versi: **1.9.15-lite · Build 25 · NativeMobile/9.15**.

## v1.9.16 — Settings List + Focused DeApp AI

- Pengaturan utama menjadi daftar menu vertikal dengan Logout akun di posisi paling bawah.
- Tab pengaturan dibuka sebagai subhalaman fokus dengan header kembali + judul.
- DeApp AI dibersihkan menjadi pengalaman chat: tanpa FAB, burger, subtitle, pin/trash, thread rail, dan control strip.
- Composer DeApp AI diperbarui menjadi composer fixed bawah yang lebih profesional.
- Versi: **1.9.16-lite · Build 26 · NativeMobile/9.16**.
