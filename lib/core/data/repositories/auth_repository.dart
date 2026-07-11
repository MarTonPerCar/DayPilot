import '../models/app_user.dart';

abstract class AuthRepository {
  Future<AppUser?> currentUser();

  Future<AppUser> login({required String email, required String password});

  Future<AppUser?> signUp({
    required String name,
    required String username,
    required String email,
    required String password,
    String? region,
  });

  Future<void> logout();

  Future<void> sendPasswordResetEmail(String email);
}
