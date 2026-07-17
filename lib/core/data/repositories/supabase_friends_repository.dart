import 'package:supabase_flutter/supabase_flutter.dart';

import '../models/app_friend.dart';
import '../reaction_types.dart';
import 'friends_repository.dart';

class SupabaseFriendsRepository implements FriendsRepository {
  SupabaseFriendsRepository(this._client);

  final SupabaseClient _client;

  String? get _userId => _client.auth.currentUser?.id;

  @override
  Future<List<AppFriend>> getFriends() async {
    final uid = _userId;
    if (uid == null) return [];

    final friendRows =
        await _client.from('friends').select().or('requester_id.eq.$uid,receiver_id.eq.$uid');
    if (friendRows.isEmpty) return [];

    final friendRowByUserId = {
      for (final f in friendRows)
        (f['requester_id'] == uid ? f['receiver_id'] : f['requester_id']) as String: f,
    };
    final friendIds = friendRowByUserId.keys.toList();

    final users = await _client
        .from('users')
        .select('id, name, username, photo_url, total_points_historical')
        .inFilter('id', friendIds);

    final streakRows =
        await _client.from('user_streaks').select('user_id, current_streak').inFilter('user_id', friendIds);
    final streakByUserId = {for (final s in streakRows) s['user_id'] as String: s['current_streak'] as int};

    final summaryRows = await _client
        .from('user_weekly_summary')
        .select('id, user_id, total_points, total_steps, total_tasks_completed, best_streak')
        .inFilter('user_id', friendIds)
        .order('week_start', ascending: false);
    final summaryByUserId = <String, Map<String, dynamic>>{};
    for (final s in summaryRows) {
      summaryByUserId.putIfAbsent(s['user_id'] as String, () => s);
    }

    final summaryIds = summaryByUserId.values.map((s) => s['id'] as String).toList();
    var myReactionTypeBySummaryId = <String, String>{};
    if (summaryIds.isNotEmpty) {
      final myReactions = await _client
          .from('reactions')
          .select('weekly_summary_id, type')
          .eq('from_user_id', uid)
          .inFilter('weekly_summary_id', summaryIds);
      myReactionTypeBySummaryId = {
        for (final r in myReactions) r['weekly_summary_id'] as String: r['type'] as String,
      };
    }

    return [
      for (final u in users)
        AppFriend(
          friendRowId: friendRowByUserId[u['id']]!['id'] as String,
          userId: u['id'] as String,
          name: u['name'] as String,
          username: u['username'] as String,
          avatarUrl: u['photo_url'] as String?,
          points: u['total_points_historical'] as int,
          streak: streakByUserId[u['id']] ?? 0,
          weeklyPoints: summaryByUserId[u['id']]?['total_points'] as int?,
          weeklyTasks: summaryByUserId[u['id']]?['total_tasks_completed'] as int?,
          weeklySteps: summaryByUserId[u['id']]?['total_steps'] as int?,
          weeklyStreak: summaryByUserId[u['id']]?['best_streak'] as int?,
          weeklySummaryId: summaryByUserId[u['id']]?['id'] as String?,
          reactionSelected: switch (summaryByUserId[u['id']]?['id']) {
            final summaryId? => reactionEmojiByType[myReactionTypeBySummaryId[summaryId]],
            null => null,
          },
        ),
    ];
  }

  @override
  Future<List<AppFriendRequest>> getIncomingRequests() async {
    final uid = _userId;
    if (uid == null) return [];

    final rows = await _client.from('friend_requests').select('id, from_user_id').eq('to_user_id', uid);
    if (rows.isEmpty) return [];

    final fromIds = rows.map((r) => r['from_user_id'] as String).toList();
    final users = await _client.from('users').select('id, name, username, photo_url').inFilter('id', fromIds);
    final userById = {for (final u in users) u['id'] as String: u};

    return [
      for (final r in rows)
        if (userById[r['from_user_id']] case final u?)
          AppFriendRequest(
            requestId: r['id'] as String,
            fromUserId: r['from_user_id'] as String,
            name: u['name'] as String,
            username: u['username'] as String,
            avatarUrl: u['photo_url'] as String?,
          ),
    ];
  }

  @override
  Future<List<AppUserSearchResult>> searchUsers(String query) async {
    final uid = _userId;
    final term = query.trim().replaceAll(RegExp('[,()]'), '');
    if (uid == null || term.isEmpty) return [];

    final rows = await _client
        .from('users')
        .select('id, name, username, photo_url')
        .or('name.ilike.%$term%,username.ilike.%$term%')
        .neq('id', uid)
        .limit(20);
    if (rows.isEmpty) return [];

    final resultIds = rows.map((r) => r['id'] as String).toList();

    final friendRows =
        await _client.from('friends').select('requester_id, receiver_id').or('requester_id.eq.$uid,receiver_id.eq.$uid');
    final friendIds = {
      for (final f in friendRows)
        (f['requester_id'] == uid ? f['receiver_id'] : f['requester_id']) as String,
    };

    final pendingRows = await _client
        .from('friend_requests')
        .select('to_user_id')
        .eq('from_user_id', uid)
        .inFilter('to_user_id', resultIds);
    final pendingIds = {for (final p in pendingRows) p['to_user_id'] as String};

    return [
      for (final r in rows)
        AppUserSearchResult(
          userId: r['id'] as String,
          name: r['name'] as String,
          username: r['username'] as String,
          avatarUrl: r['photo_url'] as String?,
          isFriend: friendIds.contains(r['id']),
          isPending: pendingIds.contains(r['id']),
        ),
    ];
  }

  @override
  Future<void> sendFriendRequest(String toUserId) async {
    final uid = _userId;
    if (uid == null) return;
    await _client.from('friend_requests').insert({'from_user_id': uid, 'to_user_id': toUserId});
  }

  @override
  Future<void> acceptRequest({required String requestId, required String fromUserId}) async {
    final uid = _userId;
    if (uid == null) return;
    await _client.from('friends').insert({'requester_id': fromUserId, 'receiver_id': uid});
    await _client.from('friend_requests').delete().eq('id', requestId);
  }

  @override
  Future<void> declineRequest(String requestId) async {
    await _client.from('friend_requests').delete().eq('id', requestId);
  }

  @override
  Future<void> removeFriend(String friendRowId) async {
    await _client.from('friends').delete().eq('id', friendRowId);
  }

  @override
  Future<void> sendReaction({required String toUserId, required String weeklySummaryId, required String emoji}) async {
    final uid = _userId;
    final type = reactionTypeForEmoji(emoji);
    if (uid == null || type == null) return;
    await _client.from('reactions').upsert(
      {
        'from_user_id': uid,
        'to_user_id': toUserId,
        'weekly_summary_id': weeklySummaryId,
        'type': type,
      },
      onConflict: 'from_user_id, weekly_summary_id',
    );
  }
}
