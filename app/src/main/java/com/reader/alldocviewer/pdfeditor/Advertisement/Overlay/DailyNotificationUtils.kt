package com.reader.alldocviewer.pdfeditor.Advertisement.Overlay

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.reader.alldocviewer.pdfeditor.CallFunction.Notification.ReminderSchedule
import java.util.Calendar
import kotlin.apply

object DailyNotificationUtils {

    fun scheduleDailyNotification(context: Context) {
        val intent = Intent(context, DailyNotificationReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        ReminderSchedule.scheduleAlarmAt(context, pendingIntent, calendar.timeInMillis)
    }
}
