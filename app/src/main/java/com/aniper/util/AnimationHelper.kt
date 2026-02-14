package com.aniper.util

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.OvershootInterpolator

/**
 * Helper class for pet view animations (squash, stretch, bounce, etc.)
 */
class AnimationHelper {

    /**
     * Play a tap bounce reaction - the pet jumps up slightly and comes back.
     */
    fun playTapBounce(view: View) {
        val jumpUp = ObjectAnimator.ofFloat(view, "translationY", 0f, -40f, 0f).apply {
            duration = 400
            interpolator = BounceInterpolator()
        }
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.15f, 1f).apply {
            duration = 400
            interpolator = OvershootInterpolator()
        }
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.15f, 1f).apply {
            duration = 400
            interpolator = OvershootInterpolator()
        }

        AnimatorSet().apply {
            playTogether(jumpUp, scaleX, scaleY)
            start()
        }
    }

    /**
     * Play landing squash animation - pet squishes on impact then returns to normal.
     */
    fun playLandingSquash(view: View) {
        val squashX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.3f, 0.9f, 1f).apply {
            duration = 350
            interpolator = AccelerateDecelerateInterpolator()
        }
        val squashY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.7f, 1.1f, 1f).apply {
            duration = 350
            interpolator = AccelerateDecelerateInterpolator()
        }

        AnimatorSet().apply {
            playTogether(squashX, squashY)
            start()
        }
    }

    /**
     * Play grabbed wiggle animation - the pet wiggles when grabbed.
     * [DISABLED - wiggle animation removed]
     */
    fun playGrabbedWiggle(view: View) {
        // Wiggle animation disabled per user request
    }

    /**
     * Stop all animations on a view.
     */
    fun stopAnimations(view: View) {
        view.animate().cancel()
        view.rotation = 0f
        view.scaleX = 1f
        view.scaleY = 1f
        view.translationY = 0f
    }

    /**
     * Fade in animation for when a pet appears.
     */
    fun playAppearAnimation(view: View) {
        view.alpha = 0f
        view.scaleX = 0.5f
        view.scaleY = 0.5f

        val fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.5f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.5f, 1f)

        AnimatorSet().apply {
            playTogether(fadeIn, scaleX, scaleY)
            duration = 500
            interpolator = OvershootInterpolator()
            start()
        }
    }

    /**
     * Fade out animation for when a pet disappears.
     */
    fun playDisappearAnimation(view: View, onEnd: () -> Unit = {}) {
        val fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f)
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.3f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.3f)

        AnimatorSet().apply {
            playTogether(fadeOut, scaleX, scaleY)
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        view.postDelayed(onEnd, 300)
    }
}
