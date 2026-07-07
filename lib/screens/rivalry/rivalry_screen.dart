import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../components/basic/top_bar.dart';
import '../../components/cards/ranking_card.dart';
import '../../features/rivalry/ranking_notifier.dart';
import '../../l10n/app_localizations.dart';

class RivalryScreen extends ConsumerWidget {
  const RivalryScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);
    final ranking = ref.watch(rankingNotifierProvider);

    return Scaffold(
      appBar: DayPilotTopBar(title: l10n.rivalryTitle, showBack: true),
      body: ranking.isEmpty
          ? Center(child: Text(l10n.rivalryEmpty))
          : ListView(
              padding: const EdgeInsets.fromLTRB(16, 8, 16, 32),
              children: [
                Row(
                  children: [
                    Icon(Icons.calendar_month_rounded, size: 16, color: colors.onSurfaceVariant),
                    const SizedBox(width: 6),
                    Text(
                      l10n.rivalryPointsThisMonth,
                      style: text.labelMedium?.copyWith(
                        color: colors.onSurfaceVariant,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 16),

                if (ranking.length >= 3) ...[
                  PodiumCard(
                    firstName: ranking[0].name,
                    firstAvatar: ranking[0].avatarUrl,
                    firstPoints: ranking[0].points,
                    secondName: ranking[1].name,
                    secondAvatar: ranking[1].avatarUrl,
                    secondPoints: ranking[1].points,
                    thirdName: ranking[2].name,
                    thirdAvatar: ranking[2].avatarUrl,
                    thirdPoints: ranking[2].points,
                  ),
                  const SizedBox(height: 20),
                ],

                Text(
                  l10n.rivalryFullRanking,
                  style: text.labelSmall?.copyWith(
                    color: colors.primary,
                    letterSpacing: 1.2,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                const SizedBox(height: 10),

                ...ranking.asMap().entries.map((e) {
                  final i = e.key;
                  final u = e.value;
                  return RankingCard(
                    position: i + 1,
                    username: u.name,
                    avatarUrl: u.avatarUrl,
                    points: u.points,
                    streak: u.streak,
                    isCurrentUser: u.isCurrentUser,
                  );
                }),
              ],
            ),
    );
  }
}
