import '../../core/data/models/app_user.dart';

enum AuthStatus { idle, authenticating, unauthenticated, authenticated }

/// Local state for the login form itself (loading spinner, inline error) —
/// separate from the app-wide "is there a session" question, which
/// `authStateChangesProvider` answers by watching Supabase directly.
class AuthSession {
  const AuthSession({required this.status, this.user, this.error});

  final AuthStatus status;
  final AppUser? user;

  /// Raw failure from the last attempt, if any. Kept un-localized here —
  /// widgets turn it into text via `friendlyAuthError`, which needs the
  /// current [AppLocalizations].
  final Object? error;

  static const initial = AuthSession(status: AuthStatus.idle);

  AuthSession copyWith({AuthStatus? status, AppUser? user, Object? error}) {
    return AuthSession(
      status: status ?? this.status,
      user: user ?? this.user,
      error: error,
    );
  }
}
