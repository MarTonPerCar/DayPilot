import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/data/models/app_tech_restriction.dart';
import '../../core/data/repositories/providers.dart';
import '../../core/logging/app_logger.dart';

class TechHealthState {
  const TechHealthState({this.restrictions = const [], this.pointEarnedToday = false});

  final List<AppTechRestriction> restrictions;
  final bool pointEarnedToday;
}

class TechHealthNotifier extends Notifier<TechHealthState> {
  @override
  TechHealthState build() {
    Future.microtask(refresh);
    return const TechHealthState();
  }

  Future<void> refresh() async {
    try {
      final repo = ref.read(techHealthRepositoryProvider);
      final restrictions = await repo.getRestrictions();
      final pointEarnedToday = await repo.getPointEarnedToday();
      state = TechHealthState(restrictions: restrictions, pointEarnedToday: pointEarnedToday);
    } catch (e, st) {
      AppLogger.logError('TechHealthNotifier.refresh', e, st);
    }
  }

  Future<void> saveRestriction({
    required String appPackage,
    required String appName,
    required int limitMinutes,
  }) async {
    await ref.read(techHealthRepositoryProvider).saveRestriction(
          appPackage: appPackage,
          appName: appName,
          limitMinutes: limitMinutes,
        );
    await refresh();
  }

  Future<void> toggleRestriction(String appPackage, bool isActive) async {
    await ref.read(techHealthRepositoryProvider).toggleRestriction(appPackage, isActive);
    await refresh();
  }

  Future<void> deleteRestriction(String appPackage) async {
    await ref.read(techHealthRepositoryProvider).deleteRestriction(appPackage);
    await refresh();
  }
}

final techHealthNotifierProvider = NotifierProvider<TechHealthNotifier, TechHealthState>(TechHealthNotifier.new);
