# Audit v1.9.21

- Back saat native bottom sheet aktif hanya menutup sheet dan tidak menavigasi history.
- Back saat web bottom sheet/modal aktif memanggil `closeVisibleSheet()` dan selalu mengonsumsi aksi Back.
- Header Back pada section Pengaturan/Notifikasi juga menutup sheet lebih dulu bila sheet masih aktif.
- Popup `.nx-pop-toasts` dibatasi maksimal satu alert terlihat; item lama dibuang agar tap berulang tidak menumpuk kartu.
- Chat aktif memakai canvas putih (light mode), composer Telegram solid, dan hanya tombol media, emoji, voice note yang ditampilkan sebagai opsi utama.
- Picker emoji chat diposisikan fixed di bawah composer seperti panel keyboard Telegram; composer otomatis naik sesuai tinggi panel.
- Long-press selection/copy callout dinonaktifkan pada WebView dan konten non-input, tanpa mengganggu gesture tahan Story.
- Dark mode Chat/emoji panel tetap memiliki warna khusus.
