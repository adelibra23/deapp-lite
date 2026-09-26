# DeApp Lite Android v1.9.21 — Modal Back Guard, Single Alert & Telegram Chat Polish

**Versi:** 1.9.21-lite (Build 31)

Patch ini memperbaiki aksi Back ketika bottom sheet/modal aktif, membatasi alert popup tengah menjadi satu kartu, merapikan Chat Telegram dengan background putih dan composer media/emoji/voice note, memindahkan picker emoji ke panel bawah seperti Telegram, serta menonaktifkan long-press selection/copy callout pada tampilan aplikasi.

## Perubahan utama

- Back saat bottom sheet/modal aktif hanya menutup modal; halaman tidak pindah ke history sebelumnya.
- Alert/notifikasi popup di tengah layar dibuat single-instance sehingga tap berulang tidak menumpuk banyak popup.
- Body Chat light mode putih dengan bubble ringan.
- Composer Chat: media, emoji, voice note, input multiline, dan tombol kirim tertata rapi.
- Picker emoji Chat tampil sebagai panel keyboard di bawah composer; composer otomatis naik.
- Long-press menu salin/select pada konten WebView dinonaktifkan; input tetap dapat digunakan normal dan Story hold-to-pause tetap bekerja.
