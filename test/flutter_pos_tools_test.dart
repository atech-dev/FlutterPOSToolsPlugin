import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_pos_tools/flutter_pos_tools.dart';
import 'package:flutter_pos_tools/flutter_pos_tools_platform_interface.dart';
import 'package:flutter_pos_tools/flutter_pos_tools_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFlutterPosToolsPlatform
    with MockPlatformInterfaceMixin
    implements FlutterPosToolsPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final FlutterPosToolsPlatform initialPlatform = FlutterPosToolsPlatform.instance;

  test('$MethodChannelFlutterPosTools is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFlutterPosTools>());
  });

  test('getPlatformVersion', () async {
    FlutterPosTools flutterPosToolsPlugin = FlutterPosTools();
    MockFlutterPosToolsPlatform fakePlatform = MockFlutterPosToolsPlatform();
    FlutterPosToolsPlatform.instance = fakePlatform;

    expect(await flutterPosToolsPlugin.getPlatformVersion(), '42');
  });
}
