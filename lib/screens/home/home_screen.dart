import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../components/cards/daily_summary_card.dart';
import '../../components/cards/home_menu_card.dart';
import '../../features/profile/profile_notifier.dart';
import '../../features/progress/progress_notifier.dart';
import '../../features/rivalry/ranking_notifier.dart';
import '../../features/steps/steps_notifier.dart';
import '../../features/tasks/tasks_notifier.dart';
import '../../l10n/app_localizations.dart';
import '../calendar/calendar_screen.dart';
import '../habits/habits_screen.dart';
import '../progress/progress_screen.dart';
import '../rivalry/rivalry_screen.dart';

bool _isToday(DateTime date) {
  final now = DateTime.now();
  return date.year == now.year && date.month == now.month && date.day == now.day;
}

class HomeScreen extends ConsumerWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final stats = ref.watch(profileStatsNotifierProvider);
    final steps = ref.watch(stepsNotifierProvider);
    final progress = ref.watch(progressNotifierProvider);
    final allTasks = ref.watch(tasksNotifierProvider).tasks;
    final tasksToday = allTasks.where((t) => _isToday(t.date)).toList();
    final ranking = ref.watch(rankingNotifierProvider);

    final myRankingIndex = ranking.indexWhere((r) => r.isCurrentUser);
    final rankingPosition = myRankingIndex == -1 ? 0 : myRankingIndex + 1;
    final rankingTotal = ranking.length;

    return Scaffold(
      body: SafeArea(
        child: LayoutBuilder(
          builder: (context, constraints) {
            final available = (constraints.maxHeight - 32).clamp(0.0, double.infinity);
            final summaryH = available * 0.38;
            final gridH = (available - summaryH - 10).clamp(0.0, double.infinity);

            return Padding(
              padding: const EdgeInsets.fromLTRB(16, 16, 16, 16),
              child: Column(
                children: [
                  SizedBox(
                    height: summaryH,
                    child: stats == null
                        ? const Center(child: CircularProgressIndicator())
                        : DailySummaryCard(
                            userName: stats.name.split(' ').first,
                            streak: stats.streak,
                            stepsToday: steps?.steps ?? 0,
                            stepsGoal: steps?.goal ?? 10000,
                            tasksCompleted: tasksToday.where((t) => t.done).length,
                            tasksTotal: tasksToday.length,
                            pointsToday: progress?.pointsToday ?? 0,
                            rankingPosition: rankingPosition,
                          ),
                  ),
                  const SizedBox(height: 10),
                  SizedBox(
                    height: gridH,
                    child: _HomeGrid(
                      tasksCompletedAll: allTasks.where((t) => t.done).length,
                      tasksTotalAll: allTasks.length,
                      stepsToday: steps?.steps ?? 0,
                      stepsGoal: steps?.goal ?? 10000,
                      weeklyPointsTrend: _lastSeven(progress?.pointsHistory ?? const []),
                      rankingPosition: rankingPosition,
                      rankingTotal: rankingTotal,
                    ),
                  ),
                ],
              ),
            );
          },
        ),
      ),
    );
  }

  static List<double> _lastSeven(List<double> history) {
    if (history.length <= 7) return history;
    return history.sublist(history.length - 7);
  }
}

class _HomeGrid extends StatelessWidget {
  const _HomeGrid({
    required this.tasksCompletedAll,
    required this.tasksTotalAll,
    required this.stepsToday,
    required this.stepsGoal,
    required this.weeklyPointsTrend,
    required this.rankingPosition,
    required this.rankingTotal,
  });

  final int tasksCompletedAll;
  final int tasksTotalAll;
  final int stepsToday;
  final int stepsGoal;
  final List<double> weeklyPointsTrend;
  final int rankingPosition;
  final int rankingTotal;

  static const _calendarColor = Color(0xFF4A7C59);
  static const _progressColor = Color(0xFF1A6B8A);
  static const _habitsColor = Color(0xFF6B4FA8);
  static const _rivalryColor = Color(0xFFB85C00);

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final stepsProgress = stepsGoal > 0 ? stepsToday / stepsGoal : 0.0;
    final stepsPercent = stepsGoal > 0 ? (stepsToday / stepsGoal * 100).round() : 0;

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
                    value: tasksTotalAll > 0 ? tasksCompletedAll / tasksTotalAll : 0.0,
                    label: l10n.homeTasksLabel(tasksCompletedAll, tasksTotalAll),
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
                  indicator: HomeMenuMiniBarChart(
                    values: weeklyPointsTrend,
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
                    stepsProgress: stepsProgress,
                    stepsLabel: l10n.homeStepsProgressLabel(stepsPercent),
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
                  indicator: HomeMenuRivalryIndicator(
                    position: rankingPosition,
                    total: rankingTotal,
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
