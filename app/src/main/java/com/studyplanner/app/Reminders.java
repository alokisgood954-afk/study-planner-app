package com.studyplanner.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Calendar;

public class Reminders {
    static PendingIntent pi(Context c, int id, String title, String body) {
        Intent i = new Intent(c, ReminderReceiver.class);
        i.putExtra("title", title);
        i.putExtra("body", body);
        i.putExtra("id", id);
        return PendingIntent.getBroadcast(c, id, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static void schedule(Context c) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        SharedPreferences sp = c.getSharedPreferences("rem", Context.MODE_PRIVATE);
        String old = sp.getString("ids", "");
        if (!old.isEmpty()) {
            for (String s : old.split(",")) {
                try { am.cancel(pi(c, Integer.parseInt(s), "", "")); } catch (Exception e) { }
            }
        }
        StringBuilder ids = new StringBuilder();
        try {
            JSONArray arr = new JSONArray(sp.getString("json", "[]"));
            for (int k = 0; k < arr.length(); k++) {
                JSONObject o = arr.getJSONObject(k);
                int id = o.getInt("id");
                int min = o.getInt("min");
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_WEEK, o.getInt("dow"));
                cal.set(Calendar.HOUR_OF_DAY, min / 60);
                cal.set(Calendar.MINUTE, min % 60);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                if (cal.getTimeInMillis() <= System.currentTimeMillis()) cal.add(Calendar.DAY_OF_YEAR, 7);
                am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pi(c, id, o.getString("title"), o.getString("body")));
                if (ids.length() > 0) ids.append(",");
                ids.append(id);
            }
        } catch (Exception e) { }
        sp.edit().putString("ids", ids.toString()).apply();
    }
}
