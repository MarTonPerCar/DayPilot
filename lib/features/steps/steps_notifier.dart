import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/connectivity/connectivity_service.dart';
import '../../core/connectivity/offline_notifier.dart';
import '../../core/data/models/app_steps.dart';
import '../../core/data/repositories/providers.dart';
import '../../core/logging/app_logger.dart';

class StepsNotifier extends Notifier<AppSteps?> {
  static const _refreshInterval = Duration(minutes: 5);

  @override
  AppSteps? build() {
    Future.microtask(refresh);
    final timer = Timer.periodic(_refreshInterval, (_) => refresh());
    ref.onDispose(timer.cancel);
    return null;
  }

  Future<void> refresh() async {
    if (!await ensureOnline(ref)) return;
    try {
      state = await ref.read(stepsRepositoryProvider).getSteps();
    } catch (e, st) {
      AppLogger.logError('StepsNotifier.refresh', e, st);
      if (isConnectivityError(e)) {
        ref.read(isOfflineProvider.notifier).setOffline(true);
      }
    }
  }

  Future<void> setGoal(int newGoal) async {
    if (!await ensureOnline(ref)) return;
    try {
      await ref.read(stepsRepositoryProvider).setGoal(newGoal);
      await refresh();
    } catch (e, st) {
      AppLogger.logError('StepsNotifier.setGoal', e, st);
      if (isConnectivityError(e)) {
        ref.read(isOfflineProvider.notifier).setOffline(true);
      }
    }
  }
}

final stepsNotifierProvider = NotifierProvider<StepsNotifier, AppSteps?>(StepsNotifier.new);
