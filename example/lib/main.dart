import 'dart:async';

import 'package:flutter/material.dart';
import 'package:incoming_call_kit/incoming_call_kit.dart';

@pragma('vm:entry-point')
Future<void> _backgroundCallHandler(CallKitEvent event) async {
  debugPrint('[BG] Event: ${event.action} for call ${event.callId}');
}

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  IncomingCallKit.registerBackgroundHandler(_backgroundCallHandler);
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'IncomingCallKit Example',
      theme: ThemeData(colorSchemeSeed: Colors.deepPurple, useMaterial3: true),
      home: const HomePage(),
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  final _callKit = IncomingCallKit.instance;
  final _events = <String>[];
  StreamSubscription<CallKitEvent>? _eventSubscription;
  bool _autoStartAvailable = false;
  String _voipToken = '';

  @override
  void initState() {
    super.initState();
    _listenEvents();
    _checkPermissions();
  }

  @override
  void dispose() {
    _eventSubscription?.cancel();
    super.dispose();
  }

  void _listenEvents() {
    _eventSubscription = _callKit.onEvent.listen((event) {
      setState(() {
        _events.insert(
          0,
          '${event.action.name} — ${event.callId.substring(0, 8)}…',
        );
        if (_events.length > 50) _events.removeLast();
      });
    });
  }

  Future<void> _checkPermissions() async {
    final autoStart = await _callKit.isAutoStartAvailable();
    final token = await _callKit.getDevicePushTokenVoIP();
    if (!mounted) return;
    setState(() {
      _autoStartAvailable = autoStart;
      _voipToken = token;
    });
  }

  CallKitParams _buildIncomingParams() {
    return CallKitParams(
      id: 'call-${DateTime.now().millisecondsSinceEpoch}',
      callerName: 'John Doe',
      callerNumber: '+1 234 567 890',
      avatar: 'https://i.pravatar.cc/120',
      type: 0,
      duration: const Duration(seconds: 30),
      textAccept: 'Accept',
      textDecline: 'Decline',
      extra: {'userId': '12345'},
      missedCallNotification: NotificationParams(
        showNotification: true,
        subtitle: 'Missed call from John Doe',
        showCallback: true,
        callbackText: 'Call Back',
      ),
      android: AndroidCallKitParams(
        backgroundGradient: GradientConfig(
          colors: ['#1A1A2E', '#16213E', '#0F3460'],
        ),
        showOnLockScreen: true,
        ringtonePath: 'system_ringtone_default',
      ),
      ios: IOSCallKitParams(handleType: 'phoneNumber', supportsVideo: false),
    );
  }

  CallKitParams _buildOutgoingParams() {
    return CallKitParams(
      id: 'out-${DateTime.now().millisecondsSinceEpoch}',
      callerName: 'Jane Smith',
      callerNumber: '+1 987 654 321',
      avatar: 'https://i.pravatar.cc/120',
      type: 0,
      extra: {'userId': '67890'},
      android: AndroidCallKitParams(),
      ios: IOSCallKitParams(),
    );
  }

  Future<void> _showIncomingCall() async {
    await _callKit.show(_buildIncomingParams());
  }

  Future<void> _showIncomingCallDelayed() async {
    // 7-second delay — press the button, then lock the screen
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('Incoming call in 7 seconds — lock the screen now!'),
      ),
    );
    await Future.delayed(const Duration(seconds: 7));
    await _callKit.show(_buildIncomingParams());
  }

  Future<void> _dismissAll() async {
    await _callKit.dismissAll();
  }

  Future<void> _startOutgoingCall() async {
    final params = _buildOutgoingParams();
    await _callKit.startCall(params);

    // Simulate connection after 2 seconds
    Future.delayed(const Duration(seconds: 2), () {
      _callKit.setCallConnected(params.id);
    });
  }

  Future<void> _endAllCalls() async {
    await _callKit.endAllCalls();
  }

  Future<void> _showMissedCall() async {
    await _callKit.showMissedCallNotification(
      CallKitParams(
        id: 'missed-${DateTime.now().millisecondsSinceEpoch}',
        callerName: 'Missed Caller',
        avatar: 'https://i.pravatar.cc/120',
        callerNumber: '+1 111 222 333',
        missedCallNotification: NotificationParams(
          showNotification: true,
          subtitle: 'Missed Call',
          showCallback: true,
          callbackText: 'Call Back',
        ),
        android: AndroidCallKitParams(),
        ios: IOSCallKitParams(),
      ),
    );
  }

  Future<void> _requestPermissions() async {
    final hasNotif = await _callKit.hasNotificationPermission();
    if (!hasNotif) {
      await _callKit.requestNotificationPermission();
    }

    final canFullScreen = await _callKit.canUseFullScreenIntent();
    if (!canFullScreen) {
      await _callKit.requestFullIntentPermission();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('IncomingCallKit Example')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          _buildSection('Incoming Call', [
            _ActionButton('Show Incoming Call', _showIncomingCall),
            _ActionButton(
              'Show After 7s (Lock Screen Test)',
              _showIncomingCallDelayed,
            ),
            _ActionButton('Dismiss All', _dismissAll),
          ]),
          _buildSection('Outgoing Call', [
            _ActionButton('Start Outgoing Call', _startOutgoingCall),
            _ActionButton('End All Calls', _endAllCalls),
          ]),
          _buildSection('Missed Call', [
            _ActionButton('Show Missed Notification', _showMissedCall),
          ]),
          _buildSection('Permissions', [
            _ActionButton('Request Permissions', _requestPermissions),
            if (_autoStartAvailable)
              _ActionButton(
                'Open Autostart Settings',
                _callKit.openAutoStartSettings,
              ),
          ]),
          if (_voipToken.isNotEmpty) ...[
            const SizedBox(height: 16),
            Text(
              'VoIP Token: ${_voipToken.substring(0, 20)}…',
              style: Theme.of(context).textTheme.bodySmall,
            ),
          ],
          const SizedBox(height: 24),
          Text('Events', style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(height: 8),
          if (_events.isEmpty)
            const Text('No events yet', style: TextStyle(color: Colors.grey))
          else
            ..._events.map(
              (e) => Padding(
                padding: const EdgeInsets.symmetric(vertical: 2),
                child: Text(e, style: Theme.of(context).textTheme.bodySmall),
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildSection(String title, List<Widget> children) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        const SizedBox(height: 16),
        Text(title, style: Theme.of(context).textTheme.titleMedium),
        const SizedBox(height: 8),
        ...children,
      ],
    );
  }
}

class _ActionButton extends StatelessWidget {
  const _ActionButton(this.label, this.onPressed);

  final String label;
  final VoidCallback onPressed;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: FilledButton(onPressed: onPressed, child: Text(label)),
    );
  }
}
