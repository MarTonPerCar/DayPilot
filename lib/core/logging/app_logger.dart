import 'dart:io';

import 'package:flutter/foundation.dart';

class AppLogger {
  AppLogger._();

  static File? _file;

  static Future<void> init() async {
    try {
      final dir = File(Platform.resolvedExecutable).parent;
      _file = File('${dir.path}${Platform.pathSeparator}daypilot_log.txt');
      _file!.writeAsStringSync('');
      log('=== DayPilot log started ===');
      log('Platform: ${Platform.operatingSystem} ${Platform.operatingSystemVersion}');
      log('Executable: ${Platform.resolvedExecutable}');
    } catch (e, st) {
      debugPrint('AppLogger init failed: $e\n$st');
    }
  }

  static void log(String message) {
    final line = '[${DateTime.now().toIso8601String()}] $message';
    debugPrint(line);
    try {
      _file?.writeAsStringSync('$line\n', mode: FileMode.append);
    } catch (_) {

    }
  }

  static void logError(String context, Object error, StackTrace stackTrace) {
    log('ERROR in $context: $error\n$stackTrace');
  }
}
