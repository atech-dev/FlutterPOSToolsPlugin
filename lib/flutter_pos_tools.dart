
import 'flutter_pos_tools_platform_interface.dart';

class FlutterPosTools {
  Future<String?> getPlatformVersion() {
    return FlutterPosToolsPlatform.instance.getPlatformVersion();
  }

  Future<String?> getSerialNumber() {
  return FlutterPosToolsPlatform.instance.getSerialNumber();
  }
}
