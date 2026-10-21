// This is an integration example, NOT a replacement for app/index.html.
// Call after the existing reminder is saved successfully:
function scheduleNativeReminder(reminder) {
  if (!window.ChronicleAndroid) return 'NATIVE_NOT_AVAILABLE';
  // reminder.whenMillis must be an absolute UNIX timestamp in milliseconds.
  return window.ChronicleAndroid.schedule(String(reminder.id), String(reminder.title || 'Reminder'), String(reminder.body || ''), Number(reminder.whenMillis));
}
// Call after a reminder is deleted or before changing its time:
function cancelNativeReminder(id) {
  if (!window.ChronicleAndroid) return 'NATIVE_NOT_AVAILABLE';
  return window.ChronicleAndroid.cancel(String(id));
}
