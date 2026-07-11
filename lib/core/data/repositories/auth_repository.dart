import '../models/app_user.dart';

abstract class AuthRepository {
  Future<AppUser?> currentUser();

  Future<AppUser> login({required String email, required String password});

  /// Returns null if email confirmation is required (no session yet).
  Future<AppUser?> signUp({
    required String name,
    required String username,
    required String email,
    required String password,
    String? region,
  });

  Future<void> logout();
}
