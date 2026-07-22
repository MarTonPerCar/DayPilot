import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import '../../core/connectivity/connectivity_service.dart';
import '../../core/connectivity/offline_notifier.dart';
import '../../core/data/friend_stats_broadcast.dart';
import '../../core/data/models/app_ranking_entry.dart';
import '../../core/data/repositories/providers.dart';
import '../../core/logging/app_logger.dart';

class RankingNotifier extends Notifier<List<AppRankingEntry>> {
  RealtimeChannel? _channel;
  bool _refreshing = false;

  @override
  List<AppRankingEntry> build() {
    Future.microtask(refresh);
    final broadcast = ref.read(friendStatsBroadcastProvider);
    ref.onDispose(() {
      _channel?.unsubscribe();
      broadcast.removeListener(_refreshFromRealtime);
    });
    return const [];
  }

  Future<void> refresh() async {
    if (!await ensureOnline(ref)) return;
    try {
      state = await ref.read(rankingRepositoryProvider).getRanking();
      _subscribeToRealtimeOnce();
    } catch (e, st) {
      AppLogger.logError('RankingNotifier.refresh', e, st);
      if (isConnectivityError(e)) {
        ref.read(isOfflineProvider.notifier).setOffline(true);
      }
    }
  }

  void _subscribeToRealtimeOnce() {
    if (_channel != null) return;
    final client = ref.read(supabaseClientProvider);
    final uid = client.auth.currentUser?.id;
    if (uid == null) return;

    _channel = client
        .channel('ranking-$uid')
        .onPostgresChanges(
          event: PostgresChangeEvent.all,
          schema: 'public',
          table: 'friends',
          filter: PostgresChangeFilter(type: PostgresChangeFilterType.eq, column: 'requester_id', value: uid),
          callback: (payload) => _refreshFromRealtime(),
        )
        .onPostgresChanges(
          event: PostgresChangeEvent.all,
          schema: 'public',
          table: 'friends',
          filter: PostgresChangeFilter(type: PostgresChangeFilterType.eq, column: 'receiver_id', value: uid),
          callback: (payload) => _refreshFromRealtime(),
        )
        .subscribe();

    ref.read(friendStatsBroadcastProvider).addListener(_refreshFromRealtime);
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
}

final rankingNotifierProvider = NotifierProvider<RankingNotifier, List<AppRankingEntry>>(RankingNotifier.new);
