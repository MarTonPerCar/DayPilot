import 'package:supabase_flutter/supabase_flutter.dart';

import '../models/app_user.dart';
import '../models/auth_exceptions.dart';
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
  Future<AppUser?> signUp({
    required String name,
    required String username,
    required String email,
    required String password,
    String? region,
  }) async {
    try {
      await _client.auth.signUp(email: email, password: password);
    } on AuthException catch (e) {
      // Could be a real existing account, or an orphan from a previous
      // failed signup — sign in to tell the two apart.
      if (e.message.toLowerCase().contains('user already registered')) {
        await _client.auth.signInWithPassword(email: email, password: password);
      } else {
        rethrow;
      }
    }

    final uid = _client.auth.currentUser?.id;
    if (uid == null) {
      throw const EmailConfirmationRequiredError();
    }

    final existing = await _client.from('users').select('id').eq('id', uid).limit(1);
    if (existing.isNotEmpty) {
      throw const AuthException('User already registered');
    }

    await _client.from('users').insert({
      'id': uid,
      'email': email,
      'name': name,
      'username': username,
      'username_lower': username.toLowerCase(),
      'region': region,
    });

    return _fetchProfile(uid);
  }

  @override
  Future<void> logout() => _client.auth.signOut();

  Future<AppUser> _fetchProfile(String uid) async {
    final row = await _client.from('users').select().eq('id', uid).single();
    return AppUser.fromMap(row);
  }
}
