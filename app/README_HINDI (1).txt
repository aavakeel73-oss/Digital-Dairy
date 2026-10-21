Chronicle Android Native Fix — GitHub Actions project

Files to upload to repository folder app/:
1. MainActivity.java (replace existing after backing it up)
2. ReminderBridge.java (new)
3. ReminderReceiver.java (new)

4. Edit existing app/AndroidManifest.xml using manifest_additions.txt.
5. Existing app/index.html MUST remain intact. To activate native reminders, connect the actual Save/Edit/Delete JS handlers to the bridge using HTML_integration_example.js. This package does not claim notifications are fully connected until that step is completed.
6. Build APK via GitHub Actions. Test dark status icons and a reminder scheduled a few minutes ahead.
7. Android Settings > Apps > Chronicle > Notifications: Allow.
8. Exact alarms may require special access. If not granted, alarms can be inexact.

Limitations:
- Reminder alarms are not automatically restored after reboot in this version.
- Selected custom ringtone mapping is not implemented; notification channel default sound is used.
- Browser Notification API is not the native reminder bridge.
- Do not delete app/index.html or overwrite the entire AndroidManifest.xml.
