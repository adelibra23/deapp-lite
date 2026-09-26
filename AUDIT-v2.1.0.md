# Audit DeApp Android 2.1.0

## Native-only runtime
- [x] `applicationId id.deapp.app`
- [x] `namespace id.deapp.app`
- [x] WebView assets excluded from build
- [x] legacy `id/deapp/lite/**` source excluded from build
- [x] no `android.webkit` imports
- [x] no WebView/WebViewClient/WebChromeClient runtime
- [x] UI built with Android View classes

## Store migration
- [x] Toko & Dompet is a native parent dashboard
- [x] wallet detail native screen
- [x] top up native screen
- [x] send coin native screen
- [x] promo native screen
- [x] storefront native screen
- [x] pet / virtual / VIP native screens
- [x] Menu lainnya is a standalone native page
- [x] detail Back -> Toko & Dompet
- [x] Toko & Dompet Back -> Beranda

## Home
- [x] "Disarankan untuk Anda" absent
- [x] People you may know rail native
- [x] Sponsored rail native

## Settings
- [x] profile settings hub native
- [x] avatar/cover picker and multipart upload native
- [x] profile info standalone native form
- [x] username standalone native form
- [x] save action moved to toolbar for primary settings forms
- [x] select -> Android Spinner
- [x] checkbox -> Android Switch
- [x] text/password/email/number/textarea -> Android EditText

## Build identity
- [x] Version 2.1.0 / build 45
- [x] workflow renamed from Lite to DeApp Android
- [x] APK artifact renamed to deapp-android.apk
