import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/connectivity/connectivity_service.dart';
import '../../core/connectivity/offline_notifier.dart';
import '../../core/data/models/app_profile_stats.dart';
import '../../core/data/repositories/providers.dart';
import '../../core/logging/app_logger.dart';

class ProfileStatsNotifier extends Notifier<AppProfileStats?> {
  static const _refreshInterval = Duration(minutes: 5);

  @override
  AppProfileStats? build() {
    Future.microtask(refresh);
    final timer = Timer.periodic(_refreshInterval, (_) => refresh());
    ref.onDispose(timer.cancel);
    return null;
  }

  Future<void> refresh() async {
    if (!await ensureOnline(ref)) return;
    try {
      state = await ref.read(profileRepositoryProvider).getProfileStats();
    } catch (e, st) {
      AppLogger.logError('ProfileStatsNotifier.refresh', e, st);
      if (isConnectivityError(e)) {
        ref.read(isOfflineProvider.notifier).setOffline(true);
      }
    }
  }
}

final profileStatsNotifierProvider =
    NotifierProvider<ProfileStatsNotifier, AppProfileStats?>(ProfileStatsNotifier.new);
