import 'package:daypilot/core/connectivity/connectivity_service.dart';
import 'package:daypilot/core/data/models/app_profile_stats.dart';
import 'package:daypilot/core/data/repositories/providers.dart';
import 'package:daypilot/screens/profile/edit_profile_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';

import '../../support/fake_connectivity.dart';
import '../../support/mocks.dart';
import '../../support/pump_app.dart';

void main() {
  late MockProfileRepository profileRepo;

  final stats = AppProfileStats(
    name: 'Ana',
    username: 'ana',
    email: 'ana@daypilot.test',
    createdAt: DateTime(2026, 1, 1),
    region: 'Europe/Madrid',
    level: 3,
    currentXp: 40,
    xpToNextLevel: 100,
    totalPoints: 340,
    streak: 5,
    bestStreak: 10,
  );

  setUp(() {
    profileRepo = MockProfileRepository();
    when(() => profileRepo.getProfileStats()).thenAnswer((_) async => stats);
    when(
      () => profileRepo.updateProfile(
        name: any(named: 'name'),
        username: any(named: 'username'),
        region: any(named: 'region'),
      ),
    ).thenAnswer((_) async {});
  });

  // The screen's ListView is taller than flutter_test's default 800x600
  // surface, which would leave the Save button unbuilt (off the sliver's
  // cache extent) rather than just scrolled off-screen — resize instead of
  // scrolling to it.
  void useTallSurface(WidgetTester tester) {
    tester.view.physicalSize = const Size(800, 2000);
    tester.view.devicePixelRatio = 1.0;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
  }

  Widget scoped(Widget child) => ProviderScope(
    overrides: [
      profileRepositoryProvider.overrideWithValue(profileRepo),
      connectivityServiceProvider.overrideWithValue(FakeConnectivityService()),
      supabaseClientProvider.overrideWithValue(buildSignedOutSupabaseClient()),
    ],
    child: child,
  );

  testWidgets('pre-fills the name and username fields from the passed-in stats', (tester) async {
    await pumpApp(tester, scoped(EditProfileScreen(stats: stats)));
    await tester.pump();

    expect(find.text('Ana'), findsOneWidget);
    expect(find.text('ana'), findsOneWidget);
  });

  testWidgets('saving with a non-empty name and username calls updateProfile', (tester) async {
    useTallSurface(tester);
    await pumpApp(tester, scoped(EditProfileScreen(stats: stats)));
    await tester.pump();

    await tester.tap(find.byType(FilledButton));
    await tester.pump();

    verify(
      () => profileRepo.updateProfile(name: 'Ana', username: 'ana', region: 'Europe/Madrid'),
    ).called(1);
  });

  testWidgets('saving with an empty name does not call updateProfile', (tester) async {
    useTallSurface(tester);
    await pumpApp(tester, scoped(EditProfileScreen(stats: stats)));
    await tester.pump();

    await tester.enterText(find.byType(TextFormField).first, '');
    await tester.tap(find.byType(FilledButton));
    await tester.pump();

    verifyNever(
      () => profileRepo.updateProfile(
        name: any(named: 'name'),
        username: any(named: 'username'),
        region: any(named: 'region'),
      ),
    );
  });
}
