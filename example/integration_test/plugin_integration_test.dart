import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';

import 'package:incoming_call_kit/incoming_call_kit.dart';

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('getActiveCalls returns list', (WidgetTester tester) async {
    final calls = await IncomingCallKit.instance.getActiveCalls();
    expect(calls, isA<List<String>>());
  });
}
