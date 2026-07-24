import 'package:daypilot/core/data/models/app_profile_stats.dart';
import 'package:daypilot/core/data/models/app_weekly_summary.dart';
import 'package:daypilot/core/connectivity/connectivity_service.dart';
import 'package:daypilot/core/data/repositories/providers.dart';
import 'package:daypilot/features/profile/profile_notifier.dart';
import 'package:daypilot/features/profile/weekly_summary_notifier.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';

import '../../support/fake_connectivity.dart';
import '../../support/mocks.dart';

void main() {
  late MockProfileRepository profileRepo;
  late FakeConnectivityService connectivity;
  late ProviderContainer container;

  final stats = AppProfileStats(
    name: 'Ana',
    username: 'ana',
    email: 'ana@daypilot.test',
    createdAt: DateTime(2026, 1, 1),
    region: 'ES',
    level: 3,
    currentXp: 40,
    xpToNextLevel: 100,
    totalPoints: 340,
    streak: 5,
    bestStreak: 10,
  );

  const weeklySummary = AppWeeklySummary(
    totalPoints: 200,
    totalSteps: 30000,
    tasksCompleted: 12,
    bestStreak: 5,
    reactions: [],
  );

  setUp(() {
    profileRepo = MockProfileRepository();
    connectivity = FakeConnectivityService();
    container = ProviderContainer(
      overrides: [
        profileRepositoryProvider.overrideWithValue(profileRepo),
        connectivityServiceProvider.overrideWithValue(connectivity),
        supabaseClientProvider.overrideWithValue(buildSignedOutSupabaseClient()),
      ],
    );
  });

  tearDown(() => container.dispose());

  test('ProfileStatsNotifier.refresh populates state from the repository', () async {
    when(() => profileRepo.getProfileStats()).thenAnswer((_) async => stats);

    await container.read(profileStatsNotifierProvider.notifier).refresh();

    expect(container.read(profileStatsNotifierProvider), stats);
  });

  test('ProfileStatsNotifier.refresh while offline never calls the repository', () async {
    connectivity.online = false;

    await container.read(profileStatsNotifierProvider.notifier).refresh();

    expect(container.read(profileStatsNotifierProvider), isNull);
    verifyNever(() => profileRepo.getProfileStats());
  });

  test('WeeklySummaryNotifier.refresh populates state from the repository', () async {
    when(() => profileRepo.getWeeklySummary()).thenAnswer((_) async => weeklySummary);

    await container.read(weeklySummaryNotifierProvider.notifier).refresh();

    expect(container.read(weeklySummaryNotifierProvider), weeklySummary);
  });
}
