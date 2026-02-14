package com.aniper.overlay

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.aniper.R
import com.aniper.util.AnimationHelper

/**
 * Trash bin view displayed at the bottom center of the screen.
 * Used for deleting pets via drag and drop.
 */
@SuppressLint("ViewConstructor")
class TrashBinView(
    context: Context,
    private val windowManager: WindowManager,
    screenWidth: Int
) : FrameLayout(context) {

    private val imageView: ImageView
    private val size = 80  // 80dp
    private val density = context.resources.displayMetrics.density
    private val sizePx = (size * density).toInt()
    private var isHighlighted = false
    private var pulseAnimatorSet: AnimatorSet? = null

    val wmParams: WindowManager.LayoutParams = WindowManager.LayoutParams(
        sizePx,
        sizePx,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        x = 0
        y = 100  // 100px above bottom
    }

    init {
        setBackgroundResource(R.drawable.trash_bin_background)

        imageView = ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_trash_bin))
        }
        addView(imageView)
    }

    fun startPulseAnimation() {
        stopPulseAnimation()

        val scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.1f, 1f)
        val scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.1f, 1f)

        scaleX.duration = 600
        scaleY.duration = 600
        scaleX.repeatCount = ObjectAnimator.INFINITE
        scaleX.repeatMode = ObjectAnimator.RESTART
        scaleY.repeatCount = ObjectAnimator.INFINITE
        scaleY.repeatMode = ObjectAnimator.RESTART

        pulseAnimatorSet = AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            start()
        }
    }

    fun stopPulseAnimation() {
        pulseAnimatorSet?.cancel()
        pulseAnimatorSet = null
        scaleX = 1f
        scaleY = 1f
    }

    fun highlightForCollision() {
        if (isHighlighted) return
        isHighlighted = true

        // Animate to red and larger
        AnimatorSet().apply {
            val scaleX = ObjectAnimator.ofFloat(this@TrashBinView, "scaleX", 1f, 1.3f)
            val scaleY = ObjectAnimator.ofFloat(this@TrashBinView, "scaleY", 1f, 1.3f)

            scaleX.duration = 200
            scaleY.duration = 200

            playTogether(scaleX, scaleY)
            start()
        }

        // Change background color to red
        setBackgroundResource(R.drawable.trash_bin_background_highlighted)
    }

    fun resetHighlight() {
        if (!isHighlighted) return
        isHighlighted = false

        // Animate back to normal
        AnimatorSet().apply {
            val scaleX = ObjectAnimator.ofFloat(this@TrashBinView, "scaleX", 1.3f, 1f)
            val scaleY = ObjectAnimator.ofFloat(this@TrashBinView, "scaleY", 1.3f, 1f)

            scaleX.duration = 200
            scaleY.duration = 200

            playTogether(scaleX, scaleY)
            start()
        }

        // Change background back to normal
        setBackgroundResource(R.drawable.trash_bin_background)
    }

    fun slideIn(onComplete: () -> Unit = {}) {
        stopPulseAnimation()
        scaleX = 0.3f
        scaleY = 0.3f
        alpha = 0f
        translationY = 100f

        AnimatorSet().apply {
            val scaleXAnim = ObjectAnimator.ofFloat(this@TrashBinView, "scaleX", 0.3f, 1f)
            val scaleYAnim = ObjectAnimator.ofFloat(this@TrashBinView, "scaleY", 0.3f, 1f)
            val alphaAnim = ObjectAnimator.ofFloat(this@TrashBinView, "alpha", 0f, 1f)
            val transYAnim = ObjectAnimator.ofFloat(this@TrashBinView, "translationY", 100f, 0f)

            scaleXAnim.duration = 300
            scaleYAnim.duration = 300
            alphaAnim.duration = 300
            transYAnim.duration = 300

            playTogether(scaleXAnim, scaleYAnim, alphaAnim, transYAnim)
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    onComplete()
                    startPulseAnimation()
                }
            })
            start()
        }
    }

    fun slideOut() {
        stopPulseAnimation()
        isHighlighted = false

        AnimatorSet().apply {
            val scaleX = ObjectAnimator.ofFloat(this@TrashBinView, "scaleX", 1f, 0.3f)
            val scaleY = ObjectAnimator.ofFloat(this@TrashBinView, "scaleY", 1f, 0.3f)
            val alpha = ObjectAnimator.ofFloat(this@TrashBinView, "alpha", 1f, 0f)
            val transY = ObjectAnimator.ofFloat(this@TrashBinView, "translationY", 0f, 100f)

            scaleX.duration = 300
            scaleY.duration = 300
            alpha.duration = 300
            transY.duration = 300

            playTogether(scaleX, scaleY, alpha, transY)
            start()
        }
    }

    fun destroy() {
        stopPulseAnimation()
        try {
            windowManager.removeView(this)
        } catch (_: Exception) {}
    }
}
