import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show rootBundle;
import 'package:supabase_flutter/supabase_flutter.dart';

import 'db_test_screen.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  final envString = await rootBundle.loadString('env.json');
  final env = jsonDecode(envString) as Map<String, dynamic>;

  await Supabase.initialize(
    url: env['SUPABASE_URL'] as String,
    publishableKey: env['SUPABASE_KEY'] as String,
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
