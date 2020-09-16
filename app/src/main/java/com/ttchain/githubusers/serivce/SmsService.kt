package com.ttchain.githubusers.serivce

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.widget.Toast
import com.ttchain.githubusers.App
import com.ttchain.githubusers.R
import com.ttchain.githubusers.enum.Actions
import com.ttchain.githubusers.enum.ServiceState
import com.ttchain.githubusers.tools.ConnectionHelper
import com.ttchain.githubusers.tools.SMSContentObserver
import com.ttchain.githubusers.ui.sms.SmsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class SmsService : Service(), SMSContentObserver.MessageListener {

    private var smsContentObserver: SMSContentObserver? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Timber.i("Service onCreate")
        val notification = createNotification()
        startForeground(1, notification)
        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("Service onDestroy")
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_SHORT).show()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Timber.i("onTaskRemoved")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.i("onStartCommand")
        if (intent != null) {
            when (intent.action) {
                Actions.START.name -> startService()
                Actions.STOP.name -> stopService()
            }
        } else Timber.i("with a null intent. It has been probably restarted by the system.")
        return START_STICKY
    }

    private fun startService() {
        if (isServiceStarted) return
        Timber.i("startService")
        isServiceStarted = true
        App.preferenceHelper.serviceState = ServiceState.STARTED

        // we need this lock so our service gets not affected by Doze Mode
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SmsService::lock")
                    .apply { acquire(Long.MAX_VALUE) }
            }

        GlobalScope.launch(Dispatchers.IO) {
//            while (isServiceStarted) {
//                Timber.i("Run Service time ${Calendar.getInstance().time}")
//                delay(2000)
//            }
            registerSMSObserver()
//            Timber.i("End of the loop for the service")
        }
    }

    /**
     * 註冊簡訊觀察器
     */
    private fun registerSMSObserver() {
        if (smsContentObserver == null) {
            smsContentObserver =
                SMSContentObserver(applicationContext, Handler(Looper.getMainLooper()))
            smsContentObserver?.register(this)
        }
        Timber.i("registerSMSObserver")
    }

    private fun unregisterSMSObserver() {
        smsContentObserver?.unRegister()
        smsContentObserver = null
        Timber.i("unregisterSMSObserver")
    }

    override fun onReceived(message: String?) {
        Timber.i("onReceived message= $message")
        ConnectionHelper.connectDbAndInsert(message.orEmpty())
    }

    private fun stopService() {
        Timber.i("stopService")

        try {
            wakeLock?.let { if (it.isHeld) it.release() }
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            Timber.e("Service stopped without being started: ${e.message}")
        }
        isServiceStarted = false
        unregisterSMSObserver()

        App.preferenceHelper.serviceState = ServiceState.STOPPED
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "SMS DB SERVICE CHANNEL"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                notificationChannelId,
                "SMS Service Notifications Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "SMS Service Channel"
                it.enableLights(true)
                it.lightColor = Color.YELLOW
                it.enableVibration(true)
                it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent =
            Intent(this, SmsActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val builder: Notification.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
                this,
                notificationChannelId
            ) else Notification.Builder(this)

        return builder
            .setContentTitle("SMS DB Service")
            .setContentText("Keep listening SMS")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
            .build()
    }
}