# Deapp Lite Android v1.7.0

Fokus rilis ini adalah stabilitas gesture dan loading agar shell Android terasa lebih native.

- Pull-to-refresh memakai satu spinner Android saja. Animasi logo Deapp pada gesture refresh dihapus.
- SwipeRefreshLayout otomatis dinonaktifkan ketika native bottom sheet, web bottom sheet, composer penuh, atau fullscreen media sedang aktif.
- Bottom sheet web melaporkan status buka/tutup ke Android melalui `DeappNative.syncWebSheetState(...)`, sehingga gesture tarik turun sheet tidak memicu refresh halaman.
- Garis progress horizontal di bagian atas layar dihapus sepenuhnya.
- Navigasi halaman biasa menampilkan spinner loading di tengah layar.
- Saat refresh dengan tarik dari atas, hanya spinner refresh yang tampil; spinner tengah tidak ditampilkan bersamaan.
- Loading dan refresh selalu dihentikan dengan aman ketika halaman selesai atau terjadi error jaringan.
- Versi aplikasi: `1.7.0-lite` (versionCode 8).

Tidak ada perubahan database Deapp.
