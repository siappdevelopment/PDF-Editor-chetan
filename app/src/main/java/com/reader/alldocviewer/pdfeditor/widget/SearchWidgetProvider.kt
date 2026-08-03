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

class SearchWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_search)

            // 1. Search Bar: SearchAllFilesActivity
            val searchIntent = Intent(context, SearchAllFilesActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val searchPI = PendingIntent.getActivity(
                context,
                201,
                searchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btnSearch, searchPI)

            // 2. Home Tab: MainActivity with nav_files
            val homeIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("select_tab", R.id.nav_files)
            }
            val homePI = PendingIntent.getActivity(
                context,
                202,
                homeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btnHome, homePI)

            // 3. Recent Tab: MainActivity with nav_recent
            val recentIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("select_tab", R.id.nav_recent)
            }
            val recentPI = PendingIntent.getActivity(
                context,
                203,
                recentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btnRecent, recentPI)

            // 4. Bookmarks Tab: MainActivity with nav_bookmark
            val bookmarkIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("select_tab", R.id.nav_bookmark)
            }
            val bookmarkPI = PendingIntent.getActivity(
                context,
                204,
                bookmarkIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btnBookmarks, bookmarkPI)

            // 5. Tools Tab: MainActivity with nav_tools
            val toolsIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("select_tab", R.id.nav_tools)
            }
            val toolsPI = PendingIntent.getActivity(
                context,
                205,
                toolsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btnTools, toolsPI)

            // 6. Edit PDF Button: SearchAllFilesActivity with checkFileName = "edit_pdf"
            val editIntent = Intent(context, SearchAllFilesActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("checkFileName", "edit_pdf")
            }
            val editPI = PendingIntent.getActivity(
                context,
                206,
                editIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btnEditPdf, editPI)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
