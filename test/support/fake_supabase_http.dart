import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

/// Builds a real [SupabaseClient] wired to a queue of canned HTTP responses
/// instead of the network. `postgrest`'s query builders implement [Future]
/// themselves and can't be mocked with mocktail, so repository tests drive
/// the real query-building/response-parsing code against a fake transport.
class FakeSupabaseHttp {
  FakeSupabaseHttp();

  final List<http.Response> _responses = [];
  final List<http.Request> requests = [];

  /// Queues [response] to be returned for the next outgoing request, in order.
  void enqueue(http.Response response) => _responses.add(response);

  late final SupabaseClient client = SupabaseClient(
    'https://test.supabase.co',
    'test-anon-key',
    // The default PKCE flow asserts on a configured async storage (normally
    // wired up by Supabase.initialize/shared_preferences), which these tests
    // never set up — the implicit flow skips that requirement entirely.
    authOptions: const AuthClientOptions(authFlowType: AuthFlowType.implicit),
    httpClient: MockClient((request) async {
      requests.add(request);
      if (_responses.isEmpty) {
        throw StateError('Unexpected extra HTTP request: ${request.method} ${request.url}');
      }
      final canned = _responses.removeAt(0);
      // postgrest reads `response.request` off the response it gets back, so
      // the canned response (built without a request in scope) needs it
      // stitched in here, where the real request is known.
      return http.Response.bytes(
        canned.bodyBytes,
        canned.statusCode,
        request: request,
        headers: canned.headers,
      );
    }),
  );

  /// Signs [client].auth in locally (no HTTP call) so `_client.auth.currentUser`
  /// resolves to a user with [userId], matching what repositories read.
  Future<void> signIn(String userId, {String? email, Map<String, dynamic>? userMetadata}) async {
    final exp = DateTime.now().add(const Duration(hours: 1)).millisecondsSinceEpoch ~/ 1000;
    final header = base64Url.encode(utf8.encode('{"alg":"HS256","typ":"JWT"}'));
    final payload = base64Url.encode(utf8.encode('{"exp":$exp,"sub":"$userId"}'));
    final jwt = '$header.$payload.signature';

    await client.auth.recoverSession(jsonEncode({
      'access_token': jwt,
      'token_type': 'bearer',
      'expires_in': 3600,
      'refresh_token': 'refresh-$userId',
      'user': {
        'id': userId,
        'email': email ?? '$userId@daypilot.test',
        'app_metadata': <String, dynamic>{},
        'user_metadata': userMetadata ?? <String, dynamic>{},
        'aud': 'authenticated',
        'created_at': '2024-01-01T00:00:00Z',
      },
    }));
  }

  Future<void> dispose() => client.dispose();
}

http.Response jsonRows(List<Map<String, dynamic>> rows) =>
    http.Response(jsonEncode(rows), 200, headers: {'content-type': 'application/json'});

http.Response jsonRow(Map<String, dynamic> row) =>
    http.Response(jsonEncode(row), 200, headers: {'content-type': 'application/json'});

http.Response emptyRows() => jsonRows(const []);

http.Response noContent() => http.Response('', 204);

http.Response postgrestError({String message = 'boom', String code = 'PGRST000', int statusCode = 400}) =>
    http.Response(
      jsonEncode({'message': message, 'code': code, 'details': null, 'hint': null}),
      statusCode,
      headers: {'content-type': 'application/json'},
    );
