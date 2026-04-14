class IOSCallKitParams {
  final String? iconName;
  final String handleType;
  final bool supportsVideo;
  final int maximumCallGroups;
  final int maximumCallsPerCallGroup;
  final String? ringtonePath;
  final bool supportsDTMF;
  final bool supportsHolding;

  const IOSCallKitParams({
    this.iconName = 'CallKitLogo',
    this.handleType = 'generic',
    this.supportsVideo = false,
    this.maximumCallGroups = 2,
    this.maximumCallsPerCallGroup = 1,
    this.ringtonePath,
    this.supportsDTMF = true,
    this.supportsHolding = false,
  });

  Map<String, dynamic> toMap() {
    return {
      'iconName': iconName,
      'handleType': handleType,
      'supportsVideo': supportsVideo,
      'maximumCallGroups': maximumCallGroups,
      'maximumCallsPerCallGroup': maximumCallsPerCallGroup,
      'ringtonePath': ringtonePath,
      'supportsDTMF': supportsDTMF,
      'supportsHolding': supportsHolding,
    };
  }

  factory IOSCallKitParams.fromMap(Map<String, dynamic> map) {
    return IOSCallKitParams(
      iconName: map['iconName'] as String?,
      handleType: map['handleType'] as String? ?? 'generic',
      supportsVideo: map['supportsVideo'] as bool? ?? false,
      maximumCallGroups: map['maximumCallGroups'] as int? ?? 2,
      maximumCallsPerCallGroup: map['maximumCallsPerCallGroup'] as int? ?? 1,
      ringtonePath: map['ringtonePath'] as String?,
      supportsDTMF: map['supportsDTMF'] as bool? ?? true,
      supportsHolding: map['supportsHolding'] as bool? ?? false,
    );
  }

  IOSCallKitParams copyWith({
    String? iconName,
    String? handleType,
    bool? supportsVideo,
    int? maximumCallGroups,
    int? maximumCallsPerCallGroup,
    String? ringtonePath,
    bool? supportsDTMF,
    bool? supportsHolding,
  }) {
    return IOSCallKitParams(
      iconName: iconName ?? this.iconName,
      handleType: handleType ?? this.handleType,
      supportsVideo: supportsVideo ?? this.supportsVideo,
      maximumCallGroups: maximumCallGroups ?? this.maximumCallGroups,
      maximumCallsPerCallGroup:
          maximumCallsPerCallGroup ?? this.maximumCallsPerCallGroup,
      ringtonePath: ringtonePath ?? this.ringtonePath,
      supportsDTMF: supportsDTMF ?? this.supportsDTMF,
      supportsHolding: supportsHolding ?? this.supportsHolding,
    );
  }
}
