import 'package:flutter/material.dart';
import '../../components/basic/top_bar.dart';
import '../../components/basic/avatar.dart';
import '../../components/cards/daily_summary_card.dart';
import '../../components/cards/home_menu_card.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return Scaffold(
      appBar: DayPilotTopBarWithActions(
        title: 'DayPilot',
        actions: [
          IconButton(
            icon: Badge(
              label: const Text('3'),
              child: const Icon(Icons.notifications_outlined),
            ),
            onPressed: () {},
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
      body: LayoutBuilder(
        builder: (context, constraints) {
          final available = constraints.maxHeight - 32; // 16 top + 16 bottom
          final summaryH = available * 0.38;
          final gridH = available - summaryH - 10;

          return Padding(
            padding: const EdgeInsets.fromLTRB(16, 16, 16, 16),
            child: Column(
              children: [
                // ── Resumen del día (38%)
                SizedBox(
                  height: summaryH,
                  child: const DailySummaryCard(
                    userName: 'Mario',
                    streak: 12,
                    stepsToday: 7432,
                    stepsGoal: 10000,
                    tasksCompleted: 5,
                    tasksTotal: 8,
                    pointsToday: 237,
                    rankingPosition: 4,
                  ),
                ),
                const SizedBox(height: 10),
                // ── 2×2 menú principal (62%)
                SizedBox(
                  height: gridH,
                  child: _HomeGrid(colors: colors),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}

class _HomeGrid extends StatelessWidget {
  final ColorScheme colors;
  const _HomeGrid({required this.colors});

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Expanded(
          child: Row(
            children: [
              Expanded(
                child: HomeMenuCard(
                  icon: Icons.calendar_month_rounded,
                  title: 'Calendario',
                  accentColor: colors.primary,
                  onTap: () {},
                  indicator: HomeMenuProgressBar(
                    value: 5 / 8,
                    label: '5/8 tareas hoy',
                    color: colors.primary,
                  ),
                ),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: HomeMenuCard(
                  icon: Icons.trending_up_rounded,
                  title: 'Progreso',
                  accentColor: colors.secondary,
                  onTap: () {},
                  indicator: HomeMenuMiniBarChart(
                    values: const [120, 180, 95, 210, 160, 240, 237],
                    color: colors.secondary,
                  ),
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 10),
        Expanded(
          child: Row(
            children: [
              Expanded(
                child: HomeMenuCard(
                  icon: Icons.self_improvement_rounded,
                  title: 'Hábitos',
                  accentColor: colors.tertiary,
                  onTap: () {},
                  indicator: HomeMenuProgressBar(
                    value: 7432 / 10000,
                    label: '7.4k / 10k pasos',
                    color: colors.tertiary,
                  ),
                ),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: HomeMenuCard(
                  icon: Icons.emoji_events_rounded,
                  title: 'Rivalidad',
                  accentColor: const Color(0xFFB85C00),
                  onTap: () {},
                  indicator: const HomeMenuRivalryIndicator(
                    position: 4,
                    total: 28,
                    color: Color(0xFFB85C00),
                  ),
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }
}
