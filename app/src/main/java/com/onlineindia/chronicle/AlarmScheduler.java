package com.onlineindia.chronicle;
import android.app.*;import android.content.*;import android.net.Uri;import android.os.Build;
import org.json.*;import java.text.*;import java.util.*;
public final class AlarmScheduler {
 static final String ACTION="com.onlineindia.chronicle.ALARM";
 static PendingIntent pending(Context c,String id){Intent i=new Intent(c,ReminderReceiver.class).setAction(ACTION).setData(Uri.parse("chronicle://alarm/"+Uri.encode(id)));return PendingIntent.getBroadcast(c,0,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
 static long next(JSONObject r,long now){try{
 String date=r.optString("date"),time=r.optString("time");String[] d=date.split("-"),t=time.split(":");if(d.length!=3||t.length<2)return -1;
 Calendar base=Calendar.getInstance();base.setLenient(false);base.set(Integer.parseInt(d[0]),Integer.parseInt(d[1])-1,Integer.parseInt(d[2]),Integer.parseInt(t[0]),Integer.parseInt(t[1]),0);base.set(Calendar.MILLISECOND,0);long start=base.getTimeInMillis();String repeat=r.optString("repeat","none");if("none".equals(repeat))return start>now?start:-1;
 Calendar candidate=(Calendar)base.clone();for(int i=0;i<3700;i++){
 long ts=candidate.getTimeInMillis();if(ts>now)return ts;
 if("daily".equals(repeat))candidate.add(Calendar.DAY_OF_YEAR,1);
 else if("weekly".equals(repeat))candidate.add(Calendar.WEEK_OF_YEAR,1);
 else if("monthly".equals(repeat))candidate.add(Calendar.MONTH,1);
 else if("yearly".equals(repeat))candidate.add(Calendar.YEAR,1);
 else return -1;
 }return -1;
 }catch(Exception e){return -1;}}
 static synchronized String schedule(Context c,JSONObject r){String id=r.optString("id");if(id.isEmpty())return "INVALID_ID";cancelPending(c,id);if(r.optBoolean("isCompleted",false))return "COMPLETED";
 long when=next(r,System.currentTimeMillis());if(when<0)return "NO_FUTURE_OCCURRENCE";
 AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);if(am==null)return "NO_ALARM_MANAGER";
 try{PendingIntent pi=pending(c,id);Intent show=new Intent(c,MainActivity.class);PendingIntent display=PendingIntent.getActivity(c,0,show,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
 if(Build.VERSION.SDK_INT>=31&&!am.canScheduleExactAlarms())return "EXACT_ALARM_PERMISSION_REQUIRED";
 am.setAlarmClock(new AlarmManager.AlarmClockInfo(when,display),pi);return "SCHEDULED";
 }catch(SecurityException e){return "EXACT_ALARM_PERMISSION_REQUIRED";}catch(Exception e){return "ERROR";}}
 static void cancelPending(Context c,String id){AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);if(am!=null)am.cancel(pending(c,id));}
 static synchronized void rescheduleAll(Context c){JSONArray a=AlarmStore.all(c);for(int i=0;i<a.length();i++){JSONObject r=a.optJSONObject(i);if(r!=null)schedule(c,r);}}
}
