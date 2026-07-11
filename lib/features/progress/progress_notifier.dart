import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import '../../core/data/models/app_progress.dart';
import '../../core/data/repositories/providers.dart';
import '../notifications/notifications_notifier.dart';

class ProgressNotifier extends Notifier<AppProgress?> {
  // Realtime (below) keeps this fresh instantly; this poll is only a safety
  // net in case a realtime event is ever missed, e.g. after a dropped
  // websocket reconnects silently.
  static const _refreshInterval = Duration(minutes: 5);

  RealtimeChannel? _channel;

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
    state = await ref.read(progressRepositoryProvider).getProgress();
    _subscribeToRealtimeOnce();
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
          callback: (_) => refresh(),
        )
        .subscribe();
  }

  Future<void> completeTimerSession() async {
    final awarded = await ref.read(progressRepositoryProvider).completeTimerSession();
    if (!awarded) return;
    await refresh();
    await ref.read(notificationsNotifierProvider.notifier).refresh();
  }
}

final progressNotifierProvider = NotifierProvider<ProgressNotifier, AppProgress?>(ProgressNotifier.new);
