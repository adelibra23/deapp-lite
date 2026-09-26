# Audit v1.9.20

## Perubahan
1. Back navigation route-aware untuk Notifikasi, Live, Toko, dan Settings.
2. `settings.php?tab=notifications` kembali ke `notifications.php`.
3. Filter status Notifikasi dipaksa keluar dari sticky/tab stacking context.
4. Refresh indicator menggunakan `R.drawable.deapp_logo` tunggal; indikator SwipeRefresh bawaan transparan.
5. Click capture pada `.post-actions .js-toggle-comments` mengarah ke permalink `post.php?id=...` di feed.

## Kompatibilitas
- Tidak mengubah endpoint/backend DeApp.
- Tidak mengubah handler reaction, submit komentar, Story, Chat, AI, atau modal.
