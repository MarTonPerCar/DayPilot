import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/data/models/app_steps.dart';
import '../../core/data/repositories/providers.dart';

class StepsNotifier extends Notifier<AppSteps?> {
  static const _refreshInterval = Duration(minutes: 5);

  @override
  AppSteps? build() {
    Future.microtask(refresh);
    final timer = Timer.periodic(_refreshInterval, (_) => refresh());
    ref.onDispose(timer.cancel);
    return null;
  }

  Future<void> refresh() async {
    state = await ref.read(stepsRepositoryProvider).getSteps();
  }

  Future<void> setGoal(int newGoal) async {
    await ref.read(stepsRepositoryProvider).setGoal(newGoal);
    await refresh();
  }
}

final stepsNotifierProvider = NotifierProvider<StepsNotifier, AppSteps?>(StepsNotifier.new);
