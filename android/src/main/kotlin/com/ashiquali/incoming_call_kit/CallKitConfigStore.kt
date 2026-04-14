package com.ashiquali.incoming_call_kit

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object CallKitConfigStore {

    fun store(context: Context, callId: String, params: Map<String, Any?>) {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val json = JSONObject(params).toString()
        prefs.edit()
            .putString("call_$callId", json)
            .putStringSet(
                Constants.PREFS_ACTIVE_CALL_IDS,
                getActiveCallIds(context).toMutableSet().apply { add(callId) }
            )
            .apply()
    }

    fun load(context: Context, callId: String): Map<String, Any?>? {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString("call_$callId", null) ?: return null
        return jsonToMap(JSONObject(json))
    }

    fun remove(context: Context, callId: String) {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove("call_$callId")
            .putStringSet(
                Constants.PREFS_ACTIVE_CALL_IDS,
                getActiveCallIds(context).toMutableSet().apply { remove(callId) }
            )
            .apply()
    }

    fun getActiveCallIds(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(Constants.PREFS_ACTIVE_CALL_IDS, emptySet()) ?: emptySet()
    }

    // === Pending event persistence (for killed state) ===

    fun storePendingEvent(context: Context, event: Map<String, Any?>) {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getString(Constants.PREFS_PENDING_EVENTS, "[]")
        val arr = JSONArray(existing)
        arr.put(JSONObject(event))
        prefs.edit().putString(Constants.PREFS_PENDING_EVENTS, arr.toString()).apply()
    }

    fun getPendingEvents(context: Context): List<Map<String, Any?>> {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(Constants.PREFS_PENDING_EVENTS, "[]")
        val arr = JSONArray(json)
        val events = mutableListOf<Map<String, Any?>>()
        for (i in 0 until arr.length()) {
            events.add(jsonToMap(arr.getJSONObject(i)))
        }
        return events
    }

    fun clearPendingEvents(context: Context) {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(Constants.PREFS_PENDING_EVENTS, "[]").apply()
    }

    // === JSON helpers ===

    private fun jsonToMap(json: JSONObject): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = json.get(key)
            map[key] = when (value) {
                is JSONObject -> jsonToMap(value)
                is JSONArray -> jsonToList(value)
                JSONObject.NULL -> null
                else -> value
            }
        }
        return map
    }

    private fun jsonToList(arr: JSONArray): List<Any?> {
        val list = mutableListOf<Any?>()
        for (i in 0 until arr.length()) {
            val value = arr.get(i)
            list.add(
                when (value) {
                    is JSONObject -> jsonToMap(value)
                    is JSONArray -> jsonToList(value)
                    JSONObject.NULL -> null
                    else -> value
                }
            )
        }
        return list
    }
}
