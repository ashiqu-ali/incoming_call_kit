enum CallKitAction {
  // Incoming
  accept,
  decline,
  timeout,
  dismissed,
  callback,
  // Outgoing
  callStart,
  callConnected,
  callEnded,
  // iOS specific
  audioSessionActivated,
  toggleHold,
  toggleMute,
  toggleDmtf,
  toggleGroup,
  // VoIP
  voipTokenUpdated,
}

class CallKitEvent {
  final CallKitAction action;
  final String callId;
  final Map<String, dynamic>? extra;

  const CallKitEvent({required this.action, required this.callId, this.extra});

  factory CallKitEvent.fromMap(Map<String, dynamic> map) {
    return CallKitEvent(
      action: _parseAction(map['action'] as String? ?? ''),
      callId: map['callId'] as String? ?? '',
      extra: map['extra'] != null
          ? Map<String, dynamic>.from(map['extra'] as Map)
          : null,
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'action': action.name,
      'callId': callId,
      if (extra != null) 'extra': extra,
    };
  }

  static CallKitAction _parseAction(String action) {
    return CallKitAction.values.firstWhere(
      (e) => e.name == action,
      orElse: () => CallKitAction.dismissed,
    );
  }

  @override
  String toString() =>
      'CallKitEvent(action: $action, callId: $callId, extra: $extra)';
}
