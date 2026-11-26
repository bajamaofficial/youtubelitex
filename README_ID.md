# YouTube Lite X 3.0.1 — Build Fix

Perbaikan ini merapikan kode Java, menetapkan Java 8 compatibility secara eksplisit, membuat BuildConfig aktif, dan membuat workflow tetap dapat membangun APK meskipun API key belum dimasukkan.

## Build tanpa Android Studio
1. Unduh ZIP perbaikan ini.
2. Ekstrak, lalu unggah seluruh isi folder `YouTubeLiteX-3.0-FIX` ke repositori GitHub yang sama, timpa file versi lama.
3. Pastikan folder `.github/workflows/build.yml` ikut diperbarui.
4. Buka GitHub → Actions → Build YouTube Lite X 3.0 → Run workflow.
5. Jika build berhasil, unduh artifact `YouTubeLiteX-3.0.1-debug`.

## Mengaktifkan pencarian
Tambahkan secret repository bernama `YOUTUBE_API_KEY` berisi YouTube Data API v3 key, kemudian jalankan workflow ulang.

## Batasan
Antarmuka beranda dan hasil pencarian menggunakan komponen Android Native. Area pemutar menggunakan YouTube IFrame Player API resmi. Iklan YouTube masih dapat muncul; aplikasi tidak memblokirnya. Background playback dan download video YouTube tidak disediakan. Kompatibilitas Android 5–16 harus diuji pada perangkat sebenarnya; project ini belum dikompilasi di lingkungan ini.
