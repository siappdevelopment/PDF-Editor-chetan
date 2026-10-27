package com.reader.alldocviewer.pdfeditor.ui.dialogs

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.databinding.DialogWidgetSelectionBinding
import com.reader.alldocviewer.pdfeditor.utils.Utils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
class WidgetBottomSheetDialog(
    private val fileExtension: String,
    private val widgetId: (Int) -> Unit
) : BaseBottomSheetDialogFragment() {

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialogTheme
    }

    private var _binding: DialogWidgetSelectionBinding? = null
    private val binding get() = _binding!!
    private var selectedWidgetId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogWidgetSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set dynamic preview images for each style card based on the file extension
        binding.ivStyle0.setImageBitmap(
            Utils.createShortcutIcon(
                requireContext(),
                0,
                fileExtension
            )
        )
        binding.ivStyle1.setImageBitmap(
            Utils.createShortcutIcon(
                requireContext(),
                1,
                fileExtension
            )
        )
        binding.ivStyle2.setImageBitmap(
            Utils.createShortcutIcon(
                requireContext(),
                2,
                fileExtension
            )
        )
        binding.ivStyle3.setImageBitmap(
            Utils.createShortcutIcon(
                requireContext(),
                3,
                fileExtension
            )
        )

        // Initialize selection (default is 0)
        selectStyle(0)

        // Set click listeners for style selections
        binding.cardStyle0.setOnClickListener { selectStyle(0) }
        binding.cardStyle1.setOnClickListener { selectStyle(1) }
        binding.cardStyle2.setOnClickListener { selectStyle(2) }
        binding.cardStyle3.setOnClickListener { selectStyle(3) }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnOk.setOnClickListener {
            widgetId(selectedWidgetId)
            dismiss()
        }
    }

    private fun selectStyle(styleId: Int) {
        selectedWidgetId = styleId

        // Reset backgrounds of all cards to white, and text colors to default
        val normalBg = ContextCompat.getColor(requireContext(), R.color.white)
        val selectedBg =
            Color.parseColor("#FFEAEB") // Light red tint to match app main_color E30000

        binding.cardStyle0.setCardBackgroundColor(if (styleId == 0) selectedBg else normalBg)
        binding.cardStyle1.setCardBackgroundColor(if (styleId == 1) selectedBg else normalBg)
        binding.cardStyle2.setCardBackgroundColor(if (styleId == 2) selectedBg else normalBg)
        binding.cardStyle3.setCardBackgroundColor(if (styleId == 3) selectedBg else normalBg)

        // Set selected border/elevation
        binding.cardStyle0.cardElevation = if (styleId == 0) 12f else 4f
        binding.cardStyle1.cardElevation = if (styleId == 1) 12f else 4f
        binding.cardStyle2.cardElevation = if (styleId == 2) 12f else 4f
        binding.cardStyle3.cardElevation = if (styleId == 3) 12f else 4f

        val accentColor = Color.parseColor("#E30000")
        val blackColor = ContextCompat.getColor(requireContext(), R.color.black)

        binding.tvStyle0.setTextColor(if (styleId == 0) accentColor else blackColor)
        binding.tvStyle1.setTextColor(if (styleId == 1) accentColor else blackColor)
        binding.tvStyle2.setTextColor(if (styleId == 2) accentColor else blackColor)
        binding.tvStyle3.setTextColor(if (styleId == 3) accentColor else blackColor)
    }

    override fun onConfigureBottomSheetDialog(dialog: BottomSheetDialog) {
        // Bounce fix: dialog create hote hi behavior set karo — onBottomSheetShown ke andar
        // state change karne se collapse→expand animation (bounce) aata tha
        dialog.behavior.apply {
            skipCollapsed = true
            isFitToContents = true
            state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onBottomSheetShown(dialog: BottomSheetDialog) {
        val bottomSheet =
            dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            it.setBackgroundResource(R.drawable.shape_bg_bottom_dialog)
            val behavior = BottomSheetBehavior.from(it)
            behavior.peekHeight = 0
            behavior.skipCollapsed = true
            behavior.isFitToContents = true
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
