package com.ashiquali.incoming_call_kit

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AnswerTrampolineActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callId = intent.getStringExtra(Constants.EXTRA_CALL_ID) ?: run { finish(); return }

        // Send ACTION_ACCEPT to IncomingCallService
        Intent(this, IncomingCallService::class.java).apply {
            action = Constants.ACTION_ACCEPT
            putExtra(Constants.EXTRA_CALL_ID, callId)
        }.also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(it)
            } else {
                startService(it)
            }
        }

        // Launch host app
        packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            putExtra("incoming_call_kit_answered", true)
            putExtra("incoming_call_kit_call_id", callId)
        }?.also { startActivity(it) }

        finish()
    }
}
