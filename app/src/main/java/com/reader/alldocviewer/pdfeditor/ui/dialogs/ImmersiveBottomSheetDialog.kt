package com.reader.alldocviewer.pdfeditor.ui.dialogs

import android.content.Context
import android.os.Bundle
import androidx.annotation.StyleRes
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.utils.Utils

class ImmersiveBottomSheetDialog(
    context: Context,
    @StyleRes theme: Int = R.style.ImmersiveBottomSheetDialogTheme,
) : BottomSheetDialog(context, theme) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.applyBottomSheetImmersiveNavigation(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Utils.applyBottomSheetImmersiveNavigation(this)
    }

    override fun onStart() {
        super.onStart()
        Utils.applyBottomSheetImmersiveNavigation(this)
    }

    override fun dismiss() {
        Utils.resetBottomSheetImmersiveNavigation(this)
        super.dismiss()
    }
}
