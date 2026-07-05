import 'package:supabase_flutter/supabase_flutter.dart';

import '../models/app_user.dart';
import 'auth_repository.dart';

class SupabaseAuthRepository implements AuthRepository {
  SupabaseAuthRepository(this._client);

  final SupabaseClient _client;

  @override
  Future<AppUser?> currentUser() async {
    final uid = _client.auth.currentUser?.id;
    if (uid == null) return null;
    return _fetchProfile(uid);
  }

  @override
  Future<AppUser> login({required String email, required String password}) async {
    final response = await _client.auth.signInWithPassword(
      email: email,
      password: password,
    );
    final uid = response.user?.id;
    if (uid == null) {
      throw const AuthException('No se pudo iniciar sesión.');
    }
    return _fetchProfile(uid);
  }

  @override
  Future<void> logout() => _client.auth.signOut();

  Future<AppUser> _fetchProfile(String uid) async {
    final row = await _client.from('users').select().eq('id', uid).single();
    return AppUser.fromMap(row);
  }
}
