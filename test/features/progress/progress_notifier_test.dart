import 'package:daypilot/core/data/models/app_progress.dart';
import 'package:daypilot/core/connectivity/connectivity_service.dart';
import 'package:daypilot/core/data/repositories/providers.dart';
import 'package:daypilot/features/progress/progress_notifier.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';

import '../../support/fake_connectivity.dart';
import '../../support/mocks.dart';

void main() {
  late MockProgressRepository progressRepo;
  late FakeConnectivityService connectivity;
  late ProviderContainer container;

  const progressA = AppProgress(
    pointsToday: 10,
    pointsFromTasks: 10,
    pointsFromSteps: 0,
    pointsFromHabits: 0,
    pointsFromTimer: 0,
    pointsHistory: [10],
    stepsHistory: [0],
    tasksHistory: [1],
    dayLabels: [24],
  );
  const progressB = AppProgress(
    pointsToday: 30,
    pointsFromTasks: 10,
    pointsFromSteps: 0,
    pointsFromHabits: 0,
    pointsFromTimer: 20,
    pointsHistory: [30],
    stepsHistory: [0],
    tasksHistory: [1],
    dayLabels: [24],
  );

  setUp(() {
    progressRepo = MockProgressRepository();
    connectivity = FakeConnectivityService();
    when(() => progressRepo.getProgress()).thenAnswer((_) async => progressA);
    container = ProviderContainer(
      overrides: [
        progressRepositoryProvider.overrideWithValue(progressRepo),
        connectivityServiceProvider.overrideWithValue(connectivity),
        supabaseClientProvider.overrideWithValue(buildSignedOutSupabaseClient()),
      ],
    );
  });

  tearDown(() => container.dispose());

  test('refresh populates state from the repository', () async {
    await container.read(progressNotifierProvider.notifier).refresh();

    expect(container.read(progressNotifierProvider), progressA);
  });

  test('refresh while offline never calls the repository and leaves state untouched', () async {
    connectivity.online = false;

    await container.read(progressNotifierProvider.notifier).refresh();

    expect(container.read(progressNotifierProvider), isNull);
    verifyNever(() => progressRepo.getProgress());
  });

  test('completeTimerSession only refreshes when the server actually awarded points', () async {
    await container.read(progressNotifierProvider.notifier).refresh();
    expect(container.read(progressNotifierProvider), progressA);

    when(() => progressRepo.completeTimerSession()).thenAnswer((_) async => false);
    when(() => progressRepo.getProgress()).thenAnswer((_) async => progressB);

    await container.read(progressNotifierProvider.notifier).completeTimerSession();

    // Not awarded -> no re-fetch, state should still be the stale progressA.
    expect(container.read(progressNotifierProvider), progressA);
  });
}
