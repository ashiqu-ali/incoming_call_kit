# PLAN.MD — incoming_call_kit Implementation Specification
# This is the ONLY file needed. An AI should create the full package from this spec alone.
# No external Flutter packages referenced. No prior codebase knowledge needed.
# Target: create as a NEW Flutter plugin project.

---

## WHAT IS THIS

A standalone Flutter plugin called `incoming_call_kit` that displays a customizable incoming call UI.

- **Android**: Native full-screen Activity (background / lock screen) + Flutter widget (foreground)
- **iOS**: Apple CallKit wrapper (no custom UI — Apple enforced)
- **Zero VoIP dependency** — works with any VoIP SDK or none at all
- **Zero external image libraries** — avatar loaded via HttpURLConnection
- **Only dependency**: `plugin_platform_interface`

---

## DEFAULT COLOR PALETTE

All colors used throughout the package. Users can override every one of these.

| Token                      | Hex         | Usage                                        |
|----------------------------|-------------|----------------------------------------------|
| `background`               | `#1B1B2F`   | Screen background (solid fallback)           |
| `defaultGradientStart`     | `#1A1A2E`   | Default gradient top color                   |
| `defaultGradientMiddle`    | `#16213E`   | Default gradient middle color                |
| `defaultGradientEnd`       | `#0F3460`   | Default gradient bottom color                |
| `callerNameText`           | `#FFFFFF`   | Caller name text                             |
| `callerNumberText`         | `#B3FFFFFF` | Caller number text (70% white)               |
| `statusText`               | `#80FFFFFF` | "Incoming Call" label (50% white)            |
| `acceptButton`             | `#4CAF50`   | Accept button background (Material Green)    |
| `declineButton`            | `#F44336`   | Decline button background (Material Red)     |
| `buttonLabelText`          | `#FFFFFF`   | Button label text ("Accept" / "Decline")     |
| `buttonIconTint`           | `#FFFFFF`   | Phone / close icon tint                      |
| `initialsBackground`       | `#3A3A5C`   | Circle behind initials when no avatar        |
| `initialsText`             | `#FFFFFF`   | Initials letter color                        |
| `avatarBorder`             | `#FFFFFF`   | Avatar border ring (when enabled)            |
| `pulseRing`                | `#33FFFFFF` | Pulse animation ring (20% white)             |
| `notificationSmallIcon`    | —           | Uses `android.R.drawable.ic_menu_call`       |
| `missedCallAccent`         | `#FF9800`   | Missed call notification accent (orange)     |

---

## PACKAGE STRUCTURE

```
incoming_call_kit/
├── lib/
│   ├── incoming_call_kit.dart                          # barrel export
│   └── src/
│       ├── incoming_call_kit_controller.dart            # public singleton API
│       ├── models/
│       │   ├── call_kit_params.dart                     # main config object
│       │   ├── android_call_kit_params.dart             # Android-only UI config
│       │   ├── ios_call_kit_params.dart                 # iOS-only CallKit config
│       │   ├── call_kit_event.dart                      # event enum + model
│       │   ├── gradient_config.dart                     # gradient serialization
│       │   └── notification_params.dart                 # missed call notification config
│       ├── platform/
│       │   ├── incoming_call_kit_platform_interface.dart # abstract platform
│       │   └── incoming_call_kit_method_channel.dart     # method + event channel impl
│       └── widgets/
│           └── incoming_call_screen.dart                 # Flutter foreground widget
├── android/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── kotlin/com/iocod/incoming_call_kit/
│       │   ├── IncomingCallKitPlugin.kt                 # Flutter ↔ native bridge
│       │   ├── CallKitConfigStore.kt                    # SharedPreferences config
│       │   ├── IncomingCallService.kt                   # foreground service
│       │   ├── IncomingCallActivity.kt                  # native lock screen UI
│       │   ├── AnswerTrampolineActivity.kt              # invisible answer bridge
│       │   ├── NotificationBuilder.kt                   # notification factory
│       │   ├── CallKitRingtoneManager.kt                # ringtone + vibration
│       │   └── Constants.kt                             # all string constants
│       └── res/
│           ├── layout/activity_incoming_call.xml
│           ├── drawable/bg_accept_button.xml
│           ├── drawable/bg_decline_button.xml
│           └── values/styles.xml
├── ios/
│   ├── incoming_call_kit.podspec
│   └── Classes/
│       └── IncomingCallKitPlugin.swift
├── example/
│   └── lib/main.dart
├── pubspec.yaml
├── README.md
├── CHANGELOG.md
└── LICENSE
```

---

## DART MODELS — COMPLETE FIELD SPECS

### CallKitParams (`lib/src/models/call_kit_params.dart`)

| Field                    | Type                      | Required | Default                        |
|--------------------------|---------------------------|----------|--------------------------------|
| `id`                     | `String`                  | YES      | —                              |
| `callerName`             | `String`                  | YES      | —                              |
| `callerNumber`           | `String?`                 | no       | `null`                         |
| `avatar`                 | `String?`                 | no       | `null` (shows initials)        |
| `type`                   | `int`                     | no       | `0` (0=audio, 1=video)         |
| `textAccept`             | `String`                  | no       | `'Accept'`                     |
| `textDecline`            | `String`                  | no       | `'Decline'`                    |
| `duration`               | `Duration`                | no       | `Duration(seconds: 30)`        |
| `extra`                  | `Map<String, dynamic>?`   | no       | `null`                         |
| `missedCallNotification` | `NotificationParams?`     | no       | `null`                         |
| `android`                | `AndroidCallKitParams?`   | no       | `null` (uses all defaults)     |
| `ios`                    | `IOSCallKitParams?`       | no       | `null` (uses all defaults)     |

