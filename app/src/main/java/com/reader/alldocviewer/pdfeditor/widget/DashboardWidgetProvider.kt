package com.reader.alldocviewer.pdfeditor.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.ui.activities.main.MainActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.search.SearchAllFilesActivity

class DashboardWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_dashboard)

            // 1. Recent: MainActivity with nav_recent
            val recentIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("select_tab", R.id.nav_recent)
            }
            val recentPI = PendingIntent.getActivity(
                context,
                101,
                recentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btnRecent, recentPI)

            // 2. Edit PDF: SearchAllFilesActivity with checkFileName = "edit_pdf"
            val editIntent = Intent(context, SearchAllFilesActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("checkFileName", "edit_pdf")
            }
            val editPI = PendingIntent.getActivity(
                context,
                102,
                editIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btnEditPdf, editPI)

            // 3. PDF tab (position 1)
            val pdfIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("select_file_tab", 1)
            }
            val pdfPI = PendingIntent.getActivity(
                context,
                103,
                pdfIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btnPdf, pdfPI)

            // 4. Word tab (position 2)
            val wordIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("select_file_tab", 2)
            }
            val wordPI = PendingIntent.getActivity(
                context,
                104,
                wordIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btnWord, wordPI)

            // 5. Excel tab (position 3)
            val excelIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("select_file_tab", 3)
            }
            val excelPI = PendingIntent.getActivity(
                context,
                105,
                excelIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btnExcel, excelPI)

            // 6. PPT tab (position 4)
            val pptIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("select_file_tab", 4)
            }
            val pptPI = PendingIntent.getActivity(
                context,
                106,
                pptIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btnPpt, pptPI)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
