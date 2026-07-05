import '../models/app_user.dart';

abstract class AuthRepository {
  Future<AppUser?> currentUser();

  Future<AppUser> login({required String email, required String password});

  Future<void> logout();
}
