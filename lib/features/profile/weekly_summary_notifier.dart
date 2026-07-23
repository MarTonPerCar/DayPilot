import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/connectivity/connectivity_service.dart';
import '../../core/connectivity/offline_notifier.dart';
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
    if (!await ensureOnline(ref)) return;
    try {
      state = await ref.read(profileRepositoryProvider).getWeeklySummary();
    } catch (e, st) {
      AppLogger.logError('WeeklySummaryNotifier.refresh', e, st);
      if (isConnectivityError(e)) {
        ref.read(isOfflineProvider.notifier).setOffline(true);
      }
    }
  }
}

final weeklySummaryNotifierProvider =
    NotifierProvider<WeeklySummaryNotifier, AppWeeklySummary?>(WeeklySummaryNotifier.new);
