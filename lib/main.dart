import 'package:flutter/material.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import 'db_test_screen.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Supabase.initialize(
    url: const String.fromEnvironment('SUPABASE_URL'),
    publishableKey: const String.fromEnvironment('SUPABASE_KEY'),
  );
  runApp(const TestSupabaseApp());
}

class TestSupabaseApp extends StatelessWidget {
  const TestSupabaseApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'DayPilot — Test Supabase',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF4A7C59)),
        useMaterial3: true,
      ),
      home: const DbTestScreen(),
    );
  }
}