Must have: `const` constructor, `toMap()`, `fromMap()`, `copyWith()`

### AndroidCallKitParams (`lib/src/models/android_call_kit_params.dart`)

| Field                        | Type            | Default          | Notes                                  |
|------------------------------|-----------------|------------------|----------------------------------------|
| `backgroundColor`            | `String?`       | `'#1B1B2F'`      | Hex. Ignored if gradient set           |
| `backgroundGradient`         | `GradientConfig?`| `null`           | Overrides backgroundColor              |
| `backgroundImageUrl`         | `String?`       | `null`           | URL or asset. Behind gradient overlay  |
| `avatarSize`                 | `double`        | `96`             | dp                                     |
| `avatarBorderColor`          | `String?`       | `null`           | No border when null                    |
| `avatarBorderWidth`          | `double`        | `0`              | dp                                     |
| `avatarPulseAnimation`       | `bool`          | `true`           | Pulsing ring around avatar             |
| `initialsBackgroundColor`    | `String?`       | `'#3A3A5C'`      | Circle behind initials                 |
| `initialsTextColor`          | `String?`       | `'#FFFFFF'`       | Initials letter color                  |
| `callerNameColor`            | `String?`       | `'#FFFFFF'`       |                                        |
| `callerNameFontSize`         | `double`        | `28`             | sp                                     |
| `callerNumberColor`          | `String?`       | `'#B3FFFFFF'`     | 70% white                              |
| `callerNumberFontSize`       | `double`        | `16`             | sp                                     |
| `statusText`                 | `String?`       | `'Incoming Call'` |                                        |
| `statusTextColor`            | `String?`       | `'#80FFFFFF'`     | 50% white                              |
| `acceptButtonColor`          | `String?`       | `'#4CAF50'`       | Material Green                         |
| `declineButtonColor`         | `String?`       | `'#F44336'`       | Material Red                           |
| `buttonSize`                 | `double`        | `64`             | dp                                     |
| `enableSwipeGesture`         | `bool`          | `true`           | Swipe-to-answer/decline                |
| `swipeThreshold`             | `double`        | `120`            | dp drag distance to trigger            |
| `ringtonePath`               | `String?`       | `null`           | res/raw filename (no ext). null=system |
| `enableVibration`            | `bool`          | `true`           |                                        |
| `vibrationPattern`           | `List<int>?`    | `[0, 1000, 1000]`| ms [pause, vib, pause, ...]            |
| `showOnLockScreen`           | `bool`          | `true`           | Show native Activity on lock screen    |
| `channelName`                | `String?`       | `'Incoming Calls'`| Notification channel display name      |
| `logoUrl`                    | `String?`       | `null`           | Notification large icon                |
| `showCallerIdInNotification` | `bool`          | `true`           |                                        |

Must have: `const` constructor, `toMap()`

### IOSCallKitParams (`lib/src/models/ios_call_kit_params.dart`)

| Field                       | Type      | Default           |
|-----------------------------|-----------|-------------------|
| `iconName`                  | `String?` | `'CallKitLogo'`   |
| `handleType`                | `String`  | `'generic'`       |
| `supportsVideo`             | `bool`    | `false`           |
| `maximumCallGroups`         | `int`     | `2`               |
| `maximumCallsPerCallGroup`  | `int`     | `1`               |
| `ringtonePath`              | `String?` | `null` (system)   |
| `supportsDTMF`              | `bool`    | `true`            |
| `supportsHolding`           | `bool`    | `false`           |

Must have: `const` constructor, `toMap()`

### GradientConfig (`lib/src/models/gradient_config.dart`)

| Field      | Type                   | Default                     | Notes                           |
|------------|------------------------|-----------------------------|---------------------------------|
| `type`     | `String`               | `'linear'`                  | `'linear'` or `'radial'`       |
| `colors`   | `List<String>`         | REQUIRED                    | Hex strings, min 2             |
| `stops`    | `List<double>?`        | `null` (evenly distributed) | 0.0–1.0, must match colors len |
| `begin`    | `Map<String, double>?` | `{'x': 0.5, 'y': 0.0}`     | Top center                     |
| `end`      | `Map<String, double>?` | `{'x': 0.5, 'y': 1.0}`     | Bottom center                  |
| `center`   | `Map<String, double>?` | `{'x': 0.5, 'y': 0.3}`     | Radial only                    |
| `radius`   | `double?`              | `0.8`                       | Radial only, 0.0–1.0           |

Must have: `const` constructor, `toMap()`

When neither `backgroundGradient` nor `backgroundColor` is set by the user, the default behavior should apply the default gradient: `['#1A1A2E', '#16213E', '#0F3460']` top-to-bottom.

### CallKitEvent + CallKitAction (`lib/src/models/call_kit_event.dart`)

```dart
enum CallKitAction { accept, decline, timeout, dismissed, callback }

class CallKitEvent {
  final CallKitAction action;
  final String callId;
  final Map<String, dynamic>? extra;

  factory CallKitEvent.fromMap(Map<String, dynamic> map);
}
```

