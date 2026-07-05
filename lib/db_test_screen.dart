import 'package:flutter/material.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

final supabase = Supabase.instance.client;

class DbTestScreen extends StatefulWidget {
  const DbTestScreen({super.key});

  @override
  State<DbTestScreen> createState() => _DbTestScreenState();
}

class _DbTestScreenState extends State<DbTestScreen> {
  String _log = 'Sin acciones todavía.';
  bool _busy = false;

  Future<void> _run(Future<String> Function() action) async {
    setState(() => _busy = true);
    try {
      final result = await action();
      setState(() => _log = result);
    } catch (e) {
      setState(() => _log = 'Error: $e');
    } finally {
      setState(() => _busy = false);
    }
  }

  Future<String> _login() async {
    await supabase.auth.signInWithPassword(
      email: 'ana.garcia@daypilot.test',
      password: 'password123',
    );
    return 'Login OK. UID: ${supabase.auth.currentUser?.id}';
  }

  Future<String> _fetchTasks() async {
    final tasks = await supabase.from('tasks').select();
    final titles = tasks.map((t) => '- ${t['title']}').join('\n');
    return 'Tareas: ${tasks.length}\n$titles';
  }

  Future<String> _insertTask() async {
    final uid = supabase.auth.currentUser?.id;
    if (uid == null) return 'Primero haz login';
    await supabase.from('tasks').insert({
      'user_id': uid,
      'title': 'Tarea desde Flutter',
      'category': 'General',
      'difficulty': 'EASY',
    });
    return 'Tarea insertada OK';
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('DayPilot — prueba Supabase')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            FilledButton(
              onPressed: _busy ? null : () => _run(_login),
              child: const Text('1. Login'),
            ),
            const SizedBox(height: 12),
            FilledButton(
              onPressed: _busy ? null : () => _run(_fetchTasks),
              child: const Text('2. Traer tareas'),
            ),
            const SizedBox(height: 12),
            FilledButton(
              onPressed: _busy ? null : () => _run(_insertTask),
              child: const Text('3. Insertar tarea'),
            ),
            const SizedBox(height: 24),
            const Divider(),
            const Text('Resultado:', style: TextStyle(fontWeight: FontWeight.bold)),
            const SizedBox(height: 8),
            if (_busy) const CircularProgressIndicator(),
            Text(_log),
          ],
        ),
      ),
    );
  }
}
