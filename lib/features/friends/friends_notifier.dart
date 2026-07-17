import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import '../../core/data/friend_stats_broadcast.dart';
import '../../core/data/models/app_friend.dart';
import '../../core/data/repositories/providers.dart';
import '../rivalry/ranking_notifier.dart';

class FriendsState {
  const FriendsState({this.friends = const [], this.requests = const []});

  final List<AppFriend> friends;
  final List<AppFriendRequest> requests;
}

class FriendsNotifier extends Notifier<FriendsState> {
  RealtimeChannel? _channel;
  bool _refreshing = false;

  @override
  FriendsState build() {
    Future.microtask(refresh);
    final broadcast = ref.read(friendStatsBroadcastProvider);
    ref.onDispose(() {
      _channel?.unsubscribe();
      broadcast.removeListener(_refreshFromRealtime);
    });
    return const FriendsState();
  }

  Future<void> refresh() async {
    final repo = ref.read(friendsRepositoryProvider);
    final friends = await repo.getFriends();
    final requests = await repo.getIncomingRequests();
    state = FriendsState(friends: friends, requests: requests);
    _subscribeToRealtimeOnce();
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
    await ref.read(friendsRepositoryProvider).acceptRequest(
          requestId: request.requestId,
          fromUserId: request.fromUserId,
        );
    await refresh();
    await ref.read(rankingNotifierProvider.notifier).refresh();
  }

  Future<void> declineRequest(String requestId) async {
    await ref.read(friendsRepositoryProvider).declineRequest(requestId);
    await refresh();
  }

  Future<void> removeFriend(String friendRowId) async {
    await ref.read(friendsRepositoryProvider).removeFriend(friendRowId);
    await refresh();
    await ref.read(rankingNotifierProvider.notifier).refresh();
  }

  Future<void> react(AppFriend friend, String emoji) async {
    final summaryId = friend.weeklySummaryId;
    if (summaryId == null) return;
    await ref.read(friendsRepositoryProvider).sendReaction(
          toUserId: friend.userId,
          weeklySummaryId: summaryId,
          emoji: emoji,
        );
    await refresh();
  }
}

final friendsNotifierProvider = NotifierProvider<FriendsNotifier, FriendsState>(FriendsNotifier.new);