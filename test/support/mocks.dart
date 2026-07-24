import 'package:daypilot/core/data/repositories/auth_repository.dart';
import 'package:daypilot/core/data/repositories/friends_repository.dart';
import 'package:daypilot/core/data/repositories/notifications_repository.dart';
import 'package:daypilot/core/data/repositories/profile_repository.dart';
import 'package:daypilot/core/data/repositories/progress_repository.dart';
import 'package:daypilot/core/data/repositories/ranking_repository.dart';
import 'package:daypilot/core/data/repositories/steps_repository.dart';
import 'package:daypilot/core/data/repositories/task_repository.dart';
import 'package:daypilot/core/data/repositories/tech_health_repository.dart';
import 'package:mocktail/mocktail.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

class MockAuthRepository extends Mock implements AuthRepository {}

class MockTaskRepository extends Mock implements TaskRepository {}

class MockStepsRepository extends Mock implements StepsRepository {}

class MockProgressRepository extends Mock implements ProgressRepository {}

class MockProfileRepository extends Mock implements ProfileRepository {}

class MockFriendsRepository extends Mock implements FriendsRepository {}

class MockRankingRepository extends Mock implements RankingRepository {}

class MockNotificationsRepository extends Mock implements NotificationsRepository {}

class MockTechHealthRepository extends Mock implements TechHealthRepository {}

class MockSupabaseClient extends Mock implements SupabaseClient {}

class MockGoTrueClient extends Mock implements GoTrueClient {}

/// A [SupabaseClient] whose `auth.currentUser` is always null, so every
/// notifier's `_subscribeToRealtimeOnce()` short-circuits before touching a
/// real realtime channel — the notifier logic under test never needs a live
/// socket, just a client that answers "no signed-in user" when asked.
MockSupabaseClient buildSignedOutSupabaseClient() {
  final client = MockSupabaseClient();
  final auth = MockGoTrueClient();
  when(() => auth.currentUser).thenReturn(null);
  when(() => client.auth).thenReturn(auth);
  return client;
}
