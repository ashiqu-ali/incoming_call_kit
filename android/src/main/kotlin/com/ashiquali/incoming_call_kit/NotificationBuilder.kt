package com.ashiquali.incoming_call_kit

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.Person

object NotificationBuilder {

    fun getNotificationId(callId: String): Int {
        return (callId.hashCode() and 0x7FFFFFFF) % 100000 + 10000
    }

    fun getMissedNotificationId(callId: String): Int {
        return (callId.hashCode() and 0x7FFFFFFF) % 100000 + 200000
    }

    fun getOngoingNotificationId(callId: String): Int {
        return (callId.hashCode() and 0x7FFFFFFF) % 100000 + 300000
    }

    fun buildIncomingCallNotification(
        context: Context,
        callId: String,
        callerName: String,
        callerNumber: String?,
        androidConfig: Map<String, Any?>?
    ): Notification {
        createIncomingCallChannel(context, androidConfig)

        val requestBase = (callId.hashCode() and 0x7FFFFFFF) % 10000

        // Full screen intent → IncomingCallActivity
        val fullScreenIntent = Intent(context, IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(Constants.EXTRA_CALL_ID, callId)
        }
        val fullScreenPI = PendingIntent.getActivity(
            context, requestBase + 1000, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Answer action → AnswerTrampolineActivity
        val answerIntent = Intent(context, AnswerTrampolineActivity::class.java).apply {
            putExtra(Constants.EXTRA_CALL_ID, callId)
        }
        val answerPI = PendingIntent.getActivity(
            context, requestBase + 2000, answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Decline action → IncomingCallService
        val declineIntent = Intent(context, IncomingCallService::class.java).apply {
            action = Constants.ACTION_DECLINE
            putExtra(Constants.EXTRA_CALL_ID, callId)
        }
        val declinePI = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(
                context, requestBase + 3000, declineIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getService(
                context, requestBase + 3000, declineIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val textAccept = "Accept"
        val textDecline = "Decline"

        val person = Person.Builder()
            .setName(callerName)
            .setImportant(true)
            .build()

        val builder = NotificationCompat.Builder(context, Constants.INCOMING_CALL_CHANNEL_ID)
            .setContentTitle(callerName)
            .setContentText(callerNumber ?: "Incoming Call")
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setFullScreenIntent(fullScreenPI, true)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // Use CallStyle on API 31+ for native call treatment
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setStyle(
                NotificationCompat.CallStyle.forIncomingCall(person, declinePI, answerPI)
            )
        } else {
            builder.addAction(
                android.R.drawable.ic_menu_call, textAccept, answerPI
            )
            builder.addAction(
                android.R.drawable.ic_menu_close_clear_cancel, textDecline, declinePI
            )
            builder.addPerson(person)
        }

        return builder.build()
    }

    fun buildOngoingCallNotification(
        context: Context,
        callId: String,
        callerName: String,
        connected: Boolean
    ): Notification {
        createOngoingCallChannel(context)

        val requestBase = (callId.hashCode() and 0x7FFFFFFF) % 10000

        // End call action
        val endIntent = Intent(context, IncomingCallService::class.java).apply {
            action = Constants.ACTION_END_CALL
            putExtra(Constants.EXTRA_CALL_ID, callId)
        }
        val endPI = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(
                context, requestBase + 4000, endIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getService(
                context, requestBase + 4000, endIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val person = Person.Builder()
            .setName(callerName)
            .setImportant(true)
            .build()

        val contentText = if (connected) "Connected" else "Connecting..."

        val builder = NotificationCompat.Builder(context, Constants.ONGOING_CALL_CHANNEL_ID)
            .setContentTitle(callerName)
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (connected) {
            builder.setUsesChronometer(true)
            builder.setWhen(System.currentTimeMillis())
        }

        // Use CallStyle on API 31+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setStyle(
                NotificationCompat.CallStyle.forOngoingCall(person, endPI)
            )
        } else {
            builder.addAction(
                android.R.drawable.ic_menu_close_clear_cancel, "End Call", endPI
            )
            builder.addPerson(person)
        }

        return builder.build()
    }

    fun buildMissedCallNotification(
        context: Context,
        callId: String,
        callerName: String,
        missedConfig: Map<String, Any?>?,
        androidConfig: Map<String, Any?>?
    ): Notification {
        createMissedCallChannel(context)

        val subtitle = missedConfig?.get("subtitle") as? String ?: "Missed Call"
        val showCallback = missedConfig?.get("showCallback") as? Boolean ?: true
        val callbackText = missedConfig?.get("callbackText") as? String ?: "Call Back"

        val requestBase = (callId.hashCode() and 0x7FFFFFFF) % 10000

        val builder = NotificationCompat.Builder(context, Constants.MISSED_CALL_CHANNEL_ID)
            .setContentTitle(callerName)
            .setContentText(subtitle)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setColor(0xFFFF9800.toInt())

        if (showCallback) {
            val callbackIntent = Intent(context, IncomingCallService::class.java).apply {
                action = Constants.ACTION_CALLBACK
                putExtra(Constants.EXTRA_CALL_ID, callId)
            }
            val callbackPI = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PendingIntent.getForegroundService(
                    context, requestBase + 5000, callbackIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                PendingIntent.getService(
                    context, requestBase + 5000, callbackIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
            builder.addAction(android.R.drawable.ic_menu_call, callbackText, callbackPI)
        }

        return builder.build()
    }

    fun buildMinimalForegroundNotification(context: Context): Notification {
        createIncomingCallChannel(context, null)
        return NotificationCompat.Builder(context, Constants.INCOMING_CALL_CHANNEL_ID)
            .setContentTitle("Call Service")
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSilent(true)
            .build()
    }

    private fun createIncomingCallChannel(context: Context, config: Map<String, Any?>?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = config?.get("channelName") as? String ?: "Incoming Calls"
            val channel = NotificationChannel(
                Constants.INCOMING_CALL_CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(null, null)
                enableVibration(false)
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun createMissedCallChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.MISSED_CALL_CHANNEL_ID,
                "Missed Calls",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun createOngoingCallChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.ONGOING_CALL_CHANNEL_ID,
                "Ongoing Calls",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setSound(null, null)
                enableVibration(false)
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }
}
