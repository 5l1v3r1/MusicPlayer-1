package com.ftmusic.hadise
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class getBackgroundNotification(private val context: Context, private var myService: MusicService, private val musicName:String) : AsyncTask<Long, Void, Any>() {

    override fun doInBackground(vararg params: Long?): Any? {

        //Create Channel
        createChannel(context)
        var notifyId = 101

        var notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifyIntent = Intent(context, MainActivity::class.java)

        val title = musicName
        val message = context.resources.getString(R.string.app_name)

        notifyIntent.putExtra("title", message)
        notifyIntent.putExtra("message", title)
        notifyIntent.putExtra("notification", true)

        notifyIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK

        val pendingIntent = PendingIntent.getActivity(context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            var builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.play_r)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setColor(ContextCompat.getColor(context, R.color.colorBorder))
                .setStyle(NotificationCompat.BigTextStyle())
                .build()

            with(NotificationManagerCompat.from(context)) {
                notificationManager.notify(notifyId, builder)
            }
            myService.startForeground(notifyId, builder)

        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                var builder = NotificationCompat.Builder(context, CHANNEL_ID)
                        // Set the intent that will fire when the user taps the notification
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(R.mipmap.play_r)
                        .setAutoCancel(true)
                        .setContentTitle(title)
                        .setColor(ContextCompat.getColor(context, R.color.colorBorder))
                        .setStyle(NotificationCompat.BigTextStyle())
                        .build()
                with(NotificationManagerCompat.from(context)) {
                    notificationManager.notify(notifyId, builder)
                }
                myService.startForeground(notifyId, builder)

            } else {
                var builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.mipmap.play_r)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setStyle(NotificationCompat.BigTextStyle())
                    .build()
                with(NotificationManagerCompat.from(context)) {
                    notificationManager.notify(notifyId, builder)
                }
                myService.startForeground(notifyId, builder)

            }

        }

        return null
    }




    companion object {
        const val CHANNEL_ID = "ForegroundServiceChannel"
        const val CHANNEL_NAME = "ForegroundServiceName"
    }

    @SuppressLint("ResourceType")
    private fun createChannel(context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
            notificationChannel.enableVibration(true)
            notificationChannel.setShowBadge(true)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = ContextCompat.getColor(context, R.color.colorBorder)
            notificationChannel.description = context.resources.getString(R.string.app_name)
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

            notificationChannel.setSound(null, null)

            notificationManager.createNotificationChannel(notificationChannel)
        }

    }
}