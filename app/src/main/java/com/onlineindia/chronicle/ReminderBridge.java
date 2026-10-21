package com.onlineindia.chronicle;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.webkit.JavascriptInterface;
import org.json.JSONObject;

public class ReminderBridge {
    private final Activity activity;
    ReminderBridge(Activity activity) { this.activity = activity; }
    @JavascriptInterface public String schedule(String id, String title, String message, long whenMillis) {
        try {
            if (id == null || id.isEmpty() || whenMillis <= System.currentTimeMillis()) return "INVALID_TIME";
            AlarmManager manager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pending = intent(id, title, message);
            if (Build.VERSION.SDK_INT >= 31 && !manager.canScheduleExactAlarms()) {
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenMillis, pending);
                return "SCHEDULED_INEXACT";
            }
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenMillis, pending);
            return "SCHEDULED";
        } catch (Exception ex) { return "ERROR: " + ex.getMessage(); }
    }
    @JavascriptInterface public String cancel(String id) {
        try {
            AlarmManager manager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
            manager.cancel(intent(id, "", ""));
            return "CANCELLED";
        } catch(Exception ex) { return "ERROR: " + ex.getMessage(); }
    }
    private PendingIntent intent(String id, String title, String message) {
        Intent i = new Intent(activity, ReminderReceiver.class);
        i.setAction("com.onlineindia.chronicle.REMINDER");
        i.setData(android.net.Uri.parse("chronicle://reminder/" + android.net.Uri.encode(id)));
        i.putExtra("title", title);
        i.putExtra("message", message);
        return PendingIntent.getBroadcast(activity, 0, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
