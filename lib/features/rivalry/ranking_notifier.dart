import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/data/models/app_ranking_entry.dart';
import '../../core/data/repositories/providers.dart';

class RankingNotifier extends Notifier<List<AppRankingEntry>> {
  @override
  List<AppRankingEntry> build() {
    Future.microtask(refresh);
    return const [];
  }

  Future<void> refresh() async {
    state = await ref.read(rankingRepositoryProvider).getRanking();
  }
}

final rankingNotifierProvider = NotifierProvider<RankingNotifier, List<AppRankingEntry>>(RankingNotifier.new);
