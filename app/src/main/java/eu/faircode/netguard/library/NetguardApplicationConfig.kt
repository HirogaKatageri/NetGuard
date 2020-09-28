package eu.faircode.netguard.library

import android.annotation.TargetApi
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.Keep
import eu.faircode.netguard.R
import eu.faircode.netguard.Util

@Keep
class NetguardApplicationConfig(private val app: Application) {

    private val TAG = "Netguard.App"

    private var mPrevHandler: Thread.UncaughtExceptionHandler? = null

    fun onCreate() {
        Log.i(TAG, "Create version=" + Util.getSelfVersionName(app) + "/" + Util.getSelfVersionCode(app))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannels()

        mPrevHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, ex ->
            if (Util.ownFault(app, ex) && Util.isPlayStoreInstall(app)) {
                Log.e(TAG, """$ex${Log.getStackTraceString(ex)}""".trimIndent())
                mPrevHandler?.uncaughtException(thread, ex)
            } else {
                Log.w(TAG, """$ex${Log.getStackTraceString(ex)}""".trimIndent())
                System.exit(1)
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannels() {
        val nm = app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val foreground = NotificationChannel("foreground", app.getString(R.string.channel_foreground), NotificationManager.IMPORTANCE_MIN)
        foreground.setSound(null, Notification.AUDIO_ATTRIBUTES_DEFAULT)
        nm.createNotificationChannel(foreground)
        val notify = NotificationChannel("notify", app.getString(R.string.channel_notify), NotificationManager.IMPORTANCE_DEFAULT)
        notify.setSound(null, Notification.AUDIO_ATTRIBUTES_DEFAULT)
        nm.createNotificationChannel(notify)
        val access = NotificationChannel("access", app.getString(R.string.channel_access), NotificationManager.IMPORTANCE_DEFAULT)
        access.setSound(null, Notification.AUDIO_ATTRIBUTES_DEFAULT)
        nm.createNotificationChannel(access)
    }
}