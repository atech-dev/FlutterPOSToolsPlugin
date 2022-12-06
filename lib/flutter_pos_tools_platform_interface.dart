import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_pos_tools_method_channel.dart';

abstract class FlutterPosToolsPlatform extends PlatformInterface {
  /// Constructs a FlutterPosToolsPlatform.
  FlutterPosToolsPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterPosToolsPlatform _instance = MethodChannelFlutterPosTools();

  /// The default instance of [FlutterPosToolsPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterPosTools].
  static FlutterPosToolsPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterPosToolsPlatform] when
  /// they register themselves.
  static set instance(FlutterPosToolsPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> getSerialNumber() {
    throw UnimplementedError('getSerialNumber() has not been implemented.');
  }
}
