import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_pos_tools_platform_interface.dart';

/// An implementation of [FlutterPosToolsPlatform] that uses method channels.
class MethodChannelFlutterPosTools extends FlutterPosToolsPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_pos_tools');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<String?> getSerialNumber() async {
    return await methodChannel.invokeMethod<String>('getSerialNumber');
  }
}
