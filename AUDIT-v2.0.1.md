# DeApp Android v2.0.1 — Build Fix

- versionCode 44 / versionName 2.0.1
- Fixed GitHub Actions Java compile error in `MainActivity.java` line 470.
- `SwipeRefreshLayout#setOnRefreshListener` now receives a valid `OnRefreshListener` lambda and delegates to the existing `Runnable`.
- No WebView runtime reintroduced.
