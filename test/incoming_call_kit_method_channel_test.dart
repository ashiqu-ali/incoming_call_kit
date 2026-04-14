import 'package:flutter_test/flutter_test.dart';
import 'package:incoming_call_kit/incoming_call_kit.dart';

void main() {
  test('NotificationParams serialization', () {
    const params = NotificationParams(
      showNotification: true,
      subtitle: 'Missed Call',
      showCallback: true,
      callbackText: 'Call Back',
    );

    final map = params.toMap();
    expect(map['showNotification'], true);
    expect(map['subtitle'], 'Missed Call');
    expect(map['callbackText'], 'Call Back');

    final restored = NotificationParams.fromMap(map);
    expect(restored.showNotification, params.showNotification);
    expect(restored.subtitle, params.subtitle);
  });

  test('CallKitParams with all params serializes correctly', () {
    final params = CallKitParams(
      id: 'abc',
      callerName: 'Alice',
      callerNumber: '+1000',
      avatar: 'https://example.com/avatar.png',
      type: 1,
      textAccept: 'Answer',
      textDecline: 'Reject',
      duration: const Duration(seconds: 45),
      extra: {'room': 'r1'},
      missedCallNotification: const NotificationParams(
        showNotification: true,
        subtitle: 'You missed a call',
      ),
      android: const AndroidCallKitParams(
        backgroundColor: '#112233',
        enableSwipeGesture: false,
      ),
      ios: const IOSCallKitParams(
        supportsVideo: true,
        handleType: 'phoneNumber',
      ),
    );

    final map = params.toMap();
    expect(map['id'], 'abc');
    expect(map['avatar'], 'https://example.com/avatar.png');
    expect(map['duration'], 45000);
    expect(map['android']['backgroundColor'], '#112233');
    expect(map['android']['enableSwipeGesture'], false);
    expect(map['ios']['supportsVideo'], true);
  });
}
