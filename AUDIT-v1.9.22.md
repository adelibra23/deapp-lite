# Audit v1.9.22

## Temuan

1. FAB Beranda dapat memenuhi kondisi `homePage` sebelum frame halaman final terlihat.
2. Header Android hanya diberi foreground redup saat `webSheetOpen`, belum saat native sheet aktif.
3. Beberapa Back header masih mengandalkan history generik sehingga sub-route dapat terasa memutar.

## Perbaikan

- Menambahkan `pageReadyForChrome` dan menahan FAB selama startup splash aktif.
- `pageReadyForChrome` di-reset pada `onPageStarted` dan diaktifkan saat `onPageCommitVisible`/`onPageFinished`.
- Native/web sheet memakai satu status `sheetActive` untuk meredupkan header Android.
- Header web khusus diberi dim yang lebih nyata dan konsisten.
- Chat detail diarahkan ke `messages.php`; Pengaturan Notifikasi diarahkan kembali ke `notifications.php`; Reels memakai handler Back section yang sama.
- Header native memprioritaskan penutupan sheet sebelum aksi Back/Search.
