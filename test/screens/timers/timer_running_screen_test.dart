import 'dart:async';

import 'package:daypilot/core/connectivity/connectivity_service.dart';
import 'package:daypilot/core/data/models/app_progress.dart';
import 'package:daypilot/core/data/repositories/providers.dart';
import 'package:daypilot/screens/timers/timer_running_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';

import '../../support/fake_connectivity.dart';
import '../../support/mocks.dart';
import '../../support/pump_app.dart';

void main() {
  late MockProgressRepository progressRepo;

  setUp(() {
    progressRepo = MockProgressRepository();
    // Deliberately unresolved-forever: no test here needs the real progress
    // payload, and each test's own pump()s never wait on it.
    when(() => progressRepo.getProgress()).thenAnswer((_) => Completer<AppProgress>().future);
  });

  Widget scoped(Widget child) => ProviderScope(
    overrides: [
      progressRepositoryProvider.overrideWithValue(progressRepo),
      connectivityServiceProvider.overrideWithValue(FakeConnectivityService()),
      supabaseClientProvider.overrideWithValue(buildSignedOutSupabaseClient()),
    ],
    child: child,
  );

  testWidgets('shows the configured work duration before starting', (tester) async {
    await pumpApp(
      tester,
      scoped(const TimerRunningScreen(title: 'Training', color: Colors.green, workMinutes: 5)),
    );
    await tester.pump();

    expect(find.text('05:00'), findsOneWidget);
  });

  testWidgets('counts down one second at a time once started', (tester) async {
    await pumpApp(
      tester,
      scoped(const TimerRunningScreen(title: 'Training', color: Colors.green, workMinutes: 5)),
    );
    await tester.pump();

    await tester.tap(find.byIcon(Icons.play_arrow_rounded));
    await tester.pump();
    await tester.pump(const Duration(seconds: 3));

    expect(find.text('04:57'), findsOneWidget);
  });

  testWidgets('reset restores the original duration and stops the ticker', (tester) async {
    await pumpApp(
      tester,
      scoped(const TimerRunningScreen(title: 'Training', color: Colors.green, workMinutes: 5)),
    );
    await tester.pump();

    await tester.tap(find.byIcon(Icons.play_arrow_rounded));
    await tester.pump();
    await tester.pump(const Duration(seconds: 3));
    expect(find.text('04:57'), findsOneWidget);

    await tester.tap(find.byIcon(Icons.refresh_rounded));
    await tester.pump();

    expect(find.text('05:00'), findsOneWidget);
    expect(find.byIcon(Icons.play_arrow_rounded), findsOneWidget);
  });
}
