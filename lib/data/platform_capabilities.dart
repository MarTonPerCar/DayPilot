import 'package:flutter/foundation.dart';

class PlatformCapabilities {
  PlatformCapabilities._();

  /// Tech Health enforcement is only wired up for Android — technically
  /// possible on other platforms too, just not built yet.
  static bool get supportsDeviceFeatures => defaultTargetPlatform == TargetPlatform.android;
}
