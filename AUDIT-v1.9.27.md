# Audit DeApp Lite Android v1.9.27

Versi: **1.9.27-lite**  
Build: **37**

## Perubahan

- Notifikasi memakai satu filter shell dua tingkat agar tab kategori (Semua, Sebutan, Balasan, Reaksi, Hadiah, Pengikut, Prediksi, Live, Sistem) tidak bentrok dengan filter status baca.
- Filter status `Semua` dan `Belum dibaca` berada pada baris tersendiri di bawah kategori dan mengikuti sticky container yang sama.
- Pengaturan Profil dipecah menjadi halaman induk dan tiga subhalaman mandiri: `Foto profil & Sampul`, `Info Profil`, dan `Username`.
- Tombol Back dari subhalaman Profil kembali ke halaman induk Profil, bukan langsung keluar dari Pengaturan.
- Halaman `Mode tampilan`, `Pengalaman aplikasi`, `Bahasa & terjemahan`, dan `Aksesibilitas` memindahkan aksi Simpan ke header kanan atas.
- Aksi simpan header memakai ikon check minimal dan `requestSubmit()` agar validasi HTML/form backend tetap berjalan.
- Tombol Simpan lama di bagian bawah form disembunyikan hanya pada empat halaman tersebut.
- Status disabled tombol Simpan pada backend (misalnya konfigurasi terjemahan belum siap) ikut diterapkan pada aksi Simpan header.

## Kompatibilitas

Tidak mengubah endpoint atau struktur backend. Seluruh form asli tetap digunakan dan route tambahan Profil memakai parameter query `sub` hanya untuk tampilan Android Lite.
