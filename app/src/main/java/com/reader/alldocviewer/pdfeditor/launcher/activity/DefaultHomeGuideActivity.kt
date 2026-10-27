package com.reader.alldocviewer.pdfeditor.launcher.activity

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.reader.alldocviewer.pdfeditor.databinding.ActivityDefaultHomeGuideBinding
import com.reader.alldocviewer.pdfeditor.launcher.common.Utils

/**
 * Guide card at the bottom of the screen, shown on top of the system "Default home app" list.
 * It is a normal translucent activity, so it needs no overlay permission. Any touch closes it,
 * and it closes by itself once this app is the Home app.
 */
class DefaultHomeGuideActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDefaultHomeGuideBinding
    private var tapAnimator: ValueAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDefaultHomeGuideBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.guideIcon.setImageDrawable(applicationInfo.loadIcon(packageManager))
        binding.guideName.text = applicationInfo.loadLabel(packageManager)

        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.attributes = window.attributes.apply {
            gravity = Gravity.BOTTOM
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            dimAmount = DIM_AMOUNT
        }
        setFinishOnTouchOutside(true)
        binding.guideRoot.setOnClickListener { finish() }
        startTapAnimation()
    }

    // Settings opens its list a moment after this card starts and covers it: show the card once more,
    // so the second copy sits on top of the system list.
    override fun onPause() {
        super.onPause()
        if (!isFinishing && !intent.getBooleanExtra(EXTRA_RESHOWN, false)) {
            startActivity(
                newIntent(this)
                    .putExtra(EXTRA_RESHOWN, true)
                    // MULTIPLE_TASK: without it Android only brings the same task to the front.
                    .addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK or
                            Intent.FLAG_ACTIVITY_NO_ANIMATION
                    )
            )
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Utils.isDefaultHomeApp(this)) finish()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        finish()
        return super.dispatchTouchEvent(ev)
    }

    override fun onDestroy() {
        tapAnimator?.cancel()
        super.onDestroy()
    }

    /** Looping "tap" on the radio: the hand rises while the ripple and the dot grow in. */
    private fun startTapAnimation() {
        val rise = RISE_DP * resources.displayMetrics.density
        tapAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = TAP_DURATION_MILLIS
            repeatCount = ValueAnimator.INFINITE
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                val progress = animation.animatedValue as Float
                val rippleScale = RIPPLE_MIN_SCALE + (1f - RIPPLE_MIN_SCALE) * progress
                val dotScale = DOT_MIN_SCALE + (1f - DOT_MIN_SCALE) * progress
                binding.guideHand.translationY = rise * (1f - progress)
                binding.guideRipple.alpha = progress
                binding.guideRipple.scaleX = rippleScale
                binding.guideRipple.scaleY = rippleScale
                binding.guideRadioDot.alpha = progress
                binding.guideRadioDot.scaleX = dotScale
                binding.guideRadioDot.scaleY = dotScale
            }
            start()
        }
    }

    companion object {
        private const val EXTRA_RESHOWN = "extra_reshown"
        private const val DIM_AMOUNT = 0.2f
        private const val RISE_DP = 30f
        private const val TAP_DURATION_MILLIS = 900L
        private const val RIPPLE_MIN_SCALE = 0.55f
        private const val DOT_MIN_SCALE = 0.3f

        @JvmStatic
        fun newIntent(context: Context): Intent = Intent(context, DefaultHomeGuideActivity::class.java)

        /** Starts the card in its own task so it sits above the system screen. */
        @JvmStatic
        fun show(context: Context) {
            try {
                context.startActivity(
                    newIntent(context)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
                )
            } catch (e: RuntimeException) {
                Log.w("DefaultHomeGuide", "Could not show the default Home guide", e)
            }
        }
    }
}
