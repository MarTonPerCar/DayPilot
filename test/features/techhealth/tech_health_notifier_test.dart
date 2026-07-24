import 'package:daypilot/core/data/models/app_tech_restriction.dart';
import 'package:daypilot/core/connectivity/connectivity_service.dart';
import 'package:daypilot/core/data/repositories/providers.dart';
import 'package:daypilot/features/techhealth/tech_health_notifier.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';

import '../../support/fake_connectivity.dart';
import '../../support/mocks.dart';

void main() {
  late MockTechHealthRepository techHealthRepo;
  late FakeConnectivityService connectivity;
  late ProviderContainer container;

  const restrictionA = AppTechRestriction(
    appPackage: 'com.instagram.android',
    appName: 'Instagram',
    limitMinutes: 30,
    isActive: true,
  );
  const restrictionB = AppTechRestriction(
    appPackage: 'com.reddit.frontpage',
    appName: 'Reddit',
    limitMinutes: 15,
    isActive: true,
  );

  setUp(() {
    techHealthRepo = MockTechHealthRepository();
    connectivity = FakeConnectivityService();
    when(() => techHealthRepo.getRestrictions()).thenAnswer((_) async => [restrictionA]);
    when(() => techHealthRepo.getPointEarnedToday()).thenAnswer((_) async => false);
    container = ProviderContainer(
      overrides: [
        techHealthRepositoryProvider.overrideWithValue(techHealthRepo),
        connectivityServiceProvider.overrideWithValue(connectivity),
        supabaseClientProvider.overrideWithValue(buildSignedOutSupabaseClient()),
      ],
    );
  });

  tearDown(() => container.dispose());

  test('refresh populates restrictions and the point-earned flag', () async {
    await container.read(techHealthNotifierProvider.notifier).refresh();

    final state = container.read(techHealthNotifierProvider);
    expect(state.restrictions, [restrictionA]);
    expect(state.pointEarnedToday, isFalse);
  });

  test('toggleRestriction failure leaves state untouched instead of crashing', () async {
    await container.read(techHealthNotifierProvider.notifier).refresh();

    when(() => techHealthRepo.toggleRestriction('com.instagram.android', false))
        .thenThrow(Exception('toggle failed'));

    await container.read(techHealthNotifierProvider.notifier).toggleRestriction('com.instagram.android', false);

    expect(container.read(techHealthNotifierProvider).restrictions, [restrictionA]);
  });

  test('saveRestriction persists then re-fetches the updated restriction list', () async {
    await container.read(techHealthNotifierProvider.notifier).refresh();

    when(() => techHealthRepo.saveRestriction(
          appPackage: 'com.reddit.frontpage',
          appName: 'Reddit',
          limitMinutes: 15,
        )).thenAnswer((_) async {});
    when(() => techHealthRepo.getRestrictions()).thenAnswer((_) async => [restrictionA, restrictionB]);

    await container.read(techHealthNotifierProvider.notifier).saveRestriction(
          appPackage: 'com.reddit.frontpage',
          appName: 'Reddit',
          limitMinutes: 15,
        );

    expect(container.read(techHealthNotifierProvider).restrictions, [restrictionA, restrictionB]);
  });
}
