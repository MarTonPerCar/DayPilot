import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/cache/session_cache.dart';
import '../../core/connectivity/connectivity_service.dart';
import '../../core/connectivity/offline_notifier.dart';
import '../../core/data/models/auth_exceptions.dart';
import '../../core/data/repositories/providers.dart';
import '../../core/logging/app_logger.dart';
import '../friends/friends_notifier.dart';
import '../notifications/notifications_notifier.dart';
import '../profile/profile_notifier.dart';
import '../profile/weekly_summary_notifier.dart';
import '../progress/progress_notifier.dart';
import '../rivalry/ranking_notifier.dart';
import '../steps/steps_notifier.dart';
import '../tasks/tasks_notifier.dart';
import '../techhealth/tech_health_notifier.dart';
import 'auth_error.dart';
import 'auth_session.dart';

class AuthNotifier extends Notifier<AuthSession> {
  @override
  AuthSession build() => AuthSession.initial;

  void _invalidateUserScopedProviders() {
    ref.invalidate(profileStatsNotifierProvider);
    ref.invalidate(weeklySummaryNotifierProvider);
    ref.invalidate(stepsNotifierProvider);
    ref.invalidate(progressNotifierProvider);
    ref.invalidate(friendsNotifierProvider);
    ref.invalidate(tasksNotifierProvider);
    ref.invalidate(rankingNotifierProvider);
    ref.invalidate(notificationsNotifierProvider);
    ref.invalidate(techHealthNotifierProvider);

    ref.invalidate(tasksCacheProvider);
  }

  Future<void> login({required String email, required String password}) async {
    if (email.isEmpty || password.isEmpty) {
      state = AuthSession(status: AuthStatus.unauthenticated, error: const EmptyCredentialsError());
      return;
    }
    state = state.copyWith(status: AuthStatus.authenticating, error: null);
    if (!await ensureOnline(ref)) {
      state = state.copyWith(status: AuthStatus.unauthenticated);
      return;
    }
    try {
      final user = await ref.read(authRepositoryProvider).login(
            email: email,
            password: password,
          );
      _invalidateUserScopedProviders();
      state = state.copyWith(status: AuthStatus.authenticated, user: user);
    } catch (e, st) {
      AppLogger.logError('AuthNotifier.login', e, st);
      if (isConnectivityError(e)) {
        ref.read(isOfflineProvider.notifier).setOffline(true);
      }
      state = AuthSession(status: AuthStatus.unauthenticated, error: e);
    }
  }

  Future<void> signUp({
    required String name,
    required String username,
    required String email,
    required String password,
    String? region,
  }) async {
    if (name.isEmpty || username.isEmpty || email.isEmpty || password.isEmpty) {
      state = AuthSession(status: AuthStatus.unauthenticated, error: const EmptyRegisterFieldsError());
      return;
    }
    state = state.copyWith(status: AuthStatus.authenticating, error: null);
    if (!await ensureOnline(ref)) {
      state = state.copyWith(status: AuthStatus.unauthenticated);
      return;
    }
    try {
      final user = await ref.read(authRepositoryProvider).signUp(
            name: name,
            username: username,
            email: email,
            password: password,
            region: region,
          );
      if (user == null) {
        state = AuthSession(status: AuthStatus.unauthenticated, error: const EmailConfirmationRequiredError());
        return;
      }
      _invalidateUserScopedProviders();
      state = state.copyWith(status: AuthStatus.authenticated, user: user);
    } catch (e, st) {
      AppLogger.logError('AuthNotifier.signUp', e, st);
      if (isConnectivityError(e)) {
        ref.read(isOfflineProvider.notifier).setOffline(true);
      }
      state = AuthSession(status: AuthStatus.unauthenticated, error: e);
    }
  }

  Future<void> logout() async {
    if (!await ensureOnline(ref)) {
      _invalidateUserScopedProviders();
      state = AuthSession.initial;
      return;
    }
    try {
      await ref.read(authRepositoryProvider).logout();
    } catch (e, st) {
      AppLogger.logError('AuthNotifier.logout', e, st);
      if (isConnectivityError(e)) {
        ref.read(isOfflineProvider.notifier).setOffline(true);
      }
    }
    _invalidateUserScopedProviders();
    state = AuthSession.initial;
  }
}

final authNotifierProvider = NotifierProvider<AuthNotifier, AuthSession>(AuthNotifier.new);
