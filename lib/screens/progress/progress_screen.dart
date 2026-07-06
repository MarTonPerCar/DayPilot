import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../components/basic/top_bar.dart';
import '../../components/cards/daily_points_card.dart';
import '../../components/cards/progress_chart_card.dart';
import '../../features/progress/progress_notifier.dart';
import '../../l10n/app_localizations.dart';

class ProgressScreen extends ConsumerWidget {
  const ProgressScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    final progress = ref.watch(progressNotifierProvider);

    return Scaffold(
      appBar: DayPilotTopBar(title: l10n.progressTitle, showBack: true),
      body: progress == null
          ? const Center(child: CircularProgressIndicator())
          : ListView(
              padding: const EdgeInsets.fromLTRB(16, 16, 16, 40),
              children: [
                DailyPointsCard(
                  pointsToday: progress.pointsToday,
                  pointsFromTasks: progress.pointsFromTasks,
                  pointsFromSteps: progress.pointsFromSteps,
                  pointsFromHabits: progress.pointsFromHabits,
                  pointsFromTimer: progress.pointsFromTimer,
                ),
                const SizedBox(height: 16),
                ProgressChartCard(
                  pointsHistory: progress.pointsHistory,
                  stepsHistory: progress.stepsHistory,
                  tasksHistory: progress.tasksHistory,
                  dayLabels: progress.dayLabels,
                ),
              ],
            ),
    );
  }
}
