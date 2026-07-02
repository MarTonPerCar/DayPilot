import 'package:flutter/material.dart';
import '../../components/basic/top_bar.dart';
import '../../components/basic/filter_selector.dart';
import '../../components/cards/ranking_card.dart';

class RankingScreen extends StatefulWidget {
  const RankingScreen({super.key});

  @override
  State<RankingScreen> createState() => _RankingScreenState();
}

class _RankingScreenState extends State<RankingScreen> {
  String _period = 'Semanal';
  static const _periods = ['Semanal', 'Mensual', 'Global'];

  static const _users = [
    _UserData('mario_garcia', 2840, 12, true),
    _UserData('ana_lopez', 2610, 8, false),
    _UserData('carlos_ruiz', 2290, 5, false),
    _UserData('lucia_fdez', 1870, 3, false),
    _UserData('pedro_gz', 1640, 7, false),
    _UserData('sofia_mn', 1420, 2, false),
    _UserData('javier_ps', 1180, 0, false),
    _UserData('elena_vg', 950, 4, false),
  ];

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    return Scaffold(
      appBar: DayPilotTopBarWithActions(
        title: 'Ranking',
        actions: [
          IconButton(icon: const Icon(Icons.share_outlined), onPressed: () {}),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 32),
        children: [
          // Period filter
          DayPilotFilterSelector<String>(
            options: _periods,
            selected: _period,
            label: (s) => s,
            onSelected: (p) => setState(() => _period = p),
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
            'CLASIFICACIÓN COMPLETA',
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

class _UserData {
  final String name;
  final int points;
  final int streak;
  final bool isCurrentUser;
  const _UserData(this.name, this.points, this.streak, this.isCurrentUser);
}
