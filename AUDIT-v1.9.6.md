# Audit DeApp Lite Android v1.9.6

## Section chrome

Patch menambahkan page-type khusus untuk `messages`, `notifications`, `live`, `ai`, `shop`, `settings`, dan state dinamis `story`. Android menyembunyikan toolbar, bottom navigation, dan FAB generik ketika section tersebut aktif sehingga tidak terjadi dua lapis navigasi.

## Story

Viewer Story tetap memakai DOM dan handler asli DeApp. Patch hanya memperbarui visual progress, topbar, close, reply, reaction, dan bottom sheet. State `hidden` viewer dipantau agar system bar dan chrome Android kembali normal setelah Story ditutup.

## Chat

Pada daftar pesan, header/footer khusus menyediakan akses cepat ke Chat, Orang, Notifikasi, dan Pengaturan. Ketika `messages.php?c=...` aktif, chrome buatan tidak ditumpuk di atas chat: `.chat-head` asli menjadi header dan `.chat-compose` asli menjadi footer, sehingga upload gambar, emoji, stiker, hadiah, voice note, multiline input, dan submit tetap memakai handler DeApp.

## Notifikasi

Header membaca filter `f`. Footer menyediakan shortcut Semua, Sebutan, Balasan, Reaksi, dan Sistem. Tab web asli disembunyikan di mobile chrome agar navigasi tidak duplikat.

## Live

Route `live.php` mencakup discover, following, mine, `studio=1`, dan `id=...`. Header/footer tetap kontekstual di semua state tersebut tanpa mengganti fungsi player, chat Live, atau Creator Studio.

## DeApp AI

Footer khusus mem-proxy tombol `.deapp-ai-nav` asli, bukan membuat state AI baru. Karena itu Chat AI, AI Tools, Kreator, dan Pustaka tetap memakai JavaScript/endpoint DeApp. Tombol header Chat Baru memicu `.js-ai-new-chat` asli.

## Toko & Dompet

Judul header dipetakan dari `tab` aktif. Footer mengelompokkan banyak tab toko menjadi lima area utama sehingga subtab seperti kirim koin, promo, wishlist, virtual, pet, tiket, tas, tema, bingkai, stiker, efek, dan level tetap punya konteks navigasi.

## Pengaturan dan subhalaman

Selain `settings.php?tab=...`, patch mencakup `security.php`, `security-password.php`, `security-email.php`, `security-wallet-pin.php`, `security-2fa.php`, `security-sessions.php`, `help.php`, `privacy.php`, `policy.php`, `cookies.php`, dan `profile-edit.php`.

## Validasi statis

- `node --check app/src/main/assets/deapp_native_v19.js` lulus.
- Kurung kurawal `MainActivity.java` seimbang.
- Nomor versi disinkronkan ke `1.9.6-lite`, Build `16`, `NativeMobile/9.6`.
