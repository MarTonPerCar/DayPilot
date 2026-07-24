import 'package:daypilot/core/data/models/app_steps.dart';
import 'package:daypilot/core/connectivity/connectivity_service.dart';
import 'package:daypilot/core/data/repositories/providers.dart';
import 'package:daypilot/features/steps/steps_notifier.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';

import '../../support/fake_connectivity.dart';
import '../../support/mocks.dart';

void main() {
  late MockStepsRepository stepsRepo;
  late FakeConnectivityService connectivity;
  late ProviderContainer container;

  const initialSteps = AppSteps(steps: 1000, goal: 10000, pointsEarnedToday: 10);

  setUp(() {
    stepsRepo = MockStepsRepository();
    connectivity = FakeConnectivityService();
    when(() => stepsRepo.getSteps()).thenAnswer((_) async => initialSteps);
    container = ProviderContainer(
      overrides: [
        stepsRepositoryProvider.overrideWithValue(stepsRepo),
        connectivityServiceProvider.overrideWithValue(connectivity),
        supabaseClientProvider.overrideWithValue(buildSignedOutSupabaseClient()),
      ],
    );
  });

  tearDown(() => container.dispose());

  test('refresh populates state from the repository', () async {
    await container.read(stepsNotifierProvider.notifier).refresh();

    expect(container.read(stepsNotifierProvider), initialSteps);
  });

  test('refresh while offline never calls the repository and leaves state untouched', () async {
    connectivity.online = false;

    await container.read(stepsNotifierProvider.notifier).refresh();

    expect(container.read(stepsNotifierProvider), isNull);
    verifyNever(() => stepsRepo.getSteps());
  });

  test('setGoal saves the new goal and re-fetches steps afterward', () async {
    await container.read(stepsNotifierProvider.notifier).refresh();
    expect(container.read(stepsNotifierProvider), initialSteps);

    const updatedSteps = AppSteps(steps: 1000, goal: 12000, pointsEarnedToday: 10);
    when(() => stepsRepo.setGoal(12000)).thenAnswer((_) async {});
    when(() => stepsRepo.getSteps()).thenAnswer((_) async => updatedSteps);

    await container.read(stepsNotifierProvider.notifier).setGoal(12000);

    verify(() => stepsRepo.setGoal(12000)).called(1);
    expect(container.read(stepsNotifierProvider), updatedSteps);
  });
}
