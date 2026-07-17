import 'package:supabase_flutter/supabase_flutter.dart';

import '../models/app_ranking_entry.dart';
import 'ranking_repository.dart';

int _asInt(dynamic value) => value is num ? value.toInt() : int.parse(value.toString());

class SupabaseRankingRepository implements RankingRepository {
  SupabaseRankingRepository(this._client);

  final SupabaseClient _client;

  String? get _userId => _client.auth.currentUser?.id;

  @override
  Future<List<AppRankingEntry>> getRanking() async {
    final uid = _userId;
    if (uid == null) return [];

    final friendRows =
        await _client.from('friends').select('requester_id, receiver_id').or('requester_id.eq.$uid,receiver_id.eq.$uid');
    final ids = {
      uid,
      for (final f in friendRows)
        (f['requester_id'] == uid ? f['receiver_id'] : f['requester_id']) as String,
    };

    final rows = await _client.from('friends_ranking').select().inFilter('id', ids.toList());

    final entries = [
      for (final r in rows)
        AppRankingEntry(
          userId: r['id'] as String,
          name: r['name'] as String,
          username: r['username'] as String,
          avatarUrl: r['photo_url'] as String?,
          points: _asInt(r['points_last_30_days']),
          streak: r['current_streak'] as int,
          isCurrentUser: r['id'] == uid,
        ),
    ]..sort((a, b) => b.points.compareTo(a.points));

    return entries;
  }
}
