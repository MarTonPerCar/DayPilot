import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import 'auth_repository.dart';
import 'friends_repository.dart';
import 'notifications_repository.dart';
import 'profile_repository.dart';
import 'progress_repository.dart';
import 'ranking_repository.dart';
import 'steps_repository.dart';
import 'supabase_auth_repository.dart';
import 'supabase_friends_repository.dart';
import 'supabase_notifications_repository.dart';
import 'supabase_profile_repository.dart';
import 'supabase_progress_repository.dart';
import 'supabase_ranking_repository.dart';
import 'supabase_steps_repository.dart';
import 'supabase_task_repository.dart';
import 'supabase_tech_health_repository.dart';
import 'task_repository.dart';
import 'tech_health_repository.dart';

final supabaseClientProvider = Provider<SupabaseClient>((ref) {
  return Supabase.instance.client;
});

final authRepositoryProvider = Provider<AuthRepository>((ref) {
  return SupabaseAuthRepository(ref.read(supabaseClientProvider));
});

final taskRepositoryProvider = Provider<TaskRepository>((ref) {
  return SupabaseTaskRepository(ref.read(supabaseClientProvider), ref);
});

final stepsRepositoryProvider = Provider<StepsRepository>((ref) {
  return SupabaseStepsRepository(ref.read(supabaseClientProvider));
});

final progressRepositoryProvider = Provider<ProgressRepository>((ref) {
  return SupabaseProgressRepository(ref.read(supabaseClientProvider));
});

final profileRepositoryProvider = Provider<ProfileRepository>((ref) {
  return SupabaseProfileRepository(ref.read(supabaseClientProvider));
});

final friendsRepositoryProvider = Provider<FriendsRepository>((ref) {
  return SupabaseFriendsRepository(ref.read(supabaseClientProvider));
});

final rankingRepositoryProvider = Provider<RankingRepository>((ref) {
  return SupabaseRankingRepository(ref.read(supabaseClientProvider));
});

final notificationsRepositoryProvider = Provider<NotificationsRepository>((ref) {
  return SupabaseNotificationsRepository(ref.read(supabaseClientProvider));
});

final techHealthRepositoryProvider = Provider<TechHealthRepository>((ref) {
  return SupabaseTechHealthRepository(ref.read(supabaseClientProvider));
});
