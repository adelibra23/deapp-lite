# Audit DeApp Lite v1.9.26

Versi: **1.9.26-lite (Build 36)**

## Page transition
- `WebView` diberi state awal alpha 0.70, translateY 7dp, scale 0.995 pada navigasi non-refresh.
- `onPageCommitVisible` memainkan reveal 235ms dengan decelerate curve.
- Loader lambat ditunda dari 120ms menjadi 360ms untuk menghindari flash pada navigasi cepat.

## Pull-to-refresh
- `CircleImageView` bawaan `SwipeRefreshLayout` tetap tidak digambar.
- Subclass `DeappSwipeRefreshLayout` membaca gerakan drag dan menampilkan indikator custom selama pull, tidak hanya setelah threshold refresh tercapai.
- Ikon memakai `ic_native_refresh`, mengikuti warna theme, berotasi selama refresh, dan memiliki minimum visible time 520ms.
- Preview pull memiliki alpha/scale/translation progresif agar terasa seperti native social app.

## Bottom sheet scrim
- Native bottom sheet: overlay `#6B000000` (~42%). Tidak ada foreground tambahan pada native header agar tidak double-dim.
- Web bottom sheet: `deapp-web-sheet-active` menambahkan satu pseudo scrim 42% di seluruh WebView, termasuk web header.
- Native Android header memakai foreground 42% hanya ketika `webSheetOpen=true`.
- Modal/backdrop web dibuat transparan sebagai click-catcher; visual dim berasal dari unified scrim tunggal.
- Filter brightness header lama dioverride agar header dan body memiliki tingkat redup yang sama.

## Validasi
- JavaScript diperiksa dengan `node --check`.
- Struktur kurung Java diperiksa seimbang.
- ZIP diuji dengan `unzip -t`.
