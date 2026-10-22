package com.onlineindia.chronicle;
import android.app.*;import android.os.*;import android.content.*;import android.graphics.Color;import android.media.*;import android.net.Uri;import android.view.*;import android.widget.*;
import org.json.*;
public class AlarmActivity extends Activity {
 private MediaPlayer player;private android.os.Vibrator vibrator;private String id;
 static java.lang.ref.WeakReference<AlarmActivity> active = new java.lang.ref.WeakReference<>(null);
 static void stopActive(String target){AlarmActivity a=active.get();if(a!=null&&target.equals(a.id))a.runOnUiThread(a::stopAlarm);}
 @Override public void onCreate(Bundle state){super.onCreate(state);if(Build.VERSION.SDK_INT>=27){setShowWhenLocked(true);setTurnScreenOn(true);}else getWindow().addFlags(6815872);
 getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);id=getIntent().getStringExtra("id");JSONObject r=AlarmStore.get(this,id==null?"":id);
 if(r==null){finish();return;}
 LinearLayout layout=new LinearLayout(this);layout.setOrientation(LinearLayout.VERTICAL);layout.setGravity(Gravity.CENTER);layout.setPadding(36,36,36,36);layout.setBackgroundColor(Color.rgb(10,27,40));
 TextView title=new TextView(this);title.setText(r.optString("title","Reminder"));title.setTextColor(Color.WHITE);title.setTextSize(30);title.setGravity(Gravity.CENTER);layout.addView(title);
 TextView message=new TextView(this);message.setText(r.optString("message","Alarm time"));message.setTextColor(Color.LTGRAY);message.setTextSize(18);message.setGravity(Gravity.CENTER);layout.addView(message);
 Button snooze=new Button(this);snooze.setText("SNOOZE 10 MIN");layout.addView(snooze);Button stop=new Button(this);stop.setText("STOP ALARM");layout.addView(stop);setContentView(layout);active=new java.lang.ref.WeakReference<>(this);
 stop.setOnClickListener(v->stopAlarm());snooze.setOnClickListener(v->{try{JSONObject item=AlarmStore.get(this,id);if(item!=null){JSONObject copy=new JSONObject(item.toString());java.text.SimpleDateFormat df=new java.text.SimpleDateFormat("yyyy-MM-dd",java.util.Locale.US),tf=new java.text.SimpleDateFormat("HH:mm",java.util.Locale.US);java.util.Date d=new java.util.Date(System.currentTimeMillis()+600000);copy.put("date",df.format(d));copy.put("time",tf.format(d));copy.put("repeat","none");copy.put("id",id+"_snooze_"+System.currentTimeMillis());AlarmStore.put(this,copy);AlarmScheduler.schedule(this,copy);}}catch(Exception ignored){}stopAlarm();});
 try{Uri uri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);if(uri==null)uri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);player=new MediaPlayer();player.setDataSource(this,uri);player.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build());player.setLooping(true);player.prepare();player.start();}catch(Exception ignored){}
 vibrator=(android.os.Vibrator)getSystemService(VIBRATOR_SERVICE);if(vibrator!=null&&vibrator.hasVibrator())vibrator.vibrate(new long[]{0,400,250,400},0);
 }
 private void stopAlarm(){if(id!=null)((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel(id.hashCode());release();finish();}
 private void release(){if(player!=null){try{player.stop();}catch(Exception ignored){}player.release();player=null;}if(vibrator!=null)vibrator.cancel();}
 @Override protected void onDestroy(){release();if(active.get()==this)active.clear();super.onDestroy();}
}
