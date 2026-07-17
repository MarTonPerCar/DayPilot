import '../models/app_ranking_entry.dart';

abstract class RankingRepository {

  Future<List<AppRankingEntry>> getRanking();
}
