import 'package:flutter/foundation.dart';

class PlatformCapabilities {
  PlatformCapabilities._();

  /// Tech Health's enforcement (usage monitoring + blocking) is only wired
  /// up for Android today (UsageStatsManager + AccessibilityService).
  /// Windows/Linux have their own foreground-window APIs that could support
  /// this, and iOS has Apple's Family Controls/Screen Time framework — but
  /// that one needs a special entitlement Apple grants on review, unlike
  /// Android's freely-available APIs. None of these are implemented here.
  static bool get supportsDeviceFeatures => defaultTargetPlatform == TargetPlatform.android;
}
