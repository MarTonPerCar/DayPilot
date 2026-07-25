import 'package:daypilot/core/data/repositories/supabase_ranking_repository.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../../support/fake_supabase_http.dart';

void main() {
  late FakeSupabaseHttp http;
  late SupabaseRankingRepository repo;

  setUp(() {
    http = FakeSupabaseHttp();
    repo = SupabaseRankingRepository(http.client);
  });

  tearDown(() => http.dispose());

  test('with no signed-in user returns empty and makes no request', () async {
    final ranking = await repo.getRanking();

    expect(ranking, isEmpty);
    expect(http.requests, isEmpty);
  });

  test('ranks the current user plus friends by points, descending', () async {
    await http.signIn('user-1');
    http.enqueue(jsonRows([
      {'requester_id': 'user-1', 'receiver_id': 'friend-2'},
    ]));
    http.enqueue(jsonRows([
      {'id': 'user-1', 'name': 'Ana', 'username': 'ana', 'photo_url': null, 'points_last_30_days': 100, 'current_streak': 4},
      {
        'id': 'friend-2',
        'name': 'Bob',
        'username': 'bob',
        'photo_url': 'bob.png',
        // A numeric-string value exercises the _asInt string-parsing branch.
        'points_last_30_days': '250',
        'current_streak': 9,
      },
    ]));

    final ranking = await repo.getRanking();

    expect(ranking, hasLength(2));
    expect(ranking[0].userId, 'friend-2');
    expect(ranking[0].points, 250);
    expect(ranking[0].isCurrentUser, isFalse);
    expect(ranking[1].userId, 'user-1');
    expect(ranking[1].points, 100);
    expect(ranking[1].isCurrentUser, isTrue);

    expect(http.requests[1].url.path, '/rest/v1/friends_ranking');
    expect(http.requests[1].url.queryParameters['id'], 'in.("user-1","friend-2")');
  });

  test('with no friends still ranks the current user alone', () async {
    await http.signIn('user-1');
    http.enqueue(emptyRows());
    http.enqueue(jsonRows([
      {'id': 'user-1', 'name': 'Ana', 'username': 'ana', 'photo_url': null, 'points_last_30_days': 100, 'current_streak': 4},
    ]));

    final ranking = await repo.getRanking();

    expect(ranking, hasLength(1));
    expect(ranking.single.isCurrentUser, isTrue);
    expect(http.requests[1].url.queryParameters['id'], 'in.("user-1")');
  });
}
