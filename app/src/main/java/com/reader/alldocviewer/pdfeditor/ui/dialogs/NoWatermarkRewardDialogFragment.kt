package com.reader.alldocviewer.pdfeditor.ui.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.reader.alldocviewer.pdfeditor.Advertisement.AdPlacement
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSRewardDisplay
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.databinding.DialogNoWatermarkRewardBinding

/**
 * Custom No Watermark reward dialog UI.
 * Rewarded Ad load/show is handled by [ADSRewardDisplay].
 */
class NoWatermarkRewardDialogFragment : DialogFragment() {

    fun interface Listener {
        fun onNoWatermarkUnlocked()
    }

    private var _binding: DialogNoWatermarkRewardBinding? = null
    private val binding get() = _binding!!

    private var listener: Listener? = null
    private var isClickLocked = false
    private var isRewardAdInProgress = false

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DialogNoWatermarkRewardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyTitleSpan()
        binding.ivClose.setOnClickListener { dismissSafely() }
        binding.btnMaybeLater.setOnClickListener { dismissSafely() }
        binding.btnWatchAd.setOnClickListener { onWatchAdClicked() }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                (resources.displayMetrics.widthPixels * 0.92).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDimAmount(0.55f)
        }
    }

    private fun applyTitleSpan() {
        val prefix = getString(R.string.unlock_no_watermark_title_prefix).trimEnd()
        val highlight = getString(R.string.unlock_no_watermark_title_highlight)
        val full = "$prefix $highlight"
        val spannable = SpannableString(full)
        val start = prefix.length + 1
        val end = full.length
        val highlightColor = ContextCompat.getColor(requireContext(), R.color.primary)
        spannable.setSpan(
            ForegroundColorSpan(highlightColor),
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        binding.tvTitle.text = spannable
    }

    private fun onWatchAdClicked() {
        if (isClickLocked) return
        val hostActivity = activity ?: return
        if (hostActivity.isFinishing) return

        isClickLocked = true
        setWatchAdEnabled(false)
        isRewardAdInProgress = true
        dialog?.hide()

        if (AdPlacement.shouldUseQuizPriority()) {
            showQuizInterstitialForUnlock(hostActivity)
            return
        }

        ADSRewardDisplay.show(
            hostActivity,
            object : ADSRewardDisplay.OnRewardAdListener {
                override fun onUserEarnedReward() {
                    completeUnlockAfterAd()
                }

                override fun onAdDismissedWithoutReward() {
                    isRewardAdInProgress = false
                    restoreDialogAfterAd()
                    unlockClickLock()
                }

                override fun onAdNotAvailable() {
                    tryQuizInterstitialFallback(hostActivity)
                }

                override fun onAdShowFailed() {
                    tryQuizInterstitialFallback(hostActivity)
                }
            },
        )
    }

    private fun showQuizInterstitialForUnlock(hostActivity: Activity) {
        val shown = AdPlacement.showQuizInterstitialAd(hostActivity) {
            completeUnlockAfterAd()
        }
        if (!shown) {
            handleQuizInterstitialUnavailable()
        }
    }

    private fun tryQuizInterstitialFallback(hostActivity: Activity) {
        if (AdPlacement.getGoogleAdFailedShowQuiz()) {
            showQuizInterstitialForUnlock(hostActivity)
            return
        }
        handleQuizInterstitialUnavailable()
    }

    private fun completeUnlockAfterAd() {
        isRewardAdInProgress = false
        listener?.onNoWatermarkUnlocked()
        dismissSafely()
    }

    private fun handleQuizInterstitialUnavailable() {
        isRewardAdInProgress = false
        restoreDialogAfterAd()
        unlockClickLock()
        showAdUnavailableToast()
    }

    private fun restoreDialogAfterAd() {
        if (!isAdded) return
        try {
            dialog?.show()
        } catch (_: Exception) {
        }
    }

    private fun unlockClickLock() {
        isClickLocked = false
        setWatchAdEnabled(true)
    }

    private fun setWatchAdEnabled(enabled: Boolean) {
        _binding ?: return
        binding.btnWatchAd.isEnabled = enabled
        binding.btnWatchAd.alpha = if (enabled) 1f else 0.6f
        binding.tvWatchAd.setTextColor(
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white)),
        )
    }

    private fun showAdUnavailableToast() {
        val ctx = context ?: return
        Toast.makeText(ctx, R.string.ad_not_available_try_again, Toast.LENGTH_SHORT).show()
    }

    private fun dismissSafely() {
        if (!isAdded) return
        try {
            dismissAllowingStateLoss()
        } catch (_: Exception) {
        }
    }

    override fun onDestroyView() {
        if (!isRewardAdInProgress) {
            ADSRewardDisplay.clearPending()
        }
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "NoWatermarkRewardDialog"

        fun show(fragmentManager: FragmentManager, listener: Listener): NoWatermarkRewardDialogFragment {
            val existing = fragmentManager.findFragmentByTag(TAG) as? NoWatermarkRewardDialogFragment
            if (existing != null) {
                existing.listener = listener
                return existing
            }
            return NoWatermarkRewardDialogFragment().apply {
                this.listener = listener
                show(fragmentManager, TAG)
            }
        }
    }
}
