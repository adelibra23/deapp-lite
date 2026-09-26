# DeApp Lite Android v1.9.29 — Native-First Page Audit

Build: 39

## Audit halaman
Route yang sebelumnya paling mudah terlihat seperti WebView diaudit dan diberi native-first shell:
- messages list, notifications, live, AI, shop/wallet, settings/security/help/privacy;
- ASK, bookmarks, character/characters, community, connections;
- developer, games, hashtag, media, memories, DeApp Tap;
- top up, proof top up, Ads, admin, inbox, post activity, dan halaman ekspor.

## Native toolbar
- Header HTML section tetap dibuat secara internal sebagai controller aksi, tetapi tidak dirender.
- Toolbar Android menampilkan judul, Back, dan action icon.
- Action toolbar Android meneruskan klik ke controller tersembunyi sehingga route-aware Back, Top Up, settings, dan submit lama tidak perlu ditulis ulang.
- Chat thread dikecualikan karena memakai chat-head lawan bicara sendiri.

## Edit & save
- Pencarian form utama hanya memilih form non-modal/non-danger yang mempunyai satu aksi utama bertipe Simpan/Perbarui/Update/Ganti/Terapkan.
- Tombol submit bawah asli disembunyikan setelah dipromosikan ke toolbar.
- Submit memakai requestSubmit agar validasi HTML dan handler backend tetap berjalan.
- Berlaku pada subhalaman profil yang relevan, Custom Profile Studio, security page tertentu, dan halaman edit lain yang memenuhi aturan aman.

## Visual
- page-head desktop dan website footer dihilangkan dalam shell Android.
- side rail desktop disembunyikan pada generic native routes.
- card generik dibuat flat, field input/textarea/select dibuat lebih menyerupai kontrol aplikasi.
- body padding disesuaikan karena toolbar Android berada di luar WebView.

## Validasi
- versionCode 39 / versionName 1.9.29-lite
- node --check deapp_native_v19.js
- XML drawable parse
- MainActivity.java brace structure
- ZIP integrity
