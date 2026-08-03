package com.reader.alldocviewer.pdfeditor.Advertisement.CallendClass

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import com.reader.alldocviewer.pdfeditor.R

class CallEndViewManager {

    fun showCallEnd(
        context: Context,
        intent: Intent
    ){
        val inflater = LayoutInflater.from(context)
        var floatView = inflater.inflate(R.layout.call_end_view,null)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        )

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        windowManager.addView(floatView,params)
        Handler(Looper.getMainLooper()).postDelayed({
            //        Log.d(TAG, "openNewActivity: delegated to CallEndLaunchHelper");
            Log.d("TAG", "onReceive:fgsggdsgdsg     4444444 ")
            context.startActivity(intent)
            windowManager.removeView(floatView)
            floatView = null
        },250)
    }

}
