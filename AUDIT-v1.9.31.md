# Audit DeApp Lite v1.9.31

## Native notice
- Popup/toast HTML DeApp tidak lagi terlihat di APK.
- Pesan toast diteruskan melalui JavascriptInterface ke overlay Android native di tengah layar.
- Maksimal satu notice native aktif; notice baru menggantikan notice lama.
- Gaya netral ala Threads: surface card, border tipis, ikon tone kecil, judul dan pesan, fade/scale halus.

## Semua fitur DeApp
- Menu fitur tetap bottom sheet Android native.
- Tile diberi label kategori, subteks, ikon berwarna dan motion ringan.
- Grid dapat discroll pada layar kecil.

## Toko & Koleksi
- Dompet & Koin tetap terpisah sebagai empat aksi utama.
- Toko & Koleksi hanya menampilkan Etalase, Pet, Item Virtual dan VIP.
- Menu sisanya dibuka lewat tombol `Menu lainnya`.
- `Menu lainnya` memanggil bottom sheet Android native melalui bridge.
- Card/menu web dibuat lebih flat agar tidak terlihat seperti komponen browser.

## Versi
- versionCode 41
- versionName 1.9.31-lite
