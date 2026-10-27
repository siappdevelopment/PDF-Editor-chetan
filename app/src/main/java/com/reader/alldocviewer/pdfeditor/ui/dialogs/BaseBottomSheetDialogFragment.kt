package com.reader.alldocviewer.pdfeditor.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.reader.alldocviewer.pdfeditor.utils.Utils
import com.reader.alldocviewer.pdfeditor.utils.setupImmersiveBottomSheetNavigation

open class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = ImmersiveBottomSheetDialog(requireContext(), getTheme())
        onConfigureBottomSheetDialog(dialog)
        dialog.setupImmersiveBottomSheetNavigation(::onBottomSheetShown)
        return dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        (dialog as? BottomSheetDialog)?.let { Utils.resetBottomSheetImmersiveNavigation(it) }
        super.onDismiss(dialog)
        activity?.let { Utils.restoreHostSystemBarsAfterDialog(it) }
    }

    protected open fun onConfigureBottomSheetDialog(dialog: BottomSheetDialog) {}

    protected open fun onBottomSheetShown(dialog: BottomSheetDialog) {}
}
