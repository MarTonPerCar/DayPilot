import 'package:flutter/material.dart';
import '../../components/cards/daily_summary_card.dart';
import '../../components/cards/home_menu_card.dart';
import '../../data/app_data.dart';
import '../../l10n/app_localizations.dart';
import '../calendar/calendar_screen.dart';
import '../habits/habits_screen.dart';
import '../progress/progress_screen.dart';
import '../rivalry/rivalry_screen.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: LayoutBuilder(
          builder: (context, constraints) {
            final available = (constraints.maxHeight - 32).clamp(0.0, double.infinity); // 16 top + 16 bottom
            final summaryH = available * 0.38;
            final gridH = (available - summaryH - 10).clamp(0.0, double.infinity);

            return Padding(
              padding: const EdgeInsets.fromLTRB(16, 16, 16, 16),
              child: Column(
                children: [
                  SizedBox(
                    height: summaryH,
                    child: const DailySummaryCard(
                      userName: 'Mario',
                      streak: AppData.currentUserStreak,
                      stepsToday: AppData.stepsToday,
                      stepsGoal: AppData.stepsGoal,
                      tasksCompleted: AppData.tasksCompletedToday,
                      tasksTotal: AppData.tasksTotalToday,
                      pointsToday: AppData.pointsToday,
                      rankingPosition: AppData.rankingPositionToday,
                    ),
                  ),
                  const SizedBox(height: 10),
                  SizedBox(
                    height: gridH,
                    child: const _HomeGrid(),
                  ),
                ],
              ),
            );
          },
        ),
      ),
    );
  }
}

class _HomeGrid extends StatelessWidget {
  const _HomeGrid();

  // Colores fijos por sección (no dependen del tema activo).
  static const _calendarColor = Color(0xFF4A7C59); // verde
  static const _progressColor = Color(0xFF1A6B8A); // azul
  static const _habitsColor = Color(0xFF6B4FA8); // morado
  static const _rivalryColor = Color(0xFFB85C00); // naranja

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);

    return Column(
      children: [
        Expanded(
          child: Row(
            children: [
              Expanded(
                child: HomeMenuCard(
                  icon: Icons.calendar_month_rounded,
                  title: l10n.calendarTitle,
                  accentColor: _calendarColor,
                  onTap: () => Navigator.push(
                    context,
                    MaterialPageRoute(builder: (_) => const CalendarScreen()),
                  ),
                  indicator: HomeMenuProgressBar(
                    value: AppData.tasksCompletedToday / AppData.tasksTotalToday,
                    label: l10n.homeTasksTodayLabel(AppData.tasksCompletedToday, AppData.tasksTotalToday),
                    color: _calendarColor,
                  ),
                ),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: HomeMenuCard(
                  icon: Icons.trending_up_rounded,
                  title: l10n.progressTitle,
                  accentColor: _progressColor,
                  onTap: () => Navigator.push(
                    context,
                    MaterialPageRoute(builder: (_) => const ProgressScreen()),
                  ),
                  indicator: const HomeMenuMiniBarChart(
                    values: AppData.homeWeeklyPointsTrend,
                    color: _progressColor,
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
                  icon: Icons.fitness_center_rounded,
                  title: l10n.commonHabits,
                  accentColor: _habitsColor,
                  onTap: () => Navigator.push(
                    context,
                    MaterialPageRoute(builder: (_) => const HabitsScreen()),
                  ),
                  indicator: HomeMenuHabitsIndicator(
                    stepsProgress: AppData.stepsGoal > 0 ? AppData.stepsToday / AppData.stepsGoal : 0.0,
                    stepsLabel: l10n.homeStepsProgressLabel(AppData.homeStepsProgressPercent),
                    timerLabel: l10n.homeTimerPending,
                    color: _habitsColor,
                  ),
                ),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: HomeMenuCard(
                  icon: Icons.emoji_events_rounded,
                  title: l10n.rivalryTitle,
                  accentColor: _rivalryColor,
                  onTap: () => Navigator.push(
                    context,
                    MaterialPageRoute(builder: (_) => const RivalryScreen()),
                  ),
                  indicator: const HomeMenuRivalryIndicator(
                    position: AppData.homeRivalryPosition,
                    total: AppData.homeRivalryTotal,
                    color: _rivalryColor,
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
