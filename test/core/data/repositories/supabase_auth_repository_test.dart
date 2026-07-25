import 'dart:convert';

import 'package:daypilot/core/data/models/auth_exceptions.dart';
import 'package:daypilot/core/data/repositories/supabase_auth_repository.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import '../../../support/fake_supabase_http.dart';

/// Builds a GoTrue-shaped success body: a session (so `AuthResponse.fromJson`
/// resolves both `.session` and `.user`) wrapping the given user id.
Map<String, dynamic> _sessionBody(String userId, {Map<String, dynamic>? userMetadata}) {
  final exp = DateTime.now().add(const Duration(hours: 1)).millisecondsSinceEpoch ~/ 1000;
  final header = base64Url.encode(utf8.encode('{"alg":"HS256","typ":"JWT"}'));
  final payload = base64Url.encode(utf8.encode('{"exp":$exp,"sub":"$userId"}'));
  return {
    'access_token': '$header.$payload.signature',
    'token_type': 'bearer',
    'expires_in': 3600,
    'refresh_token': 'refresh-$userId',
    'user': {
      'id': userId,
      'app_metadata': <String, dynamic>{},
      'user_metadata': userMetadata ?? <String, dynamic>{},
      'aud': 'authenticated',
      'created_at': '2024-01-01T00:00:00Z',
    },
  };
}

void main() {
  late FakeSupabaseHttp http;
  late SupabaseAuthRepository repo;

  setUp(() {
    http = FakeSupabaseHttp();
    repo = SupabaseAuthRepository(http.client);
  });

  tearDown(() => http.dispose());

  group('currentUser', () {
    test('with no signed-in user returns null and makes no request', () async {
      final user = await repo.currentUser();

      expect(user, isNull);
      expect(http.requests, isEmpty);
    });

    test('with an existing profile row skips the insert', () async {
      await http.signIn('user-1');
      http.enqueue(jsonRows([
        {'id': 'user-1'},
      ]));
      http.enqueue(jsonRow({
        'id': 'user-1',
        'email': 'ana@daypilot.test',
        'name': 'Ana',
        'username': 'ana',
        'photo_url': null,
        'region': null,
        'level': 3,
      }));

      final user = await repo.currentUser();

      expect(user!.username, 'ana');
      expect(http.requests, hasLength(2));
    });

    test('with no profile row yet creates one from user metadata', () async {
      await http.signIn('user-1');
      http.enqueue(emptyRows());
      http.enqueue(noContent());
      http.enqueue(jsonRow({
        'id': 'user-1',
        'email': 'ana@daypilot.test',
        'name': 'Ana',
        'username': 'ana',
        'photo_url': null,
        'region': null,
        'level': 1,
      }));

      final user = await repo.currentUser();

      expect(user!.level, 1);
      expect(http.requests, hasLength(3));
      expect(http.requests[1].method, 'POST');
      expect(http.requests[1].url.path, '/rest/v1/users');
    });
  });

  group('login', () {
    test('signs in and fetches the existing profile', () async {
      http.enqueue(jsonRow(_sessionBody('user-1')));
      http.enqueue(jsonRows([
        {'id': 'user-1'},
      ]));
      http.enqueue(jsonRow({
        'id': 'user-1',
        'email': 'ana@daypilot.test',
        'name': 'Ana',
        'username': 'ana',
        'photo_url': null,
        'region': null,
        'level': 2,
      }));

      final user = await repo.login(email: 'ana@daypilot.test', password: 'password123');

      expect(user.username, 'ana');
      expect(http.requests[0].url.path, '/auth/v1/token');
      expect(http.requests[0].body, contains('"email":"ana@daypilot.test"'));
    });

    test('with no user in the response throws', () async {
      http.enqueue(jsonRow(const {}));

      expect(
        () => repo.login(email: 'ana@daypilot.test', password: 'wrong'),
        throwsA(isA<AuthException>()),
      );
    });
  });

  group('signUp', () {
    test('creates a new account and profile', () async {
      http.enqueue(jsonRow(_sessionBody('user-1', userMetadata: {'username': 'ana', 'name': 'Ana'})));
      http.enqueue(emptyRows());
      http.enqueue(noContent());
      http.enqueue(jsonRow({
        'id': 'user-1',
        'email': 'ana@daypilot.test',
        'name': 'Ana',
        'username': 'ana',
        'photo_url': null,
        'region': 'ES',
        'level': 1,
      }));

      final user = await repo.signUp(
        name: 'Ana',
        username: 'ana',
        email: 'ana@daypilot.test',
        password: 'password123',
        region: 'ES',
      );

      expect(user!.username, 'ana');
      expect(http.requests[2].body, contains('"username_lower":"ana"'));
    });

    test('with no session in the response requires email confirmation', () async {
      http.enqueue(jsonRow(const {}));

      expect(
        () => repo.signUp(name: 'Ana', username: 'ana', email: 'ana@daypilot.test', password: 'password123'),
        throwsA(isA<EmailConfirmationRequiredError>()),
      );
    });

    test('for an already-registered email whose retry-login succeeds still reports the conflict', () async {
      http.enqueue(postgrestError(message: 'User already registered', statusCode: 400));
      http.enqueue(jsonRow(_sessionBody('user-1')));
      http.enqueue(jsonRows([
        {'id': 'user-1'},
      ]));

      expect(
        () => repo.signUp(name: 'Ana', username: 'ana', email: 'ana@daypilot.test', password: 'password123'),
        throwsA(isA<AuthException>()),
      );
    });

    test('for an already-registered email with the wrong password reports the conflict', () async {
      http.enqueue(postgrestError(message: 'User already registered', statusCode: 400));
      http.enqueue(postgrestError(message: 'Invalid login credentials', statusCode: 400));

      expect(
        () => repo.signUp(name: 'Ana', username: 'ana', email: 'ana@daypilot.test', password: 'wrong'),
        throwsA(isA<AuthException>()),
      );
    });

    test('rethrows an unrelated signup failure as-is', () async {
      http.enqueue(postgrestError(message: 'Signups not allowed for this instance', statusCode: 400));

      expect(
        () => repo.signUp(name: 'Ana', username: 'ana', email: 'ana@daypilot.test', password: 'password123'),
        throwsA(isA<AuthException>()),
      );
    });
  });

  group('logout', () {
    test('with an active session also revokes it server-side', () async {
      await http.signIn('user-1');
      http.enqueue(noContent());

      await repo.logout();

      expect(http.requests, hasLength(1));
      expect(http.requests[0].url.path, '/auth/v1/logout');
    });

    test('with no active session makes no request', () async {
      await repo.logout();

      expect(http.requests, isEmpty);
    });
  });

  test('sendPasswordResetEmail posts to the recovery endpoint', () async {
    http.enqueue(noContent());

    await repo.sendPasswordResetEmail('ana@daypilot.test');

    expect(http.requests[0].url.path, '/auth/v1/recover');
    expect(http.requests[0].body, contains('"email":"ana@daypilot.test"'));
  });
}
