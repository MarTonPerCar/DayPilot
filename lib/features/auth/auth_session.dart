import '../../core/data/models/app_user.dart';

enum AuthStatus { idle, authenticating, unauthenticated, authenticated }

class AuthSession {
  const AuthSession({required this.status, this.user, this.error});

  final AuthStatus status;
  final AppUser? user;

  /// Un-localized — widgets format it via `friendlyAuthError`.
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