### NotificationParams (`lib/src/models/notification_params.dart`)

| Field            | Type     | Default        |
|------------------|----------|----------------|
| `showNotification`| `bool`  | `true`         |
| `subtitle`       | `String` | `'Missed Call'`|
| `showCallback`   | `bool`  | `true`         |
| `callbackText`   | `String` | `'Call Back'`  |

Must have: `const` constructor, `toMap()`

---

## DART CONTROLLER — `IncomingCallKit`

File: `lib/src/incoming_call_kit_controller.dart`

Singleton accessed via `IncomingCallKit.instance`.

```dart
class IncomingCallKit {
  static final IncomingCallKit instance = IncomingCallKit._();
  IncomingCallKit._();

  /// Show incoming call UI.
  /// Android background/locked: starts foreground service → notification → native Activity.
  /// iOS: reports incoming call to CallKit.
  Future<void> show(CallKitParams params);

  /// Dismiss a specific incoming call by ID. Use when remote party cancels.
  Future<void> dismiss(String callId);

  /// Dismiss all active incoming calls.
  Future<void> dismissAll();

  /// Stream of user actions. Emits accept, decline, timeout, dismissed, callback.
  Stream<CallKitEvent> get onEvent;

  /// [Android 14+] Check if USE_FULL_SCREEN_INTENT permission is granted.
  Future<bool> canUseFullScreenIntent();

  /// [Android 14+] Open system settings to grant full-screen intent permission.
  Future<void> requestFullIntentPermission();

  /// [Android 13+] Check if POST_NOTIFICATIONS permission is granted.
  Future<bool> hasNotificationPermission();

  /// [Android 13+] Request POST_NOTIFICATIONS permission.
  Future<bool> requestNotificationPermission();

  /// Get list of currently ringing call IDs.
  Future<List<String>> getActiveCalls();
}
```

---

## PLATFORM LAYER

### Platform Interface (`lib/src/platform/incoming_call_kit_platform_interface.dart`)

Abstract class `IncomingCallKitPlatform extends PlatformInterface`.
Default instance: `IncomingCallKitMethodChannel`.
Methods mirror the controller but take/return `Map` and primitive types only.

### Method Channel (`lib/src/platform/incoming_call_kit_method_channel.dart`)

- **MethodChannel name**: `'com.iocod.incoming_call_kit/methods'`
- **EventChannel name**: `'com.iocod.incoming_call_kit/events'`

Method calls to native:
- `show` → passes `CallKitParams.toMap()`
- `dismiss` → passes `{'id': callId}`
- `dismissAll` → no args
- `canUseFullScreenIntent` → returns `bool`
- `requestFullIntentPermission` → void
- `hasNotificationPermission` → returns `bool`
- `requestNotificationPermission` → returns `bool`
- `getActiveCalls` → returns `List<String>`

EventChannel emits maps from native:
```json
{"action": "accept", "callId": "uuid-here", "extra": {"key": "value"}}
```

---

## FLUTTER WIDGET — `IncomingCallScreen`

File: `lib/src/widgets/incoming_call_screen.dart`

A StatefulWidget for use when the app is in the **foreground**. This is NOT the native Android Activity — this is a pure Dart widget the host app can push onto its Navigator.

### Constructor

```dart
const IncomingCallScreen({
  required this.params,
  required this.onAccept,
  required this.onDecline,
  this.onTimeout,
});
```

### Visual Layout (top to bottom)

1. **Full screen** — no AppBar, no system UI overlays
2. **Background** — `BoxDecoration`:
   - If `androidParams.backgroundGradient` is set → `LinearGradient` or `RadialGradient`
   - Else if `androidParams.backgroundColor` is set → solid color
   - Else → default gradient `['#1A1A2E', '#16213E', '#0F3460']` top-to-bottom
3. **Top ~30% spacer**
4. **Avatar section** (centered):
   - If `params.avatar` is a URL → `CircleAvatar` with `NetworkImage`
   - Else → `CircleAvatar` with initials `Text`, background `#3A3A5C`, text `#FFFFFF`
   - Size: `avatarSize` dp (default 96)
   - Optional border: `Container` with `BoxDecoration` border
   - Optional pulse animation: `ScaleTransition` with `AnimationController` (1.5s, repeat, scale 1.0→1.15)
     - Outer ring: `Container` with `BoxDecoration` circle, color `#33FFFFFF`
5. **Caller name** — `Text`, color `#FFFFFF`, fontSize 28, bold, maxLines 2, ellipsize
6. **8dp gap**
7. **Caller number** — `Text`, color `#B3FFFFFF`, fontSize 16
8. **8dp gap**
9. **Status text** — `Text`, `'Incoming Call'`, color `#80FFFFFF`, fontSize 14
10. **Flexible spacer** (fills remaining)
11. **Bottom buttons** (horizontal, 64dp bottom margin):
    - **Decline** (left): red circle `#F44336`, 64dp, close icon `#FFFFFF`, label "Decline" below
    - **Accept** (right): green circle `#4CAF50`, 64dp, phone icon `#FFFFFF`, label "Accept" below
    - Optional swipe gesture: `GestureDetector` with `onPanUpdate`/`onPanEnd`
12. **Auto-timeout**: `Timer` fires `onTimeout` after `params.duration`

