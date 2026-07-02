import 'package:flutter/material.dart';
import '../../components/basic/top_bar.dart';
import '../../components/basic/filter_selector.dart';
import '../../components/cards/friend_card.dart';
import '../../components/cards/ranking_card.dart';

class FriendsScreen extends StatefulWidget {
  const FriendsScreen({super.key});

  @override
  State<FriendsScreen> createState() => _FriendsScreenState();
}

class _FriendsScreenState extends State<FriendsScreen> {
  String _tab = 'Amigos';
  static const _tabs = ['Amigos', 'Ranking', 'Buscar'];

  static const _friends = [
    _Friend('ana_lopez', 2610, 8, FriendStatus.accepted),
    _Friend('carlos_ruiz', 2290, 5, FriendStatus.accepted),
    _Friend('lucia_fdez', 1870, 3, FriendStatus.accepted),
    _Friend('sofia_mn', 1420, 2, FriendStatus.pendingReceived),
    _Friend('javier_ps', 1180, 0, FriendStatus.pendingSent),
  ];

  static const _ranking = [
    _Friend('mario_garcia', 2840, 12, FriendStatus.accepted),
    _Friend('ana_lopez', 2610, 8, FriendStatus.accepted),
    _Friend('carlos_ruiz', 2290, 5, FriendStatus.accepted),
    _Friend('lucia_fdez', 1870, 3, FriendStatus.accepted),
    _Friend('sofia_mn', 1420, 2, FriendStatus.accepted),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: DayPilotTopBarWithActions(
        title: 'Amigos',
        actions: [
          IconButton(
            icon: const Icon(Icons.person_add_outlined),
            onPressed: () => setState(() => _tab = 'Buscar'),
          ),
        ],
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 8, 16, 0),
            child: DayPilotFilterSelector<String>(
              options: _tabs,
              selected: _tab,
              label: (s) => s,
              onSelected: (t) => setState(() => _tab = t),
            ),
          ),
          Expanded(
            child: _tab == 'Ranking'
                ? _buildRanking()
                : _tab == 'Buscar'
                    ? _buildSearch()
                    : _buildFriends(),
          ),
        ],
      ),
    );
  }

  Widget _buildFriends() {
    return ListView.separated(
      padding: const EdgeInsets.fromLTRB(16, 12, 16, 32),
      itemCount: _friends.length,
      separatorBuilder: (_, __) => const SizedBox(height: 8),
      itemBuilder: (ctx, i) {
        final f = _friends[i];
        return FriendCard(
          username: f.username,
          status: f.status,
          onAccept: f.status == FriendStatus.pendingReceived ? () {} : null,
          onDecline: f.status == FriendStatus.pendingReceived ? () {} : null,
        );
      },
    );
  }

  Widget _buildRanking() {
    return ListView(
      padding: const EdgeInsets.fromLTRB(16, 12, 16, 32),
      children: [
        PodiumCard(
          firstName: _ranking[0].username,
          firstPoints: _ranking[0].points,
          secondName: _ranking[1].username,
          secondPoints: _ranking[1].points,
          thirdName: _ranking[2].username,
          thirdPoints: _ranking[2].points,
        ),
        const SizedBox(height: 16),
        ..._ranking.asMap().entries.map((e) => Padding(
              padding: const EdgeInsets.only(bottom: 8),
              child: RankingCard(
                position: e.key + 1,
                username: e.value.username,
                points: e.value.points,
                streak: e.value.streak,
                isCurrentUser: e.key == 0,
              ),
            )),
      ],
    );
  }

  Widget _buildSearch() {
    return ListView(
      padding: const EdgeInsets.fromLTRB(16, 12, 16, 32),
      children: [
        const UserSearchCard(username: 'elena_vg'),
        const SizedBox(height: 8),
        const UserSearchCard(username: 'pedro_gz'),
      ],
    );
  }
}

class _Friend {
  final String username;
  final int points;
  final int streak;
  final FriendStatus status;
  const _Friend(this.username, this.points, this.streak, this.status);
}
