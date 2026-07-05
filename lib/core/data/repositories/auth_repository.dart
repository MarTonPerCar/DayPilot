import '../models/app_user.dart';

abstract class AuthRepository {
  /// The signed-in user's profile, or `null` if there's no active session.
  Future<AppUser?> currentUser();

  Future<AppUser> login({required String email, required String password});

  Future<void> logout();
}
