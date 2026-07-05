import 'dart:io';

import 'package:supabase/supabase.dart';

Future<void> main() async {
  final client = SupabaseClient(
    'https://xcsaajaslvaqkhghkwpb.supabase.co',
    'sb_publishable_y5-_I0EguDDK51MGzZMCKg_r2mYYZVG',
  );

  print('1. Login...');
  final auth = await client.auth.signInWithPassword(
    email: 'ana.garcia@daypilot.test',
    password: 'password123',
  );
  print('   OK. UID: ${auth.user?.id}');

  print('2. Fetch tasks...');
  final tasks = await client.from('tasks').select();
  print('   OK. ${tasks.length} tarea(s).');

  print('3. Insert task...');
  final uid = auth.user!.id;
  await client.from('tasks').insert({
    'user_id': uid,
    'title': 'Tarea desde script Dart (verify_connection)',
    'category': 'General',
    'difficulty': 'EASY',
  });
  print('   OK.');

  print('4. Re-fetch tasks to confirm insert...');
  final tasksAfter = await client.from('tasks').select();
  print('   OK. ${tasksAfter.length} tarea(s) ahora.');

  print('\nConexión Flutter <-> Supabase verificada correctamente.');
  exit(0);
}
