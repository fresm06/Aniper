package com.aniper.util

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.OvershootInterpolator

/**
 * Helper class for pet view animations (squash, stretch, bounce, etc.)
 */
class AnimationHelper {
    private val runningAnimations = mutableMapOf<View, AnimatorSet>()

    /**
     * Play a tap bounce reaction - the pet jumps up slightly and comes back.
     */
    fun playTapBounce(view: View) {
        // Cancel any previous animations on this view
        cancelAnimations(view)

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

        val animatorSet = AnimatorSet().apply {
            playTogether(jumpUp, scaleX, scaleY)
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    runningAnimations.remove(view)
                }
            })
            start()
        }
        runningAnimations[view] = animatorSet
    }

    /**
     * Play landing squash animation - pet squishes on impact then returns to normal.
     */
    fun playLandingSquash(view: View) {
        // Cancel any previous animations on this view
        cancelAnimations(view)

        val squashX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.3f, 0.9f, 1f).apply {
            duration = 600
            interpolator = AccelerateDecelerateInterpolator()
        }
        val squashY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.7f, 1.1f, 1f).apply {
            duration = 600
            interpolator = AccelerateDecelerateInterpolator()
        }

        val animatorSet = AnimatorSet().apply {
            playTogether(squashX, squashY)
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    runningAnimations.remove(view)
                }
            })
            start()
        }
        runningAnimations[view] = animatorSet
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
        cancelAnimations(view)
        view.animate().cancel()
        // Reset to neutral state
        view.rotation = 0f
        view.scaleX = 1f
        view.scaleY = 1f
        view.translationY = 0f
        view.alpha = 1f
    }

    /**
     * Cancel running animations for a view without resetting values.
     */
    private fun cancelAnimations(view: View) {
        runningAnimations[view]?.let {
            if (it.isRunning) {
                it.cancel()
            }
            runningAnimations.remove(view)
        }
    }

    /**
     * Fade in animation for when a pet appears.
     */
    fun playAppearAnimation(view: View) {
        cancelAnimations(view)

        view.alpha = 0f
        view.scaleX = 0.5f
        view.scaleY = 0.5f

        val fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.5f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.5f, 1f)

        val animatorSet = AnimatorSet().apply {
            playTogether(fadeIn, scaleX, scaleY)
            duration = 500
            interpolator = OvershootInterpolator()
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    runningAnimations.remove(view)
                }
            })
            start()
        }
        runningAnimations[view] = animatorSet
    }

    /**
     * Fade out animation for when a pet disappears.
     */
    fun playDisappearAnimation(view: View, onEnd: () -> Unit = {}) {
        cancelAnimations(view)

        val fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f)
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.3f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.3f)

        val animatorSet = AnimatorSet().apply {
            playTogether(fadeOut, scaleX, scaleY)
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    runningAnimations.remove(view)
                    onEnd()
                }
            })
            start()
        }
        runningAnimations[view] = animatorSet
    }

    /**
     * Delete animation - pet rotates, shrinks, and fades out.
     * Plays when a pet is dropped into the trash bin.
     */
    fun playDeleteAnimation(view: View, onEnd: () -> Unit = {}) {
        cancelAnimations(view)

        val rotation = ObjectAnimator.ofFloat(view, "rotation", 0f, 720f).apply {
            duration = 600
            interpolator = AccelerateInterpolator()
        }
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0f).apply {
            duration = 600
            interpolator = AccelerateInterpolator()
        }
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0f).apply {
            duration = 600
            interpolator = AccelerateInterpolator()
        }
        val fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f).apply {
            duration = 600
            interpolator = AccelerateInterpolator()
        }

        val animatorSet = AnimatorSet().apply {
            playTogether(rotation, scaleX, scaleY, fadeOut)
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    runningAnimations.remove(view)
                    onEnd()
                }
            })
            start()
        }
        runningAnimations[view] = animatorSet
    }
}
