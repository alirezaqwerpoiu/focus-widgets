# Focus (Android)

This project packages your Focus web app (`app/src/main/assets/index.html`) as an Android app,
and adds home-screen widgets that read the same data.

## How data is shared
The web app keeps its data in localStorage. On Android, a small bridge (`WebStore`) stores that data in
the app's SharedPreferences, and the widgets read the same keys. So the app, the widgets and the
timer always show the same numbers. No CSV import or export is needed.

## Widgets
- Tiny (1×1): start/stop with the day's ring.
- Small (2×2): ring, today's time and start/stop.
- Tall (1×2): status, time and start/stop.
- Card (4×2): today's time, goal progress and start/stop.
- Strip (4×1): ring, status, time and start/stop in one row.
- Week (4×2): seven rings from Saturday to Friday, with this week's total and the change from last week.
- Month (4×4): the Persian month calendar, with future days dimmed.

## Build without a PC (GitHub Actions)
1. Create a free GitHub account and a new repository.
2. Upload the project (keep the folder structure, including the hidden `.github` folder).
3. Open the Actions tab, run "Build APK", and download `focus-apk` when it finishes.
4. Install `app-debug.apk` on your phone.

## Build on a PC
Open this folder in Android Studio, let Gradle sync, then Build > Build APK(s).

## Known limits
- Widget actions only change the timer. Session names and categories are not recorded when you stop from a widget.
- The web app's CSV export and import use file downloads that do not work inside the Android WebView yet.
- The app's notifications are not available inside the WebView.
- Changes made from a widget appear in an open app when it comes back to the foreground.
- The project has not been compiled in this environment. If a build fails, send me the error text.

## Notification (the quick strip)
In the web app, go to Settings and turn on "اعلان دائمی تایمر". A persistent notification then shows today's time and a start/stop button. Turn it off to remove it. On Android 13 and newer, the app asks for notification permission the first time it opens.

## Adding widgets from the app
In Settings, open "افزودن ویجت به صفحه‌ی اصلی" and tap a widget. The launcher then asks you to confirm adding it. Some launchers do not support this request; in that case, add the widget manually from the home screen's widget list.
