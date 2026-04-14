import 'gradient_config.dart';

class AndroidCallKitParams {
  final String? backgroundColor;
  final GradientConfig? backgroundGradient;
  final String? backgroundImageUrl;
  final double avatarSize;
  final String? avatarBorderColor;
  final double avatarBorderWidth;
  final bool avatarPulseAnimation;
  final String? initialsBackgroundColor;
  final String? initialsTextColor;
  final String? callerNameColor;
  final double callerNameFontSize;
  final String? callerNumberColor;
  final double callerNumberFontSize;
  final String? statusText;
  final String? statusTextColor;
  final String? acceptButtonColor;
  final String? declineButtonColor;
  final double buttonSize;
  final bool enableSwipeGesture;
  final double swipeThreshold;
  final String? ringtonePath;
  final bool enableVibration;
  final List<int>? vibrationPattern;
  final bool showOnLockScreen;
  final String? channelName;
  final String? logoUrl;
  final bool showCallerIdInNotification;

  const AndroidCallKitParams({
    this.backgroundColor,
    this.backgroundGradient,
    this.backgroundImageUrl,
    this.avatarSize = 96,
    this.avatarBorderColor,
    this.avatarBorderWidth = 0,
    this.avatarPulseAnimation = true,
    this.initialsBackgroundColor = '#3A3A5C',
    this.initialsTextColor = '#FFFFFF',
    this.callerNameColor = '#FFFFFF',
    this.callerNameFontSize = 28,
    this.callerNumberColor = '#B3FFFFFF',
    this.callerNumberFontSize = 16,
    this.statusText = 'Incoming Call',
    this.statusTextColor = '#80FFFFFF',
    this.acceptButtonColor = '#4CAF50',
    this.declineButtonColor = '#F44336',
    this.buttonSize = 64,
    this.enableSwipeGesture = true,
    this.swipeThreshold = 120,
    this.ringtonePath,
    this.enableVibration = true,
    this.vibrationPattern = const [0, 1000, 1000],
    this.showOnLockScreen = true,
    this.channelName = 'Incoming Calls',
    this.logoUrl,
    this.showCallerIdInNotification = true,
  }) : assert(
         backgroundColor == null || backgroundGradient == null,
         'Cannot set both backgroundColor and backgroundGradient. '
         'Use backgroundGradient for gradients, or backgroundColor for solid colors.',
       );

  Map<String, dynamic> toMap() {
    return {
      'backgroundColor': backgroundColor,
      if (backgroundGradient != null)
        'backgroundGradient': backgroundGradient!.toMap(),
      'backgroundImageUrl': backgroundImageUrl,
      'avatarSize': avatarSize,
      'avatarBorderColor': avatarBorderColor,
      'avatarBorderWidth': avatarBorderWidth,
      'avatarPulseAnimation': avatarPulseAnimation,
      'initialsBackgroundColor': initialsBackgroundColor,
      'initialsTextColor': initialsTextColor,
      'callerNameColor': callerNameColor,
      'callerNameFontSize': callerNameFontSize,
      'callerNumberColor': callerNumberColor,
      'callerNumberFontSize': callerNumberFontSize,
      'statusText': statusText,
      'statusTextColor': statusTextColor,
      'acceptButtonColor': acceptButtonColor,
      'declineButtonColor': declineButtonColor,
      'buttonSize': buttonSize,
      'enableSwipeGesture': enableSwipeGesture,
      'swipeThreshold': swipeThreshold,
      'ringtonePath': ringtonePath,
      'enableVibration': enableVibration,
      'vibrationPattern': vibrationPattern,
      'showOnLockScreen': showOnLockScreen,
      'channelName': channelName,
      'logoUrl': logoUrl,
      'showCallerIdInNotification': showCallerIdInNotification,
    };
  }

