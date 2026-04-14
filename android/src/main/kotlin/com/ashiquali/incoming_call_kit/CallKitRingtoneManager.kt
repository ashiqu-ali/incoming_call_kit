package com.ashiquali.incoming_call_kit

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class CallKitRingtoneManager(private val context: Context) {
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private var isRinging = false

    fun startRinging(config: Map<String, Any?>) {
        if (isRinging) return
        isRinging = true

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val ringerMode = audioManager.ringerMode

        // Ringtone: skip if silent or vibrate
        if (ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            startRingtone(config)
        }

        // Vibration: play if normal or vibrate mode (not silent)
        val enableVibration = config["enableVibration"] as? Boolean ?: true
        if (enableVibration && ringerMode != AudioManager.RINGER_MODE_SILENT) {
            startVibration(config)
        }
    }

    fun stopRinging() {
        if (!isRinging) return
        isRinging = false

        try {
            ringtone?.stop()
        } catch (_: Exception) {}
        ringtone = null

        try {
            vibrator?.cancel()
        } catch (_: Exception) {}
        vibrator = null
    }

    private fun startRingtone(config: Map<String, Any?>) {
        val ringtonePath = config["ringtonePath"] as? String

        val ringtoneUri: Uri = if (ringtonePath != null) {
            val resId = context.resources.getIdentifier(
                ringtonePath, "raw", context.packageName
            )
            if (resId != 0) {
                Uri.parse("android.resource://${context.packageName}/$resId")
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        }

        ringtone = RingtoneManager.getRingtone(context, ringtoneUri)?.apply {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            audioAttributes = attrs
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                isLooping = true
            }
            play()
        }
    }

    @Suppress("DEPRECATION")
    private fun startVibration(config: Map<String, Any?>) {
        val patternRaw = config["vibrationPattern"] as? List<*>
        val pattern = patternRaw?.map { (it as Number).toLong() }?.toLongArray()
            ?: longArrayOf(0L, 1000L, 1000L)

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }
}
