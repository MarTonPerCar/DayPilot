import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import '../../components/basic/top_bar.dart';
import '../../components/cards/profile_stats_card.dart';
import '../../components/cards/profile_info_card.dart';
import '../../components/cards/daily_points_card.dart';
import '../../components/cards/weekly_reaction_card.dart';
import '../../features/profile/profile_notifier.dart';
import '../../features/profile/weekly_summary_notifier.dart';
import '../../features/progress/progress_notifier.dart';
import '../../l10n/app_localizations.dart';
import 'settings_screen.dart';

class ProfileScreen extends ConsumerWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    final stats = ref.watch(profileStatsNotifierProvider);
    final progress = ref.watch(progressNotifierProvider);
    final weeklySummary = ref.watch(weeklySummaryNotifierProvider);

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
        children: [
          if (stats == null)
            const Padding(
              padding: EdgeInsets.symmetric(vertical: 24),
              child: Center(child: CircularProgressIndicator()),
            )
          else
            ProfileStatsCard(
              name: stats.name,
              username: stats.username,
              avatarUrl: stats.avatarUrl,
              level: stats.level,
              currentXp: stats.currentXp,
              xpToNextLevel: stats.xpToNextLevel,
              totalPoints: stats.totalPoints,
              streak: stats.streak,
              bestStreak: stats.bestStreak,
            ),
          const SizedBox(height: 16),

          if (stats != null)
            ProfileInfoCard(
              username: stats.username,
              email: stats.email,
              memberSince: _formatMemberSince(context, stats.createdAt),
            ),
          const SizedBox(height: 16),

          if (progress != null)
            DailyPointsCard(
              pointsToday: progress.pointsToday,
              pointsFromTasks: progress.pointsFromTasks,
              pointsFromSteps: progress.pointsFromSteps,
              pointsFromHabits: progress.pointsFromHabits,
              pointsFromTimer: progress.pointsFromTimer,
            ),
          const SizedBox(height: 16),

          if (weeklySummary != null)
            WeeklyReactionCard(
              weekLabel: l10n.weeklySummaryLastWeek,
              points: weeklySummary.totalPoints,
              steps: weeklySummary.totalSteps,
              tasks: weeklySummary.tasksCompleted,
              streak: weeklySummary.bestStreak,
              reactions: [
                for (final r in weeklySummary.reactions)
                  WeeklyReaction(name: r.fromName, avatarUrl: r.avatarUrl, emoji: r.emoji),
              ],
            ),
        ],
      ),
    );
  }

  String _formatMemberSince(BuildContext context, DateTime date) {
    final locale = Localizations.localeOf(context).languageCode;
    return DateFormat.yMMM(locale).format(date).toLowerCase();
  }
}