  factory AndroidCallKitParams.fromMap(Map<String, dynamic> map) {
    return AndroidCallKitParams(
      backgroundColor: map['backgroundColor'] as String?,
      backgroundGradient: map['backgroundGradient'] != null
          ? GradientConfig.fromMap(
              Map<String, dynamic>.from(map['backgroundGradient'] as Map),
            )
          : null,
      backgroundImageUrl: map['backgroundImageUrl'] as String?,
      avatarSize: (map['avatarSize'] as num?)?.toDouble() ?? 96,
      avatarBorderColor: map['avatarBorderColor'] as String?,
      avatarBorderWidth: (map['avatarBorderWidth'] as num?)?.toDouble() ?? 0,
      avatarPulseAnimation: map['avatarPulseAnimation'] as bool? ?? true,
      initialsBackgroundColor: map['initialsBackgroundColor'] as String?,
      initialsTextColor: map['initialsTextColor'] as String?,
      callerNameColor: map['callerNameColor'] as String?,
      callerNameFontSize: (map['callerNameFontSize'] as num?)?.toDouble() ?? 28,
      callerNumberColor: map['callerNumberColor'] as String?,
      callerNumberFontSize:
          (map['callerNumberFontSize'] as num?)?.toDouble() ?? 16,
      statusText: map['statusText'] as String?,
      statusTextColor: map['statusTextColor'] as String?,
      acceptButtonColor: map['acceptButtonColor'] as String?,
      declineButtonColor: map['declineButtonColor'] as String?,
      buttonSize: (map['buttonSize'] as num?)?.toDouble() ?? 64,
      enableSwipeGesture: map['enableSwipeGesture'] as bool? ?? true,
      swipeThreshold: (map['swipeThreshold'] as num?)?.toDouble() ?? 120,
      ringtonePath: map['ringtonePath'] as String?,
      enableVibration: map['enableVibration'] as bool? ?? true,
      vibrationPattern: (map['vibrationPattern'] as List?)?.cast<int>(),
      showOnLockScreen: map['showOnLockScreen'] as bool? ?? true,
      channelName: map['channelName'] as String?,
      logoUrl: map['logoUrl'] as String?,
      showCallerIdInNotification:
          map['showCallerIdInNotification'] as bool? ?? true,
    );
  }

  AndroidCallKitParams copyWith({
    String? backgroundColor,
    GradientConfig? backgroundGradient,
    String? backgroundImageUrl,
    double? avatarSize,
    String? avatarBorderColor,
    double? avatarBorderWidth,
    bool? avatarPulseAnimation,
    String? initialsBackgroundColor,
    String? initialsTextColor,
    String? callerNameColor,
    double? callerNameFontSize,
    String? callerNumberColor,
    double? callerNumberFontSize,
    String? statusText,
    String? statusTextColor,
    String? acceptButtonColor,
    String? declineButtonColor,
    double? buttonSize,
    bool? enableSwipeGesture,
    double? swipeThreshold,
    String? ringtonePath,
    bool? enableVibration,
    List<int>? vibrationPattern,
    bool? showOnLockScreen,
    String? channelName,
    String? logoUrl,
    bool? showCallerIdInNotification,
  }) {
    return AndroidCallKitParams(
      backgroundColor: backgroundColor ?? this.backgroundColor,
      backgroundGradient: backgroundGradient ?? this.backgroundGradient,
      backgroundImageUrl: backgroundImageUrl ?? this.backgroundImageUrl,
      avatarSize: avatarSize ?? this.avatarSize,
      avatarBorderColor: avatarBorderColor ?? this.avatarBorderColor,
      avatarBorderWidth: avatarBorderWidth ?? this.avatarBorderWidth,
      avatarPulseAnimation: avatarPulseAnimation ?? this.avatarPulseAnimation,
      initialsBackgroundColor:
          initialsBackgroundColor ?? this.initialsBackgroundColor,
      initialsTextColor: initialsTextColor ?? this.initialsTextColor,
      callerNameColor: callerNameColor ?? this.callerNameColor,
      callerNameFontSize: callerNameFontSize ?? this.callerNameFontSize,
      callerNumberColor: callerNumberColor ?? this.callerNumberColor,
      callerNumberFontSize: callerNumberFontSize ?? this.callerNumberFontSize,
      statusText: statusText ?? this.statusText,
      statusTextColor: statusTextColor ?? this.statusTextColor,
      acceptButtonColor: acceptButtonColor ?? this.acceptButtonColor,
      declineButtonColor: declineButtonColor ?? this.declineButtonColor,
      buttonSize: buttonSize ?? this.buttonSize,
      enableSwipeGesture: enableSwipeGesture ?? this.enableSwipeGesture,
      swipeThreshold: swipeThreshold ?? this.swipeThreshold,
      ringtonePath: ringtonePath ?? this.ringtonePath,
      enableVibration: enableVibration ?? this.enableVibration,
      vibrationPattern: vibrationPattern ?? this.vibrationPattern,
      showOnLockScreen: showOnLockScreen ?? this.showOnLockScreen,
      channelName: channelName ?? this.channelName,
      logoUrl: logoUrl ?? this.logoUrl,
      showCallerIdInNotification:
          showCallerIdInNotification ?? this.showCallerIdInNotification,
    );
  }
}