### Behavior
- Does NOT handle any call media — only fires callbacks
- Disposes timer and animation controller in `dispose()`
- Calls `HapticFeedback.mediumImpact()` on accept/decline

---

## ANDROID NATIVE — COMPLETE SPECS

### Constants.kt

```kotlin
object Constants {
    const val ACTION_SHOW_INCOMING = "com.iocod.incoming_call_kit.ACTION_SHOW_INCOMING"
    const val ACTION_ACCEPT = "com.iocod.incoming_call_kit.ACTION_ACCEPT"
    const val ACTION_DECLINE = "com.iocod.incoming_call_kit.ACTION_DECLINE"
    const val ACTION_DISMISS = "com.iocod.incoming_call_kit.ACTION_DISMISS"
    const val ACTION_TIMEOUT = "com.iocod.incoming_call_kit.ACTION_TIMEOUT"
    const val ACTION_CALLBACK = "com.iocod.incoming_call_kit.ACTION_CALLBACK"

    const val EXTRA_CALL_ID = "extra_call_id"
    const val EXTRA_CALLER_NAME = "extra_caller_name"
    const val EXTRA_CALLER_NUMBER = "extra_caller_number"

    const val NOTIFICATION_ID = 9001
    const val MISSED_NOTIFICATION_ID = 9002
    const val INCOMING_CALL_CHANNEL_ID = "incoming_call_kit_channel"
    const val MISSED_CALL_CHANNEL_ID = "incoming_call_kit_missed_channel"

    const val BROADCAST_ACCEPTED = "incoming_call_kit.ACCEPTED"
    const val BROADCAST_DECLINED = "incoming_call_kit.DECLINED"
    const val BROADCAST_TIMEOUT = "incoming_call_kit.TIMEOUT"
    const val BROADCAST_DISMISSED = "incoming_call_kit.DISMISSED"
    const val BROADCAST_CALLBACK = "incoming_call_kit.CALLBACK"

    const val PREFS_NAME = "incoming_call_kit_config"
}
```

### CallKitConfigStore.kt

Persists Dart config to `SharedPreferences` as JSON so the native `IncomingCallActivity` can read it even when Flutter engine is dead (process killed, app not started).

```
object CallKitConfigStore {
    fun store(context, callId, params: Map<String, Any?>) →
        serialize params to JSONObject, store under key "call_$callId",
        add callId to "active_call_ids" StringSet

    fun load(context, callId): Map<String, Any?>? →
        read JSONObject, deserialize to Map

    fun remove(context, callId) →
        remove "call_$callId" entry, remove from "active_call_ids" set

    fun getActiveCallIds(context): Set<String>
}
```

Uses `org.json.JSONObject` (built into Android, no dependency needed).

### IncomingCallKitPlugin.kt

Implements: `FlutterPlugin`, `MethodCallHandler`, `EventChannel.StreamHandler`

- `onAttachedToEngine`: register MethodChannel + EventChannel, register `LocalBroadcastReceiver` for all `BROADCAST_*` actions
- `onMethodCall`:
  - `"show"` → `CallKitConfigStore.store(params)` then start `IncomingCallService` with `ACTION_SHOW_INCOMING`
  - `"dismiss"` → start `IncomingCallService` with `ACTION_DISMISS` + callId
  - `"dismissAll"` → dismiss all active call IDs
  - `"canUseFullScreenIntent"` → check `NotificationManager.canUseFullScreenIntent()` (API 34+)
  - `"requestFullIntentPermission"` → open `ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT` settings
  - `"hasNotificationPermission"` → check `POST_NOTIFICATIONS` (API 33+)
  - `"requestNotificationPermission"` → request via `ActivityCompat`
  - `"getActiveCalls"` → `CallKitConfigStore.getActiveCallIds()`
- `LocalBroadcastReceiver` callback → `eventSink?.success(mapOf("action" to action, "callId" to callId, "extra" to extraMap))`
- `onDetachedFromEngine`: unregister channels and receiver

### CallKitRingtoneManager.kt

```
class CallKitRingtoneManager(context: Context) {
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private var isRinging = false

    fun startRinging(config: Map<String, Any?>) {
        if (isRinging) return
        isRinging = true

        // Ringtone: check config["ringtonePath"]
        // If non-null → look up res/raw/<name> via resources.getIdentifier()
        // If null or not found → RingtoneManager.getDefaultUri(TYPE_RINGTONE)
        // AudioAttributes: USAGE_NOTIFICATION_RINGTONE, CONTENT_TYPE_SONIFICATION
        // API 28+: isLooping = true

        // Vibration: check config["enableVibration"] (default true)
        // Pattern from config["vibrationPattern"] or [0L, 1000L, 1000L]
        // API 31+: use VibratorManager.defaultVibrator
        // Below: context.getSystemService(VIBRATOR_SERVICE)
        // vibrate(VibrationEffect.createWaveform(pattern, 0)) for API 26+
    }

    fun stopRinging() {
        isRinging = false
        ringtone?.stop(); ringtone = null
        vibrator?.cancel(); vibrator = null
    }
}
```

### NotificationBuilder.kt

