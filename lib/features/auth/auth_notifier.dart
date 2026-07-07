import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/cache/session_cache.dart';
import '../../core/data/repositories/providers.dart';
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

  // These hold per-user state fetched once on first read, so switching
  // accounts within the same running app (no restart) would otherwise keep
  // showing whichever user's data was fetched first.
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
    // SupabaseTaskRepository.getTasks() short-circuits through this cache
    // before ever touching Supabase — invalidating tasksNotifierProvider
    // alone still hands the new user the previous one's cached list back.
    ref.invalidate(tasksCacheProvider);
  }

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
      _invalidateUserScopedProviders();
      state = state.copyWith(status: AuthStatus.authenticated, user: user);
    } catch (e) {
      state = AuthSession(status: AuthStatus.unauthenticated, error: e);
    }
  }

  Future<void> logout() async {
    await ref.read(authRepositoryProvider).logout();
    _invalidateUserScopedProviders();
    state = AuthSession.initial;
  }
}

final authNotifierProvider = NotifierProvider<AuthNotifier, AuthSession>(AuthNotifier.new);
