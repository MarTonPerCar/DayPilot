import 'package:flutter/material.dart';
import '../../components/basic/top_bar.dart';
import '../../components/cards/daily_points_card.dart';
import '../../components/cards/progress_chart_card.dart';
import '../../data/app_data.dart';
import '../../l10n/app_localizations.dart';

class ProgressScreen extends StatelessWidget {
  const ProgressScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    return Scaffold(
      appBar: DayPilotTopBar(title: l10n.progressTitle, showBack: true),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 16, 16, 40),
        children: const [
          // ── Resumen del día
          DailyPointsCard(
            rankingPosition: AppData.rankingPositionToday,
            pointsToday: AppData.pointsToday,
            pointsFromTasks: AppData.pointsTodayFromTasks,
            pointsFromSteps: AppData.pointsTodayFromSteps,
            pointsFromHabits: AppData.pointsTodayFromHabits,
            pointsFromTimer: AppData.pointsTodayFromTimer,
          ),
          SizedBox(height: 16),

          // ── Gráfica de progreso de los últimos 30 días
          ProgressChartCard(
            pointsHistory: AppData.last30DaysPoints,
            stepsHistory: AppData.last30DaysSteps,
            tasksHistory: AppData.last30DaysTasks,
          ),
        ],
      ),
    );
  }
}
