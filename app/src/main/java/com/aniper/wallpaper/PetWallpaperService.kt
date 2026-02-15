package com.aniper.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.aniper.data.LocalPetData
import com.aniper.model.Pet

class PetWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = PetWallpaperEngine()

    private inner class PetWallpaperEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private val drawRunnable = object : Runnable {
            override fun run() {
                draw()
                handler.postDelayed(this, 33) // ~30 FPS
            }
        }

        private var screenWidth = 0
        private var screenHeight = 0
        private var petX = 0f
        private var petY = 0f
        private var velocityX = 2f
        private var isRunning = false
        private var animationFrame = 0

        init {
            setTouchEventsEnabled(false)
            initializePosition()
        }

        private fun initializePosition() {
            val frame = surfaceHolder.surfaceFrame
            screenWidth = frame.width()
            screenHeight = frame.height()
            petX = screenWidth / 2f
            petY = screenHeight / 2f
        }

        private fun draw() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    // Draw background
                    canvas.drawColor(Color.parseColor("#F9F7F4"))

                    // Update pet position
                    updatePet()

                    // Draw pet
                    drawPet(canvas)
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas)
                }
            }
        }

        private fun updatePet() {
            // Move pet horizontally
            petX += velocityX

            // Bounce at edges
            if (petX <= 40f || petX >= screenWidth - 40f) {
                velocityX *= -1f
                petX = petX.coerceIn(40f, screenWidth - 40f)
            }

            // Slight vertical movement
            animationFrame = (animationFrame + 1) % 60
            val bobAmount = kotlin.math.sin(animationFrame.toFloat() / 10f) * 5f
            petY = (screenHeight / 2f) + bobAmount
        }

        private fun drawPet(canvas: Canvas) {
            val petSize = 100f
            val paint = Paint().apply {
                color = Color.parseColor("#F0A8D8")
                isAntiAlias = true
            }

            // Draw pet circle
            canvas.drawCircle(petX, petY, petSize / 2, paint)

            // Draw eyes
            val eyePaint = Paint().apply {
                color = Color.WHITE
                isAntiAlias = true
            }
            canvas.drawCircle(petX - 15f, petY - 15f, 8f, eyePaint)
            canvas.drawCircle(petX + 15f, petY - 15f, 8f, eyePaint)

            // Draw pupils
            val pupilPaint = Paint().apply {
                color = Color.parseColor("#F0A8D8")
                isAntiAlias = true
            }
            val pupilOffset = kotlin.math.sin(animationFrame.toFloat() / 15f) * 3f
            canvas.drawCircle(petX - 15f + pupilOffset, petY - 15f, 4f, pupilPaint)
            canvas.drawCircle(petX + 15f + pupilOffset, petY - 15f, 4f, pupilPaint)

            // Draw smile
            val mouthPaint = Paint().apply {
                color = Color.parseColor("#F0A8D8")
                strokeWidth = 2f
                isAntiAlias = true
                style = Paint.Style.STROKE
            }
            canvas.drawArc(petX - 10f, petY, petX + 10f, petY + 15f, 0f, 180f, false, mouthPaint)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                isRunning = true
                handler.post(drawRunnable)
            } else {
                isRunning = false
                handler.removeCallbacks(drawRunnable)
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            handler.removeCallbacks(drawRunnable)
        }
    }
}
