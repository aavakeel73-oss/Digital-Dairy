package com.onlineindia.chronicle;
import android.content.*;import android.app.*;
public class AlarmActionReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context c,Intent i){if(i==null||i.getData()==null)return;String id=i.getData().getLastPathSegment();((NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(id.hashCode());AlarmActivity.stopActive(id);}
}
