import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_pos_tools/flutter_pos_tools_method_channel.dart';

void main() {
  MethodChannelFlutterPosTools platform = MethodChannelFlutterPosTools();
  const MethodChannel channel = MethodChannel('flutter_pos_tools');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await platform.getPlatformVersion(), '42');
  });
}
