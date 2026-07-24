// The previous version of this test predated Supabase/Riverpod auth wiring —
// it pumped DayPilotApp with no ProviderScope and looked for placeholder text
// ("DayPilot") and a Material icon (Icons.flight_takeoff_rounded) that no
// longer exist anywhere in the app. This version overrides the two
// dependencies AuthGate actually needs (the Supabase auth-state stream and
// the connectivity check) instead of hitting real Supabase/network from a
// widget test, and asserts against the real, stable output for each branch.

import 'package:daypilot/core/auth/auth_gate.dart';
import 'package:daypilot/core/connectivity/connectivity_service.dart';
import 'package:daypilot/main.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

class _FakeConnectivityService implements ConnectivityService {
  @override
  Future<bool> hasInternetConnection() async => true;
}

void main() {
  testWidgets('shows the login screen when there is no active session', (tester) async {
    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          authStateChangesProvider.overrideWith(
            (ref) => Stream.value(const AuthState(AuthChangeEvent.signedOut, null)),
          ),
          connectivityServiceProvider.overrideWithValue(_FakeConnectivityService()),
        ],
        child: const DayPilotApp(),
      ),
    );
    await tester.pump();

    expect(find.image(const AssetImage('assets/images/daypilot_logo.png')), findsOneWidget);
  });

  testWidgets('shows a loading indicator while the auth state is still resolving', (tester) async {
    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          // A stream that never emits keeps AuthGate in its "loading" branch.
          authStateChangesProvider.overrideWith((ref) => const Stream<AuthState>.empty().asBroadcastStream()),
          connectivityServiceProvider.overrideWithValue(_FakeConnectivityService()),
        ],
        child: const DayPilotApp(),
      ),
    );
    await tester.pump();

    expect(find.byType(CircularProgressIndicator), findsOneWidget);
  });
}
