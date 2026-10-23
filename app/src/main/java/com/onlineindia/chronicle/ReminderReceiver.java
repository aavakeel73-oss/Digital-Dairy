package com.onlineindia.chronicle;
import android.app.*;import android.content.*;import android.os.*;import android.Manifest;import android.content.pm.PackageManager;
import org.json.*;
public class ReminderReceiver extends android.content.BroadcastReceiver {
 public static final String CHANNEL="chronicle_alarm_v2";
 static void channel(Context c){if(Build.VERSION.SDK_INT>=26){NotificationChannel n=new NotificationChannel(CHANNEL,"Chronicle Alarms",NotificationManager.IMPORTANCE_HIGH);n.enableVibration(true);n.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);((NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(n);}}
 @Override public void onReceive(Context c,Intent intent){if(intent==null||intent.getData()==null)return;String id=intent.getData().getLastPathSegment();JSONObject r=AlarmStore.get(c,id);if(r==null||r.optBoolean("isCompleted",false))return;
 channel(c);String title=r.optString("title","Chronicle Reminder");Intent alarm=new Intent(c,AlarmActivity.class).putExtra("id",id).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
 PendingIntent full=PendingIntent.getActivity(c,id.hashCode(),alarm,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
 Intent stop=new Intent(c,AlarmActionReceiver.class).setAction("STOP").setData(android.net.Uri.parse("chronicle://action/"+android.net.Uri.encode(id)));
 PendingIntent stopPi=PendingIntent.getBroadcast(c,id.hashCode(),stop,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
 Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(c,CHANNEL):new Notification.Builder(c);
 b.setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle(title).setContentText(r.optString("message","Reminder is due"))
 .setCategory(Notification.CATEGORY_ALARM).setVisibility(Notification.VISIBILITY_PUBLIC).setAutoCancel(false).setOngoing(true)
 .setContentIntent(full).setFullScreenIntent(full,true).addAction(android.R.drawable.ic_menu_close_clear_cancel,"Stop",stopPi);
 if(Build.VERSION.SDK_INT<26)b.setPriority(Notification.PRIORITY_HIGH).setDefaults(Notification.DEFAULT_ALL);
 if(Build.VERSION.SDK_INT<33||c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED){((NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE)).notify(id.hashCode(),b.build());}
 try{c.startActivity(alarm);}catch(Exception ignored){}
 // Only reschedule after the currently due occurrence, so recurring alarms survive a process death.
 if(!"none".equals(r.optString("repeat","none")))AlarmScheduler.schedule(c,r);
 }
}
