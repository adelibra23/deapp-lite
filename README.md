# DeApp Android 2.0

DeApp Android 2.0 is the native rebuild of the previous Android wrapper. The app name and version no longer use **Lite**.

## What changed

The Android UI no longer renders PHP pages inside a WebView. `MainActivity` is a native Java activity and all visible screens are made from Android Views. PHP/MySQL remains the backend and the app communicates with it over HTTP/JSON.

Core flows such as login, 2FA, registration, feed, posts, comments, reactions, chat, notifications, profile, search, Shop & Wallet, Settings and All Features are represented by native Android screens. Server pages that do not yet have a complete JSON endpoint are passed through a native document/form adapter so they still render as Android Views instead of a website.

## Version

- versionCode: 43
- versionName: 2.0.0
- namespace: `id.deapp.app`
- applicationId: `id.deapp.lite` only for upgrade compatibility with existing installations

## Existing server configuration

On first launch after upgrading, DeApp attempts to migrate the server URL stored by older releases. The old WebView login cookie cannot be transferred safely into the new native HTTP session, so the user may need to sign in once again after upgrading to 2.0.

## Build

This patch is intended to be extracted over the existing DeApp Android project. It adds `org.jsoup:jsoup` for safe server document parsing in the native compatibility layer. GitHub Actions/Gradle will download that dependency during build.
