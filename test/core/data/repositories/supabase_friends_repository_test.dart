import 'package:daypilot/core/data/repositories/supabase_friends_repository.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../../support/fake_supabase_http.dart';

void main() {
  late FakeSupabaseHttp http;
  late SupabaseFriendsRepository repo;

  setUp(() {
    http = FakeSupabaseHttp();
    repo = SupabaseFriendsRepository(http.client);
  });

  tearDown(() => http.dispose());

  group('getFriends', () {
    test('with no signed-in user returns empty and makes no request', () async {
      final friends = await repo.getFriends();

      expect(friends, isEmpty);
      expect(http.requests, isEmpty);
    });

    test('with no friend rows short-circuits after the first request', () async {
      await http.signIn('user-1');
      http.enqueue(emptyRows());

      final friends = await repo.getFriends();

      expect(friends, isEmpty);
      expect(http.requests, hasLength(1));
      expect(http.requests[0].url.path, '/rest/v1/friends');
      expect(http.requests[0].url.queryParameters['or'], '(requester_id.eq.user-1,receiver_id.eq.user-1)');
    });

    test('maps a friend with no weekly summary yet, skipping the reactions request', () async {
      await http.signIn('user-1');
      http.enqueue(jsonRows([
        {'id': 'friendrow-1', 'requester_id': 'user-1', 'receiver_id': 'friend-1'},
      ]));
      http.enqueue(jsonRows([
        {'id': 'friend-1', 'name': 'Bob', 'username': 'bob', 'photo_url': null, 'total_points_historical': 500},
      ]));
      http.enqueue(jsonRows([
        {'user_id': 'friend-1', 'current_streak': 7},
      ]));
      http.enqueue(emptyRows());

      final friends = await repo.getFriends();

      expect(friends, hasLength(1));
      final friend = friends.single;
      expect(friend.friendRowId, 'friendrow-1');
      expect(friend.userId, 'friend-1');
      expect(friend.name, 'Bob');
      expect(friend.points, 500);
      expect(friend.streak, 7);
      expect(friend.weeklyPoints, isNull);
      expect(friend.weeklySummaryId, isNull);
      expect(friend.reactionSelected, isNull);

      // No weekly summary rows -> summaryIds is empty -> the reactions call is skipped.
      expect(http.requests, hasLength(4));
    });

    test('maps a friend with a weekly summary and a reaction already sent', () async {
      await http.signIn('user-1');
      http.enqueue(jsonRows([
        {'id': 'friendrow-1', 'requester_id': 'friend-1', 'receiver_id': 'user-1'},
      ]));
      http.enqueue(jsonRows([
        {'id': 'friend-1', 'name': 'Bob', 'username': 'bob', 'photo_url': 'bob.png', 'total_points_historical': 500},
      ]));
      http.enqueue(jsonRows([
        {'user_id': 'friend-1', 'current_streak': 7},
      ]));
      http.enqueue(jsonRows([
        {
          'id': 'summary-1',
          'user_id': 'friend-1',
          'total_points': 120,
          'total_steps': 8000,
          'total_tasks_completed': 5,
          'best_streak': 3,
        },
      ]));
      http.enqueue(jsonRows([
        {'weekly_summary_id': 'summary-1', 'type': 'fire'},
      ]));

      final friends = await repo.getFriends();

      final friend = friends.single;
      expect(friend.weeklyPoints, 120);
      expect(friend.weeklySteps, 8000);
      expect(friend.weeklyTasks, 5);
      expect(friend.weeklyStreak, 3);
      expect(friend.weeklySummaryId, 'summary-1');
      expect(friend.reactionSelected, '🔥');

      expect(http.requests, hasLength(5));
      expect(http.requests[4].url.path, '/rest/v1/reactions');
      expect(http.requests[4].url.queryParameters['from_user_id'], 'eq.user-1');
    });
  });

  group('getIncomingRequests', () {
    test('with no signed-in user returns empty and makes no request', () async {
      final requests = await repo.getIncomingRequests();

      expect(requests, isEmpty);
      expect(http.requests, isEmpty);
    });

    test('maps incoming requests joined with the sender profile', () async {
      await http.signIn('user-1');
      http.enqueue(jsonRows([
        {'id': 'req-1', 'from_user_id': 'friend-2'},
      ]));
      http.enqueue(jsonRows([
        {'id': 'friend-2', 'name': 'Carla', 'username': 'carla', 'photo_url': null},
      ]));

      final requests = await repo.getIncomingRequests();

      expect(requests, hasLength(1));
      expect(requests.single.requestId, 'req-1');
      expect(requests.single.fromUserId, 'friend-2');
      expect(requests.single.name, 'Carla');
    });
  });

  group('searchUsers', () {
    test('with a blank query returns empty and makes no request', () async {
      await http.signIn('user-1');

      final results = await repo.searchUsers('   ');

      expect(results, isEmpty);
      expect(http.requests, isEmpty);
    });

    test('flags existing friends and pending requests among the matches', () async {
      await http.signIn('user-1');
      http.enqueue(jsonRows([
        {'id': 'match-1', 'name': 'Dana', 'username': 'dana', 'photo_url': null},
        {'id': 'match-2', 'name': 'Eve', 'username': 'eve', 'photo_url': null},
      ]));
      http.enqueue(jsonRows([
        {'requester_id': 'user-1', 'receiver_id': 'match-1'},
      ]));
      http.enqueue(jsonRows([
        {'to_user_id': 'match-2'},
      ]));

      final results = await repo.searchUsers('da');

      expect(results, hasLength(2));
      expect(results[0].isFriend, isTrue);
      expect(results[0].isPending, isFalse);
      expect(results[1].isFriend, isFalse);
      expect(results[1].isPending, isTrue);
      expect(http.requests[0].url.queryParameters['or'], '(name.ilike.%da%,username.ilike.%da%)');
    });
  });

  test('sendFriendRequest inserts a row for the target user', () async {
    await http.signIn('user-1');
    http.enqueue(noContent());

    await repo.sendFriendRequest('friend-9');

    expect(http.requests, hasLength(1));
    expect(http.requests[0].method, 'POST');
    expect(http.requests[0].url.path, '/rest/v1/friend_requests');
    expect(http.requests[0].body, contains('"from_user_id":"user-1"'));
    expect(http.requests[0].body, contains('"to_user_id":"friend-9"'));
  });

  test('acceptRequest inserts the friendship and deletes the request', () async {
    await http.signIn('user-1');
    http.enqueue(noContent());
    http.enqueue(noContent());

    await repo.acceptRequest(requestId: 'req-1', fromUserId: 'friend-9');

    expect(http.requests, hasLength(2));
    expect(http.requests[0].url.path, '/rest/v1/friends');
    expect(http.requests[0].body, contains('"requester_id":"friend-9"'));
    expect(http.requests[1].method, 'DELETE');
    expect(http.requests[1].url.path, '/rest/v1/friend_requests');
    expect(http.requests[1].url.queryParameters['id'], 'eq.req-1');
  });

  test('declineRequest deletes the request row', () async {
    http.enqueue(noContent());

    await repo.declineRequest('req-1');

    expect(http.requests[0].method, 'DELETE');
    expect(http.requests[0].url.queryParameters['id'], 'eq.req-1');
  });

  test('removeFriend deletes the friendship row', () async {
    http.enqueue(noContent());

    await repo.removeFriend('friendrow-1');

    expect(http.requests[0].method, 'DELETE');
    expect(http.requests[0].url.path, '/rest/v1/friends');
    expect(http.requests[0].url.queryParameters['id'], 'eq.friendrow-1');
  });

  test('sendReaction upserts on the from/summary composite key', () async {
    await http.signIn('user-1');
    http.enqueue(noContent());

    await repo.sendReaction(toUserId: 'friend-9', weeklySummaryId: 'summary-1', emoji: '🔥');

    expect(http.requests[0].url.path, '/rest/v1/reactions');
    expect(http.requests[0].headers['Prefer'], contains('resolution=merge-duplicates'));
    expect(http.requests[0].body, contains('"type":"fire"'));
  });

  test('sendReaction with an unknown emoji makes no request', () async {
    await http.signIn('user-1');

    await repo.sendReaction(toUserId: 'friend-9', weeklySummaryId: 'summary-1', emoji: '🦄');

    expect(http.requests, isEmpty);
  });
}
