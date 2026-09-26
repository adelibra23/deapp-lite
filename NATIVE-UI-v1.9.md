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


## v1.9.17 — Reaction Audio + Threads Search + Story Hold

- Header umum: ikon kiri menjadi Search dan menuju `explore.php`.
- Explore dipoles seperti halaman pencarian Threads.
- Reaction: haptic + audio native pendek.
- Toast/pop notification: dipusatkan.
- Post action row: compact reaction/comment/gift/share counts.
- Story: tap kiri/kanan + press-hold pause/resume timer secara nyata.
- Story composer: visual lebih dekat Instagram Stories.
- Versi: **1.9.17-lite · Build 27 · NativeMobile/9.17**.

## v1.9.18 — Telegram Chat Body + Clean Splash

- Percakapan aktif full-width tanpa body card.
- Bubble/chat canvas dan composer bawah dipoles bergaya Telegram.
- Quick reaction composer disembunyikan untuk UI yang lebih bersih.
- Splash screen hanya menampilkan logo DeApp; label Welcome dihapus.
- Versi: **1.9.18-lite · Build 28 · NativeMobile/9.18**.

## v1.9.19 — Telegram Composer, Notification Status & Clean Bio

- Percakapan aktif menyembunyikan bottom navigation Android; composer chat menjadi kontrol bawah utama.
- Composer Telegram adaptif: opsi media/stiker/emoji/voice/gift disembunyikan saat ada teks, textarea auto-grow 46–138 px.
- Kolom pencarian Notifikasi disembunyikan; filter status dipertahankan dan tiap item diberi label Dibaca/Belum dibaca.
- UI terjemahan bio pada Profil dihapus dari DeApp Lite, sementara bio asli tetap tampil.
- Versi: **1.9.19-lite · Build 29 · NativeMobile/9.19**.


## v1.9.20 — Navigation, Notification Status, Refresh & Comment Routing

- Build **30**, versi **1.9.20-lite**, UA `NativeMobile/9.20`.
- Back route section dibuat parent-aware agar tab/filter tidak membingungkan.
- Pengaturan Notifikasi kembali ke `notifications.php`.
- Status Dibaca/Belum dibaca dipisahkan dari kategori Notifikasi.
- Pull-to-refresh menggunakan satu logo DeApp sebagai indikator unik.
- Ikon komentar pada feed membuka detail postingan langsung.

## v1.9.21 — Back Guard, Single Alert, Telegram Emoji Panel & No Selection Callout

- Build **31**, versi **1.9.21-lite**, UA `NativeMobile/9.21`.
- Back menutup bottom sheet/modal terlebih dahulu tanpa berpindah ke history sebelumnya.
- Alert tengah dibatasi satu instance agar tap berulang tidak menumpuk toast.
- Body chat light mode putih; composer Telegram dirapikan menjadi media + emoji + voice note + input + kirim.
- Picker emoji chat tampil sebagai panel keyboard di bawah composer.
- Long-press callout/selection copy pada konten aplikasi dinonaktifkan.


## v1.9.22 — Stable Header Back, Full Sheet Dim & Page-ready FAB

- Build **32**, versi **1.9.22-lite**, UA `NativeMobile/9.22`.
- Header Back tidak melewati bottom sheet/modal aktif.
- Chat detail dan section tertentu memakai parent-aware back agar tidak memutar history tab/filter.
- Header Android dan header web ikut redup ketika sheet aktif.
- FAB plus Beranda hanya muncul setelah `onPageCommitVisible`/`onPageFinished` dan splash startup selesai.
