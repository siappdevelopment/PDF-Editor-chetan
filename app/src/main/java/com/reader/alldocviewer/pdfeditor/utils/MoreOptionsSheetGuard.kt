package com.reader.alldocviewer.pdfeditor.utils

import com.google.android.material.bottomsheet.BottomSheetDialog

/** Prevents the more-options bottom sheet from opening twice on rapid clicks. */
object MoreOptionsSheetGuard {
    private var activeDialog: BottomSheetDialog? = null
    private var pendingOpen = false

    fun begin(): Boolean {
        if (pendingOpen || activeDialog?.isShowing == true) return false
        pendingOpen = true
        return true
    }

    fun show(dialog: BottomSheetDialog) {
        pendingOpen = false
        activeDialog = dialog
        dialog.setOnDismissListener {
            if (activeDialog === dialog) {
                activeDialog = null
            }
        }
        dialog.show()
    }

    fun abort() {
        pendingOpen = false
    }
}
