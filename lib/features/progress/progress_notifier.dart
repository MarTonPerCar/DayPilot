import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/data/models/app_progress.dart';
import '../../core/data/repositories/providers.dart';
import '../notifications/notifications_notifier.dart';

class ProgressNotifier extends Notifier<AppProgress?> {
  static const _refreshInterval = Duration(minutes: 5);

  @override
  AppProgress? build() {
    Future.microtask(refresh);
    final timer = Timer.periodic(_refreshInterval, (_) => refresh());
    ref.onDispose(timer.cancel);
    return null;
  }

  Future<void> refresh() async {
    state = await ref.read(progressRepositoryProvider).getProgress();
  }

  Future<void> completeTimerSession() async {
    final awarded = await ref.read(progressRepositoryProvider).completeTimerSession();
    if (!awarded) return;
    await refresh();
    await ref.read(notificationsNotifierProvider.notifier).refresh();
  }
}

final progressNotifierProvider = NotifierProvider<ProgressNotifier, AppProgress?>(ProgressNotifier.new);
