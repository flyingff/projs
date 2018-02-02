package net.flyingff.soundnotification

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.IBinder

class SoundService : Service() {
    companion object {
        val NOTIFICATION_ID = 10086
        val ACTION_CLOSE = "net.flyingff.CLOSE"
        val ACTION_PLAY = "net.flyingff.PLAY"
    }
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private var ringId : Int = -1
    private val pool : SoundPool by lazy {
        val pool = SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build())
                .build()
        ringId = pool.load(this, R.raw.ding, 1)
        // play after load
        pool.setOnLoadCompleteListener { _, _, status ->
            if(status == 0) {
                pool.play(ringId, .8f, .8f, 0, 0, 1f)
            }
        }
        pool
    }
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when(intent.action) {
            ACTION_CLOSE -> stopSelf()
            ACTION_PLAY -> {
                pool.play(ringId, .8f, .8f, 0, 0, 1f)
            }
            "android.intent.action.MAIN" -> {
                println("Service started.")
            }
            else -> {
                System.err.println("Unknown intent: $intent")
            }
        }
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        val intentExit = Intent(ACTION_CLOSE)

        val notification = Notification.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.icon))
                .setContentTitle("Ding! Service")
                .setContentText("Tap to close me")
                .setAutoCancel(false)
                .setContentIntent(PendingIntent.getService(this, 1, intentExit, 0))
                .build()
        startForeground(NOTIFICATION_ID, notification)
    }
}
