import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import '../../core/connectivity/connectivity_service.dart';
import '../../core/connectivity/offline_notifier.dart';
import '../../core/data/models/app_progress.dart';
import '../../core/data/repositories/providers.dart';
import '../../core/logging/app_logger.dart';
import '../notifications/notifications_notifier.dart';

class ProgressNotifier extends Notifier<AppProgress?> {

  static const _refreshInterval = Duration(minutes: 5);
  RealtimeChannel? _channel;
  bool _refreshing = false;

  @override
  AppProgress? build() {
    Future.microtask(refresh);
    final timer = Timer.periodic(_refreshInterval, (_) => refresh());
    ref.onDispose(() {
      timer.cancel();
      _channel?.unsubscribe();
    });
    return null;
  }

  Future<void> refresh() async {
    if (!await ensureOnline(ref)) return;
    try {
      state = await ref.read(progressRepositoryProvider).getProgress();
      _subscribeToRealtimeOnce();
    } catch (e, st) {
      AppLogger.logError('ProgressNotifier.refresh', e, st);
      if (isConnectivityError(e)) {
        ref.read(isOfflineProvider.notifier).setOffline(true);
      }
    }
  }

  void _subscribeToRealtimeOnce() {
    if (_channel != null) return;
    final uid = ref.read(supabaseClientProvider).auth.currentUser?.id;
    if (uid == null) return;

    _channel = ref
        .read(supabaseClientProvider)
        .channel('daily-progress-$uid')
        .onPostgresChanges(
          event: PostgresChangeEvent.all,
          schema: 'public',
          table: 'daily_progress',
          filter: PostgresChangeFilter(type: PostgresChangeFilterType.eq, column: 'user_id', value: uid),
          callback: (payload) => _refreshFromRealtime(),
        )
        .onPostgresChanges(
          event: PostgresChangeEvent.all,
          schema: 'public',
          table: 'user_daily_log',
          filter: PostgresChangeFilter(type: PostgresChangeFilterType.eq, column: 'user_id', value: uid),
          callback: (payload) => _refreshFromRealtime(),
        )
        .subscribe();
  }

  Future<void> _refreshFromRealtime() async {
    if (_refreshing) return;
    _refreshing = true;
    try {
      await refresh();
    } finally {
      _refreshing = false;
    }
  }

  Future<void> completeTimerSession() async {
    if (!await ensureOnline(ref)) return;
    try {
      final awarded = await ref.read(progressRepositoryProvider).completeTimerSession();
      if (!awarded) return;
      await refresh();
      await ref.read(notificationsNotifierProvider.notifier).refresh();
    } catch (e, st) {
      AppLogger.logError('ProgressNotifier.completeTimerSession', e, st);
      if (isConnectivityError(e)) {
        ref.read(isOfflineProvider.notifier).setOffline(true);
      }
    }
  }
}

final progressNotifierProvider = NotifierProvider<ProgressNotifier, AppProgress?>(ProgressNotifier.new);