```
object NotificationBuilder {

    fun buildIncomingCallNotification(
        context, callId, callerName, callerNumber, config
    ): Notification {
        createIncomingCallChannel(context, config)

        // fullScreenIntent → IncomingCallActivity.createIntent(context, callId)
        //   PendingIntent.getActivity, FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE
        //   requestCode: (callId.hashCode() and 0x7FFFFFFF) % 10000 + 1000

        // Answer action → AnswerTrampolineActivity
        //   PendingIntent.getActivity, requestCode: ...+ 2000

        // Decline action → IncomingCallService ACTION_DECLINE
        //   PendingIntent.getForegroundService, requestCode: ...+ 3000

        // Build:
        //   NotificationCompat.Builder(context, INCOMING_CALL_CHANNEL_ID)
        //     .setContentTitle(callerName)
        //     .setContentText(callerNumber ?: "Incoming Call")
        //     .setSmallIcon(android.R.drawable.ic_menu_call)
        //     .setFullScreenIntent(fullScreenPI, true)
        //     .setOngoing(true)
        //     .setPriority(PRIORITY_MAX)
        //     .setCategory(CATEGORY_CALL)
        //     .setVisibility(VISIBILITY_PUBLIC)
        //     .addPerson(Person.Builder().setName(callerName).setImportant(true).build())
        //     .addAction(ic_menu_call, textAccept, answerPI)
        //     .addAction(ic_delete, textDecline, declinePI)
        //     .build()
    }

    fun buildMissedCallNotification(context, callId, callerName, config): Notification {
        createMissedCallChannel(context)
        // IMPORTANCE_DEFAULT, color #FF9800 (missedCallAccent)
        // Optional "Call Back" action → IncomingCallService ACTION_CALLBACK
    }

    fun createIncomingCallChannel(context, config) {
        // IMPORTANCE_HIGH, silent (sound=null, vibration=false)
        // ringtone handled by service, not notification
        // setBypassDnd(true)
        // lockscreenVisibility = VISIBILITY_PUBLIC
        // Name from config["channelName"] or "Incoming Calls"
    }

    fun createMissedCallChannel(context) {
        // IMPORTANCE_DEFAULT, name "Missed Calls"
    }
}
```

### IncomingCallService.kt (extends Service)

Foreground service managing notification, ringtone, wake lock, and timeout.

```
class IncomingCallService : Service() {

    private lateinit var ringtoneManager: CallKitRingtoneManager
    private var wakeLock: PowerManager.WakeLock? = null
    private val timeoutHandler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null
    private val activeCallIds = mutableSetOf<String>()

    override fun onStartCommand(intent, flags, startId): Int {
        // CRITICAL: call ensureForeground() IMMEDIATELY — Android kills after 5s otherwise
        ensureForeground(intent)

        when (intent?.action) {
            ACTION_SHOW_INCOMING → handleShowIncoming(intent)
            ACTION_ACCEPT → handleAccept(intent)
            ACTION_DECLINE → handleDecline(intent)
            ACTION_DISMISS → handleDismiss(intent)
            ACTION_CALLBACK → handleCallback(intent)
        }
        return START_NOT_STICKY
    }

    // ensureForeground:
    //   Build minimal notification, call startForeground(NOTIFICATION_ID, notification,
    //   ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL)

    // handleShowIncoming:
    //   1. Read callId from intent
    //   2. Load config from CallKitConfigStore
    //   3. Build full notification via NotificationBuilder
    //   4. startForeground(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_PHONE_CALL)
    //   5. ALSO NotificationManager.notify(NOTIFICATION_ID, notification) ← OEM dual-post
    //   6. Start ringtone + vibration
    //   7. Acquire wake lock: FULL_WAKE_LOCK | ACQUIRE_CAUSES_WAKEUP, 60s timeout
    //   8. Schedule timeout: Handler.postDelayed(duration millis)
    //   9. Add to activeCallIds

    // handleAccept:
    //   1. Stop ringtone
    //   2. Release wake lock
    //   3. Cancel timeout runnable
    //   4. Cancel notification
    //   5. LocalBroadcast BROADCAST_ACCEPTED with callId + extra
    //   6. CallKitConfigStore.remove(callId)
    //   7. Remove from activeCallIds
    //   8. Finish IncomingCallActivity if alive
    //   9. If activeCallIds empty → stopSelf()

    // handleDecline: same flow as accept, broadcast BROADCAST_DECLINED
    // handleDismiss: same flow, broadcast BROADCAST_DISMISSED (remote cancel)

    // handleTimeout (called by runnable):
    //   1. Stop ringtone
    //   2. Cancel notification
    //   3. If missedCallNotification configured → show missed call notification
    //   4. LocalBroadcast BROADCAST_TIMEOUT
    //   5. Finish IncomingCallActivity
    //   6. Cleanup

    // handleCallback: LocalBroadcast BROADCAST_CALLBACK with callId, then cancel missed notification
}
```

**Service type**: `foregroundServiceType="phoneCall"`

### IncomingCallActivity.kt (extends AppCompatActivity)

Native full-screen Activity shown on lock screen or when app is in background. Reads ALL customization from `CallKitConfigStore`.

