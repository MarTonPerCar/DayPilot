import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_riverpod/legacy.dart';

import '../data/models/app_user.dart';

const socialCacheTtl = Duration(minutes: 5);
const historyCacheTtl = Duration(hours: 1);

class TtlEntry<T> {
  const TtlEntry({this.value, this.fetchedAt});

  final T? value;
  final DateTime? fetchedAt;

  bool isExpired(Duration ttl) =>
      fetchedAt == null || DateTime.now().difference(fetchedAt!) > ttl;
}

final todayProgressCacheProvider = StateProvider<dynamic>((ref) => null);
final tasksCacheProvider = StateProvider<List<dynamic>?>((ref) => null);
final userProfileCacheProvider = StateProvider<AppUser?>((ref) => null);

final weeklyHistoryCacheProvider =
    StateProvider<TtlEntry<List<dynamic>>>((ref) => const TtlEntry());
final friendsCacheProvider =
    StateProvider<TtlEntry<List<dynamic>>>((ref) => const TtlEntry());
final rankingCacheProvider =
    StateProvider<TtlEntry<List<dynamic>>>((ref) => const TtlEntry());

void clearSessionCache(Ref ref) {
  ref.invalidate(todayProgressCacheProvider);
  ref.invalidate(tasksCacheProvider);
  ref.invalidate(userProfileCacheProvider);
  ref.invalidate(weeklyHistoryCacheProvider);
  ref.invalidate(friendsCacheProvider);
  ref.invalidate(rankingCacheProvider);
}
