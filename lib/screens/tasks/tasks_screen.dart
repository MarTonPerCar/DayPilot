import 'package:flutter/material.dart';
import '../../components/basic/top_bar.dart';
import '../../components/basic/filter_selector.dart';
import '../../components/basic/empty_state.dart';
import '../../components/basic/task_dot.dart';
import '../../components/cards/task_card.dart';
import 'task_form_screen.dart';

class TasksScreen extends StatefulWidget {
  const TasksScreen({super.key});

  @override
  State<TasksScreen> createState() => _TasksScreenState();
}

class _TasksScreenState extends State<TasksScreen> {
  String _filter = 'Todas';
  static const _filters = ['Todas', 'Pendientes', 'Completadas', 'Hoy'];

  final _tasks = [
    _Task('1', 'Diseñar pantalla de inicio', 'Crear wireframe y componentes base', TaskPriority.high, 'Diseño', 'hoy', false),
    _Task('2', 'Reunión de equipo', null, TaskPriority.medium, 'Trabajo', 'hoy, 18:00', false),
    _Task('3', 'Revisar documentación de Flutter', null, TaskPriority.medium, null, null, false),
    _Task('4', 'Preparar presentación', 'Slides para el TFG', TaskPriority.high, 'Estudio', 'mañana', false),
    _Task('5', 'Llamar al médico', null, TaskPriority.low, 'Personal', null, true),
    _Task('6', 'Comprar comida', null, TaskPriority.low, null, null, true),
    _Task('7', 'Hacer deporte 30 min', 'Salir a correr o ir al gimnasio', TaskPriority.medium, 'Salud', 'hoy', true),
  ];

  List<_Task> get _filtered {
    return switch (_filter) {
      'Pendientes'  => _tasks.where((t) => !t.done).toList(),
      'Completadas' => _tasks.where((t) => t.done).toList(),
      'Hoy'         => _tasks.where((t) => t.date != null && t.date!.contains('hoy')).toList(),
      _             => _tasks,
    };
  }

  @override
  Widget build(BuildContext context) {
    final filtered = _filtered;

    return Scaffold(
      appBar: DayPilotTopBarWithActions(
        title: 'Tareas',
        actions: [
          IconButton(icon: const Icon(Icons.search_rounded), onPressed: () {}),
          IconButton(icon: const Icon(Icons.sort_rounded), onPressed: () {}),
        ],
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 8, 16, 4),
            child: DayPilotFilterSelector<String>(
              options: _filters,
              selected: _filter,
              label: (s) => s,
              onSelected: (s) => setState(() => _filter = s),
            ),
          ),
          Expanded(
            child: filtered.isEmpty
                ? const DayPilotEmptyState(
                    icon: Icons.task_alt_outlined,
                    title: 'Sin tareas',
                    subtitle: 'Añade una tarea con el botón +',
                  )
                : ListView.separated(
                    padding: const EdgeInsets.fromLTRB(16, 8, 16, 100),
                    itemCount: filtered.length,
                    separatorBuilder: (_, __) => const SizedBox(height: 6),
                    itemBuilder: (ctx, i) {
                      final t = filtered[i];
                      return TaskSwipeCard(
                        key: ValueKey(t.id),
                        id: t.id,
                        title: t.title,
                        description: t.description,
                        dueDate: t.date,
                        priority: t.priority,
                        category: t.category,
                        completed: t.done,
                        onToggle: () => setState(() => t.done = !t.done),
                        onDelete: () => setState(() => _tasks.removeWhere((x) => x.id == t.id)),
                      );
                    },
                  ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => Navigator.push(
          context,
          MaterialPageRoute(builder: (_) => const TaskFormScreen()),
        ),
        tooltip: 'Nueva tarea',
        child: const Icon(Icons.add_rounded),
      ),
    );
  }
}

class _Task {
  final String id;
  final String title;
  final String? description;
  final TaskPriority priority;
  final String? category;
  final String? date;
  bool done;

  _Task(this.id, this.title, this.description, this.priority, this.category, this.date, this.done);
}
