import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/data/models/app_progress.dart';
import '../../core/data/repositories/providers.dart';

class ProgressNotifier extends Notifier<AppProgress?> {
  static const _refreshInterval = Duration(minutes: 5);

  @override
  AppProgress? build() {
    Future.microtask(refresh);
    final timer = Timer.periodic(_refreshInterval, (_) => refresh());
    ref.onDispose(timer.cancel);
    return null;
  }

  Future<void> refresh() async {
    state = await ref.read(progressRepositoryProvider).getProgress();
  }
}

final progressNotifierProvider = NotifierProvider<ProgressNotifier, AppProgress?>(ProgressNotifier.new);
