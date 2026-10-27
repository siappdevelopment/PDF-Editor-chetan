package com.reader.alldocviewer.pdfeditor.utils

import com.google.android.material.bottomsheet.BottomSheetDialog
import com.reader.alldocviewer.pdfeditor.utils.setupImmersiveBottomSheetNavigation

/** Prevents the more-options bottom sheet from opening twice on rapid clicks. */
object MoreOptionsSheetGuard {
    private var activeDialog: BottomSheetDialog? = null
    private var pendingOpen = false

    fun begin(): Boolean {
        if (pendingOpen || activeDialog?.isShowing == true) return false
        pendingOpen = true
        return true
    }

    fun show(dialog: BottomSheetDialog, onDismiss: (() -> Unit)? = null) {
        pendingOpen = false
        activeDialog = dialog
        dialog.setOnDismissListener {
            Utils.resetBottomSheetImmersiveNavigation(dialog)
            if (activeDialog === dialog) {
                activeDialog = null
            }
            onDismiss?.invoke()
        }
        dialog.setupImmersiveBottomSheetNavigation()
        dialog.show()
    }

    fun abort() {
        pendingOpen = false
    }

    fun dismiss() {
        pendingOpen = false
        val dialog = activeDialog
        activeDialog = null
        if (dialog?.isShowing == true) {
            dialog.dismiss()
        }
    }
}
