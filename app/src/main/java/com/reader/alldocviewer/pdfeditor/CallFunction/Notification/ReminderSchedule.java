package com.reader.alldocviewer.pdfeditor.CallFunction.Notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

public class ReminderSchedule {

    public static void scheduleReminder(Context context, String txt, String title, Calendar calendar, int notificationId) {
        Intent intent = new Intent(context, RemindReceiver.class);
        intent.putExtra("txt", txt);
        intent.putExtra("title", title);
        intent.putExtra("notificationId", notificationId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        scheduleAlarmAt(context, pendingIntent, calendar.getTimeInMillis());
    }

    public static void scheduleAlarmAt(Context context, PendingIntent pendingIntent, long triggerAtMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
            );
        } else {
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
            );
        }
    }
}
