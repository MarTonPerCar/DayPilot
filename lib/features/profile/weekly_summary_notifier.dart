import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/data/models/app_weekly_summary.dart';
import '../../core/data/repositories/providers.dart';
import '../../core/logging/app_logger.dart';

class WeeklySummaryNotifier extends Notifier<AppWeeklySummary?> {
  static const _refreshInterval = Duration(minutes: 5);

  @override
  AppWeeklySummary? build() {
    Future.microtask(refresh);
    final timer = Timer.periodic(_refreshInterval, (_) => refresh());
    ref.onDispose(timer.cancel);
    return null;
  }

  Future<void> refresh() async {
    try {
      state = await ref.read(profileRepositoryProvider).getWeeklySummary();
    } catch (e, st) {
      AppLogger.logError('WeeklySummaryNotifier.refresh', e, st);
    }
  }
}

final weeklySummaryNotifierProvider =
    NotifierProvider<WeeklySummaryNotifier, AppWeeklySummary?>(WeeklySummaryNotifier.new);
