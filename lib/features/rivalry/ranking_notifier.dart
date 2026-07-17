import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import '../../core/data/friend_stats_broadcast.dart';
import '../../core/data/models/app_ranking_entry.dart';
import '../../core/data/repositories/providers.dart';

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
    state = await ref.read(rankingRepositoryProvider).getRanking();
    _subscribeToRealtimeOnce();
  }

  void _subscribeToRealtimeOnce() {
    if (_channel != null) return;
    final client = ref.read(supabaseClientProvider);
    final uid = client.auth.currentUser?.id;
    if (uid == null) return;

    // Quién forma parte del ranking (yo + mis amigos): Postgres Changes,
    // filtrado por usuario, igual que en todo lo demás.
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

    // Puntos de un amigo cambiando (points_log): llega por el mismo canal
    // privado compartido "friend-stats:<uid>" que usa Amigos — no crear
    // aquí un segundo canal con el mismo nombre.
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