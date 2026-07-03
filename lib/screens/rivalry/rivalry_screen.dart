import 'package:flutter/material.dart';
import '../../components/basic/top_bar.dart';
import '../../components/cards/ranking_card.dart';
import '../../data/app_data.dart';
import '../../l10n/app_localizations.dart';

class RivalryScreen extends StatelessWidget {
  const RivalryScreen({super.key});

  // Solo amigos: la rivalidad no tiene clasificación global.
  // Se puntúa únicamente por puntos acumulados en el mes en curso.
  static const _users = AppData.rankingUsers;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);

    return Scaffold(
      appBar: DayPilotTopBar(title: l10n.rivalryTitle, showBack: true),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 32),
        children: [
          // Ámbito: puntos acumulados este mes, solo entre amigos.
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

          // Podium
          PodiumCard(
            firstName: _users[0].name,
            firstPoints: _users[0].points,
            secondName: _users[1].name,
            secondPoints: _users[1].points,
            thirdName: _users[2].name,
            thirdPoints: _users[2].points,
          ),
          const SizedBox(height: 20),

          // Full list
          Text(
            l10n.rivalryFullRanking,
            style: text.labelSmall?.copyWith(
              color: colors.primary,
              letterSpacing: 1.2,
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 10),

          ..._users.asMap().entries.map((e) {
            final i = e.key;
            final u = e.value;
            return RankingCard(
              position: i + 1,
              username: u.name,
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
