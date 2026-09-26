# Audit DeApp Lite v1.9.25

Versi: **1.9.25-lite (Build 35)**

## 1. Launcher App Shortcuts

Ditambahkan `android.app.shortcuts` pada `MainActivity` dan `res/xml/shortcuts.xml`. Empat shortcut statis:

- **Postingan** → `deapp://shortcut/post` → Beranda lalu buka composer asli.
- **Video** → `deapp://shortcut/video` → `reels.php`.
- **Cerita** → `deapp://shortcut/story` → Beranda lalu klik `.story-add` / modal `story-modal`.
- **Chat** → `deapp://shortcut/chat` → `messages.php`.

`MainActivity` menggunakan `launchMode=singleTop` dan `onNewIntent()` supaya shortcut tetap diproses saat aplikasi sudah hidup. Jika server belum diatur, action shortcut disimpan dan dijalankan setelah server tersedia.

## 2. Suara kirim Chat

Bridge baru `DeappNative.chatSent()` memanggil `ToneGenerator` pada `STREAM_MUSIC` dengan bunyi pendek. Listener composer Telegram Lite menunggu textarea/file kembali kosong setelah submit sebelum memicu suara, sehingga suara tidak diputar hanya karena tombol kirim ditekan pada payload yang gagal diproses. Ada cooldown native untuk mencegah bunyi ganda.

## 3. Validasi

- JavaScript diperiksa dengan `node --check`.
- Android XML (`AndroidManifest.xml`, `shortcuts.xml`, `strings.xml`, drawable XML) diperiksa sebagai XML valid.
- Struktur brace Java diperiksa seimbang.
- ZIP diuji dengan `unzip -t`.
