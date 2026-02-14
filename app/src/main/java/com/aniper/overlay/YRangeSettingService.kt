package com.aniper.overlay

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import androidx.core.app.NotificationCompat
import com.aniper.MainActivity
import com.aniper.R
import com.aniper.util.PreferencesHelper

/**
 * Service for setting Y-axis range by dragging lines on the overlay.
 * User can directly interact with the overlay to define pet movement boundaries.
 */
class YRangeSettingService : Service() {

    companion object {
        private const val CHANNEL_ID = "aniper_y_range_setting_channel"
        private const val NOTIFICATION_ID = 1002

        fun start(context: Context) {
            val intent = Intent(context, YRangeSettingService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, YRangeSettingService::class.java))
        }
    }

    private lateinit var windowManager: WindowManager
    private lateinit var settingView: YRangeSettingView
    private var screenWidth = 0
    private var screenHeight = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        updateScreenSize()
        createNotificationChannel()

        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showSettingView()
        return START_STICKY
    }

    private fun showSettingView() {
        settingView = YRangeSettingView(this, screenWidth, screenHeight) { isConfirmed ->
            if (isConfirmed) {
                // Settings saved by YRangeSettingView to SharedPreferences
                // Notify PetOverlayService to update pet ranges
                broadcastSettingUpdate()
            }
            stopSelf()
        }

        val wmParams = WindowManager.LayoutParams(
            screenWidth,
            screenHeight,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
        }

        try {
            windowManager.addView(settingView, wmParams)
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun updateScreenSize() {
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealMetrics(metrics)
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Y-Range Setting",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Adjust pet movement range"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Aniper - Setting Range")
            .setContentText("Drag the lines to set pet movement range")
            .setSmallIcon(R.drawable.ic_pet_notification)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .build()
    }

    private fun broadcastSettingUpdate() {
        val intent = Intent(PetOverlayService.ACTION_UPDATE_Y_RANGE)
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            windowManager.removeView(settingView)
        } catch (_: Exception) {}
    }
}

/**
 * Custom view for Y-range setting overlay.
 * Displays bottom boundary lines that can be dragged.
 * User sets the movement range by dragging the lines directly on screen.
 */
@SuppressLint("ViewConstructor")
class YRangeSettingView(
    context: Context,
    private val screenWidth: Int,
    private val screenHeight: Int,
    private val onDone: (isConfirmed: Boolean) -> Unit
) : FrameLayout(context) {

    private val lineHeight = 8 // dp
    private val density = context.resources.displayMetrics.density
    private val lineHeightPx = (lineHeight * density).toInt()

    // Initialize with current saved settings from SharedPreferences
    private var topLineY = (screenHeight * PreferencesHelper.getYMinPercent(context)).toInt()
    private var bottomLineY = (screenHeight * PreferencesHelper.getYMaxPercent(context)).toInt()

    init {
        setBackgroundColor(0x00000000) // Transparent
        setupUI()
    }

    private fun setupUI() {
        // Bottom line only (no top line)
        val bottomLine = DraggableLineView(context, false) { newY ->
            bottomLineY = newY.coerceIn(topLineY + 50, screenHeight)
            invalidate()
        }
        bottomLine.translationY = bottomLineY.toFloat()
        addView(bottomLine, LayoutParams(screenWidth, lineHeightPx))

        // OK Button - positioned at top
        val okButton = Button(context).apply {
            text = "OK"
            setBackgroundColor(0xFFFF6B9E.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 16f
        }
        okButton.setOnClickListener {
            saveSettings()
            onDone(true)
        }
        addView(okButton, LayoutParams(
            (100 * density).toInt(),
            (48 * density).toInt(),
            Gravity.TOP or Gravity.END
        ).apply {
            rightMargin = (16 * density).toInt()
            topMargin = (16 * density).toInt()
        })

        // Cancel Button - positioned at top
        val cancelButton = Button(context).apply {
            text = "Cancel"
            setBackgroundColor(0xFF999999.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 16f
        }
        cancelButton.setOnClickListener {
            onDone(false)
        }
        addView(cancelButton, LayoutParams(
            (100 * density).toInt(),
            (48 * density).toInt(),
            Gravity.TOP or Gravity.END
        ).apply {
            rightMargin = (120 * density).toInt()
            topMargin = (16 * density).toInt()
        })
    }

    private fun saveSettings() {
        val topPercent = topLineY.toFloat() / screenHeight
        val bottomPercent = bottomLineY.toFloat() / screenHeight
        PreferencesHelper.setYRange(context, topPercent, bottomPercent)
    }

    @SuppressLint("ClickableViewAccessibility")
    private inner class DraggableLineView(
        context: Context,
        val isTopLine: Boolean,
        val onPositionChanged: (Int) -> Unit
    ) : View(context) {

        private var grabOffsetY = 0f

        init {
            setBackgroundColor(0xFFE91E63.toInt())  // Only pink color (bottom line)
            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Save the offset: difference between raw screen position and view's current position
                        grabOffsetY = event.rawY - translationY
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        // Calculate new position: rawY minus the offset
                        val newY = (event.rawY - grabOffsetY).toInt().coerceIn(0, screenHeight - lineHeightPx)
                        translationY = newY.toFloat()
                        onPositionChanged(newY)
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        true
                    }
                    else -> false
                }
            }
        }
    }
}
