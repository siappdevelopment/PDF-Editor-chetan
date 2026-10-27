package com.reader.alldocviewer.pdfeditor.ui.dialogs

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RemoteViews
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.databinding.DialogAddWidgetBinding
import com.reader.alldocviewer.pdfeditor.ui.adapters.WidgetPreviewAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.reader.alldocviewer.pdfeditor.widget.CreatePdfWidgetProvider
import com.reader.alldocviewer.pdfeditor.widget.DashboardWidgetProvider
import com.reader.alldocviewer.pdfeditor.widget.ImageToPdfWidgetProvider
import com.reader.alldocviewer.pdfeditor.widget.SearchWidgetProvider

class AddWidgetBottomSheetDialog : BaseBottomSheetDialogFragment() {

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialogTheme
    }

    private var _binding: DialogAddWidgetBinding? = null
    private val binding get() = _binding!!

    private val images = listOf(
        R.drawable.ic_widget_1,
        R.drawable.ic_widget_2,
        R.drawable.ic_widget_3,
        R.drawable.ic_widget_4
    )

    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
    private val autoScrollDelay = 3000L // 3 seconds

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddWidgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = WidgetPreviewAdapter(images)
        binding.viewPager.adapter = adapter

        val indicators = listOf(
            binding.indicator0,
            binding.indicator1,
            binding.indicator2,
            binding.indicator3
        )

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                for (i in indicators.indices) {
                    indicators[i].setImageResource(
                        if (i == position) R.drawable.dot_selected else R.drawable.dot_unselected
                    )
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    stopAutoScroll()
                } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    startAutoScroll()
                }
            }
        })

        // Auto scrolling logic
        runnable = object : Runnable {
            override fun run() {
                if (_binding == null) return
                var currentItem = binding.viewPager.currentItem
                currentItem = (currentItem + 1) % images.size
                binding.viewPager.setCurrentItem(currentItem, true)
                handler.postDelayed(this, autoScrollDelay)
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnAdd.setOnClickListener {
            val selectedPosition = binding.viewPager.currentItem
            pinWidget(selectedPosition)
            dismiss()
        }
    }

    private fun startAutoScroll() {
        runnable?.let {
            handler.removeCallbacks(it)
            handler.postDelayed(it, autoScrollDelay)
        }
    }

    private fun stopAutoScroll() {
        runnable?.let { handler.removeCallbacks(it) }
    }

    override fun onResume() {
        super.onResume()
        startAutoScroll()
    }

    override fun onPause() {
        super.onPause()
        stopAutoScroll()
    }

    private fun pinWidget(position: Int) {
        val context = requireContext()
        val appWidgetManager = AppWidgetManager.getInstance(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                val providerClass = when (position) {
                    0 -> DashboardWidgetProvider::class.java
                    1 -> SearchWidgetProvider::class.java
                    2 -> CreatePdfWidgetProvider::class.java
                    else -> ImageToPdfWidgetProvider::class.java
                }
                val myProvider = ComponentName(context, providerClass)

                val successIntent = Intent(context.packageName + ".WIDGET_PINNED")
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    position,
                    successIntent,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    } else {
                        PendingIntent.FLAG_UPDATE_CURRENT
                    }
                )

                val previewLayout = when (position) {
                    0 -> R.layout.widget_dashboard
                    1 -> R.layout.widget_search
                    2 -> R.layout.widget_image_create
                    else -> R.layout.widget_image_image_to_pdf
                }
                val previewRemoteViews =
                    RemoteViews(context.packageName, previewLayout)
                val optionsBundle = Bundle().apply {
                    putParcelable(
                        AppWidgetManager.EXTRA_APPWIDGET_PREVIEW,
                        previewRemoteViews
                    )
                }

                val requestSent =
                    appWidgetManager.requestPinAppWidget(myProvider, optionsBundle, pendingIntent)
                if (requestSent) {
                    Toast.makeText(
                        context,
                        getString(R.string.widget_pinning_request_sent), Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.failed_to_pin_widget), Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    context,
                    getString(R.string.launcher_does_not_support_widget_pinning), Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                context,
                getString(R.string.widget_pinning_requires_android_8_0), Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onConfigureBottomSheetDialog(dialog: BottomSheetDialog) {
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
        stopAutoScroll()
        _binding = null
    }
}
