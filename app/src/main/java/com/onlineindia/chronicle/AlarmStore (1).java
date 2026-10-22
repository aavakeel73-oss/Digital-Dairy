package com.onlineindia.chronicle;
import android.content.*;
import org.json.*;
import java.util.*;
public final class AlarmStore {
 private static final String KEY="native_reminders_v1";
 static synchronized JSONArray all(Context c){try{return new JSONArray(c.getSharedPreferences("chronicle_alarm_store",0).getString(KEY,"[]"));}catch(Exception e){return new JSONArray();}}
 static synchronized void save(Context c,JSONArray a){c.getSharedPreferences("chronicle_alarm_store",0).edit().putString(KEY,a.toString()).commit();}
 static synchronized JSONObject get(Context c,String id){JSONArray a=all(c);for(int i=0;i<a.length();i++){JSONObject r=a.optJSONObject(i);if(r!=null&&id.equals(r.optString("id")))return r;}return null;}
 static synchronized void put(Context c,JSONObject item){JSONArray a=all(c),b=new JSONArray();String id=item.optString("id");for(int i=0;i<a.length();i++){JSONObject r=a.optJSONObject(i);if(r!=null&&!id.equals(r.optString("id")))b.put(r);}b.put(item);save(c,b);}
 static synchronized void remove(Context c,String id){JSONArray a=all(c),b=new JSONArray();for(int i=0;i<a.length();i++){JSONObject r=a.optJSONObject(i);if(r!=null&&!id.equals(r.optString("id")))b.put(r);}save(c,b);}
}
