import 'package:daypilot/core/data/models/app_user.dart';
import 'package:daypilot/core/data/models/auth_exceptions.dart';
import 'package:daypilot/core/data/repositories/providers.dart';
import 'package:daypilot/core/connectivity/connectivity_service.dart';
import 'package:daypilot/features/auth/auth_error.dart';
import 'package:daypilot/features/auth/auth_notifier.dart';
import 'package:daypilot/features/auth/auth_session.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';

import '../../support/fake_connectivity.dart';
import '../../support/mocks.dart';

void main() {
  late MockAuthRepository authRepo;
  late FakeConnectivityService connectivity;
  late ProviderContainer container;

  setUp(() {
    authRepo = MockAuthRepository();
    connectivity = FakeConnectivityService();
    container = ProviderContainer(
      overrides: [
        authRepositoryProvider.overrideWithValue(authRepo),
        connectivityServiceProvider.overrideWithValue(connectivity),
        supabaseClientProvider.overrideWithValue(buildSignedOutSupabaseClient()),
      ],
    );
  });

  tearDown(() => container.dispose());

  const user = AppUser(id: 'u1', email: 'ana@daypilot.test', name: 'Ana', username: 'ana', level: 1);

  test('login with valid credentials authenticates the user', () async {
    when(() => authRepo.login(email: 'ana@daypilot.test', password: 'password123'))
        .thenAnswer((_) async => user);

    await container.read(authNotifierProvider.notifier).login(
          email: 'ana@daypilot.test',
          password: 'password123',
        );

    final state = container.read(authNotifierProvider);
    expect(state.status, AuthStatus.authenticated);
    expect(state.user, user);
    expect(state.error, isNull);
  });

  test('login with an empty field fails fast without calling the repository', () async {
    await container.read(authNotifierProvider.notifier).login(email: '', password: 'password123');

    final state = container.read(authNotifierProvider);
    expect(state.status, AuthStatus.unauthenticated);
    expect(state.error, isA<EmptyCredentialsError>());
    verifyNever(() => authRepo.login(email: any(named: 'email'), password: any(named: 'password')));
  });

  test('signUp requiring email confirmation reports that error instead of authenticating', () async {
    when(() => authRepo.signUp(
          name: any(named: 'name'),
          username: any(named: 'username'),
          email: any(named: 'email'),
          password: any(named: 'password'),
          region: any(named: 'region'),
        )).thenAnswer((_) async => null);

    await container.read(authNotifierProvider.notifier).signUp(
          name: 'Ana',
          username: 'ana',
          email: 'ana@daypilot.test',
          password: 'password123',
        );

    final state = container.read(authNotifierProvider);
    expect(state.status, AuthStatus.unauthenticated);
    expect(state.error, isA<EmailConfirmationRequiredError>());
  });
}
