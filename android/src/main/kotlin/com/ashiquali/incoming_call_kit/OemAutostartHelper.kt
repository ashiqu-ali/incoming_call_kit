package com.ashiquali.incoming_call_kit

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build

object OemAutostartHelper {

    private val autostartIntents: Map<String, List<ComponentName>> = mapOf(
        "xiaomi" to listOf(
            ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity"
            ),
        ),
        "oppo" to listOf(
            ComponentName(
                "com.coloros.safecenter",
                "com.coloros.safecenter.permission.startup.StartupAppListActivity"
            ),
            ComponentName(
                "com.oppo.safe",
                "com.oppo.safe.permission.startup.StartupAppListActivity"
            ),
        ),
        "vivo" to listOf(
            ComponentName(
                "com.vivo.permissionmanager",
                "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
            ),
            ComponentName(
                "com.iqoo.secure",
                "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"
            ),
        ),
        "huawei" to listOf(
            ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
            ),
            ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.optimize.process.ProtectActivity"
            ),
        ),
        "samsung" to listOf(
            ComponentName(
                "com.samsung.android.lool",
                "com.samsung.android.sm.battery.ui.BatteryActivity"
            ),
        ),
        "oneplus" to listOf(
            ComponentName(
                "com.oneplus.security",
                "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
            ),
        ),
        "realme" to listOf(
            ComponentName(
                "com.coloros.safecenter",
                "com.coloros.safecenter.permission.startup.StartupAppListActivity"
            ),
        ),
    )

    fun isAutoStartAvailable(context: Context): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val intents = autostartIntents[manufacturer] ?: return false
        return intents.any { component ->
            Intent().apply { this.component = component }
                .resolveActivity(context.packageManager) != null
        }
    }

    fun openAutoStartSettings(context: Context): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val intents = autostartIntents[manufacturer] ?: return false
        for (component in intents) {
            try {
                context.startActivity(
                    Intent().apply {
                        this.component = component
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
                return true
            } catch (_: Exception) {
                continue
            }
        }
        return false
    }
}
