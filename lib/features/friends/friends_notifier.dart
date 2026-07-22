import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import '../../core/connectivity/connectivity_service.dart';
import '../../core/connectivity/offline_notifier.dart';
import '../../core/data/friend_stats_broadcast.dart';
import '../../core/data/models/app_friend.dart';
import '../../core/data/repositories/providers.dart';
import '../../core/logging/app_logger.dart';
import '../rivalry/ranking_notifier.dart';

class FriendsState {
  const FriendsState({this.friends = const [], this.requests = const []});

  final List<AppFriend> friends;
  final List<AppFriendRequest> requests;
}

class FriendsNotifier extends Notifier<FriendsState> {
  // Realtime is the primary update path, but its first subscribe() after app start isn't
  // instant — anything that lands before the handshake completes is silently missed (Postgres
  // changes aren't replayed). This periodic refresh self-heals a missed event within a few
  // minutes instead of leaving it stuck until the user happens to reopen this screen.
  static const _refreshInterval = Duration(minutes: 5);
  RealtimeChannel? _channel;
  bool _refreshing = false;

  @override
  FriendsState build() {
    Future.microtask(refresh);
    final timer = Timer.periodic(_refreshInterval, (_) => refresh());
    final broadcast = ref.read(friendStatsBroadcastProvider);
    ref.onDispose(() {
      timer.cancel();
      _channel?.unsubscribe();
      broadcast.removeListener(_refreshFromRealtime);
    });
    return const FriendsState();
  }

  Future<void> refresh() async {
    if (!await ensureOnline(ref)) return;
    try {
      final repo = ref.read(friendsRepositoryProvider);
      final friends = await repo.getFriends();
      final requests = await repo.getIncomingRequests();
      state = FriendsState(friends: friends, requests: requests);
      _subscribeToRealtimeOnce();
    } catch (e, st) {
      AppLogger.logError('FriendsNotifier.refresh', e, st);
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
        .channel('friends-$uid')
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
        .onPostgresChanges(
          event: PostgresChangeEvent.all,
          schema: 'public',
          table: 'friend_requests',
          filter: PostgresChangeFilter(type: PostgresChangeFilterType.eq, column: 'to_user_id', value: uid),
          callback: (payload) => _refreshFromRealtime(),
        )
        .onPostgresChanges(
          event: PostgresChangeEvent.all,
          schema: 'public',
          table: 'reactions',
          filter: PostgresChangeFilter(type: PostgresChangeFilterType.eq, column: 'from_user_id', value: uid),
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

  Future<void> acceptRequest(AppFriendRequest request) async {
    if (!await ensureOnline(ref)) return;
    try {
      await ref.read(friendsRepositoryProvider).acceptRequest(
            requestId: request.requestId,
            fromUserId: request.fromUserId,
          );
      await refresh();
      await ref.read(rankingNotifierProvider.notifier).refresh();
    } catch (e, st) {
      AppLogger.logError('FriendsNotifier.acceptRequest', e, st);
      if (isConnectivityError(e)) {
        ref.read(isOfflineProvider.notifier).setOffline(true);
      }
    }
  }

  Future<void> declineRequest(String requestId) async {
    if (!await ensureOnline(ref)) return;
    try {
      await ref.read(friendsRepositoryProvider).declineRequest(requestId);
      await refresh();
    } catch (e, st) {
      AppLogger.logError('FriendsNotifier.declineRequest', e, st);
      if (isConnectivityError(e)) {
        ref.read(isOfflineProvider.notifier).setOffline(true);
      }
    }
  }

  Future<void> removeFriend(String friendRowId) async {
    if (!await ensureOnline(ref)) return;
    try {
      await ref.read(friendsRepositoryProvider).removeFriend(friendRowId);
      await refresh();
      await ref.read(rankingNotifierProvider.notifier).refresh();
    } catch (e, st) {
      AppLogger.logError('FriendsNotifier.removeFriend', e, st);
      if (isConnectivityError(e)) {
        ref.read(isOfflineProvider.notifier).setOffline(true);
      }
    }
  }

  Future<void> react(AppFriend friend, String emoji) async {
    final summaryId = friend.weeklySummaryId;
    if (summaryId == null) return;
    if (!await ensureOnline(ref)) return;
    try {
      await ref.read(friendsRepositoryProvider).sendReaction(
            toUserId: friend.userId,
            weeklySummaryId: summaryId,
            emoji: emoji,
          );
      await refresh();
    } catch (e, st) {
      AppLogger.logError('FriendsNotifier.react', e, st);
      if (isConnectivityError(e)) {
        ref.read(isOfflineProvider.notifier).setOffline(true);
      }
    }
  }
}

final friendsNotifierProvider = NotifierProvider<FriendsNotifier, FriendsState>(FriendsNotifier.new);
