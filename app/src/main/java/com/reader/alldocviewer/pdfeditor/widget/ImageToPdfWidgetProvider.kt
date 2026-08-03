package com.reader.alldocviewer.pdfeditor.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.picker.MediaPickerActivity
import com.reader.alldocviewer.pdfeditor.utils.Const

class ImageToPdfWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_image_image_to_pdf)

            val intent = Intent(context, MediaPickerActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                addCategory(Intent.CATEGORY_DEFAULT)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("type", Const.IMAGE_FILE)
                putExtra("checkFileName", "image_to_pdf")
                putExtra("singleSelection", false)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                401,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btnWidgetImage, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