```
class IncomingCallActivity : AppCompatActivity() {

    companion object {
        @Volatile var isActivityAlive = false
        var lastAliveTimestamp = 0L

        fun createIntent(context: Context, callId: String): Intent {
            return Intent(context, IncomingCallActivity::class.java).apply {
                flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                putExtra(EXTRA_CALL_ID, callId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(
            FLAG_SHOW_WHEN_LOCKED or FLAG_KEEP_SCREEN_ON or
            FLAG_TURN_SCREEN_ON or FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or FLAG_FULLSCREEN
        )

        // 2. Wake screen
        // PowerManager.FULL_WAKE_LOCK | ACQUIRE_CAUSES_WAKEUP | ON_AFTER_RELEASE, 60s

        // 3. Read callId, guard null → finish()

        // 4. Dedup: if isActivityAlive AND (now - lastAliveTimestamp < 60000) → finish()
        // isActivityAlive = true; lastAliveTimestamp = System.currentTimeMillis()

        // 5. Load config from CallKitConfigStore, guard null → finish()

        // 6. setContentView(R.layout.activity_incoming_call)

        // 7. applyBackground(config):
        //    Check config["backgroundGradient"]:
        //      If present → parse colors list, create GradientDrawable:
        //        "linear" → GradientDrawable(Orientation.TOP_BOTTOM, colors)
        //        "radial" → GradientDrawable().apply { gradientType=RADIAL_GRADIENT, colors=..., gradientRadius=... }
        //    Else → solid color from config["backgroundColor"] or #1B1B2F
        //    If neither set → default gradient [#1A1A2E, #16213E, #0F3460]
        //    Background image: load in separate thread if backgroundImageUrl set

        // 8. applyAvatar(config):
        //    If avatar URL → load via HttpURLConnection on background Thread:
        //      URL(url).openConnection(), connectTimeout=5000, readTimeout=5000
        //      BitmapFactory.decodeStream → getCircularBitmap → runOnUiThread { setImageBitmap }
        //    If no avatar → show initials:
        //      Extract first letter(s) from callerName
        //      Draw circle (#3A3A5C) with centered text (#FFFFFF) on Bitmap via Canvas
        //    Avatar border: if avatarBorderColor set, wrap in circular border GradientDrawable
        //    Pulse animation: if enabled, ObjectAnimator on pulse_ring view (scaleX/scaleY 1.0→1.3, alpha 1→0, 1.5s, repeat)

        // 9. applyCallerInfo(config):
        //    caller_name.text = callerName, textColor=#FFFFFF, textSize=28sp
        //    caller_number.text = callerNumber, textColor=#B3FFFFFF, textSize=16sp
        //    call_status.text = statusText or "Incoming Call", textColor=#80FFFFFF

        // 10. applyButtons(config):
        //     Accept button: tint bg_accept_button drawable with acceptButtonColor (#4CAF50)
        //     Decline button: tint bg_decline_button drawable with declineButtonColor (#F44336)
        //     Button size from config, labels from textAccept/textDecline
        //     Icon tint: #FFFFFF

        // 11. setupSwipeGesture(config): if enableSwipeGesture
        //     setOnTouchListener on each button container
        //     Track ACTION_DOWN position
        //     ACTION_MOVE: calculate dragDistance, scale button (1.0 → max 1.3 proportional to threshold)
        //     If dragDistance > swipeThreshold → trigger action + HapticFeedbackConstants.LONG_PRESS
        //     ACTION_UP with no drag → immediate tap action + haptic

        // 12. Button click handlers:
        //     Accept → Intent(ACTION_ACCEPT) with callId → startForegroundService → finish()
        //     Decline → Intent(ACTION_DECLINE) with callId → startForegroundService → finish()

        // 13. Register LocalBroadcastReceiver for BROADCAST_DISMISSED → finish()

        // 14. Display cutout: window.attributes.layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    }

    override fun onDestroy() {
        super.onDestroy()
        isActivityAlive = false
        wakeLock?.release(); wakeLock = null
        // unregister LocalBroadcastReceiver
    }

    override fun onBackPressed() { /* blocked — user must accept or decline */ }

    // getCircularBitmap(bitmap):
    //   val output = Bitmap.createBitmap(size, size, ARGB_8888)
    //   val canvas = Canvas(output)
    //   val paint = Paint(ANTI_ALIAS_FLAG)
    //   paint.shader = BitmapShader(scaled, CLAMP, CLAMP)
    //   canvas.drawCircle(center, center, radius, paint)
    //   return output
}
```

### AnswerTrampolineActivity.kt

Invisible Activity for Android 12+ notification answer action (background Activity start restrictions).

```
class AnswerTrampolineActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callId = intent.getStringExtra(EXTRA_CALL_ID) ?: run { finish(); return }

        // Send ACTION_ACCEPT to IncomingCallService
        Intent(this, IncomingCallService::class.java).apply {
            action = ACTION_ACCEPT
            putExtra(EXTRA_CALL_ID, callId)
        }.also { startForegroundService(it) }

        // Launch host app
        packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_SINGLE_TOP or FLAG_ACTIVITY_REORDER_TO_FRONT
            putExtra("incoming_call_kit_answered", true)
            putExtra("incoming_call_kit_call_id", callId)
        }?.also { startActivity(it) }

        finish()
    }
}
```

Theme: `Theme.IncomingCallKit.Transparent`

### Android Resources

