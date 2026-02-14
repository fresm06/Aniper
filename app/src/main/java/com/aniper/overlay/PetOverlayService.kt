package com.aniper.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.aniper.MainActivity
import com.aniper.R
import com.aniper.data.LocalPetData
import com.aniper.model.Pet

class PetOverlayService : Service() {

    companion object {
        private const val CHANNEL_ID = "aniper_overlay_channel"
        private const val NOTIFICATION_ID = 1001
        private const val ACTION_STOP = "com.aniper.ACTION_STOP_OVERLAY"
        const val EXTRA_PET_ID = "extra_pet_id"

        fun start(context: Context, petId: String = "pet_1") {
            val intent = Intent(context, PetOverlayService::class.java).apply {
                putExtra(EXTRA_PET_ID, petId)
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, PetOverlayService::class.java))
        }
    }

    private lateinit var windowManager: WindowManager
    private val petViews = mutableListOf<PetView>()
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
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        val petId = intent?.getStringExtra(EXTRA_PET_ID) ?: "pet_1"
        addPetToOverlay(petId)

        return START_STICKY
    }

    private fun addPetToOverlay(petId: String) {
        val pet = LocalPetData.samplePets.find { it.id == petId }
            ?: Pet(id = petId, name = "Pet", assetId = "default_cat")
        val asset = LocalPetData.getAssetById(pet.assetId)

        val petView = PetView(
            context = this,
            pet = pet,
            asset = asset,
            windowManager = windowManager,
            screenWidth = screenWidth,
            screenHeight = screenHeight
        )

        try {
            windowManager.addView(petView, petView.wmParams)
            petViews.add(petView)
            petView.startWalking()
        } catch (e: Exception) {
            e.printStackTrace()
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
            "Aniper Overlay",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows pet overlay notification"
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

        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, PetOverlayService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Aniper")
            .setContentText("Your pet is active!")
            .setSmallIcon(R.drawable.ic_pet_notification)
            .setContentIntent(openIntent)
            .addAction(R.drawable.ic_stop, "Stop", stopIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        petViews.forEach { it.destroy() }
        petViews.clear()
    }
}
