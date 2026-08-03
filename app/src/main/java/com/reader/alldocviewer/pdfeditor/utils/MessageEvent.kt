package com.reader.alldocviewer.pdfeditor.utils

import org.greenrobot.eventbus.EventBus

class MessageEvent(
    val type: String,
    val data: Any? = null
)

object MessageEventKt {
    const val SHOW = "Show"
    const val HIDE = "Hide"

    @JvmStatic
    fun sendEvent(type: String, data: Any? = null) {
        EventBus.getDefault().post(MessageEvent(type, data))
    }
}