**activity_incoming_call.xml:**
```xml
<FrameLayout id="root_layout" match_parent, match_parent, keepScreenOn=true>

  <ImageView id="background_image" match_parent, match_parent, scaleType=centerCrop, visibility=gone />
  <View id="gradient_overlay" match_parent, match_parent />

  <LinearLayout match_parent, match_parent, vertical, gravity=center_horizontal>

    <!-- Top spacer 25% -->
    <View 0dp height, weight=25 />

    <!-- Avatar container -->
    <FrameLayout wrap_content>
      <View id="pulse_ring" 120dp x 120dp, gravity=center />
      <ImageView id="caller_avatar" 96dp x 96dp, gravity=center />
    </FrameLayout>

    <Space 24dp />

    <TextView id="caller_name" wrap, 28sp, bold, #FFFFFF, center, maxLines=2, ellipsize=end />
    <Space 8dp />
    <TextView id="caller_number" wrap, 16sp, #B3FFFFFF, center />
    <Space 8dp />
    <TextView id="call_status" wrap, 14sp, #80FFFFFF, text="Incoming Call" />

    <!-- Fill spacer -->
    <View 0dp height, weight=50 />

    <!-- Buttons row, marginBottom=64dp -->
    <LinearLayout match_parent, wrap, horizontal, gravity=center>

      <!-- Decline -->
      <LinearLayout 0dp weight=1, wrap, center, vertical>
        <FrameLayout id="decline_button_container" 64dp x 64dp>
          <ImageView id="decline_button_icon" match, match,
            background=@drawable/bg_decline_button,
            src=@android:drawable/ic_menu_close_clear_cancel,
            scaleType=centerInside, padding=16dp, tint=#FFFFFF />
        </FrameLayout>
        <TextView id="decline_label" wrap, marginTop=8dp, text="Decline", #FFFFFF, 14sp />
      </LinearLayout>

      <!-- Accept -->
      <LinearLayout 0dp weight=1, wrap, center, vertical>
        <FrameLayout id="accept_button_container" 64dp x 64dp>
          <ImageView id="accept_button_icon" match, match,
            background=@drawable/bg_accept_button,
            src=@android:drawable/ic_menu_call,
            scaleType=centerInside, padding=16dp, tint=#FFFFFF />
        </FrameLayout>
        <TextView id="accept_label" wrap, marginTop=8dp, text="Accept", #FFFFFF, 14sp />
      </LinearLayout>

    </LinearLayout>
  </LinearLayout>
</FrameLayout>
```

**bg_accept_button.xml:**
```xml
<shape android:shape="oval">
  <solid android:color="#FF4CAF50" />
</shape>
```

**bg_decline_button.xml:**
```xml
<shape android:shape="oval">
  <solid android:color="#FFF44336" />
</shape>
```

**styles.xml:**
```xml
<style name="Theme.IncomingCallKit.FullScreen" parent="Theme.AppCompat.NoActionBar">
  <item name="android:windowNoTitle">true</item>
  <item name="android:windowFullscreen">true</item>
  <item name="android:windowBackground">@android:color/black</item>
  <item name="android:statusBarColor">@android:color/transparent</item>
  <item name="android:navigationBarColor">@android:color/transparent</item>
  <item name="android:windowTranslucentStatus">true</item>
  <item name="android:windowTranslucentNavigation">true</item>
</style>

<style name="Theme.IncomingCallKit.Transparent" parent="Theme.AppCompat.NoActionBar">
  <item name="android:windowIsTranslucent">true</item>
  <item name="android:windowBackground">@android:color/transparent</item>
  <item name="android:windowNoTitle">true</item>
  <item name="android:windowIsFloating">true</item>
  <item name="android:backgroundDimEnabled">false</item>
  <item name="android:windowAnimationStyle">@null</item>
</style>
```

**AndroidManifest.xml:**
```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_PHONE_CALL" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.DISABLE_KEYGUARD" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />

<activity android:name=".IncomingCallActivity"
  android:exported="false" android:launchMode="singleInstance" android:taskAffinity=""
  android:showOnLockScreen="true" android:showWhenLocked="true" android:turnScreenOn="true"
  android:noHistory="true" android:excludeFromRecents="true"
  android:theme="@style/Theme.IncomingCallKit.FullScreen" />

<activity android:name=".AnswerTrampolineActivity"
  android:exported="false" android:taskAffinity=""
  android:noHistory="true" android:excludeFromRecents="true"
  android:theme="@style/Theme.IncomingCallKit.Transparent" />

<service android:name=".IncomingCallService"
  android:exported="false" android:foregroundServiceType="phoneCall" />
```

**build.gradle:**
```
compileSdk 34, minSdk 21, targetSdk 34
dependencies:
  implementation "androidx.core:core-ktx:1.12.0"
  implementation "androidx.appcompat:appcompat:1.6.1"
  implementation "androidx.localbroadcastmanager:localbroadcastmanager:1.1.0"
```

---

## iOS NATIVE — `IncomingCallKitPlugin.swift`

