# Audit DeApp Lite Android v1.9.17

## Fokus audit

1. Feedback reaction postingan.
2. Toast/notifikasi popup yang sebelumnya muncul di bawah.
3. Header search dan halaman explore.
4. Alignment ikon/count action postingan.
5. Story tap, long-press pause, dan composer komentar.

## Perbaikan

- NativeBridge menambah `reactionFeedback()` untuk haptic + ToneGenerator pendek.
- Dua sistem popup (`#toast` dan `.nx-pop-toasts`) dipusatkan dengan card/blur yang konsisten.
- Header normal memakai ikon Search; Composer dan detail posting tetap memakai Back.
- `explore.php` mendapat layout search mobile yang lebih flat.
- Count reaction/comment/share/gift menempel pada ikon, ukuran lebih kecil, dan action row disejajarkan.
- Story memakai timer bridge yang membungkus timeout `nextStory`, sehingga long-press dapat pause/resume timer asli tanpa memutus tap navigation.
- Click setelah long-press disuppress agar tidak tidak sengaja pindah Story.
- Composer Story dibuat lebih bersih seperti Instagram Stories.

## Validasi

- `node --check` untuk JavaScript: lulus.
- Brace Java seimbang.
- `versionCode 27`, `versionName 1.9.17-lite`.
