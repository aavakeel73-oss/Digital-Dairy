Chronicle Native Alarm — corrected project

Changes: WebView native alarm status display; native bridge cloud sync hook; Android-compatible full-screen notification launch; reboot/time-change receiver; missing icon build fix; duplicate file cleanup.

Limitations: Device APK build and alarm tests are NOT performed. Room/Firebase offline-first alarm sync, per-reminder native ringtone, reliable continuous playback when full-screen is blocked are not yet implemented.

To build: upload the CONTENTS of this project to the repository root, then run GitHub Actions > Build Digital Diary APK.

Do not uninstall your existing app without a backup. Existing signing key is required for in-place upgrade.
