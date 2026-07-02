import 'package:flutter/material.dart';
import '../../components/basic/top_bar.dart';
import '../../components/basic/avatar.dart';
import '../../components/basic/task_dot.dart';
import '../../components/cards/daily_summary_card.dart';
import '../../components/cards/steps_card.dart';
import '../../components/cards/habit_card.dart';
import '../../components/cards/task_card.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  bool _task1Done = false;
  bool _task2Done = false;
  bool _task3Done = false;

  String get _greeting {
    final h = DateTime.now().hour;
    if (h < 12) return 'Buenos días';
    if (h < 20) return 'Buenas tardes';
    return 'Buenas noches';
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    return Scaffold(
      appBar: DayPilotTopBarWithActions(
        title: '$_greeting, Mario',
        actions: [
          IconButton(
            icon: Badge(
              label: const Text('3'),
              child: const Icon(Icons.notifications_outlined),
            ),
            onPressed: () {},
            tooltip: 'Notificaciones',
          ),
          Padding(
            padding: const EdgeInsets.only(right: 8),
            child: GestureDetector(
              onTap: () {},
              child: const DayPilotAvatar(name: 'Mario García', size: 36),
            ),
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 12, 16, 32),
        children: [
          // ── Resumen del día
          const DailySummaryCard(
            pointsFromTasks: 120,
            pointsFromSteps: 37,
            pointsFromTimer: 80,
            pointsFromHealth: 50,
            pointsFromWellness: 30,
          ),

          const SizedBox(height: 24),

          // ── Pasos de hoy
          const StepsCard(steps: 7432, goal: 10000, pointsEarned: 37),

          const SizedBox(height: 24),

          // ── Mis hábitos
          Text(
            'MIS HÁBITOS',
            style: text.labelSmall?.copyWith(
              color: colors.primary,
              letterSpacing: 1.2,
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 10),
          HabitCard(
            icon: Icons.task_alt_rounded,
            title: 'Tareas',
            subtitle: '3 pendientes · 5 completadas hoy',
            progress: 0.63,
            onTap: () {},
          ),
          const SizedBox(height: 8),
          HabitCard(
            icon: Icons.timer_rounded,
            title: 'Temporizadores',
            subtitle: '2 sesiones · 48 min totales',
            progress: 0.48,
            onTap: () {},
          ),
          const SizedBox(height: 8),
          HabitCard(
            icon: Icons.health_and_safety_rounded,
            title: 'Salud tecnológica',
            subtitle: '3 apps con límite · 0 superadas',
            iconColor: colors.error,
            onTap: () {},
          ),
          const SizedBox(height: 8),
          HabitCard(
            icon: Icons.self_improvement_rounded,
            title: 'Bienestar',
            subtitle: 'Racha de 12 días consecutivos',
            iconColor: Colors.purple,
            onTap: () {},
          ),

          const SizedBox(height: 24),

          // ── Tareas pendientes
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'TAREAS DE HOY',
                style: text.labelSmall?.copyWith(
                  color: colors.primary,
                  letterSpacing: 1.2,
                  fontWeight: FontWeight.w600,
                ),
              ),
              TextButton(onPressed: () {}, child: const Text('Ver todas')),
            ],
          ),
          const SizedBox(height: 8),
          TaskCard(
            title: 'Diseñar pantalla de inicio',
            description: 'Crear wireframe y componentes base',
            dueDate: 'hoy',
            priority: TaskPriority.high,
            category: 'Diseño',
            completed: _task1Done,
            onToggle: () => setState(() => _task1Done = !_task1Done),
          ),
          const SizedBox(height: 6),
          TaskCard(
            title: 'Revisar documentación de Flutter',
            priority: TaskPriority.medium,
            completed: _task2Done,
            onToggle: () => setState(() => _task2Done = !_task2Done),
          ),
          const SizedBox(height: 6),
          TaskCard(
            title: 'Reunión de equipo',
            dueDate: '18:00',
            priority: TaskPriority.medium,
            category: 'Trabajo',
            completed: _task3Done,
            onToggle: () => setState(() => _task3Done = !_task3Done),
          ),
        ],
      ),
    );
  }
}
