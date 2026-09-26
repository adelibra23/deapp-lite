# DeApp Android 2.0.0 — Native Rebuild Audit

Build 43 is the first DeApp Android 2.x package. The visible product is no longer branded Lite.

## Architecture

- Native Android activity: `id.deapp.app.MainActivity`
- Networking/session: `id.deapp.app.DeappClient`
- No Android `WebView` is instantiated.
- Legacy WebView activity source is overwritten by an inert compatibility marker.
- Legacy injected JS assets are excluded through `sourceSets.main.assets.srcDirs = []`.
- Existing `applicationId id.deapp.lite` is intentionally retained only so users can install 2.0 as an update over the older APK. It is not user-facing branding.
- PHP/MySQL remains the DeApp server. Android renders its own Views and uses HTTP/JSON plus a native HTML-to-View compatibility adapter for server pages that do not yet expose complete JSON payloads.

## Native screens included

- Server connection setup
- Login
- Two-factor verification
- Three-step registration
- Welcome to DeApp onboarding animation
- Home feed
- Native post cards
- Post detail and comments
- Text post composer
- Reactions
- Chat list
- Conversation body and message composer
- Message send sound
- Notifications
- Profile
- Search users / hashtags
- Shop & Wallet dashboard
- Settings dashboard
- All Features dashboard
- Native generic server-page renderer for remaining PHP routes
- Native simple-form adapter for standard server forms
- Android launcher shortcuts

## Networking

- Persistent HTTP cookie session stored by the native app.
- Existing server URL is migrated from the old `deapp_lite` preferences when available.
- CSRF token is fetched from authenticated server pages and sent to protected API actions.
- Redirect-aware login and registration.

## UI

- Threads-inspired monochrome surface, compact typography and outline navigation.
- Native top toolbar and bottom navigation.
- Native SwipeRefreshLayout feed refresh.
- Native notice cards; no browser alert/toast UI.
- Native page transitions.
- Native forms, cards, chat bubbles and profile UI.

## Compatibility notes

The server continues to be the source of truth. A few advanced server-only interactions that were historically driven by large JavaScript widgets are exposed through the native compatibility renderer until dedicated JSON contracts are added server-side. They still open as Android Views, not as a browser/WebView.
