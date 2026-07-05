import 'package:flutter/material.dart';
import '../../components/basic/top_bar.dart';
import '../../components/cards/profile_stats_card.dart';
import '../../components/cards/profile_info_card.dart';
import '../../components/cards/daily_points_card.dart';
import '../../components/cards/weekly_reaction_card.dart';
import '../../data/app_data.dart';
import '../../l10n/app_localizations.dart';
import 'settings_screen.dart';

class ProfileScreen extends StatelessWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    return Scaffold(
      appBar: DayPilotTopBarWithActions(
        title: l10n.navPerfil,
        actions: [
          IconButton(
            icon: const Icon(Icons.settings_outlined),
            onPressed: () => Navigator.push(
              context,
              MaterialPageRoute(builder: (_) => const SettingsScreen()),
            ),
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 16, 16, 40),
        children: const [
          ProfileStatsCard(
            name: AppData.currentUserName,
            username: AppData.currentUserUsername,
            level: AppData.currentUserLevel,
            currentXp: AppData.currentUserXp,
            xpToNextLevel: AppData.currentUserXpToNextLevel,
            totalPoints: AppData.currentUserTotalPoints,
            streak: AppData.currentUserStreak,
            bestStreak: AppData.currentUserBestStreak,
          ),
          SizedBox(height: 16),

          ProfileInfoCard(
            username: AppData.currentUserUsername,
            email: AppData.currentUserEmail,
            memberSince: AppData.currentUserMemberSince,
          ),
          SizedBox(height: 16),

          DailyPointsCard(
            rankingPosition: AppData.rankingPositionToday,
            pointsToday: AppData.pointsToday,
            pointsFromTasks: AppData.pointsTodayFromTasks,
            pointsFromSteps: AppData.pointsTodayFromSteps,
            pointsFromHabits: AppData.pointsTodayFromHabits,
            pointsFromTimer: AppData.pointsTodayFromTimer,
          ),
          SizedBox(height: 16),

          WeeklyReactionCard(
            weekLabel: AppData.lastWeekLabel,
            points: AppData.lastWeekPoints,
            steps: AppData.lastWeekSteps,
            tasks: AppData.lastWeekTasks,
            streak: AppData.lastWeekStreak,
            reactions: AppData.lastWeekReactions,
          ),
        ],
      ),
    );
  }
}
