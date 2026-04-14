class NotificationParams {
  final bool showNotification;
  final String subtitle;
  final bool showCallback;
  final String callbackText;

  const NotificationParams({
    this.showNotification = true,
    this.subtitle = 'Missed Call',
    this.showCallback = true,
    this.callbackText = 'Call Back',
  });

  Map<String, dynamic> toMap() {
    return {
      'showNotification': showNotification,
      'subtitle': subtitle,
      'showCallback': showCallback,
      'callbackText': callbackText,
    };
  }

  factory NotificationParams.fromMap(Map<String, dynamic> map) {
    return NotificationParams(
      showNotification: map['showNotification'] as bool? ?? true,
      subtitle: map['subtitle'] as String? ?? 'Missed Call',
      showCallback: map['showCallback'] as bool? ?? true,
      callbackText: map['callbackText'] as String? ?? 'Call Back',
    );
  }

  NotificationParams copyWith({
    bool? showNotification,
    String? subtitle,
    bool? showCallback,
    String? callbackText,
  }) {
    return NotificationParams(
      showNotification: showNotification ?? this.showNotification,
      subtitle: subtitle ?? this.subtitle,
      showCallback: showCallback ?? this.showCallback,
      callbackText: callbackText ?? this.callbackText,
    );
  }
}
