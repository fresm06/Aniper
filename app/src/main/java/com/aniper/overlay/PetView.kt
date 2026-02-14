package com.aniper.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.aniper.model.Pet
import com.aniper.model.PetAsset
import com.aniper.model.PetState
import com.aniper.overlay.PetOverlayService
import com.aniper.util.AnimationHelper
import com.aniper.util.PreferencesHelper
import kotlin.math.abs
import kotlin.random.Random

@SuppressLint("ViewConstructor")
class PetView(
    context: Context,
    internal val pet: Pet,
    private val asset: PetAsset,
    private val windowManager: WindowManager,
    private val screenWidth: Int,
    private val screenHeight: Int,
    private val service: PetOverlayService
) : FrameLayout(context) {

    private val imageView: ImageView
    private val physicsEngine = PhysicsEngine()
    private val handler = Handler(Looper.getMainLooper())
    private val animationHelper = AnimationHelper()

    private var currentState = PetState.IDLE
    private var isGrabbed = false
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var lastMoveX = 0f
    private var lastMoveY = 0f
    private var grabOffsetX = 0f
    private var grabOffsetY = 0f
    private var walkingRight = true
    private var isCurrentlyWalking = false

    private val density = context.resources.displayMetrics.density
    private val petSize = (asset.width * density).toInt()
    private val topBoundary: Float
        get() = screenHeight * PreferencesHelper.getYMinPercent(context)
    private val groundY: Float
        get() {
            val maxPercent = PreferencesHelper.getYMaxPercent(context)
            return (screenHeight * maxPercent - petSize).toFloat()
        }

    // Use wmParams instead of layoutParams to avoid shadowing View.getLayoutParams()
    val wmParams: WindowManager.LayoutParams = WindowManager.LayoutParams(
        petSize,
        petSize,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.START
        x = Random.nextInt(0, (screenWidth - petSize).coerceAtLeast(1))
        y = groundY.toInt()
    }

    // Create a new walk runnable each time - walks for a set duration then stops
    private fun createWalkRunnable(): Runnable {
        val walkStartTime = System.currentTimeMillis()
        val walkDuration = Random.nextLong(2000, 5000) // 2-5 seconds of walking

        return object : Runnable {
            override fun run() {
                if (isGrabbed || physicsEngine.isInAir() || !isCurrentlyWalking) return

                val elapsedTime = System.currentTimeMillis() - walkStartTime
                if (elapsedTime >= walkDuration) {
                    // Stop walking and schedule next behavior
                    scheduleNextBehavior()
                    return
                }

                val stepSize = (2 * density).toInt().coerceAtLeast(1)
                val direction = if (walkingRight) 1 else -1
                val newX = wmParams.x + direction * stepSize

                // Bounce off edges
                if (newX <= 0) {
                    walkingRight = true
                    setState(PetState.WALKING_RIGHT)
                } else if (newX >= screenWidth - petSize) {
                    walkingRight = false
                    setState(PetState.WALKING_LEFT)
                }

                wmParams.x = newX.coerceIn(0, screenWidth - petSize)
                updateView()

                handler.postDelayed(this, 32) // ~30fps walking
            }
        }
    }

    // Physics update runnable
    private val physicsRunnable = object : Runnable {
        private var lastTime = 0L

        fun resetTimer() {
            lastTime = System.nanoTime()
        }

        override fun run() {
            val now = System.nanoTime()
            val dt = ((now - lastTime) / 1_000_000_000f).coerceAtMost(0.05f) // cap at 50ms
            lastTime = now

            if (!physicsEngine.isInAir()) return

            val result = physicsEngine.update(
                dt,
                wmParams.x.toFloat(),
                wmParams.y.toFloat(),
                groundY,
                (screenWidth - petSize).coerceAtLeast(0)
            )

            wmParams.x = result.x.toInt()
            wmParams.y = result.y.toInt()
            updateView()

            if (result.landed) {
                onLanded()
            } else {
                handler.postDelayed(this, 16)
            }
        }
    }

    init {
        imageView = ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        addView(imageView)
        setupTouchListener()
        setState(PetState.IDLE)
    }

    fun startWalking() {
        scheduleNextBehavior()
    }

    private fun scheduleNextBehavior() {
        isCurrentlyWalking = false

        // Randomly decide: 60% walk, 40% idle
        val shouldWalk = Random.nextInt(100) < 60

        if (shouldWalk) {
            walkingRight = Random.nextBoolean()
            setState(if (walkingRight) PetState.WALKING_RIGHT else PetState.WALKING_LEFT)
            isCurrentlyWalking = true
            val walkRunnable = createWalkRunnable()
            handler.post(walkRunnable)
        } else {
            // Stay idle for 1-3 seconds
            setState(PetState.IDLE)
            val idleDuration = Random.nextLong(1000, 3000)
            handler.postDelayed({
                scheduleNextBehavior()
            }, idleDuration)
        }
    }

    fun stopAllBehavior() {
        handler.removeCallbacksAndMessages(null)
    }

    private fun updateView() {
        try {
            windowManager.updateViewLayout(this@PetView, wmParams)
        } catch (_: Exception) {}
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener() {
        var longPressTriggered = false
        val longPressTimeout = 400L

        val longPressRunnable = Runnable {
            longPressTriggered = true
            onGrabbed()
        }

        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastTouchX = event.rawX
                    lastTouchY = event.rawY
                    lastMoveX = event.rawX
                    lastMoveY = event.rawY
                    grabOffsetX = event.rawX - wmParams.x
                    grabOffsetY = event.rawY - wmParams.y
                    longPressTriggered = false
                    handler.postDelayed(longPressRunnable, longPressTimeout)
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (longPressTriggered || isGrabbed) {
                        lastMoveX = event.rawX
                        lastMoveY = event.rawY
                        wmParams.x = (event.rawX - grabOffsetX).toInt()
                        wmParams.y = (event.rawY - grabOffsetY).toInt()
                        updateView()

                        // Check for trash bin collision during drag
                        if (service.checkTrashBinCollision(wmParams.x, wmParams.y, petSize)) {
                            service.highlightTrashBin()
                        } else {
                            service.resetTrashBinHighlight()
                        }
                    } else {
                        val dx = abs(event.rawX - lastTouchX)
                        val dy = abs(event.rawY - lastTouchY)
                        if (dx > 20 || dy > 20) {
                            handler.removeCallbacks(longPressRunnable)
                        }
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    handler.removeCallbacks(longPressRunnable)
                    if (isGrabbed) {
                        onReleased()
                    } else if (!longPressTriggered) {
                        onTapped()
                    }
                    true
                }

                else -> false
            }
        }
    }

    private fun onTapped() {
        isCurrentlyWalking = false
        setState(PetState.TAP_REACTION)
        animationHelper.playTapBounce(this)

        handler.postDelayed({
            scheduleNextBehavior()
        }, 800)
    }

    private fun onGrabbed() {
        isGrabbed = true
        isCurrentlyWalking = false
        setState(PetState.GRABBED)
        animationHelper.playGrabbedWiggle(imageView)
        service.showTrashBin()
    }

    private fun onReleased() {
        isGrabbed = false
        animationHelper.stopAnimations(imageView)
        service.resetTrashBinHighlight()

        // Check if pet was released over trash bin
        if (service.checkTrashBinCollision(wmParams.x, wmParams.y, petSize)) {
            playDeleteAnimation()
            return
        }

        service.hideTrashBin()
        setState(PetState.FALLING)

        val velocityX = (lastMoveX - lastTouchX) * 2f
        physicsEngine.reset()
        physicsEngine.startFalling(velocityX, 0f)
        physicsRunnable.resetTimer()
        handler.post(physicsRunnable)
    }

    private fun onLanded() {
        setState(PetState.LANDING)
        animationHelper.playLandingSquash(this)

        handler.postDelayed({
            // Stay idle for 1-2 seconds before next behavior
            setState(PetState.IDLE)
            val idleDuration = Random.nextLong(1000, 2000)
            handler.postDelayed({
                scheduleNextBehavior()
            }, idleDuration)
        }, 600)
    }

    private fun setState(state: PetState) {
        // Skip if already in this state to prevent unnecessary drawable updates
        if (currentState == state) return

        currentState = state
        pet.state = state
        setDrawableForState(state)
    }

    private fun setDrawableForState(state: PetState) {
        val resId = asset.getResForState(state)
        if (resId != 0) {
            imageView.setImageDrawable(ContextCompat.getDrawable(context, resId))
        }
        imageView.scaleX = if (state == PetState.WALKING_LEFT) -1f else 1f
    }

    private fun playDeleteAnimation() {
        stopAllBehavior()
        service.resetTrashBinHighlight()
        animationHelper.playDeleteAnimation(this) {
            service.removePet(this)
            destroy()
        }
    }

    fun destroy() {
        stopAllBehavior()
        try {
            windowManager.removeView(this)
        } catch (_: Exception) {}
    }
}
