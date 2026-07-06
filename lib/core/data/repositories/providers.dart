import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import 'auth_repository.dart';
import 'progress_repository.dart';
import 'steps_repository.dart';
import 'supabase_auth_repository.dart';
import 'supabase_progress_repository.dart';
import 'supabase_steps_repository.dart';
import 'supabase_task_repository.dart';
import 'task_repository.dart';

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
