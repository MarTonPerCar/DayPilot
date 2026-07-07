import '../models/app_friend.dart';

abstract class FriendsRepository {
  Future<List<AppFriend>> getFriends();

  Future<List<AppFriendRequest>> getIncomingRequests();

  Future<List<AppUserSearchResult>> searchUsers(String query);

  Future<void> sendFriendRequest(String toUserId);

  Future<void> acceptRequest({required String requestId, required String fromUserId});

  Future<void> declineRequest(String requestId);

  Future<void> removeFriend(String friendRowId);

  Future<void> sendReaction({required String toUserId, required String weeklySummaryId, required String emoji});
}
