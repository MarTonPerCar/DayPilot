import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/data/models/app_friend.dart';
import '../../core/data/repositories/providers.dart';
import '../rivalry/ranking_notifier.dart';

class FriendsState {
  const FriendsState({this.friends = const [], this.requests = const []});

  final List<AppFriend> friends;
  final List<AppFriendRequest> requests;
}

class FriendsNotifier extends Notifier<FriendsState> {
  @override
  FriendsState build() {
    Future.microtask(refresh);
    return const FriendsState();
  }

  Future<void> refresh() async {
    final repo = ref.read(friendsRepositoryProvider);
    final friends = await repo.getFriends();
    final requests = await repo.getIncomingRequests();
    state = FriendsState(friends: friends, requests: requests);
  }

  Future<void> acceptRequest(AppFriendRequest request) async {
    await ref.read(friendsRepositoryProvider).acceptRequest(
          requestId: request.requestId,
          fromUserId: request.fromUserId,
        );
    await refresh();
    // Ranking is me + my friends — changing who's a friend makes it stale.
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
