import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/data/models/app_profile_stats.dart';
import '../../core/data/repositories/providers.dart';

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
    state = await ref.read(profileRepositoryProvider).getProfileStats();
  }
}

final profileStatsNotifierProvider =
    NotifierProvider<ProfileStatsNotifier, AppProfileStats?>(ProfileStatsNotifier.new);
