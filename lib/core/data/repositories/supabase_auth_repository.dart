import 'package:supabase_flutter/supabase_flutter.dart';

import '../models/app_user.dart';
import '../models/auth_exceptions.dart';
import 'auth_repository.dart';

class SupabaseAuthRepository implements AuthRepository {
  SupabaseAuthRepository(this._client);

  final SupabaseClient _client;

  @override
  Future<AppUser?> currentUser() async {
    final user = _client.auth.currentUser;
    if (user == null) return null;
    await _ensureProfileExists(user);
    return _fetchProfile(user.id);
  }

  @override
  Future<AppUser> login({required String email, required String password}) async {
    final response = await _client.auth.signInWithPassword(
      email: email,
      password: password,
    );
    final user = response.user;
    if (user == null) {
      throw const AuthException('No se pudo iniciar sesión.');
    }

    await _ensureProfileExists(user);
    return _fetchProfile(user.id);
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
      await _client.auth.signUp(
        email: email,
        password: password,
        data: {'name': name, 'username': username, 'region': region},
      );
    } on AuthException catch (e) {

      if (e.message.toLowerCase().contains('user already registered')) {
        try {
          await _client.auth.signInWithPassword(email: email, password: password);
        } on AuthException {

          throw const AuthException('User already registered');
        }
      } else {
        rethrow;
      }
    }

    final user = _client.auth.currentUser;
    if (user == null) {
      throw const EmailConfirmationRequiredError();
    }

    final existing = await _client.from('users').select('id').eq('id', user.id).limit(1);
    if (existing.isNotEmpty) {
      throw const AuthException('User already registered');
    }

    await _client.from('users').insert({
      'id': user.id,
      'email': email,
      'name': name,
      'username': username,
      'username_lower': username.toLowerCase(),
      'region': region,
    });

    return _fetchProfile(user.id);
  }

  @override
  Future<void> logout() => _client.auth.signOut();

  @override
  Future<void> sendPasswordResetEmail(String email) =>
      _client.auth.resetPasswordForEmail(
        email,
        redirectTo: 'https://martonpercar.github.io/DayPilot/reset-password.html',
      );

  Future<void> _ensureProfileExists(User user) async {
    final existing = await _client.from('users').select('id').eq('id', user.id).limit(1);
    if (existing.isNotEmpty) return;

    final metadata = user.userMetadata ?? {};
    final username = metadata['username'] as String? ?? user.email!.split('@').first;
    await _client.from('users').insert({
      'id': user.id,
      'email': user.email,
      'name': metadata['name'] as String? ?? username,
      'username': username,
      'username_lower': username.toLowerCase(),
      'region': metadata['region'] as String?,
    });
  }

  Future<AppUser> _fetchProfile(String uid) async {
    final row = await _client.from('users').select().eq('id', uid).single();
    return AppUser.fromMap(row);
  }
}
