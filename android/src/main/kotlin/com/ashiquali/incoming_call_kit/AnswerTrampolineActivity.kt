package com.ashiquali.incoming_call_kit

import android.app.KeyguardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class AnswerTrampolineActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AnswerTrampoline"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callId = intent.getStringExtra(Constants.EXTRA_CALL_ID) ?: run { finish(); return }

        // Send ACTION_ACCEPT to IncomingCallService
        Intent(this, IncomingCallService::class.java).apply {
            action = Constants.ACTION_ACCEPT
            putExtra(Constants.EXTRA_CALL_ID, callId)
        }.also {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(it)
                } else {
                    startService(it)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send ACTION_ACCEPT: ${e.message}")
            }
        }

        // Check lock state before launching host app
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as? KeyguardManager
        val isLocked = keyguardManager?.isKeyguardLocked == true

        if (!isLocked) {
            // Launch host app only when unlocked
            packageManager.getLaunchIntentForPackage(packageName)?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                putExtra("incoming_call_kit_answered", true)
                putExtra("incoming_call_kit_call_id", callId)
            }?.also { startActivity(it) }
        }
        // When locked: user unlocks → onResume in host app detects the call via active calls

        finish()
    }
}
