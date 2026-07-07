import '../models/app_ranking_entry.dart';

abstract class RankingRepository {
  /// Me + my friends, sorted by points earned in the last 30 days.
  Future<List<AppRankingEntry>> getRanking();
}
