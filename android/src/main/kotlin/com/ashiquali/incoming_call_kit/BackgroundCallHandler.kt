package com.ashiquali.incoming_call_kit

import android.content.Context
import android.util.Log
import io.flutter.FlutterInjector
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.FlutterCallbackInformation

object BackgroundCallHandler {
    private const val TAG = "BackgroundCallHandler"
    private const val BACKGROUND_CHANNEL = "com.ashiquali.incoming_call_kit/background"

    private var flutterEngine: FlutterEngine? = null

    fun setCallbackHandle(context: Context, handle: Long) {
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(Constants.PREFS_BACKGROUND_CALLBACK_HANDLE, handle)
            .apply()
    }

    fun getCallbackHandle(context: Context): Long {
        return context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(Constants.PREFS_BACKGROUND_CALLBACK_HANDLE, 0)
    }

    fun hasHandler(context: Context): Boolean {
        return getCallbackHandle(context) != 0L
    }

    fun dispatchEvent(context: Context, event: Map<String, Any?>) {
        val handle = getCallbackHandle(context)
        if (handle == 0L) {
            Log.w(TAG, "No background handler registered, persisting event")
            CallKitConfigStore.storePendingEvent(context, event)
            return
        }

        try {
            val callbackInfo = FlutterCallbackInformation.lookupCallbackInformation(handle)
            if (callbackInfo == null) {
                Log.e(TAG, "Failed to lookup callback information for handle: $handle")
                CallKitConfigStore.storePendingEvent(context, event)
                return
            }

            if (flutterEngine == null) {
                flutterEngine = FlutterEngine(context, null, false)
            }

            val engine = flutterEngine!!
            val backgroundChannel = MethodChannel(
                engine.dartExecutor.binaryMessenger,
                BACKGROUND_CHANNEL
            )

            backgroundChannel.setMethodCallHandler { call: MethodCall, result: MethodChannel.Result ->
                if (call.method == "backgroundHandlerInitialized") {
                    backgroundChannel.invokeMethod("onBackgroundEvent", event)
                    result.success(null)
                } else {
                    result.notImplemented()
                }
            }

            val appBundlePath = FlutterInjector.instance().flutterLoader().findAppBundlePath()
            engine.dartExecutor.executeDartCallback(
                DartExecutor.DartCallback(
                    context.assets,
                    appBundlePath,
                    callbackInfo
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to dispatch background event", e)
            CallKitConfigStore.storePendingEvent(context, event)
        }
    }

    fun destroyEngine() {
        flutterEngine?.destroy()
        flutterEngine = null
    }
}
