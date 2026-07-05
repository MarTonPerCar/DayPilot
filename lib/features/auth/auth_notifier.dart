import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/data/repositories/providers.dart';
import 'auth_error.dart';
import 'auth_session.dart';

class AuthNotifier extends Notifier<AuthSession> {
  @override
  AuthSession build() => AuthSession.initial;

  Future<void> login({required String email, required String password}) async {
    if (email.isEmpty || password.isEmpty) {
      state = AuthSession(status: AuthStatus.unauthenticated, error: const EmptyCredentialsError());
      return;
    }
    state = state.copyWith(status: AuthStatus.authenticating, error: null);
    try {
      final user = await ref.read(authRepositoryProvider).login(
            email: email,
            password: password,
          );
      state = state.copyWith(status: AuthStatus.authenticated, user: user);
    } catch (e) {
      state = AuthSession(status: AuthStatus.unauthenticated, error: e);
    }
  }
}

final authNotifierProvider = NotifierProvider<AuthNotifier, AuthSession>(AuthNotifier.new);
