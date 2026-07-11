import 'dart:io';

import 'package:flutter/foundation.dart';

/// Writes a plain-text log file next to the running executable — easy to
/// find and send along when reporting a bug. Starts fresh every launch.
///
/// Uses synchronous file writes deliberately: this exists specifically to
/// capture what happened right before a crash, so a write that's still
/// in flight when the process dies is useless.
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
      // Nothing sensible to do if the log file itself can't be written.
    }
  }

  static void logError(String context, Object error, StackTrace stackTrace) {
    log('ERROR in $context: $error\n$stackTrace');
  }
}
