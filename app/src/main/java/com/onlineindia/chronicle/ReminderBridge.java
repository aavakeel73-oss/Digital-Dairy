package com.onlineindia.chronicle;
import android.app.*;import android.content.*;import android.os.*;import android.webkit.JavascriptInterface;
import org.json.*;
public final class ReminderBridge {
 private final Activity activity;
 ReminderBridge(Activity a){activity=a;}
 @JavascriptInterface public String sync(String json){try{
 JSONArray input=new JSONArray(json);if(input.length()>2000)return "TOO_MANY_REMINDERS";
 JSONArray old=AlarmStore.all(activity);java.util.HashSet<String> ids=new java.util.HashSet<>();JSONArray clean=new JSONArray();
 for(int i=0;i<input.length();i++){JSONObject r=input.optJSONObject(i);if(r==null)continue;String id=r.optString("id");if(id.isEmpty()||id.length()>160)continue;ids.add(id);clean.put(r);}
 for(int i=0;i<old.length();i++){JSONObject r=old.optJSONObject(i);if(r!=null&&!ids.contains(r.optString("id")))AlarmScheduler.cancelPending(activity,r.optString("id"));}
 AlarmStore.save(activity,clean);int count=0;for(int i=0;i<clean.length();i++){JSONObject r=clean.optJSONObject(i);if(r!=null&&"SCHEDULED".equals(AlarmScheduler.schedule(activity,r)))count++;}
 return "SYNCED:"+count;
 }catch(Exception e){return "INVALID_JSON";}}
 @JavascriptInterface public String schedule(String id,String title,String message,long whenMillis){try{
 if(id==null||id.isEmpty()||whenMillis<=System.currentTimeMillis())return "INVALID_TIME";
 java.text.SimpleDateFormat date=new java.text.SimpleDateFormat("yyyy-MM-dd",java.util.Locale.US),time=new java.text.SimpleDateFormat("HH:mm",java.util.Locale.US);
 JSONObject r=new JSONObject();r.put("id",id);r.put("title",title);r.put("message",message);r.put("date",date.format(new java.util.Date(whenMillis)));r.put("time",time.format(new java.util.Date(whenMillis)));r.put("repeat","none");AlarmStore.put(activity,r);return AlarmScheduler.schedule(activity,r);
 }catch(Exception e){return "ERROR";}}
 @JavascriptInterface public String cancel(String id){if(id==null)return "INVALID_ID";AlarmScheduler.cancelPending(activity,id);AlarmStore.remove(activity,id);return "CANCELLED";}
 @JavascriptInterface public String permissionStatus(){android.app.AlarmManager am=(android.app.AlarmManager)activity.getSystemService(Context.ALARM_SERVICE);return Build.VERSION.SDK_INT<31||am.canScheduleExactAlarms()?"ALLOWED":"NOT_ALLOWED";}
 @JavascriptInterface public void openAlarmSettings(){activity.runOnUiThread(()->{if(Build.VERSION.SDK_INT>=31){try{Intent i=new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,android.net.Uri.parse("package:"+activity.getPackageName()));activity.startActivity(i);}catch(Exception ignored){}}});}
}
