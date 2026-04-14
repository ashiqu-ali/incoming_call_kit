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
    private val pendingEventQueue = mutableListOf<Map<String, Any?>>()
    private var isEngineReady = false

    fun setCallbackHandle(context: Context, handle: Long) {
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(Constants.PREFS_BACKGROUND_CALLBACK_HANDLE, handle)
            .commit()
    }

    fun getCallbackHandle(context: Context): Long {
        return context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(Constants.PREFS_BACKGROUND_CALLBACK_HANDLE, 0)
    }

    fun hasHandler(context: Context): Boolean {
        return getCallbackHandle(context) != 0L
    }

    @Synchronized
    fun dispatchEvent(context: Context, event: Map<String, Any?>) {
        val handle = getCallbackHandle(context)
        if (handle == 0L) {
            Log.w(TAG, "No background handler registered, persisting event")
            CallKitConfigStore.storePendingEvent(context, event)
            return
        }

        pendingEventQueue.add(event)

        if (flutterEngine != null && isEngineReady) {
            flushEvents()
            return
        }

        if (flutterEngine != null) {
            // Engine starting but not ready yet — event is queued, will flush on ready
            return
        }

        try {
            val callbackInfo = FlutterCallbackInformation.lookupCallbackInformation(handle)
            if (callbackInfo == null) {
                Log.e(TAG, "Failed to lookup callback information for handle: $handle")
                persistQueuedEvents(context)
                return
            }

            flutterEngine = FlutterEngine(context, null, false)
            val engine = flutterEngine!!
            isEngineReady = false

            val backgroundChannel = MethodChannel(
                engine.dartExecutor.binaryMessenger,
                BACKGROUND_CHANNEL
            )

            backgroundChannel.setMethodCallHandler { call: MethodCall, result: MethodChannel.Result ->
                if (call.method == "backgroundHandlerInitialized") {
                    isEngineReady = true
                    flushEvents()
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
            persistQueuedEvents(context)
            destroyEngine()
        }
    }

    private fun flushEvents() {
        val engine = flutterEngine ?: return
        val backgroundChannel = MethodChannel(
            engine.dartExecutor.binaryMessenger,
            BACKGROUND_CHANNEL
        )
        val toFlush = pendingEventQueue.toList()
        pendingEventQueue.clear()
        for (event in toFlush) {
            backgroundChannel.invokeMethod("onBackgroundEvent", event)
        }
        // Destroy engine after flushing to prevent memory leak
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            destroyEngine()
        }, 2000)
    }

    private fun persistQueuedEvents(context: Context) {
        for (event in pendingEventQueue) {
            CallKitConfigStore.storePendingEvent(context, event)
        }
        pendingEventQueue.clear()
    }

    @Synchronized
    fun destroyEngine() {
        isEngineReady = false
        flutterEngine?.destroy()
        flutterEngine = null
    }
}