```swift
public class IncomingCallKitPlugin: NSObject, FlutterPlugin, CXProviderDelegate {

    private var channel: FlutterMethodChannel
    private var eventChannel: FlutterEventChannel
    private var eventSink: FlutterEventSink?
    private var provider: CXProvider
    private var callController: CXCallController
    private var activeCallParams: [UUID: [String: Any]] = [:]

    // register(with registrar):
    //   Create MethodChannel + EventChannel
    //   Set up CXProvider with default config

    // handle(_ call, result):
    //   "show" → handleShow(call, result)
    //   "dismiss" → handleDismiss(call, result)
    //   "dismissAll" → handleDismissAll(result)
    //   Others → FlutterMethodNotImplemented

    // handleShow:
    //   Parse IOSCallKitParams from args
    //   Create CXProviderConfiguration:
    //     supportsVideo, maximumCallGroups, maximumCallsPerCallGroup
    //     iconTemplateImageData from iconName in Images.xcassets
    //     ringtoneSound from ringtonePath
    //     supportedHandleTypes from handleType
    //   Create CXCallUpdate:
    //     remoteHandle = CXHandle(type: .generic, value: callerNumber ?? callerName)
    //     localizedCallerName = callerName
    //     supportsVideo, supportsDTMF, supportsHolding
    //   provider.reportNewIncomingCall(with: uuid, update: callUpdate)
    //   Store params in activeCallParams

    // handleDismiss:
    //   CXEndCallAction(call: uuid) via callController.request(transaction)

    // CXProviderDelegate:
    //   provider:perform CXAnswerCallAction → eventSink(["action":"accept","callId":id,"extra":extra])
    //   provider:perform CXEndCallAction → eventSink(["action":"decline","callId":id,"extra":extra])
    //   provider:timedOutPerforming → eventSink(["action":"timeout","callId":id,"extra":extra])
}
```

**iOS limitations**: NO custom UI. Apple enforces CallKit. Only configurable: caller name, app icon, ringtone, supportsVideo, DTMF, hold, grouping.

---

## EXAMPLE APP

File: `example/lib/main.dart`

```dart
// Two buttons: "Simulate Incoming Call" and "Cancel Call"

void _simulateIncoming() {
  IncomingCallKit.instance.show(CallKitParams(
    id: 'test-call-001',
    callerName: 'John Doe',
    callerNumber: '+1 234 567 8900',
    avatar: 'https://i.pravatar.cc/200',
    duration: const Duration(seconds: 30),
    missedCallNotification: const NotificationParams(
      showNotification: true,
      subtitle: 'Missed Call',
      showCallback: true,
    ),
    android: const AndroidCallKitParams(
      // Uses all default colors — no overrides needed
      // Background: default gradient #1A1A2E → #16213E → #0F3460
      // Accept: #4CAF50, Decline: #F44336
      // Caller name: #FFFFFF 28sp, Number: #B3FFFFFF 16sp
      avatarPulseAnimation: true,
      enableSwipeGesture: true,
    ),
    ios: const IOSCallKitParams(
      handleType: 'generic',
      supportsVideo: false,
    ),
  ));
}

void _cancelCall() {
  IncomingCallKit.instance.dismiss('test-call-001');
}

// Listen to events:
IncomingCallKit.instance.onEvent.listen((event) {
  switch (event.action) {
    case CallKitAction.accept: print('Accepted: ${event.callId}');
    case CallKitAction.decline: print('Declined: ${event.callId}');
    case CallKitAction.timeout: print('Timed out: ${event.callId}');
    case CallKitAction.callback: print('Callback: ${event.callId}');
    case CallKitAction.dismissed: print('Dismissed: ${event.callId}');
  }
});
```

---

## PUBSPEC.YAML

```yaml
name: incoming_call_kit
description: Highly customizable Flutter plugin for incoming call UI. Custom Android full-screen with gradients and swipe gestures. CallKit for iOS.
version: 0.0.1
homepage: https://github.com/ashiqualii/incoming_call_kit

environment:
  sdk: ^3.6.0
  flutter: ">=3.27.0"

dependencies:
  flutter:
    sdk: flutter
  plugin_platform_interface: ^2.1.0

flutter:
  plugin:
    platforms:
      android:
        package: com.iocod.incoming_call_kit
        pluginClass: IncomingCallKitPlugin
      ios:
        pluginClass: IncomingCallKitPlugin
```

---

## EDGE CASES TO HANDLE

1. **isActivityAlive stuck** → reset if `System.currentTimeMillis() - lastAliveTimestamp > 60000`
2. **fullScreenIntent fails on Samsung/Xiaomi** → dual post: `startForeground()` + `NotificationManager.notify()`
3. **Android 12+ background Activity start blocked** → `AnswerTrampolineActivity` via PendingIntent
4. **Foreground service 5-second ANR** → `startForeground()` IMMEDIATELY in `onStartCommand()`
5. **Config unavailable after process kill** → all config in SharedPreferences
6. **PendingIntent collisions** → unique request codes: `(callId.hashCode() and 0x7FFFFFFF) % 10000 + offset`
7. **FULL_WAKE_LOCK deprecated** → use `setTurnScreenOn(true)` + `fullScreenIntent` as primary wake mechanism
8. **Avatar load timeout** → 5s HttpURLConnection timeout, show initials immediately as fallback

---

## IMPLEMENTATION ORDER

1. **Phase 1**: Dart models (steps 1–6), controller (7), platform layer (8) — do FIRST
2. **Phase 2**: Flutter IncomingCallScreen widget (9–10) — parallel with Phase 3
3. **Phase 3**: Android native — Constants, ConfigStore, Plugin, RingtoneManager, NotificationBuilder, Service, Activity, Trampoline, resources (11–20) — parallel with Phase 2
4. **Phase 4**: iOS native — Swift plugin (21) — after Phase 1
5. **Phase 5**: Example app, pubspec, README (22–24)